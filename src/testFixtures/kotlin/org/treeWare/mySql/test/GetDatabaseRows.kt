package org.treeWare.mySql.test

import java.io.StringWriter
import java.sql.Connection

fun getDatabaseRows(connection: Connection, database: String): String {
    val writer = StringWriter()
    printDatabase(connection, database, writer)
    return writer.toString()
}

fun getTableRows(connection: Connection, database: String, vararg tables: String): String {
    val writer = StringWriter()
    tables.forEachIndexed { index, table ->
        if (index > 0) writer.appendLine()
        printTable(connection, database, table, writer)
    }
    return writer.toString()
}