package org.treeWare.mySql.operator.delegate

internal class SelectCommandBuilder(private val tableName: String) {
    fun addSelectColumns(columns: List<SqlColumn>) {
        columns.forEach { addSelectColumn(it) }
    }

    fun addSelectColumn(column: SqlColumn) {
        if (selectBuilder.isNotEmpty()) selectBuilder.append(COMMA)
        addSqlColumnName(column, selectBuilder)
    }

    fun addWhereColumns(columns: List<SqlColumn>) {
        columns.forEach { addWhereColumn(it) }
    }

    fun addWhereColumn(column: SqlColumn) {
        if (whereBuilder.isNotEmpty()) whereBuilder.append(AND)
        val nameValueSeparator = if (column.value == null) IS else EQUALS
        addSqlColumn(column, nameValueSeparator, whereBuilder)
    }

    fun build(): String {
        val command = StringBuilder("SELECT ")
            .append(selectBuilder)
            .append(" FROM ")
            .append(tableName)
        if (whereBuilder.isNotEmpty()) command.appendLine().append("  WHERE ").append(whereBuilder)
        command.append(";")
        return command.toString()
    }

    private val selectBuilder = StringBuilder()
    private val whereBuilder = StringBuilder()
}