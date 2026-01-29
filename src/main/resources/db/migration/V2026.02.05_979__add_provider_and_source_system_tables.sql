-- 1) Create data_provider
CREATE TABLE IF NOT EXISTS data_provider
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    archived    BOOLEAN                     NOT NULL,
    created_by  UUID,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by UUID,
    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    code        VARCHAR(50)                 NOT NULL,
    name        JSONB                       NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_data_provider_code ON data_provider (code) WHERE archived = false;
CREATE INDEX IF NOT EXISTS idx_data_provider_archived ON data_provider (archived);

-- 2) Create data_source_system
CREATE TABLE IF NOT EXISTS data_source_system
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    archived         BOOLEAN                     NOT NULL,
    created_by       UUID,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by      UUID,
    modified_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    data_provider_id UUID                        NOT NULL,
    code             VARCHAR(50)                 NOT NULL,
    name             JSONB                       NULL
);

ALTER TABLE data_source_system
    ADD CONSTRAINT fk_data_source_system_provider
        FOREIGN KEY (data_provider_id) REFERENCES data_provider (id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_data_source_system_code ON data_source_system (code) WHERE archived = false;
CREATE INDEX IF NOT EXISTS idx_data_source_system_provider_id ON data_source_system (data_provider_id);
CREATE INDEX IF NOT EXISTS idx_data_source_system_archived ON data_source_system (archived);

-- 3) Add FK column to data_product (nullable for backfill)
ALTER TABLE data_product
    ADD COLUMN IF NOT EXISTS data_source_system_id UUID NULL;

CREATE INDEX IF NOT EXISTS idx_data_product_data_source_system_id
    ON data_product (data_source_system_id);

-- 4) Seed provider row
INSERT INTO public.data_provider (id, archived, created_by, created_at, modified_by, modified_at, code, name)
VALUES ('61404b83-078e-4b4f-a6d6-2aa3990f429c', false, null, '2026-01-18 09:07:31.337715', null,
        '2026-01-29 09:07:31.337715', 'BLW', '{
    "de": "BLW",
    "fr": "OFAG",
    "it": "UFAG"
  }');

-- 5) Seed data_source_system row
INSERT INTO public.data_source_system (id, archived, created_by, created_at, modified_by, modified_at,
                                       data_provider_id, code, name)
VALUES ('5335d715-e95c-4777-a424-ab73f2ff5618', false, null, '2026-01-18 09:07:31.337715', null,
        '2026-01-29 09:07:31.337715', '61404b83-078e-4b4f-a6d6-2aa3990f429c', 'AGIS', '{
    "de": "AGIS",
    "fr": "SIPA",
    "it": "AGIS"
  }');

-- 6) Backfill data_product.data_source_system_id from the new data_source_system table
UPDATE data_product dp
SET data_source_system_id = ds.id
FROM data_source_system ds
WHERE ds.code = dp.data_source_system_code
  AND dp.data_source_system_id IS NULL;

-- 7) Enforce NOT NULL and add FK once data is backfilled
ALTER TABLE data_product
    ALTER COLUMN data_source_system_id SET NOT NULL;

ALTER TABLE data_product
    ADD CONSTRAINT fk_data_product_data_source_system
        FOREIGN KEY (data_source_system_id) REFERENCES data_source_system (id);

-- 8) Drop the old data_source_system_code column and index
DROP INDEX IF EXISTS idx_data_product_data_source_system_code;

ALTER TABLE data_product
    DROP COLUMN IF EXISTS data_source_system_code;