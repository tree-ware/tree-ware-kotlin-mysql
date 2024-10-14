package org.treeWare.mySql.operator.liquibase

import okio.BufferedSink
import okio.FileSystem
import okio.Path.Companion.toPath
import org.treeWare.metaModel.aux.getResolvedVersionAux
import org.treeWare.metaModel.getMetaModelName
import org.treeWare.model.core.EntityModel
import org.treeWare.model.operator.EntityDelegateRegistry
import org.treeWare.mySql.operator.CreateForeignKeyConstraints
import org.treeWare.mySql.operator.GenerateDdlCommandsEntityDelegate
import org.treeWare.mySql.operator.generateDdlChangeSets

fun generateChangeLog(
    metaModel: EntityModel,
    entityDelegates: EntityDelegateRegistry<GenerateDdlCommandsEntityDelegate>?,
    createDatabase: Boolean,
    fullyQualifyTableNames: Boolean,
    createForeignKeyConstraints: CreateForeignKeyConstraints = CreateForeignKeyConstraints.ALL
) {
    val directoryPath = getReleaseChangeLogDirectoryPath(metaModel, GENERATED_ROOT_DIRECTORY)
    FileSystem.SYSTEM.createDirectories(directoryPath.toPath())
    val fileName = getReleaseChangeLogPath(metaModel, GENERATED_ROOT_DIRECTORY, false)
    FileSystem.SYSTEM.write(fileName.toPath()) {
        generateChangeLog(
            this,
            metaModel,
            entityDelegates,
            createDatabase,
            fullyQualifyTableNames,
            createForeignKeyConstraints
        )
    }
}

fun generateChangeLog(
    bufferedSink: BufferedSink,
    metaModel: EntityModel,
    entityDelegates: EntityDelegateRegistry<GenerateDdlCommandsEntityDelegate>?,
    createDatabase: Boolean,
    fullyQualifyTableNames: Boolean,
    createForeignKeyConstraints: CreateForeignKeyConstraints = CreateForeignKeyConstraints.ALL
) {
    bufferedSink.writeUtf8("-- liquibase formatted sql\n\n")
    bufferedSink.writeUtf8("-- AUTO-GENERATED FILE. DO NOT EDIT.\n\n")
    bufferedSink.writeUtf8(getMetaModelInfoComment(metaModel)).writeUtf8("\n")

    val changeSets = generateDdlChangeSets(
        metaModel,
        entityDelegates,
        createDatabase,
        fullyQualifyTableNames,
        createForeignKeyConstraints
    )
    changeSets.forEach { it.writeTo(bufferedSink) }
}

private fun getMetaModelInfoComment(metaModel: EntityModel): String {
    val version = getResolvedVersionAux(metaModel)
    val builder = StringBuilder("-- Meta-model version: ")
        .append(getMetaModelName(metaModel))
        .append(" ")
        .append(version.semantic)
    version.name?.also { builder.append(" ").append(it) }
    return builder.toString()
}