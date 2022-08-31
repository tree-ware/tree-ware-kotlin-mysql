package org.treeWare.mySql.ddl.traversal

import org.treeWare.model.core.*
import org.treeWare.model.traversal.IllegalStateLeader1ModelVisitor

class Leader1DdlAdapter<Return>(
    private val adaptee: Leader1DdlVisitor<Return>,
    private val defaultVisitReturn: Return
) : IllegalStateLeader1ModelVisitor<Return>() {
    override fun visitMain(leaderMain1: MainModel): Return = defaultVisitReturn
    override fun leaveMain(leaderMain1: MainModel) {}

    override fun visitEntity(leaderEntity1: EntityModel): Return =
        when (val entityMetaName = leaderEntity1.getMetaResolved()?.fullName) {
            "/org.tree_ware.sql.ddl/ddl" -> defaultVisitReturn
            "/org.tree_ware.sql.ddl/database" -> adaptee.visitDatabase(leaderEntity1)
            "/org.tree_ware.sql.ddl/table" -> adaptee.visitTable(leaderEntity1)
            "/org.tree_ware.sql.ddl/column" -> adaptee.visitColumn(leaderEntity1)
            "/org.tree_ware.sql.ddl/index" -> adaptee.visitIndex(leaderEntity1)
            "/org.tree_ware.sql.ddl/foreign_key" -> adaptee.visitForeignKey(leaderEntity1)
            else -> throw IllegalStateException("Illegal entityMetaName: $entityMetaName")
        }

    override fun leaveEntity(leaderEntity1: EntityModel) =
        when (val entityMetaName = leaderEntity1.getMetaResolved()?.fullName) {
            "/org.tree_ware.sql.ddl/ddl" -> Unit
            "/org.tree_ware.sql.ddl/database" -> adaptee.leaveDatabase(leaderEntity1)
            "/org.tree_ware.sql.ddl/table" -> adaptee.leaveTable(leaderEntity1)
            "/org.tree_ware.sql.ddl/column" -> adaptee.leaveColumn(leaderEntity1)
            "/org.tree_ware.sql.ddl/index" -> adaptee.leaveIndex(leaderEntity1)
            "/org.tree_ware.sql.ddl/foreign_key" -> adaptee.leaveForeignKey(leaderEntity1)
            else -> throw IllegalStateException("Illegal entityMetaName: $entityMetaName")
        }

    override fun visitSetField(leaderField1: SetFieldModel): Return = defaultVisitReturn
    override fun leaveSetField(leaderField1: SetFieldModel) {}

    override fun visitListField(leaderField1: ListFieldModel): Return =
        when (val listFieldMetaName = leaderField1.getMetaResolved()?.fullName) {
            "/org.tree_ware.sql.ddl/table/primary_key" -> adaptee.visitPrimaryKey(leaderField1)
            "/org.tree_ware.sql.ddl/index/columns" -> defaultVisitReturn
            "/org.tree_ware.sql.ddl/foreign_key/source_columns" -> defaultVisitReturn
            "/org.tree_ware.sql.ddl/foreign_key/target_keys" -> defaultVisitReturn
            else -> throw IllegalStateException("Illegal listFieldMetaName: $listFieldMetaName")
        }

    override fun leaveListField(leaderField1: ListFieldModel) =
        when (val listFieldMetaName = leaderField1.getMetaResolved()?.fullName) {
            "/org.tree_ware.sql.ddl/table/primary_key" -> adaptee.leavePrimaryKey(leaderField1)
            "/org.tree_ware.sql.ddl/index/columns" -> Unit
            "/org.tree_ware.sql.ddl/foreign_key/source_columns" -> Unit
            "/org.tree_ware.sql.ddl/foreign_key/target_keys" -> Unit
            else -> throw IllegalStateException("Illegal listFieldMetaName: $listFieldMetaName")
        }

    override fun visitSingleField(leaderField1: SingleFieldModel): Return = defaultVisitReturn
    override fun leaveSingleField(leaderField1: SingleFieldModel) {}

    override fun visitPrimitive(leaderValue1: PrimitiveModel): Return = defaultVisitReturn
    override fun leavePrimitive(leaderValue1: PrimitiveModel) {}
}