alter table data_request
    add data_source_system_id uuid;

-- 2) Backfill data_provider_id using the (single) distinct data_provider_id per request
UPDATE data_request dr
SET data_source_system_id = data_source_system_per_data_request.data_provider_id
FROM (SELECT dr.id                                     AS data_request_id,
             MIN(dp.data_source_system_id::text)::uuid AS data_provider_id
      FROM data_request dr
               JOIN data_request_data_product drdp
                    ON drdp.data_request_id = dr.id AND drdp.archived = false
               JOIN data_product dp
                    ON dp.id = drdp.data_product_id AND dp.archived = false
      WHERE dr.archived = false
      GROUP BY dr.id) data_source_system_per_data_request
WHERE dr.id = data_source_system_per_data_request.data_request_id
  AND dr.data_source_system_id IS NULL;

alter table data_request
    drop column data_provider_id;
