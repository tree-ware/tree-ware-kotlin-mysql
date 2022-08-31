package org.treeWare.mySql.ddl.traversal

import org.treeWare.model.core.ElementModel
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach

fun leader1DdlForEach(leader1: ElementModel, visitor: Leader1DdlVisitor<TraversalAction>): TraversalAction =
    forEach(leader1, Leader1DdlAdapter(visitor, TraversalAction.CONTINUE), false)