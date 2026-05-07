-- =============================================
-- notification_template: add webapp_title and rework DATA_REQUEST_READY_FOR_REVIEW content
-- =============================================

ALTER TABLE notification_template
    ADD COLUMN webapp_title JSONB NULL;

ALTER TABLE notification_template
    RENAME COLUMN required_generic_placeholders TO required_placeholders;

ALTER TABLE notification_batch
    RENAME COLUMN generic_placeholders TO placeholders;

UPDATE notification_template
SET id = '42b33361-5b85-46b3-b048-052e26f1ad43',
webapp_title                  = '{
  "de": "Neuer Antrag zur Kontrolle",
  "fr": "Nouvelle demande à vérifier",
  "it": "Nuova richiesta da controllare"
}',
    webapp_text                   = '{
      "de": "{{data_request_title_de}}",
      "fr": "{{data_request_title_fr}}",
      "it": "{{data_request_title_it}}"
    }',
    required_placeholders = '["data_request_title_de", "data_request_title_fr", "data_request_title_it"]',
    modified_at                   = NOW()
WHERE event_type_code = 'DATA_REQUEST_READY_FOR_REVIEW';
