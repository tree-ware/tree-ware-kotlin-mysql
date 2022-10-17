package org.treeWare.mySql.operator.delegate

import java.sql.Connection
import java.sql.PreparedStatement

internal class InsertCommandBuilder(private val tableName: String) {
    fun addColumns(columns: List<SqlColumn>) {
        columns.forEach { addColumn(it) }
    }

    fun addColumn(column: SqlColumn) {
        if (nameBuilder.length > BEGIN_LENGTH) {
            nameBuilder.append(COMMA)
            valuePlaceholderBuilder.append(COMMA)
        }
        addSqlColumnName(column, nameBuilder)
        valuePlaceholderBuilder.append(column.placeholder)
        columns.add(column)
    }

    fun prepareStatement(connection: Connection): PreparedStatement {
        nameBuilder.append(")")
        valuePlaceholderBuilder.append(")")
        val command = StringBuilder("INSERT INTO ")
            .appendLine(tableName)
            .appendLine(nameBuilder)
            .appendLine("  VALUES")
            .append(valuePlaceholderBuilder)
            .append(";")
        val statement = connection.prepareStatement(command.toString())
        columns.fold(1) { index, column -> column.bindValues(statement, index) }
        return statement
    }

    private val nameBuilder = StringBuilder(BEGIN)
    private val valuePlaceholderBuilder = StringBuilder(BEGIN)
    private val columns = mutableListOf<SqlColumn>()
}

internal class UpdateCommandBuilder(private val tableName: String) {
    fun addUpdateColumns(columns: List<SqlColumn>) {
        columns.forEach { addUpdateColumn(it) }
    }

    fun addUpdateColumn(column: SqlColumn) {
        if (updatePlaceholderBuilder.isNotEmpty()) updatePlaceholderBuilder.append(COMMA)
        addSqlColumnPlaceholder(column, EQUALS, updatePlaceholderBuilder)
        updateColumns.add(column)
    }

    fun addWhereColumns(columns: List<SqlColumn>) {
        columns.forEach { addWhereColumn(it) }
    }

    fun addWhereColumn(column: SqlColumn) {
        if (wherePlaceholderBuilder.isNotEmpty()) wherePlaceholderBuilder.append(AND)
        addSqlColumnPlaceholder(column, EQUALS, wherePlaceholderBuilder)
        whereColumns.add(column)
    }

    fun prepareStatement(connection: Connection): PreparedStatement {
        val command = StringBuilder("UPDATE ")
            .appendLine(tableName)
            .append("  SET ")
            .appendLine(updatePlaceholderBuilder)
            .append("  WHERE ")
            .append(wherePlaceholderBuilder)
            .append(";")
        val statement = connection.prepareStatement(command.toString())
        val indexAfterUpdate = updateColumns.fold(1) { index, column -> column.bindValues(statement, index) }
        whereColumns.fold(indexAfterUpdate) { index, column -> column.bindValues(statement, index) }
        return statement
    }

    private val updatePlaceholderBuilder = StringBuilder()
    private val updateColumns = mutableListOf<SqlColumn>()
    private val wherePlaceholderBuilder = StringBuilder()
    private val whereColumns = mutableListOf<SqlColumn>()
}

internal class DeleteCommandBuilder(private val tableName: String) {
    fun addWhereColumns(columns: List<SqlColumn>) {
        columns.forEach { addWhereColumn(it) }
    }

    fun addWhereColumn(column: SqlColumn) {
        if (wherePlaceholderBuilder.isNotEmpty()) wherePlaceholderBuilder.append(AND)
        addSqlColumnPlaceholder(column, EQUALS, wherePlaceholderBuilder)
        whereColumns.add(column)
    }

    fun prepareStatement(connection: Connection): PreparedStatement {
        val command = StringBuilder("DELETE FROM ")
            .appendLine(tableName)
            .append("  WHERE ")
            .append(wherePlaceholderBuilder)
            .append(";")
        val statement = connection.prepareStatement(command.toString())
        whereColumns.fold(1) { index, column -> column.bindValues(statement, index) }
        return statement
    }

    private val wherePlaceholderBuilder = StringBuilder()
    private val whereColumns = mutableListOf<SqlColumn>()
}