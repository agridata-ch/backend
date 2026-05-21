-- =============================================
-- Notification module tables
-- =============================================

-- Preparing ENUM modifications by dropping dependent objects
ALTER TABLE notification_batch
    DROP status_code;

-- ENUMs
DROP TYPE notification_batch_status_code;
CREATE TYPE notification_batch_status_code AS ENUM ('PENDING', 'COMPLETE', 'FAILED', 'PARTIALLY_FAILED');
ALTER TYPE notification_dispatch_status_code RENAME VALUE 'SENT' TO 'SUBMITTED';

-- Re-adding modified ENUM column
ALTER TABLE notification_batch
    ADD status_code notification_batch_status_code NOT NULL DEFAULT 'PENDING';

-- ===============================================
-- notification_template
-- ===============================================
INSERT INTO public.notification_template (created_at, modified_at, event_type_code, template_version, email_subject,
                                          email_text, webapp_text, mobile_text, required_generic_placeholders)
VALUES (NOW(), NOW(), 'DATA_REQUEST_READY_FOR_REVIEW', 1, '{
  "de": "Neuer Antrag zur Kontrolle eingereicht",
  "fr": "Nouvelle demande à contrôler",
  "it": "Nuova richiesta da verificare"
}', '{
  "de": "Ein neuer Antrag von {{consumer_name}} wurde zur Kontrolle eingereicht. Bitte prüfen Sie diesen im Admin-Portal.",
  "fr": "Une nouvelle demande de {{consumer_name}} a été soumise pour vérification. Veuillez la consulter dans le portail d’administration.",
  "it": "Una nuova richiesta da parte di {{consumer_name}} è stata inviata per la verifica. La preghiamo di controllarla nel portale amministrativo."
}', '{
  "de": "Neuer Antrag zur Kontrolle eingereicht",
  "fr": "Nouvelle demande à contrôler",
  "it": "Nuova richiesta da verificare"
}', null, '["consumer_name"]');
