-- ===============================================

-- Test data reset for CHE101000001 (Erika Musterfrau)
-- ===============================================

-- Delete existing consent_requests for this UID
DELETE FROM consent_request WHERE data_producer_uid = 'CHE101000001';

-- Insert consent_requests for CHE101000001
INSERT INTO consent_request (id, archived, created_at, modified_at, data_producer_uid, state_code, data_request_id, request_date, last_state_change_date) VALUES
('07813a3a-7b8d-4b68-847b-f34ce7037397'::uuid, false, NOW(), NOW(), 'CHE101000001', 'DECLINED', '3da3a459-d3c2-48af-b8d0-02bc95146468'::uuid, '2025-03-14 10:12:33'::timestamp, '2025-03-20 14:25:00'::timestamp),
('5542ff84-ab93-417a-925a-9c7711a20fff'::uuid, false, NOW(), NOW(), 'CHE101000001', 'GRANTED', '341f558a-781c-4eb5-bab7-c2f39216b9f2'::uuid, '2025-04-18 14:30:00'::timestamp, '2025-04-21 10:00:00'::timestamp),
('ef35df35-2051-416a-98ad-47ab35c8a77c'::uuid, false, NOW(), NOW(), 'CHE101000001', 'OPENED', '98a35e61-0162-4986-9e9e-ee5c65f86316'::uuid, '2025-06-25 11:20:00'::timestamp, NULL::timestamp);

-- Set migrated_from_maf for BioSuisse consent-requests (if applicable)
UPDATE consent_request cr
SET migrated_from_maf_date = '2025-09-01 00:00:00'::timestamp
FROM data_request dr
WHERE cr.data_request_id = dr.id
  AND cr.data_producer_uid = 'CHE101000001'
  AND dr.id IN ('3da3a459-d3c2-48af-b8d0-02bc95146468','81ae8571-9497-413a-99c5-237e72621ca7','85ca6584-b480-4941-84ed-1b81d24adbf1');

