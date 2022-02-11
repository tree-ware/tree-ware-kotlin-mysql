package org.treeWare.metaModel

import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.mySql.aux.MySqlMetaModelAuxPlugin

val MY_SQL_ADDRESS_BOOK_META_MODEL_FILES = listOf(
    "metaModel/my_sql_address_book_root.json",
    "metaModel/my_sql_address_book_main.json",
    "metaModel/my_sql_address_book_city.json",
)

fun newMySqlAddressBookMetaModel(environment: String, hasher: Hasher?, cipher: Cipher?): ValidatedMetaModel =
    newMetaModelFromJsonFiles(
        MY_SQL_ADDRESS_BOOK_META_MODEL_FILES,
        false,
        hasher,
        cipher,
        listOf(MySqlMetaModelAuxPlugin(environment)),
        true
    )