package org.treeWare.mySql.test

import java.io.StringWriter
import java.sql.Connection

fun getDatabaseRows(connection: Connection, database: String): String {
    val writer = StringWriter()
    printDatabase(connection, database, writer)
    return writer.toString()
}