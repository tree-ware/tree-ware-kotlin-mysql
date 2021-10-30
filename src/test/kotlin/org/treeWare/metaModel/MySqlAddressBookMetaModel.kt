package org.treeWare.metaModel

import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.mySql.aux.MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME
import org.treeWare.mySql.aux.MySqlMetaModelMapStateMachine

val MY_SQL_ADDRESS_BOOK_META_MODEL_FILES = listOf(
    "metaModel/address_book_root.json",
    "metaModel/address_book_main.json",
    "metaModel/address_book_city.json",
)

fun newMySqlAddressBookMetaModel(hasher: Hasher?, cipher: Cipher?): MutableMainModel =
    newMetaModelFromFiles(MY_SQL_ADDRESS_BOOK_META_MODEL_FILES, hasher, cipher, MultiAuxDecodingStateMachineFactory(
        MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME to { MySqlMetaModelMapStateMachine(it) }
    ))