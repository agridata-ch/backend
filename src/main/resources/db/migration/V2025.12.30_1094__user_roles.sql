ALTER TABLE users
    ADD roles_at_last_login JSONB DEFAULT '[]'::jsonb;

UPDATE users
SET roles_at_last_login = jsonb_build_array(
    'AGRIDATA_MIGRATED_AT_' || to_char(now(), 'YYYY-MM-DD"T"HH24:MI:SS'));

UPDATE users
SET roles_at_last_login = jsonb_build_array(
    'AGRIDATA_MIGRATED_AT_' || to_char(now(), 'YYYY-MM-DD"T"HH24:MI:SS'),
    'agridata.ch.Agridata_Einwilliger')
WHERE kt_id_p IS NOT NULL;
