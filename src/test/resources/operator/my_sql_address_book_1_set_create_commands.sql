INSERT INTO test__address_book.main__address_book_root
  (created_on_, updated_on_, field_path_, singleton_key_, name, last_updated)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book', 0, 'Super Heroes', '2022-04-19T17:52:57');
INSERT INTO test__address_book.main__address_book_settings
  (created_on_, updated_on_, field_path_, main__address_book_root__singleton_key_, last_name_first, encrypt_hero_name, card_colors)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/settings', 0, 1, 0, '[{"value":"orange"},{"value":"green"},{"value":"blue"}]');
INSERT INTO test__address_book.main__advanced_settings
  (created_on_, updated_on_, field_path_, main__address_book_root__singleton_key_, background_color)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/settings/advanced', 0, '3');
INSERT INTO test__address_book.main__address_book_person
  (created_on_, updated_on_, field_path_, main__address_book_root__singleton_key_, id, first_name, last_name, hero_name, email, picture)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person', 0, UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f'), 'Clark', 'Kent', 'Superman', '[{"value":"clark.kent@dailyplanet.com"},{"value":"superman@dc.com"}]', ** BYTE ARRAY DATA **);
INSERT INTO test__address_book.main__address_book_relation
  (created_on_, updated_on_, field_path_, main__address_book_person__id, id, relationship)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation', UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f'), UUID_TO_BIN('05ade278-4b44-43da-a0cc-14463854e397'), '7');
INSERT INTO test__address_book.main__address_book_relation
  (created_on_, updated_on_, field_path_, main__address_book_person__id, id, relationship)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation', UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f'), UUID_TO_BIN('3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce'), '7');
INSERT INTO test__address_book.crypto__password
  (created_on_, updated_on_, field_path_, main__address_book_person__id, previous)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/password', UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f'), '[{"hashed":"test-hashed-superman","hash_version":1}]');
INSERT INTO test__address_book.crypto__secret
  (created_on_, updated_on_, field_path_, main__address_book_person__id, other)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/secret', UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f'), '[{"encrypted":"test-encrypted-secret2","cipher_version":1}]');
INSERT INTO test__address_book.main__address_book_person
  (created_on_, updated_on_, field_path_, main__address_book_root__singleton_key_, id, first_name, last_name, email, picture)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person', 0, UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), 'Lois', 'Lane', '[{"value":"lois.lane@dailyplanet.com"}]', ** BYTE ARRAY DATA **);
INSERT INTO test__address_book.main__address_book_relation
  (created_on_, updated_on_, field_path_, main__address_book_person__id, id, relationship)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/relation', UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), UUID_TO_BIN('16634916-8f83-4376-ad42-37038e108a0b'), '7');
INSERT INTO test__address_book.crypto__password
  (created_on_, updated_on_, field_path_, main__address_book_person__id, current, previous)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/password', UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), '{"hashed":"test-hashed-lois","hash_version":1}', '[{"hashed":"test-hashed-password2","hash_version":1}]');
INSERT INTO test__address_book.crypto__secret
  (created_on_, updated_on_, field_path_, main__address_book_person__id, main, other)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/secret', UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), '{"encrypted":"test-encrypted-main-secret","cipher_version":1}', '[{"encrypted":"test-encrypted-secret2","cipher_version":1}]');
INSERT INTO test__address_book.main__address_book_person
  (created_on_, updated_on_, field_path_, main__address_book_root__singleton_key_, id, first_name, last_name)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person', 0, UUID_TO_BIN('ec983c56-320f-4d66-9dde-f180e8ac3807'), 'Jimmy', 'Olsen');
INSERT INTO test__address_book.main__person_group
  (created_on_, updated_on_, field_path_, main__address_book_root__singleton_key_, id, name)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/groups', 0, UUID_TO_BIN('ca0a22e8-c300-4347-91b0-167a5f6f4f9a'), 'DC');
INSERT INTO test__address_book.main__person_group
  (created_on_, updated_on_, field_path_, main__person_group__id, id, name)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups', UUID_TO_BIN('ca0a22e8-c300-4347-91b0-167a5f6f4f9a'), UUID_TO_BIN('fe2aa774-e1fe-4680-a439-8bd1d0eb4abc'), 'Superman');
INSERT INTO test__address_book.main__address_book_person
  (created_on_, updated_on_, field_path_, main__person_group__id, id, first_name, last_name, hero_name)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups/fe2aa774-e1fe-4680-a439-8bd1d0eb4abc/persons', UUID_TO_BIN('fe2aa774-e1fe-4680-a439-8bd1d0eb4abc'), UUID_TO_BIN('546a4982-b39a-4d01-aeb3-22d60c6963c0'), 'Clark', 'Kent', 'Superman');
INSERT INTO test__address_book.main__address_book_person
  (created_on_, updated_on_, field_path_, main__person_group__id, id, first_name, last_name)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups/fe2aa774-e1fe-4680-a439-8bd1d0eb4abc/persons', UUID_TO_BIN('fe2aa774-e1fe-4680-a439-8bd1d0eb4abc'), UUID_TO_BIN('e391c509-67d6-4846-bfea-0f7cd9c91bf7'), 'Lois', 'Lane');
