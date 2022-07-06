package org.treeWare.mySql.operator

import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.GetEntityDelegate
import org.treeWare.model.operator.SetEntityDelegate
import org.treeWare.model.operator.get.GetResponse
import org.treeWare.mySql.operator.delegate.MySqlGetDelegate
import javax.sql.DataSource

fun get(
    request: MainModel,
    setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?,
    getEntityDelegates: EntityDelegateRegistry<GetEntityDelegate>?,
    dataSource: DataSource,
    logCommands: Boolean = false
): GetResponse {
    val connection = dataSource.connection
    val getDelegate = MySqlGetDelegate(setEntityDelegates, getEntityDelegates, connection, logCommands)
    val getResponse = org.treeWare.model.operator.get(request, getDelegate, setEntityDelegates, getEntityDelegates)
    connection.close()
    return getResponse
}