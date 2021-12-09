package org.treeWare.mySql.operator

import org.treeWare.metaModel.*
import org.treeWare.model.core.*
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.encodeJson
import org.treeWare.model.traversal.AbstractLeader1Follower0ModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import org.treeWare.mySql.aux.getMySqlMetaModelMap
import java.io.StringWriter
import java.time.Instant

fun generateSetCommands(mainModel: MainModel): List<String> {
    val visitor = GenerateSetCommandsVisitor()
    forEach(mainModel, visitor)
    return visitor.commands
}

private class CommandState(parentId: String) {
    private val commandBuffer = StringBuffer()
    private val columnNames = mutableListOf<String>()
    private val columnValues = mutableListOf<String>()
    private val columnUpdates = mutableListOf<String>()

    init {
        addColumnName(PARENT_ID_COLUMN_NAME)
        addColumnValue(parentId)
        addColumnUpdate(PARENT_ID_COLUMN_NAME, parentId)
    }

    fun insertIntoTable(tableName: String) {
        commandBuffer.appendLine("INSERT INTO $tableName")
    }

    fun addColumnName(name: String) {
        columnNames.add(name)
    }

    fun addColumnValue(value: String) {
        columnValues.add(value)
    }

    fun addColumnUpdate(name: String, value: String) {
        columnUpdates.add("$name = $value")
    }

    fun getCommand(): String {
        commandBuffer
            .append("  (")
            .append(columnNames.joinToString(", "))
            .appendLine(")")
            .appendLine("  VALUES")
            .append("  (")
            .append(columnValues.joinToString(", "))
            .appendLine(")")
            .appendLine("  ON DUPLICATE KEY UPDATE")
            .append("  ")
            .append(columnUpdates.joinToString(",\n  "))
            .append(";")
        return commandBuffer.toString()
    }
}

