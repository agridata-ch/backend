-- ===============================================
-- notification_template
-- ===============================================
UPDATE public.notification_template
SET email_text = '{
  "de": "Neuer Antrag von {{dataConsumer}}\n\nEin neuer Antrag wurde zur Kontrolle eingereicht. Bitte prüfen Sie diesen im Admin-Portal.\n\nAntragsname: {{dataRequestTitleDe}}\nAntrag öffnen: {{dataRequestUrl}}",
  "fr": "Nouvelle demande de {{dataConsumer}}\n\nUne nouvelle demande a été soumise pour vérification. Veuillez la consulter dans le portail d’administration.\n\nNom de la demande: {{dataRequestTitleFr}}\nOuvrir la demande: {{dataRequestUrl}}",
  "it": "Nuova richiesta da parte di {{dataConsumer}}\n\nUna nuova richiesta è stata inviata per la verifica. La preghiamo di controllarla nel portale amministrativo.\n\nNome della richiesta: {{dataRequestTitleIt}}\nAprire la richiesta: {{dataRequestUrl}}"
}',
    webapp_text = '{
  "de": "{{dataRequestTitleDe}}",
  "fr": "{{dataRequestTitleFr}}",
  "it": "{{dataRequestTitleIt}}"
}'
WHERE event_type_code = 'DATA_REQUEST_READY_FOR_REVIEW';

ALTER TABLE public.notification_template
    DROP COLUMN required_placeholders;
