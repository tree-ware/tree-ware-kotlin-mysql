package org.treeWare.mySql.operator.delegate

import org.lighthousegames.logging.logging
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
        if (logCommands) logger.info { query }
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
        if (logCommands) logger.info { query }
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
    val json = result.getString(columnIndex) ?: return emptyList()
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
    val responseValue = getValueFromResult(responseSingleField, result, columnIndex)
    responseSingleField.setValue(responseValue)
}

private fun getValueFromResult(
    responseSingleField: MutableSingleFieldModel,
    result: ResultSet,
    columnIndex: Int
): MutableElementModel? = when (getFieldType(responseSingleField)) {
    FieldType.BOOLEAN -> result.getBoolean(columnIndex).let { boolean ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = boolean
        }
    }
    FieldType.UINT8 -> result.getByte(columnIndex).toUByte().let { number ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = number
        }
    }
    FieldType.UINT16 -> result.getShort(columnIndex).toUShort().let { number ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = number
        }
    }
    FieldType.UINT32 -> result.getInt(columnIndex).toUInt().let { number ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = number
        }
    }
    FieldType.UINT64 -> result.getLong(columnIndex).toULong().let { number ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = number
        }
    }
    FieldType.INT8 -> result.getByte(columnIndex).let { number ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = number
        }
    }
    FieldType.INT16 -> result.getShort(columnIndex).let { number ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = number
        }
    }
    FieldType.INT32 -> result.getInt(columnIndex).let { number ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = number
        }
    }
    FieldType.INT64 -> result.getLong(columnIndex).let { number ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = number
        }
    }
    FieldType.FLOAT -> result.getFloat(columnIndex).let { number ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = number
        }
    }
    FieldType.DOUBLE -> result.getDouble(columnIndex).let { number ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = number
        }
    }
    FieldType.BIG_INTEGER -> result.getBigDecimal(columnIndex)?.toBigInteger()?.let { number ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = number
        }
    }
    FieldType.BIG_DECIMAL -> result.getBigDecimal(columnIndex)?.let { number ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = number
        }
    }
    FieldType.TIMESTAMP -> result.getTimestamp(columnIndex, Calendar.getInstance(UTC_TIMEZONE))?.time?.let { time ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = time
        }
    }
    FieldType.STRING -> result.getString(columnIndex)?.let { string ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = string
        }
    }
    FieldType.UUID -> getUuidString(result, columnIndex)?.let { uuid ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutablePrimitiveModel).value = uuid
        }
    }
    FieldType.BLOB -> TODO() // TODO #### implement
    FieldType.PASSWORD1WAY,
    FieldType.PASSWORD2WAY -> result.getString(columnIndex)?.let { string ->
        val reader = StringReader(string)
        decodeJsonField(reader, responseSingleField)
        responseSingleField.value
    }
    FieldType.ALIAS -> throw UnsupportedOperationException("Aliases are not yet supported")
    FieldType.ENUMERATION -> result.getInt(columnIndex).let { number ->
        newMutableValueModel(responseSingleField.meta, responseSingleField).also {
            (it as MutableEnumerationModel).setNumber(number.toUInt())
        }
    }
    FieldType.ASSOCIATION -> throw IllegalStateException("Setting association as a single SQL value")
    FieldType.COMPOSITION -> throw IllegalStateException("Setting composition as a single SQL value")
}

private fun getUuidString(result: ResultSet, columnIndex: Int): String? {
    // TODO(cleanup): better to use `SELECT BIN_TO_UUID(id) as id` in the query and avoid potential endian issues.
    val bytes = result.getBytes(columnIndex) ?: return null
    val buffer = ByteBuffer.wrap(bytes)
    val mostSignificant = buffer.long
    val leastSignificant = buffer.long
    return UUID(mostSignificant, leastSignificant).toString()
}

private val logger = logging()