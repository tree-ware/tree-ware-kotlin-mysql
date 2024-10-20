package org.treeWare.mySql.operator.liquibase

import okio.FileSystem
import okio.Path.Companion.toPath
import org.treeWare.model.core.EntityModel

/**
 * Verify that the change-logs in the generated directory match those in the official directory.
 *
 * Tree-ware generates change-logs into the `generated` directory, not into the official directory.
 * The generated change-logs need to be manually copied into the official directory. This method helps
 * ensure (on the CI/CD build machine) that the generated change-logs have indeed been copied over and
 * committed.
 *
 * This method only validates the root change-log and the change-log for hte current release.
 *
 * @return a list of errors, if any.
 */
fun validateChangeLogs(metaModel: EntityModel, officialDirectoryPath: String): List<String> {
    if (!FileSystem.SYSTEM.exists(officialDirectoryPath.toPath())) return listOf("Official directory `$officialDirectoryPath` does not exist")

    val errors = mutableListOf<String>()

    val generatedRootChangeLog = getRootChangeLogPath(metaModel, GENERATED_ROOT_DIRECTORY)
    val officialRootChangeLog = getRootChangeLogPath(metaModel, officialDirectoryPath)
    errors.addAll(validateChangeLog(generatedRootChangeLog, officialRootChangeLog))

    val generatedReleaseChangeLog = getReleaseChangeLogPath(metaModel, GENERATED_ROOT_DIRECTORY, false)
    val officialReleaseChangeLog = getReleaseChangeLogPath(metaModel, officialDirectoryPath, false)
    errors.addAll(validateChangeLog(generatedReleaseChangeLog, officialReleaseChangeLog))

    return errors
}

private fun validateChangeLog(generatedPath: String, officialPath: String): List<String> {
    val errors = mutableListOf<String>()
    val generatedContent = getFileContents(generatedPath, errors)
    val officialContent = getFileContents(officialPath, errors)
    if (generatedContent == null || officialContent == null) return errors
    if (generatedContent != officialContent) errors.add("$generatedPath and $officialPath differ")
    return errors
}

private fun getFileContents(path: String, errors: MutableList<String>): String? =
    if (FileSystem.SYSTEM.exists(path.toPath())) FileSystem.SYSTEM.read(path.toPath()) { this.readUtf8() }
    else {
        errors.add("File `$path` not found")
        null
    }