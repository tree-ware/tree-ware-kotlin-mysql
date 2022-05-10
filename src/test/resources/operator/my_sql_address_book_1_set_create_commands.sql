INSERT INTO test$address_book.main$address_book_root
  (created_on$, updated_on$, entity_path$, singleton_key$, name, last_updated)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book', 0, 'Super Heroes', '2022-04-19T17:52:57');
INSERT INTO test$address_book.main$address_book_settings
  (created_on$, updated_on$, entity_path$, main$address_book_root$singleton_key$, last_name_first, encrypt_hero_name, card_colors)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/settings', 0, true, false, '[{"value":"orange"},{"value":"green"},{"value":"blue"}]');
INSERT INTO test$address_book.main$advanced_settings
  (created_on$, updated_on$, entity_path$, main$address_book_root$singleton_key$, background_color)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/settings/advanced', 0, 3);
INSERT INTO test$address_book.main$address_book_person
  (created_on$, updated_on$, entity_path$, id, first_name, last_name, hero_name, email, picture)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]', UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f'), 'Clark', 'Kent', 'Superman', '[{"value":"clark.kent@dailyplanet.com"},{"value":"superman@dc.com"}]', 0x50696374757265206f6620436c61726b204b656e74);
INSERT INTO test$address_book.main$address_book_relation
  (created_on$, updated_on$, entity_path$, main$address_book_person$id, id, relationship)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/relation[05ade278-4b44-43da-a0cc-14463854e397]', UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f'), UUID_TO_BIN('05ade278-4b44-43da-a0cc-14463854e397'), 7);
INSERT INTO test$address_book.main$address_book_relation
  (created_on$, updated_on$, entity_path$, main$address_book_person$id, id, relationship)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/relation[3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce]', UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f'), UUID_TO_BIN('3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce'), 7);
INSERT INTO test$address_book.crypto$password
  (created_on$, updated_on$, entity_path$, main$address_book_person$id, previous)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/password', UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f'), '[{"hashed":"test-hashed-superman","hash_version":1}]');
INSERT INTO test$address_book.crypto$secret
  (created_on$, updated_on$, entity_path$, main$address_book_person$id, other)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/secret', UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f'), '[{"encrypted":"test-encrypted-secret2","cipher_version":1}]');
INSERT INTO test$address_book.main$address_book_person
  (created_on$, updated_on$, entity_path$, id, first_name, last_name, email, picture)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]', UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), 'Lois', 'Lane', '[{"value":"lois.lane@dailyplanet.com"}]', 0x50696374757265206f66204c6f6973204c616e65);
INSERT INTO test$address_book.main$address_book_relation
  (created_on$, updated_on$, entity_path$, main$address_book_person$id, id, relationship)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/relation[16634916-8f83-4376-ad42-37038e108a0b]', UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), UUID_TO_BIN('16634916-8f83-4376-ad42-37038e108a0b'), 7);
INSERT INTO test$address_book.crypto$password
  (created_on$, updated_on$, entity_path$, main$address_book_person$id, current, previous)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/password', UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), '{"hashed":"test-hashed-lois","hash_version":1}', '[{"hashed":"test-hashed-password2","hash_version":1}]');
INSERT INTO test$address_book.crypto$secret
  (created_on$, updated_on$, entity_path$, main$address_book_person$id, main, other)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/secret', UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), '{"encrypted":"test-encrypted-main-secret","cipher_version":1}', '[{"encrypted":"test-encrypted-secret2","cipher_version":1}]');
INSERT INTO test$address_book.main$address_book_person
  (created_on$, updated_on$, entity_path$, id, first_name, last_name)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person[ec983c56-320f-4d66-9dde-f180e8ac3807]', UUID_TO_BIN('ec983c56-320f-4d66-9dde-f180e8ac3807'), 'Jimmy', 'Olsen');
INSERT INTO test$address_book.main$person_group
  (created_on$, updated_on$, entity_path$, id, name)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]', UUID_TO_BIN('ca0a22e8-c300-4347-91b0-167a5f6f4f9a'), 'DC');
INSERT INTO test$address_book.main$person_group
  (created_on$, updated_on$, entity_path$, main$person_group$id, id, name)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]', UUID_TO_BIN('ca0a22e8-c300-4347-91b0-167a5f6f4f9a'), UUID_TO_BIN('fe2aa774-e1fe-4680-a439-8bd1d0eb4abc'), 'Superman');
INSERT INTO test$address_book.main$address_book_person
  (created_on$, updated_on$, entity_path$, main$person_group$id, id, first_name, last_name, hero_name)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]/persons[546a4982-b39a-4d01-aeb3-22d60c6963c0]', UUID_TO_BIN('fe2aa774-e1fe-4680-a439-8bd1d0eb4abc'), UUID_TO_BIN('546a4982-b39a-4d01-aeb3-22d60c6963c0'), 'Clark', 'Kent', 'Superman');
