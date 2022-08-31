package org.treeWare.mySql.operator

import org.treeWare.metaModel.mySqlAddressBookMetaModel
import org.treeWare.model.operator.OperatorEntityDelegateRegistry
import org.treeWare.model.readFile
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateDdlCommandsTests {
    @Test
    fun `DDL commands must be generated for the database and tables`() {
        val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        val entityDelegates = operatorEntityDelegateRegistry.get(GenerateDdlCommandsOperatorId)

        val commands = generateDdlCommands(mySqlAddressBookMetaModel, entityDelegates)

        val expected = readFile("operator/my_sql_address_book_ddl_commands.sql")
        val actual = commands.joinToString("\n")
        assertEquals(expected, actual)
    }

    @Test
    fun `DDL commands can be generated without foreign-keys`() {
        val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        val entityDelegates = operatorEntityDelegateRegistry.get(GenerateDdlCommandsOperatorId)

        val commands = generateDdlCommands(mySqlAddressBookMetaModel, entityDelegates, CreateForeignKeyConstraints.NONE)

        val expected = readFile("operator/my_sql_address_book_ddl_commands_no_foreign_keys.sql")
        val actual = commands.joinToString("\n")
        assertEquals(expected, actual)
    }
}