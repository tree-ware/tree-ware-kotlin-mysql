package org.treeWare.mySql.operator

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.decodeJsonFileIntoEntity
import org.treeWare.model.decodeJsonStringIntoEntity
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.operator.*
import org.treeWare.model.operator.set.assertSetResponse
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.mySql.test.clearDatabase
import org.treeWare.mySql.test.getDatabaseRows
import org.treeWare.mySql.test.getTableRows
import org.treeWare.mySql.test.metaModel.mySqlAddressBookMetaModel
import org.treeWare.mySql.test.testDataSource
import org.treeWare.util.readFile
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

private const val TEST_DATABASE = "test__address_book"

private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

private const val NOW = "2022-04-14T00:40:41.450Z"
private val clock = Clock.fixed(Instant.parse(NOW), ZoneOffset.UTC)

@Tag("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SetCreateTests {
    private val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
    private val setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?

    init {
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        setEntityDelegates = operatorEntityDelegateRegistry.get(SetOperatorId)

        val createDbEntityDelegates = operatorEntityDelegateRegistry.get(GenerateDdlCommandsOperatorId)
        createDatabase(mySqlAddressBookMetaModel, createDbEntityDelegates, testDataSource)
    }

    @AfterEach
    fun afterEach() {
        clearDatabase(testDataSource, TEST_DATABASE)
    }

    @Test
    fun `Set-create must succeed for a new model`() {
        val create = MutableEntityModel(mySqlAddressBookMetaModel, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_address_book_1_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedResponse = Response.Success
        val actualResponse = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedResponse, actualResponse)
        val expectedRows = readFile("operator/my_sql_address_book_1_set_create_results.txt")
        val actualRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(expectedRows, actualRows)
    }

    @Test
    fun `Set-create must succeed for a forward-referencing association`() {
        val create = MutableEntityModel(mySqlAddressBookMetaModel, null)
        decodeJsonFileIntoEntity(
            "operator/forward_referencing_association_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedResponse = Response.Success
        val actualResponse = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedResponse, actualResponse)
        val expectedRows = readFile("operator/forward_referencing_association_set_create_results.txt")
        val actualRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(expectedRows, actualRows)
    }

    @Test
    fun `Set-create must succeed for an entity with multiple associations`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "city_info": [
            |      {
            |        "name": "Princeton",
            |        "state": "New Jersey",
            |        "country": "United States of America",
            |        "self": {
            |          "city_info": [
            |            {
            |              "name": "Princeton",
            |              "state": "New Jersey",
            |              "country": "United States of America"
            |            }
            |          ]
            |        },
            |        "self2": {
            |          "city_info": [
            |            {
            |              "country": "United States of America",
            |              "state": "New Jersey",
            |              "name": "Princeton"
            |            }
            |          ]
            |        }
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val create = MutableEntityModel(mySqlAddressBookMetaModel, null)
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedResponse = Response.Success
        val actualResponse = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedResponse, actualResponse)
        val expectedRows = """
            |= Table city__city_info =
            |
            |* Row 1 *
            |created_on_: 2022-04-14 00:40:41.450
            |updated_on_: 2022-04-14 00:40:41.450
            |field_path_: /address_book/city_info
            |name: Princeton
            |state: New Jersey
            |country: United States of America
            |info: null
            |latitude: null
            |longitude: null
            |city_center: null
            |self: {"city_info":[{"name":"Princeton","state":"New Jersey","country":"United States of America"}]}
            |self__name: Princeton
            |self__state: New Jersey
            |self__country: United States of America
            |self2: {"city_info":[{"country":"United States of America","state":"New Jersey","name":"Princeton"}]}
            |self2__name: Princeton
            |self2__state: New Jersey
            |self2__country: United States of America
            |main__address_book_root__singleton_key_: 0
            |
        """.trimMargin()
        val actualRows = getTableRows(testDataSource, TEST_DATABASE, "city__city_info")
        assertEquals(expectedRows, actualRows)
    }

    @Test
    fun `Set-create must create rows when no fields are specified in the model root`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |  }
            |}
        """.trimMargin()
        val create = MutableEntityModel(mySqlAddressBookMetaModel, null)
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedResponse = Response.Success
        val actualResponse = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedResponse, actualResponse)
        val expectedRows = """
            |= Table main__address_book_root =
            |
            |* Row 1 *
            |created_on_: 2022-04-14 00:40:41.450
            |updated_on_: 2022-04-14 00:40:41.450
            |field_path_: /address_book
            |singleton_key_: 0
            |name: null
            |last_updated: null
            |
        """.trimMargin()
        val actualRows = getTableRows(testDataSource, TEST_DATABASE, "main__address_book_root")
        assertEquals(expectedRows, actualRows)
    }

    @Test
    fun `Set-create must create rows when only composition fields are specified in the model root`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "settings": {
            |      "last_name_first": true
            |    }
            |  }
            |}
        """.trimMargin()
        val create = MutableEntityModel(mySqlAddressBookMetaModel, null)
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedResponse = Response.Success
        val actualResponse = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedResponse, actualResponse)
        val expectedRows = """
            |= Table main__address_book_root =
            |
            |* Row 1 *
            |created_on_: 2022-04-14 00:40:41.450
            |updated_on_: 2022-04-14 00:40:41.450
            |field_path_: /address_book
            |singleton_key_: 0
            |name: null
            |last_updated: null
            |
            |= Table main__address_book_settings =
            |
            |* Row 1 *
            |created_on_: 2022-04-14 00:40:41.450
            |updated_on_: 2022-04-14 00:40:41.450
            |field_path_: /address_book/settings
            |last_name_first: 1
            |encrypt_hero_name: null
            |main__address_book_root__singleton_key_: 0
            |
        """.trimMargin()
        val actualRows =
            getTableRows(testDataSource, TEST_DATABASE, "main__address_book_root", "main__address_book_settings")
        assertEquals(expectedRows, actualRows)
    }

    @Test
    fun `Set-create must create rows when no fields are specified in a single-composition entity`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "settings": {
            |    }
            |  }
            |}
        """.trimMargin()
        val create = MutableEntityModel(mySqlAddressBookMetaModel, null)
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedResponse = Response.Success
        val actualResponse = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedResponse, actualResponse)
        val expectedRows = """
            |= Table main__address_book_root =
            |
            |* Row 1 *
            |created_on_: 2022-04-14 00:40:41.450
            |updated_on_: 2022-04-14 00:40:41.450
            |field_path_: /address_book
            |singleton_key_: 0
            |name: null
            |last_updated: null
            |
            |= Table main__address_book_settings =
            |
            |* Row 1 *
            |created_on_: 2022-04-14 00:40:41.450
            |updated_on_: 2022-04-14 00:40:41.450
            |field_path_: /address_book/settings
            |last_name_first: null
            |encrypt_hero_name: null
            |main__address_book_root__singleton_key_: 0
            |
        """.trimMargin()
        val actualRows =
            getTableRows(testDataSource, TEST_DATABASE, "main__address_book_root", "main__address_book_settings")
        assertEquals(expectedRows, actualRows)
    }

    @Test
    fun `Set-create must create rows when only key fields are specified in a composition-set entity`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "person": [
            |      {
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val create = MutableEntityModel(mySqlAddressBookMetaModel, null)
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedResponse = Response.Success
        val actualResponse = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedResponse, actualResponse)
        val expectedRows = """
            |= Table main__address_book_person =
            |
            |* Row 1 *
            |created_on_: 2022-04-14 00:40:41.450
            |updated_on_: 2022-04-14 00:40:41.450
            |field_path_: /address_book/person
            |id: a8aacf55-7810-4b43-afe5-4344f25435fd
            |first_name: null
            |last_name: null
            |hero_name: null
            |picture: null
            |self: null
            |self__id: null
            |main__address_book_root__singleton_key_: 0
            |main__person_group__id: null
            |
        """.trimMargin()
        val actualRows = getTableRows(testDataSource, TEST_DATABASE, "main__address_book_person")
        assertEquals(expectedRows, actualRows)
    }

    @Test
    fun `Set-create must fail for an old model`() {
        val create = MutableEntityModel(mySqlAddressBookMetaModel, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_address_book_1_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedRows = readFile("operator/my_sql_address_book_1_set_create_results.txt")

        // Create the model the first time.
        val expectedResponse1 = Response.Success
        val actualResponse1 = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedResponse1, actualResponse1)
        val actualRows1 = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(expectedRows, actualRows1)

        // Try to create the same model again. It should fail.
        val actualResponse2 = set(create, setEntityDelegates, testDataSource, clock = clock)
        val expectedResponse2 = Response.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError("/address_book", "unable to create: duplicate"),
                ElementModelError("/address_book/settings", "unable to create: duplicate"),
                ElementModelError("/address_book/settings/advanced", "unable to create: duplicate"),
                ElementModelError(
                    "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation/05ade278-4b44-43da-a0cc-14463854e397",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation/3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/password",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/secret",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/relation/16634916-8f83-4376-ad42-37038e108a0b",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/password",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/secret",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/person/ec983c56-320f-4d66-9dde-f180e8ac3807",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups/fe2aa774-e1fe-4680-a439-8bd1d0eb4abc",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups/fe2aa774-e1fe-4680-a439-8bd1d0eb4abc/persons/546a4982-b39a-4d01-aeb3-22d60c6963c0",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups/fe2aa774-e1fe-4680-a439-8bd1d0eb4abc/persons/e391c509-67d6-4846-bfea-0f7cd9c91bf7",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/groups/ad9aaea8-30fe-45ed-93ef-bd368da0c756",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/city_info/New York City/New York/United States of America",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/city_info/Albany/New York/United States of America",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/city_info/Princeton/New Jersey/United States of America",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/address_book/city_info/San Francisco/California/United States of America",
                    "unable to create: duplicate"
                ),
            )
        )
        assertSetResponse(expectedResponse2, actualResponse2)
        val actualRows2 = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(expectedRows, actualRows2)
    }

    @Test
    fun `Set-create must fail when creating a child without a parent`() {
        val emptyDatabaseRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        val modelJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "relation": [
            |          {
            |            "set_": "create",
            |            "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |            "relationship": "colleague"
            |          }
            |        ]
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val expectedResponse = Response.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError(
                    "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation/05ade278-4b44-43da-a0cc-14463854e397",
                    "unable to create: no parent or target entity"
                )
            )
        )
        val create = MutableEntityModel(mySqlAddressBookMetaModel, null)
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val actualResponse = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedResponse, actualResponse)
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterCreateRows)
    }

    @Test
    fun `Set-create must fail when creating an entity with existing keys but different entity path`() {
        val emptyDatabaseRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        val create1Json = """
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
        val expectedResponse1 = Response.Success
        val create1 = MutableEntityModel(mySqlAddressBookMetaModel, null)
        decodeJsonStringIntoEntity(
            create1Json,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create1
        )
        val actualResponse1 = set(create1, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedResponse1, actualResponse1)
        val afterCreate1Rows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreate1Rows)

        // Attempt to recreate the relation entity but under a different person entity.
        val create2Json = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "set_": "create",
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |        "first_name": "Lois",
            |        "last_name": "Lane",
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
        val expectedResponse2 = Response.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError(
                    "/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/relation/05ade278-4b44-43da-a0cc-14463854e397",
                    "unable to create: duplicate"
                )
            )
        )
        val create2 = MutableEntityModel(mySqlAddressBookMetaModel, null)
        decodeJsonStringIntoEntity(
            create2Json,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create2
        )
        val actualResponse2 = set(create2, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedResponse2, actualResponse2)
        val afterCreate2Rows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(afterCreate1Rows, afterCreate2Rows)
    }
}