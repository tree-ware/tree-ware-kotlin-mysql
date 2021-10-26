package org.treeWare.mysql.aux

import org.treeWare.model.encoder.AuxEncoder
import org.treeWare.model.encoder.WireFormatEncoder

class MysqlMetaModelMapEncoder : AuxEncoder {
    override fun encode(fieldName: String?, auxName: String, aux: Any?, wireFormatEncoder: WireFormatEncoder) {
        aux?.also {
            val auxFieldName = wireFormatEncoder.getAuxFieldName(fieldName, auxName)
            wireFormatEncoder.encodeObjectStart(auxFieldName)
            val mysqlAux = aux as MysqlMetaModelMap
            mysqlAux.tableName?.also { wireFormatEncoder.encodeStringField(MYSQL_META_MODEL_MAP_CODEC_TABLE_NAME, it) }
            wireFormatEncoder.encodeObjectEnd()
        }
    }
}
