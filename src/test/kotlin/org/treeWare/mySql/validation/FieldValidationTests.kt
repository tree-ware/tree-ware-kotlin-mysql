package org.treeWare.mySql.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import org.treeWare.metaModel.newTestMetaModelJson
import org.treeWare.metaModel.testMetaModelCommonPackageJson
import org.treeWare.metaModel.testMetaModelCommonRootJson
import org.treeWare.mySql.aux.MySqlMetaModelMapAuxPlugin
import kotlin.test.Test

class FieldValidationTests {
    private val mySqlMetaModelMapAuxPlugin = MySqlMetaModelMapAuxPlugin("test")

    @Test
    fun `Validation must fail if string fields do not specify max_size constraint`() {
        val metaModelJson = getStringFieldMetaModelJson(true, null)
        val expectedErrors =
            listOf("String field /root/test.main/entity1/field1 must specify max_size constraint for MySQL")
        assertJsonStringValidationErrors(
            metaModelJson,
            expectedErrors,
            auxPlugins = arrayOf(mySqlMetaModelMapAuxPlugin)
        )
    }

    @Test
    fun `Validation must pass if string fields specify max_size constraint`() {
        val metaModelJson = getStringFieldMetaModelJson(true, 128)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(
            metaModelJson,
            expectedErrors,
            auxPlugins = arrayOf(mySqlMetaModelMapAuxPlugin)
        )
    }

    @Test
    fun `Validation must pass if string fields without max_size constraint are not stored in MySQL`() {
        val metaModelJson = getStringFieldMetaModelJson(false, null)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(
            metaModelJson,
            expectedErrors,
            auxPlugins = arrayOf(mySqlMetaModelMapAuxPlugin)
        )
    }
}

private fun getStringFieldMetaModelJson(isStoredInMySQL: Boolean, maxSize: Int?): String {
    val mySqlJson = if (isStoredInMySQL) "\"my_sql_\": {}," else ""
    val maxSizeJson = maxSize?.let { ", \"max_size\": $maxSize" } ?: ""
    val mainPackageJson = """
            |{
            |  "my_sql_": {
            |    "table_prefix": "main"
            |  },
            |  "name": "test.main",
            |  "entities": [
            |    {
            |      $mySqlJson
            |      "name": "entity1",
            |      "fields": [
            |        {
            |          "name": "field1",
            |          "number": 1,
            |          "type": "string"
            |          $maxSizeJson
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
    return newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, mainPackageJson)
}