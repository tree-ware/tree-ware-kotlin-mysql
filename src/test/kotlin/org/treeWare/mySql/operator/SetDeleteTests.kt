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
import org.treeWare.mySql.test.metaModel.mySqlAddressBookMetaModel
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

private const val NOW = "2022-04-14T00:40:41.450Z"
private val clock = Clock.fixed(Instant.parse(NOW), ZoneOffset.UTC)

@Tag("integrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SetDeleteTests {
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


    @Test
    fun `Set-delete must fail if entities to be deleted have children in the database`() {
        // Test strategy:
        // 1) create a model using the model in `SetCreateTests`. Verify that the model is in the DB.
        // 2) attempt to delete an entity from the above model that has child entities.
        // 3) the deletion attempt should fail and nothing should be deleted from the database.

        // 1) create a model using the model in `SetCreateTests`.
        val create = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_address_book_1_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedCreateResponse = Response.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val expectedRows = readFile("operator/my_sql_address_book_1_set_create_results.txt")
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(expectedRows, afterCreateRows)

        // 2) attempt to delete an entity from the above model that has child entities.
        val deleteJson = """
            |{
            |  "person": [
            |    {
            |      "set_": "delete",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
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
        val actualDeleteResponse = set(delete, setEntityDelegates, testDataSource, clock = clock)

        // 3) the deletion attempt should fail and nothing should be deleted from the database.
        val expectedDeleteResponse = Response.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError(
                    "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "unable to delete: has children or source entity"
                )
            )
        )
        assertSetResponse(expectedDeleteResponse, actualDeleteResponse)
        val afterDeleteRows = getDatabaseRows(testDataSource, TEST_DATABASE)
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
            |  "set_": "create",
            |  "name": "Super Heroes",
            |  "person": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "relation": [
            |        {
            |          "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |          "relationship": "colleague",
            |          "person": {
            |            "person": [
            |              {
            |                "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |              }
            |            ]
            |          }
            |        }
            |      ]
            |    },
            |    {
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |      "first_name": "Lois",
            |      "last_name": "Lane"
            |    }
            |  ]
            |}
        """.trimMargin()
        val create = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonStringIntoEntity(
            createJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedCreateResponse = Response.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        // 2) attempt to delete the target of the association.
        val deleteJson = """
            |{
            |  "person": [
            |    {
            |      "set_": "delete",
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
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
        val actualDeleteResponse = set(delete, setEntityDelegates, testDataSource, clock = clock)

        // 3) the deletion attempt should fail and nothing should be deleted from the database.
        val expectedDeleteResponse = Response.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError(
                    "/person/a8aacf55-7810-4b43-afe5-4344f25435fd",
                    "unable to delete: has children or source entity"
                )
            )
        )
        assertSetResponse(expectedDeleteResponse, actualDeleteResponse)
        val afterDeleteRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(afterCreateRows, afterDeleteRows)
    }

    @Test
    fun `Set-delete must succeed if entities to be deleted do not have children in the database`() {
        // Test strategy:
        // 1) create a model using the model in `SetCreateTests`. Verify that the model is in the DB.
        // 2) issue a delete request with all entities marked for deletion.
        // 3) since set() issues delete commands in reverse order, it should delete the entire model bottoms-up.

        // 1) create a model using the model in `SetCreateTests`.
        val create = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_address_book_1_set_create.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedCreateResponse = Response.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val expectedRows = readFile("operator/my_sql_address_book_1_set_create_results.txt")
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(expectedRows, afterCreateRows)

        // 2) issue a delete request with all entities marked for deletion.
        val delete = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_address_book_1_set_delete_bottoms_up.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = delete
        )
        val actualDeleteResponse = set(delete, setEntityDelegates, testDataSource, clock = clock)

        // 3) since set() issues delete commands in reverse order, it should delete the entire model bottoms-up.
        val expectedDeleteResponse = Response.Success
        assertSetResponse(expectedDeleteResponse, actualDeleteResponse)
        val afterDeleteRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterDeleteRows)
    }

    @Test
    fun `Set-delete must succeed for entities with associations to entities later in the request`() {
        // 1) create a model with an association to a later entity
        val createJson = """
            |{
            |  "set_": "create",
            |  "name": "Super Heroes",
            |  "person": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "relation": [
            |        {
            |          "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |          "relationship": "colleague",
            |          "person": {
            |            "person": [
            |              {
            |                "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |              }
            |            ]
            |          }
            |        }
            |      ]
            |    },
            |    {
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |      "first_name": "Lois",
            |      "last_name": "Lane"
            |    }
            |  ]
            |}
        """.trimMargin()
        val create = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonStringIntoEntity(
            createJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedCreateResponse = Response.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        // 2) delete all entities with the target of the association later in the request.
        val deleteJson = """
            |{
            |  "set_": "delete",
            |  "person": [
            |    {
            |      "set_": "delete",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "relation": [
            |        {
            |          "set_": "delete",
            |          "id": "05ade278-4b44-43da-a0cc-14463854e397"
            |        }
            |      ]
            |    },
            |    {
            |      "set_": "delete",
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
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
        val actualDeleteResponse = set(delete, setEntityDelegates, testDataSource, clock = clock)
        val expectedDeleteResponse = Response.Success
        assertSetResponse(expectedDeleteResponse, actualDeleteResponse)
        val afterDeleteRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterDeleteRows)
    }

    @Test
    fun `Set-delete must succeed for entities with associations to entities earlier in the request`() {
        // 1) create a model with an association to an earlier entity
        val createJson = """
            |{
            |  "set_": "create",
            |  "name": "Super Heroes",
            |  "person": [
            |    {
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |      "first_name": "Lois",
            |      "last_name": "Lane"
            |    },
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "relation": [
            |        {
            |          "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |          "relationship": "colleague",
            |          "person": {
            |            "person": [
            |              {
            |                "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |              }
            |            ]
            |          }
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val create = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonStringIntoEntity(
            createJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedCreateResponse = Response.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        // 2) delete all entities with the target of the association earlier in the request.
        val deleteJson = """
            |{
            |  "set_": "delete",
            |  "person": [
            |    {
            |      "set_": "delete",
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |    },
            |    {
            |      "set_": "delete",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "relation": [
            |        {
            |          "set_": "delete",
            |          "id": "05ade278-4b44-43da-a0cc-14463854e397"
            |        }
            |      ]
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
        val actualDeleteResponse = set(delete, setEntityDelegates, testDataSource, clock = clock)
        val expectedDeleteResponse = Response.Success
        assertSetResponse(expectedDeleteResponse, actualDeleteResponse)
        val afterDeleteRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(emptyDatabaseRows, afterDeleteRows)
    }

    @Test
    fun `Set-delete must succeed for entities that are non-existent or already deleted`() {
        val delete = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonFileIntoEntity(
            "model/my_sql_address_book_1_set_delete_bottoms_up.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = delete
        )
        val expectedDeleteResponse = Response.Success
        val actualDeleteResponse = set(delete, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedDeleteResponse, actualDeleteResponse)
    }

    @Test
    fun `Set-delete must fail when deleting an entity with existing keys but different entity path`() {
        val emptyDatabaseRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        val createJson = """
            |{
            |  "set_": "create",
            |  "person": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "relation": [
            |        {
            |          "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |          "relationship": "colleague"
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val create = MutableEntityModel(mySqlAddressBookRootEntityMeta, null)
        decodeJsonStringIntoEntity(
            createJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = create
        )
        val expectedCreateResponse = Response.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = clock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        // Attempt to delete the relation entity but under a different person entity.
        val deleteJson = """
            |{
            |  "person": [
            |    {
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |      "first_name": "Lois",
            |      "last_name": "Lane",
            |      "relation": [
            |        {
            |          "set_": "delete",
            |          "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |          "relationship": "colleague"
            |        }
            |      ]
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
        val actualDeleteResponse = set(delete, setEntityDelegates, testDataSource, clock = clock)
        // NOTE: delete operations never return errors, not even when the entity to be deleted is not found.
        // While no error is returned, the delete operation should fail by not updating the database.
        val expectedDeleteResponse = Response.Success
        assertSetResponse(expectedDeleteResponse, actualDeleteResponse)
        val afterDeleteRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(afterCreateRows, afterDeleteRows)
    }
}