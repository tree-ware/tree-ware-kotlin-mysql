package org.treeWare.mySql.operator

import org.junit.jupiter.api.Tag
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.decodeJsonFileIntoEntity
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.operator.*
import org.treeWare.model.operator.set.assertSetResponse
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import org.treeWare.mySql.getBoundSql
import org.treeWare.mySql.operator.delegate.MySqlSetDelegate
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.mySql.test.metaModel.compositionKeyRootEntityMeta
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
class GenerateCompositionKeySetCommandsTests {
    private val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
    private val entityDelegates: EntityDelegateRegistry<SetEntityDelegate>?

    init {
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        entityDelegates = operatorEntityDelegateRegistry.get(SetOperatorId)
    }

    @Test
    fun `Set-create commands must be generated for composition keys`() {
        val create = MutableEntityModel(compositionKeyRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/comp_key_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedCommands = readFile("operator/comp_key_set_create_commands.sql")
        val expectedResponse = Response.Success

        testDataSource.connection.use { connection ->
            val setDelegate = MySqlSetDelegate(
                compositionKeyRootEntityMeta,
                entityDelegates,
                connection,
                databasePrefix = "test",
                clock = clock,
                issueCommands = false
            )
            val actualResponse = set(create, setDelegate, entityDelegates)
            assertSetResponse(expectedResponse, actualResponse)
            assertEquals(expectedCommands, setDelegate.commands.joinToString("\n") { it.statement.getBoundSql() })
        }
    }

    @Test
    fun `Set-update commands must be generated for composition keys`() {
        val update = MutableEntityModel(compositionKeyRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/comp_key_set_update.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = update
        )
        val expectedCommands = readFile("operator/comp_key_set_update_commands.sql")
        val expectedResponse = Response.Success

        testDataSource.connection.use { connection ->
            val setDelegate = MySqlSetDelegate(
                compositionKeyRootEntityMeta,
                entityDelegates,
                connection,
                databasePrefix = "test",
                clock = clock,
                issueCommands = false
            )
            val actualResponse = set(update, setDelegate, entityDelegates)
            assertSetResponse(expectedResponse, actualResponse)
            assertEquals(expectedCommands, setDelegate.commands.joinToString("\n") { it.statement.getBoundSql() })
        }
    }

    @Test
    fun `Set-delete commands must be generated for composition keys`() {
        val delete = MutableEntityModel(compositionKeyRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/comp_key_set_delete_bottoms_up.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = delete
        )
        val expectedCommands = readFile("operator/comp_key_set_delete_bottoms_up_commands.sql")
        val expectedResponse = Response.Success

        testDataSource.connection.use { connection ->
            val setDelegate = MySqlSetDelegate(
                compositionKeyRootEntityMeta,
                entityDelegates,
                connection,
                databasePrefix = "test",
                clock = clock,
                issueCommands = false
            )
            val actualResponse = set(delete, setDelegate, entityDelegates)
            assertSetResponse(expectedResponse, actualResponse)
            assertEquals(expectedCommands, setDelegate.commands.joinToString("\n") { it.statement.getBoundSql() })
        }
    }
}
