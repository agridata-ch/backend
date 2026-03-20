ALTER TABLE data_product
RENAME COLUMN rest_client_path TO rest_client_path_template;

ALTER TABLE data_product ADD COLUMN rest_client_change_detection_path_template VARCHAR(1000);

-- AGIS: person
UPDATE data_product
SET rest_client_change_detection_path_template = 'register-data/2/uids-with-changes?since={{LAST_CHANGED_SINCE_DATE_TIME}}&type=person&type=personToPersonRelations&type=personToFarmRelations'
WHERE id = 'c661ea48-106d-4d7a-a5d1-a9a6db48dd8c';

-- AGIS: farm
UPDATE data_product
SET rest_client_change_detection_path_template = 'register-data/2/uids-with-changes?since={{LAST_CHANGED_SINCE_DATE_TIME}}&type=farm&type=farmToFarmRelations&type=personToFarmRelations'
WHERE id IN ('147e8c40-78cc-4db3-a909-65504aa62a64', '7911d98d-59eb-4cf4-be61-bfe77fe9117e');

-- AGIS: networkSurfaceData
UPDATE data_product
SET rest_client_change_detection_path_template = 'structure-data/2/uids-with-changes?since={{LAST_CHANGED_SINCE_DATE_TIME}}&type=networkSurfaceData'
WHERE id = '1dad9f91-30d8-45c9-8c82-ad72f4cb22e7';

-- AGIS: biodiversitySurfaceData
UPDATE data_product
SET rest_client_change_detection_path_template = 'structure-data/2/uids-with-changes?since={{LAST_CHANGED_SINCE_DATE_TIME}}&type=biodiversitySurfaceData'
WHERE id = '64e39df0-2e56-4204-9c44-a43e1e26a2e8';

-- AGIS: generalSurfaceData
UPDATE data_product
SET rest_client_change_detection_path_template = 'structure-data/2/uids-with-changes?since={{LAST_CHANGED_SINCE_DATE_TIME}}&type=generalSurfaceData'
WHERE id = '0a808700-d89e-4fa0-a2b8-8edb15f3addd';

-- AGIS: animalSummeringData
UPDATE data_product
SET rest_client_change_detection_path_template = 'structure-data/2/uids-with-changes?since={{LAST_CHANGED_SINCE_DATE_TIME}}&type=animalSummeringData'
WHERE id = 'a795d0b0-f177-4bb4-8e41-1ed12d358c79';

-- AGIS: grapeSurfaceData
UPDATE data_product
SET rest_client_change_detection_path_template = 'structure-data/2/uids-with-changes?since={{LAST_CHANGED_SINCE_DATE_TIME}}&type=grapeSurfaceData'
WHERE id = '2375219c-5fe3-458f-bd07-d3c2c87e2539';

-- AGIS: slopeSurfaceData
UPDATE data_product
SET rest_client_change_detection_path_template = 'structure-data/2/uids-with-changes?since={{LAST_CHANGED_SINCE_DATE_TIME}}&type=slopeSurfaceData'
WHERE id = 'ef4f42dd-eaa9-4af1-988c-86b47bd963fe';

-- AGIS: animalData
UPDATE data_product
SET rest_client_change_detection_path_template = 'structure-data/2/uids-with-changes?since={{LAST_CHANGED_SINCE_DATE_TIME}}&type=animalData'
WHERE id = '085e4b72-964d-4bd5-a3c9-224d8c5585af';

-- AGIS: ecoetho
UPDATE data_product
SET rest_client_change_detection_path_template = 'eco-etho-data/2/uids-with-changes?since={{LAST_CHANGED_SINCE_DATE_TIME}}&type=controlOrganizationName&type=programName'
WHERE id = '46f8a883-da7c-49b3-b986-10a24b1e09ef';
