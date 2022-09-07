package org.treeWare.mySql.operator.delegate

import org.lighthousegames.logging.logging
import org.treeWare.metaModel.*
import org.treeWare.model.core.*
import org.treeWare.model.operator.ElementModelError
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.ErrorCode
import org.treeWare.model.operator.SetEntityDelegate
import org.treeWare.model.operator.set.SetDelegate
import org.treeWare.model.operator.set.SetResponse
import org.treeWare.model.operator.set.aux.SetAux
import org.treeWare.mySql.operator.*
import org.treeWare.mySql.util.getEntityMetaTableName
import org.treeWare.mySql.util.getEntityTableFullName
import org.treeWare.mySql.util.getEntityTableName
import java.sql.Connection
import java.sql.SQLException
import java.time.Clock
import java.time.Instant

internal data class EntitySqlCommand(
    val action: String,
    val entityPath: String,
    val sql: String,
    val rowCount: Int? = 1 // not checked if null
)

/**
 * Creates and issues set-commands for MySQL.
 * The commands are issued only if a connection is specified.
 */
internal class MySqlSetDelegate(
    mainMeta: MainModel,
    private val entityDelegates: EntityDelegateRegistry<SetEntityDelegate>?,
    private val connection: Connection?,
    private val logCommands: Boolean = false,
    private val clock: Clock = Clock.systemUTC()
) : SetDelegate {
    // NOTE: the order of commands is important since MySQL does not support deferring constraint checks
    // to the end of the transaction.
    // 1) Recursive associations need to be cleared before entities can be deleted
    // 2) Entities should be deleted before entities are created or updated to ensure associations to them are not made.
    // 3) Entities should be created before old entities are updated in case the latter creates associations to the new
    //    entities.
    // 4) Associations in the new entities need to be created after entities are created else forward-associations will
    //    fail.
    // 5) Existing entities must be updated last so that association updates will succeed (or fail if they point to
    //    deleted entities).
    val commands: List<EntitySqlCommand>
        get() = deleteAssociationCommands +
                deleteCompositionCommands +
                createCompositionCommands +
                createAssociationCommands +
                updateCompositionCommands

    private val rootTableName: String

    init {
        if (connection != null && connection.autoCommit) {
            throw IllegalStateException("SQL connection should not be in auto-commit mode")
        }
        val rootMeta = getRootMeta(mainMeta)
        val rootEntityMeta = getMetaModelResolved(rootMeta)?.compositionMeta
            ?: throw IllegalStateException("Root is not resolved")
        rootTableName = getEntityMetaTableName(rootEntityMeta)
    }

    override fun begin(): SetResponse {
        // Use the same value of now for all entities in the model.
        now = nowAsIso8601(clock)
        return SetResponse.Success
    }

    override fun setEntity(
        setAux: SetAux,
        entity: EntityModel,
        fieldPath: String,
        entityPath: String,
        ancestorKeys: List<Keys>,
        keys: List<SingleFieldModel>,
        associations: List<FieldModel>,
        other: List<FieldModel>
    ): SetResponse {
        val tableName = getEntityTableFullName(entity)
        when (setAux) {
            SetAux.CREATE -> addCreateCommands(
                tableName,
                fieldPath,
                entityPath,
                ancestorKeys,
                keys,
                associations,
                other
            )
            SetAux.UPDATE -> addUpdateCommands(tableName, fieldPath, entityPath, keys, associations, other)
            SetAux.DELETE -> addDeleteCommands(tableName, fieldPath, entityPath, keys, entity)
        }
        return SetResponse.Success
    }

    override fun end(): SetResponse = issueCommands()

    private fun addCreateCommands(
        tableName: String,
        fieldPath: String,
        entityPath: String,
        ancestorKeys: List<Keys>,
        keys: List<SingleFieldModel>,
        associations: List<FieldModel>,
        other: List<FieldModel>
    ) {
        val insertBuilder = InsertCommandBuilder(tableName)
        val updateBuilder = UpdateCommandBuilder(tableName)
        insertBuilder.addColumn(SqlColumn(null, CREATED_ON_COLUMN_NAME, now, Preprocess.QUOTE))
        insertBuilder.addColumn(SqlColumn(null, UPDATED_ON_COLUMN_NAME, now, Preprocess.QUOTE))
        insertBuilder.addColumn(SqlColumn(null, FIELD_PATH_COLUMN_NAME, fieldPath, Preprocess.ESCAPE))
        val ancestorKeyCount = addAncestorKeys(ancestorKeys, insertBuilder)
        if (ancestorKeyCount == 0) {
            val isRoot = ancestorKeys.size == 1
            val namePrefix = if (isRoot) null else rootTableName
            insertBuilder.addColumn(SqlColumn(namePrefix, SINGLETON_KEY_COLUMN_NAME, SINGLETON_KEY_COLUMN_VALUE))
        }
        keys.forEach { insertBuilder.addColumns(getSqlColumns(null, it, entityDelegates)) }
        other.forEach { insertBuilder.addColumns(getSqlColumns(null, it, entityDelegates)) }
        createCompositionCommands.add(EntitySqlCommand("create", entityPath, insertBuilder.build()))
        if (associations.isNotEmpty()) {
            // TODO(performance): set updated-on & keys in updateBuilder at the same time as insertBuilder.
            updateBuilder.addUpdateColumn(SqlColumn(null, UPDATED_ON_COLUMN_NAME, now, Preprocess.QUOTE))
            keys.forEach { updateBuilder.addWhereColumns(getSqlColumns(null, it, entityDelegates)) }
            updateBuilder.addWhereColumn(SqlColumn(null, FIELD_PATH_COLUMN_NAME, fieldPath, Preprocess.ESCAPE))
            associations.forEach { updateBuilder.addUpdateColumns(getSqlColumns(null, it, entityDelegates)) }
            createAssociationCommands.add(
                EntitySqlCommand("create association in entity", entityPath, updateBuilder.build())
            )
        }
    }

    private fun addUpdateCommands(
        tableName: String,
        fieldPath: String,
        entityPath: String,
        keys: List<SingleFieldModel>,
        associations: List<FieldModel>,
        other: List<FieldModel>
    ) {
        val updateBuilder = UpdateCommandBuilder(tableName)
        updateBuilder.addUpdateColumn(SqlColumn(null, UPDATED_ON_COLUMN_NAME, now, Preprocess.QUOTE))
        keys.forEach { updateBuilder.addWhereColumns(getSqlColumns(null, it, entityDelegates)) }
        updateBuilder.addWhereColumn(SqlColumn(null, FIELD_PATH_COLUMN_NAME, fieldPath, Preprocess.ESCAPE))
        associations.forEach { updateBuilder.addUpdateColumns(getSqlColumns(null, it, entityDelegates)) }
        other.forEach { updateBuilder.addUpdateColumns(getSqlColumns(null, it, entityDelegates)) }
        updateCompositionCommands.add(EntitySqlCommand("update", entityPath, updateBuilder.build()))
    }

    private fun addDeleteCommands(
        tableName: String,
        fieldPath: String,
        entityPath: String,
        keys: List<SingleFieldModel>,
        entity: EntityModel
    ) {
        // Associations can prevent deletion, so they need to be cleared first.
        val entityMeta = entity.meta ?: throw IllegalStateException("Meta is missing for entity $fieldPath")
        val associationFieldsMeta =
            getFieldsMeta(entityMeta).values.filter { isAssociationFieldMeta(it as EntityModel) }
        if (associationFieldsMeta.isNotEmpty()) {
            val updateBuilder = UpdateCommandBuilder(tableName)
            keys.forEach { updateBuilder.addWhereColumns(getSqlColumns(null, it, entityDelegates)) }
            updateBuilder.addWhereColumn(SqlColumn(null, FIELD_PATH_COLUMN_NAME, fieldPath, Preprocess.ESCAPE))
            associationFieldsMeta.forEach { clearAssociationColumns(it as EntityModel, updateBuilder) }
            // `rowCount` is `null` to allow delete-related commands to pass even if they don't match any rows.
            deleteAssociationCommands.add(
                EntitySqlCommand("clear associations in", entityPath, updateBuilder.build(), null)
            )
        }

        val deleteBuilder = DeleteCommandBuilder(tableName)
        keys.forEach { deleteBuilder.addWhereColumns(getSqlColumns(null, it, entityDelegates)) }
        deleteBuilder.addWhereColumn(SqlColumn(null, FIELD_PATH_COLUMN_NAME, fieldPath, Preprocess.ESCAPE))
        // Issue delete commands in reverse order (by using addFirst() to add new delete commands) so that leaf entities
        // get deleted before their ancestors. i.e. bottoms-up. Top-down will not work due to foreign-key constraints.
        // `rowCount` is `null` to allow delete-related commands to pass even if they don't match any rows.
        deleteCompositionCommands.addFirst(EntitySqlCommand("delete", entityPath, deleteBuilder.build(), null))
    }

    /** Adds ancestor keys and returns the number of ancestor keys added. */
    private fun addAncestorKeys(ancestorKeys: List<Keys>, builder: InsertCommandBuilder): Int {
        var ancestorKeyCount = 0
        // When there are recursive entities, record only the first and skip the remaining.
        // Recursive entities will be next to each other and have the same table name.
        // The following variable helps identify recursive entities that should be skipped.
        var previousAncestorTableName: String? = null
        ancestorKeys.forEachIndexed { index, ancestor ->
            // First Keys is current entity's keys, not an ancestor's.
            if (index == 0) return@forEachIndexed
            val ancestorFirstKey = ancestor.available.firstOrNull() ?: return@forEachIndexed
            val ancestorEntity = ancestorFirstKey.parent
                ?: throw IllegalStateException("Ancestor key does not have parent entity")
            val ancestorTableName = getEntityTableName(ancestorEntity)
            if (ancestorTableName == previousAncestorTableName) return@forEachIndexed
            ancestor.available.forEach {
                builder.addColumns(getSqlColumns(ancestorTableName, it, entityDelegates))
                ++ancestorKeyCount
            }
            previousAncestorTableName = ancestorTableName
        }
        return ancestorKeyCount
    }

    private fun issueCommands(): SetResponse {
        if (connection == null) return SetResponse.Success
        val errors = mutableListOf<ElementModelError>()
        val statement = connection.createStatement()
        commands.forEach { command ->
            if (logCommands) logger.info { command }
            try {
                val rowCount = statement.executeUpdate(command.sql)
                if (command.rowCount != null && rowCount != command.rowCount) {
                    errors.add(ElementModelError(command.entityPath, "unable to ${command.action}"))
                }
            } catch (e: SQLException) {
                val reason = getReadableReason(e)
                errors.add(ElementModelError(command.entityPath, "unable to ${command.action}: $reason"))
            }
        }
        statement.close()
        return if (errors.isEmpty()) {
            if (logCommands) logger.info { "commit" }
            connection.commit()
            SetResponse.Success
        } else {
            if (logCommands) logger.info { "rollback" }
            connection.rollback()
            SetResponse.ErrorList(ErrorCode.CLIENT_ERROR, errors)
        }
    }

    private var now = ""
    private val deleteAssociationCommands = mutableListOf<EntitySqlCommand>()
    private val deleteCompositionCommands = ArrayDeque<EntitySqlCommand>()
    private val createCompositionCommands = mutableListOf<EntitySqlCommand>()
    private val createAssociationCommands = mutableListOf<EntitySqlCommand>()
    private val updateCompositionCommands = mutableListOf<EntitySqlCommand>()
}

