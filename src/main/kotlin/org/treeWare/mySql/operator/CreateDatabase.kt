package org.treeWare.mySql.operator

import org.lighthousegames.logging.logging
import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.EntityDelegateRegistry
import javax.sql.DataSource

private val logger = logging()

fun createDatabase(
    mainMeta: MainModel,
    delegates: EntityDelegateRegistry<GenerateDdlCommandsEntityDelegate>?,
    dataSource: DataSource,
    logCommands: Boolean = false,
    foreignKeyConstraints: CreateForeignKeyConstraints = CreateForeignKeyConstraints.ALL
) = dataSource.connection.use { connection ->
    val changeSets = generateDdlChangeSets(mainMeta, delegates, true, foreignKeyConstraints)
    changeSets.forEach { changeSet ->
        changeSet.commands.forEach { command ->
            if (logCommands) logger.info { command }
            connection.createStatement().use { statement ->
                try {
                    statement.executeUpdate(command)
                } catch (e: Exception) {
                    logger.error { "Exception for SQL command: $command" }
                    throw e
                }
            }
        }
    }
}