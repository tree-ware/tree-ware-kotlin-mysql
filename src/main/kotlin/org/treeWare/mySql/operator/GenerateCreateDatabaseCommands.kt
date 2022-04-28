package org.treeWare.mySql.operator

import org.treeWare.metaModel.*
import org.treeWare.metaModel.traversal.AbstractLeader1MetaModelVisitor
import org.treeWare.metaModel.traversal.metaModelForEach
import org.treeWare.model.core.*
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.OperatorId
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.mySql.aux.getMySqlMetaModelMap
import org.treeWare.mySql.util.getEntityMetaTableName
import java.io.StringWriter
import java.io.Writer

interface GenerateCreateDatabaseCommandsEntityDelegate {
    fun isSingleColumn(): Boolean
    fun getSingleColumnType(): String
}

object GenerateCreateDatabaseCommandsOperatorId : OperatorId<GenerateCreateDatabaseCommandsEntityDelegate>

fun generateCreateDatabaseCommands(
    mainMeta: MainModel,
    entityDelegates: EntityDelegateRegistry<GenerateCreateDatabaseCommandsEntityDelegate>?
): List<String> {
    val visitor = GenerateCreateDatabaseCommandsVisitor(entityDelegates)
    metaModelForEach(mainMeta, visitor)
    return visitor.createCommands + visitor.alterCommands
}

private interface SqlClauses {
    fun getColumns(): List<Column>
    fun writeColumnsTo(writer: Writer)
    fun writeIndexesTo(writer: Writer)
    fun hasForeignKeys(): Boolean
    fun writeForeignKeysTo(writer: Writer)
}

private open class Column(val name: String, val type: String) : SqlClauses {
    override fun getColumns(): List<Column> = listOf(this)

    override fun writeColumnsTo(writer: Writer) {
        writeColumnTo(writer, name, type)
    }

    override fun writeIndexesTo(writer: Writer) {}
    override fun hasForeignKeys(): Boolean = false
    override fun writeForeignKeysTo(writer: Writer) {}
}

private class ForeignKey(
    private val localPrefix: String?,
    private val foreignTable: String,
    private val onDelete: OnDelete?,
    private val isUnique: Boolean = false
) : SqlClauses {
    private val keys = mutableListOf<Column>()

    fun addKey(name: String, type: String) {
        keys.add(Column(name, type))
    }

    override fun getColumns(): List<Column> =
        if (localPrefix == null) keys
        else keys.map { Column("$localPrefix\$${it.name}", it.type) }

    override fun writeColumnsTo(writer: Writer) {
        keys.forEach { writeColumnTo(writer, it.name, it.type, localPrefix) }
    }

    override fun writeIndexesTo(writer: Writer) {
        if (!isUnique) return
        val columns = getColumns()
        val first = columns.firstOrNull() ?: throw IllegalStateException("No columns in foreign-key")
        writer.write(",\n  UNIQUE INDEX ")
        writer.write(first.name)
        writer.write(" (")
        columns.forEachIndexed { index, column ->
            if (index != 0) writer.write(", ")
            writer.write(column.name)
        }
        writer.write(")")
    }

    override fun hasForeignKeys(): Boolean = onDelete != null
    override fun writeForeignKeysTo(writer: Writer) {
        if (onDelete == null) return
        writer.write("  ADD FOREIGN KEY (")
        keys.forEachIndexed { index, column ->
            if (index != 0) writer.write(", ")
            localPrefix?.also {
                writer.write(it)
                writer.write("$")
            }
            writer.write(column.name)
        }
        writer.write(") REFERENCES ")
        writer.write(foreignTable)
        writer.write("(")
        keys.forEachIndexed { index, column ->
            if (index != 0) writer.write(", ")
            writer.write(column.name)
        }
        writer.write(") ON DELETE ")
        writer.write(onDelete.sql)
    }
}

private fun writeColumnTo(writer: Writer, name: String, type: String, namePrefix: String? = null) {
    writer.write(",\n  ")
    namePrefix?.also {
        writer.write(namePrefix)
        writer.write("$")
    }
    writer.write(name)
    writer.write(" ")
    writer.write(type)
}

