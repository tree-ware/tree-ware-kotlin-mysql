package org.treeWare.mySql.ddl.traversal

import org.treeWare.model.core.EntityModel

interface Leader1DdlVisitor<Return> {
    fun visitDatabase(leaderDatabase1: EntityModel): Return
    fun leaveDatabase(leaderDatabase1: EntityModel)

    fun visitTable(leaderTable1: EntityModel): Return
    fun leaveTable(leaderTable1: EntityModel)

    fun visitColumn(leaderColumn1: EntityModel): Return
    fun leaveColumn(leaderColumn1: EntityModel)

    fun visitIndex(leaderIndex1: EntityModel): Return
    fun leaveIndex(leaderIndex1: EntityModel)

    fun visitIndexColumn(leaderIndexColumn1: EntityModel): Return
    fun leaveIndexColumn(leaderIndexColumn1: EntityModel)

    fun visitForeignKey(leaderForeignKey1: EntityModel): Return
    fun leaveForeignKey(leaderForeignKey1: EntityModel)

    fun visitKeyMapping(leaderKeyMapping1: EntityModel): Return
    fun leaveKeyMapping(leaderKeyMapping1: EntityModel)
}