package org.treeWare.mySql.operator.delegate

import java.sql.Connection
import java.sql.PreparedStatement

internal class SelectCommandBuilder(private val tableName: String) {
    fun addSelectColumns(columns: List<SqlColumn>) {
        columns.forEach { addSelectColumn(it) }
    }

    private fun addSelectColumn(column: SqlColumn) {
        if (selectBuilder.isNotEmpty()) selectBuilder.append(COMMA)
        addSqlColumnName(column, selectBuilder)
    }

    fun addWhereColumns(columns: List<SqlColumn>) {
        columns.forEach { addWhereColumn(it) }
    }

    fun addWhereColumn(column: SqlColumn) {
        if (whereColumns.isNotEmpty()) whereBuilder.append(AND)
        val nameValueSeparator = if (column.isNull()) IS else EQUALS
        addSqlColumnPlaceholder(column, nameValueSeparator, whereBuilder)
        whereColumns.add(column)
    }

    fun prepareStatement(connection: Connection): PreparedStatement {
        val command = StringBuilder("SELECT ")
            .append(selectBuilder)
            .append(" FROM ")
            .append(tableName)
        if (whereColumns.isNotEmpty()) command.appendLine().append("  WHERE ").append(whereBuilder)
        command.append(";")
        val statement = connection.prepareStatement(command.toString())
        whereColumns.fold(1) { index, column -> column.bindValues(statement, index) }
        return statement
    }

    private val selectBuilder = StringBuilder()
    private val whereBuilder = StringBuilder()
    private val whereColumns = mutableListOf<SqlColumn>()
}