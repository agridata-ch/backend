-- Add JSON columns for product links and technical description
ALTER TABLE data_product ADD COLUMN IF NOT EXISTS links JSONB;
ALTER TABLE data_product ADD COLUMN IF NOT EXISTS technical_description JSONB;
