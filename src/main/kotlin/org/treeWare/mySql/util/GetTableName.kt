package org.treeWare.mySql.util

import org.treeWare.model.core.EntityModel
import org.treeWare.mySql.aux.getMySqlMetaModelMap

fun getEntityMetaTableName(entityMeta: EntityModel): String = getMySqlMetaModelMap(entityMeta)?.validated?.tableName
    ?: throw IllegalStateException("Entity is not mapped to MySQL")

fun getEntityMetaTableFullName(entityMeta: EntityModel, databasePrefix: String?): String =
    getMySqlMetaModelMap(entityMeta)?.validated?.getFullName(databasePrefix)
        ?: throw IllegalStateException("Entity is not mapped to MySQL")

fun getEntityTableName(entity: EntityModel): String {
    val entityMeta = entity.meta ?: throw IllegalStateException("Entity does not have meta")
    return getEntityMetaTableName(entityMeta)
}

fun getEntityTableFullName(entity: EntityModel, databasePrefix: String?): String {
    val entityMeta = entity.meta ?: throw IllegalStateException("Entity does not have meta")
    return getEntityMetaTableFullName(entityMeta, databasePrefix)
}