private class Index(val name: String, val isUnique: Boolean) {
    private val columns = StringBuffer()

    fun addColumn(name: String) {
        if (columns.isNotEmpty()) columns.append(", ")
        columns.append(name)
    }

    override fun toString(): String {
        val unique = if (isUnique) "UNIQUE " else ""
        return "  ${unique}INDEX $name ($columns)"
    }
}

private enum class OnDelete(val sql: String) {
    RESTRICT("RESTRICT"),
    SET_NULL("SET NULL")
}

private interface CreateDatabaseCommandBuilder {
    fun addAncestorClauses(clauses: SqlClauses?)
    fun addFieldClauses(clauses: SqlClauses?)
    fun addPrimaryKey(name: String)
    fun addIndex(index: Index)
}

private class GenerateCreateDatabaseCommandsVisitor(
    private val entityDelegates: EntityDelegateRegistry<GenerateCreateDatabaseCommandsEntityDelegate>?
) : CreateDatabaseCommandBuilder, AbstractLeader1MetaModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val createCommands = mutableListOf<String>()
    val alterCommands = mutableListOf<String>()

    private val ancestorClauses = mutableListOf<SqlClauses>()
    private val fieldClauses = mutableListOf<SqlClauses>()
    private val keys = mutableListOf<String>()
    private val indexes = mutableListOf<Index>()

    private fun clearEntityState() {
        ancestorClauses.clear()
        fieldClauses.clear()
        keys.clear()
        indexes.clear()
    }

    override fun addAncestorClauses(clauses: SqlClauses?) {
        clauses?.let { ancestorClauses.add(it) }
    }

    override fun addFieldClauses(clauses: SqlClauses?) {
        clauses?.let { fieldClauses.add(it) }
    }

    override fun addPrimaryKey(name: String) {
        keys.add(name)
    }

    override fun addIndex(index: Index) {
        indexes.add(index)
    }

    private fun addCreateCommand(tableName: String) {
        val createTableWriter = StringWriter()
        createTableWriter
            .appendLine("CREATE TABLE IF NOT EXISTS $tableName (")
            .append("  ").append(CREATED_ON_COLUMN_NAME)
            .appendLine(" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),")
            .append("  ").append(UPDATED_ON_COLUMN_NAME)
            .appendLine(" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),")
            .append("  $ENTITY_PATH_COLUMN_NAME TEXT")
        ancestorClauses.forEach { it.writeColumnsTo(createTableWriter) }
        fieldClauses.forEach { it.writeColumnsTo(createTableWriter) }
        if (keys.isNotEmpty()) {
            createTableWriter
                .appendLine(",")
                .append("  PRIMARY KEY (")
                .append(keys.joinToString(", "))
                .append(")")
        }
        indexes.forEach {
            createTableWriter.appendLine(",")
            createTableWriter.write(it.toString())
        }
        ancestorClauses.forEach { it.writeIndexesTo(createTableWriter) }
        fieldClauses.forEach { it.writeIndexesTo(createTableWriter) }
        createTableWriter.appendLine().append(") ENGINE = InnoDB;")
        val command = createTableWriter.toString()
        createCommands.add(command)
    }

    private fun addAlterCommand(tableName: String) {
        val foreignKeyClauses =
            ancestorClauses.filter { it.hasForeignKeys() } + fieldClauses.filter { it.hasForeignKeys() }
        if (foreignKeyClauses.isEmpty()) return
        val alterTableWriter = StringWriter()
        alterTableWriter.append("ALTER TABLE $tableName")
        foreignKeyClauses.forEachIndexed { index, clause ->
            val separator = if (index == 0) "\n" else ",\n"
            alterTableWriter.append(separator)
            clause.writeForeignKeysTo(alterTableWriter)
        }
        alterTableWriter.append(";")
        val command = alterTableWriter.toString()
        alterCommands.add(command)
    }

    override fun visitMainMeta(leaderMainMeta1: MainModel): TraversalAction {
        val databaseName = getMySqlMetaModelMap(leaderMainMeta1)?.validated?.fullName
            ?: return TraversalAction.ABORT_TREE
        val command = "CREATE DATABASE IF NOT EXISTS $databaseName;"
        createCommands.add(command)
        return TraversalAction.CONTINUE
    }

    override fun visitEntityMeta(leaderEntityMeta1: EntityModel): TraversalAction {
        clearEntityState()
        // Skip if this entity has not been mapped to MySQL.
        getMySqlMetaModelMap(leaderEntityMeta1)?.validated ?: return TraversalAction.ABORT_SUB_TREE
        addKeyedAncestors(leaderEntityMeta1)
        addUniques(leaderEntityMeta1)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntityMeta(leaderEntityMeta1: EntityModel) {
        // Skip if this entity has not been mapped to MySQL.
        val tableName = getMySqlMetaModelMap(leaderEntityMeta1)?.validated?.fullName ?: return
        addCreateCommand(tableName)
        addAlterCommand(tableName)
    }

    override fun visitFieldMeta(leaderFieldMeta1: EntityModel): TraversalAction {
        val clauses = getFieldClauses(leaderFieldMeta1)
        val isKey = isKeyFieldMeta(leaderFieldMeta1)
        if (isKey) clauses?.getColumns()?.forEach { keys.add(it.name) }
        clauses?.also { addFieldClauses(it) }
        return TraversalAction.CONTINUE
    }

    private fun addKeyedAncestors(entityMeta: EntityModel) {
        val keyedParents = LinkedHashSet<EntityModel>()
        val keyedAncestors = LinkedHashSet<EntityModel>()
        collectKeyedAncestors(entityMeta, false, keyedParents, keyedAncestors)
        keyedAncestors.forEach { addAncestorKeyClauses(it, false, false) }
        keyedParents.forEach { addAncestorKeyClauses(it, true, !hasKeyFields(entityMeta)) }
    }

    private fun collectKeyedAncestors(
        entityMeta: BaseEntityModel,
        isKeyedParentFound: Boolean,
        keyedParents: LinkedHashSet<EntityModel>,
        keyedAncestors: LinkedHashSet<EntityModel>
    ) {
        val resolved = getMetaModelResolved(entityMeta)
            ?: throw IllegalStateException("Resolved aux is missing for entity")
        resolved.parentFieldsMeta.forEach { parentFieldMeta ->
            val parentEntityMeta = getParentEntityMeta(parentFieldMeta) as? EntityModel ?: return@forEach
            if (!isEntityMeta(parentEntityMeta)) return@forEach
            if (keyedParents.contains(parentEntityMeta) || keyedAncestors.contains(parentEntityMeta)) return@forEach
            if (!hasKeyFields(parentEntityMeta)) return@forEach
            if (!isKeyedParentFound) keyedParents.add(parentEntityMeta)
            else keyedAncestors.add(parentEntityMeta)
            collectKeyedAncestors(parentEntityMeta, true, keyedParents, keyedAncestors)
        }
    }

    private fun addAncestorKeyClauses(entityMeta: EntityModel, constrain: Boolean, isUnique: Boolean) {
        val tableName = getEntityMetaTableName(entityMeta)
        val onDelete = if (constrain) OnDelete.RESTRICT else null
        val keyFieldsMeta = getKeyFieldsMeta(entityMeta)
        keyFieldsMeta.forEach { fieldMeta ->
            val foreignKey = ForeignKey(tableName, tableName, onDelete, isUnique)
            val clauses = getFieldClauses(fieldMeta)
            clauses?.getColumns()?.forEach { foreignKey.addKey(it.name, it.type) }
            addAncestorClauses(foreignKey)
        }
    }

    private fun addUniques(entityMeta: EntityModel) {
        getUniquesMeta(entityMeta)?.values?.forEach { addUnique(entityMeta, it) }
    }

    private fun addUnique(entityMeta: EntityModel, uniqueElementMeta: ElementModel) {
        val uniqueMeta = uniqueElementMeta as EntityModel
        val uniqueName = getMetaName(uniqueMeta)
        val index = Index(uniqueName, true)
        getFieldsMeta(uniqueMeta).values.forEach { uniqueFieldElementMeta ->
            val uniqueFieldMeta = uniqueFieldElementMeta as PrimitiveModel
            val entityFieldMeta = getFieldMeta(entityMeta, uniqueFieldMeta.value as String)
            getFieldClauses(entityFieldMeta)?.getColumns()?.forEach { index.addColumn(it.name) }
        }
        addIndex(index)
    }

    private fun getFieldClauses(fieldMeta: EntityModel): SqlClauses? =
        when (getFieldTypeMeta(fieldMeta)) {
            FieldType.COMPOSITION -> {
                val compositionMeta = getMetaModelResolved(fieldMeta)?.compositionMeta
                val entityFullName = getMetaModelResolved(compositionMeta)?.fullName
                val entityDelegate = entityDelegates?.get(entityFullName)
                if (entityDelegate?.isSingleColumn() != true) null
                else getSingleFieldClauses(fieldMeta, entityDelegate.getSingleColumnType())
            }
            FieldType.ASSOCIATION -> getAssociationFieldClauses(fieldMeta)
            else -> getSingleFieldClauses(fieldMeta)
        }
}

