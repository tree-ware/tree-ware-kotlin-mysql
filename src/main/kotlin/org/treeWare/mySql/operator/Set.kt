package org.treeWare.mySql.operator

import org.lighthousegames.logging.logging
import org.treeWare.model.core.MainModel
import java.sql.Connection

private val logger = logging()

// TODO(deepak-nulu): return a model with error aux
fun set(mainModel: MainModel, connection: Connection, logCommands: Boolean = false) {
    val commands = generateSetCommands(mainModel)
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