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
class SetUpdateTests {
    private val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
    private val setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?

    private val port = getAvailableServerPort()
    private val mysqld: EmbeddedMysql
    private val connection: Connection

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
    }

    @AfterEach()
    fun afterEach() {
        clearDatabase(connection, TEST_DATABASE)
    }

    @AfterAll
    fun afterAll() {
        mysqld.stop()
    }

    @Test
    fun `Set-update must fail for a new model`() {
        val emptyDatabaseRows = getDatabaseRows(connection, TEST_DATABASE)
        val update = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_address_book_1_set_update.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedErrors = listOf(
            "/address_book: unable to update",
            "/address_book/settings: unable to update",
            "/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]: unable to update",
            "/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/relation[16634916-8f83-4376-ad42-37038e108a0b]: unable to update",
            "/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/password: unable to update",
            "/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/secret: unable to update",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]: unable to update",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/relation[05ade278-4b44-43da-a0cc-14463854e397]: unable to update",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/password: unable to update",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/secret: unable to update",
            "/address_book/groups[ad9aaea8-30fe-45ed-93ef-bd368da0c756]: unable to update",
            "/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]: unable to update",
            "/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]: unable to update",
            "/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]/persons[546a4982-b39a-4d01-aeb3-22d60c6963c0]: unable to update",
            "/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]/persons[e391c509-67d6-4846-bfea-0f7cd9c91bf7]: unable to update",
            "/address_book/city_info[Albany,New York,United States of America]: unable to update",
            "/address_book/city_info[New York City,New York,United States of America]: unable to update",
            "/address_book/city_info[San Francisco,California,United States of America]: unable to update",
            "/address_book/city_info[Princeton,New Jersey,United States of America]: unable to update",
        )
        val actualErrors = set(update, setEntityDelegates, connection, clock = updateClock)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
        val actualRows = getDatabaseRows(connection, TEST_DATABASE)
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
            metaModel,
            "model/my_sql_address_book_1_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val createErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals("", createErrors.joinToString("\n"))
        val createdRows = getDatabaseRows(connection, TEST_DATABASE)
        assertNotEquals(expectedRows, createdRows)

        val update = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_address_book_1_set_update.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val updateErrors = set(update, setEntityDelegates, connection, clock = updateClock)
        assertEquals("", updateErrors.joinToString("\n"))
        val updatedRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(expectedRows, updatedRows)
    }

    @Test
    fun `Set-update must fail when updating an entity with existing keys but different entity path`() {
        val emptyDatabaseRows = getDatabaseRows(connection, TEST_DATABASE)
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
            getMainModelFromJsonString(metaModel, createJson, multiAuxDecodingStateMachineFactory = auxDecodingFactory)
        val createErrors = set(create, setEntityDelegates, connection, clock = createClock)
        assertEquals("", createErrors.joinToString("\n"))
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
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
        val updateErrorsExpected =
            listOf("/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/relation[05ade278-4b44-43da-a0cc-14463854e397]: unable to update")
        val update =
            getMainModelFromJsonString(metaModel, updateJson, multiAuxDecodingStateMachineFactory = auxDecodingFactory)
        val updateErrors = set(update, setEntityDelegates, connection, clock = updateClock)
        assertEquals(updateErrorsExpected.joinToString("\n"), updateErrors.joinToString("\n"))
        val afterUpdateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(afterCreateRows, afterUpdateRows)
    }
}