private fun getAssociationFieldClauses(fieldMeta: EntityModel): SqlClauses {
    val fieldName = getMetaName(fieldMeta)
    if (isListFieldMeta(fieldMeta)) return getSingleFieldClauses(fieldMeta)
    val targetEntityMeta = getMetaModelResolved(fieldMeta)?.associationMeta?.targetEntityMeta
        ?: throw IllegalStateException("Association meta-model is not resolved")
    val targetAux = getMySqlMetaModelMap(targetEntityMeta)
    val targetTableName = targetAux?.validated?.tableName
        ?: throw IllegalStateException("Association target entity my_sql_ aux is not validated")

    val foreignKey = ForeignKey(fieldName, targetTableName, OnDelete.RESTRICT)
    val targetKeyFieldsMeta = getKeyFieldsMeta(targetEntityMeta)
    targetKeyFieldsMeta.forEach { targetKeyFieldMeta ->
        val targetFieldClauses = getSingleFieldClauses(targetKeyFieldMeta)
        targetFieldClauses.getColumns().forEach { foreignKey.addKey(it.name, it.type) }
    }
    return foreignKey
}

private fun getSingleFieldClauses(fieldMeta: EntityModel, columnType: String? = null): SqlClauses {
    val fieldName = getMetaName(fieldMeta)
    return Column(fieldName, columnType ?: getColumnType(fieldMeta))
}

