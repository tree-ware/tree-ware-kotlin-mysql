package org.treeWare.mySql.operator

import org.lighthousegames.logging.logging
import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.EntityDelegateRegistry
import java.sql.Connection

private val logger = logging()

fun createDatabase(
    mainMeta: MainModel,
    delegates: EntityDelegateRegistry<GenerateCreateDatabaseCommandsEntityDelegate>?,
    connection: Connection,
    logCommands: Boolean = false
) {
    val commands = generateCreateDatabaseCommands(mainMeta, delegates)
    commands.forEach { command ->
        if (logCommands) logger.info { command }
        val statement = connection.createStatement()
        try {
            statement.executeUpdate(command)
        } catch (e: Exception) {
            logger.error { "Exception for SQL command: $command" }
            throw e
        } finally {
            statement.close()
        }
    }
}