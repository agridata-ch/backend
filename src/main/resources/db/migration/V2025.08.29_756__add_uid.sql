ALTER TABLE users
    ADD uid VARCHAR(20);

ALTER TABLE data_request
    DROP COLUMN data_consumer_uid;

ALTER TABLE data_request
    ADD data_consumer_uid VARCHAR(20);
