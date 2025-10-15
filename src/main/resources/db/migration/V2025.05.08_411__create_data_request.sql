CREATE TABLE consent_request
(
    id                UUID                        NOT NULL,
    archived          BOOLEAN                     NOT NULL,
    created_by        INTEGER                     NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by       INTEGER                     NOT NULL,
    modified_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    request_date      TIMESTAMP WITHOUT TIME ZONE,
    state             VARCHAR(50)                 NOT NULL,
    data_producer_uid VARCHAR(50)                 NOT NULL,
    data_request_id   UUID                        NOT NULL,
    CONSTRAINT pk_consent_request PRIMARY KEY (id)
);

CREATE TABLE data_consumer
(
    bur         VARCHAR(50)                 NOT NULL,
    archived    BOOLEAN                     NOT NULL,
    created_by  INTEGER                     NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by INTEGER                     NOT NULL,
    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name        VARCHAR(1000)               NOT NULL,
    CONSTRAINT pk_data_consumer PRIMARY KEY (bur)
);

CREATE TABLE data_request
(
    id                UUID                        NOT NULL,
    archived          BOOLEAN                     NOT NULL,
    created_by        INTEGER                     NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by       INTEGER                     NOT NULL,
    modified_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    description_de    VARCHAR(4000),
    description_fr    VARCHAR(4000),
    description_it    VARCHAR(4000),
    purpose_de        VARCHAR(4000),
    purpose_fr        VARCHAR(4000),
    purpose_it        VARCHAR(4000),
    data_consumer_bur VARCHAR(50),
    CONSTRAINT pk_data_request PRIMARY KEY (id)
);

ALTER TABLE consent_request
    ADD CONSTRAINT FK_CONSENT_REQUEST_ON_DATA_REQUEST FOREIGN KEY (data_request_id) REFERENCES data_request (id);
