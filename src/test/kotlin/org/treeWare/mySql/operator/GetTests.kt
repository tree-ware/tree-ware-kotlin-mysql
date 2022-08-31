package org.treeWare.mySql.operator

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.treeWare.metaModel.mySqlAddressBookMetaModel
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.getMainModelFromJsonFile
import org.treeWare.model.operator.*
import org.treeWare.model.operator.get.GetResponse
import org.treeWare.model.operator.set.SetResponse
import org.treeWare.model.operator.set.assertSetResponse
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.mySql.test.MySqlTestContainer
import org.treeWare.mySql.test.clearDatabase
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import javax.sql.DataSource
import kotlin.test.assertTrue

private const val TEST_DATABASE = "test__address_book"

private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetTests {
    private val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
    private val setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?
    private val getEntityDelegates: EntityDelegateRegistry<GetEntityDelegate>?

    private val dataSource: DataSource = MySqlTestContainer.getDataSource()

    init {
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        setEntityDelegates = operatorEntityDelegateRegistry.get(SetOperatorId)
        getEntityDelegates = operatorEntityDelegateRegistry.get(GetOperatorId)

        val createDbEntityDelegates = operatorEntityDelegateRegistry.get(GenerateDdlCommandsOperatorId)
        createDatabase(mySqlAddressBookMetaModel, createDbEntityDelegates, dataSource)
    }

    @BeforeEach
    fun beforeEach() {
        val create = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_get_tests_initial_create_request.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val now = "2022-04-14T00:40:41.450Z"
        val clock = Clock.fixed(Instant.parse(now), ZoneOffset.UTC)
        val expectedResponse = SetResponse.Success
        val actualResponse = set(create, setEntityDelegates, dataSource, clock = clock)
        assertSetResponse(expectedResponse, actualResponse)
    }

    @AfterEach
    fun afterEach() {
        clearDatabase(dataSource, TEST_DATABASE)
    }


    @Test
    fun `get() must fetch nested wildcard entities in a request`() {
        val request = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_get_request_nested_wildcard_entities.json"
        )
        val response = get(request, setEntityDelegates, getEntityDelegates, dataSource)
        assertTrue(response is GetResponse.Model)
        assertMatchesJson(
            response.model,
            "model/my_sql_get_response_nested_wildcard_entities.json",
            EncodePasswords.ALL
        )
    }

    @Test
    fun `get() must fetch specific and wildcard entities in a request`() {
        val request =
            getMainModelFromJsonFile(
                mySqlAddressBookMetaModel,
                "model/my_sql_get_request_specific_and_wildcard_entities.json"
            )
        val response = get(request, setEntityDelegates, getEntityDelegates, dataSource)
        assertTrue(response is GetResponse.Model)
        assertMatchesJson(
            response.model,
            "model/my_sql_get_response_specific_and_wildcard_entities.json",
            EncodePasswords.ALL
        )
    }

    @Test
    fun `get() must fetch entities when non-key fields are not requested in parent entities`() {
        val request =
            getMainModelFromJsonFile(mySqlAddressBookMetaModel, "model/my_sql_get_request_no_parent_fields.json")
        val response = get(request, setEntityDelegates, getEntityDelegates, dataSource)
        assertTrue(response is GetResponse.Model)
        assertMatchesJson(response.model, "model/my_sql_get_response_no_parent_fields.json", EncodePasswords.ALL)
    }

    @Test
    fun `get() must fetch entities when a subset of keys are specified in a request`() {
        val request =
            getMainModelFromJsonFile(mySqlAddressBookMetaModel, "model/my_sql_get_request_subset_of_keys.json")
        val response = get(request, setEntityDelegates, getEntityDelegates, dataSource)
        assertTrue(response is GetResponse.Model)
        assertMatchesJson(response.model, "model/my_sql_get_response_subset_of_keys.json", EncodePasswords.ALL)
    }

    @Test
    fun `get() must not fetch entities if entity path in request does not match entity path in DB`() {
        val request =
            getMainModelFromJsonFile(mySqlAddressBookMetaModel, "model/my_sql_get_request_invalid_entity_paths.json")
        val response = get(request, setEntityDelegates, getEntityDelegates, dataSource)
        assertTrue(response is GetResponse.Model)
        assertMatchesJson(response.model, "model/my_sql_get_response_invalid_entity_paths.json", EncodePasswords.ALL)
    }
}