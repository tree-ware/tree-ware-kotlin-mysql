package org.treeWare.mySql.operator.delegate

import org.treeWare.util.assertInDevMode

internal enum class Preprocess { QUOTE, ESCAPE, UUID_TO_BIN, TO_HEX }

internal interface SetCommandBuilder {
    fun addColumn(isKey: Boolean, namePrefix: String?, name: String, value: Any?, preprocess: Preprocess? = null)
    fun build(): String
}

private const val BEGIN = "  ("
private const val BEGIN_LENGTH = BEGIN.length
private const val COMMA = ", "
private const val AND = " AND "

// TODO(#63) use prepared statements instead of this escape function (which currently does not even escape the value).
private fun escape(value: Any): Any = value

internal class InsertCommandBuilder(private val tableName: String) : SetCommandBuilder {
    override fun addColumn(isKey: Boolean, namePrefix: String?, name: String, value: Any?, preprocess: Preprocess?) {
        if (nameBuilder.length > BEGIN_LENGTH) {
            nameBuilder.append(COMMA)
            valueBuilder.append(COMMA)
        }
        if (namePrefix != null) nameBuilder.append(namePrefix).append("$")
        nameBuilder.append(name)
        addColumnValue(valueBuilder, value, preprocess)
    }

    override fun build(): String {
        nameBuilder.append(")")
        valueBuilder.append(")")
        return StringBuilder("INSERT INTO ")
            .appendLine(tableName)
            .appendLine(nameBuilder)
            .appendLine("  VALUES")
            .append(valueBuilder)
            .append(";")
            .toString()
    }

    private val nameBuilder = StringBuilder(BEGIN)
    private val valueBuilder = StringBuilder(BEGIN)
}

internal class UpdateCommandBuilder(private val tableName: String) : SetCommandBuilder {
    override fun addColumn(isKey: Boolean, namePrefix: String?, name: String, value: Any?, preprocess: Preprocess?) {
        val builder = if (isKey) keysBuilder else nonKeysBuilder
        val separator = if (isKey) AND else COMMA
        if (builder.isNotEmpty()) builder.append(separator)
        if (namePrefix != null) builder.append(namePrefix).append("$")
        builder.append(name)
        builder.append(" = ")
        addColumnValue(builder, value, preprocess)
    }

    override fun build(): String {
        return StringBuilder("UPDATE ")
            .appendLine(tableName)
            .append("  SET ")
            .appendLine(nonKeysBuilder)
            .append("  WHERE ")
            .append(keysBuilder)
            .append(";")
            .toString()
    }

    private val keysBuilder = StringBuilder()
    private val nonKeysBuilder = StringBuilder()
}

internal class DeleteCommandBuilder(private val tableName: String) : SetCommandBuilder {
    override fun addColumn(isKey: Boolean, namePrefix: String?, name: String, value: Any?, preprocess: Preprocess?) {
        assertInDevMode(isKey)
        if (keysBuilder.isNotEmpty()) keysBuilder.append(AND)
        if (namePrefix != null) keysBuilder.append(namePrefix).append("$")
        keysBuilder.append(name)
        keysBuilder.append(" = ")
        addColumnValue(keysBuilder, value, preprocess)
    }

    override fun build(): String {
        return StringBuilder("DELETE FROM ")
            .appendLine(tableName)
            .append("  WHERE ")
            .append(keysBuilder)
            .append(";")
            .toString()
    }

    private val keysBuilder = StringBuilder()
}

private fun addColumnValue(builder: StringBuilder, value: Any?, preprocess: Preprocess?) {
    if (value == null) {
        builder.append("NULL")
        return
    }
    when (preprocess) {
        Preprocess.QUOTE -> builder.append("'").append(value).append("'")
        Preprocess.ESCAPE -> builder.append("'").append(escape(value)).append("'")
        Preprocess.UUID_TO_BIN -> builder.append("UUID_TO_BIN(").append("'").append(escape(value)).append("')")
        Preprocess.TO_HEX -> {
            builder.append("0x")
            (value as ByteArray).forEach { builder.append("%02x".format(it)) }
        }
        null -> builder.append(value)
    }
}