package org.treeWare.mySql.operator.delegate

import org.lighthousegames.logging.logging
import org.treeWare.metaModel.*
import org.treeWare.model.core.*
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.encodeJson
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.SetEntityDelegate
import org.treeWare.model.operator.getAssociationTargetEntity
import org.treeWare.model.operator.set.SetDelegate
import org.treeWare.model.operator.set.aux.SetAux
import org.treeWare.mySql.operator.CREATED_ON_COLUMN_NAME
import org.treeWare.mySql.operator.ENTITY_PATH_COLUMN_NAME
import org.treeWare.mySql.operator.UPDATED_ON_COLUMN_NAME
import org.treeWare.mySql.util.getEntityTableFullName
import org.treeWare.mySql.util.getEntityTableName
import org.treeWare.util.assertInDevMode
import java.io.StringWriter
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

    init {
        if (connection != null && connection.autoCommit) {
            throw IllegalStateException("SQL connection should not be in auto-commit mode")
        }
    }

    override fun begin(): List<String> {
        // Use the same value of now for all entities in the model.
        now = nowAsIso8601(clock)
        return emptyList()
    }

    override fun setEntity(
        setAux: SetAux,
        entity: EntityModel,
        entityPath: String,
        ancestorKeys: List<Keys>,
        keys: List<SingleFieldModel>,
        associations: List<FieldModel>,
        other: List<FieldModel>
    ): List<String> {
        val tableName = getEntityTableFullName(entity)
        when (setAux) {
            SetAux.CREATE -> addCreateCommands(tableName, entityPath, ancestorKeys, keys, associations, other)
            SetAux.UPDATE -> addUpdateCommands(tableName, entityPath, keys, associations, other)
            SetAux.DELETE -> addDeleteCommands(tableName, entityPath, keys, entity)
        }
        return emptyList()
    }

    override fun end(): List<String> {
        return issueCommands()
    }

    private fun addCreateCommands(
        tableName: String,
        entityPath: String,
        ancestorKeys: List<Keys>,
        keys: List<SingleFieldModel>,
        associations: List<FieldModel>,
        other: List<FieldModel>
    ) {
        val insertBuilder = InsertCommandBuilder(tableName)
        val updateBuilder = UpdateCommandBuilder(tableName)
        insertBuilder.addColumn(false, null, CREATED_ON_COLUMN_NAME, now, Preprocess.QUOTE)
        insertBuilder.addColumn(false, null, UPDATED_ON_COLUMN_NAME, now, Preprocess.QUOTE)
        insertBuilder.addColumn(false, null, ENTITY_PATH_COLUMN_NAME, entityPath, Preprocess.ESCAPE)
        addAncestorKeys(ancestorKeys, insertBuilder)
        keys.forEach { addField(true, null, it, insertBuilder) }
        other.forEach { addField(false, null, it, insertBuilder) }
        createCompositionCommands.add(EntitySqlCommand("create", entityPath, insertBuilder.build()))
        if (associations.isNotEmpty()) {
            // TODO(performance): set updated-on & keys in updateBuilder at the same time as insertBuilder.
            updateBuilder.addColumn(false, null, UPDATED_ON_COLUMN_NAME, now, Preprocess.QUOTE)
            keys.forEach { addField(true, null, it, updateBuilder) }
            // `UpdateCommandBuilder` adds only keys to the WHERE clause. The `entityPath` needs to be in the WHERE clause,
            // so `isKey` is set to `true` for it.
            updateBuilder.addColumn(true, null, ENTITY_PATH_COLUMN_NAME, entityPath, Preprocess.ESCAPE)
            associations.forEach { addField(false, null, it, updateBuilder) }
            createAssociationCommands.add(EntitySqlCommand("create association in", entityPath, updateBuilder.build()))
        }
    }

    private fun addUpdateCommands(
        tableName: String,
        entityPath: String,
        keys: List<SingleFieldModel>,
        associations: List<FieldModel>,
        other: List<FieldModel>
    ) {
        val updateBuilder = UpdateCommandBuilder(tableName)
        updateBuilder.addColumn(false, null, UPDATED_ON_COLUMN_NAME, now, Preprocess.QUOTE)
        keys.forEach { addField(true, null, it, updateBuilder) }
        // `UpdateCommandBuilder` adds only keys to the WHERE clause. The `entityPath` needs to be in the WHERE clause,
        // so `isKey` is set to `true` for it.
        updateBuilder.addColumn(true, null, ENTITY_PATH_COLUMN_NAME, entityPath, Preprocess.ESCAPE)
        associations.forEach { addField(false, null, it, updateBuilder) }
        other.forEach { addField(false, null, it, updateBuilder) }
        updateCompositionCommands.add(EntitySqlCommand("update", entityPath, updateBuilder.build()))
    }

    private fun addDeleteCommands(
        tableName: String,
        entityPath: String,
        keys: List<SingleFieldModel>,
        entity: EntityModel
    ) {
        // Associations can prevent deletion, so they need to be cleared first.
        val entityMeta = entity.meta ?: throw IllegalStateException("Meta is missing for entity $entityPath")
        val associationFieldsMeta =
            getFieldsMeta(entityMeta).values.filter { isAssociationFieldMeta(it as EntityModel) }
        if (associationFieldsMeta.isNotEmpty()) {
            val updateBuilder = UpdateCommandBuilder(tableName)
            keys.forEach { addField(true, null, it, updateBuilder) }
            // `UpdateCommandBuilder` adds only keys to the WHERE clause. The `entityPath` needs to be in the WHERE clause,
            // so `isKey` is set to `true` for it.
            updateBuilder.addColumn(true, null, ENTITY_PATH_COLUMN_NAME, entityPath, Preprocess.ESCAPE)
            associationFieldsMeta.forEach { clearAssociationColumns(it as EntityModel, updateBuilder) }
            // `rowCount` is `null` to allow delete-related commands to pass even if they don't match any rows.
            deleteAssociationCommands.add(
                EntitySqlCommand("clear associations in", entityPath, updateBuilder.build(), null)
            )
        }

        val deleteBuilder = DeleteCommandBuilder(tableName)
        keys.forEach { addField(true, null, it, deleteBuilder) }
        // Keys are used in the WHERE clause in a delete command. The entityPath needs to be in the WHERE clause,
        // so isKey is set to true for it.
        deleteBuilder.addColumn(true, null, ENTITY_PATH_COLUMN_NAME, entityPath, Preprocess.ESCAPE)
        // Issue delete commands in reverse order (by using addFirst() to add new delete commands) so that leaf entities
        // get deleted before their ancestors. i.e. bottoms-up. Top-down will not work due to foreign-key constraints.
        // `rowCount` is `null` to allow delete-related commands to pass even if they don't match any rows.
        deleteCompositionCommands.addFirst(EntitySqlCommand("delete", entityPath, deleteBuilder.build(), null))
    }

    private fun addAncestorKeys(ancestorKeys: List<Keys>, builder: SetCommandBuilder) {
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
            ancestor.available.forEach { addField(false, ancestorTableName, it, builder) }
            previousAncestorTableName = ancestorTableName
        }
    }

    private fun addField(
        isKey: Boolean,
        namePrefix: String?,
        field: FieldModel,
        builder: SetCommandBuilder
    ) {
        if (isListField(field)) addSqlJsonValue(isKey, field as ListFieldModel, builder)
        else when (val fieldType = getFieldType(field)) {
            FieldType.ASSOCIATION -> addAssociationColumns(isKey, field as SingleFieldModel, builder)
            FieldType.COMPOSITION -> addCompositionColumn(isKey, field as SingleFieldModel, builder)
            else -> addSqlValue(isKey, namePrefix, fieldType, field as SingleFieldModel, builder)
        }
    }

    private fun addCompositionColumn(isKey: Boolean, field: SingleFieldModel, builder: SetCommandBuilder) {
        val compositionMeta = getMetaModelResolved(field.meta)?.compositionMeta
        val entityFullName = getMetaModelResolved(compositionMeta)?.fullName
        val entityDelegate = entityDelegates?.get(entityFullName)
        if (entityDelegate?.isSingleValue() == true) {
            entityDelegate.getSingleValue(field.value as EntityModel)?.also {
                builder.addColumn(isKey, null, getFieldName(field), it)
            }
        } else throw IllegalStateException("Adding composition as a single column")
    }

    private fun issueCommands(): List<String> {
        if (connection == null) return emptyList()
        val errors = mutableListOf<String>()
        val statement = connection.createStatement()
        commands.forEach { command ->
            if (logCommands) logger.info { command }
            try {
                val rowCount = statement.executeUpdate(command.sql)
                if (command.rowCount != null && rowCount != command.rowCount) {
                    errors.add("Unable to ${command.action} ${command.entityPath}")
                }
            } catch (e: SQLException) {
                errors.add("Unable to ${command.action} ${command.entityPath}: ${getMySqlErrorMessage(e)}")
            }
        }
        statement.close()
        if (errors.isEmpty()) connection.commit() else connection.rollback()
        return errors
    }

    private var now = ""
    private val deleteAssociationCommands = mutableListOf<EntitySqlCommand>()
    private val deleteCompositionCommands = ArrayDeque<EntitySqlCommand>()
    private val createCompositionCommands = mutableListOf<EntitySqlCommand>()
    private val createAssociationCommands = mutableListOf<EntitySqlCommand>()
    private val updateCompositionCommands = mutableListOf<EntitySqlCommand>()
}

