package org.treeWare.mySql.operator.liquibase

import org.treeWare.metaModel.aux.getResolvedVersionAux
import org.treeWare.metaModel.getMainMetaName
import org.treeWare.model.core.MainModel

private const val ROOT_CHANGE_LOG_FILE_NAME = "root.xml"
private const val RELEASE_SUBDIRECTORY = "release"

internal const val GENERATED_ROOT_DIRECTORY = "generated/liquibase"

internal fun getRootChangeLogPath(mainMeta: MainModel, basePath: String): String {
    val metaModelName = getMainMetaName(mainMeta)
    return "$basePath/$metaModelName/$ROOT_CHANGE_LOG_FILE_NAME"
}

internal fun getReleaseChangeLogDirectoryPath(mainMeta: MainModel, basePath: String): String {
    val metaModelName = getMainMetaName(mainMeta)
    return "$basePath/$metaModelName/$RELEASE_SUBDIRECTORY"
}

internal fun getReleaseChangeLogPath(mainMeta: MainModel, basePath: String, relativeToRootChangeLog: Boolean): String {
    val metaModelName = getMainMetaName(mainMeta)
    val version = getResolvedVersionAux(mainMeta)
    val directory =
        if (relativeToRootChangeLog) RELEASE_SUBDIRECTORY else getReleaseChangeLogDirectoryPath(mainMeta, basePath)
    return "$directory/$metaModelName-${version.semantic}.sql"
}