ALTER TABLE data_product
    ADD rest_client_identifier_code VARCHAR(50);

ALTER TABLE data_product
    ADD rest_client_method_code VARCHAR(50);

ALTER TABLE data_product
    ADD rest_client_path VARCHAR(1000);

ALTER TABLE data_product
    ADD rest_client_request_template VARCHAR(1000);

UPDATE data_product
SET rest_client_identifier_code = 'AGIS_STRUCTURE',
    rest_client_method_code = 'POST',
    rest_client_path = 'structure',
    rest_client_request_template = '{
                                  "surveyYear": "{{year}}",
                                  "ids": {
                                    "ber": "{{bur}}"
                                  },
                                  "dataTypes": {
                                    "structureType": ["animalData"]
                                  }
                                }'
WHERE id = '085e4b72-964d-4bd5-a3c9-224d8c5585af'
