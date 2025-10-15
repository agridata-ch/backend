ALTER TABLE data_request
    ADD title JSONB;

ALTER TABLE data_request
    ADD description JSONB;

ALTER TABLE data_request
    ADD purpose JSONB;

ALTER TABLE data_request
    DROP COLUMN title_de;

ALTER TABLE data_request
    DROP COLUMN title_fr;

ALTER TABLE data_request
    DROP COLUMN title_it;

ALTER TABLE data_request
    DROP COLUMN description_de;

ALTER TABLE data_request
    DROP COLUMN description_fr;

ALTER TABLE data_request
    DROP COLUMN description_it;

ALTER TABLE data_request
    DROP COLUMN purpose_de;

ALTER TABLE data_request
    DROP COLUMN purpose_fr;

ALTER TABLE data_request
    DROP COLUMN purpose_it;