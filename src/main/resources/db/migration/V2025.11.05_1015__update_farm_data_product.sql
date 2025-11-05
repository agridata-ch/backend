UPDATE data_product
SET rest_client_request_template = '{"farmSearchParameters":{"uid":"{{uid}}"},"dataRequestType":{"relationDepth":"personToFarmRelations"}}'
WHERE id = '147e8c40-78cc-4db3-a909-65504aa62a64';
