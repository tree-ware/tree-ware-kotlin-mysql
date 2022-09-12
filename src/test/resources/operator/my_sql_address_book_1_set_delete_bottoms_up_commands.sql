UPDATE test__address_book.main__address_book_person
  SET self__id = NULL
  WHERE id = UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f') AND field_path_ = '/address_book/person';
UPDATE test__address_book.main__address_book_relation
  SET person__id = NULL
  WHERE id = UUID_TO_BIN('05ade278-4b44-43da-a0cc-14463854e397') AND field_path_ = '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation';
UPDATE test__address_book.main__address_book_relation
  SET person__id = NULL
  WHERE id = UUID_TO_BIN('3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce') AND field_path_ = '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation';
UPDATE test__address_book.main__address_book_person
  SET self__id = NULL
  WHERE id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd') AND field_path_ = '/address_book/person';
UPDATE test__address_book.main__address_book_relation
  SET person__id = NULL
  WHERE id = UUID_TO_BIN('16634916-8f83-4376-ad42-37038e108a0b') AND field_path_ = '/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/relation';
UPDATE test__address_book.main__address_book_person
  SET self__id = NULL
  WHERE id = UUID_TO_BIN('ec983c56-320f-4d66-9dde-f180e8ac3807') AND field_path_ = '/address_book/person';
UPDATE test__address_book.main__address_book_person
  SET self__id = NULL
  WHERE id = UUID_TO_BIN('546a4982-b39a-4d01-aeb3-22d60c6963c0') AND field_path_ = '/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups/fe2aa774-e1fe-4680-a439-8bd1d0eb4abc/persons';
UPDATE test__address_book.main__address_book_person
  SET self__id = NULL
  WHERE id = UUID_TO_BIN('e391c509-67d6-4846-bfea-0f7cd9c91bf7') AND field_path_ = '/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups/fe2aa774-e1fe-4680-a439-8bd1d0eb4abc/persons';
UPDATE test__address_book.city__city_info
  SET related_city_info = NULL, self__name = NULL, self__state = NULL, self__country = NULL, self2__name = NULL, self2__state = NULL, self2__country = NULL
  WHERE name = 'New York City' AND state = 'New York' AND country = 'United States of America' AND field_path_ = '/address_book/city_info';
UPDATE test__address_book.city__city_info
  SET related_city_info = NULL, self__name = NULL, self__state = NULL, self__country = NULL, self2__name = NULL, self2__state = NULL, self2__country = NULL
  WHERE name = 'Albany' AND state = 'New York' AND country = 'United States of America' AND field_path_ = '/address_book/city_info';
UPDATE test__address_book.city__city_info
  SET related_city_info = NULL, self__name = NULL, self__state = NULL, self__country = NULL, self2__name = NULL, self2__state = NULL, self2__country = NULL
  WHERE name = 'Princeton' AND state = 'New Jersey' AND country = 'United States of America' AND field_path_ = '/address_book/city_info';
UPDATE test__address_book.city__city_info
  SET related_city_info = NULL, self__name = NULL, self__state = NULL, self__country = NULL, self2__name = NULL, self2__state = NULL, self2__country = NULL
  WHERE name = 'San Francisco' AND state = 'California' AND country = 'United States of America' AND field_path_ = '/address_book/city_info';
DELETE FROM test__address_book.city__city_info
  WHERE name = 'San Francisco' AND state = 'California' AND country = 'United States of America' AND field_path_ = '/address_book/city_info';
DELETE FROM test__address_book.city__city_info
  WHERE name = 'Princeton' AND state = 'New Jersey' AND country = 'United States of America' AND field_path_ = '/address_book/city_info';
DELETE FROM test__address_book.city__city_info
  WHERE name = 'Albany' AND state = 'New York' AND country = 'United States of America' AND field_path_ = '/address_book/city_info';
DELETE FROM test__address_book.city__city_info
  WHERE name = 'New York City' AND state = 'New York' AND country = 'United States of America' AND field_path_ = '/address_book/city_info';
DELETE FROM test__address_book.main__person_group
  WHERE id = UUID_TO_BIN('ad9aaea8-30fe-45ed-93ef-bd368da0c756') AND field_path_ = '/address_book/groups';
DELETE FROM test__address_book.main__address_book_person
  WHERE id = UUID_TO_BIN('e391c509-67d6-4846-bfea-0f7cd9c91bf7') AND field_path_ = '/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups/fe2aa774-e1fe-4680-a439-8bd1d0eb4abc/persons';
DELETE FROM test__address_book.main__address_book_person
  WHERE id = UUID_TO_BIN('546a4982-b39a-4d01-aeb3-22d60c6963c0') AND field_path_ = '/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups/fe2aa774-e1fe-4680-a439-8bd1d0eb4abc/persons';
DELETE FROM test__address_book.main__person_group
  WHERE id = UUID_TO_BIN('fe2aa774-e1fe-4680-a439-8bd1d0eb4abc') AND field_path_ = '/address_book/groups/ca0a22e8-c300-4347-91b0-167a5f6f4f9a/sub_groups';
DELETE FROM test__address_book.main__person_group
  WHERE id = UUID_TO_BIN('ca0a22e8-c300-4347-91b0-167a5f6f4f9a') AND field_path_ = '/address_book/groups';
DELETE FROM test__address_book.main__address_book_person
  WHERE id = UUID_TO_BIN('ec983c56-320f-4d66-9dde-f180e8ac3807') AND field_path_ = '/address_book/person';
DELETE FROM test__address_book.crypto__secret
  WHERE field_path_ = '/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/secret';
DELETE FROM test__address_book.crypto__password
  WHERE field_path_ = '/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/password';
DELETE FROM test__address_book.main__address_book_relation
  WHERE id = UUID_TO_BIN('16634916-8f83-4376-ad42-37038e108a0b') AND field_path_ = '/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd/relation';
DELETE FROM test__address_book.main__address_book_person
  WHERE id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd') AND field_path_ = '/address_book/person';
DELETE FROM test__address_book.crypto__secret
  WHERE field_path_ = '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/secret';
DELETE FROM test__address_book.crypto__password
  WHERE field_path_ = '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/password';
DELETE FROM test__address_book.main__address_book_relation
  WHERE id = UUID_TO_BIN('3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce') AND field_path_ = '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation';
DELETE FROM test__address_book.main__address_book_relation
  WHERE id = UUID_TO_BIN('05ade278-4b44-43da-a0cc-14463854e397') AND field_path_ = '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation';
DELETE FROM test__address_book.main__address_book_person
  WHERE id = UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f') AND field_path_ = '/address_book/person';
DELETE FROM test__address_book.main__advanced_settings
  WHERE field_path_ = '/address_book/settings/advanced';
DELETE FROM test__address_book.main__address_book_settings
  WHERE field_path_ = '/address_book/settings';
DELETE FROM test__address_book.main__address_book_root
  WHERE field_path_ = '/address_book';