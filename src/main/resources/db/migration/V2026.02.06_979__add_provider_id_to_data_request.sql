-- 1) Add column (nullable for backfill)
ALTER TABLE data_request
    ADD COLUMN IF NOT EXISTS data_provider_id UUID;

CREATE INDEX IF NOT EXISTS idx_data_request_data_provider_id
    ON data_request (data_provider_id);

-- 2) Backfill data_provider_id using the (single) distinct data_provider_id per request
UPDATE data_request dr
SET data_provider_id = provider_per_data_request.data_provider_id
FROM (SELECT dr.id                                 AS data_request_id,
             MIN(dss.data_provider_id::text)::uuid AS data_provider_id
      FROM data_request dr
               JOIN data_request_data_product drdp
                    ON drdp.data_request_id = dr.id AND drdp.archived = false
               JOIN data_product dp
                    ON dp.id = drdp.data_product_id AND dp.archived = false
               JOIN data_source_system dss
                    ON dss.id = dp.data_source_system_id
      WHERE dr.archived = false
      GROUP BY dr.id) provider_per_data_request
WHERE dr.id = provider_per_data_request.data_request_id
  AND dr.data_provider_id IS NULL;