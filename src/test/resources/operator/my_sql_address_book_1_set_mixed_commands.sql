UPDATE test__address_book.main__address_book_person
  SET self__id = NULL
  WHERE id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd') AND field_path_ = '/address_book/person';
DELETE FROM test__address_book.main__address_book_person
  WHERE id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd') AND field_path_ = '/address_book/person';
INSERT INTO test__address_book.main__address_book_relation
  (created_on_, updated_on_, field_path_, main__address_book_person__id, id)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation', UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f'), UUID_TO_BIN('3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce'));
UPDATE test__address_book.main__address_book_relation
  SET updated_on_ = '2022-04-14T00:40:41.450', person__id = UUID_TO_BIN('ec983c56-320f-4d66-9dde-f180e8ac3807'), person = '{"person":[{"id":"ec983c56-320f-4d66-9dde-f180e8ac3807"}]}'
  WHERE id = UUID_TO_BIN('3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce') AND field_path_ = '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation';
UPDATE test__address_book.main__address_book_relation
  SET updated_on_ = '2022-04-14T00:40:41.450', person__id = UUID_TO_BIN('ec983c56-320f-4d66-9dde-f180e8ac3807'), person = '{"person":[{"id":"ec983c56-320f-4d66-9dde-f180e8ac3807"}]}'
  WHERE id = UUID_TO_BIN('05ade278-4b44-43da-a0cc-14463854e397') AND field_path_ = '/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation';