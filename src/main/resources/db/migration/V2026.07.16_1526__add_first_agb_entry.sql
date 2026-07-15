INSERT INTO agb_revision (
    id,
    archived,
    created_by,
    created_at,
    modified_by,
    modified_at,
    valid_from,
    valid_to,
    enforce_consent_from,
    version,
    agb_text
)
VALUES (
           '73b2f303-2e87-4970-b82d-c5a6e296f1ec',
           false,
           NULL,
           now(),
           NULL,
           now(),
           TIMESTAMP '2026-03-10 00:00:00',
           NULL,
           NULL,
           '1.2',
           jsonb_build_object(
                   'de', $agb_de$<h1>Allgemeine Geschäftsbedingungen für die Nutzung des Datenübertragungsdienstes agridata.ch</h1>
<br><h2>1. Gegenstand</h2>
<br><p>a. Das Bundesamt für Landwirtschaft BLW als Betreiber des Datenübertragungsdienstes agridata.ch erlässt diese Allgemeinen Geschäftsbedingungen (AGB) gestützt auf Art. 28b der Verordnung vom 23. Oktober 2013 über die Informationssysteme im Bereich der Landwirtschaft (ISLV; SR 919.117.71).</p>
<br><p>b. Die aktuelle, rechtsgültige Version dieser AGB findet sich auf der Website des Datenübertragungsdienstes unter agridata.ch.</p>
<br><p>c. Die AGB regeln verbindlich die Nutzungsbedingungen des Datenübertragungsdienstes agridata.ch. Massgeblich ist die zum Zeitpunkt der Nutzung aktuelle Version.</p>
<br><p>d. Betreiber des Datenübertragungsdienstes agridata.ch ist das Bundesamt für Landwirtschaft BLW.</p>
<br><p>e. Als Benutzerinnen und Benutzer des Datenübertragungsdienstes gelten Datenanbieter und Datenbezüger, die den Datenübertragungsdienst agridata.ch verwenden.</p>
<br><p>f. Datenproduzenten (z.B. Landwirte und Landwirtinnen) nutzen für die Bearbeitung ihrer Zustimmung zur Datenweitergabe das Zustimmungsmanagement von agridata.ch, müssen dafür aber nicht explizit den AGB zustimmen.</p>
<br><p>g. Die AGB gelten für diejenigen natürlichen Personen, welche den Datenübertragungsdienst als Datenanbieter oder Datenbezüger nutzen. Sofern die natürliche Person für eine juristische Person handelt, gelten die AGB auch für die juristische Person.</p>
<br><p>h. Die Benutzerin bzw. der Benutzer des Datenübertragungsdienstes muss den aktuell gültigen AGB bei der Bearbeitung von Datenanfragen zustimmen (Zustimmung per Mausklick).</p>
<br><p>i. Mit der Zustimmung zu den AGB entsteht zwischen der Benutzerin bzw. dem Benutzer und dem Betreiber des Datenübertragungsdienstes ein öffentlich-rechtlicher Vertrag (Art. 28b ISLV) zur Nutzung des Datenübertragungsdienstes agridata.ch.</p>
<br><p>j. Zusätzliche Rechte und Pflichten zwischen Datenanbieter und Datenbezüger werden im Vertrag zur Bereitstellung von Daten vom Datenanbieter an den Datenbezüger von den Vertragspartnern vereinbart. Ohne den Vertrag zwischen Datenanbieter und Datenbezüger erlaubt der Datenübertragungsdienst agidata.ch keine Datenübertragung zwischen den Vertragspartnern.</p>
<br><h2>2. Registrierung und Zugang</h2>
<br><p>a. Die Nutzung des Datenübertragungsdienstes setzt eine Registrierung durch die Benutzerin bzw. den Benutzer als Datenanbieter oder Datenbezüger voraus.</p>
<br><p>b. Nach erfolgreicher Registrierung erhält die Benutzerin bzw. der Benutzer Zugang auf den Datenübertragungsdienst und zu dessen Funktionalitäten als Datenanbieter oder Datenbezüger.</p>
<br><p>c. Mit der Registrierung willigt die Benutzerin bzw. der Benutzer in die zweckgebundene Bearbeitung ihrer bzw. seiner Personendaten ein.</p>
<br><h2>3. Nutzung / Rechte und Pflichten für Datenanbieter, Datenbezüger und Betreiber</h2>
<br><p>a. Die Nutzung des Datenübertragungsdienstes ist freiwillig.</p>
<br><p>b. Voraussetzungen: Der Datenübertragungsdienst dient einzig dem Transfer von Daten zwischen Behörden, zwischen Behörden und Akteuren des Agrar- und Ernährungssektors. Mindestens ein Akteur muss im Schweizer Agrar- und Ernährungssektor tätig sein.</p>
<br><p>c. Datenanbieter und Datenbezüger regeln die Datenübertragung untereinander vertraglich und können Leistungen, die sich für die Datenübertragung ergeben, untereinander in Rechnung stellen.</p>
<br><p>d. Verbotene Inhalte: Die Datenanbieter und Datenbezüger verpflichtet sich, keine rechtswidrigen oder gegen die guten Sitten verstossenden Inhalte bereitzustellen oder weiterzugeben.</p>
<br><p>e. Zwischen Datenanbieter und Datenbezüger dürfen nur Daten übertragen werden, welche einen Bezug zum Agrar- und Ernährungssektor haben. Ein Bezug liegt vor, wenn es sich um Daten mit ökonomischem, ökologischem oder administrativem Charakter handelt.</p>
<br><p>f. Identität: Jeder Datenanbieter und jeder Datenbezüger darf sich auf dem Datenübertragungsdienst nur mit einer einzigen Identität registrieren. Die Erstellung und Nutzung mehrerer Konten oder Identitäten durch dieselbe Nutzerin oder denselben Nutzer ist unzulässig. Ausnahmen sind möglich, wenn diese der Unterscheidung von beruflichen und privaten Aktivitäten dienen.</p>
<br><p>g. Formate: Die über den Datenübertragungsdienst zu transferierenden Daten unter oder mit Behörden müssen möglichst in anerkannten Formaten bereitgestellt werden. Zulässige Formate und deren allfällige Priorisierung sowie die definierten Dateninhalte ergeben sich aus der Metadatenplattform der Schweiz.</p>
<br><p>h. Der Datenaustausch zwischen Datenanbietern und Datenbezügern soll im Hinblick auf eine mögliche Ausdehnung des diesbezüglichen Benutzerkreises konsistent, strukturiert und gut dokumentiert sein, sodass eine einheitliche Bearbeitung und Nutzung gewährleistet werden können.</p>
<br><p>i. Die Benutzerinnen und Benutzer verpflichten sich, Vorgaben zu Identifikatoren für Unternehmen, Personen und Betriebe zu beachten.</p>
<br><p>j. Zustimmungsmanagement: Für die Datenübertragung wird eine explizite Zustimmung der Datenproduzenten vorausgesetzt. Ein «Consent Management System» als Teil des Datenübertragungsdienstes steht dafür den Datenproduzenten zur Verfügung. Datenproduzenten können Zustimmungen für Datenfreigaben elektronisch einsehen, erteilen und anpassen.</p>
<br><p>k. Der Betreiber stellt sicher, dass zwischen Datenanbieter und Datenbezüger nur Daten zu Datenanfragen übermittelt werden, für die eine Zustimmung der Produzenten in Datenübertragungsdienst agridata.ch erteilt wurde.</p>
<br><p>l. Auswertung für eigene Zwecke: Der Betreiber ist berechtigt, Daten der Nutzung des Datenübertragungsdienstes zu erheben und anonym auszuwerten. Diese Daten dienen ausschliesslich der Verbesserung des Datenübertragungsdienstes, der Optimierung der Benutzererfahrung sowie der Weiterentwicklung von Funktionen und Services. Die erhobenen Daten dürfen keine Rückschlüsse auf einzelne Nutzende zulassen und werden nicht zur individuellen Profilbildung oder zu kommerziellen Zwecken verwendet.</p>
<br><p>m. Der Betreiber ermöglicht dem Datenbezüger, Datenproduzenten auf die Datenanfrage hinzuweisen (z.B. Hyperlink, API, E-Mail) und stellt Vorlagen zur Verfügung, um gegenüber den Datenproduzenten eine durchgängige Kommunikation im Zusammenhang mit dem Datenübertragungsdienst einzuhalten.</p>
<br><p>n. Der Betreiber erhält und bearbeitet Angaben zu den Datenproduzenten, den Zustimmungen zur Datenübertragung und Verträgen zur Datenübertragung zwischen Datenanbieter und Datenbezüger sowie Metadaten zu den Datenübertragungen.</p>
<br><p>o. Der Betreiber nimmt keine Einsicht in die zwischen dem Datenanbieter und Datenbezüger übertragenen Daten, ausser mit Einverständnis der betroffenen Person oder im Bedarfsfall zu Supportzwecken.</p>
<br><p>p. Die Datenanbieter und Datenbezüger tragen ihre eigenen Aufwände und allfällige Kosten zur Anbindung an den Datenübertragungsdienst selbst.</p>
<br><p>q. Im Normalfall entstehen beim Betreiber keine Kosten für den Datenanbieter und Datenbezüger. Eine Ausnahme bildet z. B. eine übermässige Beanspruchung für Beratungsleistungen oder Arbeiten zur Anbindung durch den Betreiber.</p>
<br><h2>4. Technische Spezifikationen</h2>
<br><p>a. Für eine optimale Nutzung des Datenübertragungsdienstes müssen durch die Benutzerinnen und Benutzer gängige und aktuelle Browser- und Softwareversionen verwendet sowie regelmässige Systemwartungen durchgeführt werden.</p>
<br><p>b. Der Betreiber kann bei technischen Änderungen oder Updates neue Mindestanforderungen definieren, die von der Benutzerin oder dem Benutzer für die weitere Nutzung des Datenübertragungsdienstes zwingend beachtet werden müssen.</p>
<br><h2>5. Haftung</h2>
<br><p>a. Der Datenanbieter haftet für die von ihm bereitgestellten Daten und hält den Betreiber schadlos.</p>
<br><p>b. Die Benutzerin bzw. der Benutzer verpflichtet sich, den Datenübertragungsdienst nicht missbräuchlich zu nutzen, insbesondere nicht für Spam oder andere schädliche Aktivitäten.</p>
<br><p>c. Der Betreiber schliesst, soweit rechtlich zulässig, jegliche Haftungsansprüche, insbesondere wegen nicht funktionierendem Datenbezug oder mangelhaften Daten, aus.</p>
<br><h2>6. Datenschutz</h2>
<br><p>a. Der Betreiber verpflichtet sich, Daten der Benutzerin resp. des Benutzers gemäss den geltenden Datenschutzbestimmungen zu schützen, insbesondere ergreift der Betreiber technische und organisatorische Massnahmen für eine angemessene Datensicherheit in seiner Sphäre der Verantwortung und Zuständigkeit.</p>
<br><p>b. Die für den Datentransfer benötigten Daten werden in der Schweiz auf Servern des Betreibers oder eines hierzu beauftragten Unternehmens gehalten. Es werden sämtliche erforderlichen Vorkehrungen für eine sichere Datenhaltung getroffen.</p>
<br><h2>7. Verletzung der AGB</h2>
<br><p>Wenn die Benutzerin oder der Benutzer Bestimmungen diese AGB verletzt, so kann der Betreiber:</p>
<br><p>a. Die Benutzerin bzw. den Benutzer verwarnen.</p>
<br><p>b. Der Benutzerin bzw. dem Benutzer den Zugang zu Funktionen des Datenübertragungsdienstes einschränken.</p>
<br><p>c. Den Zugang der Benutzerin bzw. des Benutzers zum Datenübertragungsdienst für bis zu einem Jahr Dauer, in schweren Fällen für unbestimmte Zeit und ohne Rückerstattung von abgegoltenen Kosten sperren.</p>
<br><p>Ein strafrechtliches Vorgehen und Schadenersatzforderungen werden vorbehalten.</p>
<br><h2>8. Mitteilungen</h2>
<br><p>a. Mitteilungen des Betreibers an die Benutzerin bzw. den Benutzer erfolgen rechtsverbindlich durch Veröffentlichung auf der Webseite des Datenübertragungsdienstes. Dies gilt insbesondere auch für Änderungen dieser AGB.</p>
<br><h2>9. Kontakt und Support</h2>
<br><p>a. Bei Fragen zur Nutzung des Datenübertragungsdienstes oder technischen Problemen kann sich die Benutzerin bzw. Benutzer an den Support wenden unter: <a href="mailto:support@agridata.ch" title="" class="underline hover:text-agridata-primary-600">support@agridata.ch</a> 058 466 15 95.</p>
<br><h2>10. Unwirksamkeit, anwendbares Recht und Gerichtsstand</h2>
<br><p>a. Sollte eine Bestimmung der AGB unwirksam sein oder werden, oder die AGB eine an sich notwendige Regelung nicht enthalten, so wird dadurch die Wirksamkeit der übrigen Bestimmungen der AGB nicht berührt. Anstelle der unwirksamen Bestimmung oder zur Ausfüllung der Regelungslücke gilt diejenige rechtlich zulässige Bestimmung als vereinbart, die so weit wie möglich dem entspricht, was die Vertragsparteien gewollt haben oder nach Sinn und Zweck der Regelungen der AGB gewollt haben würden, wenn sie die Unwirksamkeit der betreffenden Bestimmung bzw. die Regelungslücke erkannt hätten.</p>
<br><p>b. Auf das Vertragsverhältnis ist ausschliesslich schweizerisches Recht, insbesondere das öffentliche Recht des Bundes anwendbar.</p>
<br><p>c. Über Streitigkeiten aus dem Vertragsverhältnis entscheidet das schweizerische Bundesverwaltungsgericht.</p>$agb_de$,
        'fr', $agb_fr$<h1>Conditions générales régissant l’utilisation du service de transfert de données agridata.ch</h1>
<br><h2>1. Objet</h2>
<br><p><strong>a.</strong> L’Office fédéral de l’agriculture OFAG, en sa qualité d’exploitant du service de transfert de données agridata.ch, édicte les présentes conditions générales (CG) sur la base de l’art. 28 b de l’ordonnance du 23 octobre 2013 sur les systèmes d’information dans le domaine de l’agriculture (OSIAgr ; RS 919.117.71).</p>
<br><p><strong>b.</strong> La version à jour et juridiquement valable des présentes CG est disponible sur le site Web du service de transfert de données, sous agridata.ch.</p>
<br><p><strong>c.</strong> Les CG règlent de manière contraignante les conditions d’utilisation du service de transfert de données agridata.ch. C’est la version à jour au moment de l’utilisation qui fait foi.</p>
<br><p><strong>d.</strong> L’Office fédéral de l’agriculture OFAG est l’exploitant du service de transfert de données agridata.ch.</p>
<br><p><strong>e.</strong> Sont considérés comme utilisateurs du service de transfert de données les fournisseurs de données et les utilisateurs de données qui se servent du service de transfert de données agridata.ch.</p>
<br><p><strong>f.</strong> Les producteurs de données (p. ex. les agriculteurs) utilisent, pour le traitement de leur consentement au transfert de données, l’outil de gestion des consentements d’agridata.ch, sans devoir pour autant accepter formellement les CG.</p>
<br><p><strong>g.</strong> Les CG s’appliquent aux personnes physiques qui utilisent le service de transfert de données en tant que fournisseurs de données ou utilisateurs de données. Si la personne physique agit pour le compte d’une personne morale, les CG s’appliquent également à celle-ci.</p>
<br><p><strong>h.</strong> L’utilisateur du service de transfert de données doit accepter les CG en vigueur lors du traitement des demandes de données (accord par clic de souris).</p>
<br><p><strong>i.</strong> L’acceptation des CG vaut naissance d’un contrat de droit public (art. 28 b OSIAgr) entre l’utilisateur et l’exploitant du service de transfert de données agridata.ch pour l’utilisation de celui-ci.</p>
<br><p><strong>j.</strong> Les droits et devoirs supplémentaires zwischen dem fournisseur de données et l’utilisateur de données sont convenus par les parties contractantes dans le contrat de mise à disposition des données par le fournisseur de données à l’utilisateur de données. En l’absence de contrat entre le fournisseur des données et l’utilisateur de données, le service de transfert de données agidata.ch ne permet le transfert de données entre les parties.</p>
<br><h2>2. Enregistrement et accès</h2>
<br><p><strong>a.</strong> Pour pouvoir se servir du service de transfert de données, l’utilisateur doit au préalable s’enregistrer en qualité de fournisseur de données ou d’utilisateur de données.</p>
<br><p><strong>b.</strong> Après l’enregistrement, l’utilisateur a accès au service de transfert de données et à ses fonctionnalités en qualité de fournisseur de données ou d’utilisateur de données.</p>
<br><p><strong>c.</strong> En s’enregistrant, l’utilisateur consent au traitement de ses données personnelles conformément à leur destination.</p>
<br><h2>3. Utilisation / droits et devoirs pour les fournisseurs de données, les utilisateurs de données et les exploitants</h2>
<br><p><strong>a.</strong> L’utilisation du service de transfert de données est facultative.</p>
<br><p><strong>b.</strong> Conditions : le service de transfert de données sert uniquement au transfert de données entre autorités, ainsi qu’entre les autorités et les acteurs du secteur agroalimentaire. Au moins un acteur doit être actif dans le secteur agroalimentaire suisse.</p>
<br><p><strong>c.</strong> Les fournisseurs de données et les utilisateurs de données règlent le transfert des données zwischen eux par voie de contrat et peuvent se facturer mutuellement les prestations qui résultent de ce transfert.</p>
<br><p><strong>d.</strong> Contenus interdits : les fournisseurs de données et les utilisateurs de données s’engagent à ne pas mettre à disposition ou transmettre des contenus illégaux ou contraires aux bonnes mœurs.</p>
<br><p><strong>e.</strong> Seules les données ayant un lien avec le secteur agroalimentaire peuvent être transmises entre le fournisseur de données et l’utilisateur de données. Un tel lien existe lorsque les données ont un caractère économique, écologique ou administratif.</p>
<br><p><strong>f.</strong> Identité : chaque fournisseur de données et chaque utilisateur de données ne peut s’enregistrer sur le service de transfert de données que sous une seule identité. La création et l’utilisation de plusieurs comptes ou identités par le même utilisateur sont interdites. Des exceptions sont possibles si elles servent à distinguer les activités professionnelles des activités privées.</p>
<br><p><strong>g.</strong> Formats : les données à transférer entre les autorités ou avec celles-ci par le biais du service de transfert de données doivent, dans la mesure du possible, être fournies dans des formats reconnus. Les formats autorisés et leur éventuelle priorisation ainsi que les contenus de données définis sont indiqués sur la plateforme de métadonnées de la Suisse (Catalogue de métadonnées de la Suisse).</p>
<br><p><strong>h.</strong> L’échange de données entre les fournisseurs de données et les utilisateurs de données doit être cohérent, structuré et bien documenté, de manière à garantir un traitement et une utilisation uniformes dans la perspective d’une éventuelle extension du cercle des utilisateurs concernés.</p>
<br><p><strong>i.</strong> Les utilisateurs s’engagent à respecter les directives concernant les identifiants pour les entreprises, les personnes et les établissements.</p>
<br><p><strong>j.</strong> Gestion des consentements : le transfert des données est soumis à l’accord explicite des producteurs de données. Un « système de gestion des consentements » faisant partie du service de transfert des données est à cet effet à la disposition des producteurs de données. Les producteurs de données peuvent consulter, donner et adapter électroniquement les consentements pour les partages de données.</p>
<br><p><strong>k.</strong> L’exploitant s’assure que seules les données relatives aux demandes pour lesquelles un consentement des producteurs a été accordé dans le service de transfert de données agridata.ch sont transmises entre le fournisseur de données et l’utilisateur de données.</p>
<br><p><strong>l.</strong> Évaluation à des fins propres : l’exploitant a le droit de collecter des données sur l’utilisation du service de transfert de données et à les évaluer de manière anonyme. Ces données servent uniquement à améliorer le service de transfert de données, à optimiser l’expérience utilisateur et à développer les fonctions et les services. Les données collectées ne doivent pas permettre d’identifier des utilisateurs individuels et ne sont pas utilisées pour le profilage individuel ou à des fins commerciales.</p>
<br><p><strong>m.</strong> L’exploitant permet à l’utilisateur de données d’attirer l’attention des producteurs de données sur la demande (p. ex. lien hypertexte, API, courriel) et met à disposition des modèles afin d’assurer une communication continue avec les producteurs de données en rapport avec le service de transfert de données.</p>
<br><p><strong>n.</strong> L’exploitant reçoit et traite des informations sur les producteurs de données, les consentements au transfert de données et les contrats de transfert de données entre le fournisseur de données et l’utilisateur de données, ainsi que des métadonnées sur les transferts de données.</p>
<br><p><strong>o.</strong> L’exploitant ne consulte pas les données transmises entre le fournisseur de données et l’utilisateur de données, sauf avec l’accord de la personne concernée ou, si nécessaire, à des fins d’assistance.</p>
<br><p><strong>p.</strong> Les fournisseurs de données et les utilisateurs de données assument leurs propres dépenses et les éventuels coûts de connexion au service de transfert de données.</p>
<br><p><strong>q.</strong> Dans des conditions normales, le fournisseur de données et l’utilisateur de données n’encourent pas de frais auprès de l’exploitant. La sollicitation excessive de prestations de conseil ou de travaux de raccordement par l’exploitant constitue par exemple eine exception.</p>
<br><h2>4. Spécifications techniques</h2>
<br><p><strong>a.</strong> Pour une utilisation optimale du service de transfert de données, les utilisateurs doivent utiliser des versions courantes et à jour de navigateurs et de logiciels et procéder à une maintenance régulière de leur système.</p>
<br><p><strong>b.</strong> En cas de modifications ou de mises à jour techniques, l’opérateur peut définir de nouvelles exigences minimales à respecter impérativement par l’utilisateur pour la poursuite de l’utilisation du service de transfert de données.</p>
<br><h2>5. Responsabilité</h2>
<br><p><strong>a.</strong> Le fournisseur de données répond des données qu’il met à disposition et il indemnise l’exploitant en cas de dommage.</p>
<br><p><strong>b.</strong> L’utilisateur s’engage à ne pas utiliser le service de transfert de données de manière abusive, notamment pour des spams ou d’autres activités nuisibles.</p>
<br><p><strong>c.</strong> Dans la mesure où cela est juridiquement admissible, l’exploitant exclut toute action en responsabilité, notamment en cas de non-fonctionnement de l’accès aux données ou de données défectueuses.</p>
<br><h2>6. Protection des données</h2>
<br><p><strong>a.</strong> L’exploitant s’engage à protéger les données de l’utilisateur conformément aux dispositions sur la protection des données en vigueur. En particulier, il prend des mesures techniques et organisationnelles pour assurer une sécurité appropriée des données dans sa sphère de responsabilité et de compétence.</p>
<br><p><strong>b.</strong> Les données nécessaires au transfert de données sont conservées en Suisse sur les serveurs de l’exploitant ou d’une entreprise mandatée à cet effet. Toutes les précautions nécessaires sont prises pour assurer la sécurité des données.</p>
<br><h2>7. Violation des CG</h2>
<br><p>Si l’utilisateur enfreint les dispositions des présentes CG, l’exploitant peut :</p>
<br><p><strong>a.</strong> donner un avertissement à l’utilisateur ;<br><strong>b.</strong> limiter l’accès de l’utilisateur aux fonctions du service de transfert de données ;<br><strong>c.</strong> bloquer l’accès de l’utilisateur au service de transfert de données pour une durée pouvant aller jusqu’à un an, voire pour une durée indéterminée dans les cas graves, et sans remboursement des frais déjà indemnisés.</p>
<br><p>L’exploitant se réserve le droit d’engager des poursuites pénales et de réclamer des dommages et intérêts.</p>
<br><h2>8. Communications</h2>
<br><p><strong>a.</strong> Les communications de l’exploitant à l’utilisateur sont juridiquement contraignantes par leur publication sur le site Web du service de transfert de données. Cela vaut en particulier aussi pour les modifications apportées aux présentes CG.</p>
<br><h2>9. Assistance et contact</h2>
<br><p><strong>a.</strong> En cas de questions sur l’utilisation du service de transfert de données ou de problèmes techniques, l’utilisateur peut s’adresser au service d’assistance à l’adresse <a href="mailto:support@agridata.ch" title="" class="underline hover:text-agridata-primary-600">support@agridata.ch</a> ou au 058 466 15 95.</p>
<br><h2>10. Invalidité, droit applicable et for</h2>
<br><p><strong>a.</strong> Si une disposition des CG est ou devient invalide, ou si les CG ne contiennent pas de disposition nécessaire en soi, la validité des autres dispositions des CG n’en est pas affectée. En lieu et place de la disposition invalide, ou pour combler le vide juridique, est réputée convenue la disposition juridiquement admissible qui correspond le plus possible à ce que les parties contractantes ont voulu ou auraient voulu conformément au sens et au but des présentes CG, si elles avaient eu connaissance de l’invalidité de la disposition concernée ou du vide juridique.</p>
<br><p><strong>b.</strong> Seul le droit suisse, en particulier le droit public de la Confédération, s’applique au contrat.</p>
<br><p><strong>c.</strong> Le Tribunal administratif fédéral statue sur les litiges découlant du présent contrat.</p>$agb_fr$,
        'it', $agb_it$<h1>Conditions générales régissant l’utilisation du service de transfert de données agridata.ch</h1>
<br><h2>1. Objet</h2>
<br><p><strong>a.</strong> L’Office fédéral de l’agriculture OFAG, en sa qualité d’exploitant du service de transfert de données agridata.ch, édicte les présentes conditions générales (CG) sur la base de l’art. 28 b de l’ordonnance du 23 octobre 2013 sur les systèmes d’information dans le domaine de l’agriculture (OSIAgr ; RS 919.117.71).</p>
<br><p><strong>b.</strong> La version à jour et juridiquement valable des présentes CG est disponible sur le site Web du service de transfert de données, sous agridata.ch.</p>
<br><p><strong>c.</strong> Les CG règlent de manière contraignante les conditions d’utilisation du service de transfert de données agridata.ch. C’est la version à jour au moment de l’utilisation qui fait foi.</p>
<br><p><strong>d.</strong> L’Office fédéral de l’agriculture OFAG est l’exploitant du service de transfert de données agridata.ch.</p>
<br><p><strong>e.</strong> Sont considérés comme utilisateurs du service de transfert de données les fournisseurs de données et les utilisateurs de données qui se servent du service de transfert de données agridata.ch.</p>
<br><p><strong>f.</strong> Les producteurs de données (p. ex. les agriculteurs) utilisent, pour le traitement de leur consentement au transfert de données, l’outil de gestion des consentements d’agridata.ch, sans devoir pour autant accepter formellement les CG.</p>
<br><p><strong>g.</strong> Les CG s’appliquent aux personnes physiques qui utilisent le service de transfert de données en tant que fournisseurs de données ou utilisateurs de données. Si la personne physique agit pour le compte d’une personne morale, les CG s’appliquent également à celle-ci.</p>
<br><p><strong>h.</strong> L’utilisateur du service de transfert de données doit accepter les CG en vigueur lors du traitement des demandes de données (accord par clic de souris).</p>
<br><p><strong>i.</strong> L’acceptation des CG vaut naissance d’un contrat de droit public (art. 28 b OSIAgr) entre l’utilisateur et l’exploitant du service de transfert de données agridata.ch pour l’utilisation de celui-ci.</p>
<br><p><strong>j.</strong> Les droits et devoirs supplémentaires zwischen dem fournisseur de données et l’utilisateur de données sont convenus par les parties contractantes dans le contrat de mise à disposition des données par le fournisseur de données à l’utilisateur de données. En l’absence de contrat entre le fournisseur des données et l’utilisateur de données, le service de transfert de données agidata.ch ne permet le transfert de données entre les parties.</p>
<br><h2>2. Enregistrement et accès</h2>
<br><p><strong>a.</strong> Pour pouvoir se servir du service de transfert de données, l’utilisateur doit au préalable s’enregistrer en qualité de fournisseur de données ou d’utilisateur de données.</p>
<br><p><strong>b.</strong> Après l’enregistrement, l’utilisateur a accès au service de transfert de données et à ses fonctionnalités en qualité de fournisseur de données ou d’utilisateur de données.</p>
<br><p><strong>c.</strong> En s’enregistrant, l’utilisateur consent au traitement de ses données personnelles conformément à leur destination.</p>
<br><h2>3. Utilisation / droits et devoirs pour les fournisseurs de données, les utilisateurs de données et les exploitants</h2>
<br><p><strong>a.</strong> L’utilisation du service de transfert de données est facultative.</p>
<br><p><strong>b.</strong> Conditions : le service de transfert de données sert uniquement au transfert de données entre autorités, ainsi qu’entre les autorités et les acteurs du secteur agroalimentaire. Au moins un acteur doit être actif dans le secteur agroalimentaire suisse.</p>
<br><p><strong>c.</strong> Les fournisseurs de données et les utilisateurs de données règlent le transfert des données zwischen eux par voie de contrat et peuvent se facturer mutuellement les prestations qui résultent de ce transfert.</p>
<br><p><strong>d.</strong> Contenus interdits : les fournisseurs de données et les utilisateurs de données s’engagent à ne pas mettre à disposition ou transmettre des contenus illégaux ou contraires aux bonnes mœurs.</p>
<br><p><strong>e.</strong> Seules les données ayant un lien avec le secteur agroalimentaire peuvent être transmises entre le fournisseur de données et l’utilisateur de données. Un tel lien existe lorsque les données ont un caractère économique, écologique ou administratif.</p>
<br><p><strong>f.</strong> Identité : chaque fournisseur de données et chaque utilisateur de données ne peut s’enregistrer sur le service de transfert de données que sous une seule identité. La création et l’utilisation de plusieurs comptes ou identités par le même utilisateur sont interdites. Des exceptions sont possibles si elles servent à distinguer les activités professionnelles des activités privées.</p>
<br><p><strong>g.</strong> Formats : les données à transférer entre les autorités ou avec celles-ci par le biais du service de transfert de données doivent, dans la mesure du possible, être fournies dans des formats reconnus. Les formats autorisés et leur éventuelle priorisation ainsi que les contenus de données définis sont indiqués sur la plateforme de métadonnées de la Suisse (Catalogue de métadonnées de la Suisse).</p>
<br><p><strong>h.</strong> L’échange de données entre les fournisseurs de données et les utilisateurs de données doit être cohérent, structuré et bien documenté, de manière à garantir un traitement et une utilisation uniformes dans la perspective d’une éventuelle extension du cercle des utilisateurs concernés.</p>
<br><p><strong>i.</strong> Les utilisateurs s’engagent à respecter les directives concernant les identifiants pour les entreprises, les personnes et les établissements.</p>
<br><p><strong>j.</strong> Gestion des consentements : le transfert des données est soumis à l’accord explicite des producteurs de données. Un « système de gestion des consentements » faisant partie du service de transfert des données est à cet effet à la disposition des producteurs de données. Les producteurs de données peuvent consulter, donner et adapter électroniquement les consentements pour les partages de données.</p>
<br><p><strong>k.</strong> L’exploitant s’assure que seules les données relatives aux demandes pour lesquelles un consentement des producteurs a été accordé dans le service de transfert de données agridata.ch sont transmises entre le fournisseur de données et l’utilisateur de données.</p>
<br><p><strong>l.</strong> Évaluation à des fins propres : l’exploitant a le droit de collecter des données sur l’utilisation du service de transfert de données et à les évaluer de manière anonyme. Ces données servent uniquement à améliorer le service de transfert de données, à optimiser l’expérience utilisateur et à développer les fonctions et les services. Les données collectées ne doivent pas permettre d’identifier des utilisateurs individuels et ne sont pas utilisées pour le profilage individuel ou à des fins commerciales.</p>
<br><p><strong>m.</strong> L’exploitant permet à l’utilisateur de données d’attirer l’attention des producteurs de données sur la demande (p. ex. lien hypertexte, API, courriel) et met à disposition des modèles afin d’assurer une communication continue avec les producteurs de données en rapport avec le service de transfert de données.</p>
<br><p><strong>n.</strong> L’exploitant reçoit et traite des informations sur les producteurs de données, les consentements au transfert de données et les contrats de transfert de données entre le fournisseur de données et l’utilisateur de données, ainsi que des métadonnées sur les transferts de données.</p>
<br><p><strong>o.</strong> L’exploitant ne consulte pas les données transmises entre le fournisseur de données et l’utilisateur de données, sauf avec l’accord de la personne concernée ou, si nécessaire, à des fins d’assistance.</p>
<br><p><strong>p.</strong> Les fournisseurs de données et les utilisateurs de données assument leurs propres dépenses et les éventuels coûts de connexion au service de transfert de données.</p>
<br><p><strong>q.</strong> Dans des conditions normales, le fournisseur de données et l’utilisateur de données n’encourent pas de frais auprès de l’exploitant. La sollicitation excessive de prestations de conseil ou de travaux de raccordement par l’exploitant constitue par exemple eine exception.</p>
<br><h2>4. Spécifications techniques</h2>
<br><p><strong>a.</strong> Pour une utilisation optimale du service de transfert de données, les utilisateurs doivent utiliser des versions courantes et à jour de navigateurs et de logiciels et procéder à une maintenance régulière de leur système.</p>
<br><p><strong>b.</strong> En cas de modifications ou de mises à jour techniques, l’opérateur peut définir de nouvelles exigences minimales à respecter impérativement par l’utilisateur pour la poursuite de l’utilisation du service de transfert de données.</p>
<br><h2>5. Responsabilité</h2>
<br><p><strong>a.</strong> Le fournisseur de données répond des données qu’il met à disposition et il indemnise l’exploitant en cas de dommage.</p>
<br><p><strong>b.</strong> L’utilisateur s’engage à ne pas utiliser le service de transfert de données de manière abusive, notamment pour des spams ou d’autres activités nuisibles.</p>
<br><p><strong>c.</strong> Dans la mesure où cela est juridiquement admissible, l’exploitant exclut toute action en responsabilité, notamment en cas de non-fonctionnement de l’accès aux données ou de données défectueuses.</p>
<br><h2>6. Protection des données</h2>
<br><p><strong>a.</strong> L’exploitant s’engage à protéger les données de l’utilisateur conformément aux dispositions sur la protection des données en vigueur. En particulier, il prend des mesures techniques et organisationnelles pour assurer une sécurité appropriée des données dans sa sphère de responsabilité et de compétence.</p>
<br><p><strong>b.</strong> Les données nécessaires au transfert de données sont conservées en Suisse sur les serveurs de l’exploitant ou d’une entreprise mandatée à cet effet. Toutes les précautions nécessaires sont prises pour assurer la sécurité des données.</p>
<br><h2>7. Violation des CG</h2>
<br><p>Si l’utilisateur enfreint les dispositions des présentes CG, l’exploitant peut :</p>
<br><p><strong>a.</strong> donner un avertissement à l’utilisateur ;<br><strong>b.</strong> limiter l’accès de l’utilisateur aux fonctions du service de transfert de données ;<br><strong>c.</strong> bloquer l’accès de l’utilisateur au service de transfert de données pour une durée pouvant aller jusqu’à un an, voire pour une durée indéterminée dans les cas graves, et sans remboursement des frais déjà indemnisés.</p>
<br><p>L’exploitant se réserve le droit d’engager des poursuites pénales et de réclamer des dommages et intérêts.</p>
<br><h2>8. Communications</h2>
<br><p><strong>a.</strong> Les communications de l’exploitant à l’utilisateur sont juridiquement contraignantes par leur publication sur le site Web du service de transfert de données. Cela vaut en particulier aussi pour les modifications apportées aux présentes CG.</p>
<br><h2>9. Assistance et contact</h2>
<br><p><strong>a.</strong> En cas de questions sur l’utilisation du service de transfert de données ou de problèmes techniques, l’utilisateur peut s’adresser au service d’assistance à l’adresse <a href="mailto:support@agridata.ch" title="" class="underline hover:text-agridata-primary-600">support@agridata.ch</a> ou au 058 466 15 95.</p>
<br><h2>10. Invalidité, droit applicable et for</h2>
<br><p><strong>a.</strong> Si une disposition des CG est ou devient invalide, ou si les CG ne contiennent pas de disposition nécessaire en soi, la validité des autres dispositions des CG n’en est pas affectée. En lieu et place de la disposition invalide, ou pour combler le vide juridique, est réputée convenue la disposition juridiquement admissible qui correspond le plus possible à ce que les parties contractantes ont voulu ou auraient voulu conformément au sens et au but des présentes CG, si elles avaient eu connaissance de l’invalidité de la disposition concernée ou du vide juridique.</p>
<br><p><strong>b.</strong> Seul le droit suisse, en particulier le droit public de la Confédération, s’applique au contrat.</p>
<br><p><strong>c.</strong> Le Tribunal administratif fédéral statue sur les litiges découlant du présent contrat.</p>$agb_it$
    )
);
