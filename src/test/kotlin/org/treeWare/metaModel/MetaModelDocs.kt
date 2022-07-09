package org.treeWare.metaModel

import org.treeWare.metaModel.encoder.encodeDot
import kotlin.test.Test

// TODO(deepak-nulu): make doc generation a gradle task

class MetaModelDocs {
    @Test
    fun `Generate MySQL address-book meta-model docs`() {
        encodeDot(mySqlAddressBookMetaModel)
    }
}