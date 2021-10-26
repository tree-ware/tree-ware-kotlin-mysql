package org.treeWare.mysql.aux

import org.treeWare.model.decoder.stateMachine.AbstractDecodingStateMachine
import org.treeWare.model.decoder.stateMachine.AuxDecodingStateMachine
import org.treeWare.model.decoder.stateMachine.DecodingStack

class MysqlMetaModelMapStateMachine(
    private val stack: DecodingStack
) : AuxDecodingStateMachine, AbstractDecodingStateMachine(true) {
    private var aux: MysqlMetaModelMap? = null

    override fun newAux() {
        aux = null
    }

    override fun getAux(): MysqlMetaModelMap? {
        return aux
    }

    override fun decodeStringValue(value: String): Boolean = when (keyName) {
        MYSQL_META_MODEL_MAP_CODEC_TABLE_NAME -> {
            aux?.tableName = value
            true
        }
        else -> false
    }

    override fun decodeObjectStart(): Boolean {
        aux = MysqlMetaModelMap()
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
