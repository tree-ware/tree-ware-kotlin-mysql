package org.treeWare.mySql.operator

import org.treeWare.model.core.EntityModel
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.SetEntityDelegate
import org.treeWare.model.operator.Response
import org.treeWare.mySql.operator.delegate.MySqlSetDelegate
import java.time.Clock
import javax.sql.DataSource

fun set(
    model: EntityModel,
    entityDelegates: EntityDelegateRegistry<SetEntityDelegate>?,
    dataSource: DataSource,
    logCommands: Boolean = false,
    databasePrefix: String? = null,
    clock: Clock = Clock.systemUTC()
): Response = dataSource.connection.use { connection ->
    val resolvedRootMeta = model.meta ?: throw IllegalStateException("No meta-model for model being set")
    val setDelegate = MySqlSetDelegate(resolvedRootMeta, entityDelegates, connection, logCommands, databasePrefix, clock)
    org.treeWare.model.operator.set(model, setDelegate, entityDelegates)
}