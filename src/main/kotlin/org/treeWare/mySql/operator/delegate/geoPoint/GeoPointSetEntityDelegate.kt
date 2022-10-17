package org.treeWare.mySql.operator.delegate.geoPoint

import org.treeWare.metaModel.FieldType
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.getSingleDouble
import org.treeWare.mySql.operator.delegate.*

class GeoPointSetEntityDelegate : MySqlSetEntityDelegate {
    override fun isSingleValue(): Boolean = true

    override fun getSqlColumn(namePrefix: String?, name: String, entity: EntityModel): SqlColumn {
        val latitude = getSingleDouble(entity, "latitude")
        val longitude = getSingleDouble(entity, "longitude")
        return if (latitude == null || longitude == null) SingleValuedSqlColumn(namePrefix, name, null)
        // NOTE: the longitude is equivalent to an x-coordinate and the latitude is equivalent to a y-coordinate.
        // So the longitude must be specified before the latitude in the Point() function.
        else MultiValuedSqlColumn(
            namePrefix,
            name,
            listOf(TypedValue(FieldType.DOUBLE, longitude), TypedValue(FieldType.DOUBLE, latitude)),
            "ST_SRID(Point(?, ?), 4326)"
        )
    }
}