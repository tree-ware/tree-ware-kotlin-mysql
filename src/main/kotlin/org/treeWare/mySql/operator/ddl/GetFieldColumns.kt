package org.treeWare.mySql.operator.ddl

import org.treeWare.metaModel.*
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.getMetaModelResolved
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.mySql.aux.getMySqlMetaModelMap
import org.treeWare.mySql.operator.GenerateDdlCommandsEntityDelegate

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
            val compositionMeta = getMetaModelResolved(fieldMeta)?.compositionMeta
            val entityFullName = getMetaModelResolved(compositionMeta)?.fullName
            val entityDelegate = entityDelegates?.get(entityFullName)
            if (entityDelegate?.isSingleColumn() != true) emptyList()
            else listOf(getSingleFieldColumn(fieldMeta, entityDelegate.getSingleColumnType()))
        }
        FieldType.ASSOCIATION -> getAssociationFieldColumns(fieldMeta, isForKeyOrUnique)
        else -> listOf(getSingleFieldColumn(fieldMeta))
    }

fun getAssociationFieldColumns(fieldMeta: EntityModel, isForKeyOrUnique: Boolean): List<Column> {
    if (isListFieldMeta(fieldMeta)) return listOf(getSingleFieldColumn(fieldMeta))
    val targetEntityMeta = getMetaModelResolved(fieldMeta)?.associationMeta?.targetEntityMeta
        ?: throw IllegalStateException("Association meta-model is not resolved")
    val targetAux = getMySqlMetaModelMap(targetEntityMeta)
    val targetTableName = targetAux?.validated?.tableName
        ?: throw IllegalStateException("Association target entity my_sql_ aux is not validated")

    val fieldName = getMetaName(fieldMeta)
    val pathColumn = Column(fieldName, "TEXT")
    val targetKeyFieldsMeta = getKeyFieldsMeta(targetEntityMeta)
    val foreignKeyColumns = targetKeyFieldsMeta.map {
        val targetColumn = getSingleFieldColumn(it)
        if (isForKeyOrUnique) targetColumn else targetColumn.cloneWithNamePrefix(fieldName)
    }

    return if (isForKeyOrUnique) foreignKeyColumns else listOf(pathColumn) + foreignKeyColumns
}

private fun getSingleFieldColumn(fieldMeta: EntityModel, columnType: String? = null): Column {
    val fieldName = getMetaName(fieldMeta)
    return Column(fieldName, columnType ?: getFieldColumnType(fieldMeta))
}