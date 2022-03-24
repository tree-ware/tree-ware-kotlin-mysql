package org.treeWare.mySql

import org.treeWare.model.operator.OperatorDelegateRegistry
import org.treeWare.mySql.operator.GenerateCreateCommandsOperatorId
import org.treeWare.mySql.operator.delegate.geoPoint.GeoPointGenerateCreateCommandsDelegate

private const val GEO_POINT_ENTITY_NAME = "/org.tree_ware.meta_model.geo/point"

fun registerMySqlOperatorDelegates(registry: OperatorDelegateRegistry) {
    registry.add(GEO_POINT_ENTITY_NAME, GenerateCreateCommandsOperatorId, GeoPointGenerateCreateCommandsDelegate())
}