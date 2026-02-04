ALTER TABLE data_product
    ADD flow_code VARCHAR(100);

UPDATE data_product
    SET flow_code = 'UID_DIRECT';

UPDATE data_product
    SET rest_client_identifier_code = 'AGIS_API';

UPDATE data_product
    SET rest_client_path = 'register-data/1/register'
    WHERE rest_client_path = 'register';

UPDATE data_product
    SET rest_client_path = 'structure-data/1/structure'
    WHERE rest_client_path = 'structure';

UPDATE data_product
    SET rest_client_path = 'eco-etho-data/1/ecoetho'
    WHERE rest_client_path = 'ecoetho';
