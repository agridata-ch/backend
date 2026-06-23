ALTER TABLE data_request
    ADD advantages JSONB NOT NULL DEFAULT '[]'::jsonb;

ALTER TABLE data_request
    ALTER COLUMN advantages DROP DEFAULT;
