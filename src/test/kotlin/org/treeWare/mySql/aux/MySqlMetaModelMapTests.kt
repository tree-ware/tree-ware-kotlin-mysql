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
        MY_SQL_ADDRESS_BOOK_META_MODEL_FILES.forEach { file ->
            testRoundTrip(
                file,
                multiAuxEncoder = MultiAuxEncoder(
                    MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME to MySqlMetaModelMapEncoder()
                ),
                multiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(
                    MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME to { MySqlMetaModelMapStateMachine(it) }
                ),
                metaModel = metaMetaModel
            )
        }
    }
}
