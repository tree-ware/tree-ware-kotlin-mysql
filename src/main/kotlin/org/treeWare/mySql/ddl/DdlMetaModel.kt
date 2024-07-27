package org.treeWare.mySql.ddl

import org.treeWare.metaModel.newMetaModelFromJsonFiles
import org.treeWare.model.core.defaultRootEntityFactory

private val DDL_META_MODEL_FILES = listOf(
    "tree_ware/meta_model/ddl.json"
)

val ddlMetaModel = requireNotNull(
    newMetaModelFromJsonFiles(
        DDL_META_MODEL_FILES,
        false,
        null,
        null,
        ::defaultRootEntityFactory,
        emptyList(),
        true
    ).metaModel
)