package org.treeWare.mySql.operator

import org.treeWare.model.core.MainModel
import java.sql.Connection

// TODO(deepak-nulu): return a model with error aux
fun set(mainModel: MainModel, connection: Connection) {
    val commands = generateSetCommands(mainModel)
    commands.forEach { command ->
        val statement = connection.createStatement()
        statement.executeUpdate(command)
        statement.close()
    }
}
