package org.treeWare.mySql.aux

import org.treeWare.metaModel.aux.MetaModelAuxPlugin
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.stateMachine.AuxDecodingStateMachineFactory
import org.treeWare.model.encoder.AuxEncoder
import org.treeWare.mySql.validation.validateMySqlMetaModelMap

class MySqlMetaModelAuxPlugin(private val environment: String) : MetaModelAuxPlugin {
    override val auxName: String = MY_SQL_META_MODEL_MAP_CODEC_AUX_NAME
    override val auxDecodingStateMachineFactory: AuxDecodingStateMachineFactory = { MySqlMetaModelMapStateMachine(it) }
    override val auxEncoder: AuxEncoder = MySqlMetaModelMapEncoder()

    override fun validate(mainMeta: MutableMainModel): List<String> = validateMySqlMetaModelMap(mainMeta, environment)
}