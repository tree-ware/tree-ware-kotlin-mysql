-- liquibase formatted sql

-- AUTO-GENERATED FILE. DO NOT EDIT.

-- Meta-model version: address_book 1.0.0 pacific-ocean

-- changeset address_book-1.0.0:1
CREATE DATABASE IF NOT EXISTS test__address_book;
-- rollback DROP DATABASE IF EXISTS test__address_book;

-- changeset address_book-1.0.0:2
CREATE TABLE IF NOT EXISTS test__address_book.main__address_book_root (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  singleton_key_ INT UNSIGNED,
  name VARCHAR(64),
  last_updated TIMESTAMP(3),
  PRIMARY KEY (singleton_key_)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS test__address_book.main__address_book_root;

-- changeset address_book-1.0.0:3
CREATE TABLE IF NOT EXISTS test__address_book.main__address_book_settings (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  last_name_first BOOLEAN,
  encrypt_hero_name BOOLEAN,
  card_colors JSON,
  main__address_book_root__singleton_key_ INT UNSIGNED,
  UNIQUE INDEX main__address_book_root__singleton_key_ (main__address_book_root__singleton_key_)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS test__address_book.main__address_book_settings;

-- changeset address_book-1.0.0:4
CREATE TABLE IF NOT EXISTS test__address_book.main__advanced_settings (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  background_color INT UNSIGNED,
  main__address_book_root__singleton_key_ INT UNSIGNED,
  UNIQUE INDEX main__address_book_root__singleton_key_ (main__address_book_root__singleton_key_)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS test__address_book.main__advanced_settings;

-- changeset address_book-1.0.0:5
CREATE TABLE IF NOT EXISTS test__address_book.main__address_book_person (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  id BINARY(16),
  first_name VARCHAR(64),
  last_name VARCHAR(64),
  hero_name VARCHAR(64),
  email JSON,
  picture BLOB,
  self TEXT,
  self__id BINARY(16),
  main__address_book_root__singleton_key_ INT UNSIGNED,
  main__person_group__id BINARY(16),
  PRIMARY KEY (id),
  UNIQUE INDEX self (self__id)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS test__address_book.main__address_book_person;

-- changeset address_book-1.0.0:6
CREATE TABLE IF NOT EXISTS test__address_book.main__address_book_relation (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  id BINARY(16),
  relationship INT UNSIGNED,
  person TEXT,
  person__id BINARY(16),
  main__address_book_person__id BINARY(16),
  main__person_group__id BINARY(16),
  PRIMARY KEY (id)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS test__address_book.main__address_book_relation;

-- changeset address_book-1.0.0:7
CREATE TABLE IF NOT EXISTS test__address_book.crypto__password (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  current JSON,
  previous JSON,
  main__address_book_person__id BINARY(16),
  main__person_group__id BINARY(16),
  UNIQUE INDEX main__address_book_person__id (main__address_book_person__id)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS test__address_book.crypto__password;

-- changeset address_book-1.0.0:8
CREATE TABLE IF NOT EXISTS test__address_book.crypto__secret (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  main JSON,
  other JSON,
  main__address_book_person__id BINARY(16),
  main__person_group__id BINARY(16),
  UNIQUE INDEX main__address_book_person__id (main__address_book_person__id)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS test__address_book.crypto__secret;

-- changeset address_book-1.0.0:9
CREATE TABLE IF NOT EXISTS test__address_book.keyless__keyless (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  name VARCHAR(64),
  main__address_book_person__id BINARY(16),
  main__person_group__id BINARY(16),
  city__city_info__name VARCHAR(128),
  city__city_info__state VARCHAR(64),
  city__city_info__country VARCHAR(64),
  UNIQUE INDEX main__address_book_person__id (main__address_book_person__id),
  UNIQUE INDEX city__city_info__name (city__city_info__name, city__city_info__state, city__city_info__country)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS test__address_book.keyless__keyless;

-- changeset address_book-1.0.0:10
CREATE TABLE IF NOT EXISTS test__address_book.keyless__keyless_child (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  name VARCHAR(64),
  main__address_book_person__id BINARY(16),
  main__person_group__id BINARY(16),
  city__city_info__name VARCHAR(128),
  city__city_info__state VARCHAR(64),
  city__city_info__country VARCHAR(64),
  UNIQUE INDEX main__address_book_person__id (main__address_book_person__id),
  UNIQUE INDEX city__city_info__name (city__city_info__name, city__city_info__state, city__city_info__country)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS test__address_book.keyless__keyless_child;

-- changeset address_book-1.0.0:11
CREATE TABLE IF NOT EXISTS test__address_book.keyless__keyed_child (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  name VARCHAR(64),
  other INT,
  main__address_book_person__id BINARY(16),
  main__person_group__id BINARY(16),
  city__city_info__name VARCHAR(128),
  city__city_info__state VARCHAR(64),
  city__city_info__country VARCHAR(64),
  PRIMARY KEY (name)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS test__address_book.keyless__keyed_child;

-- changeset address_book-1.0.0:12
CREATE TABLE IF NOT EXISTS test__address_book.main__person_group (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  id BINARY(16),
  name VARCHAR(64),
  main__address_book_root__singleton_key_ INT UNSIGNED,
  main__person_group__id BINARY(16),
  PRIMARY KEY (id)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS test__address_book.main__person_group;

-- changeset address_book-1.0.0:13
CREATE TABLE IF NOT EXISTS test__address_book.city__city_info (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  name VARCHAR(128),
  state VARCHAR(64),
  country VARCHAR(64),
  info VARCHAR(512),
  latitude DOUBLE,
  longitude DOUBLE,
  city_center POINT SRID 4326,
  related_city_info JSON,
  self TEXT,
  self__name VARCHAR(128),
  self__state VARCHAR(64),
  self__country VARCHAR(64),
  self2 TEXT,
  self2__name VARCHAR(128),
  self2__state VARCHAR(64),
  self2__country VARCHAR(64),
  main__address_book_root__singleton_key_ INT UNSIGNED,
  PRIMARY KEY (name, state, country),
  UNIQUE INDEX coordinates (latitude, longitude),
  UNIQUE INDEX self (self__name, self__state, self__country)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS test__address_book.city__city_info;

-- changeset address_book-1.0.0:14
ALTER TABLE test__address_book.main__address_book_settings
  ADD FOREIGN KEY main__address_book_root__singleton_key_ (main__address_book_root__singleton_key_) REFERENCES main__address_book_root(singleton_key_) ON DELETE RESTRICT;
-- rollback ALTER TABLE test__address_book.main__address_book_settings
-- rollback   DROP FOREIGN KEY main__address_book_root__singleton_key_;

-- changeset address_book-1.0.0:15
ALTER TABLE test__address_book.main__advanced_settings
  ADD FOREIGN KEY main__address_book_root__singleton_key_ (main__address_book_root__singleton_key_) REFERENCES main__address_book_settings(main__address_book_root__singleton_key_) ON DELETE RESTRICT;
-- rollback ALTER TABLE test__address_book.main__advanced_settings
-- rollback   DROP FOREIGN KEY main__address_book_root__singleton_key_;

-- changeset address_book-1.0.0:16
ALTER TABLE test__address_book.main__address_book_person
  ADD FOREIGN KEY self__id (self__id) REFERENCES main__address_book_person(id) ON DELETE RESTRICT,
  ADD FOREIGN KEY main__address_book_root__singleton_key_ (main__address_book_root__singleton_key_) REFERENCES main__address_book_root(singleton_key_) ON DELETE RESTRICT,
  ADD FOREIGN KEY main__person_group__id (main__person_group__id) REFERENCES main__person_group(id) ON DELETE RESTRICT;
-- rollback ALTER TABLE test__address_book.main__address_book_person
-- rollback   DROP FOREIGN KEY self__id,
-- rollback   DROP FOREIGN KEY main__address_book_root__singleton_key_,
-- rollback   DROP FOREIGN KEY main__person_group__id;

-- changeset address_book-1.0.0:17
ALTER TABLE test__address_book.main__address_book_relation
  ADD FOREIGN KEY person__id (person__id) REFERENCES main__address_book_person(id) ON DELETE RESTRICT,
  ADD FOREIGN KEY main__address_book_person__id (main__address_book_person__id) REFERENCES main__address_book_person(id) ON DELETE RESTRICT;
-- rollback ALTER TABLE test__address_book.main__address_book_relation
-- rollback   DROP FOREIGN KEY person__id,
-- rollback   DROP FOREIGN KEY main__address_book_person__id;

-- changeset address_book-1.0.0:18
ALTER TABLE test__address_book.crypto__password
  ADD FOREIGN KEY main__address_book_person__id (main__address_book_person__id) REFERENCES main__address_book_person(id) ON DELETE RESTRICT;
-- rollback ALTER TABLE test__address_book.crypto__password
-- rollback   DROP FOREIGN KEY main__address_book_person__id;

-- changeset address_book-1.0.0:19
ALTER TABLE test__address_book.crypto__secret
  ADD FOREIGN KEY main__address_book_person__id (main__address_book_person__id) REFERENCES main__address_book_person(id) ON DELETE RESTRICT;
-- rollback ALTER TABLE test__address_book.crypto__secret
-- rollback   DROP FOREIGN KEY main__address_book_person__id;

-- changeset address_book-1.0.0:20
ALTER TABLE test__address_book.keyless__keyless
  ADD FOREIGN KEY main__address_book_person__id (main__address_book_person__id) REFERENCES main__address_book_person(id) ON DELETE RESTRICT,
  ADD FOREIGN KEY city__city_info__name (city__city_info__name, city__city_info__state, city__city_info__country) REFERENCES city__city_info(name, state, country) ON DELETE RESTRICT;
-- rollback ALTER TABLE test__address_book.keyless__keyless
-- rollback   DROP FOREIGN KEY main__address_book_person__id,
-- rollback   DROP FOREIGN KEY city__city_info__name;

-- changeset address_book-1.0.0:21
ALTER TABLE test__address_book.keyless__keyless_child
  ADD FOREIGN KEY main__address_book_person__id (main__address_book_person__id) REFERENCES keyless__keyless(main__address_book_person__id) ON DELETE RESTRICT,
  ADD FOREIGN KEY city__city_info__name (city__city_info__name, city__city_info__state, city__city_info__country) REFERENCES keyless__keyless(city__city_info__name, city__city_info__state, city__city_info__country) ON DELETE RESTRICT;
-- rollback ALTER TABLE test__address_book.keyless__keyless_child
-- rollback   DROP FOREIGN KEY main__address_book_person__id,
-- rollback   DROP FOREIGN KEY city__city_info__name;

-- changeset address_book-1.0.0:22
ALTER TABLE test__address_book.keyless__keyed_child
  ADD FOREIGN KEY main__address_book_person__id (main__address_book_person__id) REFERENCES keyless__keyless(main__address_book_person__id) ON DELETE RESTRICT,
  ADD FOREIGN KEY city__city_info__name (city__city_info__name, city__city_info__state, city__city_info__country) REFERENCES keyless__keyless(city__city_info__name, city__city_info__state, city__city_info__country) ON DELETE RESTRICT;
-- rollback ALTER TABLE test__address_book.keyless__keyed_child
-- rollback   DROP FOREIGN KEY main__address_book_person__id,
-- rollback   DROP FOREIGN KEY city__city_info__name;

-- changeset address_book-1.0.0:23
ALTER TABLE test__address_book.main__person_group
  ADD FOREIGN KEY main__address_book_root__singleton_key_ (main__address_book_root__singleton_key_) REFERENCES main__address_book_root(singleton_key_) ON DELETE RESTRICT,
  ADD FOREIGN KEY main__person_group__id (main__person_group__id) REFERENCES main__person_group(id) ON DELETE RESTRICT;
-- rollback ALTER TABLE test__address_book.main__person_group
-- rollback   DROP FOREIGN KEY main__address_book_root__singleton_key_,
-- rollback   DROP FOREIGN KEY main__person_group__id;

-- changeset address_book-1.0.0:24
ALTER TABLE test__address_book.city__city_info
  ADD FOREIGN KEY self__name (self__name, self__state, self__country) REFERENCES city__city_info(name, state, country) ON DELETE RESTRICT,
  ADD FOREIGN KEY self2__name (self2__name, self2__state, self2__country) REFERENCES city__city_info(name, state, country) ON DELETE RESTRICT,
  ADD FOREIGN KEY main__address_book_root__singleton_key_ (main__address_book_root__singleton_key_) REFERENCES main__address_book_root(singleton_key_) ON DELETE RESTRICT;
-- rollback ALTER TABLE test__address_book.city__city_info
-- rollback   DROP FOREIGN KEY self__name,
-- rollback   DROP FOREIGN KEY self2__name,
-- rollback   DROP FOREIGN KEY main__address_book_root__singleton_key_;
