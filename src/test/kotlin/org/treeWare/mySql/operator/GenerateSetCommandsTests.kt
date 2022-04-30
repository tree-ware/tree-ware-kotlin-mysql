package org.treeWare.mySql.operator

import org.treeWare.metaModel.newMySqlAddressBookMetaModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.getMainModelFromJsonFile
import org.treeWare.model.operator.*
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import org.treeWare.model.readFile
import org.treeWare.mySql.operator.delegate.MySqlSetDelegate
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals

private val metaModel = newMySqlAddressBookMetaModel("test", null, null).metaModel
    ?: throw IllegalStateException("Meta-model has validation errors")
private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

private const val NOW = "2022-04-14T00:40:41.450Z"
private val clock = Clock.fixed(Instant.parse(NOW), ZoneOffset.UTC)

class GenerateSetCommandsTests {
    private val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
    private val entityDelegates: EntityDelegateRegistry<SetEntityDelegate>?

    init {
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        entityDelegates = operatorEntityDelegateRegistry.get(SetOperatorId)
    }

    @Test
    fun `Set-create commands must be generated for the model`() {
        val addressBook1Create = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_address_book_1_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCommands = readFile("operator/my_sql_address_book_1_set_create_commands.sql")
        val setDelegate = MySqlSetDelegate(metaModel, entityDelegates, null, clock = clock)
        val setErrors = set(addressBook1Create, setDelegate, entityDelegates)
        assertEquals("", setErrors.joinToString("\n"))
        assertEquals(expectedCommands, setDelegate.commands.joinToString("\n") { it.sql })
    }

    @Test
    fun `Set-update commands must be generated for the model`() {
        val addressBook1Create = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_address_book_1_set_update.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCommands = readFile("operator/my_sql_address_book_1_set_update_commands.sql")
        val setDelegate = MySqlSetDelegate(metaModel, entityDelegates, null, clock = clock)
        val setErrors = org.treeWare.model.operator.set(addressBook1Create, setDelegate, entityDelegates)
        assertEquals("", setErrors.joinToString("\n"))
        assertEquals(expectedCommands, setDelegate.commands.joinToString("\n") { it.sql })
    }

    @Test
    fun `Set-delete commands must be generated for the model`() {
        val delete = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_address_book_1_set_delete_bottoms_up.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCommands = readFile("operator/my_sql_address_book_1_set_delete_bottoms_up_commands.sql")
        val setDelegate = MySqlSetDelegate(metaModel, entityDelegates, null, clock = clock)
        val setErrors = set(delete, setDelegate, entityDelegates)
        assertEquals("", setErrors.joinToString("\n"))
        assertEquals(expectedCommands, setDelegate.commands.joinToString("\n") { it.sql })
    }

    @Test
    fun `Set-mixed commands must be generated for the model`() {
        val mixed = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_address_book_1_set_mixed.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCommands = readFile("operator/my_sql_address_book_1_set_mixed_commands.sql")
        val setDelegate = MySqlSetDelegate(metaModel, entityDelegates, null, clock = clock)
        val setErrors = org.treeWare.model.operator.set(mixed, setDelegate, entityDelegates)
        assertEquals("", setErrors.joinToString("\n"))
        assertEquals(expectedCommands, setDelegate.commands.joinToString("\n") { it.sql })
    }
}