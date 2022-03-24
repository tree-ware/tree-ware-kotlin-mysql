---
title: Database and Tables layout: titled nav_order: 1 parent: User Docs
---

{% include toc.md %}

# Introduction

The tree-ware MySQL library helps create a database and tables in MySQL to store tree-ware models.

# Meta-Model Aux Data

The `my_sql_` aux data in the meta-model indicates which packages and entities need to be stored in MySQL. The database
and table names are derived automatically from the package and entity names. If the derived names are longer than that
supported by MySQL, meta-model validation will fail. Shorter names can be specified using the following fields
in `my_sql_` aux data: `table_prefix` for packages and `table_name` for entities.

# Spatial Columns

Spatial columns are supported via a [geo meta-model][geo meta-model] in the core tree-ware library and
[operator delegates][operator delegates] in the mysql tree-ware library. Currently, only a latitude/longitude point
entity is supported. Use this point entity as the field type (composition), and call `registerMySqlOperatorDelegates()`
at startup to register the necessary operator delegates.

[geo meta-model]: https://github.com/tree-ware/tree-ware-kotlin-core/blob/master/src/commonMain/resources/org/treeWare/metaModel/geo.json
[operator delegates]: http://www.tree-ware.org/tree-ware-kotlin-core/contributor/operator-delegates.html