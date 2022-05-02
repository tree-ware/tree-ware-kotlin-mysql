package org.treeWare.mySql.operator

import com.wix.mysql.EmbeddedMysql
import com.wix.mysql.config.MysqldConfig
import com.wix.mysql.distribution.Version
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.treeWare.metaModel.newMySqlAddressBookMetaModel
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.getMainModelFromJsonFile
import org.treeWare.model.operator.*
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import org.treeWare.mySql.operator.delegate.registerMySqlOperatorEntityDelegates
import org.treeWare.mySql.test.getAvailableServerPort
import java.sql.Connection
import java.sql.DriverManager
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val metaModel = newMySqlAddressBookMetaModel("test", null, null).metaModel
    ?: throw IllegalStateException("Meta-model has validation errors")
private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetTests {
    private val operatorEntityDelegateRegistry = OperatorEntityDelegateRegistry()
    private val setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?
    private val getEntityDelegates: EntityDelegateRegistry<GetEntityDelegate>?

    private val port = getAvailableServerPort()
    private val mysqld: EmbeddedMysql
    private val connection: Connection

    init {
        registerMySqlOperatorEntityDelegates(operatorEntityDelegateRegistry)
        setEntityDelegates = operatorEntityDelegateRegistry.get(SetOperatorId)
        getEntityDelegates = operatorEntityDelegateRegistry.get(GetOperatorId)

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

    init {
        val create = getMainModelFromJsonFile(
            metaModel,
            "model/my_sql_get_tests_initial_create_request.json",
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
        val now = "2022-04-14T00:40:41.450Z"
        val clock = Clock.fixed(Instant.parse(now), ZoneOffset.UTC)
        val setErrors = set(create, setEntityDelegates, connection, clock = clock)
        assertEquals("", setErrors.joinToString("\n"))
    }

    @AfterAll
    fun afterAll() {
        mysqld.stop()
    }

    @Test
    fun `get() must fetch specific and wildcard entities in a request`() {
        val request =
            getMainModelFromJsonFile(metaModel, "model/my_sql_get_request_specific_and_wildcard_entities.json")
        val response = get(request, getEntityDelegates, connection)
        assertTrue(response is GetResponse.Model)
        assertMatchesJson(
            response.model,
            "model/my_sql_get_response_specific_and_wildcard_entities.json",
            EncodePasswords.ALL
        )
    }
}