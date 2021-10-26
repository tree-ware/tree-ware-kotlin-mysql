package org.treeWare.mysql.aux

import org.treeWare.metaModel.MYSQL_ADDRESS_BOOK_META_MODEL_FILES
import org.treeWare.metaModel.newMainMetaMetaModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.testRoundTrip
import kotlin.test.Test

private val metaMetaModel = newMainMetaMetaModel()

class MysqlMetaModelMapTests {
    @Test
    fun `MySQL meta-model JSON codec round trip must be lossless`() {
        MYSQL_ADDRESS_BOOK_META_MODEL_FILES.forEach { file ->
            testRoundTrip(
                file,
                multiAuxEncoder = MultiAuxEncoder(
                    MYSQL_META_MODEL_MAP_CODEC_AUX_NAME to MysqlMetaModelMapEncoder()
                ),
                multiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(
                    MYSQL_META_MODEL_MAP_CODEC_AUX_NAME to { MysqlMetaModelMapStateMachine(it) }
                ),
                metaModel = metaMetaModel
            )
        }
    }
}
