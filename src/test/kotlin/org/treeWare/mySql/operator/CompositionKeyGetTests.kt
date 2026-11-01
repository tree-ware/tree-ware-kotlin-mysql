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
class CompositionKeyGetTests {
    private val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
    private val setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?
    private val getEntityDelegates: EntityDelegateRegistry<GetEntityDelegate>?

    init {
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        setEntityDelegates = operatorEntityDelegateRegistry.get(SetOperatorId)
        getEntityDelegates = operatorEntityDelegateRegistry.get(GetOperatorId)

        val createDbEntityDelegates = operatorEntityDelegateRegistry.get(GenerateDdlCommandsOperatorId)
        createDatabase(compositionKeyMetaModel, createDbEntityDelegates, testDataSource, databasePrefix = "test")
    }

    @BeforeEach
    fun beforeEach() {
        val create = MutableEntityModel(compositionKeyRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/comp_key_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val now = "2022-04-14T00:40:41.450Z"
        val clock = Clock.fixed(Instant.parse(now), ZoneOffset.UTC)
        val expectedResponse = Response.Success
        val actualResponse = set(create, setEntityDelegates, testDataSource, true, databasePrefix = "test", clock = clock)
        assertSetResponse(expectedResponse, actualResponse)
    }

    @AfterEach
    fun afterEach() {
        clearDatabase(testDataSource, TEST_DATABASE)
    }

    @Test
    fun `get() must fetch nested wildcard entities with composition keys`() {
        runGetTest(
            "model/comp_key_get_request_nested_wildcard.json",
            "model/comp_key_get_response_nested_wildcard.json"
        )
    }

    @Test
    fun `get() must fetch specific and wildcard entities with composition keys`() {
        runGetTest(
            "model/comp_key_get_request_specific_and_wildcard.json",
            "model/comp_key_get_response_specific_and_wildcard.json"
        )
    }

    @Test
    fun `get() must fetch entities when a subset of key leaves are specified`() {
        runGetTest(
            "model/comp_key_get_request_subset_of_keys.json",
            "model/comp_key_get_response_subset_of_keys.json"
        )
    }

    @Test
    fun `get() must fetch composition keys of single compositions`() {
        runGetTest(
            "model/comp_key_get_request_single_keys.json",
            "model/comp_key_get_response_single_keys.json"
        )
    }

    @Test
    fun `get() must fetch only composition keys when nothing else is requested`() {
        runGetTest(
            "model/comp_key_get_request_keys_only.json",
            "model/comp_key_get_response_keys_only.json"
        )
    }

    private fun runGetTest(requestFile: String, expectedResponseFile: String) {
        val request = MutableEntityModel(compositionKeyRootEntityMeta, null)
        decodeJsonFileIntoEntity(requestFile, entity = request)
        val responseModel = MutableEntityModel(compositionKeyRootEntityMeta, null)
        val response = get(
            request,
            setEntityDelegates,
            getEntityDelegates,
            testDataSource,
            responseModel,
            databasePrefix = "test"
        )
        assertTrue(response is Response.Success)
        assertMatchesJson(responseModel, expectedResponseFile, EncodePasswords.ALL)
    }
}
