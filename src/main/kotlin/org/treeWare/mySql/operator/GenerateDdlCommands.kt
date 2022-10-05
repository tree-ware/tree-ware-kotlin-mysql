package org.treeWare.mySql.operator

import org.treeWare.metaModel.*
import org.treeWare.model.core.*
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.model.operator.OperatorId
import org.treeWare.mySql.aux.getMySqlMetaModelMap
import org.treeWare.mySql.ddl.GenerateDdlCommandsVisitor
import org.treeWare.mySql.ddl.traversal.leader1DdlForEach
import org.treeWare.mySql.ddlMetaModel
import org.treeWare.mySql.operator.ddl.Column
import org.treeWare.mySql.operator.ddl.getAssociationFieldColumns
import org.treeWare.mySql.operator.ddl.getFieldColumns
import org.treeWare.mySql.operator.liquibase.ChangeSet
import org.treeWare.util.assertInDevMode

interface GenerateDdlCommandsEntityDelegate {
    fun isSingleColumn(): Boolean
    fun getSingleColumnType(): String
}

object GenerateDdlCommandsOperatorId : OperatorId<GenerateDdlCommandsEntityDelegate>

fun generateDdlChangeSets(
    mainMeta: MainModel,
    entityDelegates: EntityDelegateRegistry<GenerateDdlCommandsEntityDelegate>?,
    createDatabase: Boolean,
    createForeignKeyConstraints: CreateForeignKeyConstraints = CreateForeignKeyConstraints.ALL
): List<ChangeSet> {
    val ddlModel = MutableMainModel(ddlMetaModel)
    val ddlRoot = ddlModel.getOrNewRoot()
    populateMain(ddlRoot, mainMeta, entityDelegates, createForeignKeyConstraints)

    val generateDdlCommandsVisitor = GenerateDdlCommandsVisitor(mainMeta, createDatabase)
    leader1DdlForEach(ddlModel, generateDdlCommandsVisitor)
    return generateDdlCommandsVisitor.changeSets
}

private class DdlState {
    val visitedTables = mutableSetOf<String>()
    val ancestorStack = ArrayDeque<TableKeys>()
}

private data class TableKeys(val tableName: String, val keys: List<Column>, val isRealKeys: Boolean)

private fun populateMain(
    ddlRoot: MutableEntityModel,
    mainMeta: MainModel,
    entityDelegates: EntityDelegateRegistry<GenerateDdlCommandsEntityDelegate>?,
    createForeignKeyConstraints: CreateForeignKeyConstraints = CreateForeignKeyConstraints.ALL
) {
    val databaseName = getMySqlMetaModelMap(mainMeta)?.validated?.fullName ?: return
    val rootMeta = getRootMeta(mainMeta)
    val rootCompositionMeta = getMetaModelResolved(rootMeta)?.compositionMeta
        ?: throw IllegalStateException("Root meta not resolved")
    val ddlState = DdlState()
    populateComposition(
        ddlRoot,
        databaseName,
        rootCompositionMeta,
        true,
        entityDelegates,
        createForeignKeyConstraints,
        ddlState
    )
    assertInDevMode(ddlState.ancestorStack.isEmpty())
}

