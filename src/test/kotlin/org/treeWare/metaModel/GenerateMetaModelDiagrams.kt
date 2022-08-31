package org.treeWare.metaModel

import org.treeWare.metaModel.encoder.encodeDot
import org.treeWare.mySql.ddlMetaModel
import kotlin.test.Test

// TODO(deepak-nulu): make doc generation a gradle task

class GenerateMetaModelDiagrams {
    @Test
    fun `Generate SQL DDL meta-model diagram`() {
        encodeDot(ddlMetaModel)
    }

    @Test
    fun `Generate MySQL address-book meta-model diagram`() {
        encodeDot(mySqlAddressBookMetaModel)
    }
}