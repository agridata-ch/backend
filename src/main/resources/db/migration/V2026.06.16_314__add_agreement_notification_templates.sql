-- ===============================================
-- notification_template: add the 3 new Notification-EventTypes
-- - DATA_REQUEST_TO_BE_SIGNED_BY_PROVIDER
-- - DATA_REQUEST_READY_FOR_PROVIDER_SIGNING
-- - DATA_REQUEST_ACTIVATED
-- Edit email text of former type DATA_REQUEST_READY_FOR_REVIEW
-- ===============================================

UPDATE public.notification_template
SET email_subject = '{
      "de": "agridata.ch: Neuer Antrag zur Kontrolle eingereicht",
      "fr": "agridata.ch: Nouvelle demande à contrôler",
      "it": "agridata.ch: Nuova richiesta da verificare"
    }', email_text = '{
        "de": "<p>Guten Tag</p><p>Ein neuer Antrag wurde zur Kontrolle eingereicht. Bitte prüfen Sie diesen im Admin-Portal.</p><p><strong>Zusammenfassung:</strong></p><ul><li><strong>{{dataRequestTitleDe}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Bezüger: <strong>{{dataConsumer}}</strong></li></ul><p>Hier gelangen Sie direkt zum Antrag: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Antrag öffnen</a></p><p>Freundliche Grüsse<br>Ihr agridata.ch-Team</p>",
        "fr": "<p>Bonjour</p><p>Une nouvelle demande a été soumise pour vérification. Veuillez la consulter dans le portail d’administration.</p><p><strong>Résumé:</strong></p><ul><li><strong>{{dataRequestTitleFr}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Demandeur: <strong>{{dataConsumer}}</strong></li></ul><p>Accédez directement à la demande ici: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Ouvrir la demande</a></p><p>Cordialement,<br>l''équipe de agridata.ch</p>",
        "it": "<p>Buongiorno</p><p>Una nuova richiesta è stata inviata per la verifica. La preghiamo di controllarla nel portale amministrativo.</p><p><strong>Riepilogo:</strong></p><ul><li><strong>{{dataRequestTitleIt}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Richiedente: <strong>{{dataConsumer}}</strong></li></ul><p>Può visualizzare la richiesta qui: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Apri la richiesta</a></p><p>Cordiali saluti,<br>il team di agridata.ch</p>"
    }'
WHERE event_type_code = 'DATA_REQUEST_READY_FOR_REVIEW';

INSERT INTO public.notification_template (id, created_at, modified_at, event_type_code, template_version,
                                          email_subject, email_text, webapp_title, webapp_text, mobile_text)
