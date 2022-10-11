package org.treeWare.mySql.operator.liquibase

import org.treeWare.metaModel.aux.getResolvedVersionAux
import org.treeWare.metaModel.getMainMetaName
import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.mySql.operator.CreateForeignKeyConstraints
import org.treeWare.mySql.operator.GenerateDdlCommandsEntityDelegate
import org.treeWare.mySql.operator.generateDdlChangeSets
import java.io.File
import java.io.Writer


fun generateChangeLog(
    mainMeta: MainModel,
    entityDelegates: EntityDelegateRegistry<GenerateDdlCommandsEntityDelegate>?,
    createDatabase: Boolean,
    createForeignKeyConstraints: CreateForeignKeyConstraints = CreateForeignKeyConstraints.ALL
) {
    val directoryPath = getReleaseChangeLogDirectoryPath(mainMeta, GENERATED_ROOT_DIRECTORY)
    File(directoryPath).mkdirs()
    val fileName = getReleaseChangeLogPath(mainMeta, GENERATED_ROOT_DIRECTORY, false)
    File(fileName).bufferedWriter().use { writer ->
        generateChangeLog(writer, mainMeta, entityDelegates, createDatabase, createForeignKeyConstraints)
    }
}

fun generateChangeLog(
    writer: Writer,
    mainMeta: MainModel,
    entityDelegates: EntityDelegateRegistry<GenerateDdlCommandsEntityDelegate>?,
    createDatabase: Boolean,
    createForeignKeyConstraints: CreateForeignKeyConstraints = CreateForeignKeyConstraints.ALL
) {
    writer.appendLine("-- liquibase formatted sql")
    writer.appendLine()
    writer.appendLine("-- AUTO-GENERATED FILE. DO NOT EDIT.")
    writer.appendLine()
    writer.appendLine(getMetaModelInfoComment(mainMeta))

    val changeSets = generateDdlChangeSets(mainMeta, entityDelegates, createDatabase, createForeignKeyConstraints)
    changeSets.forEach { it.writeTo(writer) }
}

private fun getMetaModelInfoComment(mainMeta: MainModel): String {
    val version = getResolvedVersionAux(mainMeta)
    val builder = StringBuilder("-- Meta-model version: ")
        .append(getMainMetaName(mainMeta))
        .append(" ")
        .append(version.semantic)
    version.name?.also { builder.append(" ").append(it) }
    return builder.toString()
}