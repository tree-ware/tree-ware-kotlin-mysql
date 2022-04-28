package org.treeWare.mySql.operator

import com.wix.mysql.EmbeddedMysql
import com.wix.mysql.config.MysqldConfig
import com.wix.mysql.distribution.Version
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
import org.treeWare.mySql.test.getAvailableServerPort
import org.treeWare.mySql.test.getDatabaseRows
import java.sql.Connection
import java.sql.DriverManager
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

private const val TEST_DATABASE = "test\$address_book"

private val metaModel = newMySqlAddressBookMetaModel("test", null, null).metaModel
    ?: throw IllegalStateException("Meta-model has validation errors")
private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

private const val NOW = "2022-04-14T00:40:41.450Z"
private val clock = Clock.fixed(Instant.parse(NOW), ZoneOffset.UTC)

class SetDeleteTests {
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

    @AfterTest
    fun afterTest() {
        mysqld.stop()
    }

    @Test
    fun `Set-delete must fail if entities to be deleted have children without set_ aux in the request`() {
        val deleteJson = """
            |{
            |  "address_book": {
            |    "set_": "delete",
            |    "name": "Super Heroes",
            |    "last_updated": "1587147731",
            |    "settings": {
            |      "last_name_first": true,
            |      "encrypt_hero_name": false
            |    },
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent"
            |      },
            |      {
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val delete =
            getMainModelFromJsonString(metaModel, deleteJson, multiAuxDecodingStateMachineFactory = auxDecodingFactory)
        val expectedErrors = listOf(
            "/address_book/settings: entity without `delete` must not be in the subtree of a `delete`",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]: entity without `delete` must not be in the subtree of a `delete`",
            "/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]: entity without `delete` must not be in the subtree of a `delete`"
        )
        val deleteErrors = set(delete, setEntityDelegates, connection, clock = clock)
        assertEquals(expectedErrors.joinToString("\n"), deleteErrors.joinToString("\n"))
    }

    @Test
    fun `Set-delete must fail if entities to be deleted have children in the database`() {
        // Test strategy:
        // 1) create a model using the model in `SetCreateTests`. Verify that the model is in the DB.
        // 2) attempt to delete an entity from the above model that has child entities.
        // 3) the deletion attempt should fail and nothing should be deleted from the database.

        // 1) create a model using the model in `SetCreateTests`.
        val create = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_address_book_1_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val setErrors = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals("", setErrors.joinToString("\n"))
        val expectedRows = readFile("operator/my_sql_address_book_1_set_create_results.txt")
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(expectedRows, afterCreateRows)

        // 2) attempt to delete an entity from the above model that has child entities.
        val deleteJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "set_": "delete",
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val delete =
            getMainModelFromJsonString(metaModel, deleteJson, multiAuxDecodingStateMachineFactory = auxDecodingFactory)
        val deleteErrors = set(delete, setEntityDelegates, connection, clock = clock)

        // 3) the deletion attempt should fail and nothing should be deleted from the database.
        val expectedErrors =
            listOf("/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]: unable to delete: has children or source entity")
        assertEquals(expectedErrors.joinToString("\n"), deleteErrors.joinToString("\n"))
        val afterDeleteRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(afterCreateRows, afterDeleteRows)
    }

    @Test
    fun `Set-delete must fail if entities to be deleted are targets of associations in the database`() {
        // Test strategy:
        // 1) create a model with an association.
        // 2) attempt to delete the target of the association.
        // 3) the deletion attempt should fail and nothing should be deleted from the database.

        // 1) create a model with an association.
        val createJson = """
            |{
            |  "address_book": {
            |    "set_": "create",
            |    "name": "Super Heroes",
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "relation": [
            |          {
            |            "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |            "relationship": "colleague",
            |            "person": {
            |              "person": [
            |                {
            |                  "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |                }
            |              ]
            |            }
            |          }
            |        ]
            |      },
            |      {
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |        "first_name": "Lois",
            |        "last_name": "Lane"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val create =
            getMainModelFromJsonString(metaModel, createJson, multiAuxDecodingStateMachineFactory = auxDecodingFactory)
        val createErrors = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals("", createErrors.joinToString("\n"))
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        // 2) attempt to delete the target of the association.
        val deleteJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "set_": "delete",
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val delete =
            getMainModelFromJsonString(metaModel, deleteJson, multiAuxDecodingStateMachineFactory = auxDecodingFactory)
        val deleteErrors = set(delete, setEntityDelegates, connection, clock = clock)

        // 3) the deletion attempt should fail and nothing should be deleted from the database.
        val expectedErrors =
            listOf("/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]: unable to delete: has children or source entity")
        assertEquals(expectedErrors.joinToString("\n"), deleteErrors.joinToString("\n"))
        assertEquals(expectedErrors.joinToString("\n"), deleteErrors.joinToString("\n"))
        val afterDeleteRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(afterCreateRows, afterDeleteRows)
    }

    @Test
    fun `Set-delete must succeed if entities to be deleted do not have children in the database`() {
        // Test strategy:
        // 1) create a model using the model in `SetCreateTests`. Verify that the model is in the DB.
        // 2) issue a delete request with all entities marked for deletion.
        // 3) since set() issues delete commands in reverse order, it should delete the entire model bottoms-up.

        // 1) create a model using the model in `SetCreateTests`.
        val create = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_address_book_1_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val setErrors = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals("", setErrors.joinToString("\n"))
        val expectedRows = readFile("operator/my_sql_address_book_1_set_create_results.txt")
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(expectedRows, afterCreateRows)

        // 2) issue a delete request with all entities marked for deletion.
        val delete = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_address_book_1_set_delete_bottoms_up.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val deleteErrors = set(delete, setEntityDelegates, connection, clock = clock)

        // 3) since set() issues delete commands in reverse order, it should delete the entire model bottoms-up.
        assertEquals("", deleteErrors.joinToString("\n"))
        val afterDeleteRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterDeleteRows)
    }

    @Test
    fun `Set-delete must succeed for entities with associations to entities later in the request`() {
        // 1) create a model with an association to a later entity
        val createJson = """
            |{
            |  "address_book": {
            |    "set_": "create",
            |    "name": "Super Heroes",
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "relation": [
            |          {
            |            "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |            "relationship": "colleague",
            |            "person": {
            |              "person": [
            |                {
            |                  "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |                }
            |              ]
            |            }
            |          }
            |        ]
            |      },
            |      {
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |        "first_name": "Lois",
            |        "last_name": "Lane"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val create =
            getMainModelFromJsonString(metaModel, createJson, multiAuxDecodingStateMachineFactory = auxDecodingFactory)
        val createErrors = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals("", createErrors.joinToString("\n"))
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        // 2) delete all entities with the target of the association later in the request.
        val deleteJson = """
            |{
            |  "address_book": {
            |    "set_": "delete",
            |    "person": [
            |      {
            |        "set_": "delete",
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "relation": [
            |          {
            |            "set_": "delete",
            |            "id": "05ade278-4b44-43da-a0cc-14463854e397"
            |          }
            |        ]
            |      },
            |      {
            |        "set_": "delete",
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val delete =
            getMainModelFromJsonString(metaModel, deleteJson, multiAuxDecodingStateMachineFactory = auxDecodingFactory)
        val deleteErrors = set(delete, setEntityDelegates, connection, clock = clock)
        assertEquals("", deleteErrors.joinToString("\n"))
        val afterDeleteRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterDeleteRows)
    }

    @Test
    fun `Set-delete must succeed for entities with associations to entities earlier in the request`() {
        // 1) create a model with an association to an earlier entity
        val createJson = """
            |{
            |  "address_book": {
            |    "set_": "create",
            |    "name": "Super Heroes",
            |    "person": [
            |      {
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |        "first_name": "Lois",
            |        "last_name": "Lane"
            |      },
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "relation": [
            |          {
            |            "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |            "relationship": "colleague",
            |            "person": {
            |              "person": [
            |                {
            |                  "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |                }
            |              ]
            |            }
            |          }
            |        ]
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val create =
            getMainModelFromJsonString(metaModel, createJson, multiAuxDecodingStateMachineFactory = auxDecodingFactory)
        val createErrors = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals("", createErrors.joinToString("\n"))
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        // 2) delete all entities with the target of the association earlier in the request.
        val deleteJson = """
            |{
            |  "address_book": {
            |    "set_": "delete",
            |    "person": [
            |      {
            |        "set_": "delete",
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |      },
            |      {
            |        "set_": "delete",
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "relation": [
            |          {
            |            "set_": "delete",
            |            "id": "05ade278-4b44-43da-a0cc-14463854e397"
            |          }
            |        ]
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val delete =
            getMainModelFromJsonString(metaModel, deleteJson, multiAuxDecodingStateMachineFactory = auxDecodingFactory)
        val deleteErrors = set(delete, setEntityDelegates, connection, clock = clock)
        assertEquals("", deleteErrors.joinToString("\n"))
        val afterDeleteRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterDeleteRows)
    }

    @Test
    fun `Set-delete must succeed for entities that are non-existent or already deleted`() {
        val delete = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_address_book_1_set_delete_bottoms_up.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val deleteErrors = set(delete, setEntityDelegates, connection, clock = clock)
        assertEquals("", deleteErrors.joinToString("\n"))
    }

    @Test
    fun `Set-delete must fail when deleting an entity with existing keys but different entity path`() {
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
        val createErrors = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals("", createErrors.joinToString("\n"))
        val afterCreateRows = getDatabaseRows(connection, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        // Attempt to delete the relation entity but under a different person entity.
        val deleteJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |        "first_name": "Lois",
            |        "last_name": "Lane",
            |        "relation": [
            |          {
            |            "set_": "delete",
            |            "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |            "relationship": "colleague"
            |          }
            |        ]
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val delete =
            getMainModelFromJsonString(metaModel, deleteJson, multiAuxDecodingStateMachineFactory = auxDecodingFactory)
        val deleteErrors = set(delete, setEntityDelegates, connection, clock = clock)
        // NOTE: delete operations never return errors, not even when the entity to be deleted is not found.
        // While no error is returned, the delete operation should fail by not updating the database.
        assertEquals("", deleteErrors.joinToString("\n"))
        val afterDeleteRows = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(afterCreateRows, afterDeleteRows)
    }
}