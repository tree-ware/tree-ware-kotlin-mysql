package org.treeWare.mySql.test.metaModel

import org.treeWare.metaModel.ValidatedMetaModel
import org.treeWare.metaModel.getResolvedRootMeta
import org.treeWare.metaModel.newMetaModelFromJsonFiles
import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableFieldModel
import org.treeWare.mySql.aux.MySqlMetaModelMapAuxPlugin

// NOTE: these files intentionally live outside of `tree_ware/meta_model/`: the tree-ware Gradle plugin
// combines every JSON file in that directory into a single meta-model for code generation, which would
// incorrectly merge this fixture with the address-book meta-model.
val COMPOSITION_KEY_META_MODEL_FILES = listOf(
    "tree_ware/comp_key_meta_model/comp_key_test_root.json",
    "tree_ware/comp_key_meta_model/comp_key_test.json"
)

fun newCompositionKeyMetaModel(hasher: Hasher?, cipher: Cipher?): ValidatedMetaModel =
    newMetaModelFromJsonFiles(
        COMPOSITION_KEY_META_MODEL_FILES,
        false,
        hasher,
        cipher,
        ::compositionKeyRootEntityFactory,
        listOf(MySqlMetaModelMapAuxPlugin()),
        true
    )

val compositionKeyMetaModel = newCompositionKeyMetaModel(null, null).metaModel
    ?: throw IllegalStateException("Meta-model has validation errors")

val compositionKeyRootEntityMeta = getResolvedRootMeta(compositionKeyMetaModel)

fun compositionKeyRootEntityFactory(parent: MutableFieldModel?) =
    MutableEntityModel(compositionKeyRootEntityMeta, parent)
