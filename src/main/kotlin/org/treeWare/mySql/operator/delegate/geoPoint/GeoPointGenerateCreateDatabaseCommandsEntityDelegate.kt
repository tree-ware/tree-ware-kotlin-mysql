package org.treeWare.mySql.operator.delegate.geoPoint

import org.treeWare.mySql.operator.GenerateCreateDatabaseCommandsEntityDelegate

class GeoPointGenerateCreateDatabaseCommandsEntityDelegate : GenerateCreateDatabaseCommandsEntityDelegate {
    override fun isSingleColumn(): Boolean = true
    override fun getSingleColumnType(): String = "POINT SRID 4326"
}