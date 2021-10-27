package org.treeWare.metaModel

import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.core.MutableMainModel

val MY_SQL_ADDRESS_BOOK_META_MODEL_FILES = listOf(
    "metaModel/address_book_root.json",
    "metaModel/address_book_main.json",
    "metaModel/address_book_city.json",
)

fun newMySqlAddressBookMetaModel(hasher: Hasher?, cipher: Cipher?): MutableMainModel =
    newMetaModelFromFiles(MY_SQL_ADDRESS_BOOK_META_MODEL_FILES, hasher, cipher)
