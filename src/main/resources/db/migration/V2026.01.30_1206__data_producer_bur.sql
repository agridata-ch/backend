ALTER TABLE consent_request
    ADD COLUMN data_producer_bur VARCHAR(50);

DROP INDEX uk_consent_request_data_request_data_producer_unarchived;

CREATE UNIQUE INDEX uk_consent_request_active_uid_only
    ON consent_request (data_request_id, data_producer_uid)
    WHERE (archived = false) AND (data_producer_bur IS NULL);

CREATE UNIQUE INDEX uk_consent_request_active_uid_bur
    ON consent_request (data_request_id, data_producer_uid, data_producer_bur)
    WHERE (archived = false) AND (data_producer_bur IS NOT NULL);
