## Goal

Support composition keys in `tree-ware-kotlin-mysql` (fixes [#61](https://github.com/tree-ware/tree-ware-kotlin-mysql/issues/61)): flatten composition-key leaves into `<composition-key-field>__<leaf>` columns across validation, DDL, set, and get, including associations whose target entity has a composition key.

## Success Criteria

- Meta-models with composition keys validate: supported leaf types pass, unsupported leaf types fail with clear errors.
- DDL generates flattened primary-key columns; ancestor-key propagation, parent foreign keys, and keyless-parent unique indexes work with flattened keys.
- Set and get round-trip against live MySQL, including single-composition key decode, composition-keyed ancestors, and associations targeting composition-keyed entities.
- Each implementation step below is independently committable in `tree-ware-kotlin-mysql` with its own passing tests, per `AGENTS.md`.
- A PR from the changes states that it fixes #61.

## Context And Current Facts

- Core supports composition keys: a key field may be a `COMPOSITION` whose target entity has only primitive keys (enforced by `hasOnlyPrimitiveKeyFields`: all target fields are keys, none are compositions — so single-level flattening suffices, no recursion). `MutableEntityModel.getKeyFields(flatten = true)` flattens them.
- Core set/get visitors treat keys differently: `ModelPathStack` passes **flattened** keys (`getKeyFields(true)`) to `SetDelegate.setEntity`, but `GetDelegateVisitor.getCompositionSet` passes **unflattened** keys (`getKeyFields()`). Both visitors skip fetching composition-key child subtrees (`isCompositionKey`); the get visitor still creates empty response shells for them via `createResponseCompositionFields`.
- Core supports associations targeting composition-key entities at the model layer: `resolveAssociationField` places no key restrictions on targets, and `AssociationValidationVisitor` explicitly skips composition-key entities when finding the association target. Core gap (out of scope): `DecodePaths.decodeSetField` rejects composition keys ("not yet supported"); it has no callers in core/server main and MySQL never parses path strings. Core also forbids composition fields in uniques, so unique-index handling needs no composition work.
- MySQL today rejects composition keys: `validateKey` in `validation/ValidateMySqlMetaModelMap.kt` allow-lists only scalar types with an `else -> "not supported for MySQL"` branch. DDL `getFieldColumns` returns `emptyList()` for generic compositions; `getFieldColumnType` and the `SqlColumn` composition paths throw.
- MySQL column joining uses the `__` separator (`SqlColumn.addSqlColumnName`, `Column.cloneWithNamePrefix`, ancestor prefixes, table names). `__` is only ever used to build names — nothing parses it — and get decodes `ResultSet`s positionally (`columnIndex`), so `__`-joined key column names cannot confuse the get operator. (The contributor docs still mention `$` joins and `$`-suffixed tree-ware columns; the code uses `__` joins and `_`-suffixed columns — the docs are stale.)
- Two existing lookups resolve the wrong table for flattened leaves and must be fixed for this work: `MySqlSetDelegate.addAncestorKeys` uses `ancestorFirstKey.parent` (for a flattened leaf, the key-child entity, not the ancestor), and `MySqlGetDelegate.getAncestorKeyColumns` uses `getParentEntityMeta(leafMeta)` (the key-target entity meta, not the ancestor).
- `tree-ware-kotlin-mysql` is a nested git repo; commits for this work go in the MySQL repo (`git -C tree-ware-kotlin-mysql ...`) with `<AI>`-prefixed messages per `AGENTS.md`.

## Constraints And Non-goals

- Scope is `tree-ware-kotlin-mysql` main + test source only. No changes to core (the single-composition get gap is fixed MySQL-locally), proto3, elasticsearch, server, or e2e-shell.
- The `validateKey` `when` must be exhaustive: replace `else` with the remaining `FieldType` options (plus a `null` branch, since `getFieldTypeMeta` is nullable).
- Nested key columns are named `<composition-key-field>__<leaf>` with the existing `__` separator; no sibling-collision validation (the prefix provides uniqueness).
- Associations targeting composition-key entities are in scope (DDL + set flattening; get side is unaffected since associations decode from the JSON path column only).
- Non-goals: core `DecodePaths` composition-key support, skipping empty key-target tables (left as-is, see decisions), backfilling existing databases.

## Key Decisions

1. **Flatten with `__`-prefixed leaf names.** DDL, set, and get all derive the same column name for a flattened leaf: `<composition-key-field>__<leaf>`, computed from the leaf's parent chain (leaf's parent entity `isCompositionKey` → grandparent field name is the prefix). One shared helper keeps the three operators consistent. Verified safe: names are only built, never parsed, and get is positional.
2. **Exhaustive `when` in `validateKey`.** Supported scalar types → `null`; `COMPOSITION` → per-leaf `validateKey` over the target's keys (core guarantees single-level, all-primitive); `BLOB`, `PASSWORD1WAY`, `PASSWORD2WAY`, `ALIAS`, `ENUMERATION`, `ASSOCIATION`, `null` → "not supported" errors. (Core already rejects password/association keys with its own messages; those branches are defense-in-depth.)
3. **No foreign key from parent flattened key columns to the key-target table.** The key-child row's ancestor FK already points back at the parent; a second FK would create an insert-order cycle and MySQL has no deferred constraint checks (cf. the ordering note in `MySqlSetDelegate`). No code change — `populateForeignKeys` only handles associations — but the omission is deliberate and must be documented.
4. **Association FK columns flatten the same way.** Target composition keys expand to `assocField__compKeyField__leaf` source columns referencing `compKeyField__leaf` target columns; `populateForeignKeys` produces this automatically once `getAssociationFieldColumns` flattens. No extra association-target validation is needed: target entities are validated in their own right during meta-model traversal, and DDL already fails fast when a target's `my_sql_` aux is unvalidated.
5. **Leave key-target table creation as-is.** DDL still creates a table for every composition target (possibly empty, since set never writes key-child rows — core aborts those subtrees). Skipping tables for entities used exclusively as keys is an optional follow-up, not part of this work.
6. **Single-composition get is fixed MySQL-locally.** Core's get visitor never delivers composition-key values for single/root gets (they are partitioned into `requestCompositionFields` and their fetch is skipped). `MySqlGetDelegate.getEntity` additionally SELECTs the entity's composition-key leaves from meta and decodes them into the existing response shells. No core change.

## Recommended Approach

Work bottom-up: validation first (pins the contract), then DDL (pins column names in goldens), then set (golden SQL, small), then get (the bulk of the work: flatten/WHERE/decode + the two ancestor-table fixes), then a live round-trip with a dedicated fixture meta-model (do not extend the shared address-book fixtures — that would invalidate unrelated goldens), then docs, then the PR. Keep the flattened-leaf naming helper shared so DDL/set/get cannot drift.

## Work Plan

Each step is one independent commit in `tree-ware-kotlin-mysql` (commit per `AGENTS.md`: `<AI>` prefix + prompt/plan/model/time).

- **Step 0 — Validation: exhaustive `when` + composition branch.**
  Surfaces: `validation/ValidateMySqlMetaModelMap.kt` (`validateKey`, new `validateCompositionKey` reusing `validateKey` per leaf).
  Tests: extend `KeyValidationTests` — composition keys with all-supported leaves pass; `BLOB`/`ENUMERATION`/`ALIAS`/password leaves fail with the MySQL key error; association keys still surface core's error; multi-leaf and multi-key mixes.
  Depends on: nothing.

- **Step 1 — DDL: flattened key columns + association targets.**
  Surfaces: `operator/ddl/GetFieldColumns.kt` (new `getCompositionKeyFieldColumns`: `<field>__<leaf>` columns from target key metas; `getAssociationFieldColumns` flattens composition target keys the same way). `getTableKeys`, `populateField`, ancestor propagation, parent FKs, and `populateForeignKeys` pick it up with no further changes; uniques need none (core forbids compositions there).
  Tests: DDL goldens (`CreateDatabaseTests` / `GenerateChangeLogTests` patterns) — flattened PKs, keyed ancestors under composition-keyed parents, keyless children inheriting flattened ancestor columns, association FK `assoc__compKey__leaf -> compKey__leaf` mappings.
  Depends on: Step 0.

- **Step 2 — Set: leaf naming + ancestor-table fix + association clearing.**
  Surfaces: new shared helper (flattened-leaf column name + true-owner-entity walk-up past composition-key shells); `MySqlSetDelegate.addAncestorKeys` uses the walk-up; `clearAssociationColumns` flattens target keys with the helper; `getAssociationSqlColumns` names flattened leaves with the helper. Plain key INSERT/WHERE paths already receive flattened leaves from core and only need the naming helper.
  Tests: `GenerateSetCommandsTests` golden SQL — create/update/delete with composition keys, composition-keyed ancestors (expect `ancestorTable__compKey__leaf` columns), association set/clear against composition-keyed targets.
  Depends on: Step 1 (column names pinned by DDL goldens).

- **Step 3 — Get: flatten, filter, decode.**
  Surfaces: `MySqlGetDelegate` — `getCompositionSet` flattens composition request keys for SELECT/WHERE (present leaves filter, absent leaves wildcard) and decodes N leaf columns into the composition shell in `getKeyFieldsMeta` order via a new `setResponseCompositionKeyField`; `getEntity` additionally SELECTs/decodes composition-key leaves from meta (decision 6); `getAncestorKeyColumns` uses the Step 2 walk-up fix.
  Tests: live-DB get tests following `GetTests` (`integrationTest` tag) — set-fetch with composition keys in sets and single compositions, filtered vs wildcard key leaves, composition-keyed ancestors.
  Depends on: Steps 1–2. Runtime-independent of Step 2, but logically after it.

- **Step 4 — Integration round-trip + docs.**
  Surfaces: dedicated composition-key fixture meta-model + round-trip test (create DB → set → get → assert, incl. associations to composition-keyed targets); update `docs/contributor/database-and-tables.md` (flattening scheme, naming, no-FK decision, empty-table note; fix the stale `$`-separator mentions it touches).
  Tests: new `integrationTest`-tagged round-trip test; default `./gradlew test` still excludes it.
  Depends on: Steps 0–3.

- **Step 5 — PR.**
  Surfaces: PR from the `support-composition-keys` branch (the Step 0–4 commits) against `tree-ware-kotlin-mysql` `master`; description states that it fixes #61 and summarizes the flattening scheme + the no-FK decision.
  Validation: CI green; `git -C tree-ware-kotlin-mysql log` shows only the Step 0–4 commits on the branch.
  Depends on: Steps 0–4.

## Validation Plan

- Per step: `cd tree-ware-kotlin-mysql && ./gradlew test` (default excludes the `integrationTest` tag); Steps 3–4 additionally run with integration tests included against a local MySQL.
- Highest-risk checks: Step 1's PK/ancestor goldens (pin the naming contract everything else builds on) and Step 3's single-composition key decode (the only behavior with no core-provided scaffold).
- After each step: `git -C tree-ware-kotlin-mysql status --short` shows only intended files; commit message follows `AGENTS.md` (`<AI>` prefix + prompt/plan/model/time).

## Risks / Rollback

- **Naming drift between DDL/set/get**: mitigated by the single shared leaf-naming helper; goldens in Steps 1–2 pin both sides.
- **Ancestor-table fix touches existing scalar-key paths**: the walk-up must be a no-op for non-flattened leaves; existing set/get tests + goldens guard this.
- **Pathological `__` collision** (a sibling field literally named e.g. `city__name` alongside flattened `city__name`): accepted per the prefix-instead-of-validation decision; DDL's `getOrNewSetFieldEntity` would merge such columns. Documented here, not validated.
- **Empty key-target tables**: harmless (never written, never referenced); optional follow-up to skip them.
- Each step is a separate commit, so rollback is `git -C tree-ware-kotlin-mysql revert <step-commit>` without affecting earlier steps.

## Open Questions

- None blocking. Answered during planning: core does support associations targeting composition-key entities (`AssociationValidationVisitor` handles them; only the caller-less `DecodePaths` rejects composition keys — out of scope); the `__` separator is safe for the get operator (build-only names, positional decode); issue #61 ("Support composition keys", opened Mar 31, 2022, labeled `roadmap`) prescribes exactly this flattening approach.
