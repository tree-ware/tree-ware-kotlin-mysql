package org.treeWare.mySql.test

import com.zaxxer.hikari.HikariDataSource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName
import javax.sql.DataSource

object MySqlTestContainer {
    private val dbServer = MySQLContainer<Nothing>(DockerImageName.parse("mysql:8.0.29"))

    init {
        dbServer.start()
    }

    fun getDataSource(
        autoCommit: Boolean = false,
        useServerPreparedStatements: Boolean = false,
        allowMultiQueries: Boolean = false
    ): DataSource =
        HikariDataSource().apply {
            this.jdbcUrl = dbServer.jdbcUrl
            this.username = "root"
            this.password = dbServer.password
            this.isAutoCommit = autoCommit
            this.addDataSourceProperty("useServerPrepStmts", useServerPreparedStatements)
            this.addDataSourceProperty("allowMultiQueries", allowMultiQueries)
        }
}