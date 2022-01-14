package org.treeWare.mySql.validation

import org.treeWare.metaModel.*
import org.treeWare.mySql.aux.MySqlMetaModelAuxPlugin
import kotlin.test.Test

private const val FIELD_ID = "Package 1 entity 0 field 0"

class KeyValidationTests {
    private val mySqlMetaModelAuxPlugin = MySqlMetaModelAuxPlugin("test")

    @Test
    fun `Entities must have only 1 key`() {
        val mainPackageJson = """
            |{
            |  "my_sql_": {
            |    "table_prefix": "main"
            |  },
            |  "name": "test.main",
            |  "entities": [
            |    {
            |      "my_sql_": {},
            |      "name": "entity_with_1_key",
            |      "fields": [
            |        {
            |          "name": "key1",
            |          "type": "uuid",
            |          "is_key": true
            |        }
            |      ]
            |    },
            |    {
            |      "my_sql_": {},
            |      "name": "entity_with_2_keys",
            |      "fields": [
            |        {
            |          "name": "key1",
            |          "type": "uuid",
            |          "is_key": true
            |        },
            |        {
            |          "name": "key2",
            |          "type": "uint32",
            |          "is_key": true
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, mainPackageJson)

        val expectedErrors =
            listOf("Entity /root/test.main/entity_with_2_keys has more than 1 key; only 1 key is supported for MySQL")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors, auxPlugins = arrayOf(mySqlMetaModelAuxPlugin))
    }

    @Test
    fun `Validation must pass for supported key types`() {
        val supportedKeyFieldTypes = listOf(
            FieldType.BOOLEAN,
            FieldType.UINT8,
            FieldType.UINT16,
            FieldType.UINT32,
            FieldType.UINT64,
            FieldType.INT8,
            FieldType.INT16,
            FieldType.INT32,
            FieldType.INT64,
            FieldType.FLOAT,
            FieldType.DOUBLE,
            FieldType.BIG_INTEGER,
            FieldType.BIG_DECIMAL,
            FieldType.TIMESTAMP,
            FieldType.UUID
        )
        supportedKeyFieldTypes.forEach { fieldType ->
            val metaModelJson = getTypedKeyMetaModelJson(fieldType.toString().lowercase())
            val expectedErrors = emptyList<String>()
            assertJsonStringValidationErrors(
                metaModelJson,
                expectedErrors,
                auxPlugins = arrayOf(mySqlMetaModelAuxPlugin)
            )
        }
    }

    @Test
    fun `Validation must fail for unsupported key types`() {
        val unsupportedKeyFieldTypes = listOf(
            FieldType.BLOB,
            FieldType.PASSWORD1WAY,
            FieldType.PASSWORD2WAY,
            FieldType.ALIAS,
            FieldType.ENUMERATION,
            FieldType.ASSOCIATION,
            FieldType.COMPOSITION
        )
        unsupportedKeyFieldTypes.forEach { fieldType ->
            val mainExpectedError =
                "Entity /root/test.main/entity1 key field type $fieldType is not supported for MySQL"
            val metaModelJson = getTypedKeyMetaModelJson(fieldType.toString().lowercase())
            val expectedErrors = when (fieldType) {
                FieldType.PASSWORD1WAY,
                FieldType.PASSWORD2WAY -> listOf(
                    "$FIELD_ID is a password field and they cannot be keys",
                    mainExpectedError
                )
                FieldType.ENUMERATION -> listOf("$FIELD_ID enumeration info is missing", mainExpectedError)
                FieldType.ASSOCIATION -> listOf(
                    "$FIELD_ID association info is missing",
                    "$FIELD_ID is an association field and they cannot be keys",
                    mainExpectedError
                )
                FieldType.COMPOSITION -> listOf("$FIELD_ID composition info is missing", mainExpectedError)
                else -> listOf(mainExpectedError)
            }
            assertJsonStringValidationErrors(
                metaModelJson,
                expectedErrors,
                auxPlugins = arrayOf(mySqlMetaModelAuxPlugin)
            )
        }
    }

    @Test
    fun `Validation must pass for string keys with max_size`() {
        val metaModelJson = getStringKeyMetaModelJson(128)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors, auxPlugins = arrayOf(mySqlMetaModelAuxPlugin))
    }

    @Test
    fun `Validation must fail for string keys without max_size`() {
        val metaModelJson = getStringKeyMetaModelJson(null)
        val expectedErrors =
            listOf("String field /root/test.main/entity1/key1 must specify max_size constraint for MySQL")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors, auxPlugins = arrayOf(mySqlMetaModelAuxPlugin))
    }
}

private fun getTypedKeyMetaModelJson(fieldType: String): String {
    val mainPackageJson = """
            |{
            |  "my_sql_": {
            |    "table_prefix": "main"
            |  },
            |  "name": "test.main",
            |  "entities": [
            |    {
            |      "my_sql_": {},
            |      "name": "entity1",
            |      "fields": [
            |        {
            |          "name": "key1",
            |          "type": "$fieldType",
            |          "is_key": true
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
    return newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, mainPackageJson)
}

private fun getStringKeyMetaModelJson(maxSize: Int?): String {
    val maxSizeJson = maxSize?.let { ", \"max_size\": $maxSize" } ?: ""
    val mainPackageJson = """
            |{
            |  "my_sql_": {
            |    "table_prefix": "main"
            |  },
            |  "name": "test.main",
            |  "entities": [
            |    {
            |      "my_sql_": {},
            |      "name": "entity1",
            |      "fields": [
            |        {
            |          "name": "key1",
            |          "type": "string",
            |          "is_key": true
            |          $maxSizeJson
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
    return newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, mainPackageJson)
}