INSERT INTO test$address_book.main$address_book_person
  (created_on$, updated_on$, entity_path$, main$person_group$id, id, first_name, last_name)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]/persons[e391c509-67d6-4846-bfea-0f7cd9c91bf7]', UUID_TO_BIN('fe2aa774-e1fe-4680-a439-8bd1d0eb4abc'), UUID_TO_BIN('e391c509-67d6-4846-bfea-0f7cd9c91bf7'), 'Lois', 'Lane');
INSERT INTO test$address_book.main$person_group
  (created_on$, updated_on$, entity_path$, id, name)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/groups[ad9aaea8-30fe-45ed-93ef-bd368da0c756]', UUID_TO_BIN('ad9aaea8-30fe-45ed-93ef-bd368da0c756'), 'Marvel');
INSERT INTO test$address_book.city$city_info
  (created_on$, updated_on$, entity_path$, name, state, country, info, latitude, longitude, city_center)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/city_info[New York City,New York,United States of America]', 'New York City', 'New York', 'United States of America', 'One of the most populous and most densely populated major city in USA', 40.712982, -74.007205, ST_SRID(Point(-74.007205, 40.712982), 4326));
INSERT INTO test$address_book.city$city_info
  (created_on$, updated_on$, entity_path$, name, state, country, info, latitude, longitude, city_center)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/city_info[Albany,New York,United States of America]', 'Albany', 'New York', 'United States of America', 'Capital of New York state', 42.651934, -73.75477, ST_SRID(Point(-73.75477, 42.651934), 4326));
INSERT INTO test$address_book.city$city_info
  (created_on$, updated_on$, entity_path$, name, state, country, info, latitude, longitude, city_center)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/city_info[Princeton,New Jersey,United States of America]', 'Princeton', 'New Jersey', 'United States of America', 'Home of Princeton University', 40.360594, -74.664441, ST_SRID(Point(-74.664441, 40.360594), 4326));
INSERT INTO test$address_book.city$city_info
  (created_on$, updated_on$, entity_path$, name, state, country, info, latitude, longitude, city_center)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/city_info[San Francisco,California,United States of America]', 'San Francisco', 'California', 'United States of America', 'The cultural and financial center of Northern California', 37.779379, -122.418433, ST_SRID(Point(-122.418433, 37.779379), 4326));
UPDATE test$address_book.main$address_book_relation
  SET updated_on$ = '2022-04-14T00:40:41.450', person$id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd')
  WHERE id = UUID_TO_BIN('05ade278-4b44-43da-a0cc-14463854e397') AND entity_path$ = '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/relation[05ade278-4b44-43da-a0cc-14463854e397]';
UPDATE test$address_book.main$address_book_relation
  SET updated_on$ = '2022-04-14T00:40:41.450', person$id = UUID_TO_BIN('ec983c56-320f-4d66-9dde-f180e8ac3807')
  WHERE id = UUID_TO_BIN('3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce') AND entity_path$ = '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/relation[3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce]';
UPDATE test$address_book.main$address_book_person
  SET updated_on$ = '2022-04-14T00:40:41.450', self$id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd')
  WHERE id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd') AND entity_path$ = '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]';
UPDATE test$address_book.main$address_book_relation
  SET updated_on$ = '2022-04-14T00:40:41.450', person$id = UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f')
  WHERE id = UUID_TO_BIN('16634916-8f83-4376-ad42-37038e108a0b') AND entity_path$ = '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/relation[16634916-8f83-4376-ad42-37038e108a0b]';
UPDATE test$address_book.city$city_info
  SET updated_on$ = '2022-04-14T00:40:41.450', related_city_info = '[{"city_info":[{"name":"Albany","state":"New York","country":"United States of America"}]},{"city_info":[{"name":"Princeton","state":"New Jersey","country":"United States of America"}]}]'
  WHERE name = 'New York City' AND state = 'New York' AND country = 'United States of America' AND entity_path$ = '/address_book/city_info[New York City,New York,United States of America]';
UPDATE test$address_book.city$city_info
  SET updated_on$ = '2022-04-14T00:40:41.450', related_city_info = '[{"city_info":[{"name":"New York City","state":"New York","country":"United States of America"}]}]'
  WHERE name = 'Albany' AND state = 'New York' AND country = 'United States of America' AND entity_path$ = '/address_book/city_info[Albany,New York,United States of America]';
UPDATE test$address_book.city$city_info
  SET updated_on$ = '2022-04-14T00:40:41.450', related_city_info = '[]', self$name = 'Princeton', self$state = 'New Jersey', self$country = 'United States of America', self2$name = 'Princeton', self2$state = 'New Jersey', self2$country = 'United States of America'
  WHERE name = 'Princeton' AND state = 'New Jersey' AND country = 'United States of America' AND entity_path$ = '/address_book/city_info[Princeton,New Jersey,United States of America]';