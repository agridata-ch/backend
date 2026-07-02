-- TODO: Remove this migration before merge to develop. No url column should exist and instead we want to fetch it directly from the rest_client configs

ALTER TABLE rest_client
    ADD url VARCHAR(255);

UPDATE rest_client
SET url = 'http://localhost:8050/tvd/animal-tracing'
WHERE id = '1c438fa1-1112-4ee9-b1af-2d96acf385f0';
UPDATE rest_client
SET url = 'http://localhost:8050/tvd/zo'
WHERE id = 'cadf12a3-af55-4919-8d30-6849ab6c13ba';
UPDATE rest_client
SET url = 'http://localhost:8050/agis'
WHERE id = 'b1398c9d-c28d-4e7e-b5f0-f5d615a6471c';
UPDATE rest_client
SET url = 'http://localhost:8050/acontrol'
WHERE id = '5d3a4a87-63fc-4428-8044-313d222efe1d';

ALTER TABLE rest_client
    ALTER COLUMN url SET NOT NULL;