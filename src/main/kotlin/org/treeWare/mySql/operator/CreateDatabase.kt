package org.treeWare.mySql.operator

import org.apache.logging.log4j.LogManager
import org.treeWare.model.core.MainModel
import java.sql.Connection

fun createDatabase(mainMeta: MainModel, connection: Connection, logCommands: Boolean = false) {
    val logger = if (logCommands) LogManager.getLogger() else null
    val commands = generateCreateCommands(mainMeta)
    commands.forEach { command ->
        logger?.info(command)
        val statement = connection.createStatement()
        statement.executeUpdate(command)
        statement.close()
    }
}