package org.treeWare.mySql.operator.liquibase

import org.treeWare.model.core.MainModel
import java.io.File

private val changeLogXmlHeader = """
<?xml version="1.0" encoding="UTF-8"?>

<!-- AUTO-GENERATED FILE. DO NOT EDIT. -->

<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.3.xsd">
""".trim()

private const val changeLogXmlFooter = "</databaseChangeLog>"

fun generateRootChangeLog(mainMeta: MainModel, officialDirectoryPath: String) {
    val currentChangeLog = getReleaseChangeLogPath(mainMeta, GENERATED_ROOT_DIRECTORY, true)

    val changeLogs = mutableSetOf(currentChangeLog)
    // TODO(deepak-nulu): add release change-logs from `officialDirectoryPath` to the above set.
    // TODO(deepak-nulu): convert set to list and sort in ascending semantic-version order.

    val rootChangeLog = getRootChangeLogPath(mainMeta, GENERATED_ROOT_DIRECTORY)
    File(rootChangeLog).bufferedWriter().use { writer ->
        writer.appendLine(changeLogXmlHeader)
        changeLogs.forEach { changeLogRelativePath ->
            writer.append("    <include file='")
            writer.append(changeLogRelativePath)
            writer.appendLine("' relativeToChangelogFile='true'/>")
        }
        writer.append(changeLogXmlFooter)
    }
}