package org.treeWare.mySql.aux

import org.treeWare.model.decoder.stateMachine.AbstractDecodingStateMachine
import org.treeWare.model.decoder.stateMachine.AuxDecodingStateMachine
import org.treeWare.model.decoder.stateMachine.DecodingStack

class MySqlMetaModelMapStateMachine(
    private val stack: DecodingStack
) : AuxDecodingStateMachine, AbstractDecodingStateMachine(true) {
    private var aux: MySqlMetaModelMap? = null

    override fun newAux() {
        aux = null
    }

    override fun getAux(): MySqlMetaModelMap? {
        return aux
    }

    override fun decodeStringValue(value: String): Boolean = when (keyName) {
        MY_SQL_META_MODEL_MAP_CODEC_TABLE_PREFIX -> {
            aux?.tablePrefix = value
            true
        }
        MY_SQL_META_MODEL_MAP_CODEC_TABLE_NAME -> {
            aux?.tableName = value
            true
        }
        else -> false
    }

    override fun decodeObjectStart(): Boolean {
        aux = MySqlMetaModelMap()
        return true
    }

    override fun decodeObjectEnd(): Boolean {
        // Remove self from stack
        stack.pollFirst()
        return true
    }

    override fun decodeListStart(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }

    override fun decodeListEnd(): Boolean {
        // This method should never get called
        assert(false)
        return false
    }


}
