package org.treeWare.mySql.operator

import org.treeWare.model.core.MainModel
import java.sql.Connection

fun createDatabase(mainMeta: MainModel, connection: Connection) {
    val commands = generateCreateCommands(mainMeta)
    commands.forEach { command ->
        val statement = connection.createStatement()
        statement.executeUpdate(command)
        statement.close()
    }
}