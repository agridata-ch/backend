CREATE TABLE audit_log
(
    id               UUID                        NOT NULL,
    archived         BOOLEAN                     NOT NULL,
    timestamp        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    actor_type_code  VARCHAR(50)                 NOT NULL,
    actor_id         VARCHAR(255),
    action_code      VARCHAR(50)                 NOT NULL,
    entity_type_code VARCHAR(50)                 NOT NULL,
    entity_id        UUID,
    request_id       VARCHAR(255),
    CONSTRAINT pk_audit_log PRIMARY KEY (id)
);

ALTER TABLE participant
    ADD agate_login_id VARCHAR(50);

ALTER TABLE participant
    ADD email VARCHAR(255);

ALTER TABLE consent_request
    DROP COLUMN created_by;

ALTER TABLE consent_request
    DROP COLUMN modified_by;

ALTER TABLE consent_request
    ADD created_by UUID;

ALTER TABLE data_request
    DROP COLUMN created_by;

ALTER TABLE data_request
    DROP COLUMN modified_by;

ALTER TABLE data_request
    ADD created_by UUID;

ALTER TABLE participant
    DROP COLUMN created_by;

ALTER TABLE participant
    DROP COLUMN modified_by;

ALTER TABLE participant
    ADD created_by UUID;

ALTER TABLE consent_request
    ADD modified_by UUID;

ALTER TABLE data_request
    ADD modified_by UUID;

ALTER TABLE participant
    ADD modified_by UUID;