private enum class EmptyJsonPolicy { USE_NULL, EXCLUDE }

private fun addSqlJsonValue(
    isKey: Boolean,
    field: FieldModel,
    builder: SetCommandBuilder,
    emptyJsonPolicy: EmptyJsonPolicy = EmptyJsonPolicy.USE_NULL
) {
    val writer = StringWriter()
    encodeJson(field, writer, encodePasswords = EncodePasswords.HASHED_AND_ENCRYPTED)
    val jsonValue = writer.toString()
    if (jsonValue.isNotEmpty() || emptyJsonPolicy != EmptyJsonPolicy.EXCLUDE) {
        builder.addColumn(isKey, null, getFieldName(field), jsonValue, Preprocess.QUOTE)
    }
}

private fun addSqlValue(
    isKey: Boolean,
    namePrefix: String?,
    fieldType: FieldType,
    field: SingleFieldModel,
    builder: SetCommandBuilder,
) {
    val columnName = getFieldName(field)
    when (fieldType) {
        FieldType.BOOLEAN,
        FieldType.UINT8,
        FieldType.UINT16,
        FieldType.UINT32,
        FieldType.UINT64,
        FieldType.INT8,
        FieldType.INT16,
        FieldType.INT32,
        FieldType.INT64,
        FieldType.FLOAT,
        FieldType.DOUBLE,
        FieldType.BIG_INTEGER,
        FieldType.BIG_DECIMAL -> builder.addColumn(isKey, namePrefix, columnName, (field.value as PrimitiveModel).value)
        FieldType.TIMESTAMP -> builder.addColumn(
            isKey,
            namePrefix,
            columnName,
            timestampMillisecondsAsIso8601((field.value as PrimitiveModel).value as Long),
            Preprocess.QUOTE
        )
        FieldType.STRING -> builder.addColumn(
            isKey,
            namePrefix,
            columnName,
            (field.value as PrimitiveModel).value as String,
            Preprocess.ESCAPE
        )
        FieldType.UUID -> builder.addColumn(
            isKey,
            namePrefix,
            columnName,
            (field.value as PrimitiveModel).value as String,
            Preprocess.UUID_TO_BIN
        )
        FieldType.BLOB -> builder.addColumn(
            isKey,
            namePrefix,
            columnName,
            (field.value as PrimitiveModel).value as ByteArray,
            Preprocess.TO_HEX
        )
        FieldType.PASSWORD1WAY,
        FieldType.PASSWORD2WAY -> addSqlJsonValue(isKey, field, builder, EmptyJsonPolicy.EXCLUDE)
        FieldType.ALIAS -> return
        FieldType.ENUMERATION -> builder.addColumn(
            isKey,
            namePrefix,
            columnName,
            (field.value as EnumerationModel).number
        )
        FieldType.ASSOCIATION -> throw IllegalStateException("Adding association as a single SQL value")
        FieldType.COMPOSITION -> throw IllegalStateException("Adding composition as a single SQL value")
    }
}

