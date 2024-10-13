package org.treeWare.mySql.ddl.traversal

import org.treeWare.model.core.*
import org.treeWare.model.traversal.IllegalStateLeader1ModelVisitor
import org.treeWare.sql.ddl.*

class Leader1DdlAdapter<Return>(
    private val adaptee: Leader1DdlVisitor<Return>,
    private val defaultVisitReturn: Return
) : IllegalStateLeader1ModelVisitor<Return>() {
    override fun visitEntity(leaderEntity1: EntityModel): Return =
        when (val entityMetaName = leaderEntity1.getMetaResolved()?.fullName) {
            "/org.tree_ware.sql.ddl/ddl_root" -> defaultVisitReturn
            "/org.tree_ware.sql.ddl/database" -> adaptee.visitDatabase(leaderEntity1 as Database)
            "/org.tree_ware.sql.ddl/table" -> adaptee.visitTable(leaderEntity1 as Table)
            "/org.tree_ware.sql.ddl/column" -> adaptee.visitColumn(leaderEntity1 as Column)
            "/org.tree_ware.sql.ddl/index" -> adaptee.visitIndex(leaderEntity1 as Index)
            "/org.tree_ware.sql.ddl/index_column" -> adaptee.visitIndexColumn(leaderEntity1 as IndexColumn)
            "/org.tree_ware.sql.ddl/foreign_key" -> adaptee.visitForeignKey(leaderEntity1 as ForeignKey)
            "/org.tree_ware.sql.ddl/key_mapping" -> adaptee.visitKeyMapping(leaderEntity1 as KeyMapping)
            else -> throw IllegalStateException("Illegal entityMetaName: $entityMetaName")
        }

    override fun leaveEntity(leaderEntity1: EntityModel) =
        when (val entityMetaName = leaderEntity1.getMetaResolved()?.fullName) {
            "/org.tree_ware.sql.ddl/ddl_root" -> Unit
            "/org.tree_ware.sql.ddl/database" -> adaptee.leaveDatabase(leaderEntity1 as Database)
            "/org.tree_ware.sql.ddl/table" -> adaptee.leaveTable(leaderEntity1 as Table)
            "/org.tree_ware.sql.ddl/column" -> adaptee.leaveColumn(leaderEntity1 as Column)
            "/org.tree_ware.sql.ddl/index" -> adaptee.leaveIndex(leaderEntity1 as Index)
            "/org.tree_ware.sql.ddl/index_column" -> adaptee.leaveIndexColumn(leaderEntity1 as IndexColumn)
            "/org.tree_ware.sql.ddl/foreign_key" -> adaptee.leaveForeignKey(leaderEntity1 as ForeignKey)
            "/org.tree_ware.sql.ddl/key_mapping" -> adaptee.leaveKeyMapping(leaderEntity1 as KeyMapping)
            else -> throw IllegalStateException("Illegal entityMetaName: $entityMetaName")
        }

    override fun visitSetField(leaderField1: SetFieldModel): Return = defaultVisitReturn
    override fun leaveSetField(leaderField1: SetFieldModel) {}

    override fun visitSingleField(leaderField1: SingleFieldModel): Return = defaultVisitReturn
    override fun leaveSingleField(leaderField1: SingleFieldModel) {}

    override fun visitPrimitive(leaderValue1: PrimitiveModel): Return = defaultVisitReturn
    override fun leavePrimitive(leaderValue1: PrimitiveModel) {}
}