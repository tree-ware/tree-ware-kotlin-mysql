package org.treeWare.mySql.operator.delegate

import okio.Buffer
import org.treeWare.metaModel.*
import org.treeWare.model.core.*
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.encodeJson
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.GetEntityDelegate
import org.treeWare.model.operator.SetEntityDelegate
import org.treeWare.model.operator.getAssociationTargetEntity
import org.treeWare.util.assertInDevMode
import java.sql.PreparedStatement
import java.time.Instant

internal const val COMMA = ", "
internal const val BEGIN = "  ("
internal const val BEGIN_LENGTH = BEGIN.length
internal const val AND = " AND "
internal const val EQUALS = " = "
internal const val IS = " IS "

internal data class TypedValue(val fieldType: FieldType, val value: Any)

interface SqlColumn {
    val namePrefix: String?
    val name: String
    val placeholder: String

    fun isNull(): Boolean

    /**
     * @return the next index (needed because some columns bind more than 1 value).
     */
    fun bindValues(statement: PreparedStatement, index: Int): Int
}

internal data class SingleValuedSqlColumn(
    override val namePrefix: String?,
    override val name: String,
    val typedValue: TypedValue?,
    override val placeholder: String = "?"
) : SqlColumn {
    override fun isNull(): Boolean = typedValue == null
    override fun bindValues(statement: PreparedStatement, index: Int): Int = bindValue(typedValue, statement, index)
}

internal data class MultiValuedSqlColumn(
    override val namePrefix: String?,
    override val name: String,
    val typedValues: List<TypedValue>,
    override val placeholder: String
) : SqlColumn {
    override fun isNull(): Boolean = false

    override fun bindValues(statement: PreparedStatement, index: Int): Int =
        typedValues.fold(index) { i, typedValue -> bindValue(typedValue, statement, i) }
}

internal fun getSqlColumns(
    namePrefix: String?,
    field: FieldModel,
    setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?,
    getEntityDelegates: EntityDelegateRegistry<GetEntityDelegate>? = null,
    forSelectClause: Boolean = false
): List<SqlColumn> {
    val fieldMeta = requireNotNull(field.meta) { "Field meta is missing" }
    val fieldValue: Any? = (field as SingleFieldModel).value
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
    when (val fieldType = requireNotNull(getFieldTypeMeta(fieldMeta)) { "Field meta is missing" }) {
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
    val buffer = Buffer()
    encodeJson(fieldValue, buffer, encodePasswords = EncodePasswords.HASHED_AND_ENCRYPTED)
    val jsonValue = buffer.readUtf8()
    return SingleValuedSqlColumn(
        null,
        getMetaName(fieldMeta),
        TypedValue(FieldType.STRING, jsonValue)
    )
}

private fun getSqlJsonColumn(
    fieldMeta: EntityModel,
    fieldValue: ElementModel
): SqlColumn? {
    val buffer = Buffer()
    encodeJson(fieldValue, buffer, encodePasswords = EncodePasswords.HASHED_AND_ENCRYPTED)
    val jsonValue = buffer.readUtf8()
    return if (jsonValue.isEmpty()) null
    else SingleValuedSqlColumn(
        null,
        getMetaName(fieldMeta),
        TypedValue(FieldType.STRING, jsonValue)
    )
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
        val jsonColumn = SingleValuedSqlColumn(null, fieldName, null)
        if (forSelectClause) listOf(jsonColumn)
        else targetKeyFieldsMeta.map { SingleValuedSqlColumn(fieldName, getMetaName(it), null) } + jsonColumn
    } else {
        val fieldName = getMetaName(fieldMeta)
        val association = fieldValue as AssociationModel
        val buffer = Buffer()
        encodeJson(association, buffer, encodePasswords = EncodePasswords.HASHED_AND_ENCRYPTED)
        val jsonValue = buffer.readUtf8()
        val jsonColumn = SingleValuedSqlColumn(
            null,
            fieldName,
            TypedValue(FieldType.STRING, jsonValue)
        )
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
    } else listOf(
        (setEntityDelegate as MySqlSetEntityDelegate).getSqlColumn(
            null,
            getMetaName(fieldMeta),
            fieldValue as EntityModel
        )
    )
}

