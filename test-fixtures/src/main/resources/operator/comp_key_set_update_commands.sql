UPDATE test__comp_key_test.ck__world
  SET updated_on_ = '2022-04-14T00:40:41.450', favorite_shipment__id__code = 'S', favorite_shipment__id__seq = '2', favorite_shipment = '{"shipments":[{"id":{"code":"S","seq":2}}]}', name = 'Test World 2'
  WHERE field_path_ = '/';
UPDATE test__comp_key_test.ck__warehouse
  SET updated_on_ = '2022-04-14T00:40:41.450', location = 'Dallas'
  WHERE id__code = 'WH-1' AND field_path_ = '/warehouses';
UPDATE test__comp_key_test.ck__shipment
  SET updated_on_ = '2022-04-14T00:40:41.450', description = 'Primary shipment rerouted'
  WHERE id__code = 'PR' AND id__seq = '1' AND field_path_ = '/primary_shipment';
UPDATE test__comp_key_test.ck__shipment
  SET updated_on_ = '2022-04-14T00:40:41.450', description = 'First shipment rerouted'
  WHERE id__code = 'S' AND id__seq = '1' AND field_path_ = '/shipments';
UPDATE test__comp_key_test.ck__parcel
  SET updated_on_ = '2022-04-14T00:40:41.450', weight = 14.0
  WHERE tracking_id = UUID_TO_BIN('11111111-1111-1111-1111-111111111111') AND field_path_ = '/shipments/S/1/parcels';
UPDATE test__comp_key_test.ck__shipment
  SET updated_on_ = '2022-04-14T00:40:41.450', hub__id__code = 'WH-1', hub = '{"warehouses":[{"id":{"code":"WH-1"}}]}'
  WHERE id__code = 'S' AND id__seq = '2' AND field_path_ = '/shipments';