package org.treeWare.metaModel

import org.treeWare.metaModel.encoder.encodeDot
import java.io.File
import kotlin.test.Test

// TODO(deepak-nulu): make doc generation a gradle task

class MetaModelDocs {
    @Test
    fun `Generate MySQL address-book meta-model docs`() {
        val mainMetaName = getMainMetaName(mySqlAddressBookMetaModel)
        val fileName = "${mainMetaName}_meta_model"
        val fileWriter = File("${fileName}.dot").bufferedWriter()
        encodeDot(mySqlAddressBookMetaModel, fileWriter)
        fileWriter.flush()

        Runtime.getRuntime().exec("dot -Tpng ${fileName}.dot -o ${fileName}.png").waitFor()
    }
}