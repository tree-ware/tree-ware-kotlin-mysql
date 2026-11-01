INSERT INTO test__comp_key_test.ck__world
  (created_on_, updated_on_, field_path_, singleton_key_, name)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/', 0, 'Test World');
INSERT INTO test__comp_key_test.ck__warehouse
  (created_on_, updated_on_, field_path_, ck__world__singleton_key_, id__code, location)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/warehouses', 0, 'WH-1', 'Austin');
INSERT INTO test__comp_key_test.ck__shipment
  (created_on_, updated_on_, field_path_, ck__world__singleton_key_, id__code, id__seq, description)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/primary_shipment', 0, 'PR', '1', 'Primary shipment');
INSERT INTO test__comp_key_test.ck__parcel
  (created_on_, updated_on_, field_path_, ck__shipment__id__code, ck__shipment__id__seq, tracking_id, weight)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/primary_shipment/PR/1/parcels', 'PR', '1', UUID_TO_BIN('33333333-3333-3333-3333-333333333333'), 7.75);
INSERT INTO test__comp_key_test.ck__label
  (created_on_, updated_on_, field_path_, ck__shipment__id__code, ck__shipment__id__seq, text)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/primary_shipment/PR/1/label', 'PR', '1', 'Primary');
INSERT INTO test__comp_key_test.ck__shipment
  (created_on_, updated_on_, field_path_, ck__world__singleton_key_, id__code, id__seq, description)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/shipments', 0, 'S', '1', 'First shipment');
INSERT INTO test__comp_key_test.ck__parcel
  (created_on_, updated_on_, field_path_, ck__shipment__id__code, ck__shipment__id__seq, tracking_id, weight)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/shipments/S/1/parcels', 'S', '1', UUID_TO_BIN('11111111-1111-1111-1111-111111111111'), 12.5);
INSERT INTO test__comp_key_test.ck__label
  (created_on_, updated_on_, field_path_, ck__shipment__id__code, ck__shipment__id__seq, text)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/shipments/S/1/label', 'S', '1', 'Fragile');
INSERT INTO test__comp_key_test.ck__shipment
  (created_on_, updated_on_, field_path_, ck__world__singleton_key_, id__code, id__seq, description)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/shipments', 0, 'S', '2', 'Second shipment');
INSERT INTO test__comp_key_test.ck__parcel
  (created_on_, updated_on_, field_path_, ck__shipment__id__code, ck__shipment__id__seq, tracking_id, weight)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/shipments/S/2/parcels', 'S', '2', UUID_TO_BIN('22222222-2222-2222-2222-222222222222'), 3.25);
INSERT INTO test__comp_key_test.ck__label
  (created_on_, updated_on_, field_path_, ck__shipment__id__code, ck__shipment__id__seq, text)
  VALUES
  ('2022-04-14T00:40:41.450', '2022-04-14T00:40:41.450', '/shipments/S/2/label', 'S', '2', 'Heavy');
UPDATE test__comp_key_test.ck__world
  SET updated_on_ = '2022-04-14T00:40:41.450', favorite_shipment__id__code = 'S', favorite_shipment__id__seq = '1', favorite_shipment = '{"shipments":[{"id":{"code":"S","seq":1}}]}'
  WHERE field_path_ = '/';
UPDATE test__comp_key_test.ck__shipment
  SET updated_on_ = '2022-04-14T00:40:41.450', hub__id__code = 'WH-1', hub = '{"warehouses":[{"id":{"code":"WH-1"}}]}'
  WHERE id__code = 'PR' AND id__seq = '1' AND field_path_ = '/primary_shipment';
UPDATE test__comp_key_test.ck__shipment
  SET updated_on_ = '2022-04-14T00:40:41.450', hub__id__code = 'WH-1', hub = '{"warehouses":[{"id":{"code":"WH-1"}}]}'
  WHERE id__code = 'S' AND id__seq = '1' AND field_path_ = '/shipments';
UPDATE test__comp_key_test.ck__shipment
  SET updated_on_ = '2022-04-14T00:40:41.450', hub__id__code = 'WH-1', hub = '{"warehouses":[{"id":{"code":"WH-1"}}]}'
  WHERE id__code = 'S' AND id__seq = '2' AND field_path_ = '/shipments';