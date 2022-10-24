package org.treeWare.mySql.operator.liquibase

import okio.BufferedSink
import okio.FileSystem
import okio.Path.Companion.toPath
import org.treeWare.metaModel.aux.getResolvedVersionAux
import org.treeWare.metaModel.getMainMetaName
import org.treeWare.model.core.MainModel
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.mySql.operator.CreateForeignKeyConstraints
import org.treeWare.mySql.operator.GenerateDdlCommandsEntityDelegate
import org.treeWare.mySql.operator.generateDdlChangeSets

fun generateChangeLog(
    mainMeta: MainModel,
    entityDelegates: EntityDelegateRegistry<GenerateDdlCommandsEntityDelegate>?,
    createDatabase: Boolean,
    fullyQualifyTableNames: Boolean,
    createForeignKeyConstraints: CreateForeignKeyConstraints = CreateForeignKeyConstraints.ALL
) {
    val directoryPath = getReleaseChangeLogDirectoryPath(mainMeta, GENERATED_ROOT_DIRECTORY)
    FileSystem.SYSTEM.createDirectories(directoryPath.toPath())
    val fileName = getReleaseChangeLogPath(mainMeta, GENERATED_ROOT_DIRECTORY, false)
    FileSystem.SYSTEM.write(fileName.toPath()) {
        generateChangeLog(
            this,
            mainMeta,
            entityDelegates,
            createDatabase,
            fullyQualifyTableNames,
            createForeignKeyConstraints
        )
    }
}

fun generateChangeLog(
    bufferedSink: BufferedSink,
    mainMeta: MainModel,
    entityDelegates: EntityDelegateRegistry<GenerateDdlCommandsEntityDelegate>?,
    createDatabase: Boolean,
    fullyQualifyTableNames: Boolean,
    createForeignKeyConstraints: CreateForeignKeyConstraints = CreateForeignKeyConstraints.ALL
) {
    bufferedSink.writeUtf8("-- liquibase formatted sql\n\n")
    bufferedSink.writeUtf8("-- AUTO-GENERATED FILE. DO NOT EDIT.\n\n")
    bufferedSink.writeUtf8(getMetaModelInfoComment(mainMeta)).writeUtf8("\n")

    val changeSets = generateDdlChangeSets(
        mainMeta,
        entityDelegates,
        createDatabase,
        fullyQualifyTableNames,
        createForeignKeyConstraints
    )
    changeSets.forEach { it.writeTo(bufferedSink) }
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