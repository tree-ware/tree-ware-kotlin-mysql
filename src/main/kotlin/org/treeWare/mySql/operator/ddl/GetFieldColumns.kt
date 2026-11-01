package org.treeWare.mySql.operator.ddl

import org.treeWare.metaModel.*
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.getMetaModelResolved
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.mySql.aux.getMySqlMetaModelMap
import org.treeWare.mySql.operator.GenerateDdlCommandsEntityDelegate
import org.treeWare.mySql.util.getCompositionKeyLeafColumnName

data class Column(val name: String, val type: String) {
    fun cloneWithNamePrefix(prefix: String): Column =
        if (prefix.isEmpty()) this else Column("${prefix}__$name", type)
}

fun getFieldColumns(
    fieldMeta: EntityModel,
    isForKeyOrUnique: Boolean,
    entityDelegates: EntityDelegateRegistry<GenerateDdlCommandsEntityDelegate>?
): List<Column> =
    when (getFieldTypeMeta(fieldMeta)) {
        FieldType.COMPOSITION -> {
            // Composition keys flatten to one column per leaf of the target entity's keys.
            // Non-key compositions are stored in their own tables (or a single column for
            // single-column entity delegates).
            if (isKeyFieldMeta(fieldMeta)) getCompositionKeyFieldColumns(fieldMeta)
            else {
                val compositionMeta = getMetaModelResolved(fieldMeta)?.compositionMeta
                val entityFullName = getMetaModelResolved(compositionMeta)?.fullName
                val entityDelegate = entityDelegates?.get(entityFullName)
                if (entityDelegate?.isSingleColumn() != true) emptyList()
                else listOf(getSingleFieldColumn(fieldMeta, entityDelegate.getSingleColumnType()))
            }
        }
        FieldType.ASSOCIATION -> getAssociationFieldColumns(fieldMeta, isForKeyOrUnique)
        else -> listOf(getSingleFieldColumn(fieldMeta))
    }

fun getCompositionKeyFieldColumns(fieldMeta: EntityModel): List<Column> {
    val targetEntityMeta = getMetaModelResolved(fieldMeta)?.compositionMeta
        ?: throw IllegalStateException("Composition meta-model is not resolved")
    val fieldName = getMetaName(fieldMeta)
    return getKeyFieldsMeta(targetEntityMeta).map {
        Column(getCompositionKeyLeafColumnName(fieldName, getMetaName(it)), getFieldColumnType(it))
    }
}

fun getAssociationFieldColumns(fieldMeta: EntityModel, isForKeyOrUnique: Boolean): List<Column> {
    val targetEntityMeta = getMetaModelResolved(fieldMeta)?.associationMeta?.targetEntityMeta
        ?: throw IllegalStateException("Association meta-model is not resolved")
    val targetAux = getMySqlMetaModelMap(targetEntityMeta)
    val targetTableName = targetAux?.validated?.tableName
        ?: throw IllegalStateException("Association target entity my_sql_ aux is not validated")

    val fieldName = getMetaName(fieldMeta)
    val pathColumn = Column(fieldName, "TEXT")
    val targetKeyFieldsMeta = getKeyFieldsMeta(targetEntityMeta)
    val foreignKeyColumns = targetKeyFieldsMeta.flatMap {
        val targetColumns = if (getFieldTypeMeta(it) == FieldType.COMPOSITION) getCompositionKeyFieldColumns(it)
        else listOf(getSingleFieldColumn(it))
        if (isForKeyOrUnique) targetColumns else targetColumns.map { targetColumn ->
            targetColumn.cloneWithNamePrefix(fieldName)
        }
    }

    return if (isForKeyOrUnique) foreignKeyColumns else listOf(pathColumn) + foreignKeyColumns
}

private fun getSingleFieldColumn(fieldMeta: EntityModel, columnType: String? = null): Column {
    val fieldName = getMetaName(fieldMeta)
    return Column(fieldName, columnType ?: getFieldColumnType(fieldMeta))
}