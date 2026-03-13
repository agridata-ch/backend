ALTER TABLE data_product
    ADD deprecated_since TIMESTAMP WITHOUT TIME ZONE;

UPDATE data_product
SET deprecated_since = '2026-03-06 00:00:00.000000'
WHERE id = '147e8c40-78cc-4db3-a909-65504aa62a64';

INSERT INTO public.data_product (id, archived, created_by, created_at, modified_by, modified_at, name, description,
                                 rest_client_identifier_code, rest_client_method_code, rest_client_path,
                                 rest_client_request_template, flow_code, data_source_system_id, deprecated_since)
VALUES ('7911d98d-59eb-4cf4-be61-bfe77fe9117e', false, null, NOW(), null,
        NOW(), '{
    "de": "Betrieb",
    "fr": "Exploitation",
    "it": "Azienda"
  }',
        '{
          "de": "Dieses Datenprodukt enthält Angaben zu Betrieben (von Tierhaltern und Bewirtschaftern), wie: kantonale Betriebsnummer, BUR Nummer, TVD-Nummer, Standortadresse, Koordinaten, Betriebsform, Personenbeziehung des Betriebs, Betriebseigenschaften",
          "fr": "Ce produit contient des informations générales sur les exploitations des éleveurs et des exploitants, telles que : numéro d’identification cantonal de la forme d’exploitation, numéro REE, numéro BDTA, adresse du site, coordonnées, forme d’exploitation, relation de personnes de l’exploitation, caractérstiques de l’exploitation",
          "it": "Questo prodotto contiene informazioni concernenti le aziende (detentori di animali e gestori): numero aziendale cantonale, numero RIS, numero BDTA, ubicazione, coordinate, forma di azienda, rapporto tra la persona e l’azienda, farmCharacteristics"
        }',
        'AGIS_API', 'POST', 'register-data/1/register',
        '{"farmSearchParameters":{"uid":"{{uid}}"}}',
        'UID_BASED_PRE_VALIDATION', '5335d715-e95c-4777-a424-ab73f2ff5618', null);