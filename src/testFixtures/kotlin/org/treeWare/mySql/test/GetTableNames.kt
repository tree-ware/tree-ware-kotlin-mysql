package org.treeWare.mySql.test

import java.sql.Connection

fun getTableNames(connection: Connection, database: String): List<String> {
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery("SHOW TABLES IN $database")
    val tableNames = mutableListOf<String>()
    while (resultSet.next()) tableNames.add(resultSet.getString(1))
    statement.close()
    return tableNames
}