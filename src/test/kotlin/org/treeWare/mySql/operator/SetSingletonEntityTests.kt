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
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.mySql.test.clearDatabase
import org.treeWare.mySql.test.getDatabaseRows
import org.treeWare.mySql.test.getTableRows
import org.treeWare.mySql.testDataSource
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SetSingletonEntityTests {
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


    private fun getSingletonTableRows(): String =
        getTableRows(
            testDataSource,
            TEST_DATABASE,
            "main__address_book_root",
            "main__address_book_settings",
            "main__advanced_settings"
        )

    @Test
    fun `set() must succeed when creating new singleton entities`() {
        val create = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_create_singleton_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCreateResponse = SetResponse.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRowsExpected = readFile("operator/my_sql_create_singleton_entities_results.txt")
        val afterCreateRows = getSingletonTableRows()
        assertEquals(afterCreateRowsExpected, afterCreateRows)
    }

    @Test
    fun `set() must fail when recreating existing singleton entities`() {
        val create = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_create_singleton_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCreateResponse = SetResponse.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        val expectedRecreateResponse = SetResponse.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError("/address_book", "unable to create: duplicate"),
                ElementModelError("/address_book/settings", "unable to create: duplicate"),
                ElementModelError("/address_book/settings/advanced", "unable to create: duplicate"),
            )
        )
        val actualRecreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedRecreateResponse, actualRecreateResponse)
        val afterRecreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(afterCreateRows, afterRecreateRows)
    }

    @Test
    fun `set() must succeed when updating existing singleton entities`() {
        val create = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_create_singleton_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCreateResponse = SetResponse.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRowsExpected = readFile("operator/my_sql_create_singleton_entities_results.txt")
        val afterCreateRows = getSingletonTableRows()
        assertEquals(afterCreateRowsExpected, afterCreateRows)

        val update = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_update_singleton_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedUpdateResponse = SetResponse.Success
        val actualUpdateResponse = set(update, setEntityDelegates, testDataSource, clock = updateClock)
        assertSetResponse(expectedUpdateResponse, actualUpdateResponse)
        val afterUpdateRowsExpected = readFile("operator/my_sql_update_singleton_entities_results.txt")
        val afterUpdateRows = getSingletonTableRows()
        assertEquals(afterUpdateRowsExpected, afterUpdateRows)
    }

    @Test
    fun `set() must fail when updating non-existing singleton entities`() {
        val update = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_update_singleton_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedUpdateResponse = SetResponse.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError("/address_book", "unable to update"),
                ElementModelError("/address_book/settings", "unable to update"),
                ElementModelError("/address_book/settings/advanced", "unable to update"),
            )
        )
        val actualUpdateResponse = set(update, setEntityDelegates, testDataSource, clock = updateClock)
        assertSetResponse(expectedUpdateResponse, actualUpdateResponse)
        val afterUpdateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterUpdateRows)
    }

    @Test
    fun `set() must succeed when deleting existing singleton entities`() {
        val create = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_create_singleton_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCreateResponse = SetResponse.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        val delete = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_delete_singleton_entities_bottoms_up.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedDeleteResponse = SetResponse.Success
        val actualDeleteResponse = set(delete, setEntityDelegates, testDataSource, clock = updateClock)
        assertSetResponse(expectedDeleteResponse, actualDeleteResponse)
        val afterUpdateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterUpdateRows)
    }

    @Test
    fun `set() must succeed when deleting non-existing singleton entities`() {
        val delete = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_delete_singleton_entities_bottoms_up.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedDeleteResponse = SetResponse.Success
        val actualDeleteResponse = set(delete, setEntityDelegates, testDataSource, clock = updateClock)
        assertSetResponse(expectedDeleteResponse, actualDeleteResponse)
        val afterUpdateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterUpdateRows)
    }

    @Test
    fun `set() must fail when creating a singleton entity without a parent`() {
        val create = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_create_singleton_entities_no_parent.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCreateResponse = SetResponse.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError("/address_book/settings", "unable to create: no parent or target entity"),
                ElementModelError("/address_book/settings/advanced", "unable to create: no parent or target entity"),
            )
        )
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterCreateRows)
    }

    @Test
    fun `set() must fail when deleting a singleton entity that has children in the database`() {
        val create = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_create_singleton_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCreateResponse = SetResponse.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        val deleteJson = """
            |{
            |  "address_book": {
            |    "set_": "delete"
            |  }
            |}
        """.trimMargin()
        val delete =
            getMainModelFromJsonString(
                mySqlAddressBookMetaModel,
                deleteJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )
        val expectedDeleteResponse = SetResponse.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError(
                    "/address_book: unable to delete",
                    "has children or source entity"
                )
            )
        )
        val actualDeleteResponse = set(delete, setEntityDelegates, testDataSource, clock = updateClock)
        assertSetResponse(expectedDeleteResponse, actualDeleteResponse)
        val afterDeleteRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(afterCreateRows, afterDeleteRows)
    }

    @Test
    fun `set() must succeed when creating children of existing singleton entities`() {
        val createRootJson = """
            |{
            |  "address_book": {
            |    "set_": "create",
            |    "name": "Super Heroes"
            |  }
            |}
        """.trimMargin()
        val createRoot = getMainModelFromJsonString(
            mySqlAddressBookMetaModel,
            createRootJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCreateRootResponse = SetResponse.Success
        val actualCreateRootResponse = set(createRoot, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateRootResponse, actualCreateRootResponse)
        val afterCreateRootRowsExpected = """
            |= Table main__address_book_root =
            |
            |* Row 1 *
            |created_on_: 2022-03-03 00:30:31.330
            |updated_on_: 2022-03-03 00:30:31.330
            |field_path_: /address_book
            |singleton_key_: 0
            |name: Super Heroes
            |last_updated: null
            |
            |= Table main__address_book_settings =
            |
            |= Table main__advanced_settings =
            |
        """.trimMargin()
        val afterCreateRootRows = getSingletonTableRows()
        assertEquals(afterCreateRootRowsExpected, afterCreateRootRows)

        val createChildrenJson = """
            |{
            |  "address_book": {
            |    "settings": {
            |      "set_": "create",
            |      "last_name_first": true,
            |      "advanced": {
            |        "set_": "create",
            |        "background_color": "blue"
            |      }
            |    }
            |  }
            |}
        """.trimMargin()
        val createChildren = getMainModelFromJsonString(
            mySqlAddressBookMetaModel,
            createChildrenJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedCreateChildrenResponse = SetResponse.Success
        val actualCreateChildrenResponse = set(createChildren, setEntityDelegates, testDataSource, clock = updateClock)
        assertSetResponse(expectedCreateChildrenResponse, actualCreateChildrenResponse)
        val afterCreateChildrenRowsExpected = """
            |= Table main__address_book_root =
            |
            |* Row 1 *
            |created_on_: 2022-03-03 00:30:31.330
            |updated_on_: 2022-03-03 00:30:31.330
            |field_path_: /address_book
            |singleton_key_: 0
            |name: Super Heroes
            |last_updated: null
            |
            |= Table main__address_book_settings =
            |
            |* Row 1 *
            |created_on_: 2022-04-04 00:40:41.440
            |updated_on_: 2022-04-04 00:40:41.440
            |field_path_: /address_book/settings
            |last_name_first: 1
            |encrypt_hero_name: null
            |card_colors: null
            |main__address_book_root__singleton_key_: 0
            |
            |= Table main__advanced_settings =
            |
            |* Row 1 *
            |created_on_: 2022-04-04 00:40:41.440
            |updated_on_: 2022-04-04 00:40:41.440
            |field_path_: /address_book/settings/advanced
            |background_color: 3
            |main__address_book_root__singleton_key_: 0
            |
        """.trimMargin()
        val afterCreateChildrenRows = getSingletonTableRows()
        assertEquals(afterCreateChildrenRowsExpected, afterCreateChildrenRows)
    }
}