private class GenerateSetCommandsVisitor :
    AbstractLeader1Follower0ModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val commands = mutableListOf<String>()

    private val commandStateStack = ArrayDeque<CommandState>()
    private val parentPath = ArrayDeque<String>()

    override fun visit(leaderRoot1: RootModel): TraversalAction {
        val mainMeta = leaderRoot1.parent.meta ?: throw IllegalStateException("Main meta is missing")
        val unresolvedRootMeta = getRootMeta(mainMeta)
        val compositionMeta = unresolvedRootMeta.getAux<Resolved>(RESOLVED_AUX)?.compositionMeta
            ?: throw IllegalStateException("Root meta-model is not resolved")
        val mySqlMap = getMySqlMetaModelMap(compositionMeta) ?: return TraversalAction.CONTINUE
        val validated = mySqlMap.validated ?: throw IllegalStateException("MySQL root meta-model map is not validated")
        val commandState = CommandState(getJsonEncodedParentPath())
        commandStateStack.addLast(commandState)
        commandState.insertIntoTable(validated.sqlIdentifier)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderRoot1: RootModel) {
        val commandState = commandStateStack.removeLast()
        commands.add(commandState.getCommand())
    }


    override fun visit(leaderEntity1: EntityModel): TraversalAction {
        val parentFieldMeta = leaderEntity1.parent.meta ?: throw IllegalStateException("No parent field for entity")
        val compositionMeta = parentFieldMeta.getAux<Resolved>(RESOLVED_AUX)?.compositionMeta
            ?: throw IllegalStateException("Composition meta-model is not resolved")
        val mySqlMap = getMySqlMetaModelMap(compositionMeta) ?: return TraversalAction.CONTINUE
        val validated =
            mySqlMap.validated ?: throw IllegalStateException("MySQL entity meta-model map is not validated")
        val commandState = CommandState(getJsonEncodedParentPath())
        commandStateStack.addLast(commandState)
        commandState.insertIntoTable(validated.sqlIdentifier)
        addKeysToParentPath(leaderEntity1)
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderEntity1: EntityModel) {
        val commandState = commandStateStack.removeLast()
        removeKeysFromParentPath(leaderEntity1)
        commands.add(commandState.getCommand())
    }

    override fun visit(leaderField1: SingleFieldModel): TraversalAction {
        if (isCompositionFieldMeta(leaderField1.meta)) {
            val fieldName = getFieldName(leaderField1)
            parentPath.addLast("\"$fieldName\"")
        }
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderField1: SingleFieldModel) {
        if (isCompositionFieldMeta(leaderField1.meta)) parentPath.removeLast()
    }

    override fun visit(leaderField1: ListFieldModel): TraversalAction {
        val fieldName = getFieldName(leaderField1)
        addAsJsonValue(fieldName, leaderField1)
        return TraversalAction.ABORT_SUB_TREE
    }

    override fun visit(leaderField1: SetFieldModel): TraversalAction {
        val fieldName = getFieldName(leaderField1)
        if (isCompositionFieldMeta(leaderField1.meta)) {
            parentPath.addLast("\"$fieldName\"")
        }
        return TraversalAction.CONTINUE
    }

    override fun leave(leaderField1: SetFieldModel) {
        if (isCompositionFieldMeta(leaderField1.meta)) {
            parentPath.removeLast()
        }
    }

    override fun visit(leaderValue1: PrimitiveModel): TraversalAction {
        val fieldName = getFieldName(leaderValue1.parent)
        val value = leaderValue1.value
        val sqlValue: String = if (value == null) "NULL"
        else when (val fieldType = leaderValue1.parent.meta?.let { getFieldTypeMeta(it) }) {
            FieldType.BOOLEAN,
            FieldType.BYTE,
            FieldType.SHORT,
            FieldType.INT,
            FieldType.LONG,
            FieldType.FLOAT,
            FieldType.DOUBLE -> "$value"
            FieldType.STRING -> "'$value'"
            FieldType.UUID -> "UUID_TO_BIN('$value')"
            FieldType.BLOB -> getHexBlob(value as ByteArray)
            FieldType.TIMESTAMP -> "'${getIso8601DateTime(value as Long)}'"
            else -> throw IllegalStateException("Invalid primitive field type: $fieldType")
        }
        val lastState = commandStateStack.last()
        lastState.addColumnName(fieldName)
        lastState.addColumnValue(sqlValue)
        lastState.addColumnUpdate(fieldName, sqlValue)
        return TraversalAction.CONTINUE
    }

    override fun visit(leaderValue1: Password1wayModel): TraversalAction {
        val fieldName = getFieldName(leaderValue1.parent)
        // TODO(deepak-nulu): return errors if there are unhashed/unencrypted passwords instead of excluding them.
        addAsJsonValue(fieldName, leaderValue1, EmptyJsonPolicy.EXCLUDE)
        return TraversalAction.CONTINUE
    }

    override fun visit(leaderValue1: Password2wayModel): TraversalAction {
        val fieldName = getFieldName(leaderValue1.parent)
        // TODO(deepak-nulu): return errors if there are unhashed/unencrypted passwords instead of excluding them.
        addAsJsonValue(fieldName, leaderValue1, EmptyJsonPolicy.EXCLUDE)
        return TraversalAction.CONTINUE
    }

    override fun visit(leaderValue1: EnumerationModel): TraversalAction {
        val fieldName = getFieldName(leaderValue1.parent)
        val sqlValue = leaderValue1.value?.let { "'$it'" } ?: "NULL"
        val lastState = commandStateStack.last()
        lastState.addColumnName(fieldName)
        lastState.addColumnValue(sqlValue)
        lastState.addColumnUpdate(fieldName, sqlValue)
        return TraversalAction.CONTINUE
    }

    override fun visit(leaderValue1: AssociationModel): TraversalAction {
        val fieldName = getFieldName(leaderValue1.parent)
        addAsJsonValue(fieldName, leaderValue1)
        return TraversalAction.CONTINUE
    }

    // Helpers

    private fun addKeysToParentPath(entity: EntityModel) {
        val writer = StringWriter()
        // Remove the field name from the top of the path stack and include it
        // as part of the parent ID (the field name will be restored onto the
        // stack when leaving this entity).
        val entityFieldName = parentPath.removeLast()
        // entityFieldName is already double-quoted, so no need to quote it.
        writer.append("{").append(entityFieldName).append(":{")
        // NOTE: the order of the key fields has to be the same every time.
        // So they are all fetched here using the order in the meta-model
        // rather than visit()ing them in the order in the current model.
        val keyFields = entity.getKeyFields()
        keyFields.forEachIndexed { index, keyField ->
            if (index != 0) writer.append(",")
            val fieldName = keyField.meta?.let { getMetaName(it) } ?: ""
            writer.append("\"").append(fieldName).append("\":")
            encodeJson(keyField, writer)
        }
        writer.append("}}")
        // The top of the stack has the field name which is also part
        parentPath.addLast(writer.toString())
    }

    private fun removeKeysFromParentPath(entity: EntityModel) {
        // Replace entity keys in stack with just the field name (opposite of
        // what was done when visiting the entity).
        parentPath.removeLast()
        val entityFieldName = entity.parent.meta?.let { getMetaName(it) } ?: ""
        parentPath.addLast("\"$entityFieldName\"")
    }

    private fun getJsonEncodedParentPath(): String {
        return "'[${parentPath.joinToString(",")}]'"
    }

    private enum class EmptyJsonPolicy { USE_NULL, EXCLUDE }

    private fun addAsJsonValue(
        fieldName: String,
        element: ElementModel,
        emptyJsonPolicy: EmptyJsonPolicy = EmptyJsonPolicy.USE_NULL
    ) {
        val writer = StringWriter()
        encodeJson(element, writer, encodePasswords = EncodePasswords.HASHED_AND_ENCRYPTED)
        val jsonValue = writer.toString()
        if (jsonValue.isEmpty() && emptyJsonPolicy == EmptyJsonPolicy.EXCLUDE) return
        val sqlValue = "'$jsonValue'"
        val lastState = commandStateStack.last()
        lastState.addColumnName(fieldName)
        lastState.addColumnValue(sqlValue)
        lastState.addColumnUpdate(fieldName, sqlValue)
    }
}

private fun getHexBlob(blob: ByteArray): String = "0x" + blob.joinToString(separator = "") { "%02x".format(it) }

private fun getIso8601DateTime(timestampMilliseconds: Long): String =
    // TODO(deepak-nulu): Timezone offsets are supported only in MySQL 8.0.19
    Instant.ofEpochMilli(timestampMilliseconds).toString()
        // Instant.ofEpochMilli() returns UTC timezone as the character 'Z' at the end.
        // drop it until the test upgrades to MySQL 8.0.19 or later.
        .let { if (it.endsWith('Z')) it.dropLast(1) else it }
