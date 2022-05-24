package org.treeWare.mySql.operator.delegate

import org.treeWare.metaModel.*
import org.treeWare.model.core.*
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.encodeJson
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.GetEntityDelegate
import org.treeWare.model.operator.SetEntityDelegate
import org.treeWare.model.operator.getAssociationTargetEntity
import org.treeWare.util.assertInDevMode
import java.io.StringWriter
import java.time.Instant

internal const val COMMA = ", "
internal const val BEGIN = "  ("
internal const val BEGIN_LENGTH = BEGIN.length
internal const val AND = " AND "
internal const val EQUALS = " = "
internal const val IS = " IS "

internal enum class Preprocess { QUOTE, ESCAPE, UUID_TO_BIN, TO_HEX }

internal data class SqlColumn(
    val namePrefix: String?,
    val name: String,
    val value: Any?,
    val preprocess: Preprocess? = null
)

internal fun getSqlColumns(
    namePrefix: String?,
    field: FieldModel,
    setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?,
    getEntityDelegates: EntityDelegateRegistry<GetEntityDelegate>? = null,
    forSelectClause: Boolean = false
): List<SqlColumn> {
    val fieldMeta = requireNotNull(field.meta) { "Field meta is missing" }
    val fieldValue: Any? =
        if (isListField(field)) (field as ListFieldModel).values else (field as SingleFieldModel).value
    return getSqlColumnsForMeta(
        namePrefix,
        fieldMeta,
        fieldValue,
        setEntityDelegates,
        getEntityDelegates,
        forSelectClause
    )
}

internal fun getSqlColumnsForMeta(
    namePrefix: String?,
    fieldMeta: EntityModel,
    fieldValue: Any?,
    setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?,
    getEntityDelegates: EntityDelegateRegistry<GetEntityDelegate>?,
    forSelectClause: Boolean = false
): List<SqlColumn> =
    if (isListFieldMeta(fieldMeta)) listOf(getSqlJsonListColumn(fieldMeta, fieldValue as List<ElementModel>))
    else when (val fieldType = requireNotNull(getFieldTypeMeta(fieldMeta)) { "Field meta is missing" }) {
        FieldType.ASSOCIATION -> getAssociationSqlColumns(fieldMeta, fieldValue, forSelectClause)
        FieldType.COMPOSITION -> getCompositionSqlColumns(
            fieldMeta,
            fieldValue,
            setEntityDelegates,
            getEntityDelegates,
            forSelectClause
        )
        else -> getSingleSqlColumn(namePrefix, fieldType, fieldMeta, fieldValue)
            ?.let { listOf(it) } ?: emptyList()
    }

private fun getSqlJsonListColumn(
    fieldMeta: EntityModel,
    fieldValue: List<ElementModel>
): SqlColumn {
    val writer = StringWriter()
    encodeJson(fieldValue, writer, encodePasswords = EncodePasswords.HASHED_AND_ENCRYPTED)
    val jsonValue = writer.toString()
    return SqlColumn(null, getMetaName(fieldMeta), jsonValue, Preprocess.QUOTE)
}

private fun getSqlJsonColumn(
    fieldMeta: EntityModel,
    fieldValue: ElementModel
): SqlColumn? {
    val writer = StringWriter()
    encodeJson(fieldValue, writer, encodePasswords = EncodePasswords.HASHED_AND_ENCRYPTED)
    val jsonValue = writer.toString()
    return if (jsonValue.isEmpty()) null
    else SqlColumn(null, getMetaName(fieldMeta), jsonValue, Preprocess.QUOTE)
}

/**
 * @return SQL columns for the association field. if `forSelectClause` is `true`, only the JSON column is returned;
 * else the columns derived from the target keys and the JSON column are returned.
 */
private fun getAssociationSqlColumns(
    fieldMeta: EntityModel,
    fieldValue: Any?,
    forSelectClause: Boolean
): List<SqlColumn> {
    return if (fieldValue == null) {
        val targetEntityMeta = getMetaModelResolved(fieldMeta)?.associationMeta?.targetEntityMeta
            ?: throw IllegalStateException("Association meta-model is not resolved")
        val targetKeyFieldsMeta = getKeyFieldsMeta(targetEntityMeta)
        val fieldName = getMetaName(fieldMeta)
        val jsonColumn = SqlColumn(null, fieldName, null)
        if (forSelectClause) listOf(jsonColumn)
        else targetKeyFieldsMeta.map { SqlColumn(fieldName, getMetaName(it), null) } + jsonColumn
    } else {
        val fieldName = getMetaName(fieldMeta)
        val association = fieldValue as AssociationModel
        val writer = StringWriter()
        encodeJson(association, writer, encodePasswords = EncodePasswords.HASHED_AND_ENCRYPTED)
        val jsonValue = writer.toString()
        val jsonColumn = SqlColumn(null, fieldName, jsonValue, Preprocess.QUOTE)
        if (forSelectClause) listOf(jsonColumn)
        else {
            val target = getAssociationTargetEntity(association)
            val keys = target.getKeyFields(true)
            assertInDevMode(keys.missing.isEmpty())
            keys.available.mapNotNull { key ->
                getSingleSqlColumn(
                    fieldName,
                    getFieldType(key),
                    requireNotNull(key.meta) { "Key field meta is missing" },
                    key.value
                )
            } + jsonColumn
        }
    }
}

