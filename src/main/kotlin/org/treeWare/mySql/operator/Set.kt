package org.treeWare.mySql.operator

import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.SetEntityDelegate
import org.treeWare.model.operator.set.SetResponse
import org.treeWare.mySql.operator.delegate.MySqlSetDelegate
import java.sql.Connection
import java.time.Clock

fun set(
    main: MainModel,
    entityDelegates: EntityDelegateRegistry<SetEntityDelegate>?,
    connection: Connection,
    logCommands: Boolean = false,
    clock: Clock = Clock.systemUTC()
): SetResponse {
    val mainMeta = main.mainMeta ?: throw IllegalStateException("No mainMeta for main model being set")
    val setDelegate = MySqlSetDelegate(mainMeta, entityDelegates, connection, logCommands, clock)
    return org.treeWare.model.operator.set(main, setDelegate, entityDelegates)
}