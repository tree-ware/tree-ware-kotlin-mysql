package org.treeWare.mySql.operator.liquibase

import okio.FileSystem
import okio.Path.Companion.toPath
import org.treeWare.model.core.EntityModel

private val changeLogXmlHeader = """
<?xml version="1.0" encoding="UTF-8"?>

<!-- AUTO-GENERATED FILE. DO NOT EDIT. -->

<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.3.xsd">
""".trim()

private const val changeLogXmlFooter = "</databaseChangeLog>"

fun generateRootChangeLog(metaModel: EntityModel, officialDirectoryPath: String) {
    val currentChangeLog = getReleaseChangeLogPath(metaModel, GENERATED_ROOT_DIRECTORY, true)

    val changeLogs = mutableSetOf(currentChangeLog)
    // TODO(deepak-nulu): add release change-logs from `officialDirectoryPath` to the above set.
    // TODO(deepak-nulu): convert set to list and sort in ascending semantic-version order.

    val rootChangeLog = getRootChangeLogPath(metaModel, GENERATED_ROOT_DIRECTORY)
    FileSystem.SYSTEM.write(rootChangeLog.toPath()) {
        this.writeUtf8(changeLogXmlHeader).writeUtf8("\n")
        changeLogs.forEach { changeLogRelativePath ->
            this.writeUtf8("    <include file='")
            this.writeUtf8(changeLogRelativePath)
            this.writeUtf8("' relativeToChangelogFile='true'/>").writeUtf8("\n")
        }
        this.writeUtf8(changeLogXmlFooter)
    }
}