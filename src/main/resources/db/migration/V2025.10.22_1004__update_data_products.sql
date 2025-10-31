UPDATE data_product
SET name = jsonb_build_object(
'de','Person',
'fr','Personne',
'it','Persona'),
    description = jsonb_build_object(
'de','Dieses Datenprodukt enthält Angaben zur Person (Tierhalter und Bewirtschafter), wie: Name, Vorname, kantonale Personennummer, UID, Rechtsform, Postadresse, Telefon, Email und die Identifikationsnummer des dazugehörigen Betriebs.',
'fr','Ce produit contient des informations personnelles sur les éleveurs et les exploitants, telles que : nom, prénom, numéro personnel cantonal, IDE, forme juridique, adresse postale, téléphone et e-mail, ainsi que l’exploitation appartenant à la personne',
'it','Questo prodotto contiene informazioni concernenti la persona (detentore di animali e gestore)e: cognome, nome, numero personale cantonale, IDI, forma giuridica, indirizzo postale, telefono, e-mail e numero di identificazione dell''azienda corrispondente.')
WHERE id = 'c661ea48-106d-4d7a-a5d1-a9a6db48dd8c';

UPDATE data_product
SET name = jsonb_build_object(
'de','Betrieb',
'fr','Exploitation',
'it','Azienda'),
    description = jsonb_build_object(
'de','Dieses Datenprodukt enthält Angaben zu Betrieben (von Tierhaltern und Bewirtschaftern), wie: kantonale Betriebsnummer, BUR Nummer, TVD-Nummer, Standortadresse, Koordinaten, Betriebsform, Personenbeziehung des Betriebs, Betriebseigenschaften',
'fr','Ce produit contient des informations générales sur les exploitations des éleveurs et des exploitants, telles que : numéro d’identification cantonal de la forme d’exploitation, numéro REE, numéro BDTA, adresse du site, coordonnées, forme d’exploitation, relation de personnes de l’exploitation, caractérstiques de l’exploitation',
'it','Questo prodotto contiene informazioni concernenti le aziende (detentori di animali e gestori): numero aziendale cantonale, numero RIS, numero BDTA, ubicazione, coordinate, forma di azienda, rapporto tra la persona e l’azienda, farmCharacteristics')
WHERE id = '147e8c40-78cc-4db3-a909-65504aa62a64';

UPDATE data_product
SET name = jsonb_build_object(
'de','Nutztiere auf Betrieb',
'fr','Animaux de rente dans l’exploitation',
'it','Animali da reddito nell’azienda'),
    description = jsonb_build_object(
'de','Dieses Datenprodukt enthält die Art und Anzahl Nutztiere, die auf einem Betrieb im Vorjahr gehalten wurden. In dem Datenprodukt werden auch Angaben zum Betrieb und zur Person bereitgestellt.',
'fr','Ce produit contient l’espèce et le nombre d’animaux de rente qui sont détenus dans l’exploitation. Ce produit contient aussi des indications sur l’exploitation et la personne.',
'it','Questo prodotto contiene informazioni concernenti gli animali da reddito (specie e numero) detenuti nell’azienda. Nel prodotto di dati sono fornite anche informazioni sull’azienda e sulla persona.')
WHERE id = '085e4b72-964d-4bd5-a3c9-224d8c5585af';

UPDATE data_product
SET name = jsonb_build_object(
'de','Nutztiere in Sömmerung',
'fr','Animaux de rente en estivage',
'it','Animali da reddito estivati'),
    description = jsonb_build_object(
'de','Dieses Datenprodukt enthält die Art und Anzahl Nutztiere pro Betrieb, die im Inland gesömmert wurden. In dem Datenprodukt werden auch Angaben zum Betrieb und zur Person bereitgestellt.',
'fr','Ce produit contient l’espèce et le nombre d’animaux de rente qui ont été estivés dans le pays. Ce produit contient aussi des indications sur l’exploitation et la personne.',
'it','Questo prodotto contiene informazioni concernenti gli animali da reddito (specie e numero) per azienda estivati all’interno del Paese. Nel prodotto di dati sono fornite anche informazioni sull’azienda e sulla persona.')
WHERE id = 'a795d0b0-f177-4bb4-8e41-1ed12d358c79';

UPDATE data_product
SET name = jsonb_build_object(
'de','Flächen Hauptkulturen',
'fr','Surfaces de cultures principales',
'it','Superfici delle colture principali'),
    description = jsonb_build_object(
'de','Dieses Datenprodukt enthält die Fläche der im entsprechenden Jahr angebauten Hauptkulturen eines Betriebs inklusive Angabe zur landwirtschaftlichen Zone, Standortgemeinde und der Produktionsform (z.B. Einsatz von Pflanzenschutzmitteln, Bio). In dem Datenprodukt werden auch Angaben zum Betrieb und zur Person bereitgestellt.',
'fr','Ce produit contient la surface des cultures principales de l’année correspondante dans l’exploitation, y compris l’indication de la zone agricole, de la commune d’implantation et de la forme d’exploitation (utilisation de produits phytosanitaires, Bio). Ce produit contient aussi des indications sur l’exploitation et la personne.',
'it','Questo prodotto contiene informazioni concernenti le superfici sulle quali nell’anno in questione sono state coltivate le colture principali di un’azienda, incluse le informazioni concernenti la zona agricola, il Comune di ubicazione e la forma di produzione (p.es. utilizzo di prodotti fitosanitari, bio). Nel prodotto di dati sono fornite anche informazioni sull’azienda e sulla persona.')
WHERE id = '0a808700-d89e-4fa0-a2b8-8edb15f3addd';

