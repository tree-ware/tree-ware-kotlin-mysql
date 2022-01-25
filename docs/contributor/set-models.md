---
title: "Set Models"
layout: "titled"
nav_order: "c"
parent: "Contributor Docs"
---

{% include toc.md %}

# SetVisitorDelegate

The `set()` operator in the tree-ware core library uses a DB-specific delegate to set models in the DB. The tree-ware
MySQL library provides a delegate to set models in MySQL. The delegate programmatically issues SQL commands in a
transaction to set the model in the DB.

## UPSERT

Since tree-ware stores all instances of an entity in a single table and requires system-wide-unique
keys [when targeting MySQL](database-and-tables.md#Tables), it can lead to issues if UPSERT is used to set data. It is
possible for a user to accidentally or maliciously set entities that the user does not have access to. The RBAC layer
will not catch this if the accidental/malicious access is a descendant of an ancestor the user has access to. An example
will help clarify this:

Consider the following paths where the alphabets are entity names and the numbers are (system-wide-unique) key values.
Assume that the following paths already exist.

1. `/a/1/b/1`
2. `/a/2/b/2`

Assume the user only has access to `/a/2` and its sub-tree, and they attempt to set `/a/2/b/1`. But `b/1` already exists
and belongs to `/a/1` which the user does not have access to. So this attempt by the user must fail.

The RBAC layer does not reject the set request since the user has access to `/a/2` and the RBAC layer does not check
beyond that for performance reasons. So the DB layer must catch/prevent this issue.

If the set-request is implemented as an UPSERT, then it will update `b/1` since it exists. But this is not desirable
since the user is not supposed to have access to `b/1`. So UPSERT must not be used.

## INSERT & UPDATE

INSERT will create (insert) an entity only if it does not exist. If it does not exist, it cannot belong to another
sub-tree, and so it is safe to create (insert) it in the sub-tree in the set-request. If it exists, then INSERT results
in an error, irrespective of which sub-tree it is in. So INSERT works as desired.

UPDATE will update an en entity only if it exists. The WHERE clause can be used to ensure that the entity being updated
has a parent-path that matches the parent path in the request. If the entity being updated has a different parent path
in the DB than that in the set-request, it will not get updated. This can be caught using `SELECT ROW_COUNT()` and the
transaction can be rolled back. So UPDATE also works as desired.

So the tree-ware MySQL `CompositionTableSetVisitorDelegate` uses INSERT and UPDATE instead of UPSERT to set the models.

## DELETE

TODO: delete the entity and all entities in the sub-tree.

TODO: how do we delete all entities in the sub-tree?