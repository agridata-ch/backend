-- Add provider signature fields to contract_revision table
ALTER TABLE contract_revision
    ADD COLUMN provider_signature_user_id1   UUID,
    ADD COLUMN provider_signature_name1      VARCHAR(255),
    ADD COLUMN provider_signature_timestamp1 TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN provider_signature_user_id2   UUID,
    ADD COLUMN provider_signature_name2      VARCHAR(255),
    ADD COLUMN provider_signature_timestamp2 TIMESTAMP WITHOUT TIME ZONE;
