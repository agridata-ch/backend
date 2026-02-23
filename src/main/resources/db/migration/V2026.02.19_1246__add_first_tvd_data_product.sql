-- ===============================================
-- data_provider
-- ===============================================
INSERT INTO public.data_provider (id, archived, created_by, created_at, modified_by, modified_at, code, name, uid)
VALUES
('e37b148b-9a0f-4c2e-80c5-fe9c9416b640', false, null, '2026-01-18 09:07:31.337715', null, '2026-01-29 09:07:31.337715', 'IDENTITAS', '{"de": "Identitas", "fr": "Identitas", "it": "Identitas"}', 'CHE105031830')
ON CONFLICT (id) DO UPDATE SET archived = EXCLUDED.archived, modified_by = EXCLUDED.modified_by, modified_at = EXCLUDED.modified_at, code = EXCLUDED.code, name = EXCLUDED.name, uid = EXCLUDED.uid;

-- ===============================================
-- data_source_system
-- ===============================================
INSERT INTO public.data_source_system (id, archived, created_by, created_at, modified_by, modified_at, data_provider_id, code, name)
VALUES
('4ccbfa06-a547-4a76-9dfc-61c22a4ea8ce', false, null, '2026-01-18 09:07:31.337715', null, '2026-01-29 09:07:31.337715', 'e37b148b-9a0f-4c2e-80c5-fe9c9416b640', 'TVD', '{"de": "TVD", "fr": "BDTA", "it": "BDTA"}')
ON CONFLICT (id) DO UPDATE SET archived = EXCLUDED.archived, modified_by = EXCLUDED.modified_by, modified_at = EXCLUDED.modified_at, data_provider_id = EXCLUDED.data_provider_id, code = EXCLUDED.code, name = EXCLUDED.name;

-- ===============================================
-- data_product
-- ===============================================
INSERT INTO data_product (id, archived, created_at, modified_at, data_source_system_id, name, description, rest_client_identifier_code, rest_client_method_code, rest_client_path, flow_code)
VALUES
('298b653c-b326-40d3-a3d1-97e2e9d9ca22'::uuid, false, NOW(), NOW(), '4ccbfa06-a547-4a76-9dfc-61c22a4ea8ce', jsonb_build_object('de', 'TVD_EquidOwnershipListV1', 'fr', 'TVD_EquidOwnershipListV1', 'it', 'TVD_EquidOwnershipListV1'), jsonb_build_object('de', 'tbd', 'fr', 'tbd', 'it', 'tbd'), 'TVD_ANIMAL_TRACING_API', 'GET', 'v1.0/equid/shared-data/legalunits/{{uid}}/ownership?dataPackage=TVD_EquidOwnershipListV1', 'UID_BASED_POST_VALIDATION')
ON CONFLICT (id) DO UPDATE SET archived = EXCLUDED.archived, modified_at = EXCLUDED.modified_at, data_source_system_id = EXCLUDED.data_source_system_id, name = EXCLUDED.name, description = EXCLUDED.description;
