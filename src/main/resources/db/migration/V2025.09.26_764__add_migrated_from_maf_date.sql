ALTER TABLE consent_request
    ADD migrated_from_maf_date TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE consent_request
    ADD migrated_from_maf_ktidp JSONB;

ALTER TABLE consent_request
    DROP COLUMN previous_state_code;
