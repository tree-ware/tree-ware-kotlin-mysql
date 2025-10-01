package org.treeWare.mySql.aux

import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.EntityModel
import java.util.*

class MySqlMetaModelMap {
    var tablePrefix: String? = null
    var tableName: String? = null
    var validated: MySqlMetaModelMapValidated? = null
    val parentMetaSet: SortedSet<EntityModel> = TreeSet()

    override fun toString(): String =
        "{tablePrefix: $tablePrefix, tableName: $tableName, validated: $validated, parents: ${
            parentMetaSet.joinToString { getMetaName(it) }
        }}"
}

data class MySqlMetaModelMapValidated(val databaseName: String, val tableName: String) {
    fun getFullName(databasePrefix: String?): String {
        val fullName = if (tableName.isEmpty()) databaseName else "$databaseName.$tableName"
        return if (databasePrefix == null) fullName else "${databasePrefix}__$fullName"
    }
}