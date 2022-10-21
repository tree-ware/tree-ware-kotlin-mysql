package org.treeWare.mySql.test

import java.io.StringWriter
import javax.sql.DataSource

fun getIndexesSchema(dataSource: DataSource, database: String): String {
    val writer = StringWriter()
    dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(
                """
                SELECT DISTINCT TABLE_NAME, INDEX_NAME, COLUMN_NAME, SEQ_IN_INDEX, NON_UNIQUE
                FROM INFORMATION_SCHEMA.STATISTICS
                WHERE TABLE_SCHEMA='$database'
                ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX
                """.trimIndent()
            )
            printResultSet(resultSet, writer, true)
        }
    }
    return writer.toString()
}