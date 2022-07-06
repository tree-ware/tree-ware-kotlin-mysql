package org.treeWare.mySql.test

import java.io.StringWriter
import javax.sql.DataSource

fun getDatabaseRows(dataSource: DataSource, database: String): String {
    val writer = StringWriter()
    printDatabase(dataSource, database, writer)
    return writer.toString()
}

fun getTableRows(dataSource: DataSource, database: String, vararg tables: String): String {
    val writer = StringWriter()
    tables.forEachIndexed { index, table ->
        if (index > 0) writer.appendLine()
        printTable(dataSource, database, table, writer)
    }
    return writer.toString()
}