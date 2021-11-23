package org.treeWare.metaModel

import org.apache.logging.log4j.LogManager
import org.treeWare.model.core.Cipher
import org.treeWare.model.core.Hasher
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.mySql.aux.MySqlMetaModelAuxPlugin

val MY_SQL_ADDRESS_BOOK_META_MODEL_FILES = listOf(
    "metaModel/my_sql_address_book_root.json",
    "metaModel/my_sql_address_book_main.json",
    "metaModel/my_sql_address_book_city.json",
)

fun newMySqlAddressBookMetaModel(environment: String, hasher: Hasher?, cipher: Cipher?): MutableMainModel {
    val mySqlMetaModelAuxPlugin = MySqlMetaModelAuxPlugin(environment)
    val metaModel = newMetaModelFromFiles(
        MY_SQL_ADDRESS_BOOK_META_MODEL_FILES,
        hasher,
        cipher,
        MultiAuxDecodingStateMachineFactory(mySqlMetaModelAuxPlugin.auxName to mySqlMetaModelAuxPlugin.auxDecodingStateMachineFactory)
    )
    val errors = mySqlMetaModelAuxPlugin.validate(metaModel)
    if (errors.isNotEmpty()) {
        val logger = LogManager.getLogger()
        errors.forEach { logger.error(it) }
        throw IllegalStateException("Address-book meta-model MySQL map is not valid")
    }
    return metaModel
}