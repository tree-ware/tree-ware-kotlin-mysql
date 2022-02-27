package org.treeWare.mySql.operator

import com.wix.mysql.EmbeddedMysql
import com.wix.mysql.config.MysqldConfig
import com.wix.mysql.distribution.Version
import org.treeWare.metaModel.newMySqlAddressBookMetaModel
import org.treeWare.model.getMainModelFromJsonFile
import org.treeWare.model.getMainModelFromJsonString
import org.treeWare.model.readFile
import org.treeWare.mySql.test.clearDatabase
import org.treeWare.mySql.test.getAvailableServerPort
import org.treeWare.mySql.test.getDatabaseRows
import org.treeWare.mySql.test.getTableRows
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.*

private const val TEST_DATABASE = "test__address_book"

class SetTests {
    private val metaModel = newMySqlAddressBookMetaModel("test", null, null).metaModel
        ?: throw IllegalStateException("Meta-model has validation errors")

    private val port = getAvailableServerPort()

    private val mysqld: EmbeddedMysql
    private val connection: Connection

    init {
        val config = MysqldConfig.aMysqldConfig(Version.v8_0_17)
            .withPort(port)
            .withServerVariable("mysqlx", 0) // disable the X plugin
            .build()
        mysqld = EmbeddedMysql.anEmbeddedMysql(config).start()
        connection = DriverManager.getConnection("jdbc:mysql://localhost:$port/", "root", "")
    }

    @AfterTest
    fun afterTest() {
        mysqld.stop()
    }

    @Test
    fun `Rows must be created`() {
        createDatabase(metaModel, connection)

        val model = getMainModelFromJsonFile(metaModel, "model/my_sql_address_book_1.json")
        set(model, connection)
        val expected = readFile("operator/my_sql_address_book_1_set_results.txt")
        val actual = getDatabaseRows(connection, TEST_DATABASE)
        assertEquals(expected, actual)
    }

    @Test
    fun `Rows must be updated`() {
        // Test strategy: set address_book_2.json and then address_book_3.json.
        // The result should be the same as taking the union of these 2 models.
        // So read the rows, clear the DB, and set address_book_union_2_3.json.
        // Read the rows and compare to the previous rows read. They should be
        // the same.

        // Like in UnionTests, ensure all the model files are different so that
        // this test is not trivial.
        val jsonModel1 = readFile("model/my_sql_address_book_2.json")
        val jsonModel2 = readFile("model/my_sql_address_book_3.json")
        val jsonModelUnion = readFile("model/my_sql_address_book_union_2_3.json")
        assertNotEquals(jsonModel1, jsonModel2)
        assertNotEquals(jsonModel1, jsonModelUnion)
        assertNotEquals(jsonModel2, jsonModelUnion)

        createDatabase(metaModel, connection)

        // Set both models in sequence.

        val model1 = getMainModelFromJsonString(metaModel, jsonModel1)
        set(model1, connection)
        val model1Rows = getDatabaseRows(connection, TEST_DATABASE)

        val model2 = getMainModelFromJsonString(metaModel, jsonModel2)
        set(model2, connection)
        val model2Rows = getDatabaseRows(connection, TEST_DATABASE)
        assertNotEquals(model1Rows, model2Rows)

        // Clear the database.

        clearDatabase(connection, TEST_DATABASE)
        val noRows = getDatabaseRows(connection, TEST_DATABASE)
        assertFalse(noRows.contains("* Row"))

        // Set the union model and get the expected rows.

        val modelUnion = getMainModelFromJsonString(metaModel, jsonModelUnion)
        set(modelUnion, connection)
        val expectedRows = getDatabaseRows(connection, TEST_DATABASE)

        assertEquals(expectedRows, model2Rows)
    }

    @Test
    fun `Rows must be created when no fields are specified in the model root`() {
        createDatabase(metaModel, connection)

        val modelJson = """
            |{
            |  "address_book": {
            |  }
            |}
        """.trimMargin()
        val model = getMainModelFromJsonString(metaModel, modelJson)
        set(model, connection)

        val expected = """
            |= Table main__address_book_root =
            |
            |* Row 1 *
            |parent_id${'$'}: []
            |name: null
            |last_updated: null
            |
        """.trimMargin()
        val actual = getTableRows(connection, TEST_DATABASE, "main__address_book_root")
        assertEquals(expected, actual)
    }

    @Test
    fun `Rows must be created when only composition fields are specified in the model root`() {
        createDatabase(metaModel, connection)

        val modelJson = """
            |{
            |  "address_book": {
            |    "settings": {
            |      "last_name_first": true
            |    }
            |  }
            |}
        """.trimMargin()
        val model = getMainModelFromJsonString(metaModel, modelJson)
        set(model, connection)

        val expected = """
            |= Table main__address_book_root =
            |
            |* Row 1 *
            |parent_id${'$'}: []
            |name: null
            |last_updated: null
            |
            |= Table main__address_book_settings =
            |
            |* Row 1 *
            |parent_id${'$'}: ["settings"]
            |last_name_first: 1
            |encrypt_hero_name: null
            |card_colors: null
            |
        """.trimMargin()
        val actual = getTableRows(connection, TEST_DATABASE, "main__address_book_root", "main__address_book_settings")
        assertEquals(expected, actual)
    }

    @Test
    fun `Rows must be created when no fields are specified in a single-composition entity`() {
        createDatabase(metaModel, connection)

        val modelJson = """
            |{
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "settings": {
            |    }
            |  }
            |}
        """.trimMargin()
        val model = getMainModelFromJsonString(metaModel, modelJson)
        set(model, connection)

        val expected = """
            |= Table main__address_book_root =
            |
            |* Row 1 *
            |parent_id${'$'}: []
            |name: Super Heroes
            |last_updated: null
            |
            |= Table main__address_book_settings =
            |
            |* Row 1 *
            |parent_id${'$'}: ["settings"]
            |last_name_first: null
            |encrypt_hero_name: null
            |card_colors: null
            |
        """.trimMargin()
        val actual = getTableRows(connection, TEST_DATABASE, "main__address_book_root", "main__address_book_settings")
        assertEquals(expected, actual)
    }

    @Test
    fun `Rows must be created when only key fields are specified in a composition-set entity`() {
        createDatabase(metaModel, connection)

        val modelJson = """
            |{
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val model = getMainModelFromJsonString(metaModel, modelJson)
        set(model, connection)

        val expected = """
            |= Table main__address_book_root =
            |
            |* Row 1 *
            |parent_id${'$'}: []
            |name: Super Heroes
            |last_updated: null
            |
            |= Table main__address_book_person =
            |
            |* Row 1 *
            |parent_id${'$'}: ["person"]
            |id: cc477201-48ec-4367-83a4-7fdbd92f8a6f
            |first_name: null
            |last_name: null
            |hero_name: null
            |email: null
            |picture: null
            |password: null
            |previous_passwords: null
            |main_secret: null
            |other_secrets: null
            |
        """.trimMargin()
        val actual = getTableRows(connection, TEST_DATABASE, "main__address_book_root", "main__address_book_person")
        assertEquals(expected, actual)
    }
}