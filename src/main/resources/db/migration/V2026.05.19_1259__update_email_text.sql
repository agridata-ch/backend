-- ===============================================
-- notification_template
-- ===============================================
UPDATE notification_template
SET email_text = '{
  "de": "<h2 style=\"margin:0 0 12px\">Neuer Antrag von {{dataConsumer}}</h2><p>Ein neuer Antrag wurde zur Kontrolle eingereicht. Bitte prüfen Sie diesen im Admin-Portal.</p><p><strong>Antragsname:</strong> {{dataRequestTitleDe}}</p><p><a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Antrag öffnen</a></p>",
  "fr": "<h2 style=\"margin:0 0 12px\">Nouvelle demande de {{dataConsumer}}</h2><p>Une nouvelle demande a été soumise pour vérification. Veuillez la consulter dans le portail d’administration.</p><p><strong>Nom de la demande:</strong> {{dataRequestTitleFr}}</p><p><a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Ouvrir la demande</a></p>",
  "it": "<h2 style=\"margin:0 0 12px\">Nuova richiesta da parte di {{dataConsumer}}</h2><p>Una nuova richiesta è stata inviata per la verifica. La preghiamo di controllarla nel portale amministrativo.</p><p><strong>Nome della richiesta:</strong> {{dataRequestTitleIt}}</p><p><a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Aprire la richiesta</a></p>"
}'
WHERE event_type_code = 'DATA_REQUEST_READY_FOR_REVIEW';
