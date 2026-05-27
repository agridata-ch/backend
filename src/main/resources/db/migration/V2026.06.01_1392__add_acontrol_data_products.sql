-- 1) add Acontrol to data_source_system
INSERT INTO public.data_source_system (id, archived, created_by, created_at, modified_by, modified_at,
                                       data_provider_id, code, name)
VALUES ('810af188-8800-48f8-9b12-0276bb4b4e0e', false, null, now(), null,
        now(), '61404b83-078e-4b4f-a6d6-2aa3990f429c', 'ACONTROL', '{
    "de": "Acontrol",
    "fr": "Acontrol",
    "it": "Acontrol"
  }');

-- 2) add data products for Acontrol
INSERT INTO data_product (
    id,
    archived,
    created_at,
    modified_at,
    data_source_system_id,
    name,
    description,
    rest_client_identifier_code,
    rest_client_method_code,
    rest_client_path_template,
    rest_client_request_template,
    flow_code,
    deprecated_since
)
VALUES
    (
        '30229210-155a-4910-861a-b8e359975142'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '01 Lebensmittelsicherheit (Pflanzliche Primärproduktion)',
                'fr', '01 Sécurité alimentaire (productions primaire végétale)',
                'it', '01 Sicurezza alimentare (produzione primaria vegetale)'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 01 Lebensmittelsicherheit (Pflanzliche Primärproduktion) mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 01 Sécurité alimentaire (productions primaire végétale) avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 01 Sicurezza alimentare (produzione primaria vegetale) con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["01.1"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        '96787125-3de0-4b5f-b9c7-8f1e5b3db516'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '01 Lebensmittelsicherheit (tierische Primärproduktion)',
                'fr', '01 Sécurité alimentaire (production primaire animale)',
                'it', '01 Sicurezza alimentare (produzione primaria animale)'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 01 Lebensmittelsicherheit (tierische Primärproduktion) mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 01 Sécurité alimentaire (production primaire animale) avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 01 Sicurezza alimentare (produzione primaria animale) con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["01.2"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        'df72eb69-fa78-4b52-8649-7068940019fe'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '02 Tiergesundheit',
                'fr', '02 Santé animale',
                'it', '02 Salute degli animali'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 02 Tiergesundheit mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 02 Santé animale avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 02 Salute degli animali con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["02"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        '905170b2-1866-41f2-a8dd-57ba67a1f7bc'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '03 Tierschutz',
                'fr', '03 Protection des animaux',
                'it', '03 Protezione degli animali'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 03 Tierschutz mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 03 Protection des animaux avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 03 Protezione degli animali con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["03"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        '3d3cb41c-6d79-4781-9e48-637692d91427'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '05 Allgemeine Beitragsvoraussetzungen - Ganzjahres- und Sömmerungsbetriebe',
                'fr', '05 Conditions générales pour l’obtention des contributions - exploitations à l’année et exploitations d’estivage',
                'it', '05 Condizioni generali per i contributi - aziende gestite tutto l’anno e d’estivazione'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 05 Allgemeine Beitragsvoraussetzungen - Ganzjahres- und Sömmerungsbetriebe mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 05 Conditions générales pour l’obtention des contributions - exploitations à l’année et exploitations d’estivage avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 05 Condizioni generali per i contributi - aziende gestite tutto l’anno e d’estivazione con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["05"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        '75db774f-8c06-4bd5-a7d3-316cc3814206'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '06 Strukturdaten - Ganzjahresbetriebe (DZV und EKBV)',
                'fr', '06 Données sur les structures - exploitations à l’année',
                'it', '06 Dati strutturali - aziende gestite tutto l’anno'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 06 Strukturdaten - Ganzjahresbetriebe (DZV und EKBV) mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 06 Données sur les structures - exploitations à l’année avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 06 Dati strutturali - aziende gestite tutto l’anno con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["06"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        '1ca1904a-4d0d-471a-b411-f4b5f2329c3b'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '07 Ökologischer Leistungsnachweis ÖLN',
                'fr', '07 Prestations écologiques requises',
                'it', '07 Prova che le esigenze ecologiche sono rispettate'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 07 Ökologischer Leistungsnachweis ÖLN mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 07 Prestations écologiques requises avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 07 Prova che le esigenze ecologiche sono rispettate con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["07"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        '1f05fef2-e4a9-42cf-9dd6-c0ee73c9b0e8'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '08 Biodiversitätsförderflächen',
                'fr', '08 Surfaces de promotion de la biodiversité',
                'it', '08 Superfici per la promozione della biodiversità'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 08 Biodiversitätsförderflächen mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 08 Surfaces de promotion de la biodiversité avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 08 Superfici per la promozione della biodiversità con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["08"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        'b993f539-4036-4b43-b61e-c029e79c866b'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '09 Biologische Landwirtschaft',
                'fr', '09 Agriculture biologique',
                'it', '09 Agricoltura biologica'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 09 Biologische Landwirtschaft mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 09 Agriculture biologique avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 09 Agricoltura biologica con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["09"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        'c009a1f9-7c16-4a47-b79a-9c9224ac55d2'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '11 Graslandbasierte Milch- und Fleischproduktion',
                'fr', '11 Production de lait et de viande basée sur les herbages',
                'it', '11 Produzione di latte e carne basata sulla superficie inerbita'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 11 Graslandbasierte Milch- und Fleischproduktion mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 11 Production de lait et de viande basée sur les herbages avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 11 Produzione di latte e carne basata sulla superficie inerbita con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["11"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        '9f3a2e49-7196-41b7-a3da-0a83b3f5e386'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '12 Tierwohl',
                'fr', '12 Bien-être des animaux',
                'it', '12 Benessere degli animali'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 12 Tierwohl mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 12 Bien-être des animaux avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 12 Benessere degli animali con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["12"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        'af225782-bd52-4470-a093-2c387dd47ea0'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '13 Ressourceneffizienzbeiträge',
                'fr', '13 Efficience des ressources',
                'it', '13 Efficienza delle risorse'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 13 Ressourceneffizienzbeiträge mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 13 Efficience des ressources avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 13 Efficienza delle risorse con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["13"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        '54019a06-59ac-4421-8d1f-84d4255f3a94'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '14 Sömmerung',
                'fr', '14 Estivage',
                'it', '14 Estivazione'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 14 Sömmerung mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 14 Estivage avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 14 Estivazione con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["14"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        '8791159f-f3fa-4c4b-8583-d5207081c262'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '15 In-situ',
                'fr', '15 In situ',
                'it', '15 In situ'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 15 In-situ mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 15 In situ avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 15 In situ con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["15"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        '340d517d-a5d4-418f-9396-6225f418afb2'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '16 Verzicht auf Pflanzenschutzmittel',
                'fr', '16 Non-recours aux produits phytosanitaires',
                'it', '16 Rinuncia a prodotti fitosanitari'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 16 Verzicht auf Pflanzenschutzmittel mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 16 Non-recours aux produits phytosanitaires avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 16 Rinuncia a prodotti fitosanitari con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["16"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        '168aec4a-8aa8-4a36-b255-59f33e9147f6'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '18 Bodenfruchtbarkeit',
                'fr', '18 Fertilité du sol',
                'it', '18 Fertilità del suolo'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 18 Bodenfruchtbarkeit mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 18 Fertilité du sol avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 18 Fertilità del suolo con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["18"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    ),
    (
        '91682ce4-33a4-463b-9f85-2e6991100d6f'::uuid,
        false,
        NOW(),
        NOW(),
        '810af188-8800-48f8-9b12-0276bb4b4e0e',
        jsonb_build_object(
                'de', '19 Klimamassnahmen',
                'fr', '19 Mesures en faveurs du climat',
                'it', '19 Misure per il clima'
        ),
        jsonb_build_object(
                'de', 'Dieses Datenprodukt enthält Angaben zu Kontrollen aus dem Kontrollbereich 19 Klimamassnahmen mit den Ergebnissen pro Kontrollpunkt. Maximal die letzten 4 Jahre oder seit Bewirtschafterwechsel.',
                'fr', 'Ce produit de données contient des indications sur les contrôles du domaine de contrôle 19 Mesures en faveurs du climat avec les résultats par point de contrôle. Au maximum les 4 dernières années ou depuis le changement d’exploitant.',
                'it', 'Questo prodotto di dati contiene i controlli relativi all’ambito di controllo 19 Misure per il clima con i risultati per ciascun punto di controllo. Sono contenuti i dati degli ultimi 4 anni al massimo o a partire dal cambio di gestore.'
        ),
        'ACONTROL_API',
        'POST',
        'api/inspections/search',
        '{"unitIdentifiers":[{"unitIdentifier":"{{bur}}","type":"BurId"}],"inspectionDomainIds":["19"],"pageOffset":{{pageOffset}},"pageSize":{{pageSize}}}',
        'BUR_BASED_PRE_VALIDATION',
        NULL
    );
