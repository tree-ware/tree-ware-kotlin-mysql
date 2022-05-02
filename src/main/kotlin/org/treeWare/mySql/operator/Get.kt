package org.treeWare.mySql.operator

import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.GetEntityDelegate
import org.treeWare.model.operator.GetResponse
import org.treeWare.mySql.operator.delegate.MySqlGetDelegate
import java.sql.Connection

fun get(
    request: MainModel,
    entityDelegates: EntityDelegateRegistry<GetEntityDelegate>?,
    connection: Connection,
    logCommands: Boolean = false
): GetResponse {
    val getDelegate = MySqlGetDelegate(entityDelegates, connection, logCommands)
    return org.treeWare.model.operator.get(request, getDelegate, entityDelegates)
}