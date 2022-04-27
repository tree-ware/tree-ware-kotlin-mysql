package org.treeWare.mySql.validation

import org.treeWare.metaModel.*
import org.treeWare.mySql.aux.MySqlMetaModelMapAuxPlugin
import kotlin.test.Test

private const val FIELD_ID = "Package 1 entity 0 field 0"

class KeyValidationTests {
    private val mySqlMetaModelMapAuxPlugin = MySqlMetaModelMapAuxPlugin("test")

    @Test
    fun `Entities may have more than 1 key`() {
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
            |          "number": 1,
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
            |          "number": 1,
            |          "type": "uuid",
            |          "is_key": true
            |        },
            |        {
            |          "name": "key2",
            |          "number": 2,
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

        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(
            metaModelJson,
            expectedErrors,
            auxPlugins = arrayOf(mySqlMetaModelMapAuxPlugin)
        )
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
            val metaModelJson = getTypedKeyMetaModelJson(fieldType)
            val expectedErrors = emptyList<String>()
            assertJsonStringValidationErrors(
                metaModelJson,
                expectedErrors,
                auxPlugins = arrayOf(mySqlMetaModelMapAuxPlugin)
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
            val metaModelJson = getTypedKeyMetaModelJson(fieldType)
            // NOTE: tree-ware-kotlin-core does not support some types as keys,
            // and tree-ware-kotlin-mysql does not support some more types. The
            // error messages are different in these two libraries.
            val expectedErrors = when (fieldType) {
                FieldType.PASSWORD1WAY,
                FieldType.PASSWORD2WAY -> listOf("$FIELD_ID is a password field and they cannot be keys")
                FieldType.ASSOCIATION -> listOf("$FIELD_ID is an association field and they cannot be keys")
                else -> listOf("Entity /root/test.main/entity1 key field type $fieldType is not supported for MySQL")
            }
            assertJsonStringValidationErrors(
                metaModelJson,
                expectedErrors,
                auxPlugins = arrayOf(mySqlMetaModelMapAuxPlugin)
            )
        }
    }

    @Test
    fun `Validation must pass for string keys with max_size`() {
        val metaModelJson = getStringKeyMetaModelJson(128)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(
            metaModelJson,
            expectedErrors,
            auxPlugins = arrayOf(mySqlMetaModelMapAuxPlugin)
        )
    }

    @Test
    fun `Validation must fail for string keys without max_size`() {
        val metaModelJson = getStringKeyMetaModelJson(null)
        val expectedErrors =
            listOf("String field /root/test.main/entity1/key1 must specify max_size constraint for MySQL")
        assertJsonStringValidationErrors(
            metaModelJson,
            expectedErrors,
            auxPlugins = arrayOf(mySqlMetaModelMapAuxPlugin)
        )
    }
}

private fun getTypedKeyMetaModelJson(fieldType: FieldType): String {
    val fieldTypeJson = fieldType.toString().lowercase()
    val typeInfoJson = when (fieldType) {
        FieldType.ENUMERATION -> """"enumeration": {"name": "enumeration1", "package": "test.common"},"""
        FieldType.ASSOCIATION -> """"association": {"name": "entity2", "package": "test.common"},"""
        FieldType.COMPOSITION -> """"composition": {"name": "entity3", "package": "test.common"},"""
        else -> ""
    }
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
            |          "number": 1,
            |          "type": "$fieldTypeJson",
            |          $typeInfoJson
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
            |          "number": 1,
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