ALTER TABLE data_request
    ADD state_code VARCHAR(255);

UPDATE data_request
    SET state_code = 'SUBMITTED'
    WHERE state_code IS NULL;

ALTER TABLE data_request
    ALTER COLUMN state_code SET NOT NULL;

ALTER TABLE data_request
    ADD data_consumer_uid VARCHAR(16);

ALTER TABLE data_request
    ALTER COLUMN data_consumer_id SET NOT NULL;

CREATE TABLE data_product
(
    id                      UUID                        NOT NULL,
    archived                BOOLEAN                     NOT NULL,
    created_by              UUID,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by             UUID,
    modified_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    data_provider_id        UUID,
    data_source_system_code VARCHAR(255),
    name                    JSONB,
    description             JSONB,
    CONSTRAINT pk_data_product PRIMARY KEY (id)
);

CREATE TABLE data_request_data_product
(
    id              UUID                        NOT NULL,
    archived        BOOLEAN                     NOT NULL,
    created_by      UUID,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by     UUID,
    modified_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    data_request_id UUID                        NOT NULL,
    data_product_id UUID                        NOT NULL,
    CONSTRAINT pk_data_request_data_product PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_datarequest_product_unarchived
ON data_request_data_product (data_request_id, data_product_id)
WHERE archived = false;

ALTER TABLE data_request_data_product
    ADD CONSTRAINT FK_DATA_REQUEST_DATA_PRODUCT_ON_DATA_REQUEST FOREIGN KEY (data_request_id) REFERENCES data_request (id);