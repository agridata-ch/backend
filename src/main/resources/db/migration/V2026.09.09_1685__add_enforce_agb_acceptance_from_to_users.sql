ALTER TABLE users
    ADD COLUMN enforce_agb_acceptance_from TIMESTAMP WITHOUT TIME ZONE;

UPDATE users
SET enforce_agb_acceptance_from = CASE WHEN last_accepted_agb_date IS NOT NULL THEN NULL ELSE NOW() END
WHERE jsonb_exists(roles_at_last_login, 'agridata.ch.Agridata_Datenbezueger')
   OR jsonb_exists(roles_at_last_login, 'agridata.ch.Agridata_Datenanbieter');
