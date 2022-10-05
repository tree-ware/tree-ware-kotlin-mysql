package org.treeWare.mySql.operator.liquibase

import org.treeWare.mySql.util.SQL_COMMENT_START
import java.io.Writer

interface ChangeSet {
    val author: String
    val id: String

    val commands: List<String>
    val rollbackCommands: List<String>

    fun add(command: String, rollbackCommand: String): ChangeSet
    fun writeTo(writer: Writer)
}

internal class MutableChangeSet(override val author: String) : ChangeSet {
    var sequenceNumber: Int = 0

    override val id: String get() = sequenceNumber.toString()

    override val commands = mutableListOf<String>()
    override val rollbackCommands = mutableListOf<String>()

    override fun add(command: String, rollbackCommand: String): MutableChangeSet {
        commands.add(command)
        rollbackCommands.add(rollbackCommand)
        return this
    }

    override fun writeTo(writer: Writer) {
        writer.appendLine()
        writer.append(SQL_COMMENT_START).append("changeset ").append(author).append(":").appendLine(id)
        commands.forEach { writer.appendLine(it) }
        rollbackCommands.forEach { rollbackCommand ->
            // Every line in a multi-line rollback command must start with `-- rollback `
            rollbackCommand.split("\n").forEach {
                writer.append(SQL_COMMENT_START).append("rollback ").appendLine(it)
            }
        }
    }
}