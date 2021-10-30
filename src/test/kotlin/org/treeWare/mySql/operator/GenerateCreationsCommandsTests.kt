package org.treeWare.mySql.operator

import org.treeWare.metaModel.newMySqlAddressBookMetaModel
import org.treeWare.model.readFile
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateCreationsCommandsTests {
    @Test
    fun `Creation commands must be generated for the database and tables`() {
        val metaModel = newMySqlAddressBookMetaModel(null, null)
        val commands = generateCreationsCommands("test", metaModel)
        val expected = readFile("operator/address_book_creation_commands.txt")
        val actual = commands.joinToString("\n")
        assertEquals(expected, actual)
    }
}