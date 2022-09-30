---
title: "Versioning"
layout: "titled"
nav_order: "d"
parent: "Contributor Docs"
---

{% include toc.md %}

# Introduction

Tree-ware generates [Liquibase](https://www.liquibase.org) change-sets to migrate the database as the meta-model
evolves.

# Liquibase Change-Set Generation

To generate a Liquibase change-set, tree-ware must first determine what has changed between the current version of a
meta-model and its last released version. This can be done by using the difference operator on the two versions of the
meta-model. But it can be hard to determine the SQL ALTER commands from these differences. So tree-ware uses a different
approach.

Tree-ware has an operator to generate a tree-ware representation of the SQL schema required to create a database from
scratch for a meta-model. This tree-ware SQL schema representation is generated for the current version of the
meta-model as well as the last released version. These two SQL schema representations are compared using the tree-ware
difference operator, and the ALTER commands are generated from these difference. It is easier to generate ALTER commands
from these differences because they directly correspond to the DB schema.

The generated change-sets might need to be in a different change-set build/runtime than the server build/runtime. There
are at least 2 approaches for ensuring that the generated change-sets make it into the right build:

1. The change-set file is generated in a `generated/db` directory. The destination directory where they need to be for
   the change-set build is passed to tree-ware. **Tree-ware will validate that the generated change-set also exists in
   the destination directory. This helps ensure that the generated change-set is copied to the destination directory and
   committed; if it is not committed, then the CI/CD build will fail.
2. The change-sets are not committed, but are instead generated as part of the change-set build.

# Change-Set Details

* author: `tree-ware`
* ID: `<semantic-version>[/<version-name>]`
* Rollback comment is generated below the actual command
    * Whether to generate the rollback comment or not is an option to the schema generator

# Tree-Ware Server Options

The tree-ware server has a boolean `createDbOnStartup` option:

* If the option is `true`, then the tree-ware server will create the database schema on startup. Currently, this option
  cannot generate and use Liquibase change-sets; it can only create the entire schema from scratch if the schema does
  not exist already. So this option is currently useful only for tests.
* If the option is `false` (default), then the database schema must be created before the server is started. This is the
  option to use when Liquibase change-sets are to be generated and used. The change-sets need to be generated in the
  build and run before the server is started.
