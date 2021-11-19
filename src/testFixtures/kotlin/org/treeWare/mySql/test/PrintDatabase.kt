package org.treeWare.mySql.test

import java.io.Writer
import java.sql.Connection
import java.sql.ResultSet

fun printDatabase(connection: Connection, database: String, writer: Writer) {
    writer.appendLine("+ Database $database +")
    val tables = getTableNames(connection, database)
    tables.forEach {
        writer.appendLine()
        printTable(connection, database, it, writer)
    }
}

fun printTable(connection: Connection, database: String, table: String, writer: Writer) {
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery("SELECT * FROM $database.$table")
    printResultSet(resultSet, writer)
    statement.close()
}

fun printResultSet(result: ResultSet, writer: Writer) {
    val metaData = result.metaData
    val tableName = metaData.getTableName(1)
    writer.appendLine("= Table $tableName =")
    while (result.next()) {
        writer.appendLine()
        writer.appendLine("* Row ${result.row} *")
        for (i in 1..metaData.columnCount) {
            writer.append(metaData.getColumnName(i))
            writer.append(": ")
            writer.appendLine(getValue(result, i))
        }
    }
}

fun getValue(result: ResultSet, column: Int): String? = when (result.metaData.getColumnTypeName(column)) {
    "BINARY" -> getUuidValue(result, column)
    else -> result.getString(column)
}

fun getUuidValue(result: ResultSet, column: Int): String {
    val bytes = result.getBytes(column)
    if (bytes.size != 16) return "Invalid UUID: ${bytes.size} bytes instead of 16 bytes"
    val uuid = StringBuffer()
    bytes.forEachIndexed { index, byte ->
        val hex = "%02x".format(byte)
        when (index) {
            4, 6, 8, 10 -> uuid.append("-")
        }
        uuid.append(hex)
    }
    return uuid.toString()
}