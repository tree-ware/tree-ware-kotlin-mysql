package org.treeWare.mySql.operator

import org.treeWare.model.core.MainModel
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.GetEntityDelegate
import org.treeWare.model.operator.SetEntityDelegate
import org.treeWare.model.operator.Response
import org.treeWare.mySql.operator.delegate.MySqlGetDelegate
import javax.sql.DataSource

fun get(
    request: MainModel,
    setEntityDelegates: EntityDelegateRegistry<SetEntityDelegate>?,
    getEntityDelegates: EntityDelegateRegistry<GetEntityDelegate>?,
    dataSource: DataSource,
    responseModel: MutableMainModel,
    logCommands: Boolean = false
): Response = dataSource.connection.use { connection ->
    val getDelegate = MySqlGetDelegate(setEntityDelegates, getEntityDelegates, connection, logCommands)
    org.treeWare.model.operator.get(request, getDelegate, setEntityDelegates, getEntityDelegates, responseModel)
}