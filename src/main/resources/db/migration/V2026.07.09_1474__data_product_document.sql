CREATE TABLE data_product_document
(
    id                UUID         NOT NULL,
    archived          BOOLEAN      NOT NULL,
    created_by        UUID,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by       UUID,
    modified_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    data_product_id   UUID         NOT NULL,
    original_filename VARCHAR(1024) NOT NULL,
    size_bytes        BIGINT       NOT NULL,
    scan_status       VARCHAR(255) NOT NULL,
    CONSTRAINT pk_data_product_document PRIMARY KEY (id)
);

ALTER TABLE data_product_document
    ADD CONSTRAINT FK_DATA_PRODUCT_DOCUMENT_ON_DATA_PRODUCT FOREIGN KEY (data_product_id) REFERENCES data_product (id);

CREATE INDEX idx_data_product_document_data_product_id ON data_product_document (data_product_id);
