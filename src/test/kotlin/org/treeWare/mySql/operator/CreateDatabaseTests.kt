package org.treeWare.mySql.operator

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName
import org.treeWare.metaModel.mySqlAddressBookMetaModel
import org.treeWare.model.operator.OperatorEntityDelegateRegistry
import org.treeWare.model.readFile
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.mySql.test.getColumnsSchema
import org.treeWare.mySql.test.getIndexesSchema
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateDatabaseTests {
    private val dbServer: MySQLContainer<Nothing>
    private val connection: Connection

    init {
        dbServer = MySQLContainer<Nothing>(DockerImageName.parse("mysql:8.0.29"))
        dbServer.start()
        connection = DriverManager.getConnection(dbServer.jdbcUrl, "root", dbServer.password)
    }

    @AfterAll
    fun afterAll() {
        dbServer.stop()
    }
    @Test
    fun `Database and tables must be created for the specified meta-model`() {
        val expectedDatabaseName = "test\$address_book"

        val columnsSchemaBefore = getColumnsSchema(connection, expectedDatabaseName)
        assertEquals("= Table tables =\n", columnsSchemaBefore)
        val indexesSchemaBefore = getIndexesSchema(connection, expectedDatabaseName)
        assertEquals("= Table tables =\n", indexesSchemaBefore)

        val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        val delegates = operatorEntityDelegateRegistry.get(GenerateCreateDatabaseCommandsOperatorId)

        createDatabase(mySqlAddressBookMetaModel, delegates, connection)

        val expectedColumnsSchemaAfter = readFile("operator/my_sql_address_book_db_columns_schema.txt")
        val actualColumnsSchemaAfter = getColumnsSchema(connection, expectedDatabaseName)
        assertEquals(expectedColumnsSchemaAfter, actualColumnsSchemaAfter)

        val expectedIndexesSchemaAfter = readFile("operator/my_sql_address_book_db_indexes_schema.txt")
        val actualIndexesSchemaAfter = getIndexesSchema(connection, expectedDatabaseName)
        assertEquals(expectedIndexesSchemaAfter, actualIndexesSchemaAfter)
    }
}