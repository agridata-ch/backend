-- ===============================================
-- Test data reset for CHE102000001 (Jonas Testmann)
-- ===============================================

-- Delete existing consent_requests for this UID
DELETE FROM consent_request WHERE data_producer_uid = 'CHE102000001';

-- Insert consent_requests for CHE102000001
INSERT INTO consent_request (id, archived, created_at, modified_at, data_producer_uid, state_code, data_request_id, request_date, last_state_change_date) VALUES
('94e4f8e3-70b1-43ae-bdfa-78b27f86958e'::uuid, false, NOW(), NOW(), 'CHE102000001', 'OPENED', '3da3a459-d3c2-48af-b8d0-02bc95146468'::uuid, '2025-02-11 16:48:20'::timestamp, NULL::timestamp),
('2643cbd7-8077-4378-8c47-27d2b31dd554'::uuid, false, NOW(), NOW(), 'CHE102000001', 'DECLINED', '81ae8571-9497-413a-99c5-237e72621ca7'::uuid, '2025-01-25 13:20:15'::timestamp, '2025-01-30 09:35:00'::timestamp),
('68c0e430-00b8-44ca-a2f7-be6197ff64a9'::uuid, false, NOW(), NOW(), 'CHE102000001', 'GRANTED', '341f558a-781c-4eb5-bab7-c2f39216b9f2'::uuid, '2025-02-23 16:00:00'::timestamp, '2025-02-25 10:00:00'::timestamp);

-- Set migrated_from_maf for BioSuisse consent-requests (if applicable)
UPDATE consent_request cr
SET migrated_from_maf_date = '2025-09-01 00:00:00'::timestamp
FROM data_request dr
WHERE cr.data_request_id = dr.id
  AND cr.data_producer_uid = 'CHE102000001'
  AND dr.id IN ('3da3a459-d3c2-48af-b8d0-02bc95146468','81ae8571-9497-413a-99c5-237e72621ca7','85ca6584-b480-4941-84ed-1b81d24adbf1');

