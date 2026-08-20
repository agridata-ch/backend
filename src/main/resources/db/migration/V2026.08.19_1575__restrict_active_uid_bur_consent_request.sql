DROP INDEX uk_consent_request_active_uid_bur;

CREATE UNIQUE INDEX uk_consent_request_active_uid_bur
    ON consent_request (data_request_id, data_producer_uid, data_producer_bur)
    WHERE (archived = false) AND (data_producer_bur IS NOT NULL) AND (uid_bur_relation_until IS NULL);
