package org.treeWare.mySql.operator.liquibase

import okio.Buffer
import org.treeWare.mySql.test.metaModel.mySqlAddressBookMetaModel
import org.treeWare.model.operator.OperatorEntityDelegateRegistry
import org.treeWare.mySql.operator.CreateForeignKeyConstraints
import org.treeWare.mySql.operator.GenerateDdlCommandsOperatorId
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.util.readFile
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateChangeLogTests {
    @Test
    fun `Changelog must be generated for table creation`() {
        val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        val entityDelegates = operatorEntityDelegateRegistry.get(GenerateDdlCommandsOperatorId)

        val buffer = Buffer()
        generateChangeLog(buffer, mySqlAddressBookMetaModel, entityDelegates, true, false, "test")

        val expected = readFile("operator/liquibase/my_sql_address_book_ddl_changelog.sql")
        val actual = buffer.readUtf8()
        assertEquals(expected, actual)
    }

    @Test
    fun `Changelog can be generated for table creation without foreign-keys`() {
        val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        val entityDelegates = operatorEntityDelegateRegistry.get(GenerateDdlCommandsOperatorId)

        val buffer = Buffer()
        generateChangeLog(
            buffer,
            mySqlAddressBookMetaModel,
            entityDelegates,
            true,
            true,
            "test",
            CreateForeignKeyConstraints.NONE
        )

        val expected = readFile("operator/liquibase/my_sql_address_book_ddl_changelog_no_foreign_keys.sql")
        val actual = buffer.readUtf8()
        assertEquals(expected, actual)
    }
}