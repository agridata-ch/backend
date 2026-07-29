-- add legal basis columns
ALTER TABLE data_source_system
    ADD legal_basis JSONB;

ALTER TABLE contract_revision
    ADD system_legal_basis JSONB;

-- populate legal basis for existing data source systems
UPDATE data_source_system
SET legal_basis = CASE
                      WHEN code IN ('ACONTROL', 'ACONTROL-BLV')
                          THEN '{"de": "Die Bekanntgabe von Kontrolldaten erfolgt gestützt auf Art. 165c bis 165g LwG (SR 910.1), Art. 27 Abs. 5, 7 und 9 ISLV (SR 919.117.71) sowie die einschlägigen Bestimmungen der ISLK-V (SR 916.408) über die Bekanntgabe von Daten. Für die Bearbeitung und Bekanntgabe von Personendaten gelten das DSG (SR 235.1) sowie die anwendbaren kantonalen Datenschutzbestimmungen.", "fr": "La publication des données de contrôle se fonde sur les articles 165c à 165g de la loi sur l’agriculture (LAgr) (RS 910.1), aux articles 27, alinéas 5, 7 et 9, de l’OSLA (RS 919.117.71) ainsi qu’aux dispositions pertinentes de l’OSLA-O (RS 916.408) relatives à la communication de données. Le traitement et la communication des données à caractère personnel sont régis par la LPD (RS 235.1) ainsi que par les dispositions cantonales applicables en matière de protection des données."}'::jsonb
                      WHEN code = 'AGIS'
                          THEN '{"de": "Die Bereitstellung der Daten aus den kantonalen Betriebs- und Strukturdatensystemen (AGIS) erfolgt gestützt auf Art. 165c ff. LwG (SR 910.1) und Art. 27 Abs. 9 ISLV (SR 919.117.71) sowie auf die jeweils anwendbare kantonale Landwirtschafts- und Geoinformationsgesetzgebung. Die Einhaltung des Datenschutzrechts richtet sich nach Art. 36 DSG (SR 235.1) beziehungsweise den anwendbaren kantonalen Datenschutzbestimmungen.", "fr": "La mise à disposition des données issues des systèmes cantonaux de données d’exploitation et de structure (SIPA) s’appuie sur les articles 165c et suivants de la loi sur l’agriculture (LAgr, RS 910.1) et sur l’article 27, alinéa 9, de l’ordonnance sur la protection des données dans l’agriculture (OPDA, RS 919.117.71), ainsi que sur la législation cantonale applicable en matière d’agriculture et de géoinformation. Le respect de la législation sur la protection des données est régi par l’art. 36 LPD (RS 235.1) ou par les dispositions cantonales applicables en matière de protection des données."}'::jsonb
                      WHEN code IN ('TVD', 'ZO_API')
                          THEN '{"de": "Die Bereitstellung der Daten aus der Tierverkehrsdatenbank (TVD) erfolgt gestützt auf Art. 165b Abs. 2 LwG sowie Art. 33 ff. der Verordnung über die Identitas AG und die Tierverkehrsdatenbank (IdTVD-V, SR 916.404.1). Die Nutzung der Schnittstelle und der Datenbezug richten sich ergänzend nach den Bestimmungen und technischen Vorgaben der Identitas AG.", "fr": "La mise à disposition des données issues de la banque de données sur le traffic d’animaux (BDTA) s’effectue en vertu de l’art. 165b, al. 2, de la loi sur l’agriculture (LAgr) ainsi que des art. 33 et suivants de l’ordonnance sur Identitas AG et la banque de données sur le traffic d’animaux (IdTVD-V, RS 916.404.1). L’utilisation de l’interface et l’obtention des données sont en outre régies par les dispositions et les spécifications techniques d’Identitas AG."}'::jsonb
                  END
WHERE code IN ('ACONTROL', 'ACONTROL-BLV', 'AGIS', 'TVD', 'ZO_API');

ALTER TABLE data_source_system
    ALTER COLUMN legal_basis SET NOT NULL;

-- backfill legal basis for existing contract revisions
UPDATE contract_revision cr
SET system_legal_basis = dss.legal_basis
FROM data_request dr
         JOIN data_source_system dss ON dss.id = dr.data_source_system_id
WHERE dr.id = cr.data_request_id
  AND cr.system_legal_basis IS NULL;

ALTER TABLE contract_revision
    ALTER COLUMN system_legal_basis SET NOT NULL;
