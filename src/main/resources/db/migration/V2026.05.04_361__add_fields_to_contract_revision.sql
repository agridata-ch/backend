ALTER TABLE contract_revision
    ADD COLUMN contact_email_address VARCHAR(255) NOT NULL,
    ADD COLUMN contact_phone_number VARCHAR(50) NOT NULL,
    ADD COLUMN description JSONB NOT NULL,
    ADD COLUMN purpose JSONB NOT NULL,
    ADD COLUMN title JSONB NOT NULL,
    ADD COLUMN target_group VARCHAR(150) NOT NULL,
    ADD COLUMN data_consumer_uid VARCHAR(20) NOT NULL,
    ADD COLUMN system_name JSONB NOT NULL,
    ADD COLUMN data_product JSONB NOT NULL;