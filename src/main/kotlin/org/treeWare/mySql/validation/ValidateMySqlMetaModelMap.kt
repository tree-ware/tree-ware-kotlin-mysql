package org.treeWare.mySql.validation

import org.treeWare.metaModel.getMetaName
import org.treeWare.metaModel.getRootMeta
import org.treeWare.metaModel.traversal.AbstractLeader1Follower0MutableMetaModelVisitor
import org.treeWare.metaModel.traversal.mutableMetaModelForEach
import org.treeWare.model.core.MutableElementModel
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.mySql.aux.MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME
import org.treeWare.mySql.aux.MySqlMetaModelMap
import org.treeWare.mySql.aux.MySqlMetaModelMapValidated
import org.treeWare.mySql.aux.getMySqlMetaModelMap

fun validateMySqlMetaModelMap(environment: String, mainMeta: MutableMainModel): List<String> {
    val visitor = ValidateMySqlMetaModelMapVisitor(environment)
    mutableMetaModelForEach(mainMeta, visitor)
    return visitor.errors
}

private fun validateDatabaseName(name: String): List<String> =
    if (name.length > 64) listOf("Specify a database name that is 64 characters or less") else emptyList()

private fun validateTableName(name: String): List<String> =
    if (name.length > 64) listOf("Specify a table name that is 64 characters or less") else emptyList()

private class ValidateMySqlMetaModelMapVisitor(
    private val environment: String
) : AbstractLeader1Follower0MutableMetaModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val errors = mutableListOf<String>()
    private var databaseName = ""
    private var tablePrefix = ""

    override fun visitMainMeta(leaderMainMeta1: MutableMainModel): TraversalAction {
        val name = getMetaName(getRootMeta(leaderMainMeta1))
        databaseName = "${environment}_$name"
        val nameErrors = validateDatabaseName(databaseName)
        if (nameErrors.isNotEmpty()) errors.addAll(nameErrors)
        else {
            val aux = getMySqlMetaModelMap(leaderMainMeta1) ?: newMySqlMetaModelMapFor(leaderMainMeta1)
            aux.validated = MySqlMetaModelMapValidated(databaseName)
        }
        return TraversalAction.CONTINUE
    }

    override fun visitPackageMeta(leaderPackageMeta1: MutableEntityModel): TraversalAction {
        val aux = getMySqlMetaModelMap(leaderPackageMeta1)
        val packageName = getMetaName(leaderPackageMeta1)
        tablePrefix = aux?.tablePrefix ?: packageName
        return TraversalAction.CONTINUE
    }

    override fun visitEntityMeta(leaderEntityMeta1: MutableEntityModel): TraversalAction {
        val aux = getMySqlMetaModelMap(leaderEntityMeta1) ?: return TraversalAction.ABORT_SUB_TREE
        val entityName = getMetaName(leaderEntityMeta1)
        val tableSuffix = aux.tableName ?: entityName
        val tableName = "${tablePrefix}__${tableSuffix}"
        val nameErrors = validateTableName(tableName)
        if (nameErrors.isNotEmpty()) errors.addAll(nameErrors)
        else aux.validated = MySqlMetaModelMapValidated("$databaseName.$tableName")
        return TraversalAction.ABORT_SUB_TREE
    }
}

private fun newMySqlMetaModelMapFor(element: MutableElementModel): MySqlMetaModelMap =
    MySqlMetaModelMap().also { element.setAux(MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME, it) }