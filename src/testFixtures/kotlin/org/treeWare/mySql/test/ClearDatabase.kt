package org.treeWare.mySql.test

import java.sql.Connection

fun clearDatabase(connection: Connection, database: String) {
    val tables = getTableNames(connection, database)
    val statement = connection.createStatement()
    statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0;")
    tables.forEach { statement.executeUpdate("TRUNCATE TABLE $database.$it") }
    statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 1;")
    statement.close()
    connection.commit()
}