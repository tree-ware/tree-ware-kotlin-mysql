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

# Primary Key

If entity instances are unique only under their parent, then parent paths need to be part of the primary-keys. These
parent paths can be long when the meta-model is deeply nested or when there are recursive entities in the parent path.
These paths contain user data, and recursive entities make the length of these paths unbounded. Therefore, parent paths
cannot be a part of the primary-key when targeting MySQL. Instead, all entities must have system-wide-unique keys when
targeting MySQL.

# Columns

The following columns are stored for each entity:

* All non-composition fields in the entity are stored in separate columns.
* The PRIMARY KEY is made up of only the key field columns.
* In order to help list all entities under an immediate parent, the keys of the immediate parent entity need to be
  stored. An entity might be composed by different entities with different types of keys. To handle such differences,
  the path to the parent entity needs to be stored. It is stored in a `parent_entity_path_` BLOB column.
    * Paths contain keys for all entities in the path. But since tree-ware requires system-wide-unique keys when
      targeting MySQL, only the keys for the last entity needs to be stored.
    * This parent entity column needs to be indexed in order to support listing of entities under a parent. Index keys
      are limited to 3072 bytes, so the path is stored in binary format using protobufs.
    * Since only the final entity's keys are needed, the path is stored as a list of field numbers of the composition
      fields along the path, followed by a binary representation of the final entity's key values, in ascending order of
      the key field numbers.
* The JSON representation of the path to the entity is stored in an `entity_path_` TEXT column.
    * Unlike the parent entity column above, this column:
        * does need to store the keys of all the entities along the path.
        * does not need to be indexed.
    * TODO: use a protobuf representation instead of a JSON representation to make it more compact.
* Association field values are paths with keys of entities along the path. Since tree-ware requires unique-keys while
  targeting MySQL, only the target entity's keys need to be stored. The target entity's keys are stored in separate
  columns. The names for these columns are created by joining the association field name and the target entity key field
  names with two underscores.

# Indexed Columns

The following columns are always indexed:

* The columns corresponding to key fields (as a result of being in the PRIMARY KEY).
* The `parent_entity_path_` BLOB column.
* The `entity_path_` TEXT column.

The following columns are indexed if specified in aux data:

* Columns corresponding to ancestors marked in `list_by_` aux data in the meta-model.
* Columns corresponding to fields that are marked for indexing in the meta-model.
    * The aux data is: `"my_sql_": { "index": true }`
    * This is typically for fields that will be constrained in the request models.
    * The constraints will be translated to WHERE clauses and indexing these columns improves query performance.

## Roadmap

* If the `list_by_` aux data in the meta-model specifies ancestors in the parent path by which entities need to be
  listed, then those ancestors are added as columns in the table and indexed to help list all entities under that
  ancestor.
    * TODO: [CTE](https://dev.mysql.com/doc/refman/8.0/en/with.html) needs to be used when there are recursive entities
      in the parent path. Until that is implemented, validation should prevent use of recursive ancestors.