private fun populateComposition(
    ddlRoot: MutableEntityModel,
    databaseName: String,
    compositionMeta: EntityModel,
    isRoot: Boolean,
    entityDelegates: EntityDelegateRegistry<GenerateDdlCommandsEntityDelegate>?,
    createForeignKeyConstraints: CreateForeignKeyConstraints,
    ddlState: DdlState
) {
    val tableName = getMySqlMetaModelMap(compositionMeta)?.validated?.tableName ?: return
    val tableFullName = getMySqlMetaModelMap(compositionMeta)?.validated?.fullName ?: return
    val ddlTable = getDdlTable(ddlRoot, databaseName, tableFullName)

    val fieldsMeta = getFieldsMeta(compositionMeta).values.filterIsInstance<EntityModel>()
    if (!ddlState.visitedTables.contains(tableFullName)) {
        ddlState.visitedTables.add(tableFullName)
        if (isRoot) populateSingletonKeys(ddlTable)
        fieldsMeta.forEach { populateField(ddlTable, it, entityDelegates, createForeignKeyConstraints) }
        getUniquesMeta(compositionMeta)?.values?.forEach {
            populateUniqueIndex(ddlTable, compositionMeta, it, entityDelegates)
        }
    }

    val tableKeys = if (isRoot) getSingletonTableKeys(tableName)
    else getTableKeys(tableName, compositionMeta, entityDelegates)

    populateAncestors(ddlTable, createForeignKeyConstraints, ddlState)
    if (tableKeys == null) {
        val keylessTableKeys = populateKeylessParentUniqueIndex(ddlTable, tableName, ddlState)
        ddlState.ancestorStack.addLast(keylessTableKeys)
    } else ddlState.ancestorStack.addLast(tableKeys)
    val compositionFieldsMeta = fieldsMeta.filter { isCompositionFieldMeta(it) }
    compositionFieldsMeta.forEach { compositionFieldMeta ->
        val fieldCompositionMeta = getMetaModelResolved(compositionFieldMeta)?.compositionMeta
            ?: throw IllegalStateException("Composition field meta not resolved")
        if (fieldCompositionMeta == compositionMeta) {
            if (isSetFieldMeta(compositionFieldMeta)) populateSelfReferentialAncestor(
                ddlTable,
                requireNotNull(tableKeys) { "Keys are missing for self-referential composition-set" },
                createForeignKeyConstraints
            )
            return@forEach
        }
        populateComposition(
            ddlRoot,
            databaseName,
            fieldCompositionMeta,
            false,
            entityDelegates,
            createForeignKeyConstraints,
            ddlState
        )
    }
    ddlState.ancestorStack.removeLast()
}

private fun populateSingletonKeys(ddlTable: MutableEntityModel) {
    val ddlColumns = getOrNewMutableSetField(ddlTable, "columns")
    populateColumn(ddlColumns, SINGLETON_KEY_COLUMN_NAME, SINGLETON_KEY_COLUMN_TYPE)
    populatePrimaryKey(ddlTable, SINGLETON_KEY_COLUMN_NAME)
}

private fun getSingletonTableKeys(tableName: String): TableKeys =
    TableKeys(tableName, listOf(Column(SINGLETON_KEY_COLUMN_NAME, SINGLETON_KEY_COLUMN_TYPE)), true)

private fun getTableKeys(
    tableName: String,
    entityMeta: EntityModel,
    entityDelegates: EntityDelegateRegistry<GenerateDdlCommandsEntityDelegate>?
): TableKeys? {
    val keysMeta = getKeyFieldsMeta(entityMeta)
    if (keysMeta.isEmpty()) return null
    val keys = keysMeta.flatMap { getFieldColumns(it, false, entityDelegates) }
    return TableKeys(tableName, keys, true)
}

private fun populateField(
    ddlTable: MutableEntityModel,
    fieldMeta: EntityModel,
    entityDelegates: EntityDelegateRegistry<GenerateDdlCommandsEntityDelegate>?,
    createForeignKeyConstraints: CreateForeignKeyConstraints
) {
    val ddlColumns = getOrNewMutableSetField(ddlTable, "columns")
    val isKey = isKeyFieldMeta(fieldMeta)
    getFieldColumns(fieldMeta, false, entityDelegates).forEach {
        populateColumn(ddlColumns, it.name, it.type)
        if (isKey) populatePrimaryKey(ddlTable, it.name)
    }
    if (createForeignKeyConstraints == CreateForeignKeyConstraints.ALL) populateForeignKeys(ddlTable, fieldMeta)
}

private fun populateAncestors(
    ddlTable: MutableEntityModel,
    createForeignKeyConstraints: CreateForeignKeyConstraints,
    ddlState: DdlState
) {
    val stackSize = ddlState.ancestorStack.size
    if (stackSize == 0) return
    var ancestorColumnsPopulated = false
    ddlState.ancestorStack.forEachIndexed { index, ancestor ->
        // The root ancestor (index == 0) has a singleton key that is needed only in immediate children (stackSize == 1)
        // to prevent the single entry in the root table from being deleted (via a foreign-key constraint) while the
        // root has children. The singleton key is of no use deeper in the tree.
        if (index == 0 && stackSize > 1) return@forEachIndexed
        if (!ancestor.isRealKeys) return@forEachIndexed
        populateAncestorColumns(ddlTable, ancestor)
        ancestorColumnsPopulated = true
    }
    val parent = ddlState.ancestorStack.last()
    if (!ancestorColumnsPopulated) populateAncestorColumns(ddlTable, parent)
    if (createForeignKeyConstraints == CreateForeignKeyConstraints.ALL) populateParentForeignKey(ddlTable, parent)
}