private fun addAssociationColumns(isKey: Boolean, field: SingleFieldModel, builder: SetCommandBuilder) {
    if (isKey) throw IllegalStateException("Associations are not yet supported as keys")
    val target = getAssociationTargetEntity(field.value as AssociationModel)
    val keys = target.getKeyFields(true)
    assertInDevMode(keys.missing.isEmpty())
    val fieldName = getFieldName(field)
    keys.available.forEach { key ->
        addSqlValue(false, fieldName, getFieldType(key), key, builder)
    }
}

private fun clearAssociationColumns(fieldMeta: EntityModel, builder: SetCommandBuilder) {
    val fieldName = getMetaName(fieldMeta)
    if (isListFieldMeta(fieldMeta)) {
        builder.addColumn(false, null, fieldName, null)
        return
    }
    val targetEntityMeta = getMetaModelResolved(fieldMeta)?.associationMeta?.targetEntityMeta
        ?: throw IllegalStateException("Association meta-model is not resolved")
    val targetKeyFieldsMeta = getKeyFieldsMeta(targetEntityMeta)
    targetKeyFieldsMeta.forEach { targetKeyFieldMeta ->
        val targetKeyName = getMetaName(targetKeyFieldMeta)
        builder.addColumn(false, fieldName, targetKeyName, null)
    }
}

private fun nowAsIso8601(clock: Clock): String =
    // Convert to milliseconds first so that microseconds are not in the resulting string.
    timestampMillisecondsAsIso8601(Instant.now(clock).toEpochMilli())

private fun timestampMillisecondsAsIso8601(timestampMilliseconds: Long): String =
    instantAsIso8601(Instant.ofEpochMilli(timestampMilliseconds))

private fun instantAsIso8601(instant: Instant): String =
    // TODO(deepak-nulu): Timezone offsets are supported only in MySQL 8.0.19
    instant.toString()
        // Instant.ofEpochMilli() returns UTC timezone as the character 'Z' at the end.
        // drop it until the test upgrades to MySQL 8.0.19 or later.
        .let { if (it.endsWith('Z')) it.dropLast(1) else it }

private fun getMySqlErrorMessage(e: SQLException): String = when (e.errorCode) {
    1062 -> "duplicate"
    1451 -> "has children or source entity"
    1452 -> "no parent or target entity"
    else -> "error code ${e.errorCode}"
}

private val logger = logging()