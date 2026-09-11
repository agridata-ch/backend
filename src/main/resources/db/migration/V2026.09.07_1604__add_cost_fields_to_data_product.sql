ALTER TABLE data_product
    ADD pricing_basis JSONB,
    ADD payment_required BOOLEAN;

-- backfill cost fields for existing data products
UPDATE data_product
SET payment_required = FALSE;

ALTER TABLE data_product
    ALTER COLUMN payment_required SET NOT NULL;