private fun populateSelfReferentialAncestor(
    ddlTable: MutableEntityModel,
    ancestor: TableKeys,
    createForeignKeyConstraints: CreateForeignKeyConstraints
) {
    populateAncestorColumns(ddlTable, ancestor)
    if (createForeignKeyConstraints == CreateForeignKeyConstraints.ALL) populateParentForeignKey(ddlTable, ancestor)
}

private fun populateAncestorColumns(ddlTable: MutableEntityModel, ancestor: TableKeys) {
    val prefix = if (ancestor.isRealKeys) "${ancestor.tableName}__" else ""
    val ddlColumns = getOrNewMutableSetField(ddlTable, "columns")
    ancestor.keys.forEach { keyColumn ->
        val columnName = "$prefix${keyColumn.name}"
        populateColumn(ddlColumns, columnName, keyColumn.type)
    }
}

private fun populateParentForeignKey(ddlTable: MutableEntityModel, parent: TableKeys) {
    val sourcePrefix = if (parent.isRealKeys) "${parent.tableName}__" else ""
    val sourceColumns = parent.keys.map { "$sourcePrefix${it.name}" }
    val targetTableName = parent.tableName
    val targetColumns = parent.keys.map { it.name }
    val foreignKeyName = sourceColumns.first()

    val ddlForeignKeys = getOrNewMutableSetField(ddlTable, "foreign_keys")
    val ddlForeignKey = getOrNewNamedEntity(ddlForeignKeys, foreignKeyName)
    val ddlSourceColumns = getOrNewMutableListField(ddlForeignKey, "source_columns")
    if (!ddlSourceColumns.isEmpty()) return

    sourceColumns.forEach { addStringListFieldElement(ddlSourceColumns, it) }
    setStringSingleField(ddlForeignKey, "target_table", targetTableName)
    val ddlTargetKeys = getOrNewMutableListField(ddlForeignKey, "target_keys")
    targetColumns.forEach { addStringListFieldElement(ddlTargetKeys, it) }
}

private fun populatePrimaryKey(ddlTable: MutableEntityModel, columnName: String) {
    val ddlPrimaryKey = getOrNewMutableListField(ddlTable, "primary_key")
    addStringListFieldElement(ddlPrimaryKey, columnName)
}

fun populateUniqueIndex(
    ddlTable: MutableEntityModel,
    compositionMeta: EntityModel,
    uniqueElementMeta: ElementModel,
    entityDelegates: EntityDelegateRegistry<GenerateDdlCommandsEntityDelegate>?
) {
    val uniqueMeta = uniqueElementMeta as EntityModel
    val uniqueName = getMetaName(uniqueMeta)
    val ddlIndexes = getOrNewMutableSetField(ddlTable, "indexes")
    val ddlIndex = getOrNewNamedEntity(ddlIndexes, uniqueName)
    setBooleanSingleField(ddlIndex, "is_unique", true)
    val ddlIndexColumns = getOrNewMutableListField(ddlIndex, "columns")
    getFieldsMeta(uniqueMeta).values.forEach { uniqueFieldElementMeta ->
        val uniqueFieldMeta = uniqueFieldElementMeta as PrimitiveModel
        val entityFieldMeta = getFieldMeta(compositionMeta, uniqueFieldMeta.value as String)
        val prefix = if (isAssociationFieldMeta(entityFieldMeta)) "${getMetaName(entityFieldMeta)}__" else ""
        getFieldColumns(entityFieldMeta, true, entityDelegates).forEach {
            addStringListFieldElement(ddlIndexColumns, "${prefix}${it.name}")
        }
    }
}

