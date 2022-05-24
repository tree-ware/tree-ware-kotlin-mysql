package org.treeWare.mySql.operator.delegate

import org.treeWare.model.operator.GetOperatorId
import org.treeWare.model.operator.OperatorEntityDelegateRegistry
import org.treeWare.model.operator.SetOperatorId
import org.treeWare.mySql.operator.GenerateCreateDatabaseCommandsOperatorId
import org.treeWare.mySql.operator.delegate.geoPoint.GeoPointGenerateCreateDatabaseCommandsEntityDelegate
import org.treeWare.mySql.operator.delegate.geoPoint.GeoPointGetEntityDelegate
import org.treeWare.mySql.operator.delegate.geoPoint.GeoPointSetEntityDelegate

private const val GEO_POINT_ENTITY_NAME = "/org.tree_ware.meta_model.geo/point"

fun registerMySqlOperatorEntityDelegates(registry: OperatorEntityDelegateRegistry) {
    registry.add(
        GEO_POINT_ENTITY_NAME,
        GenerateCreateDatabaseCommandsOperatorId,
        GeoPointGenerateCreateDatabaseCommandsEntityDelegate()
    )
    registry.add(
        GEO_POINT_ENTITY_NAME,
        SetOperatorId,
        GeoPointSetEntityDelegate()
    )
    registry.add(
        GEO_POINT_ENTITY_NAME,
        GetOperatorId,
        GeoPointGetEntityDelegate()
    )
}