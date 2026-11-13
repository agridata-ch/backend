ALTER TABLE contract_revision
    ADD contract_template_version VARCHAR(20);
UPDATE contract_revision
    SET contract_template_version = '1.0'
    WHERE contract_template_version IS NULL;
ALTER TABLE contract_revision
    ALTER COLUMN contract_template_version SET NOT NULL;