private fun populateKeylessParentUniqueIndex(
    ddlTable: MutableEntityModel,
    tableName: String,
    ddlState: DdlState
): TableKeys {
    val parent = ddlState.ancestorStack.lastOrNull() ?: throw IllegalStateException("No ancestors")
    val prefix = if (parent.isRealKeys) parent.tableName else ""
    val inheritedKeys = parent.keys.map { it.cloneWithNamePrefix(prefix) }
    val indexName = inheritedKeys.firstOrNull()?.name ?: throw IllegalStateException("No keys in any ancestor")

    val ddlIndexes = getOrNewMutableSetField(ddlTable, "indexes")
    val ddlIndex = getOrNewNamedEntity(ddlIndexes, indexName)
    val ddlIndexColumns = getOrNewMutableListField(ddlIndex, "columns")
    if (ddlIndexColumns.isEmpty()) {
        setBooleanSingleField(ddlIndex, "is_unique", true)
        inheritedKeys.forEach { addStringListFieldElement(ddlIndexColumns, it.name) }
    }

    return TableKeys(tableName, inheritedKeys, false)
}

private fun populateForeignKeys(ddlTable: MutableEntityModel, fieldMeta: EntityModel) {
    if (!isAssociationFieldMeta(fieldMeta) || isListFieldMeta(fieldMeta)) return
    val fieldName = getMetaName(fieldMeta)
    val columns = getAssociationFieldColumns(fieldMeta, true)
    val foreignKeyName = "${fieldName}__${columns.first().name}"

    val targetEntityMeta = getMetaModelResolved(fieldMeta)?.associationMeta?.targetEntityMeta
        ?: throw IllegalStateException("Association meta-model is not resolved")
    val targetAux = getMySqlMetaModelMap(targetEntityMeta)
    val targetTableName = targetAux?.validated?.tableName
        ?: throw IllegalStateException("Association target entity my_sql_ aux is not validated")

    val ddlForeignKeys = getOrNewMutableSetField(ddlTable, "foreign_keys")
    val ddlForeignKey = getOrNewNamedEntity(ddlForeignKeys, foreignKeyName)
    setStringSingleField(ddlForeignKey, "target_table", targetTableName)
    val ddlSourceColumns = getOrNewMutableListField(ddlForeignKey, "source_columns")
    val ddlTargetKeys = getOrNewMutableListField(ddlForeignKey, "target_keys")
    columns.forEach {
        addStringListFieldElement(ddlSourceColumns, "${fieldName}__${it.name}")
        addStringListFieldElement(ddlTargetKeys, it.name)
    }
}

private fun getDdlTable(ddlRoot: MutableEntityModel, databaseName: String, tableFullName: String): MutableEntityModel {
    val ddlDatabases = getOrNewMutableSetField(ddlRoot, "databases")
    val ddlDatabase = getOrNewNamedEntity(ddlDatabases, databaseName)
    val ddlTables = getOrNewMutableSetField(ddlDatabase, "tables")
    val ddlTable = getOrNewNamedEntity(ddlTables, tableFullName)
    populateTreeWareColumns(ddlTable)
    return ddlTable
}

private fun populateTreeWareColumns(ddlTable: MutableEntityModel) {
    val ddlColumns = getOrNewMutableSetField(ddlTable, "columns")
    if (!ddlColumns.isEmpty()) return
    populateColumn(ddlColumns, CREATED_ON_COLUMN_NAME, CREATED_ON_COLUMN_TYPE)
    populateColumn(ddlColumns, UPDATED_ON_COLUMN_NAME, UPDATED_ON_COLUMN_TYPE)
    populateColumn(ddlColumns, FIELD_PATH_COLUMN_NAME, FIELD_PATH_COLUMN_TYPE)
}

private fun populateColumn(ddlColumns: MutableSetFieldModel, columnName: String, columnType: String) {
    val ddlColumn = getOrNewNamedEntity(ddlColumns, columnName)
    setStringSingleField(ddlColumn, "type", columnType)
}

private fun getOrNewNamedEntity(setField: MutableSetFieldModel, entityName: String): MutableEntityModel {
    // TODO(cleanup): need a getOrNew() method with key values as parameters.
    val newEntity = getNewMutableSetEntity(setField)
    setStringSingleField(newEntity, "name", entityName)
    val oldEntity = setField.getValueMatching(newEntity) as MutableEntityModel?
    return if (oldEntity != null) oldEntity else {
        setField.addValue(newEntity)
        newEntity
    }
}