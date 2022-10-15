package org.treeWare.mySql.validation

import org.junit.jupiter.api.Tag
import org.treeWare.mySql.test.MySqlTestContainer
import org.treeWare.mySql.test.clearDatabase
import org.treeWare.mySql.test.getDatabaseRows
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals

private fun getInjectionName(databaseName: String): String = "user3'); DROP TABLE $databaseName.users; -- "

/**
 * Determine if client-side prepared-statements prevent SQL-injection.
 * There is a good explanation of the difference between client-side and server-side prepared-statements, but there
 * is no documentation of any sorts that explains whether client-side prepared-statements prevent SQL-injection by
 * escaping the values bound to the statement. These tests were created to answer that question.
 */
@Tag("integrationTest")
class PreparedStatementTests {
    @Test
    fun `SQL injection must be possible if the query is built with string concatenation`() {
        val dataSource: DataSource = MySqlTestContainer.getDataSource(true, false, true)
        val databaseName = "test1"
        createUsersTable(dataSource, databaseName)

        val injectionName = getInjectionName(databaseName)
        val command = "INSERT INTO $databaseName.users (name) VALUES ('$injectionName');"
        dataSource.connection.use { connection ->
            connection.createStatement().use { it.execute(command) }
        }

        // No rows are expected because of the DROP TABLE in the injected name.
        val expectedDatabaseRows = """
            + Database $databaseName +

        """.trimIndent()
        val actualDatabaseRows = getDatabaseRows(dataSource, databaseName)
        assertEquals(expectedDatabaseRows, actualDatabaseRows)

        clearDatabase(dataSource, databaseName)
    }

    @Test
    fun `SQL injection must not be possible if the query is built with a client-side prepared-statement`() {
        val dataSource: DataSource = MySqlTestContainer.getDataSource(true, false, false)
        val databaseName = "test2"
        createUsersTable(dataSource, databaseName)

        val statement = dataSource.connection.prepareStatement("INSERT INTO $databaseName.users (name) VALUES (?)")
        val injectionName = getInjectionName(databaseName)
        statement.setString(1, injectionName)
        statement.execute()

        val expectedDatabaseRows = """
            + Database $databaseName +

            = Table users =

            * Row 1 *
            name: Ann

            * Row 2 *
            name: Tom

            * Row 3 *
            name: $injectionName

        """.trimIndent()
        val actualDatabaseRows = getDatabaseRows(dataSource, databaseName)
        assertEquals(expectedDatabaseRows, actualDatabaseRows)

        clearDatabase(dataSource, databaseName)
    }

    @Test
    fun `SQL injection must not be possible if the query is built with a server-side prepared-statement`() {
        val dataSource: DataSource = MySqlTestContainer.getDataSource(true, true, false)
        val databaseName = "test3"
        createUsersTable(dataSource, databaseName)

        val statement = dataSource.connection.prepareStatement("INSERT INTO $databaseName.users (name) VALUES (?)")
        val injectionName = getInjectionName(databaseName)
        statement.setString(1, injectionName)
        statement.execute()

        val expectedDatabaseRows = """
            + Database $databaseName +

            = Table users =

            * Row 1 *
            name: Ann

            * Row 2 *
            name: Tom

            * Row 3 *
            name: $injectionName

        """.trimIndent()
        val actualDatabaseRows = getDatabaseRows(dataSource, databaseName)
        assertEquals(expectedDatabaseRows, actualDatabaseRows)

        clearDatabase(dataSource, databaseName)
    }
}

private fun createUsersTable(dataSource: DataSource, databaseName: String) {
    val commands = listOf(
        "CREATE DATABASE $databaseName;",
        "CREATE TABLE $databaseName.users (name VARCHAR(255))",
        "INSERT INTO $databaseName.users (name) VALUES ('Ann'); -- comment",
        "INSERT INTO $databaseName.users (name) VALUES ('Tom');",
    )
    dataSource.connection.use { connection ->
        commands.forEach { command ->
            connection.createStatement().use { it.execute(command) }
        }
    }
}