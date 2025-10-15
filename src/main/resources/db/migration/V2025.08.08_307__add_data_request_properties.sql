ALTER TABLE data_request
    ADD human_friendly_id VARCHAR(4);

ALTER TABLE data_request
    ALTER COLUMN human_friendly_id SET NOT NULL;

ALTER TABLE data_request
    ADD CONSTRAINT uc_data_request_human_friendly UNIQUE (human_friendly_id);

ALTER TABLE data_request
    ADD submission_date TIMESTAMP WITHOUT TIME ZONE;
