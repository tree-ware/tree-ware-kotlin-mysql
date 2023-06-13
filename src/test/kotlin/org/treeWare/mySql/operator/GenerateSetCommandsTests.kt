package org.treeWare.mySql.operator

import org.junit.jupiter.api.Tag
import org.treeWare.mySql.test.metaModel.mySqlAddressBookMetaModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.getMainModelFromJsonFile
import org.treeWare.model.operator.*
import org.treeWare.model.operator.set.SetResponse
import org.treeWare.model.operator.set.assertSetResponse
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import org.treeWare.mySql.getBoundSql
import org.treeWare.mySql.operator.delegate.MySqlSetDelegate
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.mySql.test.testDataSource
import org.treeWare.util.readFile
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals

private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

private const val NOW = "2022-04-14T00:40:41.450Z"
private val clock = Clock.fixed(Instant.parse(NOW), ZoneOffset.UTC)

@Tag("integrationTest")
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
            mySqlAddressBookMetaModel,
            "model/my_sql_address_book_1_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCommands = readFile("operator/my_sql_address_book_1_set_create_commands.sql")
        val expectedResponse = SetResponse.Success

        testDataSource.connection.use { connection ->
            val setDelegate = MySqlSetDelegate(
                mySqlAddressBookMetaModel,
                entityDelegates,
                connection,
                clock = clock,
                issueCommands = false
            )
            val actualResponse = set(addressBook1Create, setDelegate, entityDelegates)
            assertSetResponse(expectedResponse, actualResponse)
            assertEquals(expectedCommands, setDelegate.commands.joinToString("\n") { it.statement.getBoundSql() })
        }
    }

    @Test
    fun `Set-update commands must be generated for the model`() {
        val addressBook1Create = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_address_book_1_set_update.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCommands = readFile("operator/my_sql_address_book_1_set_update_commands.sql")
        val expectedResponse = SetResponse.Success

        testDataSource.connection.use { connection ->
            val setDelegate = MySqlSetDelegate(
                mySqlAddressBookMetaModel,
                entityDelegates,
                connection,
                clock = clock,
                issueCommands = false
            )
            val actualResponse = set(addressBook1Create, setDelegate, entityDelegates)
            assertSetResponse(expectedResponse, actualResponse)
            assertEquals(expectedCommands, setDelegate.commands.joinToString("\n") { it.statement.getBoundSql() })
        }
    }

    @Test
    fun `Set-delete commands must be generated for the model`() {
        val delete = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_address_book_1_set_delete_bottoms_up.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCommands = readFile("operator/my_sql_address_book_1_set_delete_bottoms_up_commands.sql")
        val expectedResponse = SetResponse.Success

        testDataSource.connection.use { connection ->
            val setDelegate = MySqlSetDelegate(
                mySqlAddressBookMetaModel,
                entityDelegates,
                connection,
                clock = clock,
                issueCommands = false
            )
            val actualResponse = set(delete, setDelegate, entityDelegates)
            assertSetResponse(expectedResponse, actualResponse)
            assertEquals(expectedCommands, setDelegate.commands.joinToString("\n") { it.statement.getBoundSql() })
        }
    }

    @Test
    fun `Set-mixed commands must be generated for the model`() {
        val mixed = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_address_book_1_set_mixed.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCommands = readFile("operator/my_sql_address_book_1_set_mixed_commands.sql")
        val expectedResponse = SetResponse.Success

        testDataSource.connection.use { connection ->
            val setDelegate = MySqlSetDelegate(
                mySqlAddressBookMetaModel,
                entityDelegates,
                connection,
                clock = clock,
                issueCommands = false
            )
            val actualResponse = set(mixed, setDelegate, entityDelegates)
            assertSetResponse(expectedResponse, actualResponse)
            assertEquals(expectedCommands, setDelegate.commands.joinToString("\n") { it.statement.getBoundSql() })
        }
    }
}