-- ===============================================
-- Test data reset for CHE102000002 (Sofia Mustermann)
-- ===============================================

-- Delete existing consent_requests for this UID
DELETE FROM consent_request WHERE data_producer_uid = 'CHE102000002';

-- Insert consent_requests for CHE102000002
INSERT INTO consent_request (id, archived, created_at, modified_at, data_producer_uid, state_code, data_request_id, request_date, last_state_change_date) VALUES
('f789e5ca-3b26-4ced-bcce-77df72ac06ac'::uuid, false, NOW(), NOW(), 'CHE102000002', 'GRANTED', '3da3a459-d3c2-48af-b8d0-02bc95146468'::uuid, '2025-07-02 08:00:00'::timestamp, NULL::timestamp),
('1272346f-7983-4845-b038-329116d67e08'::uuid, false, NOW(), NOW(), 'CHE102000002', 'OPENED', '81ae8571-9497-413a-99c5-237e72621ca7'::uuid, '2025-02-18 11:44:59'::timestamp, NULL::timestamp),
('fea87d49-857a-45e4-8274-fec6885697c4'::uuid, false, NOW(), NOW(), 'CHE102000002', 'DECLINED', '341f558a-781c-4eb5-bab7-c2f39216b9f2'::uuid, '2025-06-14 12:30:00'::timestamp, '2025-06-20 14:30:00'::timestamp),
('2f8ec662-9fce-417e-9b82-3ed042adb482'::uuid, false, NOW(), NOW(), 'CHE102000002', 'GRANTED', '98a35e61-0162-4986-9e9e-ee5c65f86316'::uuid, '2025-04-10 09:00:00'::timestamp, '2025-04-13 11:30:00'::timestamp);

-- Set migrated_from_maf for BioSuisse consent-requests (if applicable)
UPDATE consent_request cr
SET migrated_from_maf_date = '2025-09-01 00:00:00'::timestamp
FROM data_request dr
WHERE cr.data_request_id = dr.id
  AND cr.data_producer_uid = 'CHE102000002'
  AND dr.id IN ('3da3a459-d3c2-48af-b8d0-02bc95146468','81ae8571-9497-413a-99c5-237e72621ca7','85ca6584-b480-4941-84ed-1b81d24adbf1');

