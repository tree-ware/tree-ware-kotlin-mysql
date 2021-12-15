package org.treeWare.mySql.aux

import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.getAux

fun getMySqlMetaModelMap(elementMeta: ElementModel?): MySqlMetaModelMap? =
    elementMeta?.getAux<MySqlMetaModelMap>(MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME)