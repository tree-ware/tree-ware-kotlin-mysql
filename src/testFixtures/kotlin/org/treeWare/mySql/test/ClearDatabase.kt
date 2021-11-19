package org.treeWare.mySql.test

import java.sql.Connection

fun clearDatabase(connection: Connection, database: String) {
    val tables = getTableNames(connection, database)
    tables.forEach { clearTable(connection, database, it) }
}

fun clearTable(connection: Connection, database: String, table: String) {
    val statement = connection.createStatement()
    statement.executeUpdate("TRUNCATE TABLE $database.$table")
    statement.close()
}