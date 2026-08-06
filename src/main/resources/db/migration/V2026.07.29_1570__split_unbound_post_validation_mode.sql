-- DIGIB2-1570: split UNBOUND_POST_VALIDATION into a UID-based and a BUR-based transfer mode.
-- Equid data products resolve producer identity via UID, all other previously-unbound products via BUR.

UPDATE data_product
SET flow_code = 'UNBOUND_UID_BASED_POST_VALIDATION'
WHERE id IN (
    '593913ac-0294-431b-adf3-5227ff8fddff', -- Equidendetail
    '3e0bfd53-94c7-4a73-8d71-0f2c64313c3f'  -- Equidendetail Zucht
);

UPDATE data_product
SET flow_code = 'UNBOUND_BUR_BASED_POST_VALIDATION'
WHERE id IN (
    '6319423c-e4fc-4a47-be6e-43888f58f94f', -- Rinderdetail
    '7e4b1b3e-bcfb-4d94-923d-1277828de70b', -- Schafdetail
    '5da9e6e0-4c17-4683-af89-b49206472ae7', -- Ziegendetail
    '0b42afb7-3683-4065-9cb3-396995a5be97', -- Rinderdetail Zucht
    '2f28d2ec-8797-46fd-8149-9c70ac5f2ebe', -- Rinderbewegungsdaten Zucht
    '88dcf0f9-8502-4596-b035-45c5d548d262', -- Ziegenbewegungsdaten Zucht
    '54decb00-cecd-4c64-b368-8ab999130ac4', -- Ziegendetail Zucht
    '720aa209-7aa8-4faf-8b1a-013a041084f2', -- Schafbewegungsdaten Zucht
    'b0a4ff29-cac4-4413-b8fb-7ff61f4ff2ac'  -- Schafdetail Zucht
);
