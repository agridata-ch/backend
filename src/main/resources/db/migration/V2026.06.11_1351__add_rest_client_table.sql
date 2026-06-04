CREATE TABLE data_provider_rest_client
(
    data_provider_id UUID NOT NULL,
    rest_client_id   UUID NOT NULL,
    CONSTRAINT pk_data_provider_rest_client PRIMARY KEY (data_provider_id, rest_client_id)
);

CREATE TABLE rest_client
(
    id          UUID                        NOT NULL,
    archived    BOOLEAN                     NOT NULL,
    created_by  UUID,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by UUID,
    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    code        VARCHAR(50)                 NOT NULL,
    CONSTRAINT pk_rest_client PRIMARY KEY (id)
);

ALTER TABLE data_provider_rest_client
    ADD CONSTRAINT fk_datprorescli_on_data_provider_entity FOREIGN KEY (data_provider_id) REFERENCES data_provider (id);

ALTER TABLE data_provider_rest_client
    ADD CONSTRAINT fk_datprorescli_on_rest_client_entity FOREIGN KEY (rest_client_id) REFERENCES rest_client (id);

INSERT INTO rest_client (id, archived, created_at, modified_at, code)
VALUES ('1c438fa1-1112-4ee9-b1af-2d96acf385f0'::uuid,
        false,
        NOW(),
        NOW(),
        'TVD_ANIMAL_TRACING_API'),
       ('cadf12a3-af55-4919-8d30-6849ab6c13ba'::uuid,
        false,
        NOW(),
        NOW(),
        'TVD_ZO_API'),
       ('b1398c9d-c28d-4e7e-b5f0-f5d615a6471c'::uuid,
        false,
        NOW(),
        NOW(),
        'AGIS_API'),
       ('5d3a4a87-63fc-4428-8044-313d222efe1d'::uuid,
        false,
        NOW(),
        NOW(),
        'ACONTROL_API');

INSERT INTO data_provider_rest_client (data_provider_id, rest_client_id)
VALUES ('61404b83-078e-4b4f-a6d6-2aa3990f429c'::uuid,
        '5d3a4a87-63fc-4428-8044-313d222efe1d'::uuid),
       ('61404b83-078e-4b4f-a6d6-2aa3990f429c'::uuid,
        'b1398c9d-c28d-4e7e-b5f0-f5d615a6471c'::uuid),
       ('e37b148b-9a0f-4c2e-80c5-fe9c9416b640'::uuid,
        '1c438fa1-1112-4ee9-b1af-2d96acf385f0'::uuid),
       ('e37b148b-9a0f-4c2e-80c5-fe9c9416b640'::uuid,
        'cadf12a3-af55-4919-8d30-6849ab6c13ba'::uuid),
       ('3bbc6006-1697-4a5f-8cba-2d34fbc278db'::uuid,
        '5d3a4a87-63fc-4428-8044-313d222efe1d'::uuid);