INSERT INTO test__address_book.main__person_group
  (created_on_, updated_on_, field_path_, main__address_book_root__singleton_key_, id, name)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/groups', 0, UUID_TO_BIN('ad9aaea8-30fe-45ed-93ef-bd368da0c756'), 'Marvel');
INSERT INTO test__address_book.city__city_info
  (created_on_, updated_on_, field_path_, main__address_book_root__singleton_key_, name, state, country, info, latitude, longitude, city_center)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/city_info', 0, 'New York City', 'New York', 'United States of America', 'One of the most populous and most densely populated major city in USA', 40.712982, -74.007205, ST_SRID(Point(-74.007205, 40.712982), 4326));
INSERT INTO test__address_book.city__city_info
  (created_on_, updated_on_, field_path_, main__address_book_root__singleton_key_, name, state, country, info, latitude, longitude, city_center)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/city_info', 0, 'Albany', 'New York', 'United States of America', 'Capital of New York state', 42.651934, -73.75477, ST_SRID(Point(-73.75477, 42.651934), 4326));
INSERT INTO test__address_book.city__city_info
  (created_on_, updated_on_, field_path_, main__address_book_root__singleton_key_, name, state, country, info, latitude, longitude, city_center)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/city_info', 0, 'Princeton', 'New Jersey', 'United States of America', 'Home of Princeton University', 40.360594, -74.664441, ST_SRID(Point(-74.664441, 40.360594), 4326));
INSERT INTO test__address_book.city__city_info
  (created_on_, updated_on_, field_path_, main__address_book_root__singleton_key_, name, state, country, info, latitude, longitude, city_center)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/city_info', 0, 'San Francisco', 'California', 'United States of America', 'The cultural and financial center of Northern California', 37.779379, -122.418433, ST_SRID(Point(-122.418433, 37.779379), 4326));
UPDATE test__address_book.main__address_book_relation
  SET updated_on_ = '2022-04-14T00:40:41.450', person__id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), person = '{"person":[{"id":"a8aacf55-7810-4b43-afe5-4344f25435fd"}]}'
  WHERE id = UUID_TO_BIN('05ade278-4b44-43da-a0cc-14463854e397') AND field_path_ = '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation';
UPDATE test__address_book.main__address_book_relation
  SET updated_on_ = '2022-04-14T00:40:41.450', person__id = UUID_TO_BIN('ec983c56-320f-4d66-9dde-f180e8ac3807'), person = '{"person":[{"id":"ec983c56-320f-4d66-9dde-f180e8ac3807"}]}'
  WHERE id = UUID_TO_BIN('3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce') AND field_path_ = '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation';
UPDATE test__address_book.main__address_book_person
  SET updated_on_ = '2022-04-14T00:40:41.450', self__id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), self = '{"person":[{"id":"a8aacf55-7810-4b43-afe5-4344f25435fd"}]}'
  WHERE id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd') AND field_path_ = '/address_book/person';
UPDATE test__address_book.main__address_book_relation
  SET updated_on_ = '2022-04-14T00:40:41.450', person__id = UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f'), person = '{"person":[{"id":"cc477201-48ec-4367-83a4-7fdbd92f8a6f"}]}'
  WHERE id = UUID_TO_BIN('16634916-8f83-4376-ad42-37038e108a0b') AND field_path_ = '/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/relation';
UPDATE test__address_book.city__city_info
  SET updated_on_ = '2022-04-14T00:40:41.450', related_city_info = '[{"city_info":[{"name":"Albany","state":"New York","country":"United States of America"}]},{"city_info":[{"name":"Princeton","state":"New Jersey","country":"United States of America"}]}]'
  WHERE name = 'New York City' AND state = 'New York' AND country = 'United States of America' AND field_path_ = '/address_book/city_info';
UPDATE test__address_book.city__city_info
  SET updated_on_ = '2022-04-14T00:40:41.450', related_city_info = '[{"city_info":[{"name":"New York City","state":"New York","country":"United States of America"}]}]'
  WHERE name = 'Albany' AND state = 'New York' AND country = 'United States of America' AND field_path_ = '/address_book/city_info';
UPDATE test__address_book.city__city_info
  SET updated_on_ = '2022-04-14T00:40:41.450', related_city_info = '[]', self__name = 'Princeton', self__state = 'New Jersey', self__country = 'United States of America', self = '{"city_info":[{"name":"Princeton","state":"New Jersey","country":"United States of America"}]}', self2__name = 'Princeton', self2__state = 'New Jersey', self2__country = 'United States of America', self2 = '{"city_info":[{"country":"United States of America","state":"New Jersey","name":"Princeton"}]}'
  WHERE name = 'Princeton' AND state = 'New Jersey' AND country = 'United States of America' AND field_path_ = '/address_book/city_info';