private fun getColumnType(fieldMeta: EntityModel): String =
    if (isListFieldMeta(fieldMeta)) "JSON"
    else when (getFieldTypeMeta(fieldMeta)) {
        FieldType.BOOLEAN -> "BOOLEAN"
        FieldType.UINT8 -> "TINYINT UNSIGNED"
        FieldType.UINT16 -> "SMALLINT UNSIGNED"
        FieldType.UINT32 -> "INT UNSIGNED"
        FieldType.UINT64 -> "BIGINT UNSIGNED"
        FieldType.INT8 -> "TINYINT"
        FieldType.INT16 -> "SMALLINT"
        FieldType.INT32 -> "INT"
        FieldType.INT64 -> "BIGINT"
        FieldType.FLOAT -> "FLOAT"
        FieldType.DOUBLE -> "DOUBLE"
        FieldType.BIG_INTEGER -> "DECIMAL(65, 0)" // TODO(deepak-nulu) get size from meta-model
        FieldType.BIG_DECIMAL -> "DECIMAL" // TODO(deepak-nulu) get size from meta-model
        FieldType.TIMESTAMP -> "TIMESTAMP(3)"
        FieldType.STRING -> "VARCHAR(${getMaxSizeConstraint(fieldMeta)})"
        FieldType.UUID -> "BINARY(16)"
        FieldType.BLOB -> "BLOB"
        FieldType.PASSWORD1WAY -> "JSON"
        FieldType.PASSWORD2WAY -> "JSON"
        FieldType.ALIAS -> "TODO"
        FieldType.ENUMERATION -> "INT UNSIGNED"
        FieldType.ASSOCIATION -> throw IllegalStateException("Column type requested for association field type")
        FieldType.COMPOSITION -> throw IllegalStateException("Column type requested for composition field type")
        null -> throw IllegalStateException("Column type requested for null field type")
    }