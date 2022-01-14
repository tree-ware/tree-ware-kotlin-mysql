package org.treeWare.mySql.validation

import org.treeWare.metaModel.*
import org.treeWare.metaModel.traversal.AbstractLeader1MutableMetaModelVisitor
import org.treeWare.metaModel.traversal.mutableMetaModelForEach
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MutableElementModel
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.mySql.aux.MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME
import org.treeWare.mySql.aux.MySqlMetaModelMap
import org.treeWare.mySql.aux.MySqlMetaModelMapValidated
import org.treeWare.mySql.aux.getMySqlMetaModelMap

fun validateMySqlMetaModelMap(mainMeta: MutableMainModel, environment: String): List<String> {
    val visitor = ValidateMySqlMetaModelMapVisitor(environment)
    mutableMetaModelForEach(mainMeta, visitor)
    return visitor.errors
}

private fun validateDatabaseName(name: String): List<String> =
    if (name.length > 64) listOf("Database name $name must be 64 characters or less") else emptyList()

private fun validateTableName(name: String): List<String> =
    if (name.length > 64) listOf("Table name $name must be 64 characters or less") else emptyList()

private fun validateKeys(entityId: String, entityMeta: EntityModel): List<String> {
    val fields = getFieldsMeta(entityMeta).values
    val keyFieldsMeta = filterKeyFields(fields)
    if (keyFieldsMeta.size > 1) return listOf("Entity $entityId has more than 1 key; only 1 key is supported for MySQL")
    val keyFieldMeta = keyFieldsMeta.firstOrNull() ?: return emptyList()
    return when (val keyFieldType = getFieldTypeMeta(keyFieldMeta)) {
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
        FieldType.BIG_DECIMAL,
        FieldType.TIMESTAMP,
        FieldType.STRING,
        FieldType.UUID -> emptyList()
        else -> listOf("Entity $entityId key field type $keyFieldType is not supported for MySQL")
    }
}

private class ValidateMySqlMetaModelMapVisitor(
    private val environment: String
) : AbstractLeader1MutableMetaModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val errors = mutableListOf<String>()
    private var databaseName = ""
    private var tablePrefix = ""

    private val path = ArrayDeque<String>()
    private fun getPathName(): String = path.joinToString("/", prefix = "/")

    override fun visitMainMeta(leaderMainMeta1: MutableMainModel): TraversalAction {
        val mainMetaName = getMainMetaName(leaderMainMeta1)
        path.addLast(mainMetaName)
        databaseName = "${environment}_$mainMetaName"
        val nameErrors = validateDatabaseName(databaseName)
        if (nameErrors.isNotEmpty()) errors.addAll(nameErrors)
        else {
            val aux = getMySqlMetaModelMap(leaderMainMeta1) ?: newMySqlMetaModelMapFor(leaderMainMeta1)
            aux.validated = MySqlMetaModelMapValidated(databaseName)
        }
        return TraversalAction.CONTINUE
    }

    override fun leaveMainMeta(leaderMainMeta1: MutableMainModel) {
        path.removeLast()
    }

    override fun visitPackageMeta(leaderPackageMeta1: MutableEntityModel): TraversalAction {
        val aux = getMySqlMetaModelMap(leaderPackageMeta1)
        val packageName = getMetaName(leaderPackageMeta1)
        path.addLast(packageName)
        tablePrefix = aux?.tablePrefix ?: packageName
        return TraversalAction.CONTINUE
    }

    override fun leavePackageMeta(leaderPackageMeta1: MutableEntityModel) {
        path.removeLast()
    }

    override fun visitEntityMeta(leaderEntityMeta1: MutableEntityModel): TraversalAction {
        val entityName = getMetaName(leaderEntityMeta1)
        path.addLast(entityName)
        val aux = getMySqlMetaModelMap(leaderEntityMeta1) ?: return TraversalAction.ABORT_SUB_TREE
        val tableSuffix = aux.tableName ?: entityName
        val tableName = "${tablePrefix}__${tableSuffix}"
        val entityErrors = mutableListOf<String>()
        entityErrors.addAll(validateTableName(tableName))
        entityErrors.addAll(validateKeys(getPathName(), leaderEntityMeta1))
        if (entityErrors.isNotEmpty()) errors.addAll(entityErrors)
        else aux.validated = MySqlMetaModelMapValidated("$databaseName.$tableName")
        return TraversalAction.CONTINUE
    }

    override fun leaveEntityMeta(leaderEntityMeta1: MutableEntityModel) {
        path.removeLast()
    }

    override fun visitFieldMeta(leaderFieldMeta1: MutableEntityModel): TraversalAction {
        val fieldType = getFieldTypeMeta(leaderFieldMeta1)
        if (fieldType != FieldType.STRING) return TraversalAction.ABORT_SUB_TREE
        val fieldName = getMetaName(leaderFieldMeta1)
        val maxSize = getMaxSizeConstraint(leaderFieldMeta1)
        if (maxSize == null) {
            path.addLast(fieldName)
            errors.add("String field ${getPathName()} must specify max_size constraint for MySQL")
            path.removeLast()
        }
        return TraversalAction.ABORT_SUB_TREE
    }
}

private fun newMySqlMetaModelMapFor(element: MutableElementModel): MySqlMetaModelMap =
    MySqlMetaModelMap().also { element.setAux(MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME, it) }