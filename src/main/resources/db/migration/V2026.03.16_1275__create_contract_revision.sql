CREATE TABLE contract_revision
(
    id                 UUID                        NOT NULL,
    archived           BOOLEAN                     NOT NULL,
    created_by         UUID,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by        UUID,
    modified_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    data_request_id    UUID                        NOT NULL,
    data_consumer_name VARCHAR(255)                NOT NULL,
    data_provider_name VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_contract_revision PRIMARY KEY (id)
);

ALTER TABLE data_request
    ADD current_contract_revision_id UUID;

ALTER TABLE data_request
    ADD CONSTRAINT uc_data_request_current_contract_revision UNIQUE (current_contract_revision_id);

ALTER TABLE contract_revision
    ADD CONSTRAINT FK_CONTRACT_REVISION_ON_DATA_REQUEST FOREIGN KEY (data_request_id) REFERENCES data_request (id);

ALTER TABLE data_request
    ADD CONSTRAINT FK_DATA_REQUEST_ON_CURRENT_CONTRACT_REVISION FOREIGN KEY (current_contract_revision_id) REFERENCES contract_revision (id);