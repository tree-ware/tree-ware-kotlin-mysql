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

private const val NOW = "2022-04-14T00:40:41.450Z"
private val clock = Clock.fixed(Instant.parse(NOW), ZoneOffset.UTC)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SetCreateTests {
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
    fun `Set-create must succeed for a new model`() {
        val create = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_address_book_1_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val setErrors = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals("", setErrors.joinToString("\n"))
        val expectedRows = readFile("operator/my_sql_address_book_1_set_create_results.txt")
        val actualRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(expectedRows, actualRows)
    }

    @Test
    fun `Set-create must succeed for a forward-referencing association`() {
        val create = getMainModelFromJsonFile(
            metaModel,
            "operator/forward_referencing_association_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val setErrors = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals("", setErrors.joinToString("\n"))
        val expectedRows = readFile("operator/forward_referencing_association_set_create_results.txt")
        val actualRows = getDatabaseRows(connection, TEST_DATABASE)
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
            |        "related_city_info": [],
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
        val create = getMainModelFromJsonString(
            metaModel,
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val setErrors = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals("", setErrors.joinToString("\n"))
        val expectedRows = """
            |= Table city${'$'}city_info =
            |
            |* Row 1 *
            |created_on${'$'}: 2022-04-14 00:40:41.450
            |updated_on${'$'}: 2022-04-14 00:40:41.450
            |entity_path${'$'}: /address_book/city_info[Princeton,New Jersey,United States of America]
            |name: Princeton
            |state: New Jersey
            |country: United States of America
            |info: null
            |latitude: null
            |longitude: null
            |city_center: null
            |related_city_info: []
            |self${'$'}name: Princeton
            |self${'$'}state: New Jersey
            |self${'$'}country: United States of America
            |self2${'$'}name: Princeton
            |self2${'$'}state: New Jersey
            |self2${'$'}country: United States of America
            |
        """.trimMargin()
        val actualRows = getTableRows(connection, TEST_DATABASE, "city\$city_info")
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
        val create = getMainModelFromJsonString(
            metaModel,
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val setErrors = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals("", setErrors.joinToString("\n"))
        val expectedRows = """
            |= Table main${'$'}address_book_root =
            |
            |* Row 1 *
            |created_on${'$'}: 2022-04-14 00:40:41.450
            |updated_on${'$'}: 2022-04-14 00:40:41.450
            |entity_path${'$'}: /address_book
            |singleton_key${'$'}: 0
            |name: null
            |last_updated: null
            |
        """.trimMargin()
        val actualRows = getTableRows(connection, TEST_DATABASE, "main\$address_book_root")
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
        val create = getMainModelFromJsonString(
            metaModel,
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val setErrors = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals("", setErrors.joinToString("\n"))
        val expectedRows = """
            |= Table main${'$'}address_book_root =
            |
            |* Row 1 *
            |created_on${'$'}: 2022-04-14 00:40:41.450
            |updated_on${'$'}: 2022-04-14 00:40:41.450
            |entity_path${'$'}: /address_book
            |singleton_key${'$'}: 0
            |name: null
            |last_updated: null
            |
            |= Table main${'$'}address_book_settings =
            |
            |* Row 1 *
            |created_on${'$'}: 2022-04-14 00:40:41.450
            |updated_on${'$'}: 2022-04-14 00:40:41.450
            |entity_path${'$'}: /address_book/settings
            |main${'$'}address_book_root${'$'}singleton_key${'$'}: 0
            |last_name_first: 1
            |encrypt_hero_name: null
            |card_colors: null
            |
        """.trimMargin()
        val actualRows =
            getTableRows(connection, TEST_DATABASE, "main\$address_book_root", "main\$address_book_settings")
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
        val create = getMainModelFromJsonString(
            metaModel,
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val setErrors = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals("", setErrors.joinToString("\n"))
        val expectedRows = """
            |= Table main${'$'}address_book_root =
            |
            |* Row 1 *
            |created_on${'$'}: 2022-04-14 00:40:41.450
            |updated_on${'$'}: 2022-04-14 00:40:41.450
            |entity_path${'$'}: /address_book
            |singleton_key${'$'}: 0
            |name: null
            |last_updated: null
            |
            |= Table main${'$'}address_book_settings =
            |
            |* Row 1 *
            |created_on${'$'}: 2022-04-14 00:40:41.450
            |updated_on${'$'}: 2022-04-14 00:40:41.450
            |entity_path${'$'}: /address_book/settings
            |main${'$'}address_book_root${'$'}singleton_key${'$'}: 0
            |last_name_first: null
            |encrypt_hero_name: null
            |card_colors: null
            |
        """.trimMargin()
        val actualRows =
            getTableRows(connection, TEST_DATABASE, "main\$address_book_root", "main\$address_book_settings")
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
        val create = getMainModelFromJsonString(
            metaModel,
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val setErrors = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals("", setErrors.joinToString("\n"))
        val expectedRows = """
            |= Table main${'$'}address_book_person =
            |
            |* Row 1 *
            |created_on${'$'}: 2022-04-14 00:40:41.450
            |updated_on${'$'}: 2022-04-14 00:40:41.450
            |entity_path${'$'}: /address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]
            |main${'$'}person_group${'$'}id: null
            |id: a8aacf55-7810-4b43-afe5-4344f25435fd
            |first_name: null
            |last_name: null
            |hero_name: null
            |email: null
            |picture: null
            |self${'$'}id: null
            |
        """.trimMargin()
        val actualRows = getTableRows(connection, TEST_DATABASE, "main\$address_book_person")
        assertEquals(expectedRows, actualRows)
    }

    @Test
    fun `Set-create must fail for an old model`() {
        val create = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_address_book_1_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val expectedRows = readFile("operator/my_sql_address_book_1_set_create_results.txt")

        // Create the model the first time.
        val setErrors1 = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals("", setErrors1.joinToString("\n"))
        val actualRows1 = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(expectedRows, actualRows1)

        // Try to create the same model again. It should fail.
        val setErrors2 = set(create, setEntityDelegates, connection, clock = clock)
        val expectedErrors2 = listOf(
            "/address_book: unable to create: duplicate",
            "/address_book/settings: unable to create: duplicate",
            "/address_book/settings/advanced: unable to create: duplicate",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]: unable to create: duplicate",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/relation[05ade278-4b44-43da-a0cc-14463854e397]: unable to create: duplicate",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/password: unable to create: duplicate",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/secret: unable to create: duplicate",
            "/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]: unable to create: duplicate",
            "/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/relation[16634916-8f83-4376-ad42-37038e108a0b]: unable to create: duplicate",
            "/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/password: unable to create: duplicate",
            "/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/secret: unable to create: duplicate",
            "/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]: unable to create: duplicate",
            "/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]: unable to create: duplicate",
            "/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]/persons[546a4982-b39a-4d01-aeb3-22d60c6963c0]: unable to create: duplicate",
            "/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]/persons[e391c509-67d6-4846-bfea-0f7cd9c91bf7]: unable to create: duplicate",
            "/address_book/groups[ad9aaea8-30fe-45ed-93ef-bd368da0c756]: unable to create: duplicate",
            "/address_book/city_info[New York City,New York,United States of America]: unable to create: duplicate",
            "/address_book/city_info[Albany,New York,United States of America]: unable to create: duplicate",
            "/address_book/city_info[Princeton,New Jersey,United States of America]: unable to create: duplicate",
            "/address_book/city_info[San Francisco,California,United States of America]: unable to create: duplicate",
        )
        assertEquals(expectedErrors2.joinToString("\n"), setErrors2.joinToString("\n"))
        val actualRows2 = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(expectedRows, actualRows2)
    }

    @Test
    fun `Set-create must fail when creating a child without a parent`() {
        val emptyDatabaseRows = getDatabaseRows(connection, TEST_DATABASE)
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
        val expectedErrors =
            listOf("/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/relation[05ade278-4b44-43da-a0cc-14463854e397]: unable to create: no parent or target entity")
        val create =
            getMainModelFromJsonString(metaModel, modelJson, multiAuxDecodingStateMachineFactory = auxDecodingFactory)
        val setErrors = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals(expectedErrors.joinToString("\n"), setErrors.joinToString("\n"))
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterCreateRows)
    }

    @Test
    fun `Set-create must fail when creating an entity with existing keys but different entity path`() {
        val emptyDatabaseRows = getDatabaseRows(connection, TEST_DATABASE)
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
        val create1 =
            getMainModelFromJsonString(metaModel, create1Json, multiAuxDecodingStateMachineFactory = auxDecodingFactory)
        val create1Errors = set(create1, setEntityDelegates, connection, clock = clock)
        assertEquals("", create1Errors.joinToString("\n"))
        val afterCreate1Rows = getDatabaseRows(connection, TEST_DATABASE)
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
        val create2ErrorsExpected =
            listOf("/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/relation[05ade278-4b44-43da-a0cc-14463854e397]: unable to create: duplicate")
        val create2 =
            getMainModelFromJsonString(metaModel, create2Json, multiAuxDecodingStateMachineFactory = auxDecodingFactory)
        val create2Errors = set(create2, setEntityDelegates, connection, clock = clock)
        assertEquals(create2ErrorsExpected.joinToString("\n"), create2Errors.joinToString("\n"))
        val afterCreate2Rows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(afterCreate1Rows, afterCreate2Rows)
    }
}