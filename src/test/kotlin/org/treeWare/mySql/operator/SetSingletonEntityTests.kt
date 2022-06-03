package org.treeWare.mySql.operator

import com.wix.mysql.EmbeddedMysql
import com.wix.mysql.config.MysqldConfig
import com.wix.mysql.distribution.Version
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.treeWare.metaModel.newMySqlAddressBookMetaModel
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

private val metaModel = newMySqlAddressBookMetaModel("test", null, null).metaModel
    ?: throw IllegalStateException("Meta-model has validation errors")
private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })


private const val CREATE_TIME = "2022-03-03T00:30:31.330Z"
private val createClock = Clock.fixed(Instant.parse(CREATE_TIME), ZoneOffset.UTC)

private const val UPDATE_TIME = "2022-04-04T00:40:41.440Z"
private val updateClock = Clock.fixed(Instant.parse(UPDATE_TIME), ZoneOffset.UTC)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SetSingletonEntityTests {
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
        createDatabase(metaModel, createDbEntityDelegates, connection)
        emptyDatabaseRows = getDatabaseRows(connection, TEST_DATABASE)
    }

    @AfterEach()
    fun afterEach() {
        clearDatabase(connection, TEST_DATABASE)
    }

    @AfterAll
    fun afterAll() {
        mysqld.stop()
    }

    private fun getSingletonTableRows(): String =
        getTableRows(
            connection,
            TEST_DATABASE,
            "main\$address_book_root",
            "main\$address_book_settings",
            "main\$advanced_settings"
        )

    @Test
    fun `set() must succeed when creating new singleton entities`() {
        val create = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_create_singleton_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val createErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals("", createErrors.joinToString("\n"))
        val afterCreateRowsExpected = readFile("operator/my_sql_create_singleton_entities_results.txt")
        val afterCreateRows = getSingletonTableRows()
        assertEquals(afterCreateRowsExpected, afterCreateRows)
    }

    @Test
    fun `set() must fail when recreating existing singleton entities`() {
        val create = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_create_singleton_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val createErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals("", createErrors.joinToString("\n"))
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        val recreateErrorsExpected = listOf<String>(
            "/address_book: unable to create: duplicate",
            "/address_book/settings: unable to create: duplicate",
            "/address_book/settings/advanced: unable to create: duplicate",
        )
        val recreateErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals(recreateErrorsExpected.joinToString("\n"), recreateErrors.joinToString("\n"))
        val afterRecreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(afterCreateRows, afterRecreateRows)
    }

    @Test
    fun `set() must succeed when updating existing singleton entities`() {
        val create = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_create_singleton_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val createErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals("", createErrors.joinToString("\n"))
        val afterCreateRowsExpected = readFile("operator/my_sql_create_singleton_entities_results.txt")
        val afterCreateRows = getSingletonTableRows()
        assertEquals(afterCreateRowsExpected, afterCreateRows)

        val update = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_update_singleton_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val updateErrors = set(update, setEntityDelegates, connection, clock = updateClock)
        assertEquals("", updateErrors.joinToString("\n"))
        val afterUpdateRowsExpected = readFile("operator/my_sql_update_singleton_entities_results.txt")
        val afterUpdateRows = getSingletonTableRows()
        assertEquals(afterUpdateRowsExpected, afterUpdateRows)
    }

    @Test
    fun `set() must fail when updating non-existing singleton entities`() {
        val update = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_update_singleton_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedUpdateErrors = listOf(
            "/address_book: unable to update",
            "/address_book/settings: unable to update",
            "/address_book/settings/advanced: unable to update",
        )
        val updateErrors = set(update, setEntityDelegates, connection, clock = updateClock)
        assertEquals(expectedUpdateErrors.joinToString("\n"), updateErrors.joinToString("\n"))
        val afterUpdateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterUpdateRows)
    }

    @Test
    fun `set() must succeed when deleting existing singleton entities`() {
        val create = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_create_singleton_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val createErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals("", createErrors.joinToString("\n"))
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        val delete = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_delete_singleton_entities_bottoms_up.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val deleteErrors = set(delete, setEntityDelegates, connection, clock = updateClock)
        assertEquals("", deleteErrors.joinToString("\n"))
        val afterUpdateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterUpdateRows)
    }

    @Test
    fun `set() must succeed when deleting non-existing singleton entities`() {
        val delete = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_delete_singleton_entities_bottoms_up.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val deleteErrors = set(delete, setEntityDelegates, connection, clock = updateClock)
        assertEquals("", deleteErrors.joinToString("\n"))
        val afterUpdateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterUpdateRows)
    }

    @Test
    fun `set() must fail when creating a singleton entity without a parent`() {
        val create = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_create_singleton_entities_no_parent.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val createErrorsExpected = listOf(
            "/address_book/settings: unable to create: no parent or target entity",
            "/address_book/settings/advanced: unable to create: no parent or target entity",
        )
        val createErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals(createErrorsExpected.joinToString("\n"), createErrors.joinToString("\n"))
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterCreateRows)
    }

    @Test
    fun `set() must fail when deleting a singleton entity that has children in the database`() {
        val create = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_create_singleton_entities.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val createErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals("", createErrors.joinToString("\n"))
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        val deleteJson = """
            |{
            |  "address_book": {
            |    "set_": "delete"
            |  }
            |}
        """.trimMargin()
        val delete =
            getMainModelFromJsonString(metaModel, deleteJson, multiAuxDecodingStateMachineFactory = auxDecodingFactory)
        val deleteErrorsExpected = listOf("/address_book: unable to delete: has children or source entity")
        val deleteErrors = set(delete, setEntityDelegates, connection, clock = updateClock)
        assertEquals(deleteErrorsExpected.joinToString("\n"), deleteErrors.joinToString("\n"))
        val afterDeleteRows = getDatabaseRows(connection, TEST_DATABASE)
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
            metaModel,
            createRootJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val createRootErrors = set(createRoot, setEntityDelegates, connection, clock = createClock)
        assertEquals("", createRootErrors.joinToString("\n"))
        val afterCreateRootRowsExpected = """
            |= Table main${'$'}address_book_root =
            |
            |* Row 1 *
            |created_on${'$'}: 2022-03-03 00:30:31.330
            |updated_on${'$'}: 2022-03-03 00:30:31.330
            |field_path${'$'}: /address_book
            |singleton_key${'$'}: 0
            |name: Super Heroes
            |last_updated: null
            |
            |= Table main${'$'}address_book_settings =
            |
            |= Table main${'$'}advanced_settings =
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
            metaModel,
            createChildrenJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val createChildrenErrors = set(createChildren, setEntityDelegates, connection, clock = updateClock)
        assertEquals("", createChildrenErrors.joinToString("\n"))
        val afterCreateChildrenRowsExpected = """
            |= Table main${'$'}address_book_root =
            |
            |* Row 1 *
            |created_on${'$'}: 2022-03-03 00:30:31.330
            |updated_on${'$'}: 2022-03-03 00:30:31.330
            |field_path${'$'}: /address_book
            |singleton_key${'$'}: 0
            |name: Super Heroes
            |last_updated: null
            |
            |= Table main${'$'}address_book_settings =
            |
            |* Row 1 *
            |created_on${'$'}: 2022-04-04 00:40:41.440
            |updated_on${'$'}: 2022-04-04 00:40:41.440
            |field_path${'$'}: /address_book/settings
            |main${'$'}address_book_root${'$'}singleton_key${'$'}: 0
            |last_name_first: 1
            |encrypt_hero_name: null
            |card_colors: null
            |
            |= Table main${'$'}advanced_settings =
            |
            |* Row 1 *
            |created_on${'$'}: 2022-04-04 00:40:41.440
            |updated_on${'$'}: 2022-04-04 00:40:41.440
            |field_path${'$'}: /address_book/settings/advanced
            |main${'$'}address_book_root${'$'}singleton_key${'$'}: 0
            |background_color: 3
            |
        """.trimMargin()
        val afterCreateChildrenRows = getSingletonTableRows()
        assertEquals(afterCreateChildrenRowsExpected, afterCreateChildrenRows)
    }
}