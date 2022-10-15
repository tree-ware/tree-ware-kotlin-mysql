package org.treeWare.mySql.operator

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.treeWare.metaModel.mySqlAddressBookMetaModel
import org.treeWare.model.operator.OperatorEntityDelegateRegistry
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.mySql.test.clearDatabase
import org.treeWare.mySql.test.getColumnsSchema
import org.treeWare.mySql.test.getIndexesSchema
import org.treeWare.mySql.testDataSource
import org.treeWare.util.readFile
import kotlin.test.assertEquals

private const val TEST_DATABASE = "test__address_book"

@Tag("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateDatabaseTests {
    @AfterEach
    fun afterEach() {
        clearDatabase(testDataSource, TEST_DATABASE)
    }

    @Test
    fun `Database and tables must be created for the specified meta-model`() {
        val expectedDatabaseName = "test__address_book"

        val columnsSchemaBefore = getColumnsSchema(testDataSource, expectedDatabaseName)
        assertEquals("= Table tables =\n", columnsSchemaBefore)
        val indexesSchemaBefore = getIndexesSchema(testDataSource, expectedDatabaseName)
        assertEquals("= Table tables =\n", indexesSchemaBefore)

        val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        val delegates = operatorEntityDelegateRegistry.get(GenerateDdlCommandsOperatorId)

        createDatabase(mySqlAddressBookMetaModel, delegates, testDataSource)

        val expectedColumnsSchemaAfter = readFile("operator/my_sql_address_book_db_columns_schema.txt")
        val actualColumnsSchemaAfter = getColumnsSchema(testDataSource, expectedDatabaseName)
        assertEquals(expectedColumnsSchemaAfter, actualColumnsSchemaAfter)

        val expectedIndexesSchemaAfter = readFile("operator/my_sql_address_book_db_indexes_schema.txt")
        val actualIndexesSchemaAfter = getIndexesSchema(testDataSource, expectedDatabaseName)
        assertEquals(expectedIndexesSchemaAfter, actualIndexesSchemaAfter)
    }
}