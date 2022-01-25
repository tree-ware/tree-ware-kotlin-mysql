---
title: "Database and Tables"
layout: "titled"
nav_order: "b"
parent: "Contributor Docs"
---

{% include toc.md %}

# Introduction

The `createDatabase()` operator creates the database and tables in MySQL to store tree-ware models.

# Meta-Model Aux Data

`MySqlMetaModelAuxPlugin` handles the `my_sql_` aux data in the meta-model.

# Tables

When targeting MySQL, all instances of an entity type are stored in a single table.

If entity instances are unique only under their parent, then parent paths need to be part of the primary-keys. These
parent paths can be long when the meta-model is deeply nested or when there are recursive entities in the parent path.
These paths contain user data, and recursive entities make the length of these paths unbounded. Therefore, parent paths
cannot be a part of the primary-key when targeting MySQL. Instead, all entities must have system-wide-unique keys when
targeting MySQL.

While the parent path cannot be part of the primary-key, it can be (and is) stored in a regular JSON column.

The immediate parent of each entity is always stored in the table and indexed to help list all children of a parent. If
the immediate parent is a recursive entity, then the last entity in the recursion is stored as the parent. 

Other ancestors in the parent path can be optionally included in the table and indexed to help list all entities under
that ancestor. The ancestors to include and index are specified in the aux data in the meta-model.

TODO: how are the ancestors specified in the aux data?

TODO: when there are recursive entities in the parent path, we would have to
use [CTE](https://dev.mysql.com/doc/refman/8.0/en/with.html). Until that is supported, validation should prevent
including an ancestor that is recursive.