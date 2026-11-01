package org.treeWare.mySql.util

import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.getFieldTypeMeta
import org.treeWare.metaModel.getKeyFieldsMeta
import org.treeWare.metaModel.getMetaName
import org.treeWare.model.core.*

/**
 * Column name for a flattened composition-key leaf: `<composition-key-field>__<leaf>`.
 * This is the single definition of the flattened-leaf naming scheme shared by DDL, set, and get.
 */
fun getCompositionKeyLeafColumnName(compositionKeyFieldName: String, leafFieldName: String): String =
    "${compositionKeyFieldName}__${leafFieldName}"

/**
 * Column name for a key field model: flattened to `<composition-key-field>__<leaf>` when the field is a
 * composition-key leaf, else the plain field name. Safe to call for any field; a no-op for non-flattened fields.
 */
fun getFieldColumnName(field: SingleFieldModel): String {
    val parentEntity = field.parent
    return if (parentEntity != null && isCompositionKey(parentEntity)) {
        val compositionKeyField = requireNotNull(parentEntity.parent) { "Composition-key entity does not have parent field" }
        getCompositionKeyLeafColumnName(getFieldName(compositionKeyField), getFieldName(field))
    } else getFieldName(field)
}

/**
 * Column names for a key field meta: one flattened `<composition-key-field>__<leaf>` name per leaf when the
 * field is a composition key, else the single plain field name.
 */
fun getFlattenedKeyColumnNames(keyFieldMeta: EntityModel): List<String> =
    if (getFieldTypeMeta(keyFieldMeta) == FieldType.COMPOSITION) {
        val targetEntityMeta = getMetaModelResolved(keyFieldMeta)?.compositionMeta
            ?: throw IllegalStateException("Composition meta-model is not resolved")
        val fieldName = getMetaName(keyFieldMeta)
        getKeyFieldsMeta(targetEntityMeta).map { getCompositionKeyLeafColumnName(fieldName, getMetaName(it)) }
    } else listOf(getMetaName(keyFieldMeta))

/**
 * Walks up past composition-key shells to the entity that truly owns the key field.
 * A no-op for non-flattened key fields (their parent is returned as-is).
 */
fun getTrueOwnerEntity(keyField: SingleFieldModel): EntityModel {
    var entity = requireNotNull(keyField.parent) { "Key field does not have parent entity" }
    while (isCompositionKey(entity)) {
        val compositionKeyField = requireNotNull(entity.parent) { "Composition-key entity does not have parent field" }
        entity = requireNotNull(compositionKeyField.parent) { "Composition-key field does not have parent entity" }
    }
    return entity
}
