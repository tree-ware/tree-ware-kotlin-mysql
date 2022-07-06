package org.treeWare.mySql.test

import java.io.StringWriter
import javax.sql.DataSource

fun getColumnsSchema(dataSource: DataSource, database: String): String {
    val writer = StringWriter()
    val connection = dataSource.connection
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
    connection.close()
    return writer.toString()
}