package org.treeWare.mySql.operator

import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.SetEntityDelegate
import org.treeWare.mySql.operator.delegate.MySqlSetDelegate
import java.sql.Connection
import java.time.Clock

fun set(
    main: MainModel,
    entityDelegates: EntityDelegateRegistry<SetEntityDelegate>?,
    connection: Connection,
    logCommands: Boolean = false,
    clock: Clock = Clock.systemUTC()
): List<String> {
    val setDelegate = MySqlSetDelegate(entityDelegates, connection, logCommands, clock)
    return org.treeWare.model.operator.set(main, setDelegate, entityDelegates)
}