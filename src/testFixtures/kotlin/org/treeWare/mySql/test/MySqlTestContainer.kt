package org.treeWare.mySql.test

import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName
import java.sql.Connection
import java.sql.DriverManager


object MySqlTestContainer {
    val dbServer = MySQLContainer<Nothing>(DockerImageName.parse("mysql:8.0.29"))

    init {
        dbServer.start()
    }

    fun getConnection(autoCommit: Boolean = false): Connection {
        val connection = DriverManager.getConnection(
            dbServer.jdbcUrl, "root", dbServer.password
        )

        if(!autoCommit) connection.autoCommit = false

        return connection
    }

}