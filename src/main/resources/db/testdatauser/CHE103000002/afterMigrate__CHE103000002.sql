-- ===============================================
-- Test data reset for CHE103000002 (Lara Beispiel)
-- ===============================================

-- Delete existing consent_requests for this UID
DELETE FROM consent_request WHERE data_producer_uid = 'CHE103000002';

-- Insert consent_requests for CHE103000002
INSERT INTO consent_request (id, archived, created_at, modified_at, data_producer_uid, state_code, data_request_id, request_date, last_state_change_date) VALUES
('adbc6d5a-331d-4dd9-b80c-ee1945716293'::uuid, false, NOW(), NOW(), 'CHE103000002', 'DECLINED', '98a35e61-0162-4986-9e9e-ee5c65f86316'::uuid, '2025-04-25 12:12:12'::timestamp, '2025-04-30 17:00:00'::timestamp),
('629322ea-19ea-42a2-8bb9-1d1d2e47dda0'::uuid, false, NOW(), NOW(), 'CHE103000002', 'GRANTED', '341f558a-781c-4eb5-bab7-c2f39216b9f2'::uuid, '2025-05-15 10:30:00'::timestamp, '2025-05-18 14:20:00'::timestamp),
('584c3587-517b-49ef-aaec-cb6e0179f78c'::uuid, false, NOW(), NOW(), 'CHE103000002', 'OPENED', '81ae8571-9497-413a-99c5-237e72621ca7'::uuid, '2025-06-20 09:15:00'::timestamp, NULL::timestamp);

-- Set migrated_from_maf for BioSuisse consent-requests (if applicable)
UPDATE consent_request cr
SET migrated_from_maf_date = '2025-09-01 00:00:00'::timestamp
FROM data_request dr
WHERE cr.data_request_id = dr.id
  AND cr.data_producer_uid = 'CHE103000002'
  AND dr.id IN ('3da3a459-d3c2-48af-b8d0-02bc95146468','81ae8571-9497-413a-99c5-237e72621ca7','85ca6584-b480-4941-84ed-1b81d24adbf1');

