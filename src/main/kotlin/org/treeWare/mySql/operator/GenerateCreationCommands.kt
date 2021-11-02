package org.treeWare.mySql.operator

import org.treeWare.metaModel.*
import org.treeWare.metaModel.traversal.AbstractLeader1Follower0MetaModelVisitor
import org.treeWare.metaModel.traversal.metaModelForEach
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MainModel
import org.treeWare.model.core.getAux
import org.treeWare.model.core.getSingleString
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.mySql.aux.MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME
import org.treeWare.mySql.aux.MySqlMetaModelMap

fun generateCreationsCommands(environment: String, mainMeta: MainModel): List<String> {
    val visitor = GenerateCreationCommandsVisitor(environment)
    metaModelForEach(mainMeta, visitor)
    return visitor.creationCommands
}

private data class Entity(val packageName: String, val entityName: String)
private data class CreateTableCommand(val entity: Entity, val command: String)

private class GenerateCreationCommandsVisitor(
    private val environment: String
) : AbstractLeader1Follower0MetaModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    private var databaseName = ""
    private var createDatabase = ""
    private val createTableBuffer = StringBuffer()
    private val createTables = mutableListOf<CreateTableCommand>()
    private val entitiesInCompositionSets = HashSet<Entity>()
    private var packageName = ""
    private var tablePrefix = ""

    val creationCommands: List<String>
        get() = listOf(createDatabase) + createTables.mapNotNull {
            if (entitiesInCompositionSets.contains(it.entity)) it.command else null
        }

    override fun visitMainMeta(leaderMainMeta1: MainModel): TraversalAction {
        val name = getMetaName(getRootMeta(leaderMainMeta1))
        databaseName = "${environment}_$name"
        createDatabase = "CREATE DATABASE IF NOT EXISTS ${databaseName};"
        return TraversalAction.CONTINUE
    }

    override fun visitRootMeta(leaderRootMeta1: EntityModel): TraversalAction {
        // A table is needed for the root entity even if it is not in a
        // composition-set, so make it look like it is in a composition-set.
        val packageName = getSingleString(leaderRootMeta1, "package")
        val entityName = getSingleString(leaderRootMeta1, "entity")
        entitiesInCompositionSets.add(Entity(packageName, entityName))
        return TraversalAction.CONTINUE
    }

    override fun visitPackageMeta(leaderPackageMeta1: EntityModel): TraversalAction {
        val aux = leaderPackageMeta1.getAux<MySqlMetaModelMap>(MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME)
        packageName = getMetaName(leaderPackageMeta1)
        tablePrefix = aux?.tablePrefix ?: packageName
        return TraversalAction.CONTINUE
    }

    override fun visitEntityMeta(leaderEntityMeta1: EntityModel): TraversalAction {
        val aux = leaderEntityMeta1.getAux<MySqlMetaModelMap>(MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME)
        val entityName = getMetaName(leaderEntityMeta1)
        val tableName = aux?.tableName ?: entityName
        createTableBuffer.setLength(0) // "clear" the buffer
        createTableBuffer
            .appendLine("CREATE TABLE IF NOT EXISTS ${databaseName}.${tablePrefix}__$tableName (")
            .append("  parent_id$ VARCHAR(10000)")
        return TraversalAction.CONTINUE
    }

    override fun leaveEntityMeta(leaderEntityMeta1: EntityModel) {
        createTableBuffer.append("\n);")
        val entityName = getMetaName(leaderEntityMeta1)
        val command = createTableBuffer.toString()
        createTables.add(CreateTableCommand(Entity(packageName, entityName), command))
    }

    override fun visitFieldMeta(leaderFieldMeta1: EntityModel): TraversalAction {
        if (isCompositionFieldMeta(leaderFieldMeta1) && isSetFieldMeta(leaderFieldMeta1)) {
            val entityInfoMeta = getEntityInfoMeta(leaderFieldMeta1)
            val packageName = getSingleString(entityInfoMeta, "package")
            val entityName = getSingleString(entityInfoMeta, "name")
            entitiesInCompositionSets.add(Entity(packageName, entityName))
        } else {
            val columnName = getMetaName(leaderFieldMeta1)
            val columnType = getColumnType(leaderFieldMeta1)
            createTableBuffer
                .appendLine(",")
                .append("  ")
                .append(columnName)
                .append(" ")
                .append(columnType)
        }
        return TraversalAction.CONTINUE
    }
}

private fun getColumnType(fieldMeta: EntityModel): String =
    if (isListFieldMeta(fieldMeta)) "JSON"
    else when (getFieldTypeMeta(fieldMeta)) {
        FieldType.BOOLEAN -> "BOOLEAN"
        FieldType.BYTE -> "TINYINT UNSIGNED"
        FieldType.SHORT -> "SMALLINT"
        FieldType.INT -> "INT"
        FieldType.LONG -> "BIGINT"
        FieldType.FLOAT -> "FLOAT"
        FieldType.DOUBLE -> "DOUBLE"
        FieldType.STRING -> "VARCHAR(2048)" // TODO(deepak-nulu) get size from meta-model
        FieldType.UUID -> "BINARY(16)"
        FieldType.BLOB -> "BLOB"
        FieldType.TIMESTAMP -> "DATETIME"
        FieldType.ALIAS -> "TODO"
        FieldType.PASSWORD1WAY -> "JSON"
        FieldType.PASSWORD2WAY -> "JSON"
        FieldType.ENUMERATION -> "VARCHAR(2048)"
        FieldType.ASSOCIATION -> "JSON"
        FieldType.COMPOSITION -> "JSON"
        null -> throw IllegalStateException("Null field type")
    }