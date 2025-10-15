ALTER TABLE data_request
    ADD contact_email_address VARCHAR(255);

ALTER TABLE data_request
    ADD contact_phone_number VARCHAR(50);

ALTER TABLE data_request
    ADD data_consumer_logo BYTEA;

ALTER TABLE data_request
    ADD data_consumer_logo_type VARCHAR(50);
