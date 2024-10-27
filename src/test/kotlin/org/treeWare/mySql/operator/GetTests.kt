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
import org.treeWare.mySql.test.metaModel.mySqlAddressBookMetaModel
import org.treeWare.mySql.test.metaModel.mySqlAddressBookRootEntityMeta
import org.treeWare.mySql.test.testDataSource
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertTrue

private const val TEST_DATABASE = "test__address_book"

private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

@Tag("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetTests {
    private val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
    private val setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?
    private val getEntityDelegates: EntityDelegateRegistry<GetEntityDelegate>?

    init {
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        setEntityDelegates = operatorEntityDelegateRegistry.get(SetOperatorId)
        getEntityDelegates = operatorEntityDelegateRegistry.get(GetOperatorId)

        val createDbEntityDelegates = operatorEntityDelegateRegistry.get(GenerateDdlCommandsOperatorId)
        createDatabase(mySqlAddressBookMetaModel, createDbEntityDelegates, testDataSource)
    }

    @BeforeEach
    fun beforeEach() {
        val create = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_get_tests_initial_create_request.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val now = "2022-04-14T00:40:41.450Z"
        val clock = Clock.fixed(Instant.parse(now), ZoneOffset.UTC)
        val expectedResponse = Response.Success
        val actualResponse = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedResponse, actualResponse)
    }

    @AfterEach
    fun afterEach() {
        clearDatabase(testDataSource, TEST_DATABASE)
    }


    @Test
    fun `get() must fetch nested wildcard entities in a request`() {
        val request = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_get_request_nested_wildcard_entities.json",
            entity = request
        )
        val responseModel = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        val response = get(request, setEntityDelegates, getEntityDelegates, testDataSource, responseModel)
        assertTrue(response is Response.Success)
        assertMatchesJson(
            responseModel,
            "model/my_sql_get_response_nested_wildcard_entities.json",
            EncodePasswords.ALL
        )
    }

    @Test
    fun `get() must fetch specific and wildcard entities in a request`() {
        val request = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_get_request_specific_and_wildcard_entities.json",
            entity = request
        )
        val responseModel = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        val response = get(request, setEntityDelegates, getEntityDelegates, testDataSource, responseModel)
        assertTrue(response is Response.Success)
        assertMatchesJson(
            responseModel,
            "model/my_sql_get_response_specific_and_wildcard_entities.json",
            EncodePasswords.ALL
        )
    }

    @Test
    fun `get() must fetch entities when non-key fields are not requested in parent entities`() {
        val request = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity("model/my_sql_get_request_no_parent_fields.json", entity = request)
        val responseModel = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        val response = get(request, setEntityDelegates, getEntityDelegates, testDataSource, responseModel)
        assertTrue(response is Response.Success)
        assertMatchesJson(responseModel, "model/my_sql_get_response_no_parent_fields.json", EncodePasswords.ALL)
    }

    @Test
    fun `get() must fetch entities when a subset of keys are specified in a request`() {
        val request = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity("model/my_sql_get_request_subset_of_keys.json", entity = request)
        val responseModel = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        val response = get(request, setEntityDelegates, getEntityDelegates, testDataSource, responseModel)
        assertTrue(response is Response.Success)
        assertMatchesJson(responseModel, "model/my_sql_get_response_subset_of_keys.json", EncodePasswords.ALL)
    }

    @Test
    fun `get() must not fetch entities if entity path in request does not match entity path in DB`() {
        val request = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity( "model/my_sql_get_request_invalid_entity_paths.json", entity = request)
        val responseModel = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        val response = get(request, setEntityDelegates, getEntityDelegates, testDataSource, responseModel)
        assertTrue(response is Response.Success)
        assertMatchesJson(responseModel, "model/my_sql_get_response_invalid_entity_paths.json", EncodePasswords.ALL)
    }
}