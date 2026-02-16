ALTER TABLE IF EXISTS data_provider
    ADD COLUMN uid VARCHAR(20);

CREATE INDEX idx_data_provider_uid
    ON data_provider (uid);

UPDATE data_provider
SET uid = 'CHE146680598'
WHERE id = '61404b83-078e-4b4f-a6d6-2aa3990f429c'