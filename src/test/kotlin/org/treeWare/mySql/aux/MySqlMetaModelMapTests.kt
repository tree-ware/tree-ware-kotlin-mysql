package org.treeWare.mySql.aux

import org.treeWare.metaModel.MY_SQL_ADDRESS_BOOK_META_MODEL_FILES
import org.treeWare.metaModel.newMainMetaMetaModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.testRoundTrip
import kotlin.test.Test

private val metaMetaModel = newMainMetaMetaModel()

class MySqlMetaModelMapTests {
    @Test
    fun `MySQL meta-model JSON codec round trip must be lossless`() {
        val mySqlMetaModelAuxPlugin = MySqlMetaModelAuxPlugin("test")
        MY_SQL_ADDRESS_BOOK_META_MODEL_FILES.forEach { file ->
            testRoundTrip(
                file,
                multiAuxEncoder = MultiAuxEncoder(
                    mySqlMetaModelAuxPlugin.auxName to mySqlMetaModelAuxPlugin.auxEncoder
                ),
                multiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(
                    mySqlMetaModelAuxPlugin.auxName to mySqlMetaModelAuxPlugin.auxDecodingStateMachineFactory
                ),
                metaModel = metaMetaModel
            )
        }
    }
}
