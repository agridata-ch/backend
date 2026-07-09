-- New base path for acontrol data products
UPDATE data_product
SET rest_client_path_template = 'inspection/1'
WHERE rest_client_identifier_code = 'ACONTROL_API';

-- Speeds up the BUR-based data-transfer flows: the consent check queries
-- consent_request by (data_request_id, data_producer_bur).
-- Partial on archived = false to match the @SQLRestriction applied to every query.
CREATE INDEX idx_consent_request_data_request_id_bur
    ON consent_request (data_request_id, data_producer_bur)
    WHERE archived = false;
