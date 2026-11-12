insert into users (id, archived, created_at, modified_at, given_name)
values ('d10bca30-89ba-4e28-b03e-dee5e30ccc50', false, NOW(), NOW(), 'SYSTEM:DATA_REQUEST_TERMINATION_JOB');

UPDATE public.notification_template
SET email_text    = '{
      "de": "<p>Guten Tag</p><p>Ihr Datenantrag wurde von agridata.ch geprüft und ist jetzt freigegeben.</p><p>Als nächsten Schritt können Sie den zugehörigen Vertrag online in agridata.ch unterzeichnen.</p><p><strong>Zusammenfassung:</strong></p><ul><li><strong>{{dataRequestTitleDe}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Status: <strong>Zur Unterzeichnung genehmigt</strong></li></ul><p>Hier gelangen Sie direkt zum Antrag: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Antrag öffnen</a></p><p>Freundliche Grüsse<br>Ihr agridata.ch-Team</p>",
      "fr": "<p>Bonjour</p><p>Votre demande de données a été vérifiée par agridata.ch et est maintenant validée.</p><p>Comme prochaine étape, vous pouvez signer le contrat correspondant en ligne sur agridata.ch.</p><p><strong>Résumé:</strong></p><ul><li><strong>{{dataRequestTitleFr}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Statut: <strong>Approuvé pour signature</strong></li></ul><p>Accédez directement à votre demande ici: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Ouvrir la demande</a></p><p>Cordialement,<br>L''équipe de agridata.ch</p>",
      "it": "<p>Buongiorno</p><p>La sua richiesta dati è stata verificata da agridata.ch ed è ora approvata.</p><p>Come passo successivo, può firmare il relativo contratto online su agridata.ch.</p><p><strong>Riepilogo:</strong></p><ul><li><strong>{{dataRequestTitleIt}}</strong> (ID: {{dataRequestHumanFriendlyId}})</li><li>Stato: <strong>Approvato per la firma</strong></li></ul><p>Acceda direttamente alla richiesta qui: <a href=\"{{dataRequestUrl}}\" target=\"_blank\" rel=\"noopener noreferrer\">Apri la richiesta</a></p><p>Cordiali saluti,<br>Il team di agridata.ch</p>"
    }',
    modified_at   = NOW()
WHERE event_type_code = 'DATA_REQUEST_APPROVED';
