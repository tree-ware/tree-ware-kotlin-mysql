package org.treeWare.mySql.aux

import org.treeWare.model.encoder.AuxEncoder
import org.treeWare.model.encoder.WireFormatEncoder

class MySqlMetaModelMapEncoder : AuxEncoder {
    override fun encode(fieldName: String?, auxName: String, aux: Any?, wireFormatEncoder: WireFormatEncoder) {
        aux?.also {
            val auxFieldName = wireFormatEncoder.getAuxFieldName(fieldName, auxName)
            wireFormatEncoder.encodeObjectStart(auxFieldName)
            val mySqlAux = aux as MySqlMetaModelMap
            mySqlAux.tablePrefix?.also {
                wireFormatEncoder.encodeStringField(MY_SQL_META_MODEL_MAP_CODEC_TABLE_PREFIX, it)
            }
            mySqlAux.tableName?.also { wireFormatEncoder.encodeStringField(MY_SQL_META_MODEL_MAP_CODEC_TABLE_NAME, it) }
            wireFormatEncoder.encodeObjectEnd()
        }
    }
}
