package org.treeWare.mySql.operator.delegate

import org.treeWare.model.core.EntityModel
import org.treeWare.model.operator.SetEntityDelegate

internal interface MySqlSetEntityDelegate : SetEntityDelegate {
    fun getSqlColumn(namePrefix: String?, name: String, entity: EntityModel): SqlColumn
}