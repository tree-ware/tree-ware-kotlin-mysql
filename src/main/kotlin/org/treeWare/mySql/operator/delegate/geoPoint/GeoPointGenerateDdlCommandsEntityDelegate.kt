package org.treeWare.mySql.operator.delegate.geoPoint

import org.treeWare.mySql.operator.GenerateDdlCommandsEntityDelegate

class GeoPointGenerateDdlCommandsEntityDelegate : GenerateDdlCommandsEntityDelegate {
    override fun isSingleColumn(): Boolean = true
    override fun getSingleColumnType(): String = "POINT SRID 4326"
}