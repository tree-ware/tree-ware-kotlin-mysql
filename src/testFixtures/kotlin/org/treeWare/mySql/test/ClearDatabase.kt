package org.treeWare.mySql.test

import javax.sql.DataSource

fun clearDatabase(dataSource: DataSource, database: String) {
    val tables = getTableNames(dataSource, database)
    dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0;")
            tables.forEach { statement.executeUpdate("TRUNCATE TABLE $database.$it") }
            statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 1;")
        }
        if (!connection.autoCommit) connection.commit()
    }
}