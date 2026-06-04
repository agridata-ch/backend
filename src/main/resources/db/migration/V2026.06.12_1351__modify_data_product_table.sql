-- Column rest_client_id --
ALTER TABLE data_product
    ADD rest_client_id UUID;

ALTER TABLE data_product
    ADD CONSTRAINT FK_DATA_PRODUCT_ON_REST_CLIENT FOREIGN KEY (rest_client_id) REFERENCES rest_client (id);

UPDATE data_product dp
SET rest_client_id = rc.id
FROM rest_client rc
WHERE dp.rest_client_identifier_code = rc.code;

-- Column state_code --
ALTER TABLE data_product
    ADD state_code VARCHAR(255);

UPDATE data_product
SET state_code = 'ACTIVE';

ALTER TABLE data_product
    ALTER COLUMN state_code SET NOT NULL;

-- Column uid --
ALTER TABLE data_product
    ADD COLUMN data_provider_uid varchar(20);

UPDATE data_product dp
SET data_provider_uid = dpr.uid
FROM data_source_system dss
         JOIN data_provider dpr ON dpr.id = dss.data_provider_id
WHERE dp.data_source_system_id = dss.id;

-- Column data_source_system_id --

alter table data_product
    alter column data_source_system_id drop not null;