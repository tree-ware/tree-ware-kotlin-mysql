package org.treeWare.mySql.ddl.traversal

import org.treeWare.sql.ddl.*

interface Leader1DdlVisitor<Return> {
    fun visitDatabase(leaderDatabase1: Database): Return
    fun leaveDatabase(leaderDatabase1: Database)

    fun visitTable(leaderTable1: Table): Return
    fun leaveTable(leaderTable1: Table)

    fun visitColumn(leaderColumn1: Column): Return
    fun leaveColumn(leaderColumn1: Column)

    fun visitIndex(leaderIndex1: Index): Return
    fun leaveIndex(leaderIndex1: Index)

    fun visitIndexColumn(leaderIndexColumn1: IndexColumn): Return
    fun leaveIndexColumn(leaderIndexColumn1: IndexColumn)

    fun visitForeignKey(leaderForeignKey1: ForeignKey): Return
    fun leaveForeignKey(leaderForeignKey1: ForeignKey)

    fun visitKeyMapping(leaderKeyMapping1: KeyMapping): Return
    fun leaveKeyMapping(leaderKeyMapping1: KeyMapping)
}