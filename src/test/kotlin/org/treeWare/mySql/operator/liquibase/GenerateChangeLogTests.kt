package org.treeWare.mySql.operator.liquibase

import org.treeWare.metaModel.mySqlAddressBookMetaModel
import org.treeWare.model.operator.OperatorEntityDelegateRegistry
import org.treeWare.mySql.operator.CreateForeignKeyConstraints
import org.treeWare.mySql.operator.GenerateDdlCommandsOperatorId
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.util.readFile
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateChangeLogTests {
    @Test
    fun `Changelog must be generated for table creation`() {
        val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        val entityDelegates = operatorEntityDelegateRegistry.get(GenerateDdlCommandsOperatorId)

        val writer = StringWriter()
        generateChangeLog(writer, mySqlAddressBookMetaModel, entityDelegates, true)

        val expected = readFile("operator/liquibase/my_sql_address_book_ddl_changelog.sql")
        val actual = writer.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun `Changelog can be generated for table creation without foreign-keys`() {
        val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        val entityDelegates = operatorEntityDelegateRegistry.get(GenerateDdlCommandsOperatorId)

        val writer = StringWriter()
        generateChangeLog(writer, mySqlAddressBookMetaModel, entityDelegates, true, CreateForeignKeyConstraints.NONE)

        val expected = readFile("operator/liquibase/my_sql_address_book_ddl_changelog_no_foreign_keys.sql")
        val actual = writer.toString()
        assertEquals(expected, actual)
    }
}