UPDATE test$address_book.main$address_book_person
  SET self$id = NULL
  WHERE id = UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f') AND entity_path$ = '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]';
UPDATE test$address_book.main$address_book_relation
  SET person$id = NULL
  WHERE id = UUID_TO_BIN('05ade278-4b44-43da-a0cc-14463854e397') AND entity_path$ = '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/relation[05ade278-4b44-43da-a0cc-14463854e397]';
UPDATE test$address_book.main$address_book_person
  SET self$id = NULL
  WHERE id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd') AND entity_path$ = '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]';
UPDATE test$address_book.main$address_book_relation
  SET person$id = NULL
  WHERE id = UUID_TO_BIN('16634916-8f83-4376-ad42-37038e108a0b') AND entity_path$ = '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/relation[16634916-8f83-4376-ad42-37038e108a0b]';
UPDATE test$address_book.main$address_book_person
  SET self$id = NULL
  WHERE id = UUID_TO_BIN('546a4982-b39a-4d01-aeb3-22d60c6963c0') AND entity_path$ = '/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]/persons[546a4982-b39a-4d01-aeb3-22d60c6963c0]';
UPDATE test$address_book.main$address_book_person
  SET self$id = NULL
  WHERE id = UUID_TO_BIN('e391c509-67d6-4846-bfea-0f7cd9c91bf7') AND entity_path$ = '/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]/persons[e391c509-67d6-4846-bfea-0f7cd9c91bf7]';
UPDATE test$address_book.city$city_info
  SET related_city_info = NULL, self$name = NULL, self$state = NULL, self$country = NULL, self2$name = NULL, self2$state = NULL, self2$country = NULL
  WHERE name = 'New York City' AND state = 'New York' AND country = 'United States of America' AND entity_path$ = '/address_book/city_info[New York City,New York,United States of America]';
UPDATE test$address_book.city$city_info
  SET related_city_info = NULL, self$name = NULL, self$state = NULL, self$country = NULL, self2$name = NULL, self2$state = NULL, self2$country = NULL
  WHERE name = 'Albany' AND state = 'New York' AND country = 'United States of America' AND entity_path$ = '/address_book/city_info[Albany,New York,United States of America]';
UPDATE test$address_book.city$city_info
  SET related_city_info = NULL, self$name = NULL, self$state = NULL, self$country = NULL, self2$name = NULL, self2$state = NULL, self2$country = NULL
  WHERE name = 'Princeton' AND state = 'New Jersey' AND country = 'United States of America' AND entity_path$ = '/address_book/city_info[Princeton,New Jersey,United States of America]';
UPDATE test$address_book.city$city_info
  SET related_city_info = NULL, self$name = NULL, self$state = NULL, self$country = NULL, self2$name = NULL, self2$state = NULL, self2$country = NULL
  WHERE name = 'San Francisco' AND state = 'California' AND country = 'United States of America' AND entity_path$ = '/address_book/city_info[San Francisco,California,United States of America]';
DELETE FROM test$address_book.city$city_info
  WHERE name = 'San Francisco' AND state = 'California' AND country = 'United States of America' AND entity_path$ = '/address_book/city_info[San Francisco,California,United States of America]';
DELETE FROM test$address_book.city$city_info
  WHERE name = 'Princeton' AND state = 'New Jersey' AND country = 'United States of America' AND entity_path$ = '/address_book/city_info[Princeton,New Jersey,United States of America]';
DELETE FROM test$address_book.city$city_info
  WHERE name = 'Albany' AND state = 'New York' AND country = 'United States of America' AND entity_path$ = '/address_book/city_info[Albany,New York,United States of America]';
DELETE FROM test$address_book.city$city_info
  WHERE name = 'New York City' AND state = 'New York' AND country = 'United States of America' AND entity_path$ = '/address_book/city_info[New York City,New York,United States of America]';
DELETE FROM test$address_book.main$person_group
  WHERE id = UUID_TO_BIN('ad9aaea8-30fe-45ed-93ef-bd368da0c756') AND entity_path$ = '/address_book/groups[ad9aaea8-30fe-45ed-93ef-bd368da0c756]';
DELETE FROM test$address_book.main$address_book_person
  WHERE id = UUID_TO_BIN('e391c509-67d6-4846-bfea-0f7cd9c91bf7') AND entity_path$ = '/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]/persons[e391c509-67d6-4846-bfea-0f7cd9c91bf7]';
DELETE FROM test$address_book.main$address_book_person
  WHERE id = UUID_TO_BIN('546a4982-b39a-4d01-aeb3-22d60c6963c0') AND entity_path$ = '/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]/persons[546a4982-b39a-4d01-aeb3-22d60c6963c0]';
DELETE FROM test$address_book.main$person_group
  WHERE id = UUID_TO_BIN('fe2aa774-e1fe-4680-a439-8bd1d0eb4abc') AND entity_path$ = '/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]/sub_groups[fe2aa774-e1fe-4680-a439-8bd1d0eb4abc]';
DELETE FROM test$address_book.main$person_group
  WHERE id = UUID_TO_BIN('ca0a22e8-c300-4347-91b0-167a5f6f4f9a') AND entity_path$ = '/address_book/groups[ca0a22e8-c300-4347-91b0-167a5f6f4f9a]';
DELETE FROM test$address_book.crypto$secret
  WHERE entity_path$ = '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/secret';
DELETE FROM test$address_book.crypto$password
  WHERE entity_path$ = '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/password';
DELETE FROM test$address_book.main$address_book_relation
  WHERE id = UUID_TO_BIN('16634916-8f83-4376-ad42-37038e108a0b') AND entity_path$ = '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]/relation[16634916-8f83-4376-ad42-37038e108a0b]';
DELETE FROM test$address_book.main$address_book_person
  WHERE id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd') AND entity_path$ = '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]';
DELETE FROM test$address_book.crypto$secret
  WHERE entity_path$ = '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/secret';
DELETE FROM test$address_book.crypto$password
  WHERE entity_path$ = '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/password';
DELETE FROM test$address_book.main$address_book_relation
  WHERE id = UUID_TO_BIN('05ade278-4b44-43da-a0cc-14463854e397') AND entity_path$ = '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/relation[05ade278-4b44-43da-a0cc-14463854e397]';
DELETE FROM test$address_book.main$address_book_person
  WHERE id = UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f') AND entity_path$ = '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]';
DELETE FROM test$address_book.main$address_book_settings
  WHERE entity_path$ = '/address_book/settings';
DELETE FROM test$address_book.main$address_book_root
  WHERE entity_path$ = '/address_book';