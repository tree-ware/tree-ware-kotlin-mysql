UPDATE test__comp_key_test.ck__world
  SET favorite_shipment__id__code = NULL, favorite_shipment__id__seq = NULL
  WHERE field_path_ = '/';
UPDATE test__comp_key_test.ck__shipment
  SET hub__id__code = NULL
  WHERE id__code = 'PR' AND id__seq = '1' AND field_path_ = '/primary_shipment';
UPDATE test__comp_key_test.ck__shipment
  SET hub__id__code = NULL
  WHERE id__code = 'S' AND id__seq = '1' AND field_path_ = '/shipments';
UPDATE test__comp_key_test.ck__shipment
  SET hub__id__code = NULL
  WHERE id__code = 'S' AND id__seq = '2' AND field_path_ = '/shipments';
DELETE FROM test__comp_key_test.ck__label
  WHERE field_path_ = '/shipments/S/2/label';
DELETE FROM test__comp_key_test.ck__parcel
  WHERE tracking_id = UUID_TO_BIN('22222222-2222-2222-2222-222222222222') AND field_path_ = '/shipments/S/2/parcels';
DELETE FROM test__comp_key_test.ck__shipment
  WHERE id__code = 'S' AND id__seq = '2' AND field_path_ = '/shipments';
DELETE FROM test__comp_key_test.ck__label
  WHERE field_path_ = '/shipments/S/1/label';
DELETE FROM test__comp_key_test.ck__parcel
  WHERE tracking_id = UUID_TO_BIN('11111111-1111-1111-1111-111111111111') AND field_path_ = '/shipments/S/1/parcels';
DELETE FROM test__comp_key_test.ck__shipment
  WHERE id__code = 'S' AND id__seq = '1' AND field_path_ = '/shipments';
DELETE FROM test__comp_key_test.ck__label
  WHERE field_path_ = '/primary_shipment/PR/1/label';
DELETE FROM test__comp_key_test.ck__parcel
  WHERE tracking_id = UUID_TO_BIN('33333333-3333-3333-3333-333333333333') AND field_path_ = '/primary_shipment/PR/1/parcels';
DELETE FROM test__comp_key_test.ck__shipment
  WHERE id__code = 'PR' AND id__seq = '1' AND field_path_ = '/primary_shipment';
DELETE FROM test__comp_key_test.ck__warehouse
  WHERE id__code = 'WH-1' AND field_path_ = '/warehouses';
DELETE FROM test__comp_key_test.ck__world
  WHERE field_path_ = '/';