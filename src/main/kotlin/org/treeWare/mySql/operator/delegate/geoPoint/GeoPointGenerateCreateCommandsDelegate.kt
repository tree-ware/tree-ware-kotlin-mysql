package org.treeWare.mySql.operator.delegate.geoPoint

import org.treeWare.mySql.operator.GenerateCreateCommandsDelegate

class GeoPointGenerateCreateCommandsDelegate : GenerateCreateCommandsDelegate {
    override fun isSingleColumn(): Boolean = true
    override fun getSingleColumnType(): String = "POINT"
}