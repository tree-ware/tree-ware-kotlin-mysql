package org.treeWare.mySql.operator

import com.wix.mysql.EmbeddedMysql
import com.wix.mysql.config.MysqldConfig
import com.wix.mysql.distribution.Version
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.treeWare.metaModel.mySqlAddressBookMetaModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.getMainModelFromJsonFile
import org.treeWare.model.getMainModelFromJsonString
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.OperatorEntityDelegateRegistry
import org.treeWare.model.operator.SetEntityDelegate
import org.treeWare.model.operator.SetOperatorId
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import org.treeWare.model.readFile
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.mySql.test.clearDatabase
import org.treeWare.mySql.test.getAvailableServerPort
import org.treeWare.mySql.test.getDatabaseRows
import org.treeWare.mySql.test.getTableRows
import java.sql.Connection
import java.sql.DriverManager
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

private const val TEST_DATABASE = "test\$address_book"

private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

private const val CREATE_TIME = "2022-03-03T00:30:31.330Z"
private val createClock = Clock.fixed(Instant.parse(CREATE_TIME), ZoneOffset.UTC)

private const val UPDATE_TIME = "2022-04-04T00:40:41.440Z"
private val updateClock = Clock.fixed(Instant.parse(UPDATE_TIME), ZoneOffset.UTC)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SetKeylessEntityTests {
    private val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
    private val setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?

    private val port = getAvailableServerPort()
    private val mysqld: EmbeddedMysql
    private val connection: Connection
    private val emptyDatabaseRows: String

    init {
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        setEntityDelegates = operatorEntityDelegateRegistry.get(SetOperatorId)

        val config = MysqldConfig.aMysqldConfig(Version.v8_0_17)
            .withPort(port)
            .withServerVariable("mysqlx", 0) // disable the X plugin
            .build()
        mysqld = EmbeddedMysql.anEmbeddedMysql(config).start()
        connection = DriverManager.getConnection("jdbc:mysql://localhost:$port/", "root", "")
        connection.autoCommit = false

        val createDbEntityDelegates = operatorEntityDelegateRegistry.get(GenerateCreateDatabaseCommandsOperatorId)
        createDatabase(mySqlAddressBookMetaModel, createDbEntityDelegates, connection)
        emptyDatabaseRows = getDatabaseRows(connection, TEST_DATABASE)
    }

    @AfterEach
    fun afterEach() {
        clearDatabase(connection, TEST_DATABASE)
    }

    @AfterAll
    fun afterAll() {
        mysqld.stop()
    }

    private fun getKeylessTableRows(): String =
        getTableRows(connection, TEST_DATABASE, "keyless\$keyless", "keyless\$keyless_child", "keyless\$keyed_child")

    @Test
    fun `set() must succeed when creating new keyless entities`() {
        val create = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_create_keyless_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val createErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals("", createErrors.joinToString("\n"))
        val afterCreateRowsExpected = readFile("operator/my_sql_create_keyless_entities_results.txt")
        val afterCreateRows = getKeylessTableRows()
        assertEquals(afterCreateRowsExpected, afterCreateRows)
    }

    @Test
    fun `set() must fail when recreating existing keyless entities`() {
        val create = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_create_keyless_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val createErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals("", createErrors.joinToString("\n"))
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        val recreateErrorsExpected = listOf(
            "/address_book: unable to create: duplicate",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]: unable to create: duplicate",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/keyless: unable to create: duplicate",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/keyless/keyless_child: unable to create: duplicate",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/keyless/keyed_child[Clark keyed child]: unable to create: duplicate",
            "/address_book/city_info[Fremont,California,USA]: unable to create: duplicate",
            "/address_book/city_info[Fremont,California,USA]/keyless: unable to create: duplicate",
            "/address_book/city_info[Fremont,California,USA]/keyless/keyless_child: unable to create: duplicate",
            "/address_book/city_info[Fremont,California,USA]/keyless/keyed_child[Fremont keyed child]: unable to create: duplicate",
        )
        val recreateErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals(recreateErrorsExpected.joinToString("\n"), recreateErrors.joinToString("\n"))
        val afterRecreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(afterCreateRows, afterRecreateRows)
    }

    @Test
    fun `set() must succeed when updating existing keyless entities`() {
        val create = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_create_keyless_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val createErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals("", createErrors.joinToString("\n"))
        val afterCreateRowsExpected = readFile("operator/my_sql_create_keyless_entities_results.txt")
        val afterCreateRows = getKeylessTableRows()
        assertEquals(afterCreateRowsExpected, afterCreateRows)

        val update = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_update_keyless_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val updateErrors = set(update, setEntityDelegates, connection, clock = updateClock)
        assertEquals("", updateErrors.joinToString("\n"))
        val afterUpdateRowsExpected = readFile("operator/my_sql_update_keyless_entities_results.txt")
        val afterUpdateRows = getKeylessTableRows()
        assertEquals(afterUpdateRowsExpected, afterUpdateRows)
    }

    @Test
    fun `set() must fail when updating non-existing keyless entities`() {
        val update = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_update_keyless_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedUpdateErrors = listOf(
            "/address_book: unable to update",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]: unable to update",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/keyless: unable to update",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/keyless/keyless_child: unable to update",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/keyless/keyed_child[Clark keyed child]: unable to update",
            "/address_book/city_info[Fremont,California,USA]: unable to update",
            "/address_book/city_info[Fremont,California,USA]/keyless: unable to update",
            "/address_book/city_info[Fremont,California,USA]/keyless/keyless_child: unable to update",
            "/address_book/city_info[Fremont,California,USA]/keyless/keyed_child[Fremont keyed child]: unable to update",
        )
        val updateErrors = set(update, setEntityDelegates, connection, clock = updateClock)
        assertEquals(expectedUpdateErrors.joinToString("\n"), updateErrors.joinToString("\n"))
        val afterUpdateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterUpdateRows)
    }

    @Test
    fun `set() must succeed when deleting existing keyless entities`() {
        val create = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_create_keyless_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val createErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals("", createErrors.joinToString("\n"))
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        val delete = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_delete_keyless_entities_bottoms_up.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val deleteErrors = set(delete, setEntityDelegates, connection, clock = updateClock)
        assertEquals("", deleteErrors.joinToString("\n"))
        val afterUpdateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterUpdateRows)
    }

    @Test
    fun `set() must succeed when deleting non-existing keyless entities`() {
        val delete = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_delete_keyless_entities_bottoms_up.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val deleteErrors = set(delete, setEntityDelegates, connection, clock = updateClock)
        assertEquals("", deleteErrors.joinToString("\n"))
        val afterUpdateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterUpdateRows)
    }

    @Test
    fun `set() must fail when creating a keyless entity without a parent`() {
        val create = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_create_keyless_entities_no_parent.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val createErrorsExpected = listOf(
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/keyless: unable to create: no parent or target entity",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/keyless/keyless_child: unable to create: no parent or target entity",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/keyless/keyed_child[Clark keyed child]: unable to create: no parent or target entity",
            "/address_book/city_info[Fremont,California,USA]/keyless: unable to create: no parent or target entity",
            "/address_book/city_info[Fremont,California,USA]/keyless/keyless_child: unable to create: no parent or target entity",
            "/address_book/city_info[Fremont,California,USA]/keyless/keyed_child[Fremont keyed child]: unable to create: no parent or target entity",
        )
        val createErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals(createErrorsExpected.joinToString("\n"), createErrors.joinToString("\n"))
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterCreateRows)
    }

    @Test
    fun `set() must fail when deleting a keyless entity that has children in the database`() {
        val create = getMainModelFromJsonFile(
            mySqlAddressBookMetaModel,
            "model/my_sql_create_keyless_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val createErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals("", createErrors.joinToString("\n"))
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        val deleteJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "keyless": {
            |          "set_": "delete"
            |        }
            |      }
            |    ],
            |    "city_info": [
            |      {
            |        "name": "Fremont",
            |        "state": "California",
            |        "country": "USA",
            |        "keyless": {
            |          "set_": "delete"
            |        }
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val delete =
            getMainModelFromJsonString(
                mySqlAddressBookMetaModel,
                deleteJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )
        val deleteErrorsExpected = listOf(
            "/address_book/city_info[Fremont,California,USA]/keyless: unable to delete: has children or source entity",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/keyless: unable to delete: has children or source entity",
        )
        val deleteErrors = set(delete, setEntityDelegates, connection, clock = updateClock)
        assertEquals(deleteErrorsExpected.joinToString("\n"), deleteErrors.joinToString("\n"))
        val afterDeleteRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(afterCreateRows, afterDeleteRows)
    }
}