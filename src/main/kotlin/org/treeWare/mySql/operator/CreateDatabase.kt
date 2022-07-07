package org.treeWare.mySql.operator

import org.lighthousegames.logging.logging
import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.EntityDelegateRegistry
import javax.sql.DataSource

private val logger = logging()

fun createDatabase(
    mainMeta: MainModel,
    delegates: EntityDelegateRegistry<GenerateCreateDatabaseCommandsEntityDelegate>?,
    dataSource: DataSource,
    logCommands: Boolean = false,
    foreignKeyConstraints: CreateForeignKeyConstraints = CreateForeignKeyConstraints.ALL
) {
    val connection = dataSource.connection
    val commands = generateCreateDatabaseCommands(mainMeta, delegates, foreignKeyConstraints)
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
    connection.close()
}