package org.treeWare.mySql.operator

import org.treeWare.metaModel.newMySqlAddressBookMetaModel
import org.treeWare.model.operator.OperatorDelegateRegistry
import org.treeWare.model.readFile
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorDelegates
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateCreateCommandsTests {
    @Test
    fun `Create-commands must be generated for the database and tables`() {
        val metaModel = newMySqlAddressBookMetaModel("test", null, null).metaModel
            ?: throw IllegalStateException("Meta-model has validation errors")
        val operatorDelegateRegistry = OperatorDelegateRegistry()
        registerMySqlOperatorDelegates(operatorDelegateRegistry)
        val delegates = operatorDelegateRegistry.get(GenerateCreateCommandsOperatorId)

        val commands = generateCreateCommands(metaModel, delegates)

        val expected = readFile("operator/my_sql_address_book_create_commands.txt")
        val actual = commands.joinToString("\n")
        assertEquals(expected, actual)
    }
}