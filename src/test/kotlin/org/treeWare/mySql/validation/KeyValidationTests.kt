package org.treeWare.mySql.validation

import org.treeWare.metaModel.*
import org.treeWare.mySql.aux.MySqlMetaModelMapAuxPlugin
import kotlin.test.Test

private const val FIELD_ID = "Package 1 entity 0 field 0"

class KeyValidationTests {
    private val mySqlMetaModelMapAuxPlugin = MySqlMetaModelMapAuxPlugin()

    @Test
    fun `Entities may have more than 1 key`() {
        val mainPackageJson = """
            |{
            |  "my_sql_": {
            |    "table_prefix": "main"
            |  },
            |  "name": "org.tree_ware.test.main",
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
            FieldType.ASSOCIATION
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
                else -> listOf("Entity /test_meta_model/org.tree_ware.test.main/entity1 key field type $fieldType is not supported for MySQL")
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
            listOf("String field /test_meta_model/org.tree_ware.test.main/entity1/key1 must specify max_size constraint for MySQL")
        assertJsonStringValidationErrors(
            metaModelJson,
            expectedErrors,
            auxPlugins = arrayOf(mySqlMetaModelMapAuxPlugin)
        )
    }

    @Test
    fun `Validation must pass for composition keys with supported leaves`() {
        val supportedLeafFieldTypes = listOf(
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
            FieldType.STRING,
            FieldType.UUID
        )
        supportedLeafFieldTypes.forEach { fieldType ->
            val maxSize = if (fieldType == FieldType.STRING) 128 else null
            val targetFieldsJson = getLeafFieldJson("leaf1", 1, fieldType, maxSize)
            val metaModelJson = getCompositionKeyMetaModelJson(targetFieldsJson)
            val expectedErrors = emptyList<String>()
            assertJsonStringValidationErrors(
                metaModelJson,
                expectedErrors,
                auxPlugins = arrayOf(mySqlMetaModelMapAuxPlugin)
            )
        }
    }

    @Test
    fun `Validation must pass for composition keys targeting entities without MySQL aux`() {
        val metaModelJson = getTypedKeyMetaModelJson(FieldType.COMPOSITION)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(
            metaModelJson,
            expectedErrors,
            auxPlugins = arrayOf(mySqlMetaModelMapAuxPlugin)
        )
    }

    @Test
    fun `Validation must fail for composition keys with unsupported leaves`() {
        val unsupportedLeafFieldTypes = listOf(
            FieldType.BLOB,
            FieldType.PASSWORD1WAY,
            FieldType.PASSWORD2WAY,
            FieldType.ALIAS,
            FieldType.ENUMERATION,
            FieldType.ASSOCIATION
        )
        unsupportedLeafFieldTypes.forEach { fieldType ->
            val targetFieldsJson = getLeafFieldJson("leaf1", 1, fieldType)
            // The target has no my_sql_ aux so that only the composition-leaf error surfaces.
            val metaModelJson = getCompositionKeyMetaModelJson(targetFieldsJson, targetHasMySqlAux = false)
            // NOTE: tree-ware-kotlin-core does not support some types as keys,
            // and tree-ware-kotlin-mysql does not support some more types. The
            // error messages are different in these two libraries.
            val expectedErrors = when (fieldType) {
                FieldType.PASSWORD1WAY,
                FieldType.PASSWORD2WAY -> listOf("Package 1 entity 0 field 0 is a password field and they cannot be keys")
                FieldType.ASSOCIATION -> listOf("Package 1 entity 0 field 0 is an association field and they cannot be keys")
                else -> listOf("Entity /test_meta_model/org.tree_ware.test.main/entity1 key field type $fieldType is not supported for MySQL")
            }
            assertJsonStringValidationErrors(
                metaModelJson,
                expectedErrors,
                auxPlugins = arrayOf(mySqlMetaModelMapAuxPlugin)
            )
        }
    }

    @Test
    fun `Validation must pass for composition keys with multiple supported leaves`() {
        val targetFieldsJson = listOf(
            getLeafFieldJson("leaf1", 1, FieldType.UUID),
            getLeafFieldJson("leaf2", 2, FieldType.STRING, 64),
            getLeafFieldJson("leaf3", 3, FieldType.INT32)
        ).joinToString(",\n")
        val metaModelJson = getCompositionKeyMetaModelJson(targetFieldsJson)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(
            metaModelJson,
            expectedErrors,
            auxPlugins = arrayOf(mySqlMetaModelMapAuxPlugin)
        )
    }

    @Test
    fun `Validation must fail for composition keys with mixed supported and unsupported leaves`() {
        val targetFieldsJson = listOf(
            getLeafFieldJson("leaf1", 1, FieldType.UUID),
            getLeafFieldJson("leaf2", 2, FieldType.BLOB)
        ).joinToString(",\n")
        // The target has no my_sql_ aux so that only the composition-leaf error surfaces.
        val metaModelJson = getCompositionKeyMetaModelJson(targetFieldsJson, targetHasMySqlAux = false)
        val expectedErrors =
            listOf("Entity /test_meta_model/org.tree_ware.test.main/entity1 key field type BLOB is not supported for MySQL")
        assertJsonStringValidationErrors(
            metaModelJson,
            expectedErrors,
            auxPlugins = arrayOf(mySqlMetaModelMapAuxPlugin)
        )
    }

    @Test
    fun `Validation must pass for entities with scalar and composition keys`() {
        val targetFieldsJson = getLeafFieldJson("leaf1", 1, FieldType.UINT32)
        val entityExtraFieldsJson = """,
        |        {
        |          "name": "key2",
        |          "number": 2,
        |          "type": "uuid",
        |          "is_key": true
        |        }""".trimMargin()
        val metaModelJson = getCompositionKeyMetaModelJson(targetFieldsJson, entityExtraFieldsJson = entityExtraFieldsJson)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(
            metaModelJson,
            expectedErrors,
            auxPlugins = arrayOf(mySqlMetaModelMapAuxPlugin)
        )
    }

    @Test
    fun `Validation must report both target and leaf errors when target has MySQL aux`() {
        val targetFieldsJson = getLeafFieldJson("leaf1", 1, FieldType.BLOB)
        val metaModelJson = getCompositionKeyMetaModelJson(targetFieldsJson, targetHasMySqlAux = true)
        val expectedErrors = listOf(
            "Entity /test_meta_model/org.tree_ware.test.main/key_target key field type BLOB is not supported for MySQL",
            "Entity /test_meta_model/org.tree_ware.test.main/entity1 key field type BLOB is not supported for MySQL"
        )
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
        FieldType.ENUMERATION -> """"enumeration": {"name": "enumeration1", "package": "org.tree_ware.test.common"},"""
        FieldType.ASSOCIATION -> """"association": {"entity": "entity2", "package": "org.tree_ware.test.common"},"""
        FieldType.COMPOSITION -> """"composition": {"entity": "entity3", "package": "org.tree_ware.test.common"},"""
        else -> ""
    }
    val mainPackageJson = """
            |{
            |  "my_sql_": {
            |    "table_prefix": "main"
            |  },
            |  "name": "org.tree_ware.test.main",
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

private fun getCompositionKeyMetaModelJson(
    targetFieldsJson: String,
    targetHasMySqlAux: Boolean = true,
    entityExtraFieldsJson: String = ""
): String {
    val targetMySqlJson = if (targetHasMySqlAux) "\"my_sql_\": {}," else ""
    val mainPackageJson = """
            |{
            |  "my_sql_": {
            |    "table_prefix": "main"
            |  },
            |  "name": "org.tree_ware.test.main",
            |  "entities": [
            |    {
            |      $targetMySqlJson
            |      "name": "key_target",
            |      "fields": [
            |        $targetFieldsJson
            |      ]
            |    },
            |    {
            |      "my_sql_": {},
            |      "name": "entity1",
            |      "fields": [
            |        {
            |          "name": "key1",
            |          "number": 1,
            |          "type": "composition",
            |          "composition": {"entity": "key_target", "package": "org.tree_ware.test.main"},
            |          "is_key": true
            |        }$entityExtraFieldsJson
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
    return newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, mainPackageJson)
}

private fun getLeafFieldJson(name: String, number: Int, fieldType: FieldType, maxSize: Int? = null): String {
    val fieldTypeJson = fieldType.toString().lowercase()
    val typeInfoJson = when (fieldType) {
        FieldType.ENUMERATION -> """"enumeration": {"name": "enumeration1", "package": "org.tree_ware.test.common"},"""
        FieldType.ASSOCIATION -> """"association": {"entity": "entity2", "package": "org.tree_ware.test.common"},"""
        else -> ""
    }
    val maxSizeJson = maxSize?.let { ", \"max_size\": $maxSize" } ?: ""
    return """
        {
          "name": "$name",
          "number": $number,
          "type": "$fieldTypeJson",
          $typeInfoJson
          "is_key": true
          $maxSizeJson
        }
    """.trimIndent()
}

private fun getStringKeyMetaModelJson(maxSize: Int?): String {
    val maxSizeJson = maxSize?.let { ", \"max_size\": $maxSize" } ?: ""
    val mainPackageJson = """
            |{
            |  "my_sql_": {
            |    "table_prefix": "main"
            |  },
            |  "name": "org.tree_ware.test.main",
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