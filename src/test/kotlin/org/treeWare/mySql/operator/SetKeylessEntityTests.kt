package org.treeWare.mySql.operator

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.decodeJsonFileIntoEntity
import org.treeWare.model.decodeJsonStringIntoEntity
import org.treeWare.mySql.test.metaModel.mySqlAddressBookMetaModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.operator.*
import org.treeWare.model.operator.Response
import org.treeWare.model.operator.set.assertSetResponse
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.mySql.test.clearDatabase
import org.treeWare.mySql.test.getDatabaseRows
import org.treeWare.mySql.test.getTableRows
import org.treeWare.mySql.test.metaModel.mySqlAddressBookRootEntityMeta
import org.treeWare.mySql.test.testDataSource
import org.treeWare.util.readFile
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

private const val TEST_DATABASE = "test__address_book"

private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

private const val CREATE_TIME = "2022-03-03T00:30:31.330Z"
private val createClock = Clock.fixed(Instant.parse(CREATE_TIME), ZoneOffset.UTC)

private const val UPDATE_TIME = "2022-04-04T00:40:41.440Z"
private val updateClock = Clock.fixed(Instant.parse(UPDATE_TIME), ZoneOffset.UTC)

@Tag("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SetKeylessEntityTests {
    private val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
    private val setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?

    private val emptyDatabaseRows: String

    init {
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        setEntityDelegates = operatorEntityDelegateRegistry.get(SetOperatorId)

        val createDbEntityDelegates = operatorEntityDelegateRegistry.get(GenerateDdlCommandsOperatorId)
        createDatabase(mySqlAddressBookMetaModel, createDbEntityDelegates, testDataSource)
        emptyDatabaseRows = getDatabaseRows(testDataSource, TEST_DATABASE)
    }

    @AfterEach
    fun afterEach() {
        clearDatabase(testDataSource, TEST_DATABASE)
    }


    private fun getKeylessTableRows(): String =
        getTableRows(
            testDataSource,
            TEST_DATABASE,
            "keyless__keyless",
            "keyless__keyless_child",
            "keyless__keyed_child"
        )

    @Test
    fun `set() must succeed when creating new keyless entities`() {
        val create = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_create_keyless_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedCreateResponse = Response.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRowsExpected = readFile("operator/my_sql_create_keyless_entities_results.txt")
        val afterCreateRows = getKeylessTableRows()
        assertEquals(afterCreateRowsExpected, afterCreateRows)
    }

    @Test
    fun `set() must fail when recreating existing keyless entities`() {
        val create = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_create_keyless_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedCreateResponse = Response.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        val expectedRecreateResponse = Response.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError("/", "unable to create: duplicate"),
                ElementModelError(
                    "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/keyless",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/keyless/keyless_child",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/keyless/keyed_child/Clark keyed child",
                    "unable to create: duplicate"
                ),
                ElementModelError("/city_info/Fremont/California/USA", "unable to create: duplicate"),
                ElementModelError(
                    "/city_info/Fremont/California/USA/keyless", "unable to create: duplicate"
                ),
                ElementModelError(
                    "/city_info/Fremont/California/USA/keyless/keyless_child",
                    "unable to create: duplicate"
                ),
                ElementModelError(
                    "/city_info/Fremont/California/USA/keyless/keyed_child/Fremont keyed child",
                    "unable to create: duplicate"
                ),
            )
        )
        val actualRecreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedRecreateResponse, actualRecreateResponse)
        val afterRecreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(afterCreateRows, afterRecreateRows)
    }

    @Test
    fun `set() must succeed when updating existing keyless entities`() {
        val create = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_create_keyless_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedCreateResponse = Response.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRowsExpected = readFile("operator/my_sql_create_keyless_entities_results.txt")
        val afterCreateRows = getKeylessTableRows()
        assertEquals(afterCreateRowsExpected, afterCreateRows)

        val update = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_update_keyless_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = update
        )
        val expectedUpdateResponse = Response.Success
        val actualUpdateResponse = set(update, setEntityDelegates, testDataSource, clock = updateClock)
        assertSetResponse(expectedUpdateResponse, actualUpdateResponse)
        val afterUpdateRowsExpected = readFile("operator/my_sql_update_keyless_entities_results.txt")
        val afterUpdateRows = getKeylessTableRows()
        assertEquals(afterUpdateRowsExpected, afterUpdateRows)
    }

    @Test
    fun `set() must fail when updating non-existing keyless entities`() {
        val update = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_update_keyless_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = update
        )
        val expectedUpdateResponse = Response.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError("/", "unable to update"),
                ElementModelError("/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f", "unable to update"),
                ElementModelError(
                    "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/keyless",
                    "unable to update"
                ),
                ElementModelError(
                    "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/keyless/keyless_child",
                    "unable to update"
                ),
                ElementModelError(
                    "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/keyless/keyed_child/Clark keyed child",
                    "unable to update"
                ),
                ElementModelError("/city_info/Fremont/California/USA", "unable to update"),
                ElementModelError("/city_info/Fremont/California/USA/keyless", "unable to update"),
                ElementModelError(
                    "/city_info/Fremont/California/USA/keyless/keyless_child",
                    "unable to update"
                ),
                ElementModelError(
                    "/city_info/Fremont/California/USA/keyless/keyed_child/Fremont keyed child",
                    "unable to update"
                ),
            )
        )
        val actualUpdateResponse = set(update, setEntityDelegates, testDataSource, clock = updateClock)
        assertSetResponse(expectedUpdateResponse, actualUpdateResponse)
        val afterUpdateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterUpdateRows)
    }

    @Test
    fun `set() must succeed when deleting existing keyless entities`() {
        val create = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_create_keyless_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedCreateResponse = Response.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        val delete = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_delete_keyless_entities_bottoms_up.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = delete
        )
        val expectedDeleteResponse = Response.Success
        val actualDeleteResponse = set(delete, setEntityDelegates, testDataSource, clock = updateClock)
        assertSetResponse(expectedDeleteResponse, actualDeleteResponse)
        val afterUpdateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterUpdateRows)
    }

    @Test
    fun `set() must succeed when deleting non-existing keyless entities`() {
        val delete = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_delete_keyless_entities_bottoms_up.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = delete
        )
        val expectedDeleteResponse = Response.Success
        val actualDeleteResponse = set(delete, setEntityDelegates, testDataSource, clock = updateClock)
        assertSetResponse(expectedDeleteResponse, actualDeleteResponse)
        val afterUpdateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterUpdateRows)
    }

    @Test
    fun `set() must fail when creating a keyless entity without a parent`() {
        val create = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_create_keyless_entities_no_parent.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedCreateResponse = Response.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError(
                    "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/keyless",
                    "unable to create: no parent or target entity"
                ),
                ElementModelError(
                    "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/keyless/keyless_child",
                    "unable to create: no parent or target entity"
                ),
                ElementModelError(
                    "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/keyless/keyed_child/Clark keyed child",
                    "unable to create: no parent or target entity"
                ),
                ElementModelError(
                    "/city_info/Fremont/California/USA/keyless",
                    "unable to create: no parent or target entity"
                ),
                ElementModelError(
                    "/city_info/Fremont/California/USA/keyless/keyless_child",
                    "unable to create: no parent or target entity"
                ),
                ElementModelError(
                    "/city_info/Fremont/California/USA/keyless/keyed_child/Fremont keyed child",
                    "unable to create: no parent or target entity"
                ),
            )
        )
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterCreateRows)
    }

    @Test
    fun `set() must fail when deleting a keyless entity that has children in the database`() {
        val create = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_create_keyless_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedCreateResponse = Response.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        val deleteJson = """
            |{
            |  "person": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "keyless": {
            |        "set_": "delete"
            |      }
            |    }
            |  ],
            |  "city_info": [
            |    {
            |      "name": "Fremont",
            |      "state": "California",
            |      "country": "USA",
            |      "keyless": {
            |        "set_": "delete"
            |      }
            |    }
            |  ]
            |}
        """.trimMargin()
        val delete = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonStringIntoEntity(
            deleteJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = delete
        )
        val expectedDeleteResponse = Response.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError(
                    "/city_info/Fremont/California/USA/keyless",
                    "unable to delete: has children or source entity"
                ),
                ElementModelError(
                    "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/keyless",
                    "unable to delete: has children or source entity"
                ),
            )
        )
        val actualDeleteResponse = set(delete, setEntityDelegates, testDataSource, clock = updateClock)
        assertSetResponse(expectedDeleteResponse, actualDeleteResponse)
        val afterDeleteRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(afterCreateRows, afterDeleteRows)
    }
}