UPDATE data_product
SET name = jsonb_build_object(
'de','Flächen Hanglagen',
'fr','Surfaces en pente',
'it','Superfici in zone declive'),
    description = jsonb_build_object(
'de','Dieses Datenprodukt enthält Flächen, die in einem Hang liegen mit einer Neigung grösser als 18%. Die Angaben sind aufgegliedert nach der Zone, Hauptkultur und Betrieb. In dem Datenprodukt werden auch Angaben zum Betrieb und zur Person bereitgestellt.',
'fr','Ce produit contient des surfaces qui se situent dans une pente dont la déclivité est supérieure à 18 %. Les données sont séparées selon la zone, la culture principale et l’exploitation. Ce produit contient aussi des indications sur l’exploitation et la personne.',
'it','Questo prodotto contiene informazioni concernenti le superfici in zone con una declività superiore al 18%. I dati sono suddivisi per zona, coltura principale e azienda. Nel prodotto di dati sono fornite anche informazioni sull’azienda e sulla persona.')
WHERE id = 'ef4f42dd-eaa9-4af1-988c-86b47bd963fe';

UPDATE data_product
SET name = jsonb_build_object(
'de','Rebflächen Hanglagen',
'fr','Surfaces viticoles en pente',
'it','Vigneti in zone declive'),
    description = jsonb_build_object(
'de','Dieses Datenprodukt enthält Rebflächen, die in einem Hang liegen mit einer Neigung grösser als 30%. Die Angaben sind aufgegliedert nach der Zone, Hauptkultur und Betrieb. In dem Datenprodukt werden auch Angaben zum Betrieb und zur Person bereitgestellt.',
'fr','Ce produit contient des surfaces viticoles qui se situent dans une pente dont la déclivité est supérieure à 30 %. Les données sont séparées selon la zone, la culture principale et l’exploitation. Ce produit contient aussi des indications sur l’exploitation et la personne.',
'it','Questo prodotto contiene informazioni concernenti i vigneti con una declività superiore al 30%. I dati sono suddivisi per zona, coltura principale e azienda. Nel prodotto di dati sono fornite anche informazioni sull’azienda e sulla persona.')
WHERE id = '2375219c-5fe3-458f-bd07-d3c2c87e2539';

UPDATE data_product
SET name = jsonb_build_object(
'de','Biodiversitätsförderflächen',
'fr','Surfaces de promotion de la biodiversité',
'it','Superfici per la promozione della biodiversità'),
    description = jsonb_build_object(
'de','Dieses Datenprodukt enthält die Biodiversitätsförderflächen gemäss Direktzahlungsverordnung (DZV). Die Angaben sind aufgegliedert nach der Zone, Hauptkultur und Betrieb. In dem Datenprodukt werden auch Angaben zum Betrieb und zur Person bereitgestellt.',
'fr','Ce produit contient les données sur les surfaces de promotion de la biodiversité selon l’ordonnance sur les paiements directs (OPD). Les données sont séparées selon la zone, la culture principale et l’exploitation. Ce produit contient aussi des indications sur l’exploitation et la personne.',
'it','Questo prodotto contiene informazioni concernenti le superfici per la promozione della biodiversità secondo l’ordinanza sui pagamenti diretti (OPD). I dati sono suddivisi per zona, coltura principale e azienda. Nel prodotto di dati sono fornite anche informazioni sull’azienda e sulla persona.')
WHERE id = '64e39df0-2e56-4204-9c44-a43e1e26a2e8';

UPDATE data_product
SET name = jsonb_build_object(
'de','Vernetzungsflächen',
'fr','Surfaces de mise en réseau',
'it','Superfici di interconnessione'),
    description = jsonb_build_object(
'de','Dieses Datenprodukt enthält die Vernetzungsflächen gemäss Direktzahlungsverordnung (DZV). Die Angaben sind aufgegliedert nach der Zone, Hauptkultur und Betrieb. In dem Datenprodukt werden auch Angaben zum Betrieb und zur Person bereitgestellt.',
'fr','Ce produit contient les données sur les surfaces de mise en réseau selon l’ordonnance sur les paiements directs (OPD). Les données sont séparées selon la zone, la culture principale et l’exploitation. Ce produit contient aussi des indications sur l’exploitation et la personne.',
'it','Questo prodotto contiene informazioni concernenti le superfici di interconnessione secondo l’ordinanza sui pagamenti diretti (OPD). I dati sono suddivisi per zona, coltura principale e azienda. Nel prodotto di dati sono fornite anche informazioni sull’azienda e sulla persona.')
WHERE id = '1dad9f91-30d8-45c9-8c82-ad72f4cb22e7';

UPDATE data_product
SET name = jsonb_build_object(
'de','Anmeldungsdaten für die Direktzahlungsarten, den ÖLN und die Kontrollstellen',
'fr','Données d’inscription pour les types de paiements directs, les PER et les organes de contrôle',
'it','Dati di notifica per i tipi di pagamenti diretti, la PER e gli organi di controllo'),
    description = jsonb_build_object(
'de','Dieses Datenprodukt enthält die Anmeldedaten für die Direktzahlungsprogramme, für den Ökologischen Leistungsnachweis (ÖLN) sowie die Kontrollstelle. In dem Datenprodukt werden auch Angaben zum Betrieb und zur Person bereitgestellt.',
'fr','Ce produit contient les données d’inscription pour les programmes des paiements directs, les prestations écologiques requises (PER) et les organes de contrôle. Ce produit contient aussi des indications sur l’exploitation et la personne.',
'it','Questo prodotto contiene i dati di notifica per i programmi dei pagamenti diretti, per la prova che le esigenze ecologiche sono rispettate (PER) e per gli organi di controllo. Nel prodotto di dati sono fornite anche informazioni sull’azienda e sulla persona.')
WHERE id = '46f8a883-da7c-49b3-b986-10a24b1e09ef';
