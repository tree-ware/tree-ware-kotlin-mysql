package org.treeWare.mySql.operator.delegate

import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getParentEntityMeta
import org.treeWare.model.core.*
import org.treeWare.model.decoder.decodeJsonField
import org.treeWare.model.operator.ElementModelError
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.GetEntityDelegate
import org.treeWare.model.operator.copy
import org.treeWare.model.operator.get.FetchCompositionResult
import org.treeWare.model.operator.get.FetchCompositionSetResult
import org.treeWare.model.operator.get.GetDelegate
import org.treeWare.mySql.operator.FIELD_PATH_COLUMN_NAME
import org.treeWare.mySql.operator.SINGLETON_KEY_COLUMN_NAME
import org.treeWare.mySql.operator.SINGLETON_KEY_COLUMN_VALUE
import org.treeWare.mySql.util.getEntityMetaTableFullName
import org.treeWare.mySql.util.getEntityMetaTableName
import java.io.StringReader
import java.nio.ByteBuffer
import java.sql.Connection
import java.sql.ResultSet
import java.time.ZoneOffset
import java.util.*

private val UTC_TIMEZONE = TimeZone.getTimeZone(ZoneOffset.UTC)

private val SINGLETON_SQL_COLUMN = SqlColumn(null, SINGLETON_KEY_COLUMN_NAME, SINGLETON_KEY_COLUMN_VALUE)

class MySqlGetDelegate(
    private val entityDelegates: EntityDelegateRegistry<GetEntityDelegate>?,
    private val connection: Connection,
    private val logCommands: Boolean
) : GetDelegate {
    override fun fetchComposition(
        fieldPath: String,
        ancestorKeys: List<Keys>,
        requestFields: List<FieldModel>,
        responseParentField: MutableSingleFieldModel
    ): FetchCompositionResult {
        val entityMeta = getCompositionEntityMeta(responseParentField)
        val tableName = getEntityMetaTableFullName(entityMeta)
        val select = SelectCommandBuilder(tableName)
        if (ancestorKeys.isEmpty()) select.addWhereColumn(SINGLETON_SQL_COLUMN)
        else getAncestorKeyColumns(entityMeta, ancestorKeys[0]).forEach { select.addWhereColumn(it) }
        select.addWhereColumn(SqlColumn(null, FIELD_PATH_COLUMN_NAME, fieldPath, Preprocess.QUOTE))
        requestFields.forEach { select.addSelectColumns(getSqlColumns(null, it, null, true)) }
        val query = select.build()
        println("#### fetchComposition() query: $query")
        val statement = connection.createStatement()
        return try {
            val result = statement.executeQuery(query)
            val responseEntity = responseParentField.getOrNewValue() as MutableEntityModel
            val errors = mutableListOf<String>()
            while (result.next()) {
                requestFields.forEachIndexed { index, requestField ->
                    val responseField = responseEntity.getOrNewField(getFieldName(requestField))
                    errors.addAll(setResponseField(result, index + 1, responseField))
                }
            }
            if (errors.isEmpty()) FetchCompositionResult.Entity(responseEntity)
            else FetchCompositionResult.ErrorList(errors.map { ElementModelError(fieldPath, it) })
        } catch (e: Exception) {
            FetchCompositionResult.ErrorList(
                listOf(ElementModelError(fieldPath, e.message ?: "Exception while getting entity"))
            )
        } finally {
            statement.close()
        }
    }

    override fun fetchCompositionSet(
        fieldPath: String,
        ancestorKeys: List<Keys>,
        requestKeys: List<SingleFieldModel>,
        requestFields: List<FieldModel>,
        responseParentField: MutableSetFieldModel
    ): FetchCompositionSetResult {
        val entityMeta = getCompositionEntityMeta(responseParentField)
        val tableName = getEntityMetaTableFullName(entityMeta)
        val select = SelectCommandBuilder(tableName)
        requestKeys.forEach { requestKey ->
            val columns = getSqlColumns(null, requestKey, null)
            if (requestKey.value == null) select.addSelectColumns(columns)
            else select.addWhereColumns(columns)
        }
        getAncestorKeyColumns(entityMeta, ancestorKeys[0]).forEach { select.addWhereColumn(it) }
        select.addWhereColumn(SqlColumn(null, FIELD_PATH_COLUMN_NAME, fieldPath, Preprocess.QUOTE))
        requestFields.forEach { select.addSelectColumns(getSqlColumns(null, it, null, true)) }
        val query = select.build()
        println("#### fetchCompositionSet() query: $query")
        val statement = connection.createStatement()
        return try {
            val result = statement.executeQuery(query)
            val responseEntities = mutableListOf<MutableEntityModel>()
            val errors = mutableListOf<String>()
            while (result.next()) {
                val responseEntity = getNewMutableSetEntity(responseParentField)
                var columnIndex = 1
                requestKeys.forEach { requestKey ->
                    val responseKey = responseEntity.getOrNewField(getFieldName(requestKey))
                    // Non-null key fields are in the WHERE clause and therefore not part of the result.
                    if (requestKey.value == null) errors.addAll(setResponseField(result, columnIndex++, responseKey))
                    else copy(requestKey, responseKey)
                }
                requestFields.forEach { requestField ->
                    val responseField = responseEntity.getOrNewField(getFieldName(requestField))
                    errors.addAll(setResponseField(result, columnIndex++, responseField))
                }
                responseEntities.add(responseEntity)
            }
            if (errors.isEmpty()) FetchCompositionSetResult.Entities(responseEntities)
            else FetchCompositionSetResult.ErrorList(errors.map { ElementModelError(fieldPath, it) })
        } catch (e: Exception) {
            e.printStackTrace()
            FetchCompositionSetResult.ErrorList(
                listOf(ElementModelError(fieldPath, e.message ?: "Exception while getting entities"))
            )
        } finally {
            statement.close()
        }
    }
}

