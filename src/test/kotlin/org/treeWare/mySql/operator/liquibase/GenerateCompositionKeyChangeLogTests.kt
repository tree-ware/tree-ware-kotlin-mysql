package org.treeWare.mySql.operator.liquibase

import okio.Buffer
import org.treeWare.model.operator.OperatorEntityDelegateRegistry
import org.treeWare.mySql.operator.GenerateDdlCommandsOperatorId
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.mySql.test.metaModel.compositionKeyMetaModel
import org.treeWare.util.readFile
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateCompositionKeyChangeLogTests {
    @Test
    fun `Changelog must be generated for composition-key tables`() {
        val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        val entityDelegates = operatorEntityDelegateRegistry.get(GenerateDdlCommandsOperatorId)

        val buffer = Buffer()
        generateChangeLog(buffer, compositionKeyMetaModel, entityDelegates, true, false, "test")

        val expected = readFile("operator/liquibase/comp_key_ddl_changelog.sql")
        val actual = buffer.readUtf8()
        assertEquals(expected, actual)
    }
}
