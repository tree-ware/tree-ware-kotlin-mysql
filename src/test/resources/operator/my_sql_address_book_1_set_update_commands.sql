UPDATE test$address_book.main$address_book_root
  SET updated_on$ = '2022-04-14T00:40:41.450', name = 'Super Heroes 2', last_updated = '2022-04-19T17:52:57.222'
  WHERE field_path$ = '/address_book';
UPDATE test$address_book.main$address_book_settings
  SET updated_on$ = '2022-04-14T00:40:41.450', last_name_first = false, encrypt_hero_name = true, card_colors = '[{"value":"blue"},{"value":"red"}]'
  WHERE field_path$ = '/address_book/settings';
UPDATE test$address_book.main$advanced_settings
  SET updated_on$ = '2022-04-14T00:40:41.450', background_color = 4
  WHERE field_path$ = '/address_book/settings/advanced';
UPDATE test$address_book.main$address_book_person
  SET updated_on$ = '2022-04-14T00:40:41.450', self$id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), self = '{"person":[{"id":"a8aacf55-7810-4b43-afe5-4344f25435fd"}]}', first_name = 'Lois 2', last_name = 'Lane 2', email = '[{"value":"lois.lane.2@dailyplanet.com"}]', picture = 0x50696374757265206f66204c6f6973204c616e652032
  WHERE id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd') AND field_path$ = '/address_book/person';
UPDATE test$address_book.main$address_book_relation
  SET updated_on$ = '2022-04-14T00:40:41.450', person$id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), person = '{"person":[{"id":"a8aacf55-7810-4b43-afe5-4344f25435fd"}]}', relationship = 6
  WHERE id = UUID_TO_BIN('16634916-8f83-4376-ad42-37038e108a0b') AND field_path$ = '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/relation';
UPDATE test$address_book.crypto$password
  SET updated_on$ = '2022-04-14T00:40:41.450', current = '{"hashed":"test-hashed-lois-2","hash_version":1}', previous = '[{"hashed":"test-hashed-password2-2","hash_version":1}]'
  WHERE field_path$ = '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/password';
UPDATE test$address_book.crypto$secret
  SET updated_on$ = '2022-04-14T00:40:41.450', main = '{"encrypted":"test-encrypted-main-secret-2","cipher_version":1}', other = '[{"encrypted":"test-encrypted-secret2-2","cipher_version":1}]'
  WHERE field_path$ = '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/secret';
UPDATE test$address_book.main$address_book_person
  SET updated_on$ = '2022-04-14T00:40:41.450', first_name = 'Clark 2', last_name = 'Kent 2', hero_name = 'Superman 2', email = '[{"value":"clark.kent.2@dailyplanet.com"}]', picture = 0x50696374757265206f6620436c61726b204b656e742032
  WHERE id = UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f') AND field_path$ = '/address_book/person';
UPDATE test$address_book.main$address_book_relation
  SET updated_on$ = '2022-04-14T00:40:41.450', person$id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), person = '{"person":[{"id":"a8aacf55-7810-4b43-afe5-4344f25435fd"}]}', relationship = 6
  WHERE id = UUID_TO_BIN('05ade278-4b44-43da-a0cc-14463854e397') AND field_path$ = '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/relation';
UPDATE test$address_book.crypto$password
  SET updated_on$ = '2022-04-14T00:40:41.450', previous = '[{"hashed":"test-hashed-superman-2","hash_version":1}]'
  WHERE field_path$ = '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/password';
UPDATE test$address_book.crypto$secret
  SET updated_on$ = '2022-04-14T00:40:41.450', other = '[{"encrypted":"test-encrypted-secret2-2","cipher_version":1}]'
  WHERE field_path$ = '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/secret';
UPDATE test$address_book.main$person_group
  SET updated_on$ = '2022-04-14T00:40:41.450', name = 'Marvel 2'
  WHERE id = UUID_TO_BIN('ad9aaea8-30fe-45ed-93ef-bd368da0c756') AND field_path$ = '/address_book/groups';
UPDATE test$address_book.main$person_group
  SET updated_on$ = '2022-04-14T00:40:41.450', name = 'DC 2'
  WHERE id = UUID_TO_BIN('ca0a22e8-c300-4347-91b0-167a5f6f4f9a') AND field_path$ = '/address_book/groups';
UPDATE test$address_book.main$person_group
  SET updated_on$ = '2022-04-14T00:40:41.450', name = 'Superman 2'
  WHERE id = UUID_TO_BIN('fe2aa774-e1fe-4680-a439-8bd1d0eb4abc') AND field_path$ = '/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups';
UPDATE test$address_book.main$address_book_person
  SET updated_on$ = '2022-04-14T00:40:41.450', first_name = 'Clark 3', last_name = 'Kent 3', hero_name = 'Superman 3'
  WHERE id = UUID_TO_BIN('546a4982-b39a-4d01-aeb3-22d60c6963c0') AND field_path$ = '/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]/persons';
UPDATE test$address_book.main$address_book_person
  SET updated_on$ = '2022-04-14T00:40:41.450', first_name = 'Lois 3', last_name = 'Lane 3', hero_name = 'n/a'
  WHERE id = UUID_TO_BIN('e391c509-67d6-4846-bfea-0f7cd9c91bf7') AND field_path$ = '/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]/persons';
UPDATE test$address_book.city$city_info
  SET updated_on$ = '2022-04-14T00:40:41.450', related_city_info = '[{"city_info":[{"name":"Princeton","state":"New Jersey","country":"United States of America"}]}]', info = 'Capital of New York state 2', latitude = 42.651935, longitude = -73.75478, city_center = ST_SRID(Point(-73.75478, 42.651935), 4326)
  WHERE name = 'Albany' AND state = 'New York' AND country = 'United States of America' AND field_path$ = '/address_book/city_info';
UPDATE test$address_book.city$city_info
  SET updated_on$ = '2022-04-14T00:40:41.450', related_city_info = '[{"city_info":[{"name":"San Francisco","state":"California","country":"United States of America"}]},{"city_info":[{"name":"Albany","state":"New York","country":"United States of America"}]}]', info = 'One of the most populous and most densely populated major city in USA 2', latitude = 40.712983, longitude = -74.007206, city_center = ST_SRID(Point(-74.007206, 40.712983), 4326)
  WHERE name = 'New York City' AND state = 'New York' AND country = 'United States of America' AND field_path$ = '/address_book/city_info';
UPDATE test$address_book.city$city_info
  SET updated_on$ = '2022-04-14T00:40:41.450', related_city_info = '[{"city_info":[{"name":"Princeton","state":"New Jersey","country":"United States of America"}]}]', info = 'The cultural and financial center of Northern California 2', latitude = 37.77938, longitude = -122.418434, city_center = ST_SRID(Point(-122.418434, 37.77938), 4326)
  WHERE name = 'San Francisco' AND state = 'California' AND country = 'United States of America' AND field_path$ = '/address_book/city_info';
UPDATE test$address_book.city$city_info
  SET updated_on$ = '2022-04-14T00:40:41.450', related_city_info = '[]', self2$name = 'Princeton', self2$state = 'New Jersey', self2$country = 'United States of America', self2 = '{"city_info":[{"country":"United States of America","state":"New Jersey","name":"Princeton"}]}', info = 'Home of Princeton University 2', latitude = 40.360595, longitude = -74.664442, city_center = ST_SRID(Point(-74.664442, 40.360595), 4326)
  WHERE name = 'Princeton' AND state = 'New Jersey' AND country = 'United States of America' AND field_path$ = '/address_book/city_info';