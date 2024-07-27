package org.treeWare.mySql.operator

import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.SetEntityDelegate
import org.treeWare.model.operator.Response
import org.treeWare.mySql.operator.delegate.MySqlSetDelegate
import java.time.Clock
import javax.sql.DataSource

fun set(
    main: MainModel,
    entityDelegates: EntityDelegateRegistry<SetEntityDelegate>?,
    dataSource: DataSource,
    logCommands: Boolean = false,
    clock: Clock = Clock.systemUTC()
): Response = dataSource.connection.use { connection ->
    val mainMeta = main.mainMeta ?: throw IllegalStateException("No mainMeta for main model being set")
    val setDelegate = MySqlSetDelegate(mainMeta, entityDelegates, connection, logCommands, clock)
    org.treeWare.model.operator.set(main, setDelegate, entityDelegates)
}