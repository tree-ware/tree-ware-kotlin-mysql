-- liquibase formatted sql

-- AUTO-GENERATED FILE. DO NOT EDIT.

-- Meta-model version: comp_key_test 1.0.0 pacific-ocean

-- changeset comp_key_test-1.0.0:1
CREATE DATABASE IF NOT EXISTS test__comp_key_test;
-- rollback DROP DATABASE IF EXISTS test__comp_key_test;

-- changeset comp_key_test-1.0.0:2
CREATE TABLE IF NOT EXISTS ck__world (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  singleton_key_ INT UNSIGNED,
  name VARCHAR(64),
  favorite_shipment TEXT,
  favorite_shipment__id__code VARCHAR(32),
  favorite_shipment__id__seq INT UNSIGNED,
  PRIMARY KEY (singleton_key_)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS ck__world;

-- changeset comp_key_test-1.0.0:3
CREATE TABLE IF NOT EXISTS ck__shipment (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  id__code VARCHAR(32),
  id__seq INT UNSIGNED,
  description VARCHAR(256),
  hub TEXT,
  hub__id__code VARCHAR(32),
  ck__world__singleton_key_ INT UNSIGNED,
  PRIMARY KEY (id__code, id__seq)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS ck__shipment;

-- changeset comp_key_test-1.0.0:4
CREATE TABLE IF NOT EXISTS ck__shipment_id (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  code VARCHAR(32),
  seq INT UNSIGNED,
  ck__shipment__id__code VARCHAR(32),
  ck__shipment__id__seq INT UNSIGNED,
  PRIMARY KEY (code, seq)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS ck__shipment_id;

-- changeset comp_key_test-1.0.0:5
CREATE TABLE IF NOT EXISTS ck__parcel (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  tracking_id BINARY(16),
  weight DOUBLE,
  ck__shipment__id__code VARCHAR(32),
  ck__shipment__id__seq INT UNSIGNED,
  PRIMARY KEY (tracking_id)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS ck__parcel;

-- changeset comp_key_test-1.0.0:6
CREATE TABLE IF NOT EXISTS ck__label (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  text VARCHAR(128),
  ck__shipment__id__code VARCHAR(32),
  ck__shipment__id__seq INT UNSIGNED,
  UNIQUE INDEX ck__shipment__id__code (ck__shipment__id__code, ck__shipment__id__seq)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS ck__label;

-- changeset comp_key_test-1.0.0:7
CREATE TABLE IF NOT EXISTS ck__warehouse (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  id__code VARCHAR(32),
  location VARCHAR(128),
  ck__world__singleton_key_ INT UNSIGNED,
  PRIMARY KEY (id__code)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS ck__warehouse;

-- changeset comp_key_test-1.0.0:8
CREATE TABLE IF NOT EXISTS ck__warehouse_id (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  code VARCHAR(32),
  ck__warehouse__id__code VARCHAR(32),
  PRIMARY KEY (code)
) ENGINE = InnoDB;
-- rollback DROP TABLE IF EXISTS ck__warehouse_id;

-- changeset comp_key_test-1.0.0:9
ALTER TABLE ck__world
  ADD FOREIGN KEY favorite_shipment__id__code (favorite_shipment__id__code, favorite_shipment__id__seq) REFERENCES ck__shipment(id__code, id__seq) ON DELETE RESTRICT;
-- rollback ALTER TABLE ck__world
-- rollback   DROP FOREIGN KEY favorite_shipment__id__code;

-- changeset comp_key_test-1.0.0:10
ALTER TABLE ck__shipment
  ADD FOREIGN KEY hub__id__code (hub__id__code) REFERENCES ck__warehouse(id__code) ON DELETE RESTRICT,
  ADD FOREIGN KEY ck__world__singleton_key_ (ck__world__singleton_key_) REFERENCES ck__world(singleton_key_) ON DELETE RESTRICT;
-- rollback ALTER TABLE ck__shipment
-- rollback   DROP FOREIGN KEY hub__id__code,
-- rollback   DROP FOREIGN KEY ck__world__singleton_key_;

-- changeset comp_key_test-1.0.0:11
ALTER TABLE ck__shipment_id
  ADD FOREIGN KEY ck__shipment__id__code (ck__shipment__id__code, ck__shipment__id__seq) REFERENCES ck__shipment(id__code, id__seq) ON DELETE RESTRICT;
-- rollback ALTER TABLE ck__shipment_id
-- rollback   DROP FOREIGN KEY ck__shipment__id__code;

-- changeset comp_key_test-1.0.0:12
ALTER TABLE ck__parcel
  ADD FOREIGN KEY ck__shipment__id__code (ck__shipment__id__code, ck__shipment__id__seq) REFERENCES ck__shipment(id__code, id__seq) ON DELETE RESTRICT;
-- rollback ALTER TABLE ck__parcel
-- rollback   DROP FOREIGN KEY ck__shipment__id__code;

-- changeset comp_key_test-1.0.0:13
ALTER TABLE ck__label
  ADD FOREIGN KEY ck__shipment__id__code (ck__shipment__id__code, ck__shipment__id__seq) REFERENCES ck__shipment(id__code, id__seq) ON DELETE RESTRICT;
-- rollback ALTER TABLE ck__label
-- rollback   DROP FOREIGN KEY ck__shipment__id__code;

-- changeset comp_key_test-1.0.0:14
ALTER TABLE ck__warehouse
  ADD FOREIGN KEY ck__world__singleton_key_ (ck__world__singleton_key_) REFERENCES ck__world(singleton_key_) ON DELETE RESTRICT;
-- rollback ALTER TABLE ck__warehouse
-- rollback   DROP FOREIGN KEY ck__world__singleton_key_;

-- changeset comp_key_test-1.0.0:15
ALTER TABLE ck__warehouse_id
  ADD FOREIGN KEY ck__warehouse__id__code (ck__warehouse__id__code) REFERENCES ck__warehouse(id__code) ON DELETE RESTRICT;
-- rollback ALTER TABLE ck__warehouse_id
-- rollback   DROP FOREIGN KEY ck__warehouse__id__code;
