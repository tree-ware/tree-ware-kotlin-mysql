package org.treeWare.mySql.operator.delegate

internal class InsertCommandBuilder(private val tableName: String) {
    fun addColumns(columns: List<SqlColumn>) {
        columns.forEach { addColumn(it) }
    }

    fun addColumn(column: SqlColumn) {
        if (nameBuilder.length > BEGIN_LENGTH) {
            nameBuilder.append(COMMA)
            valueBuilder.append(COMMA)
        }
        addSqlColumnName(column, nameBuilder)
        addSqlColumnValue(column, valueBuilder)
    }

    fun build(): String {
        nameBuilder.append(")")
        valueBuilder.append(")")
        return StringBuilder("INSERT INTO ")
            .appendLine(tableName)
            .appendLine(nameBuilder)
            .appendLine("  VALUES")
            .append(valueBuilder)
            .append(";")
            .toString()
    }

    private val nameBuilder = StringBuilder(BEGIN)
    private val valueBuilder = StringBuilder(BEGIN)
}

internal class UpdateCommandBuilder(private val tableName: String) {
    fun addUpdateColumns(columns: List<SqlColumn>) {
        columns.forEach { addUpdateColumn(it) }
    }

    fun addUpdateColumn(column: SqlColumn) {
        if (updateBuilder.isNotEmpty()) updateBuilder.append(COMMA)
        addSqlColumn(column, EQUALS, updateBuilder)
    }

    fun addWhereColumns(columns: List<SqlColumn>) {
        columns.forEach { addWhereColumn(it) }
    }

    fun addWhereColumn(column: SqlColumn) {
        if (whereBuilder.isNotEmpty()) whereBuilder.append(AND)
        addSqlColumn(column, EQUALS, whereBuilder)
    }

    fun build(): String = StringBuilder("UPDATE ")
        .appendLine(tableName)
        .append("  SET ")
        .appendLine(updateBuilder)
        .append("  WHERE ")
        .append(whereBuilder)
        .append(";")
        .toString()

    private val updateBuilder = StringBuilder()
    private val whereBuilder = StringBuilder()
}

internal class DeleteCommandBuilder(private val tableName: String) {
    fun addWhereColumns(columns: List<SqlColumn>) {
        columns.forEach { addWhereColumn(it) }
    }

    fun addWhereColumn(column: SqlColumn) {
        if (whereBuilder.isNotEmpty()) whereBuilder.append(AND)
        addSqlColumn(column, EQUALS, whereBuilder)
    }

    fun build(): String = StringBuilder("DELETE FROM ")
        .appendLine(tableName)
        .append("  WHERE ")
        .append(whereBuilder)
        .append(";")
        .toString()

    private val whereBuilder = StringBuilder()
}