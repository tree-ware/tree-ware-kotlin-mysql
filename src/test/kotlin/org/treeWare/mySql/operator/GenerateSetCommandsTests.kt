package org.treeWare.mySql.operator

import org.treeWare.metaModel.newMySqlAddressBookMetaModel
import org.treeWare.model.getMainModelFromJsonFile
import org.treeWare.model.readFile
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateSetCommandsTests {
    @Test
    fun `Set-commands must be generated for the model`() {
        val metaModel = newMySqlAddressBookMetaModel("test", null, null)
        val model = getMainModelFromJsonFile(metaModel, "model/my_sql_address_book_1.json")
        val commands = generateSetCommands(model)
        val expected = readFile("operator/my_sql_address_book_1_set_commands.txt")
        val actual = commands.joinToString("\n")
        assertEquals(expected, actual)
    }
}