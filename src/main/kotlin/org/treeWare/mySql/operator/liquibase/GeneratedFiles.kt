package org.treeWare.mySql.operator.liquibase

import org.treeWare.metaModel.aux.getResolvedVersionAux
import org.treeWare.metaModel.getMetaModelName
import org.treeWare.model.core.EntityModel

private const val ROOT_CHANGE_LOG_FILE_NAME = "root.xml"
private const val RELEASE_SUBDIRECTORY = "release"

internal const val GENERATED_ROOT_DIRECTORY = "generated/liquibase"

internal fun getRootChangeLogPath(metaModel: EntityModel, basePath: String): String {
    val metaModelName = getMetaModelName(metaModel)
    return "$basePath/$metaModelName/$ROOT_CHANGE_LOG_FILE_NAME"
}

internal fun getReleaseChangeLogDirectoryPath(metaModel: EntityModel, basePath: String): String {
    val metaModelName = getMetaModelName(metaModel)
    return "$basePath/$metaModelName/$RELEASE_SUBDIRECTORY"
}

internal fun getReleaseChangeLogPath(metaModel: EntityModel, basePath: String, relativeToRootChangeLog: Boolean): String {
    val metaModelName = getMetaModelName(metaModel)
    val version = getResolvedVersionAux(metaModel)
    val directory =
        if (relativeToRootChangeLog) RELEASE_SUBDIRECTORY else getReleaseChangeLogDirectoryPath(metaModel, basePath)
    return "$directory/$metaModelName-${version.semantic}.sql"
}