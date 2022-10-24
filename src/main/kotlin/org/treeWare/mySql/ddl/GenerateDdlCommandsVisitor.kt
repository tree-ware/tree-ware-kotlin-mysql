package org.treeWare.mySql.ddl

import org.treeWare.metaModel.aux.getResolvedVersionAux
import org.treeWare.metaModel.getMainMetaName
import org.treeWare.model.core.*
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.mySql.ddl.traversal.Leader1DdlVisitor
import org.treeWare.mySql.operator.liquibase.ChangeSet
import org.treeWare.mySql.operator.liquibase.MutableChangeSet

class GenerateDdlCommandsVisitor(
    mainMeta: MainModel,
    private val createDatabase: Boolean
) : Leader1DdlVisitor<TraversalAction> {
    val changeSets: List<ChangeSet>
        get() = (createChangeSets + alterChangeSets).also { changeSets ->
            changeSets.forEachIndexed { index, changeSet ->
                changeSet.sequenceNumber = index + 1
            }
        }

    private val liquibaseAuthor = getLiquibaseAuthor(mainMeta)

    private val createChangeSets = mutableListOf<MutableChangeSet>()
    private val alterChangeSets = mutableListOf<MutableChangeSet>()

    private var createTableCommand = StringBuilder()
    private var createTableHasContent = false
    private var createTableRollbackCommand = StringBuilder()
    private var alterTableCommand = StringBuilder()
    private var alterTableHasContent = false
    private var alterTableRollbackCommand = StringBuilder()

    private fun resetCommandState() {
        createTableCommand = StringBuilder()
        createTableHasContent = false
        createTableRollbackCommand = StringBuilder()
        alterTableCommand = StringBuilder()
        alterTableHasContent = false
        alterTableRollbackCommand = StringBuilder()
    }

    override fun visitDatabase(leaderDatabase1: EntityModel): TraversalAction {
        if (createDatabase) {
            val databaseName = getSingleString(leaderDatabase1, "name")
            val command = "CREATE DATABASE IF NOT EXISTS $databaseName;"
            val rollbackCommand = "DROP DATABASE IF EXISTS $databaseName;"
            val createChangeSet = MutableChangeSet(liquibaseAuthor).add(command, rollbackCommand)
            createChangeSets.add(createChangeSet)
        }
        return TraversalAction.CONTINUE
    }

    override fun leaveDatabase(leaderDatabase1: EntityModel) {}

    override fun visitTable(leaderTable1: EntityModel): TraversalAction {
        resetCommandState()
        val tableName = getSingleString(leaderTable1, "name")
        createTableCommand.append("CREATE TABLE IF NOT EXISTS $tableName (")
        createTableRollbackCommand.append("DROP TABLE IF EXISTS $tableName;")
        alterTableCommand.append("ALTER TABLE $tableName")
        alterTableRollbackCommand.append("ALTER TABLE $tableName")
        return TraversalAction.CONTINUE
    }

    override fun leaveTable(leaderTable1: EntityModel) {
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

    override fun visitColumn(leaderColumn1: EntityModel): TraversalAction {
        val columnName = getSingleString(leaderColumn1, "name")
        val columnType = getSingleString(leaderColumn1, "type")
        createTableCommand
            .appendLine(if (createTableHasContent) "," else "")
            .append("  $columnName $columnType")
        createTableHasContent = true
        return TraversalAction.CONTINUE
    }

    override fun leaveColumn(leaderColumn1: EntityModel) {}

    override fun visitPrimaryKey(leaderField1: ListFieldModel): TraversalAction {
        val columnNames = getPrimitiveValues(leaderField1)
        createTableCommand.appendLine(",")
            .append("  PRIMARY KEY (")
            .append(columnNames.joinToString())
            .append(")")
        return TraversalAction.CONTINUE
    }

    override fun leavePrimaryKey(leaderField1: ListFieldModel) {}

    override fun visitIndex(leaderIndex1: EntityModel): TraversalAction {
        val indexName = getSingleString(leaderIndex1, "name")
        val isUnique = getSingleBoolean(leaderIndex1, "is_unique")
        val columns = getPrimitiveValues(getCollectionField(leaderIndex1, "columns"))
        createTableCommand
            .appendLine(",").append("  ")
            .append(if (isUnique) "UNIQUE " else "")
            .append("INDEX ")
            .append(indexName)
            .append(" (")
            .append(columns.joinToString())
            .append(")")
        return TraversalAction.CONTINUE
    }

    override fun leaveIndex(leaderIndex1: EntityModel) {}

    override fun visitForeignKey(leaderForeignKey1: EntityModel): TraversalAction {
        val foreignKeyName = getSingleString(leaderForeignKey1, "name")
        val sourceColumns = getPrimitiveValues(getCollectionField(leaderForeignKey1, "source_columns"))
        val targetTable = getSingleString(leaderForeignKey1, "target_table")
        val targetKeys = getPrimitiveValues(getCollectionField(leaderForeignKey1, "target_keys"))
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
            .append(targetKeys.joinToString())
            .append(") ON DELETE RESTRICT")
        alterTableRollbackCommand
            .appendLine(if (alterTableHasContent) "," else "")
            .append("  DROP FOREIGN KEY ")
            .append(foreignKeyName)
        alterTableHasContent = true
        return TraversalAction.CONTINUE
    }

    override fun leaveForeignKey(leaderForeignKey1: EntityModel) {}
}

private fun getLiquibaseAuthor(mainMeta: MainModel): String =
    "${getMainMetaName(mainMeta)}-${getResolvedVersionAux(mainMeta).semantic}"

private fun getPrimitiveValues(stringListField: CollectionFieldModel): List<Any> = stringListField.values
    .filterIsInstance<PrimitiveModel>()
    .map { it.value }