package org.treeWare.mySql.operator.delegate

import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MutableEntityModel
import java.sql.ResultSet

internal interface MySqlGetEntityDelegate {
    fun getSelectColumns(fieldMeta: EntityModel): List<SqlColumn>

    /**
     * @return the number of columns consumed from the result to set the value.
     */
    fun setValueFromResult(responseEntity: MutableEntityModel, result: ResultSet, columnIndex: Int): Int
}