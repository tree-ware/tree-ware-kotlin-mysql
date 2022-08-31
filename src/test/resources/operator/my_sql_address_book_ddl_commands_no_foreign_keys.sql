CREATE DATABASE IF NOT EXISTS test__address_book;
CREATE TABLE IF NOT EXISTS test__address_book.main__address_book_root (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  singleton_key_ INT UNSIGNED,
  name VARCHAR(64),
  last_updated TIMESTAMP(3),
  PRIMARY KEY (singleton_key_)
) ENGINE = InnoDB;
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
CREATE TABLE IF NOT EXISTS test__address_book.main__advanced_settings (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  background_color INT UNSIGNED,
  main__address_book_root__singleton_key_ INT UNSIGNED,
  UNIQUE INDEX main__address_book_root__singleton_key_ (main__address_book_root__singleton_key_)
) ENGINE = InnoDB;
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
  main__person_group__id BINARY(16),
  PRIMARY KEY (id),
  UNIQUE INDEX self (self__id)
) ENGINE = InnoDB;
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
CREATE TABLE IF NOT EXISTS test__address_book.main__person_group (
  created_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on_ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path_ TEXT,
  id BINARY(16),
  name VARCHAR(64),
  main__person_group__id BINARY(16),
  PRIMARY KEY (id)
) ENGINE = InnoDB;
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
  PRIMARY KEY (name, state, country),
  UNIQUE INDEX coordinates (latitude, longitude),
  UNIQUE INDEX self (self__name, self__state, self__country)
) ENGINE = InnoDB;