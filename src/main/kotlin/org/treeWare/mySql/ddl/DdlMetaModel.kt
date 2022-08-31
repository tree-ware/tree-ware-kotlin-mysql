package org.treeWare.mySql

import org.treeWare.metaModel.newMetaModelFromJsonFiles

private val DDL_META_MODEL_FILES = listOf(
    "org/treeWare/sql/ddl.json"
)

val ddlMetaModel = requireNotNull(
    newMetaModelFromJsonFiles(
        DDL_META_MODEL_FILES,
        false,
        null,
        null,
        emptyList(),
        true
    ).metaModel
)