---
title: "Set Models"
layout: "titled"
nav_order: "c"
parent: "Contributor Docs"
---

{% include toc.md %}

# MySqlSetDelegate

The [`set()` operator](http://www.tree-ware.org/tree-ware-kotlin-core/contributor/set-models.html) in the tree-ware core
library uses a DB-specific delegate to set models in the DB. `MySqlSetDelegate` is a delegate for setting a model in
MySQL. The delegate programmatically issues SQL commands in a transaction to set the model in the DB.

## UPSERT

Since tree-ware stores all instances of an entity in a single table and requires system-wide-unique
keys [when targeting MySQL](database-and-tables.md#Tables), it can lead to issues if UPSERT is used to set data. It is
possible for a user to accidentally or maliciously set entities that the user does not have access to. The RBAC layer
will not catch this if the accidental/malicious access is a descendant of an ancestor the user has access to. An example
will help clarify this:

Consider the following paths where the alphabets are entity names and the numbers are (system-wide-unique) key values.
Assume that the following paths already exist.

1. `/a[1]/b[1]`
2. `/a[2]/b[2]`

Assume the user only has access to `/a[2]` and its sub-tree, and they attempt to set `/a[2]/b[1]`. But `b[1]` already
exists and belongs to `/a[1]` which the user does not have access to. So this attempt by the user must fail.

The RBAC layer does not reject the set request since the user has access to `/a[2]` and the RBAC layer does not check
beyond that for performance reasons. So the DB layer must catch/prevent this issue.

If the set-request is implemented as an UPSERT, then it will update `b[1]` since it exists. But this is not desirable
since the user is not supposed to have access to `b[1]`. So UPSERT must not be used.

## INSERT & UPDATE

INSERT will create (insert) an entity only if it does not exist. If it does not exist, it cannot belong to another
sub-tree, and so it is safe to create (insert) it in the sub-tree in the set-request. If it exists, then INSERT results
in an error, irrespective of which sub-tree it is in. So INSERT works as desired.

UPDATE will update an en entity only if it exists. The WHERE clause can be used to ensure that the entity being updated
has an `entity_path_` column value that matches the entity path in the request. If they don't match, the entity in the
DB will not get updated. This can be caught using `SELECT ROW_COUNT()` and the transaction can be rolled back. So UPDATE
also works as desired.

Therefore `MySqlSetDelegate` uses INSERT and UPDATE instead of UPSERT to set the models.

## DELETE

Entities are deleted using `DELETE` commands. Like in the case of updates, a `WHERE` clause on the `entity_path_` is
used for ensuring that the entity being deleted matches the entity path in the request. Tree-ware creates foreign-key
restrictions for compositions and associations, so deletes will fail if the entity being deleted has a sub-tree or if it
is the target of an association.

# Roadmap

* Support recursive deletion using `"set_": "delete_recursive"` aux value, and a corresponding permission.
    * Should handle compositions as well as associations.
    * Might need long-running-operation (LRO) support in tree-ware.