---
title: Database and Tables
layout: titled
nav_order: 1
parent: User Docs
---

{% include toc.md %}

# Introduction

The tree-ware MySQL library helps create a database and tables in MySQL to store tree-ware models.

# Meta-Model Aux Data

The `my_sql_` aux data in the meta-model indicates which packages and entities need to be stored in MySQL. The database
and table names are derived automatically from the package and entity names. If the derived names are longer than that
supported by MySQL, meta-model validation will fail. Shorter names can be specified using the following fields
in `my_sql_` aux data: `table_prefix` for packages and `table_name` for entities.

# Database

The database name is created by joining the environment name and the meta-model name, separated by two underscores.

# Tables

The table name is created by joining the `table_prefix` and `table_name` specified in the `my_sql_` aux data in the
meta-model. The two parts are separated by two underscores. If the `table_prefix` is not specified for a package, then
the package name is used in its place. Similarly, if the `table_name` is not specified for an entity, then the entity
name is used in its place.

# Columns

TODO: document the column name, and the column types for each of the tree-ware field types.