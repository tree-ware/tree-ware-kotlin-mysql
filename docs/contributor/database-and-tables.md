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

`MySqlMetaModelAuxPlugin` handles the [`my_sql_` aux data](../user/database-and-tables.md#meta-model-aux-data) in the
meta-model.

# Example Meta-Model

The following meta-model is used as an example in the sections below:

![Example meta-model](example-meta-model.drawio.svg)

Note that sites can be nested, and devices can be at any level in the site hierarchy.

The following model (based on the above meta-model) is used as an example in the sections below:

```json
{
  "organization": {
    "id": "uuid-1",
    "name": "customer 1",
    "sites": [
      {
        "id": "uuid-1",
        "name": "Site 1",
        "devices": [
          {
            "id": "uuid-1",
            "name": "Device 1"
          },
          {
            "id": "uuid-2",
            "name": "Device 2"
          }
        ],
        "sub_sites": [
          {
            "id": "uuid-2",
            "name": "Site 1.1",
            "devices": [
              {
                "id": "uuid-3",
                "name": "Device 3"
              }
            ],
            "sub_sites": [
              {
                "id": "uuid-3",
                "name": "Site 1.1.1",
                "devices": [
                  {
                    "id": "uuid-4",
                    "name": "Device 4"
                  }
                ]
              }
            ]
          }
        ]
      }
    ]
  }
}
```

# Database

The database name is created by joining the environment name and the meta-model name, separated by two underscores.

# Tables

When targeting MySQL, all instances of an entity type are stored in a single table.

The example meta-model has 3 entity types (`organization`, `site`, `device`), so 3 tables will be created for it.

The table name is created by joining the `table_prefix` and `table_name` specified in the `my_sql_` aux data in the
meta-model. The two parts are separated by two underscores. If the `table_prefix` is not specified for a package, then
the package name is used in its place. Similarly, if the `table_name` is not specified for an entity, then the entity
name is used in its place.

# Primary Keys

If entity instances are unique only under their parent, then parent paths need to be part of the primary-keys. These
parent paths can be long when the meta-model is deeply nested or when there are recursive entities in the parent path.
These paths contain user data, and recursive entities make the length of these paths unbounded. Therefore, parent paths
cannot be a part of the primary-key when targeting MySQL. Instead, all entities must have system-wide-unique keys when
targeting MySQL. The keys have to be unique only within a specific entity type, not across entity types.

The example meta-model uses UUIDs as keys for entities. A UUID value has to be unique only within a given entity type.
That is why the example model is able to reuse `"uuid-1"` in an organization instance, in a site instance, and in a
device instance (this is for illustration purposes only and reusing UUIDs in different entity types is not recommended).

# Columns

The following columns are stored for each entity:

* All non-composition fields in the entity are stored in separate columns.
    * For example:
        * the `id`, `name`, and `parent` fields in the `organization` entity
        * the `id`, and `name` fields in the `site` entity
        * the `id`, `name`, `model`, and `sw_version` fields in the `device` entity
* The PRIMARY KEY is made up of only the key field columns.
    * For example, the `id` fields in the `organization`, `site`, and `device` entities
* The JSON representation of the path to the entity is stored in a non-indexed `entity_path_` TEXT column.
    * TODO: use a protobuf representation instead of a JSON representation to make it more compact.
* Association field values are paths with keys of entities along the path. Since tree-ware requires unique-keys while
  targeting MySQL, and since the type of the target entity is fixed in the association meta-model, only the target
  entity's keys need to be stored. The target entity's keys are stored in separate columns. The names for these columns
  are created by joining the association field name and the target entity key field names with two underscores.
    * For example, there is a `parent` association in the `organization` entity, and its target entity is
      the `organization` entity (self-referential). The `id` field is the key field in the `organization` entity. So the
      `parent` association is stored in a column called `parent__id`.

# Listing Entities

Since the model is a tree, every entity instance in the model tree has a parent path. Tree-ware supports the ability to
list all entities under a given ancestor in the parent path. Since the indexing required for this can be expensive, it
is supported by default only for the immediate parent, and not by default for the other ancestors in the path. If
listing all entities under an ancestor is desired, it must be specified in the entity's
[`list_by_` aux data](http://www.tree-ware.org/tree-ware-kotlin-core/user/meta-model-aux-data.html#list-by) in the
meta-model.

The parent for an entity is a field in an entity instance, so the parts in the parent path are fields of entity
instances. The entity instance is identified by its keys, and the field is identified by the meta-model numbers of the
field, its entity, and its package. These are combined in a protobuf and stored in an indexed BLOB column.

TBR: BLOBs require an index prefix size. Is it ok to use the maximum size possible? When targeting MySQL, most entities
will have a single UUID key, so we could set the prefix size to be just large enough for that.

The column for the immediate parent is called `parent_`. There are a few choices for how the `list_by_` ancestor columns
are named:

1. `ancestor0_`, `ancestor1_`, etc. based on the position in the parent path. An entity could be composed under
   different paths, so `ancestorN_` may not be the same entity/field across the different paths. This is not an issue
   since the columns are blobs and each blob value identifies the entity and the field.
2. Named as `<package>__<entity>_`. The blob value would have to contain only the field and the entity keys since the
   package and entity are known. This is more readable if the tables had to be debugged directly, but the column name
   could exceed the maximum size.
3. Specify a name in the `list_by_` aux data. More work for the developer. Since `list_by_` aux data is not MySQL
   specific, the name would have to be of use in the API as well.

TODO: figure out which option should be used and update this page accordingly.

## Recursive Ancestors

The above approach does not work if the ancestor is a recursive entity. In the case of recursive entities, there can be
many consecutive instances of that entity in the parent path. It is not possible to create columns for each instance
since the number of instances can vary at runtime. If the immediate parent is a recursive entity, it is still possible
to create the single `parent_` column since only the last instance needs to be stored. So the issue is for recursive
ancestors listed in the `list_by_` aux data.

The solution is to use a join-table to store the one-to-many relationship: one entity to many instances of its recursive
ancestors. The join-table has the following columns:

* `ancestor_` is the indexed BLOB column that identifies the part in the parent path. The values are as described
  earlier for non-recursive parent path parts.
* Columns for the key fields of the entity (whose parent path parts are being stored in this join-table).
    * These columns have the same names and types as their counterparts in the main entity table.
    * Values are the key field values of the entity.
    * There is no need to query this table using entity keys, so these columns are not indexed.

TBR: [CTE](https://dev.mysql.com/doc/refman/8.0/en/with.html) was suggested as a solution for nested sites. Will CTE be
more performant than using a join-table or is CTE a bunch of joins as well?

TBR: should non-recursive ancestors also be in this join-table or will it be more performant if they are part of the
main entity table?

# Indexed Columns

The following columns are always indexed:

* The columns corresponding to key fields (as a result of being in the PRIMARY KEY).

The following columns are indexed if specified in aux data:

* Columns corresponding to ancestors marked in `list_by_` aux data in the meta-model.
* Columns corresponding to fields that are marked for indexing in the meta-model.
    * The aux data is: `"my_sql_": { "index": true }`
    * This is typically for fields that will be constrained in requests.
        * Constraints are translated to WHERE clauses and indexing these columns improves query performance.

# Validations

* Identifiers/names for the following should not exceed 64 characters:
    * Database
    * Table
    * Column
    * Index
    * [Other identifiers](https://dev.mysql.com/doc/refman/8.0/en/identifier-length.html) as they get used in tree-ware
* Values of the following columns are indexed and should not exceed 3072:
    * Primary key
    * The parent path part columns in the join-table
    * Any other columns that are indexed based on meta-model aux data
* [Other limits](https://dev.mysql.com/doc/refman/8.0/en/innodb-limits.html)