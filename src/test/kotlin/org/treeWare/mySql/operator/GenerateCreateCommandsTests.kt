package org.treeWare.mySql.operator

import org.treeWare.metaModel.newMySqlAddressBookMetaModel
import org.treeWare.model.readFile
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateCreateCommandsTests {
    @Test
    fun `Create-commands must be generated for the database and tables`() {
        val metaModel = newMySqlAddressBookMetaModel("test", null, null).metaModel
            ?: throw IllegalStateException("Meta-model has validation errors")
        val commands = generateCreateCommands(metaModel)
        val expected = readFile("operator/my_sql_address_book_create_commands.txt")
        val actual = commands.joinToString("\n")
        assertEquals(expected, actual)
    }
}