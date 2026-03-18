CREATE TABLE otp_challenge
(
    id                    UUID                        NOT NULL,
    archived              BOOLEAN                     NOT NULL,
    created_by            UUID,
    created_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by           UUID,
    modified_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id               UUID                        NOT NULL,
    contract_revision_id  UUID                        NOT NULL,
    signature_slot_code   VARCHAR(255)                NOT NULL,
    otp_hash              VARCHAR(128)                NOT NULL,
    phone_number_snapshot VARCHAR(50)                 NOT NULL,
    expires_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    consumed_at           TIMESTAMP WITHOUT TIME ZONE,
    attempt_count         INTEGER                     NOT NULL,
    max_attempts          INTEGER                     NOT NULL,
    CONSTRAINT pk_otp_challenge PRIMARY KEY (id)
);

ALTER TABLE contract_revision
    ADD consumer_signature_name1 VARCHAR(255);

ALTER TABLE contract_revision
    ADD consumer_signature_name2 VARCHAR(255);

ALTER TABLE contract_revision
    ADD consumer_signature_timestamp1 TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE contract_revision
    ADD consumer_signature_timestamp2 TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE contract_revision
    ADD consumer_signature_user_id1 UUID;

ALTER TABLE contract_revision
    ADD consumer_signature_user_id2 UUID;