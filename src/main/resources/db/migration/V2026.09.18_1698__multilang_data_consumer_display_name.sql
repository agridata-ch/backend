-- Convert data_request.data_consumer_display_name from a single string to a multilingual JSONB value.
-- Existing values are copied into all three languages so nothing renders blank until translations are edited.
ALTER TABLE data_request
    ALTER COLUMN data_consumer_display_name TYPE JSONB
        USING CASE
                  WHEN data_consumer_display_name IS NULL THEN NULL
                  ELSE jsonb_build_object(
                          'de', data_consumer_display_name,
                          'fr', data_consumer_display_name,
                          'it', data_consumer_display_name)
        END;

-- Notification templates embed a single {{dataConsumer}} placeholder inside each language block. Now that the display
-- name is multilingual, each language block must reference its matching per-language placeholder.
UPDATE notification_template
SET email_text = jsonb_strip_nulls(jsonb_build_object(
        'de', replace(email_text ->> 'de', '{{dataConsumer}}', '{{dataConsumerDe}}'),
        'fr', replace(email_text ->> 'fr', '{{dataConsumer}}', '{{dataConsumerFr}}'),
        'it', replace(email_text ->> 'it', '{{dataConsumer}}', '{{dataConsumerIt}}')))
WHERE email_text IS NOT NULL;
