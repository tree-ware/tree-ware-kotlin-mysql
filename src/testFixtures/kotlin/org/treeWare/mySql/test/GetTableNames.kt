package org.treeWare.mySql.test

import javax.sql.DataSource

fun getTableNames(dataSource: DataSource, database: String): List<String> {
    val tableNames = mutableListOf<String>()
    dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery("SHOW TABLES IN $database")
            while (resultSet.next()) tableNames.add(resultSet.getString(1))
        }
    }
    return tableNames
}