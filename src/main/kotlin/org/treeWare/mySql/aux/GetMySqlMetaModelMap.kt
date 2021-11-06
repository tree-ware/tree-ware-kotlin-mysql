package org.treeWare.mySql.aux

import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.getAux

fun getMySqlMetaModelMap(element: ElementModel?): MySqlMetaModelMap? =
    element?.getAux<MySqlMetaModelMap>(MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME)