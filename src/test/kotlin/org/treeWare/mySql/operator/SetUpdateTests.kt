package org.treeWare.mySql.operator

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.treeWare.metaModel.mySqlAddressBookMetaModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.getMainModelFromJsonFile
import org.treeWare.model.getMainModelFromJsonString
import org.treeWare.model.operator.*
import org.treeWare.model.operator.set.SetResponse
import org.treeWare.model.operator.set.assertSetResponse
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import org.treeWare.model.readFile
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.mySql.test.MySqlTestContainer
import org.treeWare.mySql.test.clearDatabase
import org.treeWare.mySql.test.getDatabaseRows
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import javax.sql.DataSource
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

private const val TEST_DATABASE = "test__address_book"

private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

private const val CREATE_TIME = "2022-03-03T00:30:31.330Z"
private val createClock = Clock.fixed(Instant.parse(CREATE_TIME), ZoneOffset.UTC)

private const val UPDATE_TIME = "2022-04-04T00:40:41.440Z"
private val updateClock = Clock.fixed(Instant.parse(UPDATE_TIME), ZoneOffset.UTC)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SetUpdateTests {
    private val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
    private val setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?

    private val dataSource: DataSource = MySqlTestContainer.getDataSource()

    init {
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        setEntityDelegates = operatorEntityDelegateRegistry.get(SetOperatorId)

        val createDbEntityDelegates = operatorEntityDelegateRegistry.get(GenerateDdlCommandsOperatorId)
        createDatabase(mySqlAddressBookMetaModel, createDbEntityDelegates, dataSource)
    }

    @AfterEach
    fun afterEach() {
        clearDatabase(dataSource, TEST_DATABASE)
    }


    @Test
    fun `Set-update must fail for a new model`() {
        val emptyDatabaseRows = getDatabaseRows(dataSource, TEST_DATABASE)
        val update = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_address_book_1_set_update.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedUpdateResponse = SetResponse.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError("/address_book", "unable to update"),
                ElementModelError("/address_book/settings", "unable to update"),
                ElementModelError("/address_book/settings/advanced", "unable to update"),
                ElementModelError("/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd", "unable to update"),
                ElementModelError(
                    "/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/relation/16634916-8f83-4376-ad42-37038e108a0b",
                    "unable to update"
                ),
                ElementModelError(
                    "/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/password",
                    "unable to update"
                ),
                ElementModelError(
                    "/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/secret",
                    "unable to update"
                ),
                ElementModelError("/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f", "unable to update"),
                ElementModelError(
                    "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation/05ade278-4b44-43da-a0cc-14463854e397",
                    "unable to update"
                ),
                ElementModelError(
                    "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/password",
                    "unable to update"
                ),
                ElementModelError(
                    "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/secret",
                    "unable to update"
                ),
                ElementModelError("/address_book/groups/ad9aaea8-30fe-45ed-93ef-bd368da0c756", "unable to update"),
                ElementModelError("/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a", "unable to update"),
                ElementModelError(
                    "/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups/fe2aa774-e1fe-4680-a439-8bd1d0eb4abc",
                    "unable to update"
                ),
                ElementModelError(
                    "/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups/fe2aa774-e1fe-4680-a439-8bd1d0eb4abc/persons/546a4982-b39a-4d01-aeb3-22d60c6963c0",
                    "unable to update"
                ),
                ElementModelError(
                    "/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups/fe2aa774-e1fe-4680-a439-8bd1d0eb4abc/persons/e391c509-67d6-4846-bfea-0f7cd9c91bf7",
                    "unable to update"
                ),
                ElementModelError(
                    "/address_book/city_info/Albany/New York/United States of America",
                    "unable to update"
                ),
                ElementModelError(
                    "/address_book/city_info/New York City/New York/United States of America",
                    "unable to update"
                ),
                ElementModelError(
                    "/address_book/city_info/San Francisco/California/United States of America",
                    "unable to update"
                ),
                ElementModelError(
                    "/address_book/city_info/Princeton/New Jersey/United States of America",
                    "unable to update"
                ),
            )
        )
        val actualUpdateResponse = set(update, setEntityDelegates, dataSource, clock = updateClock)
        assertSetResponse(expectedUpdateResponse, actualUpdateResponse)
        val actualRows = getDatabaseRows(dataSource, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, actualRows)
    }

    @Test
    fun `Set-update must succeed for an old model`() {
        // Test strategy:
        // 1) create a model using the model used in `SetCreateTests`.
        // 2) update every node of the created model using the model from the above test.
        //    The update model has different values as well as different ordering of entities in sets.
        val expectedRows = readFile("operator/my_sql_address_book_1_set_update_results.txt")

        val create = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_address_book_1_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCreateResponse = SetResponse.Success
        val actualCreateResponse = set(create, setEntityDelegates, dataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val createdRows = getDatabaseRows(dataSource, TEST_DATABASE)
        assertNotEquals(expectedRows, createdRows)

        val update = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_address_book_1_set_update.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedUpdateResponse = SetResponse.Success
        val actualUpdateResponse = set(update, setEntityDelegates, dataSource, clock = updateClock)
        assertSetResponse(expectedUpdateResponse, actualUpdateResponse)
        val updatedRows = getDatabaseRows(dataSource, TEST_DATABASE)
        assertEquals(expectedRows, updatedRows)
    }

    @Test
    fun `Set-update must fail when updating an entity with existing keys but different entity path`() {
        val emptyDatabaseRows = getDatabaseRows(dataSource, TEST_DATABASE)
        val createJson = """
            |{
            |  "address_book": {
            |    "set_": "create",
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "relation": [
            |          {
            |            "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |            "relationship": "colleague"
            |          }
            |        ]
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val create =
            getMainModelFromJsonString(
                mySqlAddressBookMetaModel,
                createJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )
        val expectedCreateResponse = SetResponse.Success
        val actualCreateResponse = set(create, setEntityDelegates, dataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRows = getDatabaseRows(dataSource, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        // Attempt to update the relation entity but under a different person entity.
        val updateJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |        "first_name": "Lois",
            |        "last_name": "Lane",
            |        "relation": [
            |          {
            |            "set_": "update",
            |            "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |            "relationship": "colleague"
            |          }
            |        ]
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val expectedUpdateResponse = SetResponse.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError(
                    "/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/relation/05ade278-4b44-43da-a0cc-14463854e397",
                    "unable to update"
                )
            )
        )
        val update =
            getMainModelFromJsonString(
                mySqlAddressBookMetaModel,
                updateJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )
        val actualUpdateResponse = set(update, setEntityDelegates, dataSource, clock = updateClock)
        assertSetResponse(expectedUpdateResponse, actualUpdateResponse)
        val afterUpdateRows = getDatabaseRows(dataSource, TEST_DATABASE)
        assertEquals(afterCreateRows, afterUpdateRows)
    }
}