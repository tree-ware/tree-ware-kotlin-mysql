package org.treeWare.mySql.test

import java.io.Writer
import java.nio.ByteBuffer
import java.sql.ResultSet
import javax.sql.DataSource

fun printDatabase(dataSource: DataSource, database: String, writer: Writer) {
    writer.appendLine("+ Database $database +")
    val tables = getTableNames(dataSource, database)
    tables.forEach {
        writer.appendLine()
        printTable(dataSource, database, it, writer)
    }
}

fun printTable(dataSource: DataSource, database: String, table: String, writer: Writer) {
    dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery("SELECT * FROM $database.$table")
            printResultSet(resultSet, writer)
        }
    }
}

fun printResultSet(result: ResultSet, writer: Writer, withoutRowNumbers: Boolean = false) {
    val metaData = result.metaData
    val tableName = metaData.getTableName(1)
    if (tableName.isNotEmpty()) writer.appendLine("= Table $tableName =")
    while (result.next()) {
        writer.appendLine()
        if (!withoutRowNumbers) writer.appendLine("* Row ${result.row} *")
        for (i in 1..metaData.columnCount) {
            writer.append(metaData.getColumnName(i))
            writer.append(":")
            val value = getValue(result, i)
            if (value.isNotBlank()) writer.append(" ")
            writer.appendLine(value)
        }
    }
}

fun getValue(result: ResultSet, column: Int): String = when (result.metaData.getColumnTypeName(column)) {
    "BINARY" -> getUuidValue(result, column)
    "GEOMETRY" -> getPointValue(result, column)
    else -> result.getString(column) ?: "null"
}

fun getUuidValue(result: ResultSet, column: Int): String {
    val bytes = result.getBytes(column) ?: return "null"
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

fun getPointValue(result: ResultSet, column: Int): String {
    val bytes = result.getBytes(column) ?: return "null"
    // MySQL uses little-endian order for the bytes. Reverse them to get big-endian.
    // Note that reversing also reverse the order of the fields.
    bytes.reverse()
    val buffer = ByteBuffer.wrap(bytes)
    val latitude = buffer.double
    val longitude = buffer.double
    val wkbType = buffer.int
    if (wkbType != 1) throw IllegalStateException("Geometry is not a point")
    val byteOrder = buffer.get()
    if (byteOrder != 1.toByte()) throw IllegalStateException("Geometry value is not little-endian")
    val srid = buffer.int
    return "Point(latitude: $latitude, longitude: $longitude, SRID: $srid)"
}