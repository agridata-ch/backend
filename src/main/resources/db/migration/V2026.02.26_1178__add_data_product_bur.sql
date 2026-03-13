INSERT INTO data_product (id, archived, created_at, modified_at, data_source_system_id, name, description, rest_client_identifier_code, rest_client_method_code, rest_client_path, flow_code)
VALUES
('e08af9d2-99ec-41b3-a77c-d4457415944f'::uuid, false, NOW(), NOW(), '4ccbfa06-a547-4a76-9dfc-61c22a4ea8ce', jsonb_build_object('de', 'TVD_FarmDataV1', 'fr', 'TVD_FarmDataV1', 'it', 'TVD_FarmDataV1'), jsonb_build_object('de', 'tbd', 'fr', 'tbd', 'it', 'tbd'), 'TVD_ANIMAL_TRACING_API', 'GET', 'v1.0/customer/shared-data/localunits/{{bur}}?dataPackage=TVD_FarmDataV1&recipientUid={{recipientUid}}', 'BUR_BASED_POST_VALIDATION')
ON CONFLICT (id) DO UPDATE SET archived = EXCLUDED.archived, modified_at = EXCLUDED.modified_at, data_source_system_id = EXCLUDED.data_source_system_id, name = EXCLUDED.name, description = EXCLUDED.description;

UPDATE data_product
SET rest_client_path = 'v1.0/equid/shared-data/legalunits/{{uid}}/ownership?dataPackage=TVD_EquidOwnershipListV1&recipientUid={{recipientUid}}'
WHERE id = '298b653c-b326-40d3-a3d1-97e2e9d9ca22'::uuid
