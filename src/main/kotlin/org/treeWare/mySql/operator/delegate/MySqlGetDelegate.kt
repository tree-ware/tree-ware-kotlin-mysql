package org.treeWare.mySql.operator.delegate

import okio.Buffer
import org.lighthousegames.logging.logging
import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getParentEntityMeta
import org.treeWare.model.core.*
import org.treeWare.model.decoder.decodeJsonField
import org.treeWare.model.operator.*
import org.treeWare.model.operator.get.GetCompositionResult
import org.treeWare.model.operator.get.GetCompositionSetResult
import org.treeWare.model.operator.get.GetDelegate
import org.treeWare.mySql.getBoundSql
import org.treeWare.mySql.operator.*
import org.treeWare.mySql.util.getEntityMetaTableFullName
import org.treeWare.mySql.util.getEntityMetaTableName
import java.nio.ByteBuffer
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.ZoneOffset
import java.util.*

private val UTC_TIMEZONE = TimeZone.getTimeZone(ZoneOffset.UTC)

private val SINGLETON_SQL_COLUMN =
    SingleValuedSqlColumn(
        null,
        SINGLETON_KEY_COLUMN_NAME,
        TypedValue(SINGLETON_KEY_COLUMN_SQL_TYPE, SINGLETON_KEY_COLUMN_VALUE)
    )

private data class SetResponseFieldResult(val errors: List<String>, val columnsConsumed: Int)

