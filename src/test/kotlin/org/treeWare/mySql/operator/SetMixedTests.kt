package org.treeWare.mySql.operator

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.treeWare.metaModel.mySqlAddressBookMetaModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
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
class SetMixedTests {
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
    fun `set() must succeed when updating an association to point to an entity being created`() {
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
            getMainModelFromJsonString(
                mySqlAddressBookMetaModel,
                createJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )
        val expectedCreateResponse = SetResponse.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRowsExpected = """
            |= Table main__address_book_person =
            |
            |* Row 1 *
            |created_on_: 2022-03-03 00:30:31.330
            |updated_on_: 2022-03-03 00:30:31.330
            |field_path_: /address_book/person
            |id: a8aacf55-7810-4b43-afe5-4344f25435fd
            |first_name: Lois
            |last_name: Lane
            |hero_name: null
            |email: null
            |picture: null
            |self: null
            |self__id: null
            |main__address_book_root__singleton_key_: 0
            |main__person_group__id: null
            |
            |* Row 2 *
            |created_on_: 2022-03-03 00:30:31.330
            |updated_on_: 2022-03-03 00:30:31.330
            |field_path_: /address_book/person
            |id: cc477201-48ec-4367-83a4-7fdbd92f8a6f
            |first_name: Clark
            |last_name: Kent
            |hero_name: null
            |email: null
            |picture: null
            |self: null
            |self__id: null
            |main__address_book_root__singleton_key_: 0
            |main__person_group__id: null
            |
            |= Table main__address_book_relation =
            |
            |* Row 1 *
            |created_on_: 2022-03-03 00:30:31.330
            |updated_on_: 2022-03-03 00:30:31.330
            |field_path_: /address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation
            |id: 05ade278-4b44-43da-a0cc-14463854e397
            |relationship: 7
            |person: {"person":[{"id":"a8aacf55-7810-4b43-afe5-4344f25435fd"}]}
            |person__id: a8aacf55-7810-4b43-afe5-4344f25435fd
            |main__address_book_person__id: cc477201-48ec-4367-83a4-7fdbd92f8a6f
            |main__person_group__id: null
            |
        """.trimMargin()
        val afterCreateRows =
            getTableRows(testDataSource, TEST_DATABASE, "main__address_book_person", "main__address_book_relation")
        assertEquals(afterCreateRowsExpected, afterCreateRows)

        val mixedJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "relation": [
            |          {
            |            "set_": "update",
            |            "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |            "person": {
            |              "person": [
            |                {
            |                  "id": "ec983c56-320f-4d66-9dde-f180e8ac3807"
            |                }
            |              ]
            |            }
            |          }
            |        ]
            |      },
            |      {
            |        "set_": "create",
            |        "id": "ec983c56-320f-4d66-9dde-f180e8ac3807",
            |        "first_name": "Jimmy",
            |        "last_name": "Olsen"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val mixed =
            getMainModelFromJsonString(
                mySqlAddressBookMetaModel,
                mixedJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )
        val expectedMixedResponse = SetResponse.Success
        val actualMixedResponse = set(mixed, setEntityDelegates, testDataSource, clock = updateClock)
        assertSetResponse(expectedMixedResponse, actualMixedResponse)
        val afterMixedRowsExpected = """
            |= Table main__address_book_person =
            |
            |* Row 1 *
            |created_on_: 2022-03-03 00:30:31.330
            |updated_on_: 2022-03-03 00:30:31.330
            |field_path_: /address_book/person
            |id: a8aacf55-7810-4b43-afe5-4344f25435fd
            |first_name: Lois
            |last_name: Lane
            |hero_name: null
            |email: null
            |picture: null
            |self: null
            |self__id: null
            |main__address_book_root__singleton_key_: 0
            |main__person_group__id: null
            |
            |* Row 2 *
            |created_on_: 2022-03-03 00:30:31.330
            |updated_on_: 2022-03-03 00:30:31.330
            |field_path_: /address_book/person
            |id: cc477201-48ec-4367-83a4-7fdbd92f8a6f
            |first_name: Clark
            |last_name: Kent
            |hero_name: null
            |email: null
            |picture: null
            |self: null
            |self__id: null
            |main__address_book_root__singleton_key_: 0
            |main__person_group__id: null
            |
            |* Row 3 *
            |created_on_: 2022-04-04 00:40:41.440
            |updated_on_: 2022-04-04 00:40:41.440
            |field_path_: /address_book/person
            |id: ec983c56-320f-4d66-9dde-f180e8ac3807
            |first_name: Jimmy
            |last_name: Olsen
            |hero_name: null
            |email: null
            |picture: null
            |self: null
            |self__id: null
            |main__address_book_root__singleton_key_: 0
            |main__person_group__id: null
            |
            |= Table main__address_book_relation =
            |
            |* Row 1 *
            |created_on_: 2022-03-03 00:30:31.330
            |updated_on_: 2022-04-04 00:40:41.440
            |field_path_: /address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation
            |id: 05ade278-4b44-43da-a0cc-14463854e397
            |relationship: 7
            |person: {"person":[{"id":"ec983c56-320f-4d66-9dde-f180e8ac3807"}]}
            |person__id: ec983c56-320f-4d66-9dde-f180e8ac3807
            |main__address_book_person__id: cc477201-48ec-4367-83a4-7fdbd92f8a6f
            |main__person_group__id: null
            |
        """.trimMargin()
        val afterMixedRows =
            getTableRows(testDataSource, TEST_DATABASE, "main__address_book_person", "main__address_book_relation")
        assertEquals(afterMixedRowsExpected, afterMixedRows)
    }

    @Test
    fun `set() must fail when updating an association to point to an entity being deleted`() {
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
            |      },
            |      {
            |        "id": "ec983c56-320f-4d66-9dde-f180e8ac3807",
            |        "first_name": "Jimmy",
            |        "last_name": "Olsen"
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
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRowsExpected = """
            |= Table main__address_book_person =
            |
            |* Row 1 *
            |created_on_: 2022-03-03 00:30:31.330
            |updated_on_: 2022-03-03 00:30:31.330
            |field_path_: /address_book/person
            |id: a8aacf55-7810-4b43-afe5-4344f25435fd
            |first_name: Lois
            |last_name: Lane
            |hero_name: null
            |email: null
            |picture: null
            |self: null
            |self__id: null
            |main__address_book_root__singleton_key_: 0
            |main__person_group__id: null
            |
            |* Row 2 *
            |created_on_: 2022-03-03 00:30:31.330
            |updated_on_: 2022-03-03 00:30:31.330
            |field_path_: /address_book/person
            |id: cc477201-48ec-4367-83a4-7fdbd92f8a6f
            |first_name: Clark
            |last_name: Kent
            |hero_name: null
            |email: null
            |picture: null
            |self: null
            |self__id: null
            |main__address_book_root__singleton_key_: 0
            |main__person_group__id: null
            |
            |* Row 3 *
            |created_on_: 2022-03-03 00:30:31.330
            |updated_on_: 2022-03-03 00:30:31.330
            |field_path_: /address_book/person
            |id: ec983c56-320f-4d66-9dde-f180e8ac3807
            |first_name: Jimmy
            |last_name: Olsen
            |hero_name: null
            |email: null
            |picture: null
            |self: null
            |self__id: null
            |main__address_book_root__singleton_key_: 0
            |main__person_group__id: null
            |
            |= Table main__address_book_relation =
            |
            |* Row 1 *
            |created_on_: 2022-03-03 00:30:31.330
            |updated_on_: 2022-03-03 00:30:31.330
            |field_path_: /address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation
            |id: 05ade278-4b44-43da-a0cc-14463854e397
            |relationship: 7
            |person: {"person":[{"id":"a8aacf55-7810-4b43-afe5-4344f25435fd"}]}
            |person__id: a8aacf55-7810-4b43-afe5-4344f25435fd
            |main__address_book_person__id: cc477201-48ec-4367-83a4-7fdbd92f8a6f
            |main__person_group__id: null
            |
        """.trimMargin()
        val afterCreateRows =
            getTableRows(testDataSource, TEST_DATABASE, "main__address_book_person", "main__address_book_relation")
        assertEquals(afterCreateRowsExpected, afterCreateRows)

        val mixedJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "relation": [
            |          {
            |            "set_": "update",
            |            "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |            "person": {
            |              "person": [
            |                {
            |                  "id": "ec983c56-320f-4d66-9dde-f180e8ac3807"
            |                }
            |              ]
            |            }
            |          }
            |        ]
            |      },
            |      {
            |        "set_": "delete",
            |        "id": "ec983c56-320f-4d66-9dde-f180e8ac3807"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val expectedMixedResponse = SetResponse.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError(
                    "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation/05ade278-4b44-43da-a0cc-14463854e397",
                    "unable to update: no parent or target entity"
                )
            )
        )
        val mixed =
            getMainModelFromJsonString(
                mySqlAddressBookMetaModel,
                mixedJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )
        val actualMixedResponse = set(mixed, setEntityDelegates, testDataSource, clock = updateClock)
        assertSetResponse(expectedMixedResponse, actualMixedResponse)
        val afterMixedRows =
            getTableRows(testDataSource, TEST_DATABASE, "main__address_book_person", "main__address_book_relation")
        assertEquals(afterCreateRowsExpected, afterMixedRows)
    }

    @Test
    fun `set() must fail when creating an association to point to an entity being deleted`() {
        val createJson = """
            |{
            |  "address_book": {
            |    "set_": "create",
            |    "name": "Super Heroes",
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent"
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
        val expectedCreateResponse = SetResponse.Success
        val create =
            getMainModelFromJsonString(
                mySqlAddressBookMetaModel,
                createJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRowsExpected = """
            |= Table main__address_book_person =
            |
            |* Row 1 *
            |created_on_: 2022-03-03 00:30:31.330
            |updated_on_: 2022-03-03 00:30:31.330
            |field_path_: /address_book/person
            |id: a8aacf55-7810-4b43-afe5-4344f25435fd
            |first_name: Lois
            |last_name: Lane
            |hero_name: null
            |email: null
            |picture: null
            |self: null
            |self__id: null
            |main__address_book_root__singleton_key_: 0
            |main__person_group__id: null
            |
            |* Row 2 *
            |created_on_: 2022-03-03 00:30:31.330
            |updated_on_: 2022-03-03 00:30:31.330
            |field_path_: /address_book/person
            |id: cc477201-48ec-4367-83a4-7fdbd92f8a6f
            |first_name: Clark
            |last_name: Kent
            |hero_name: null
            |email: null
            |picture: null
            |self: null
            |self__id: null
            |main__address_book_root__singleton_key_: 0
            |main__person_group__id: null
            |
            |= Table main__address_book_relation =
            |
        """.trimMargin()
        val afterCreateRows =
            getTableRows(testDataSource, TEST_DATABASE, "main__address_book_person", "main__address_book_relation")
        assertEquals(afterCreateRowsExpected, afterCreateRows)

        val mixedJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "relation": [
            |          {
            |            "set_": "create",
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
            |        "set_": "delete",
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val expectedMixedResponse = SetResponse.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError(
                    "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation/05ade278-4b44-43da-a0cc-14463854e397",
                    "unable to create association in entity: no parent or target entity"
                )
            )
        )
        val mixed =
            getMainModelFromJsonString(
                mySqlAddressBookMetaModel,
                mixedJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )
        val actualMixedResponse = set(mixed, setEntityDelegates, testDataSource, clock = updateClock)
        assertSetResponse(expectedMixedResponse, actualMixedResponse)
        val afterMixedRows =
            getTableRows(testDataSource, TEST_DATABASE, "main__address_book_person", "main__address_book_relation")
        assertEquals(afterCreateRowsExpected, afterMixedRows)
    }

    @Test
    fun `set() must return multiple error messages for multiple errors in a set-request`() {
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
            |        "last_name": "Lane",
            |        "relation": [
            |          {
            |            "id": "16634916-8f83-4376-ad42-37038e108a0b",
            |            "relationship": "colleague",
            |            "person": {
            |              "person": [
            |                {
            |                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
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
            getMainModelFromJsonString(
                mySqlAddressBookMetaModel,
                createJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )
        val expectedCreateResponse = SetResponse.Success
        val actualCreateResponse = set(create, setEntityDelegates, testDataSource, clock = createClock)
        assertSetResponse(expectedCreateResponse, actualCreateResponse)
        val afterCreateRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertNotEquals(emptyDatabaseRows, afterCreateRows)

        val mixedJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "relation": [
            |          {
            |            "set_": "update",
            |            "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |            "person": {
            |              "person": [
            |                {
            |                  "id": "ec983c56-320f-4d66-9dde-f180e8ac3807"
            |                }
            |              ]
            |            }
            |          },
            |          {
            |            "set_": "create",
            |            "id": "3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce",
            |            "person": {
            |              "person": [
            |                {
            |                  "id": "ec983c56-320f-4d66-9dde-f180e8ac3807"
            |                }
            |              ]
            |            }
            |          }
            |        ]
            |      },
            |      {
            |        "set_": "delete",
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |        "first_name": "Lois",
            |        "last_name": "Lane"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val expectedMixedResponse = SetResponse.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError(
                    "/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd",
                    "unable to delete: has children or source entity"
                ),
                ElementModelError(
                    "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation/3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce",
                    "unable to create association in entity: no parent or target entity"
                ),
                ElementModelError(
                    "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation/05ade278-4b44-43da-a0cc-14463854e397",
                    "unable to update: no parent or target entity"
                ),
            )
        )
        val mixed =
            getMainModelFromJsonString(
                mySqlAddressBookMetaModel,
                mixedJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )
        val actualMixedResponse = set(mixed, setEntityDelegates, testDataSource, clock = updateClock)
        assertSetResponse(expectedMixedResponse, actualMixedResponse)
        val afterMixedRows = getDatabaseRows(testDataSource, TEST_DATABASE)
        assertEquals(afterCreateRows, afterMixedRows)
    }
}