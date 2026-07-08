-- ===============================================
-- notification_template: add 2 new Notification-EventTypes
-- - DATA_REQUEST_APPROVED
-- - DATA_REQUEST_CHANGE_NEEDED
-- ===============================================

UPDATE public.notification_template
SET email_text = '{
      "de": "<p>Guten Tag</p><p>Der Datenbezüger {{dataConsumer}} möchte gerne Daten von Ihnen beziehen und hat hierzu einen Antrag auf agridata.ch gestellt.</p><p>Damit der Datenantrag aktiviert werden kann, wird noch Ihre Zustimmung benötigt. Der vom Bezüger bereits unterzeichnete Vertrag liegt zur Prüfung und Gegenzeichnung für Sie bereit.</p><p><strong>Zusammenfassung:</strong></p><ul><li><strong>{{dataRequestTitleDe}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Datenbezüger: <strong>{{dataConsumer}}</strong></li></ul><p>Sie können den Antrag hier einsehen und direkt online unterzeichnen: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Antrag öffnen</a></p><p>Bei Fragen zum Inhalt wenden Sie sich bitte direkt an {{dataConsumer}}. Bei technischen Fragen steht Ihnen unser Support gerne zur Verfügung.</p><p>Freundliche Grüsse<br>Ihr agridata.ch-Team</p>",
      "fr": "<p>Bonjour</p><p>Le demandeur de données {{dataConsumer}} souhaite obtenir des données de votre part et a déposé une demande à cet effet sur agridata.ch.</p><p>Pour que la demande de données puisse être activée, votre accord est encore nécessaire. Le contrat, déjà signé par le demandeur, est à votre disposition pour examen et contre-seing.</p><p><strong>Résumé:</strong></p><ul><li><strong>{{dataRequestTitleFr}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Demandeur: <strong>{{dataConsumer}}</strong></li></ul><p>Vous pouvez consulter la demande ici et la signer directement en ligne: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Ouvrir la demande</a></p><p>Pour toute question sur le contenu, veuillez contacter directement {{dataConsumer}}. En cas de questions techniques, notre support se tient volontiers à votre disposition.</p><p>Cordialement,<br>l''équipe de agridata.ch</p>",
      "it": "<p>Buongiorno</p><p>Il richiedente dei dati {{dataConsumer}} desidera ricevere dati da lei e ha presentato una richiesta in tal senso su agridata.ch.</p><p>Affinché la richiesta dati possa essere attivata, è ancora necessario il suo consenso. Il contratto, già firmato dal richiedente, è pronto per la sua revisione e controfirma.</p><p><strong>Riepilogo:</strong></p><ul><li><strong>{{dataRequestTitleIt}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Richiedente: <strong>{{dataConsumer}}</strong></li></ul><p>Può visualizzare la richiesta qui e firmarla direttamente online: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Apri la richiesta</a></p><p>Per domande sul contenuto, la preghiamo di contattare direttamente {{dataConsumer}}. In caso di domande tecniche, il nostro supporto è volentieri a sua disposizione.</p><p>Cordiali saluti,<br>il team di agridata.ch</p>"
    }'
WHERE event_type_code = 'DATA_REQUEST_READY_FOR_REVIEW';

INSERT INTO public.notification_template (id, created_at, modified_at, event_type_code, template_version,
                                          email_subject, email_text, webapp_title, webapp_text, mobile_text)
