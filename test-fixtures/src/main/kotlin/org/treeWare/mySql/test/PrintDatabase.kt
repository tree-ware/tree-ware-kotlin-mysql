package org.treeWare.mySql.test

import okio.BufferedSink
import okio.Sink
import org.treeWare.util.buffered
import java.nio.ByteBuffer
import java.sql.ResultSet
import javax.sql.DataSource

fun printDatabase(dataSource: DataSource, database: String, sink: Sink) {
    sink.buffered().use { bufferedSink ->
        bufferedSink.writeUtf8("+ Database $database +\n")
        val tables = getTableNames(dataSource, database)
        tables.forEach {
            bufferedSink.writeUtf8("\n")
            printTable(dataSource, database, it, bufferedSink)
        }
    }
}

fun printTable(dataSource: DataSource, database: String, table: String, bufferedSink: BufferedSink) {
    dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery("SELECT * FROM $database.$table")
            printResultSet(resultSet, bufferedSink)
        }
    }
}

fun printResultSet(result: ResultSet, bufferedSink: BufferedSink, withoutRowNumbers: Boolean = false) {
    val metaData = result.metaData
    val tableName = metaData.getTableName(1)
    if (tableName.isNotEmpty()) bufferedSink.writeUtf8("= Table $tableName =\n")
    while (result.next()) {
        bufferedSink.writeUtf8("\n")
        if (!withoutRowNumbers) bufferedSink.writeUtf8("* Row ${result.row} *\n")
        for (i in 1..metaData.columnCount) {
            bufferedSink.writeUtf8(metaData.getColumnName(i))
            bufferedSink.writeUtf8(":")
            val value = getValue(result, i)
            if (value.isNotBlank()) bufferedSink.writeUtf8(" ")
            bufferedSink.writeUtf8(value).writeUtf8("\n")
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