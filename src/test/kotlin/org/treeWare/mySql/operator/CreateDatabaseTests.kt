package org.treeWare.mySql.operator

import com.wix.mysql.EmbeddedMysql
import com.wix.mysql.EmbeddedMysql.anEmbeddedMysql
import com.wix.mysql.config.MysqldConfig.aMysqldConfig
import com.wix.mysql.distribution.Version.v8_0_17
import org.treeWare.metaModel.newMySqlAddressBookMetaModel
import org.treeWare.mySql.test.getAvailableServerPort
import org.treeWare.mySql.test.getTableNames
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.*

class CreateDatabaseTests {
    private val port = getAvailableServerPort()

    private val mysqld: EmbeddedMysql
    private val connection: Connection

    init {
        val config = aMysqldConfig(v8_0_17)
            .withPort(port)
            .withServerVariable("mysqlx", 0) // disable the X plugin
            .build()
        mysqld = anEmbeddedMysql(config).start()
        connection = DriverManager.getConnection("jdbc:mysql://localhost:$port/", "root", "")
    }

    @AfterTest
    fun afterTest() {
        mysqld.stop()
    }

    @Test
    fun `Database and tables must be created for the specified meta-model`() {
        val expectedDatabaseName = "test_address_book"
        val expectedTableNames = listOf(
            "city__city_info",
            "main__address_book_person",
            "main__address_book_relation",
            "main__address_book_root",
            "main__address_book_settings"
        )

        val before = getDatabaseNames(connection)
        assertFalse(before.contains(expectedDatabaseName))

        val metaModel = newMySqlAddressBookMetaModel("test", null, null).metaModel
            ?: throw IllegalStateException("Meta-model has validation errors")
        createDatabase(metaModel, connection)
        val after = getDatabaseNames(connection)
        assertTrue(after.contains(expectedDatabaseName))

        val tableNames = getTableNames(connection, expectedDatabaseName)
        assertEquals(expectedTableNames.joinToString("\n"), tableNames.joinToString("\n"))

        // TODO(deepak-nulu): verify details of each table.
    }
}

private fun getDatabaseNames(connection: Connection): List<String> {
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery("SHOW DATABASES")
    val databaseNames = mutableListOf<String>()
    while (resultSet.next()) databaseNames.add(resultSet.getString(1))
    statement.close()
    return databaseNames
}