CREATE TABLE participant
(
    id          UUID                        NOT NULL,
    archived    BOOLEAN                     NOT NULL,
    created_by  INTEGER                     NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by INTEGER                     NOT NULL,
    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    kt_id_p     VARCHAR(50),
    uid         VARCHAR(50),
    name        VARCHAR(1000),
    CONSTRAINT pk_participant PRIMARY KEY (id)
);

ALTER TABLE data_request
    ADD data_consumer_id UUID;

ALTER TABLE data_request
    ADD number VARCHAR(50);

ALTER TABLE consent_request
    ADD data_producer_id UUID;

ALTER TABLE consent_request
    ADD state_code VARCHAR(50);

ALTER TABLE consent_request
    ALTER COLUMN data_producer_id SET NOT NULL;

ALTER TABLE consent_request
    ALTER COLUMN state_code SET NOT NULL;

DROP TABLE data_consumer CASCADE;

ALTER TABLE data_request
    DROP COLUMN data_consumer_bur;

ALTER TABLE consent_request
    DROP COLUMN data_producer_uid;

ALTER TABLE consent_request
    DROP COLUMN state;
