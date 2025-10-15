ALTER TABLE data_product
    ADD data_source_product_id VARCHAR(255);

UPDATE data_product
SET data_source_product_id = CASE name->>'de'
    WHEN 'R01' THEN 'not_set'
    WHEN 'R02' THEN 'not_set'
    WHEN 'R03' THEN 'not_set'
    WHEN 'S01' THEN 'animalData'
    WHEN 'S02' THEN 'animalSummeringData'
    WHEN 'S03' THEN 'generalSurfaceData'
    WHEN 'S04' THEN 'slopeSurfaceData'
    WHEN 'S05' THEN 'grapeSurfaceData'
    WHEN 'S06' THEN 'biodiversitySurfaceData'
    WHEN 'S07' THEN 'networkSurfaceData'
    WHEN 'A01' THEN 'not_set'
    ELSE 'not_set'
END;

ALTER TABLE data_product
    ALTER COLUMN data_source_product_id SET NOT NULL;

