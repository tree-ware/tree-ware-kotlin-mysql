package org.treeWare.mySql.test

import okio.Buffer
import javax.sql.DataSource

fun getDatabaseRows(dataSource: DataSource, database: String): String {
    val buffer = Buffer()
    printDatabase(dataSource, database, buffer)
    return buffer.readUtf8()
}

fun getTableRows(dataSource: DataSource, database: String, vararg tables: String): String {
    val buffer = Buffer()
    tables.forEachIndexed { index, table ->
        if (index > 0) buffer.writeUtf8("\n")
        printTable(dataSource, database, table, buffer)
    }
    return buffer.readUtf8()
}