class MySqlGetDelegate(
    private val setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?,
    private val getEntityDelegates: EntityDelegateRegistry<GetEntityDelegate>?,
    private val connection: Connection,
    private val logCommands: Boolean
) : GetDelegate {
    override fun getComposition(
        fieldPath: String,
        ancestorKeys: List<Keys>,
        requestFields: List<FieldModel>,
        responseParentField: MutableSingleFieldModel
    ): GetCompositionResult {
        val responseEntity = responseParentField.getOrNewValue() as MutableEntityModel
        if (requestFields.isEmpty()) return GetCompositionResult.Entity(responseEntity)
        val entityMeta = getCompositionEntityMeta(responseParentField)
        val tableName = getEntityMetaTableFullName(entityMeta)
        val select = SelectCommandBuilder(tableName)
        if (ancestorKeys.isEmpty()) select.addWhereColumn(SINGLETON_SQL_COLUMN)
        else getAncestorKeyColumns(ancestorKeys[0]).forEach { select.addWhereColumn(it) }
        select.addWhereColumn(
            SingleValuedSqlColumn(
                null,
                FIELD_PATH_COLUMN_NAME,
                TypedValue(FIELD_PATH_COLUMN_SQL_TYPE, fieldPath)
            )
        )
        requestFields.forEach {
            select.addSelectColumns(
                getSqlColumns(
                    null,
                    it,
                    setEntityDelegates,
                    getEntityDelegates,
                    true
                )
            )
        }
        val statement: PreparedStatement = select.prepareStatement(connection)
        if (logCommands) logger.info { statement.getBoundSql() }
        return try {
            val result = statement.executeQuery()
            val errors = mutableListOf<String>()
            while (result.next()) {
                var columnIndex = 1
                requestFields.forEach { requestField ->
                    val responseField = responseEntity.getOrNewField(getFieldName(requestField))
                    val (fieldErrors, columnsConsumed) = setResponseField(result, columnIndex, responseField)
                    errors.addAll(fieldErrors)
                    columnIndex += columnsConsumed
                }
            }
            if (errors.isEmpty()) GetCompositionResult.Entity(responseEntity)
            else GetCompositionResult.ErrorList(ErrorCode.CLIENT_ERROR, errors.map { ElementModelError(fieldPath, it) })
        } catch (e: Exception) {
            GetCompositionResult.ErrorList(
                ErrorCode.SERVER_ERROR,
                listOf(ElementModelError(fieldPath, e.message ?: "Exception while getting entity"))
            )
        } finally {
            statement.close()
        }
    }

    override fun getCompositionSet(
        fieldPath: String,
        ancestorKeys: List<Keys>,
        requestKeys: List<SingleFieldModel>,
        requestFields: List<FieldModel>,
        responseParentField: MutableSetFieldModel
    ): GetCompositionSetResult {
        if (requestKeys.isEmpty() && requestFields.isEmpty()) return GetCompositionSetResult.Entities(emptyList())
        val entityMeta = getCompositionEntityMeta(responseParentField)
        val tableName = getEntityMetaTableFullName(entityMeta)
        val select = SelectCommandBuilder(tableName)
        requestKeys.forEach { requestKey ->
            val columns = getSqlColumns(null, requestKey, setEntityDelegates)
            select.addSelectColumns(columns)
            if (requestKey.value != null) select.addWhereColumns(columns)
        }
        getAncestorKeyColumns(ancestorKeys[0]).forEach { select.addWhereColumn(it) }
        select.addWhereColumn(
            SingleValuedSqlColumn(
                null,
                FIELD_PATH_COLUMN_NAME,
                TypedValue(FIELD_PATH_COLUMN_SQL_TYPE, fieldPath)
            )
        )
        requestFields.forEach {
            select.addSelectColumns(
                getSqlColumns(
                    null,
                    it,
                    setEntityDelegates,
                    getEntityDelegates,
                    true
                )
            )
        }
        val statement = select.prepareStatement(connection)
        if (logCommands) logger.info { statement.getBoundSql() }
        return try {
            val result = statement.executeQuery()
            val responseEntities = mutableListOf<MutableEntityModel>()
            val errors = mutableListOf<String>()
            while (result.next()) {
                val responseEntity = getNewMutableSetEntity(responseParentField)
                var columnIndex = 1
                requestKeys.forEach { requestKey ->
                    val responseKey = responseEntity.getOrNewField(getFieldName(requestKey))
                    val (fieldErrors, columnsConsumed) = setResponseField(result, columnIndex, responseKey)
                    errors.addAll(fieldErrors)
                    columnIndex += columnsConsumed
                }
                requestFields.forEach { requestField ->
                    val responseField = responseEntity.getOrNewField(getFieldName(requestField))
                    val (fieldErrors, columnsConsumed) = setResponseField(result, columnIndex, responseField)
                    errors.addAll(fieldErrors)
                    columnIndex += columnsConsumed
                }
                responseEntities.add(responseEntity)
            }
            if (errors.isEmpty()) GetCompositionSetResult.Entities(responseEntities)
            else GetCompositionSetResult.ErrorList(
                ErrorCode.CLIENT_ERROR,
                errors.map { ElementModelError(fieldPath, it) })
        } catch (e: Exception) {
            e.printStackTrace()
            GetCompositionSetResult.ErrorList(
                ErrorCode.SERVER_ERROR,
                listOf(ElementModelError(fieldPath, e.message ?: "Exception while getting entities"))
            )
        } finally {
            statement.close()
        }
    }

    private fun getAncestorKeyColumns(ancestorKeys: Keys): List<SqlColumn> {
        val ancestorFirstKey = ancestorKeys.available.firstOrNull() ?: return emptyList()
        val ancestorFirstKeyMeta = requireNotNull(ancestorFirstKey.meta) { "Ancestor key field meta is missing" }
        val ancestorEntityMeta =
            requireNotNull(getParentEntityMeta(ancestorFirstKeyMeta)) { "Ancestor key parent entity is missing" }
        val ancestorTableName = getEntityMetaTableName(ancestorEntityMeta)
        return ancestorKeys.available.flatMap { getSqlColumns(ancestorTableName, it, setEntityDelegates) }
    }

    private fun setResponseField(
        result: ResultSet,
        columnIndex: Int,
        responseField: FieldModel
    ): SetResponseFieldResult =
        if (isListField(responseField)) {
            setResponseListField(result, columnIndex, responseField as MutableListFieldModel)
        } else when (getFieldType(responseField)) {
            FieldType.ASSOCIATION ->
                setResponseAssociationField(result, columnIndex, responseField as MutableSingleFieldModel)
            FieldType.COMPOSITION ->
                setResponseCompositionField(result, columnIndex, responseField as MutableSingleFieldModel)
            else -> {
                setResponseSingleField(result, columnIndex, responseField as MutableSingleFieldModel)
                SetResponseFieldResult(emptyList(), 1)
            }
        }

    private fun setResponseListField(
        result: ResultSet,
        columnIndex: Int,
        responseListField: MutableListFieldModel
    ): SetResponseFieldResult {
        val json = result.getString(columnIndex) ?: return SetResponseFieldResult(emptyList(), 1)
        val buffer = Buffer().writeUtf8(json)
        val decodeErrors = decodeJsonField(buffer, responseListField)
        return SetResponseFieldResult(decodeErrors, 1)
    }

    private fun setResponseAssociationField(
        result: ResultSet,
        columnIndex: Int,
        responseAssociationField: MutableSingleFieldModel
    ): SetResponseFieldResult {
        val json = result.getString(columnIndex) ?: return SetResponseFieldResult(emptyList(), 1)
        val buffer = Buffer().writeUtf8(json)
        val decodeErrors = decodeJsonField(buffer, responseAssociationField)
        return SetResponseFieldResult(decodeErrors, 1)
    }

    private fun setResponseCompositionField(
        result: ResultSet,
        columnIndex: Int,
        responseCompositionField: MutableSingleFieldModel
    ): SetResponseFieldResult {
        val compositionMeta = getMetaModelResolved(responseCompositionField.meta)?.compositionMeta
        val entityFullName = getMetaModelResolved(compositionMeta)?.fullName
        val entityDelegate = getEntityDelegates?.get(entityFullName) as MySqlGetEntityDelegate?
            ?: throw IllegalStateException("Setting composition as a single SQL value")
        val entity = newMutableValueModel(responseCompositionField.meta, responseCompositionField) as MutableEntityModel
        responseCompositionField.setValue(entity)
        val columnsConsumed = entityDelegate.setValueFromResult(entity, result, columnIndex)
        return SetResponseFieldResult(emptyList(), columnsConsumed)
    }

    private fun setResponseSingleField(
        result: ResultSet,
        columnIndex: Int,
        responseSingleField: MutableSingleFieldModel
    ) {
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
        FieldType.BLOB -> result.getBytes(columnIndex)?.let { bytes ->
            newMutableValueModel(responseSingleField.meta, responseSingleField).also {
                (it as MutablePrimitiveModel).value = bytes
            }
        }
        FieldType.PASSWORD1WAY,
        FieldType.PASSWORD2WAY -> result.getString(columnIndex)?.let { string ->
            val buffer = Buffer().writeUtf8(string)
            decodeJsonField(buffer, responseSingleField)
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