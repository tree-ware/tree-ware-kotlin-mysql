package org.treeWare.mySql.aux

class MySqlMetaModelMap {
    var tablePrefix: String? = null
    var tableName: String? = null
    var validated: MySqlMetaModelMapValidated? = null

    override fun toString(): String =
        "{tablePrefix: $tablePrefix, tableName: $tableName, validated: $validated}"
}

data class MySqlMetaModelMapValidated(val sqlIdentifier: String)
