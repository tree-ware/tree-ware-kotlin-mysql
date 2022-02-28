package org.treeWare.mySql.operator

import org.treeWare.metaModel.*
import org.treeWare.metaModel.traversal.AbstractLeader1MetaModelVisitor
import org.treeWare.metaModel.traversal.metaModelForEach
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MainModel
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.mySql.aux.getMySqlMetaModelMap

fun generateCreateCommands(mainMeta: MainModel): List<String> {
    val visitor = GenerateCreateCommandsVisitor()
    metaModelForEach(mainMeta, visitor)
    return visitor.createCommands
}

private class GenerateCreateCommandsVisitor :
    AbstractLeader1MetaModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val createCommands = mutableListOf<String>()

    private val createTableBuffer = StringBuffer()
    private val keys = mutableListOf<String>()
    private var packageName = ""
    private var tablePrefix = ""

    override fun visitMainMeta(leaderMainMeta1: MainModel): TraversalAction {
        val databaseName = getMySqlMetaModelMap(leaderMainMeta1)?.validated?.sqlIdentifier
            ?: return TraversalAction.ABORT_TREE
        val command = "CREATE DATABASE IF NOT EXISTS $databaseName;"
        createCommands.add(command)
        return TraversalAction.CONTINUE
    }

    override fun visitPackageMeta(leaderPackageMeta1: EntityModel): TraversalAction {
        val aux = getMySqlMetaModelMap(leaderPackageMeta1)
        packageName = getMetaName(leaderPackageMeta1)
        tablePrefix = aux?.tablePrefix ?: packageName
        return TraversalAction.CONTINUE
    }

    override fun visitEntityMeta(leaderEntityMeta1: EntityModel): TraversalAction {
        val aux = getMySqlMetaModelMap(leaderEntityMeta1)
        val tableName = aux?.validated?.sqlIdentifier ?: ""
        createTableBuffer.setLength(0) // "clear" the buffer
        createTableBuffer
            .appendLine("CREATE TABLE IF NOT EXISTS $tableName (")
            .appendLine("  $PARENT_ID_COLUMN_NAME VARCHAR(700),")
        keys.clear()
        keys.add(PARENT_ID_COLUMN_NAME)
        return TraversalAction.CONTINUE
    }

    override fun leaveEntityMeta(leaderEntityMeta1: EntityModel) {
        createTableBuffer
            .append("  PRIMARY KEY (")
            .append(keys.joinToString(", "))
            .appendLine(")")
            .append(");")
        val command = createTableBuffer.toString()
        createCommands.add(command)
    }

    override fun visitFieldMeta(leaderFieldMeta1: EntityModel): TraversalAction {
        if (isCompositionFieldMeta(leaderFieldMeta1)) return TraversalAction.CONTINUE
        val columnName = getMetaName(leaderFieldMeta1)
        val columnType = getColumnType(leaderFieldMeta1)
        createTableBuffer
            .append("  ")
            .append(columnName)
            .append(" ")
            .append(columnType)
            .appendLine(",")
        if (isKeyFieldMeta(leaderFieldMeta1)) keys.add(columnName)
        return TraversalAction.CONTINUE
    }
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
        FieldType.ENUMERATION -> "VARCHAR(1024)"  // TODO(tree-ware-kotlin-core#82) use number instead of name
        FieldType.ASSOCIATION -> "JSON"
        FieldType.COMPOSITION -> throw IllegalStateException("Column type requested for composition field type")
        null -> throw IllegalStateException("Column type requested for null field type")
    }