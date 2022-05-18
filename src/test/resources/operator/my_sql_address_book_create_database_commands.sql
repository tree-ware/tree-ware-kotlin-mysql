CREATE DATABASE IF NOT EXISTS test$address_book;
CREATE TABLE IF NOT EXISTS test$address_book.main$address_book_root (
  created_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path$ TEXT,
  singleton_key$ INT UNSIGNED,
  name VARCHAR(64),
  last_updated TIMESTAMP(3),
  PRIMARY KEY (singleton_key$)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS test$address_book.main$address_book_settings (
  created_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path$ TEXT,
  main$address_book_root$singleton_key$ INT UNSIGNED,
  last_name_first BOOLEAN,
  encrypt_hero_name BOOLEAN,
  card_colors JSON,
  UNIQUE INDEX main$address_book_root$singleton_key$ (main$address_book_root$singleton_key$)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS test$address_book.main$advanced_settings (
  created_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path$ TEXT,
  main$address_book_root$singleton_key$ INT UNSIGNED,
  background_color INT UNSIGNED,
  UNIQUE INDEX main$address_book_root$singleton_key$ (main$address_book_root$singleton_key$)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS test$address_book.main$person_group (
  created_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path$ TEXT,
  main$person_group$id BINARY(16),
  id BINARY(16),
  name VARCHAR(64),
  PRIMARY KEY (id)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS test$address_book.main$address_book_person (
  created_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path$ TEXT,
  main$person_group$id BINARY(16),
  id BINARY(16),
  first_name VARCHAR(64),
  last_name VARCHAR(64),
  hero_name VARCHAR(64),
  email JSON,
  picture BLOB,
  self$id BINARY(16),
  PRIMARY KEY (id),
  UNIQUE INDEX self (self$id)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS test$address_book.main$address_book_relation (
  created_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path$ TEXT,
  main$person_group$id BINARY(16),
  main$address_book_person$id BINARY(16),
  id BINARY(16),
  relationship INT UNSIGNED,
  person$id BINARY(16),
  PRIMARY KEY (id)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS test$address_book.city$city_info (
  created_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path$ TEXT,
  name VARCHAR(128),
  state VARCHAR(64),
  country VARCHAR(64),
  info VARCHAR(512),
  latitude DOUBLE,
  longitude DOUBLE,
  city_center POINT SRID 4326,
  related_city_info JSON,
  self$name VARCHAR(128),
  self$state VARCHAR(64),
  self$country VARCHAR(64),
  self2$name VARCHAR(128),
  self2$state VARCHAR(64),
  self2$country VARCHAR(64),
  PRIMARY KEY (name, state, country),
  UNIQUE INDEX coordinates (latitude, longitude),
  UNIQUE INDEX self (self$name, self$state, self$country)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS test$address_book.crypto$password (
  created_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path$ TEXT,
  main$person_group$id BINARY(16),
  main$address_book_person$id BINARY(16),
  current JSON,
  previous JSON,
  UNIQUE INDEX main$address_book_person$id (main$address_book_person$id)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS test$address_book.crypto$secret (
  created_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path$ TEXT,
  main$person_group$id BINARY(16),
  main$address_book_person$id BINARY(16),
  main JSON,
  other JSON,
  UNIQUE INDEX main$address_book_person$id (main$address_book_person$id)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS test$address_book.keyless$keyless (
  created_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path$ TEXT,
  main$person_group$id BINARY(16),
  main$address_book_person$id BINARY(16),
  city$city_info$name VARCHAR(128),
  city$city_info$state VARCHAR(64),
  city$city_info$country VARCHAR(64),
  name VARCHAR(64),
  UNIQUE INDEX main$address_book_person$id (main$address_book_person$id),
  UNIQUE INDEX city$city_info$name (city$city_info$name, city$city_info$state, city$city_info$country)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS test$address_book.keyless$keyless_child (
  created_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path$ TEXT,
  main$person_group$id BINARY(16),
  main$address_book_person$id BINARY(16),
  city$city_info$name VARCHAR(128),
  city$city_info$state VARCHAR(64),
  city$city_info$country VARCHAR(64),
  name VARCHAR(64),
  UNIQUE INDEX main$address_book_person$id (main$address_book_person$id),
  UNIQUE INDEX city$city_info$name (city$city_info$name, city$city_info$state, city$city_info$country)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS test$address_book.keyless$keyed_child (
  created_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_on$ TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  field_path$ TEXT,
  main$person_group$id BINARY(16),
  main$address_book_person$id BINARY(16),
  city$city_info$name VARCHAR(128),
  city$city_info$state VARCHAR(64),
  city$city_info$country VARCHAR(64),
  name VARCHAR(64),
  other INT,
  PRIMARY KEY (name)
) ENGINE = InnoDB;
ALTER TABLE test$address_book.main$address_book_settings
  ADD FOREIGN KEY (main$address_book_root$singleton_key$) REFERENCES main$address_book_root(singleton_key$) ON DELETE RESTRICT;
ALTER TABLE test$address_book.main$advanced_settings
  ADD FOREIGN KEY (main$address_book_root$singleton_key$) REFERENCES main$address_book_settings(main$address_book_root$singleton_key$) ON DELETE RESTRICT;
ALTER TABLE test$address_book.main$person_group
  ADD FOREIGN KEY (main$person_group$id) REFERENCES main$person_group(id) ON DELETE RESTRICT;
ALTER TABLE test$address_book.main$address_book_person
  ADD FOREIGN KEY (main$person_group$id) REFERENCES main$person_group(id) ON DELETE RESTRICT,
  ADD FOREIGN KEY (self$id) REFERENCES main$address_book_person(id) ON DELETE RESTRICT;
ALTER TABLE test$address_book.main$address_book_relation
  ADD FOREIGN KEY (main$address_book_person$id) REFERENCES main$address_book_person(id) ON DELETE RESTRICT,
  ADD FOREIGN KEY (person$id) REFERENCES main$address_book_person(id) ON DELETE RESTRICT;
ALTER TABLE test$address_book.city$city_info
  ADD FOREIGN KEY (self$name, self$state, self$country) REFERENCES city$city_info(name, state, country) ON DELETE RESTRICT,
  ADD FOREIGN KEY (self2$name, self2$state, self2$country) REFERENCES city$city_info(name, state, country) ON DELETE RESTRICT;
ALTER TABLE test$address_book.crypto$password
  ADD FOREIGN KEY (main$address_book_person$id) REFERENCES main$address_book_person(id) ON DELETE RESTRICT;
ALTER TABLE test$address_book.crypto$secret
  ADD FOREIGN KEY (main$address_book_person$id) REFERENCES main$address_book_person(id) ON DELETE RESTRICT;
ALTER TABLE test$address_book.keyless$keyless
  ADD FOREIGN KEY (main$address_book_person$id) REFERENCES main$address_book_person(id) ON DELETE RESTRICT,
  ADD FOREIGN KEY (city$city_info$name, city$city_info$state, city$city_info$country) REFERENCES city$city_info(name, state, country) ON DELETE RESTRICT;
ALTER TABLE test$address_book.keyless$keyless_child
  ADD FOREIGN KEY (main$address_book_person$id) REFERENCES keyless$keyless(main$address_book_person$id) ON DELETE RESTRICT,
  ADD FOREIGN KEY (city$city_info$name, city$city_info$state, city$city_info$country) REFERENCES keyless$keyless(city$city_info$name, city$city_info$state, city$city_info$country) ON DELETE RESTRICT;
ALTER TABLE test$address_book.keyless$keyed_child
  ADD FOREIGN KEY (main$address_book_person$id) REFERENCES keyless$keyless(main$address_book_person$id) ON DELETE RESTRICT,
  ADD FOREIGN KEY (city$city_info$name, city$city_info$state, city$city_info$country) REFERENCES keyless$keyless(city$city_info$name, city$city_info$state, city$city_info$country) ON DELETE RESTRICT;