VALUES ('b0137bea-1e05-431b-9020-dea6eeef7687', NOW(), NOW(), 'DATA_REQUEST_READY_FOR_PROVIDER_SIGNING', 1, '{
      "de": "agridata.ch: Neuer Datenantrag zum Unterzeichnen",
      "fr": "agridata.ch: Nouvelle demande de données à signer",
      "it": "agridata.ch: Nuova richiesta dati da firmare"
    }', '{
      "de": "<p>Guten Tag</p><p>Der Datenbezüger {{dataConsumer}} möchte gerne Daten von Ihnen beziehen und hat hierzu einen Antrag auf agridata.ch gestellt.</p><p>Damit der Datenantrag aktiviert werden kann, wird noch Ihre Zustimmung benötigt. Der vom Bezüger bereits unterzeichnete Vertrag liegt zur Prüfung und Gegenzeichnung für Sie bereit.</p><p><strong>Zusammenfassung:</strong></p><ul><li><strong>{{dataRequestTitleDe}} (ID: {{dataRequestHumanFriendlyId}})</li><li>Datenbezüger: <strong>{{dataConsumer}}</strong></li></ul><p>Sie können den Antrag hier einsehen und direkt online unterzeichnen: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Antrag öffnen</a></p><p>Bei Fragen zum Inhalt wenden Sie sich bitte direkt an {{dataConsumer}}. Bei technischen Fragen steht Ihnen unser Support gerne zur Verfügung.</p><p>Freundliche Grüsse<br>Ihr agridata.ch-Team</p>",
      "fr": "<p>Bonjour</p><p>Le demandeur de données {{dataConsumer}} souhaite obtenir des données de votre part et a déposé une demande à cet effet sur agridata.ch.</p><p>Pour que la demande de données puisse être activée, votre accord est encore nécessaire. Le contrat, déjà signé par le demandeur, est à votre disposition pour examen et contre-seing.</p><p><strong>Résumé:</strong></p><ul><li><strong>{{dataRequestTitleFr}} (ID: {{dataRequestHumanFriendlyId}})</li><li>Demandeur: <strong>{{dataConsumer}}</strong></li></ul><p>Vous pouvez consulter la demande ici et la signer directement en ligne: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Ouvrir la demande</a></p><p>Pour toute question sur le contenu, veuillez contacter directement {{dataConsumer}}. En cas de questions techniques, notre support se tient volontiers à votre disposition.</p><p>Cordialement,<br>l''équipe de agridata.ch</p>",
      "it": "<p>Buongiorno</p><p>Il richiedente dei dati {{dataConsumer}} desidera ricevere dati da lei e ha presentato una richiesta in tal senso su agridata.ch.</p><p>Affinché la richiesta dati possa essere attivata, è ancora necessario il suo consenso. Il contratto, già firmato dal richiedente, è pronto per la sua revisione e controfirma.</p><p><strong>Riepilogo:</strong></p><ul><li><strong>{{dataRequestTitleIt}} (ID: {{dataRequestHumanFriendlyId}})</li><li>Richiedente: <strong>{{dataConsumer}}</strong></li></ul><p>Può visualizzare la richiesta qui e firmarla direttamente online: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Apri la richiesta</a></p><p>Per domande sul contenuto, la preghiamo di contattare direttamente {{dataConsumer}}. In caso di domande tecniche, il nostro supporto è volentieri a sua disposizione.</p><p>Cordiali saluti,<br>il team di agridata.ch</p>"
    }', '{
      "de": "Neuer Antrag zum Unterzeichnen",
      "fr": "Nouvelle demande à signer",
      "it": "Nuova richiesta da firmare"
    }', '{
      "de": "{{dataRequestTitleDe}}",
      "fr": "{{dataRequestTitleFr}}",
      "it": "{{dataRequestTitleIt}}"
    }',
    NULL
),
('149a3b47-6536-4240-9856-e5305ccd1015', NOW(), NOW(), 'DATA_REQUEST_READY_FOR_ACTIVATION', 1, '{
    "de": "agridata.ch: Neuer Datenantrag zur Aktivierung bereit",
    "fr": "agridata.ch: Nouvelle demande de données prête à être activée",
    "it": "agridata.ch: Nuova richiesta dati pronta per essere attivata"
    }', '{
    "de": "<p>Guten Tag</p><p>Der Datenanbieter hat den Vertrag für den unten genannten Antrag unterzeichnet und eingereicht.</p><p><strong>Zusammenfassung:</strong></p><ul><li><strong>{{dataRequestTitleDe}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Bezüger: <strong>{{dataConsumer}}</strong></li></ul><p>Sobald der Antrag von der Administration final geprüft und aktiviert wurde, werden Sie benachrichtigt.</p><p>Hier gelangen Sie direkt zum Antrag: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Antrag öffnen</a></p><p>Freundliche Grüsse<br>Ihr agridata.ch-Team</p>",
    "fr": "<p>Bonjour</p><p>Le fournisseur de données a signé et soumis le contrat pour la demande mentionnée ci-dessous.</p><p><strong>Résumé:</strong></p><ul><li><strong>{{dataRequestTitleFr}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Demandeur: <strong>{{dataConsumer}}</strong></li></ul><p>Vous serez informé dès que la demande aura été définitivement vérifiée et activée par l''administration.</p><p>Accédez directement à la demande ici: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Ouvrir la demande</a></p><p>Cordialement,<br>l''équipe de agridata.ch</p>",
    "it": "<p>Buongiorno</p><p>Il fornitore di dati ha firmato e inviato il contratto per la richiesta sotto indicata.</p><p><strong>Riepilogo:</strong></p><ul><li><strong>{{dataRequestTitleIt}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Richiedente: <strong>{{dataConsumer}}</strong></li></ul><p>Riceverà una notifica non appena la richiesta sarà stata definitivamente verificata e attivata dall''amministrazione.</p><p>Può visualizzare la richiesta qui: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Apri la richiesta</a></p><p>Cordiali saluti,<br>il team di agridata.ch</p>"
    }', '{
    "de": "Neuer Antrag zur Aktivierung bereit",
    "fr": "Nouvelle demande prête à être activée",
    "it": "Nuova richiesta pronta per essere attivata"
    }', '{
    "de": "{{dataRequestTitleDe}}",
    "fr": "{{dataRequestTitleFr}}",
    "it": "{{dataRequestTitleIt}}"
    }',
    NULL
),
('d9c8e5b7-1c3a-4c8e-9b0c-2f1e5a6b8f9d', NOW(), NOW(), 'DATA_REQUEST_ACTIVATED', 1, '{
    "de": "agridata.ch: Datenantrag erfolgreich aktiviert",
    "fr": "agridata.ch: Demande de données activée avec succès",
    "it": "agridata.ch: Richiesta dati attivata con successo"
    }', '{
    "de": "<p>Guten Tag</p><p>Der Prozess für den unten genannten Antrag wurde erfolgreich abgeschlossen. Der Antrag ist nun im System aktiv.</p><p><strong>Zusammenfassung:</strong></p><ul><li><strong>{{dataRequestTitleDe}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Bezüger: <strong>{{dataConsumer}}</strong></li><li>Status: <strong>Aktiv</strong></li></ul><p>Sie können den aktuellen Stand und alle Details jederzeit in der Webapplikation einsehen: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Antrag öffnen</a></p><p>Freundliche Grüsse<br>Ihr agridata.ch-Team</p>",
    "fr": "<p>Bonjour</p><p>Le processus pour la demande mentionnée ci-dessous a été mené à bien. La demande est désormais active dans le système.</p><p><strong>Résumé:</strong></p><ul><li><strong>{{dataRequestTitleFr}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Demandeur: <strong>{{dataConsumer}}</strong></li><li>Statut: <strong>Actif</strong></li></ul><p>Vous pouvez consulter l''état actuel et tous les détails à tout moment dans l''application web: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Ouvrir la demande</a></p><p>Cordialement,<br>l''équipe de agridata.ch</p>",
    "it": "<p>Buongiorno</p><p>Il processo per la richiesta sotto indicata è stato completato con successo. La richiesta è ora attiva nel sistema.</p><p><strong>Riepilogo:</strong></p><ul><li><strong>{{dataRequestTitleIt}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Richiedente: <strong>{{dataConsumer}}</strong></li><li>Stato: <strong>Attivo</strong></li></ul><p>Può consultare lo stato attuale e tutti i dettagli in qualsiasi momento nell''applicazione web: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Apri la richiesta</a></p><p>Cordiali saluti,<br>il team di agridata.ch</p>"
    }', '{
    "de": "Antrag aktiviert",
    "fr": "Demande activée",
    "it": "Richiesta attivata"
    }', '{
    "de": "{{dataRequestTitleDe}}",
    "fr": "{{dataRequestTitleFr}}",
    "it": "{{dataRequestTitleIt}}"
    }',
    NULL
);
