ALTER TABLE contract_revision
    ADD data_request_human_friendly_id VARCHAR(4);

-- Backfill
UPDATE contract_revision cr
SET data_request_human_friendly_id = dr.human_friendly_id
FROM data_request dr
WHERE dr.id = cr.data_request_id;

ALTER TABLE contract_revision
    ALTER COLUMN data_request_human_friendly_id SET NOT NULL;