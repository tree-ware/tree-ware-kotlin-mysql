package org.treeWare.mySql.ddl.traversal

import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.ListFieldModel

interface Leader1DdlVisitor<Return> {
    fun visitDatabase(leaderDatabase1: EntityModel): Return
    fun leaveDatabase(leaderDatabase1: EntityModel)

    fun visitTable(leaderTable1: EntityModel): Return
    fun leaveTable(leaderTable1: EntityModel)

    fun visitColumn(leaderColumn1: EntityModel): Return
    fun leaveColumn(leaderColumn1: EntityModel)

    fun visitPrimaryKey(leaderField1: ListFieldModel): Return
    fun leavePrimaryKey(leaderField1: ListFieldModel)

    fun visitIndex(leaderIndex1: EntityModel): Return
    fun leaveIndex(leaderIndex1: EntityModel)

    fun visitForeignKey(leaderForeignKey1: EntityModel): Return
    fun leaveForeignKey(leaderForeignKey1: EntityModel)
}