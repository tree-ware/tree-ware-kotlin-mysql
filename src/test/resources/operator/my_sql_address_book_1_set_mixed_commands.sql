UPDATE test$address_book.main$address_book_person
  SET self$id = NULL
  WHERE id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd') AND entity_path$ = '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]';
DELETE FROM test$address_book.main$address_book_person
  WHERE id = UUID_TO_BIN('a8aacf55-7810-4b43-afe5-4344f25435fd') AND entity_path$ = '/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]';
INSERT INTO test$address_book.main$address_book_relation
  (created_on$, updated_on$, entity_path$, main$address_book_person$id, id)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/relation[3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce]', UUID_TO_BIN('cc477201-48ec-4367-83a4-7fdbd92f8a6f'), UUID_TO_BIN('3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce'));
UPDATE test$address_book.main$address_book_relation
  SET updated_on$ = '2022-04-14T00:40:41.450', person$id = UUID_TO_BIN('ec983c56-320f-4d66-9dde-f180e8ac3807')
  WHERE id = UUID_TO_BIN('3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce') AND entity_path$ = '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/relation[3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce]';
UPDATE test$address_book.main$address_book_relation
  SET updated_on$ = '2022-04-14T00:40:41.450', person$id = UUID_TO_BIN('ec983c56-320f-4d66-9dde-f180e8ac3807')
  WHERE id = UUID_TO_BIN('05ade278-4b44-43da-a0cc-14463854e397') AND entity_path$ = '/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/relation[05ade278-4b44-43da-a0cc-14463854e397]';