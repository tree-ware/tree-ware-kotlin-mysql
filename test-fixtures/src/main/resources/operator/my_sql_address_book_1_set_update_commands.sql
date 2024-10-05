UPDATE test__address_book.main__address_book_root
  SET updated_on_ = '2022-04-14T00:40:41.450', name = 'Super Heroes 2', last_updated = '2022-04-19T17:52:57.222'
  WHERE field_path_ = '/address_book';
UPDATE test__address_book.main__address_book_settings
  SET updated_on_ = '2022-04-14T00:40:41.450', last_name_first = 0, encrypt_hero_name = 1
  WHERE field_path_ = '/address_book/settings';
UPDATE test__address_book.main__advanced_settings
  SET updated_on_ = '2022-04-14T00:40:41.450', background_color = '4'
  WHERE field_path_ = '/address_book/settings/advanced';
UPDATE test__address_book.main__address_book_person
  SET updated_on_ = '2022-04-14T00:40:41.450', self__id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), self = '{"person":[{"id":"a8aacf55-7810-4b43-afe5-4344f25435fd"}]}', first_name = 'Lois 2', last_name = 'Lane 2', picture = ** BYTE ARRAY DATA **
  WHERE id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd') AND field_path_ = '/address_book/person';
UPDATE test__address_book.main__address_book_relation
  SET updated_on_ = '2022-04-14T00:40:41.450', person__id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), person = '{"person":[{"id":"a8aacf55-7810-4b43-afe5-4344f25435fd"}]}', relationship = '6'
  WHERE id = UUID_TO_BIN('16634916-8f83-4376-ad42-37038e108a0b') AND field_path_ = '/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/relation';
UPDATE test__address_book.crypto__password
  SET updated_on_ = '2022-04-14T00:40:41.450', current = '{"hashed":"test-hashed-lois-2","hash_version":1}'
  WHERE field_path_ = '/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/password';
UPDATE test__address_book.main__address_book_person
  SET updated_on_ = '2022-04-14T00:40:41.450', first_name = 'Clark 2', last_name = 'Kent 2', hero_name = 'Superman 2', picture = ** BYTE ARRAY DATA **
  WHERE id = UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f') AND field_path_ = '/address_book/person';
UPDATE test__address_book.main__address_book_relation
  SET updated_on_ = '2022-04-14T00:40:41.450', person__id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd'), person = '{"person":[{"id":"a8aacf55-7810-4b43-afe5-4344f25435fd"}]}', relationship = '6'
  WHERE id = UUID_TO_BIN('05ade278-4b44-43da-a0cc-14463854e397') AND field_path_ = '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation';
UPDATE test__address_book.main__person_group
  SET updated_on_ = '2022-04-14T00:40:41.450', name = 'Marvel 2'
  WHERE id = UUID_TO_BIN('ad9aaea8-30fe-45ed-93ef-bd368da0c756') AND field_path_ = '/address_book/groups';
UPDATE test__address_book.main__person_group
  SET updated_on_ = '2022-04-14T00:40:41.450', name = 'DC 2'
  WHERE id = UUID_TO_BIN('ca0a22e8-c300-4347-91b0-167a5f6f4f9a') AND field_path_ = '/address_book/groups';
UPDATE test__address_book.main__person_group
  SET updated_on_ = '2022-04-14T00:40:41.450', name = 'Superman 2'
  WHERE id = UUID_TO_BIN('fe2aa774-e1fe-4680-a439-8bd1d0eb4abc') AND field_path_ = '/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups';
UPDATE test__address_book.main__address_book_person
  SET updated_on_ = '2022-04-14T00:40:41.450', first_name = 'Clark 3', last_name = 'Kent 3', hero_name = 'Superman 3'
  WHERE id = UUID_TO_BIN('546a4982-b39a-4d01-aeb3-22d60c6963c0') AND field_path_ = '/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups/fe2aa774-e1fe-4680-a439-8bd1d0eb4abc/persons';
UPDATE test__address_book.main__address_book_person
  SET updated_on_ = '2022-04-14T00:40:41.450', first_name = 'Lois 3', last_name = 'Lane 3', hero_name = 'n/a'
  WHERE id = UUID_TO_BIN('e391c509-67d6-4846-bfea-0f7cd9c91bf7') AND field_path_ = '/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups/fe2aa774-e1fe-4680-a439-8bd1d0eb4abc/persons';
UPDATE test__address_book.city__city_info
  SET updated_on_ = '2022-04-14T00:40:41.450', info = 'Capital of New York state 2', latitude = 42.651935, longitude = -73.75478, city_center = ST_SRID(Point(-73.75478, 42.651935), 4326)
  WHERE name = 'Albany' AND state = 'New York' AND country = 'United States of America' AND field_path_ = '/address_book/city_info';
UPDATE test__address_book.city__city_info
  SET updated_on_ = '2022-04-14T00:40:41.450', info = 'One of the most populous and most densely populated major city in USA 2', latitude = 40.712983, longitude = -74.007206, city_center = ST_SRID(Point(-74.007206, 40.712983), 4326)
  WHERE name = 'New York City' AND state = 'New York' AND country = 'United States of America' AND field_path_ = '/address_book/city_info';
UPDATE test__address_book.city__city_info
  SET updated_on_ = '2022-04-14T00:40:41.450', info = 'The cultural and financial center of Northern California 2', latitude = 37.77938, longitude = -122.418434, city_center = ST_SRID(Point(-122.418434, 37.77938), 4326)
  WHERE name = 'San Francisco' AND state = 'California' AND country = 'United States of America' AND field_path_ = '/address_book/city_info';
UPDATE test__address_book.city__city_info
  SET updated_on_ = '2022-04-14T00:40:41.450', self2__name = 'Princeton', self2__state = 'New Jersey', self2__country = 'United States of America', self2 = '{"city_info":[{"country":"United States of America","state":"New Jersey","name":"Princeton"}]}', info = 'Home of Princeton University 2', latitude = 40.360595, longitude = -74.664442, city_center = ST_SRID(Point(-74.664442, 40.360595), 4326)
  WHERE name = 'Princeton' AND state = 'New Jersey' AND country = 'United States of America' AND field_path_ = '/address_book/city_info';