package org.treeWare.mySql.operator.ddl

import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getFieldTypeMeta
import org.treeWare.metaModel.getMaxSizeConstraint
import org.treeWare.metaModel.isListFieldMeta
import org.treeWare.model.core.EntityModel

fun getFieldColumnType(fieldMeta: EntityModel): String =
    if (isListFieldMeta(fieldMeta)) "JSON"
    else when (getFieldTypeMeta(fieldMeta)) {
        FieldType.BOOLEAN -> "BOOLEAN"
        FieldType.UINT8 -> "TINYINT UNSIGNED"
        FieldType.UINT16 -> "SMALLINT UNSIGNED"
        FieldType.UINT32 -> "INT UNSIGNED"
        FieldType.UINT64 -> "BIGINT UNSIGNED"
        FieldType.INT8 -> "TINYINT"
        FieldType.INT16 -> "SMALLINT"
        FieldType.INT32 -> "INT"
        FieldType.INT64 -> "BIGINT"
        FieldType.FLOAT -> "FLOAT"
        FieldType.DOUBLE -> "DOUBLE"
        FieldType.BIG_INTEGER -> "DECIMAL(65, 0)" // TODO(deepak-nulu) get size from meta-model
        FieldType.BIG_DECIMAL -> "DECIMAL" // TODO(deepak-nulu) get size from meta-model
        FieldType.TIMESTAMP -> "TIMESTAMP(3)"
        FieldType.STRING -> "VARCHAR(${getMaxSizeConstraint(fieldMeta)})"
        FieldType.UUID -> "BINARY(16)"
        FieldType.BLOB -> "BLOB"
        FieldType.PASSWORD1WAY -> "JSON"
        FieldType.PASSWORD2WAY -> "JSON"
        FieldType.ALIAS -> "TODO"
        FieldType.ENUMERATION -> "INT UNSIGNED"
        FieldType.ASSOCIATION -> throw IllegalStateException("Column type requested for association field type")
        FieldType.COMPOSITION -> throw IllegalStateException("Column type requested for composition field type")
        null -> throw IllegalStateException("Column type requested for null field type")
    }