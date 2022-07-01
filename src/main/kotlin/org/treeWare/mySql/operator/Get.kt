package org.treeWare.mySql.operator

import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.GetEntityDelegate
import org.treeWare.model.operator.SetEntityDelegate
import org.treeWare.model.operator.get.GetResponse
import org.treeWare.mySql.operator.delegate.MySqlGetDelegate
import java.sql.Connection

fun get(
    request: MainModel,
    setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?,
    getEntityDelegates: EntityDelegateRegistry<GetEntityDelegate>?,
    connection: Connection,
    logCommands: Boolean = false
): GetResponse {
    val getDelegate = MySqlGetDelegate(setEntityDelegates, getEntityDelegates, connection, logCommands)
    return org.treeWare.model.operator.get(request, getDelegate, setEntityDelegates, getEntityDelegates)
}