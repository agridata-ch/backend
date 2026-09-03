-- ===============================================
-- data_product: reassign from BLV's ACONTROL-BLV source system to BLW's ACONTROL source system
-- ===============================================
UPDATE data_product
SET data_source_system_id = '810af188-8800-48f8-9b12-0276bb4b4e0e',
    data_provider_uid     = 'CHE113614519'
WHERE id IN ('96787125-3de0-4b5f-b9c7-8f1e5b3db516', 'df72eb69-fa78-4b52-8649-7068940019fe',
             '905170b2-1866-41f2-a8dd-57ba67a1f7bc');

-- ===============================================
-- data_request: reassign any requests still pointing at BLV's ACONTROL-BLV source system
-- ===============================================
UPDATE data_request
SET data_source_system_id = '810af188-8800-48f8-9b12-0276bb4b4e0e'
WHERE data_source_system_id = '7b8fe538-e8cf-472c-a171-c24e8fb8a704';

-- ===============================================
-- data_provider_rest_client: remove BLV's mapping
-- ===============================================
DELETE
FROM data_provider_rest_client
WHERE data_provider_id = '3bbc6006-1697-4a5f-8cba-2d34fbc278db';

-- ===============================================
-- data_source_system: remove BLV's ACONTROL-BLV source system
-- ===============================================
DELETE
FROM data_source_system
WHERE id = '7b8fe538-e8cf-472c-a171-c24e8fb8a704';

-- ===============================================
-- data_provider: remove BLV
-- ===============================================
DELETE
FROM data_provider
WHERE id = '3bbc6006-1697-4a5f-8cba-2d34fbc278db';
