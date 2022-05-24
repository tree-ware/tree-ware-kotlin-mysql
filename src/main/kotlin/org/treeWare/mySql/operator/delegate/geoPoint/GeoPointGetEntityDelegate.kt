package org.treeWare.mySql.operator.delegate.geoPoint

import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.setDoubleSingleField
import org.treeWare.model.operator.GetEntityDelegate
import org.treeWare.mySql.operator.delegate.MySqlGetEntityDelegate
import org.treeWare.mySql.operator.delegate.SqlColumn
import java.sql.ResultSet

internal class GeoPointGetEntityDelegate : GetEntityDelegate, MySqlGetEntityDelegate {
    override fun getSelectColumns(fieldMeta: EntityModel): List<SqlColumn> {
        val columnName = getMetaName(fieldMeta)
        return listOf(
            SqlColumn(null, "ST_Longitude($columnName) AS $columnName\$lng", null),
            SqlColumn(null, "ST_Latitude($columnName) AS $columnName\$lat", null),
        )
    }

    override fun setValueFromResult(responseEntity: MutableEntityModel, result: ResultSet, columnIndex: Int): Int {
        val longitude = result.getDouble(columnIndex)
        val latitude = result.getDouble(columnIndex + 1)
        setDoubleSingleField(responseEntity, "latitude", latitude)
        setDoubleSingleField(responseEntity, "longitude", longitude)
        return 2
    }
}