private fun getSingleSqlColumn(
    namePrefix: String?,
    fieldType: FieldType,
    fieldMeta: EntityModel,
    fieldValue: Any?
): SqlColumn? {
    val columnName = getMetaName(fieldMeta)
    return if (fieldValue == null) SingleValuedSqlColumn(namePrefix, columnName, null)
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
        FieldType.BIG_DECIMAL -> getPrimitiveSqlColumn(namePrefix, columnName, fieldType, fieldValue)
        FieldType.TIMESTAMP -> SingleValuedSqlColumn(
            namePrefix,
            columnName,
            TypedValue(fieldType, timestampMillisecondsAsIso8601((fieldValue as PrimitiveModel).value as ULong))
        )
        FieldType.STRING -> getPrimitiveSqlColumn(namePrefix, columnName, fieldType, fieldValue)
        FieldType.UUID -> getPrimitiveSqlColumn(namePrefix, columnName, fieldType, fieldValue, "UUID_TO_BIN(?)")
        FieldType.BLOB -> getPrimitiveSqlColumn(namePrefix, columnName, fieldType, fieldValue)
        FieldType.PASSWORD1WAY -> getSqlJsonColumn(fieldMeta, fieldValue as Password1wayModel)
        FieldType.PASSWORD2WAY -> getSqlJsonColumn(fieldMeta, fieldValue as Password2wayModel)
        FieldType.ALIAS -> throw IllegalStateException("Getting alias as a single SQL column")
        FieldType.ENUMERATION -> SingleValuedSqlColumn(
            namePrefix,
            columnName,
            TypedValue(fieldType, (fieldValue as EnumerationModel).number)
        )
        FieldType.ASSOCIATION -> throw IllegalStateException("Getting association as a single SQL column")
        FieldType.COMPOSITION -> throw IllegalStateException("Getting composition as a single SQL column")
    }
}

private fun getPrimitiveSqlColumn(
    namePrefix: String?,
    columnName: String,
    fieldType: FieldType,
    fieldValue: Any,
    placeholder: String = "?"
) = SingleValuedSqlColumn(
    namePrefix,
    columnName,
    TypedValue(fieldType, (fieldValue as PrimitiveModel).value),
    placeholder
)

internal fun timestampMillisecondsAsIso8601(timestampMilliseconds: ULong): String =
    instantAsIso8601(Instant.ofEpochMilli(timestampMilliseconds.toLong()))

private fun instantAsIso8601(instant: Instant): String =
    // TODO(deepak-nulu): Timezone offsets are supported only in MySQL 8.0.19
    instant.toString()
        // Instant.ofEpochMilli() returns UTC timezone as the character 'Z' at the end.
        // drop it until the test upgrades to MySQL 8.0.19 or later.
        .let { if (it.endsWith('Z')) it.dropLast(1) else it }

internal fun addSqlColumnPlaceholder(column: SqlColumn, nameValueSeparator: String, builder: StringBuilder) {
    addSqlColumnName(column, builder)
    builder.append(nameValueSeparator)
    builder.append(if (column.isNull()) "NULL" else column.placeholder)
}

internal fun addSqlColumnName(column: SqlColumn, builder: StringBuilder) {
    column.namePrefix?.also { builder.append(it).append("__") }
    builder.append(column.name)
}

private fun bindValue(typedValue: TypedValue?, statement: PreparedStatement, index: Int): Int {
    val value = typedValue?.value ?: return index
    when (typedValue.fieldType) {
        FieldType.BOOLEAN -> statement.setBoolean(index, value as Boolean)
        // PreparedStatement does not have setters for unsigned numbers. So all integers are bound as string values.
        // There is no risk of SQL injection since tree-ware can only contain numbers in numeric fields.
        FieldType.UINT8,
        FieldType.UINT16,
        FieldType.UINT32,
        FieldType.UINT64 -> statement.setString(index, value.toString())
        FieldType.INT8,
        FieldType.INT16,
        FieldType.INT32 -> statement.setInt(index, value as Int)
        FieldType.INT64 -> statement.setLong(index, value as Long)
        FieldType.FLOAT -> statement.setFloat(index, value as Float)
        FieldType.DOUBLE -> statement.setDouble(index, value as Double)
        FieldType.BIG_INTEGER,
        FieldType.BIG_DECIMAL -> statement.setString(index, value.toString())
        FieldType.TIMESTAMP -> statement.setString(index, value as String)
        FieldType.STRING -> statement.setString(index, value as String)
        FieldType.UUID -> statement.setString(index, value.toString())
        FieldType.BLOB -> statement.setBytes(index, value as ByteArray)
        FieldType.PASSWORD1WAY,
        FieldType.PASSWORD2WAY -> statement.setString(index, value as String)
        FieldType.ALIAS -> TODO()
        // Enumerations are unsigned integers, so bound as a string, for the same reason as the unsigned integers above.
        FieldType.ENUMERATION -> statement.setString(index, value.toString())
        FieldType.ASSOCIATION -> throw IllegalStateException("Value binding requested for association field type")
        FieldType.COMPOSITION -> throw IllegalStateException("Value binding requested for composition field type")
    }
    return index + 1
}