VALUES ('69748ec7-0876-4e93-96f0-4fb772ce5039', NOW(), NOW(), 'DATA_REQUEST_APPROVED', 1, '{
      "de": "agridata.ch: Ihr Datenantrag wurde freigegeben",
      "fr": "agridata.ch: Votre demande de données a été approuvée",
      "it": "agridata.ch: La sua richiesta dati è stata approvata"
    }', '{
      "de": "<p>Guten Tag</p><p>Ihr Datenantrag wurde von agridata.ch geprüft und ist nun freigegeben.</p><p>Als nächsten Schritt können Sie nun den zugehörigen Vertrag direkt online in agridata.ch unterzeichnen.</p><p><strong>Zusammenfassung:</strong></p><ul><li><strong>{{dataRequestTitleDe}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Status: <strong>Zur Unterzeichnung genehmigt</strong></li></ul><p>Hier gelangen Sie direkt zum Antrag: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Antrag öffnen</a></p><p>Freundliche Grüsse<br>Ihr agridata.ch-Team</p>",
      "fr": "<p>Bonjour</p><p>Votre demande a été vérifiée par agridata.ch et a désormais été validée.</p><p>L’étape suivante consiste désormais à signer le contrat correspondant.</p><p><strong>Résumé:</strong></p><ul><li><strong>{{dataRequestTitleFr}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Statut: <strong>Approuvé pour signature</strong></li></ul><p>Accédez directement à votre demande ici: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Ouvrir la demande</a></p><p>Cordialement,<br>l''équipe de agridata.ch</p>",
      "it": "<p>Buongiorno</p><p>La sua richiesta è stata verificata da agridata.ch ed è ora stata approvata.</p><p>Come fase successiva, può ora procedere alla firma del relativo contratto.</p><p><strong>Riepilogo:</strong></p><ul><li><strong>{{dataRequestTitleIt}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Stato: <strong>Approvato per la firma</strong></li></ul><p>Acceda direttamente alla richiesta qui: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Apri la richiesta</a></p><p>Cordiali saluti,<br>il team di agridata.ch</p>"
    }', '{
      "de": "Datenantrag freigegeben",
      "fr": "Demande approuvée",
      "it": "Richiesta approvata"
    }', '{
      "de": "{{dataRequestTitleDe}}",
      "fr": "{{dataRequestTitleFr}}",
      "it": "{{dataRequestTitleIt}}"
    }',
    NULL
),
('45f5feb0-5119-4694-b98e-233179e7eb7c', NOW(), NOW(), 'DATA_REQUEST_CHANGES_NEEDED', 1, '{
    "de": "agridata.ch: Offene Punkt am Datenantrag",
    "fr": "agridata.ch: Points en suspens concernant la demande de données",
    "it": "agridata.ch: Punti in sospeso relativi alla richiesta dati"
    }', '{
    "de": "<p>Guten Tag</p><p>Bei der Überprüfung Ihres Datenantrags durch agridata.ch haben sich noch offene Punkte ergeben.</p><p>Ein Mitarbeiter von agridata.ch wird sich in Kürze persönlich bei Ihnen melden, um Sie bei der Finalisierung zu unterstützen und die nächsten Schritte gemeinsam zu besprechen.</p><p><strong>Zusammenfassung:</strong></p><ul><li><strong>{{dataRequestTitleDe}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Status: <strong>Rücksprache ausstehend</strong></li></ul><p>Sie können Ihren Antrag hier einsehen: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Antrag öffnen</a></p><p>Freundliche Grüsse<br>Ihr agridata.ch-Team</p>",
    "fr": "<p>Bonjour</p><p>Lors de la vérification de votre demande de données par agridata.ch, des points sont restés en suspens.</p><p>Un collaborateur d’agridata.ch prendra personnellement contact avec vous sous peu afin de vous aider à finaliser la demande et de discuter ensemble des prochaines étapes.</p><p><strong>Résumé:</strong></p><ul><li><strong>{{dataRequestTitleFr}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Status: <strong>Consultation en cours</strong></li></ul><p>Vous pouvez consulter votre demande ici: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Ouvrir la demande</a></p><p>Cordialement,<br>l''équipe de agridata.ch</p>",
    "it": "<p>Buongiorno</p><p>Durante la verifica della sua richiesta dati da parte di agridata.ch, sono emersi alcuni punti da chiarire.</p><p>Un collaboratore di agridata.ch la contatterà personalmente al più presto per supportarla nella finalizzazione e discutere insieme i passi successivi.</p><p><strong>Riepilogo:</strong></p><ul><li><strong>{{dataRequestTitleIt}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Status: <strong>Consultazione in corso</strong></li></ul><p>Può visualizzare la sua richiesta qui: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Apri la richiesta</a></p><p>Cordiali saluti,<br>il team di agridata.ch</p>"
    }', '{
    "de": "Offene Punkte am Datenantrag",
    "fr": "Points en suspens de la demande",
    "it": "Punti in sospeso della richiesta"
    }', '{
    "de": "{{dataRequestTitleDe}}",
    "fr": "{{dataRequestTitleFr}}",
    "it": "{{dataRequestTitleIt}}"
    }',
    NULL
);
