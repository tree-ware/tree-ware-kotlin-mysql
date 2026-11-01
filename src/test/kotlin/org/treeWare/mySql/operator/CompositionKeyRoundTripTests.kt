package org.treeWare.mySql.operator

import org.junit.jupiter.api.*
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.decodeJsonFileIntoEntity
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.operator.*
import org.treeWare.model.operator.set.assertSetResponse
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.mySql.test.clearDatabase
import org.treeWare.mySql.test.metaModel.compositionKeyMetaModel
import org.treeWare.mySql.test.metaModel.compositionKeyRootEntityMeta
import org.treeWare.mySql.test.testDataSource
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertTrue

private const val TEST_DATABASE = "test__comp_key_test"

private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

@Tag("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompositionKeyRoundTripTests {
    private val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
    private val setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?
    private val getEntityDelegates: EntityDelegateRegistry<GetEntityDelegate>?

    init {
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        setEntityDelegates = operatorEntityDelegateRegistry.get(SetOperatorId)
        getEntityDelegates = operatorEntityDelegateRegistry.get(GetOperatorId)
    }

    @AfterEach
    fun afterEach() {
        clearDatabase(testDataSource, TEST_DATABASE)
    }

    @Test
    fun `set-get round trip must preserve composition keys and associations`() {
        val createDbEntityDelegates = operatorEntityDelegateRegistry.get(GenerateDdlCommandsOperatorId)
        createDatabase(compositionKeyMetaModel, createDbEntityDelegates, testDataSource, databasePrefix = "test")

        val create = MutableEntityModel(compositionKeyRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/comp_key_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val now = "2022-04-14T00:40:41.450Z"
        val clock = Clock.fixed(Instant.parse(now), ZoneOffset.UTC)
        val setResponse = set(
            create,
            setEntityDelegates,
            testDataSource,
            true,
            databasePrefix = "test",
            clock = clock
        )
        assertSetResponse(Response.Success, setResponse)

        val request = MutableEntityModel(compositionKeyRootEntityMeta, null)
        decodeJsonFileIntoEntity("model/comp_key_round_trip_get_request.json", entity = request)
        val responseModel = MutableEntityModel(compositionKeyRootEntityMeta, null)
        val getResponse = get(
            request,
            setEntityDelegates,
            getEntityDelegates,
            testDataSource,
            responseModel,
            databasePrefix = "test"
        )
        assertTrue(getResponse is Response.Success)
        assertMatchesJson(responseModel, "model/comp_key_round_trip_get_response.json", EncodePasswords.ALL)
    }
}
