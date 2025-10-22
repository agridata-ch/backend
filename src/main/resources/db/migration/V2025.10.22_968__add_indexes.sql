CREATE INDEX idx_audit_log_actor_id ON audit_log (actor_id);

CREATE INDEX idx_audit_log_request_id ON audit_log (request_id);

CREATE INDEX idx_consent_request_data_producer_uid ON consent_request (data_producer_uid);

CREATE INDEX idx_data_product_data_source_system_code ON data_product (data_source_system_code);

CREATE INDEX idx_data_request_data_consumer_uid ON data_request (data_consumer_uid);

CREATE INDEX idx_users_agate_login_id ON users (agate_login_id);

CREATE INDEX idx_users_kt_id_p ON users (kt_id_p);

CREATE INDEX idx_users_uid ON users (uid);

DROP INDEX uk_consent_request_data_request_unarchived;

CREATE UNIQUE INDEX uk_consent_request_data_request_data_producer_unarchived
ON consent_request (data_request_id, data_producer_uid)
WHERE archived = false;
