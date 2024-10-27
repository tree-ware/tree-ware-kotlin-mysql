package org.treeWare.mySql.test.metaModel

import org.treeWare.metaModel.ValidatedMetaModel
import org.treeWare.metaModel.getResolvedRootMeta
import org.treeWare.metaModel.newMetaModelFromJsonFiles
import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.core.defaultRootEntityFactory
import org.treeWare.mySql.aux.MySqlMetaModelMapAuxPlugin

val MY_SQL_ADDRESS_BOOK_META_MODEL_FILES = listOf(
    "tree_ware/meta_model/my_sql_address_book_root.json",
    "tree_ware/meta_model/my_sql_address_book_main.json",
    "tree_ware/meta_model/my_sql_address_book_city.json",
    "tree_ware/meta_model/my_sql_test_crypto.json",
    "tree_ware/meta_model/my_sql_test_keyless.json",
    "tree_ware/meta_model/geo.json"
)

val mySqlAddressBookMetaModel = newMySqlAddressBookMetaModel("test", null, null).metaModel
    ?: throw IllegalStateException("Meta-model has validation errors")

val mySqlAddressBookRootEntityMeta = getResolvedRootMeta(mySqlAddressBookMetaModel)

fun newMySqlAddressBookMetaModel(environment: String, hasher: Hasher?, cipher: Cipher?): ValidatedMetaModel =
    newMetaModelFromJsonFiles(
        MY_SQL_ADDRESS_BOOK_META_MODEL_FILES,
        false,
        hasher,
        cipher,
        ::defaultRootEntityFactory,
        listOf(MySqlMetaModelMapAuxPlugin(environment)),
        true
    )