private fun clearAssociationColumns(fieldMeta: EntityModel, builder: UpdateCommandBuilder) {
    val fieldName = getMetaName(fieldMeta)
    if (isListFieldMeta(fieldMeta)) {
        builder.addUpdateColumn(SqlColumn(null, fieldName, null))
        return
    }
    val targetEntityMeta = getMetaModelResolved(fieldMeta)?.associationMeta?.targetEntityMeta
        ?: throw IllegalStateException("Association meta-model is not resolved")
    val targetKeyFieldsMeta = getKeyFieldsMeta(targetEntityMeta)
    targetKeyFieldsMeta.forEach { targetKeyFieldMeta ->
        val targetKeyName = getMetaName(targetKeyFieldMeta)
        builder.addUpdateColumn(SqlColumn(fieldName, targetKeyName, null))
    }
}

private fun nowAsIso8601(clock: Clock): String =
    // Convert to milliseconds first so that microseconds are not in the resulting string.
    timestampMillisecondsAsIso8601(Instant.now(clock).toEpochMilli())

private fun getReadableReason(e: SQLException): String = when (e.errorCode) {
    1062 -> "duplicate"
    1451 -> "has children or source entity"
    1452 -> "no parent or target entity"
    else -> "error code ${e.errorCode}"
}

private val logger = logging()