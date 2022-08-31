package org.treeWare.mySql.ddl

import org.treeWare.model.core.*
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.mySql.ddl.traversal.Leader1DdlVisitor
import java.io.StringWriter

class GenerateDdlCommandsVisitor : Leader1DdlVisitor<TraversalAction> {
    val commands: List<String> get() = createCommands + alterCommands

    private val createCommands = mutableListOf<String>()
    private val alterCommands = mutableListOf<String>()

    private var createTableCommand = StringWriter()
    private var createTableHasContent = false
    private var alterTableCommand = StringWriter()
    private var alterTableHasContent = false

    override fun visitDatabase(leaderDatabase1: EntityModel): TraversalAction {
        val databaseName = getSingleString(leaderDatabase1, "name")
        val command = "CREATE DATABASE IF NOT EXISTS $databaseName;"
        createCommands.add(command)
        return TraversalAction.CONTINUE
    }

    override fun leaveDatabase(leaderDatabase1: EntityModel) {}

    override fun visitTable(leaderTable1: EntityModel): TraversalAction {
        val tableName = getSingleString(leaderTable1, "name")
        createTableCommand = StringWriter()
        createTableHasContent = false
        createTableCommand.append("CREATE TABLE IF NOT EXISTS $tableName (")
        alterTableCommand = StringWriter()
        alterTableHasContent = false
        alterTableCommand.append("ALTER TABLE $tableName")
        return TraversalAction.CONTINUE
    }

    override fun leaveTable(leaderTable1: EntityModel) {
        createTableCommand.appendLine().append(") ENGINE = InnoDB;")
        createCommands.add(createTableCommand.toString())
        if (alterTableHasContent) {
            alterTableCommand.append(";")
            alterCommands.add(alterTableCommand.toString())
        }
    }

    override fun visitColumn(leaderColumn1: EntityModel): TraversalAction {
        val columnName = getSingleString(leaderColumn1, "name")
        val columnType = getSingleString(leaderColumn1, "type")
        createTableCommand
            .appendLine(if (createTableHasContent) "," else "")
            .append("  $columnName $columnType")
        createTableHasContent = true
        return TraversalAction.CONTINUE
    }

    override fun leaveColumn(leaderColumn1: EntityModel) {}

    override fun visitPrimaryKey(leaderField1: ListFieldModel): TraversalAction {
        val columnNames = getPrimitiveValues(leaderField1)
        createTableCommand.appendLine(",")
            .append("  PRIMARY KEY (")
            .append(columnNames.joinToString())
            .append(")")
        return TraversalAction.CONTINUE
    }

    override fun leavePrimaryKey(leaderField1: ListFieldModel) {}

    override fun visitIndex(leaderIndex1: EntityModel): TraversalAction {
        val indexName = getSingleString(leaderIndex1, "name")
        val isUnique = getSingleBoolean(leaderIndex1, "is_unique")
        val columns = getPrimitiveValues(getCollectionField(leaderIndex1, "columns"))
        createTableCommand
            .appendLine(",").append("  ")
            .append(if (isUnique) "UNIQUE " else "")
            .append("INDEX ")
            .append(indexName)
            .append(" (")
            .append(columns.joinToString())
            .append(")")
        return TraversalAction.CONTINUE
    }

    override fun leaveIndex(leaderIndex1: EntityModel) {}

    override fun visitForeignKey(leaderForeignKey1: EntityModel): TraversalAction {
        val sourceColumns = getPrimitiveValues(getCollectionField(leaderForeignKey1, "source_columns"))
        val targetTable = getSingleString(leaderForeignKey1, "target_table")
        val targetKeys = getPrimitiveValues(getCollectionField(leaderForeignKey1, "target_keys"))
        alterTableCommand
            .appendLine(if (alterTableHasContent) "," else "")
            .append("  ADD FOREIGN KEY ")
            .append("(")
            .append(sourceColumns.joinToString())
            .append(")")
            .append(" REFERENCES ")
            .append(targetTable)
            .append("(")
            .append(targetKeys.joinToString())
            .append(") ON DELETE RESTRICT")
        alterTableHasContent = true
        return TraversalAction.CONTINUE
    }

    override fun leaveForeignKey(leaderForeignKey1: EntityModel) {}
}

private fun getPrimitiveValues(stringListField: CollectionFieldModel): List<Any> = stringListField.values
    .filterIsInstance<PrimitiveModel>()
    .map { it.value }