private fun getCompositionSqlColumns(
    fieldMeta: EntityModel,
    fieldValue: Any?,
    setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?,
    getEntityDelegates: EntityDelegateRegistry<GetEntityDelegate>?,
    forSelectClause: Boolean
): List<SqlColumn> {
    val compositionMeta = getMetaModelResolved(fieldMeta)?.compositionMeta
    val entityFullName = getMetaModelResolved(compositionMeta)?.fullName
    val setEntityDelegate = setEntityDelegates?.get(entityFullName)
    if (setEntityDelegate?.isSingleValue() != true) throw IllegalStateException("Getting composition as a single SQL column")
    return if (forSelectClause) {
        val getEntityDelegate = getEntityDelegates?.get(entityFullName) as MySqlGetEntityDelegate?
            ?: throw IllegalStateException("Get entity delegate is missing")
        getEntityDelegate.getSelectColumns(fieldMeta)
    } else {
        val singleValue = setEntityDelegate.getSingleValue(fieldValue as EntityModel)
        listOf(SqlColumn(null, getMetaName(fieldMeta), singleValue))
    }
}

private fun getSingleSqlColumn(
    namePrefix: String?,
    fieldType: FieldType,
    fieldMeta: EntityModel,
    fieldValue: Any?
): SqlColumn? {
    val columnName = getMetaName(fieldMeta)
    return if (fieldValue == null) SqlColumn(namePrefix, columnName, null)
    else when (fieldType) {
        FieldType.BOOLEAN,
        FieldType.UINT8,
        FieldType.UINT16,
        FieldType.UINT32,
        FieldType.UINT64,
        FieldType.INT8,
        FieldType.INT16,
        FieldType.INT32,
        FieldType.INT64,
        FieldType.FLOAT,
        FieldType.DOUBLE,
        FieldType.BIG_INTEGER,
        FieldType.BIG_DECIMAL -> SqlColumn(namePrefix, columnName, (fieldValue as PrimitiveModel).value)
        FieldType.TIMESTAMP -> SqlColumn(
            namePrefix,
            columnName,
            timestampMillisecondsAsIso8601((fieldValue as PrimitiveModel).value as Long),
            Preprocess.QUOTE
        )
        FieldType.STRING -> SqlColumn(
            namePrefix,
            columnName,
            (fieldValue as PrimitiveModel).value as String,
            Preprocess.ESCAPE
        )
        FieldType.UUID -> SqlColumn(
            namePrefix,
            columnName,
            (fieldValue as PrimitiveModel).value as String,
            Preprocess.UUID_TO_BIN
        )
        FieldType.BLOB -> SqlColumn(
            namePrefix,
            columnName,
            (fieldValue as PrimitiveModel).value as ByteArray,
            Preprocess.TO_HEX
        )
        FieldType.PASSWORD1WAY -> getSqlJsonColumn(fieldMeta, fieldValue as Password1wayModel)
        FieldType.PASSWORD2WAY -> getSqlJsonColumn(fieldMeta, fieldValue as Password2wayModel)
        FieldType.ALIAS -> throw IllegalStateException("Getting alias as a single SQL column")
        FieldType.ENUMERATION -> SqlColumn(namePrefix, columnName, (fieldValue as EnumerationModel).number)
        FieldType.ASSOCIATION -> throw IllegalStateException("Getting association as a single SQL column")
        FieldType.COMPOSITION -> throw IllegalStateException("Getting composition as a single SQL column")
    }
}

internal fun timestampMillisecondsAsIso8601(timestampMilliseconds: Long): String =
    instantAsIso8601(Instant.ofEpochMilli(timestampMilliseconds))

private fun instantAsIso8601(instant: Instant): String =
    // TODO(deepak-nulu): Timezone offsets are supported only in MySQL 8.0.19
    instant.toString()
        // Instant.ofEpochMilli() returns UTC timezone as the character 'Z' at the end.
        // drop it until the test upgrades to MySQL 8.0.19 or later.
        .let { if (it.endsWith('Z')) it.dropLast(1) else it }

// TODO(#63) use prepared statements instead of this escape function (which currently does not even escape the value).
internal fun escape(value: Any): Any = value

internal fun addSqlColumn(column: SqlColumn, nameValueSeparator: String, builder: StringBuilder) {
    addSqlColumnName(column, builder)
    builder.append(nameValueSeparator)
    addSqlColumnValue(column, builder)
}

internal fun addSqlColumnName(column: SqlColumn, builder: StringBuilder) {
    column.namePrefix?.also { builder.append(it).append("$") }
    builder.append(column.name)
}

internal fun addSqlColumnValue(column: SqlColumn, builder: StringBuilder) {
    val value = column.value
    if (value == null) {
        builder.append("NULL")
        return
    }
    when (column.preprocess) {
        Preprocess.QUOTE -> builder.append("'").append(value).append("'")
        Preprocess.ESCAPE -> builder.append("'").append(escape(value)).append("'")
        Preprocess.UUID_TO_BIN -> builder.append("UUID_TO_BIN(").append("'").append(escape(value)).append("')")
        Preprocess.TO_HEX -> {
            builder.append("0x")
            (value as ByteArray).forEach { builder.append("%02x".format(it)) }
        }
        null -> builder.append(value)
    }
}