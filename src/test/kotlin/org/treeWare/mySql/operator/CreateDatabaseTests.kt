package org.treeWare.mySql.operator

import com.wix.mysql.EmbeddedMysql
import com.wix.mysql.EmbeddedMysql.anEmbeddedMysql
import com.wix.mysql.config.MysqldConfig.aMysqldConfig
import com.wix.mysql.distribution.Version.v8_0_17
import org.treeWare.metaModel.newMySqlAddressBookMetaModel
import org.treeWare.model.operator.OperatorEntityDelegateRegistry
import org.treeWare.model.readFile
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.mySql.test.getAvailableServerPort
import org.treeWare.mySql.test.getColumnsSchema
import org.treeWare.mySql.test.getIndexesSchema
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
        val expectedDatabaseName = "test\$address_book"

        val columnsSchemaBefore = getColumnsSchema(connection, expectedDatabaseName)
        assertEquals("", columnsSchemaBefore)
        val indexesSchemaBefore = getIndexesSchema(connection, expectedDatabaseName)
        assertEquals("", indexesSchemaBefore)

        val metaModel = newMySqlAddressBookMetaModel("test", null, null).metaModel
            ?: throw IllegalStateException("Meta-model has validation errors")
        val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        val delegates = operatorEntityDelegateRegistry.get(GenerateCreateDatabaseCommandsOperatorId)

        createDatabase(metaModel, delegates, connection)

        val expectedColumnsSchemaAfter = readFile("operator/my_sql_address_book_db_columns_schema.txt")
        val actualColumnsSchemaAfter = getColumnsSchema(connection, expectedDatabaseName)
        assertEquals(expectedColumnsSchemaAfter, actualColumnsSchemaAfter)

        val expectedIndexesSchemaAfter = readFile("operator/my_sql_address_book_db_indexes_schema.txt")
        val actualIndexesSchemaAfter = getIndexesSchema(connection, expectedDatabaseName)
        assertEquals(expectedIndexesSchemaAfter, actualIndexesSchemaAfter)
    }
}