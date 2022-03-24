package org.treeWare.mySql.test

import java.io.StringWriter
import java.sql.Connection

fun getColumnsSchema(connection: Connection, database: String): String {
    val writer = StringWriter()
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery(
        """
        SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA='$database'
        ORDER BY TABLE_NAME, COLUMN_NAME
        """.trimIndent()
    )
    printResultSet(resultSet, writer, true)
    statement.close()
    return writer.toString()
}