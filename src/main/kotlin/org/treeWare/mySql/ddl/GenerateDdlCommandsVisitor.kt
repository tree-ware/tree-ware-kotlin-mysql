package org.treeWare.mySql.ddl

import org.treeWare.metaModel.aux.getResolvedVersionAux
import org.treeWare.metaModel.getMetaModelName
import org.treeWare.model.core.*
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.mySql.ddl.traversal.Leader1DdlVisitor
import org.treeWare.mySql.operator.liquibase.ChangeSet
import org.treeWare.mySql.operator.liquibase.MutableChangeSet
import org.treeWare.sql.ddl.*

class GenerateDdlCommandsVisitor(
    meta: EntityModel,
    private val createDatabase: Boolean
) : Leader1DdlVisitor<TraversalAction> {
    val changeSets: List<ChangeSet>
        get() = (createChangeSets + alterChangeSets).also { changeSets ->
            changeSets.forEachIndexed { index, changeSet ->
                changeSet.sequenceNumber = index + 1
            }
        }

    private val liquibaseAuthor = getLiquibaseAuthor(meta)

    private val createChangeSets = mutableListOf<MutableChangeSet>()
    private val alterChangeSets = mutableListOf<MutableChangeSet>()

    private var createTableCommand = StringBuilder()
    private var createTableHasContent = false
    private var createTableRollbackCommand = StringBuilder()
    private var primaryKeyColumnNames = mutableListOf<String>()
    private var alterTableCommand = StringBuilder()
    private var alterTableHasContent = false
    private var alterTableRollbackCommand = StringBuilder()

    private fun resetCommandState() {
        createTableCommand = StringBuilder()
        createTableHasContent = false
        createTableRollbackCommand = StringBuilder()
        primaryKeyColumnNames.clear()
        alterTableCommand = StringBuilder()
        alterTableHasContent = false
        alterTableRollbackCommand = StringBuilder()
    }

    override fun visitDatabase(leaderDatabase1: Database): TraversalAction {
        if (createDatabase) {
            val databaseName = leaderDatabase1.name ?: throw IllegalStateException()
            val command = "CREATE DATABASE IF NOT EXISTS $databaseName;"
            val rollbackCommand = "DROP DATABASE IF EXISTS $databaseName;"
            val createChangeSet = MutableChangeSet(liquibaseAuthor).add(command, rollbackCommand)
            createChangeSets.add(createChangeSet)
        }
        return TraversalAction.CONTINUE
    }

    override fun leaveDatabase(leaderDatabase1: Database) {}

    override fun visitTable(leaderTable1: Table): TraversalAction {
        resetCommandState()
        val tableName = leaderTable1.name ?: throw IllegalStateException()
        createTableCommand.append("CREATE TABLE IF NOT EXISTS $tableName (")
        createTableRollbackCommand.append("DROP TABLE IF EXISTS $tableName;")
        alterTableCommand.append("ALTER TABLE $tableName")
        alterTableRollbackCommand.append("ALTER TABLE $tableName")
        return TraversalAction.CONTINUE
    }

    override fun leaveTable(leaderTable1: Table) {
        createTableCommand.appendLine(",")
            .append("  PRIMARY KEY (")
            .append(primaryKeyColumnNames.joinToString())
            .append(")")
        createTableCommand.appendLine().append(") ENGINE = InnoDB;")
        val createChangeSet =
            MutableChangeSet(liquibaseAuthor).add(createTableCommand.toString(), createTableRollbackCommand.toString())
        createChangeSets.add(createChangeSet)
        if (alterTableHasContent) {
            alterTableCommand.append(";")
            alterTableRollbackCommand.append(";")
            val alterChangeSet = MutableChangeSet(liquibaseAuthor).add(
                alterTableCommand.toString(),
                alterTableRollbackCommand.toString()
            )
            alterChangeSets.add(alterChangeSet)
        }
    }

    override fun visitColumn(leaderColumn1: Column): TraversalAction {
        val columnName = leaderColumn1.name ?: throw IllegalStateException()
        val columnType = leaderColumn1.type ?: throw IllegalStateException()
        createTableCommand
            .appendLine(if (createTableHasContent) "," else "")
            .append("  $columnName $columnType")
        createTableHasContent = true
        if (leaderColumn1.isPrimaryKey == true) primaryKeyColumnNames.add(columnName)
        return TraversalAction.CONTINUE
    }

    override fun leaveColumn(leaderColumn1: Column) {}

    override fun visitIndex(leaderIndex1: Index): TraversalAction {
        val indexName = leaderIndex1.name ?: throw IllegalStateException()
        val isUnique = leaderIndex1.isUnique ?: throw IllegalStateException()
        val columns = leaderIndex1.columns ?: throw IllegalStateException()
        val columnNames = columns.map { it.name ?: throw IllegalStateException() }
        createTableCommand
            .appendLine(",").append("  ")
            .append(if (isUnique) "UNIQUE " else "")
            .append("INDEX ")
            .append(indexName)
            .append(" (")
            .append(columnNames.joinToString())
            .append(")")
        return TraversalAction.ABORT_SUB_TREE // children (index-columns) are handled above
    }

    override fun leaveIndex(leaderIndex1: Index) {}

    override fun visitIndexColumn(leaderIndexColumn1: IndexColumn): TraversalAction {
        // Nothing to do here since index-columns are handled when the parent (index) is visited.
        return TraversalAction.CONTINUE
    }

    override fun leaveIndexColumn(leaderIndexColumn1: IndexColumn) {}

    override fun visitForeignKey(leaderForeignKey1: ForeignKey): TraversalAction {
        val foreignKeyName = leaderForeignKey1.name ?: throw IllegalStateException()
        val targetTable = leaderForeignKey1.targetTable ?: throw IllegalStateException()
        val keyMappings = leaderForeignKey1.keyMappings ?: throw IllegalStateException()
        val sourceColumns = keyMappings.map { it.sourceKey ?: throw IllegalStateException() }
        val targetColumns = keyMappings.map { it.targetKey ?: throw IllegalStateException() }
        alterTableCommand
            .appendLine(if (alterTableHasContent) "," else "")
            .append("  ADD FOREIGN KEY ")
            .append(foreignKeyName)
            .append(" (")
            .append(sourceColumns.joinToString())
            .append(")")
            .append(" REFERENCES ")
            .append(targetTable)
            .append("(")
            .append(targetColumns.joinToString())
            .append(") ON DELETE RESTRICT")
        alterTableRollbackCommand
            .appendLine(if (alterTableHasContent) "," else "")
            .append("  DROP FOREIGN KEY ")
            .append(foreignKeyName)
        alterTableHasContent = true
        return TraversalAction.ABORT_SUB_TREE // children (key-mappings) are handled above
    }

    override fun leaveForeignKey(leaderForeignKey1: ForeignKey) {}

    override fun visitKeyMapping(leaderKeyMapping1: KeyMapping): TraversalAction {
        // Nothing to do here since key-mappings are handled when the parent (foreign-key) is visited.
        return TraversalAction.CONTINUE
    }

    override fun leaveKeyMapping(leaderKeyMapping1: KeyMapping) {}
}

private fun getLiquibaseAuthor(meta: EntityModel): String =
    "${getMetaModelName(meta)}-${getResolvedVersionAux(meta).semantic}"
