-- ===============================================
-- data_provider
-- ===============================================
INSERT INTO public.data_provider (id, archived, created_by, created_at, modified_by, modified_at, code, name, uid)
VALUES ('3bbc6006-1697-4a5f-8cba-2d34fbc278db', false, null, '2026-06-03 09:00:00.000000', null,
        '2026-06-03 09:00:00.000000', 'BLV', '{
    "de": "BLV",
    "fr": "OSAV",
    "it": "USAV"
  }', 'CHE403244345')
ON CONFLICT (id) DO UPDATE SET archived    = EXCLUDED.archived,
                               modified_by = EXCLUDED.modified_by,
                               modified_at = EXCLUDED.modified_at,
                               code        = EXCLUDED.code,
                               name        = EXCLUDED.name,
                               uid         = EXCLUDED.uid;

-- ===============================================
-- data_source_system
-- ===============================================
INSERT INTO public.data_source_system (id, archived, created_by, created_at, modified_by, modified_at, data_provider_id,
                                       code, name)
VALUES ('7b8fe538-e8cf-472c-a171-c24e8fb8a704', false, null, '2026-06-03 09:00:00.000000', null,
        '2026-06-03 09:00:00.000000', '3bbc6006-1697-4a5f-8cba-2d34fbc278db', 'ACONTROL-BLV', '{
    "de": "Acontrol",
    "fr": "Acontrol",
    "it": "Acontrol"
  }')
ON CONFLICT (id) DO UPDATE SET archived         = EXCLUDED.archived,
                               modified_by      = EXCLUDED.modified_by,
                               modified_at      = EXCLUDED.modified_at,
                               data_provider_id = EXCLUDED.data_provider_id,
                               code             = EXCLUDED.code,
                               name             = EXCLUDED.name;

-- ===============================================
-- data_product
-- ===============================================

UPDATE data_product
SET data_source_system_id = '7b8fe538-e8cf-472c-a171-c24e8fb8a704'
WHERE id IN ('96787125-3de0-4b5f-b9c7-8f1e5b3db516', 'df72eb69-fa78-4b52-8649-7068940019fe',
             '905170b2-1866-41f2-a8dd-57ba67a1f7bc')