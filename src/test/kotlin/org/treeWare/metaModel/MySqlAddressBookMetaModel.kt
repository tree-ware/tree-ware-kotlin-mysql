package org.treeWare.metaModel

import org.apache.logging.log4j.LogManager
import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.mySql.aux.MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME
import org.treeWare.mySql.aux.MySqlMetaModelMapStateMachine
import org.treeWare.mySql.validation.validateMySqlMetaModelMap

val MY_SQL_ADDRESS_BOOK_META_MODEL_FILES = listOf(
    "metaModel/address_book_root.json",
    "metaModel/address_book_main.json",
    "metaModel/address_book_city.json",
)

fun newMySqlAddressBookMetaModel(environment: String, hasher: Hasher?, cipher: Cipher?): MutableMainModel {
    val metaModel =
        newMetaModelFromFiles(MY_SQL_ADDRESS_BOOK_META_MODEL_FILES, hasher, cipher, MultiAuxDecodingStateMachineFactory(
            MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME to { MySqlMetaModelMapStateMachine(it) }
        ))
    val errors = validateMySqlMetaModelMap(environment, metaModel)
    if (errors.isNotEmpty()) {
        val logger = LogManager.getLogger()
        errors.forEach { logger.error(it) }
        throw IllegalStateException("Address-book meta-model MySQL map is not valid")
    }
    return metaModel
}