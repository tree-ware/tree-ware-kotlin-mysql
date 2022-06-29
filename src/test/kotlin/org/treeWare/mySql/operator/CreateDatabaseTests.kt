package org.treeWare.mySql.operator

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.treeWare.metaModel.mySqlAddressBookMetaModel
import org.treeWare.model.operator.OperatorEntityDelegateRegistry
import org.treeWare.model.readFile
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.mySql.test.MySqlTestContainer
import org.treeWare.mySql.test.clearDatabase
import org.treeWare.mySql.test.getColumnsSchema
import org.treeWare.mySql.test.getIndexesSchema
import java.sql.Connection
import kotlin.test.assertEquals

private const val TEST_DATABASE = "test\$address_book"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateDatabaseTests {
    private val connection: Connection

    init {
        connection = MySqlTestContainer.getConnection()
    }

    @AfterAll
    fun afterAll() {
        clearDatabase(connection, TEST_DATABASE)
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