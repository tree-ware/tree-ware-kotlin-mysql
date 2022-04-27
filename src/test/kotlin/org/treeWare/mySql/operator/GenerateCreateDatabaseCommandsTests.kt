package org.treeWare.mySql.operator

import org.treeWare.metaModel.newMySqlAddressBookMetaModel
import org.treeWare.model.operator.OperatorEntityDelegateRegistry
import org.treeWare.model.readFile
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateCreateDatabaseCommandsTests {
    @Test
    fun `Create-commands must be generated for the database and tables`() {
        val metaModel = newMySqlAddressBookMetaModel("test", null, null).metaModel
            ?: throw IllegalStateException("Meta-model has validation errors")
        val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        val entityDelegates = operatorEntityDelegateRegistry.get(GenerateCreateDatabaseCommandsOperatorId)

        val commands = generateCreateDatabaseCommands(metaModel, entityDelegates)

        val expected = readFile("operator/my_sql_address_book_create_database_commands.sql")
        val actual = commands.joinToString("\n")
        assertEquals(expected, actual)
    }
}