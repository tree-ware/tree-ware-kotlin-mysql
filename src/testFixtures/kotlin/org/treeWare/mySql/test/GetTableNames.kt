package org.treeWare.mySql.test

import javax.sql.DataSource

fun getTableNames(dataSource: DataSource, database: String): List<String> {
    val connection = dataSource.connection
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery("SHOW TABLES IN $database")
    val tableNames = mutableListOf<String>()
    while (resultSet.next()) tableNames.add(resultSet.getString(1))
    statement.close()
    connection.close()
    return tableNames
}