ALTER TABLE data_request
    ADD data_consumer_city VARCHAR(255);

ALTER TABLE data_request
    ADD data_consumer_country VARCHAR(2);

ALTER TABLE data_request
    ADD data_consumer_legal_name VARCHAR(255);

ALTER TABLE data_request
    ADD data_consumer_display_name VARCHAR(255);

ALTER TABLE data_request
    ADD data_consumer_street VARCHAR(255);

ALTER TABLE data_request
    ADD data_consumer_uid numeric;

ALTER TABLE data_request
    ADD data_consumer_zip VARCHAR(10);

ALTER TABLE data_request
    DROP COLUMN data_consumer_id;