package org.treeWare.mySql.operator

import org.apache.logging.log4j.LogManager
import org.treeWare.model.core.MainModel
import java.sql.Connection

private val logger = LogManager.getLogger()

fun createDatabase(mainMeta: MainModel, connection: Connection, logCommands: Boolean = false) {
    val commands = generateCreateCommands(mainMeta)
    commands.forEach { command ->
        if (logCommands) logger.info(command)
        val statement = connection.createStatement()
        try {
            statement.executeUpdate(command)
        } catch (e: Exception) {
            logger.error("Exception for SQL command: $command")
            throw e
        } finally {
            statement.close()
        }
    }
}