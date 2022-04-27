package org.treeWare.mySql.operator.delegate.geoPoint

import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.getSingleDouble
import org.treeWare.model.operator.SetEntityDelegate

class GeoPointSetEntityDelegate : SetEntityDelegate {
    override fun isSingleValue(): Boolean = true
    override fun getSingleValue(entity: EntityModel): Any {
        val latitude = getSingleDouble(entity, "latitude")
        val longitude = getSingleDouble(entity, "longitude")
        // NOTE: the longitude is equivalent to an x-coordinate and the latitude is equivalent to a y-coordinate.
        // So the longitude must be specified before the latitude in the Point() function.
        return "ST_SRID(Point($longitude, $latitude), 4326)"
    }
}