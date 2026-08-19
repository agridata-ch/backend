-- populate legal basis for existing data source systems
UPDATE data_source_system
SET legal_basis = CASE
                      WHEN code IN ('ACONTROL', 'ACONTROL-BLV')
                          THEN '{"de": "Die Bereitstellung von Kontrolldaten erfolgt gestützt auf Art. 165c ff. LwG (SR 910.1), Art. 27 Abs. 5, 7 und 9 ISLV (SR 919.117.71) sowie die einschlägigen Bestimmungen der ISLK-V (SR 916.408). Für die Einhaltung des Datenschutzrechts gelten für Organe des Bundes Art. 36 DSG (SR 235.1) und für kantonale Stellen die jeweiligen kantonalen Datenschutzbestimmungen.", "fr": "La mise à disposition des données de contrôle repose sur les art. 165c ss LAgr (RS 910.1), sur l’art. 27, al. 5, 7 et 9, OSIAgr (RS 919.117.71) et sur les dispositions pertinentes de l’O-SICAL (RS 916.408). S’agissant de la protection des données, les organes de la Confédération sont soumis à l’art. 36 LPD (RS 235.1), tandis que les services cantonaux doivent respecter les dispositions cantonales applicables en la matière."}'::jsonb
                      WHEN code = 'AGIS'
                          THEN '{"de": "Die Bereitstellung der Daten aus den kantonalen Betriebs- und Strukturdatensystemen (AGIS) erfolgt gestützt auf Art. 165c ff. LwG (SR 910.1) und Art. 27 Abs. 9 ISLV (SR 919.117.71) sowie auf die jeweils anwendbare kantonale Landwirtschafts- und Geoinformationsgesetzgebung. Für die Einhaltung des Datenschutzrechts gelten für Organe des Bundes Art. 36 DSG (SR 235.1) und für kantonale Stellen die jeweiligen kantonalen Datenschutzbestimmungen.", "fr": "La mise à disposition des données issues des systèmes cantonaux de données structurelles et d’exploitation (SIPA) repose sur les art. 165c ss LAgr (RS 910.1), sur l’art. 27, al. 9, OSIAgr (RS 919.117.71) et sur la législation cantonale applicable sur l’agriculture et sur la géoinformation. S’agissant de la protection des données, les organes de la Confédération sont soumis à l’art. 36 LPD (RS 235.1), tandis que les services cantonaux doivent respecter les dispositions cantonales applicables en la matière."}'::jsonb
                      WHEN code IN ('TVD', 'ZO_API')
                          THEN '{"de": "Die Bereitstellung der Daten aus der Tierverkehrsdatenbank (TVD) erfolgt gestützt auf Art. 165b Abs. 2 LwG sowie Art. 33 ff. der Verordnung über die Identitas AG und die Tierverkehrsdatenbank (IdTVD-V, SR 916.404.1). Die Nutzung der Schnittstelle und der Datenbezug richten sich ergänzend nach den Bestimmungen und technischen Vorgaben der Identitas AG.", "fr": "La mise à disposition des données issues de la banque de données sur le trafic des animaux (BDTA) repose sur l’art. 165b, al. 2, LAgr et sur les art. 33 ss de l’ordonnance relative à Identitas SA et à la banque de données sur le trafic des animaux (OId-BDTA ; RS 916.404.1). L’utilisation de l’interface et l’accès aux données sont également régis par les dispositions et les spécifications techniques d’Identitas SA."}'::jsonb
                  END;

-- backfill legal basis for existing contract revisions
UPDATE contract_revision cr
SET system_legal_basis = dss.legal_basis
FROM data_request dr
         JOIN data_source_system dss ON dss.id = dr.data_source_system_id
WHERE dr.id = cr.data_request_id
  AND cr.system_legal_basis IS NULL;
