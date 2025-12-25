-- ===============================================
-- Test data reset for CHE103000001 (Max Mustermann)
-- ===============================================

-- Delete existing consent_requests for this UID
DELETE FROM consent_request WHERE data_producer_uid = 'CHE103000001';

-- Insert consent_requests for CHE103000001
INSERT INTO consent_request (id, archived, created_at, modified_at, data_producer_uid, state_code, data_request_id, request_date, last_state_change_date) VALUES
('5e439777-8564-4954-9d01-7ebeabf4fc39'::uuid, false, NOW(), NOW(), 'CHE103000001', 'DECLINED', '98a35e61-0162-4986-9e9e-ee5c65f86316'::uuid, '2025-01-30 10:10:10'::timestamp, '2025-02-02 11:11:11'::timestamp),
('235aed61-7da2-41ac-94c4-bcca91328ad6'::uuid, false, NOW(), NOW(), 'CHE103000001', 'GRANTED', '81ae8571-9497-413a-99c5-237e72621ca7'::uuid, '2025-03-22 08:20:00'::timestamp, '2025-03-25 10:15:00'::timestamp),
('0dd93ea9-5a14-40a4-a36e-eafb2585ece3'::uuid, false, NOW(), NOW(), 'CHE103000001', 'OPENED', '341f558a-781c-4eb5-bab7-c2f39216b9f2'::uuid, '2025-06-28 14:00:00'::timestamp, NULL::timestamp);

-- Set migrated_from_maf for BioSuisse consent-requests (if applicable)
UPDATE consent_request cr
SET migrated_from_maf_date = '2025-09-01 00:00:00'::timestamp
FROM data_request dr
WHERE cr.data_request_id = dr.id
  AND cr.data_producer_uid = 'CHE103000001'
  AND dr.id IN ('3da3a459-d3c2-48af-b8d0-02bc95146468','81ae8571-9497-413a-99c5-237e72621ca7','85ca6584-b480-4941-84ed-1b81d24adbf1');

