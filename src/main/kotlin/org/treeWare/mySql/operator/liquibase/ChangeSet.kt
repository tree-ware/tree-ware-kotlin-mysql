package org.treeWare.mySql.operator.liquibase

import okio.BufferedSink
import org.treeWare.mySql.util.SQL_COMMENT_START

interface ChangeSet {
    val author: String
    val id: String

    val commands: List<String>
    val rollbackCommands: List<String>

    fun add(command: String, rollbackCommand: String): ChangeSet
    fun writeTo(bufferedSink: BufferedSink)
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

    override fun writeTo(bufferedSink: BufferedSink) {
        bufferedSink
            .writeUtf8("\n")
            .writeUtf8(SQL_COMMENT_START)
            .writeUtf8("changeset ")
            .writeUtf8(author)
            .writeUtf8(":")
            .writeUtf8(id)
            .writeUtf8("\n")
        commands.forEach { bufferedSink.writeUtf8(it).writeUtf8("\n") }
        rollbackCommands.forEach { rollbackCommand ->
            // Every line in a multi-line rollback command must start with `-- rollback `
            rollbackCommand.split("\n").forEach {
                bufferedSink.writeUtf8(SQL_COMMENT_START).writeUtf8("rollback ").writeUtf8(it).writeUtf8("\n")
            }
        }
    }
}