private fun getAncestorKeyColumns(entityMeta: EntityModel, ancestorKeys: Keys): List<SqlColumn> {
    val ancestorFirstKey = ancestorKeys.available.firstOrNull() ?: return emptyList()
    val ancestorFirstKeyMeta = requireNotNull(ancestorFirstKey.meta) { "Ancestor key field meta is missing" }
    val ancestorEntityMeta =
        requireNotNull(getParentEntityMeta(ancestorFirstKeyMeta)) { "Ancestor key parent entity is missing" }
    val ancestorTableName = getEntityMetaTableName(ancestorEntityMeta)
    return ancestorKeys.available.flatMap { getSqlColumns(ancestorTableName, it, null) }
}

private fun setResponseField(result: ResultSet, columnIndex: Int, responseField: FieldModel): List<String> {
    if (isListField(responseField)) {
        return setResponseListField(result, columnIndex, responseField as MutableListFieldModel)
    } else if (isAssociationField(responseField)) {
        return setResponseAssociationField(result, columnIndex, responseField as MutableSingleFieldModel)
    } else setResponseSingleField(result, columnIndex, responseField as MutableSingleFieldModel)
    return emptyList()
}

private fun setResponseListField(
    result: ResultSet,
    columnIndex: Int,
    responseListField: MutableListFieldModel
): List<String> {
    val json = result.getString(columnIndex)
    val reader = StringReader(json)
    return decodeJsonField(reader, responseListField)
}

private fun setResponseAssociationField(
    result: ResultSet,
    columnIndex: Int,
    responseAssociationField: MutableSingleFieldModel
): List<String> {
    val json = result.getString(columnIndex) ?: return emptyList()
    val reader = StringReader(json)
    return decodeJsonField(reader, responseAssociationField)
}

private fun setResponseSingleField(result: ResultSet, columnIndex: Int, responseSingleField: MutableSingleFieldModel) {
    val responseValue = newMutableValueModel(responseSingleField.meta, responseSingleField)
    setValueFromResult(getFieldType(responseSingleField), responseValue, result, columnIndex)
    responseSingleField.setValue(responseValue)
}

private fun setValueFromResult(
    fieldType: FieldType,
    value: MutableElementModel,
    result: ResultSet,
    columnIndex: Int
) {
    when (fieldType) {
        FieldType.BOOLEAN -> (value as MutablePrimitiveModel).value = result.getBoolean(columnIndex)
        FieldType.UINT8 -> (value as MutablePrimitiveModel).value = result.getByte(columnIndex).toUByte()
        FieldType.UINT16 -> (value as MutablePrimitiveModel).value = result.getShort(columnIndex).toUShort()
        FieldType.UINT32 -> (value as MutablePrimitiveModel).value = result.getInt(columnIndex).toUInt()
        FieldType.UINT64 -> (value as MutablePrimitiveModel).value = result.getLong(columnIndex).toULong()
        FieldType.INT8 -> (value as MutablePrimitiveModel).value = result.getByte(columnIndex)
        FieldType.INT16 -> (value as MutablePrimitiveModel).value = result.getShort(columnIndex)
        FieldType.INT32 -> (value as MutablePrimitiveModel).value = result.getInt(columnIndex)
        FieldType.INT64 -> (value as MutablePrimitiveModel).value = result.getLong(columnIndex)
        FieldType.FLOAT -> (value as MutablePrimitiveModel).value = result.getFloat(columnIndex)
        FieldType.DOUBLE -> (value as MutablePrimitiveModel).value = result.getDouble(columnIndex)
        FieldType.BIG_INTEGER -> (value as MutablePrimitiveModel).value =
            result.getBigDecimal(columnIndex).toBigInteger()
        FieldType.BIG_DECIMAL -> (value as MutablePrimitiveModel).value = result.getBigDecimal(columnIndex)
        FieldType.TIMESTAMP -> (value as MutablePrimitiveModel).value =
            result.getTimestamp(columnIndex, Calendar.getInstance(UTC_TIMEZONE)).time
        FieldType.STRING -> (value as MutablePrimitiveModel).value = result.getString(columnIndex)
        FieldType.UUID -> (value as MutablePrimitiveModel).value = getUuidString(result, columnIndex)
        FieldType.BLOB -> TODO()
        FieldType.PASSWORD1WAY -> result.getString(columnIndex) // TODO #### convert to JSON
        FieldType.PASSWORD2WAY -> result.getString(columnIndex) // TODO #### convert to JSON
        FieldType.ALIAS -> throw UnsupportedOperationException("Aliases are not yet supported")
        FieldType.ENUMERATION -> (value as MutableEnumerationModel).setNumber(result.getInt(columnIndex).toUInt())
        FieldType.ASSOCIATION -> throw IllegalStateException("Setting association as a single SQL value")
        FieldType.COMPOSITION -> throw IllegalStateException("Setting composition as a single SQL value")
    }
}

private fun getUuidString(result: ResultSet, columnIndex: Int): String {
    // TODO(cleanup): better to use `SELECT BIN_TO_UUID(id) as id` in the query and avoid potential endian issues.
    val bytes = result.getBytes(columnIndex)
    val buffer = ByteBuffer.wrap(bytes)
    val mostSignificant = buffer.long
    val leastSignificant = buffer.long
    return UUID(mostSignificant, leastSignificant).toString()
}