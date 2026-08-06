<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:template name="germanContract">

        <fo:block xsl:use-attribute-sets="document-title">
            Datenaustauschvertrag agridata.ch
        </fo:block>

        <xsl:call-template name="horizontal-rule">
            <xsl:with-param name="space-after">10mm</xsl:with-param>
        </xsl:call-template>

        <fo:block xsl:use-attribute-sets="body-text-spacious">
            zwischen
        </fo:block>

        <fo:block xsl:use-attribute-sets="bold-paragraph">
            <xsl:value-of select="consumerAddressInline"/> (hiernach «Datenanbieter»)
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text-spacious">
            und
        </fo:block>

        <fo:block xsl:use-attribute-sets="bold-paragraph">
            <xsl:value-of select="providerAddressInline"/> (hiernach «Datenbezüger»)
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text-spacious">
            (zusammen «Vertragspartner»)
        </fo:block>

        <xsl:call-template name="horizontal-rule"/>

        <fo:block xsl:use-attribute-sets="section-heading">
            Rechtsgrundlage für Datenübermittlung
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text-justify">
            <fo:list-block>
                <fo:list-item>
                    <fo:list-item-label end-indent="label-end()">
                        <fo:block>1.</fo:block>
                    </fo:list-item-label>
                    <fo:list-item-body start-indent="body-start()">
                        <fo:block>
                            Nutzung des Datenübertragungsdienst: Die Bereitstellung und Nutzung des technischen Dienstes
                            agridata.ch erfolgen gestützt auf Art. 165c ff. LwG und Art. 27 Abs. 9 ISLV.
                        </fo:block>
                    </fo:list-item-body>
                </fo:list-item>
                <fo:list-item>
                    <fo:list-item-label end-indent="label-end()">
                        <fo:block>2.</fo:block>
                    </fo:list-item-label>
                    <fo:list-item-body start-indent="body-start()">
                        <fo:block>
                            <xsl:value-of select="providerSystemLegalBasis/de"/>
                        </fo:block>
                    </fo:list-item-body>
                </fo:list-item>
                <fo:list-item>
                    <fo:list-item-label end-indent="label-end()">
                        <fo:block>3.</fo:block>
                    </fo:list-item-label>
                    <fo:list-item-body start-indent="body-start()">
                        <fo:block>
                            Sofern sich der Sitz oder Wohnsitz des Datenbezügers ausserhalb der Schweiz befindet,
                            verpflichtet sich dieser zur Einhaltung der Bestimmungen des Schweizerischen
                            Datenschutzgesetzes (DSG). Liegt im Sitzstaat des Datenbezügers kein vom Schweizerischen
                            Bundesrat als angemessen anerkanntes Datenschutzniveau vor, garantiert der Datenbezüger den
                            Schutz der übermittelten Daten durch die Übernahme der Standardvertragsklauseln (SCC)
                            beziehungsweise gleichwertiger, durch den EDÖB anerkannter Garantien (gemäss Art. 16 Abs. 2
                            DSG).
                        </fo:block>
                    </fo:list-item-body>
                </fo:list-item>
            </fo:list-block>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Vertragsgegenstand
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            Der vorliegende Datenaustauschvertrag regelt die Bereitstellung von Daten durch den Datenanbieter zum Abruf
            durch den Datenbezüger über den Datenübertragungsdienst agridata.ch. Er legt die diesbezüglichen Rechte und
            Pflichten der Vertragspartner fest.
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Dieser Datenaustauschvertrag wird in deutscher und französischer Sprache ausgefertigt. Im Falle von
            Widersprüchen oder Auslegungszweifeln gilt ausschliesslich die deutsche Version.
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            1. Definition und Erklärung von Begriffen
        </fo:block>

        <fo:list-block provisional-distance-between-starts="5mm"
                       margin-left="10mm"
                       font-family="LiberationSans"
                       font-size="10pt"
                       line-height="15pt">

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">agridata.ch</fo:inline>:
                        Die Vertragspartner nutzen für die Datenübertragung den Datenübertragungsdienst agridata.ch.
                        Dieser dient dem Informieren der Datenproduzenten sowie, bei zustimmungspflichtigen
                        Datenprodukten, der Einholung von deren Zustimmung. Die Nutzung von agridata.ch erfolgt für die
                        Vertragspartner auf freiwilliger Basis und die geltenden Allgemeinen Geschäftsbedingungen (AGB)
                        sind von den Vertragspartnern unabhängig vom vorliegenden Datenaustauschvertrag einzuhalten.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Datenproduzent</fo:inline>:
                        Als Datenproduzenten gelten natürliche oder juristische Personen, die im Rahmen ihrer Tätigkeit
                        Daten erzeugen, erfassen oder bereitstellen, welche über agridata.ch übermittelt werden können.
                        Dazu gehören insbesondere, aber nicht abschliessend, Bewirtschafterinnen und Bewirtschafter,
                        Tierhalterinnen und Tierhalter, Eigentümerinnen und Eigentümer von Equiden sowie Unternehmen,
                        Organisationen oder Institutionen, die landwirtschaftsrelevante Daten erzeugen oder verwalten.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Datenbezüger</fo:inline>:
                        Als Datenbezüger gelten juristische Personen, Behörden oder natürliche Personen in Ausübung
                        ihrer beruflichen oder gewerblichen Tätigkeit (z. B. Einzelfirmen), die über agridata.ch auf die
                        Daten der Datenproduzenten zugreifen oder entsprechende Datenanfragen stellen, um diese
                        ausschliesslich zu den im Datenantrag festgelegten Zwecken zu verwenden. Der Zugriff auf Daten
                        ist nur möglich, sofern die Bereitstellungsvoraussetzungen gemäss diesem Vertrag erfüllt sind.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Datenanbieter</fo:inline>:
                        Als Datenanbieter gelten natürliche oder juristische Personen, oder Behörden, die über
                        agridata.ch einem Datenbezüger die von diesem beantragten Daten bereitstellen. Der Datenanbieter
                        entscheidet eigenständig unter Einhaltung der für ihn massgebenden rechtlichen Bestimmungen, ob
                        und welchem Datenbezüger er seine Daten zur Verfügung stellt.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Datenprodukt</fo:inline>:
                        Datenanbieter stellen Datenprodukte mit klar definierten Inhalten und Datenformaten für die
                        Datenbezüger zur Auswahl. Ein Datenbezüger kann mehrere Datenprodukte eines Datenanbieters
                        beantragen.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Datenantrag</fo:inline>:
                        Von einem Datenbezüger über den Datenübertragungsdienst agridata.ch eingereichtes Gesuch an den
                        Datenanbieter zum Bezug definierter Datenprodukte für einen festgelegten Nutzungszweck. Der
                        genehmigte Datenantrag bildet die Grundlage des vorliegenden Datenaustauschvertrages.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Datenanfrage</fo:inline>:
                        Die auf einem genehmigten Datenantrag basierende, an die einzelnen Datenproduzenten gerichtete
                        Information auf agridata.ch. Sie dient der Transparenz und ermöglicht den Datenproduzenten
                        Einsicht in den Zweck der Datennutzung sowie, bei zustimmungspflichtigen Datenprodukten, die
                        Erteilung oder Ablehnung ihrer Zustimmung zur Datenweitergabe.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Zustimmungsprüfung</fo:inline>:
                        Automatisierte Systemfunktion des Datenübertragungsdienstes agridata.ch, welche vor jeder
                        Datenübermittlung an der Schnittstelle überprüft und sicherstellt, dass die rechtlichen
                        Bereitstellungsvoraussetzungen (bei zustimmungspflichtigen Datenprodukten das Vorliegen einer
                        aktiven und gültigen Zustimmung des jeweiligen Datenproduzenten) erfüllt sind.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Datenschnittstelle</fo:inline>:
                        Geschützte technische Schnittstelle, über die der Datenbezüger bzw. der Datenanbieter bei
                        agridata.ch angebunden ist und über die pro Datenantrag die Daten der definierten
                        Datenproduzenten unter Berücksichtigung der jeweiligen Bereitstellungsvoraussetzungen bezogen
                        werden können.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            2. Inhaltliche Angaben zum Datenantrag
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Die nachfolgenden Angaben basieren auf dem vom Datenbezüger auf agridata.ch eingereichten Datenantrag. Sie
            werden den Datenproduzenten in der Datenanfrage angezeigt und dienen als Entscheidungsgrundlage für ihre
            Zustimmung:
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Datenbezüger:
        </fo:block>

        <fo:block>
            <xsl:value-of select="consumerName"/>
        </fo:block>
        <fo:block>
            UID:
            <xsl:value-of select="consumerUid"/>
        </fo:block>
        <fo:block>
            <xsl:value-of select="consumerStreet"/>
        </fo:block>
        <fo:block>
            <xsl:value-of select="consumerZipCity"/>
        </fo:block>
        <fo:block>
            <xsl:value-of select="consumerCountry/de"/>
        </fo:block>

        <fo:block>
            Kontakt: <xsl:value-of select="consumerPhoneNumber"/>,
            <xsl:value-of select="consumerEmailAddress"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Name Datenantrag:
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Der im Antrag definierte Titel des Datenanbieters
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Beschreibung des Datenantrags:
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Deutsch:
            <xsl:value-of select="requestDescription/de"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            Französisch:
            <xsl:value-of select="requestDescription/fr"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            Italienisch:
            <xsl:value-of select="requestDescription/it"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Zweck der Datennutzung durch den Datenbezüger:
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Deutsch:
            <xsl:value-of select="requestPurpose/de"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            Französisch:
            <xsl:value-of select="requestPurpose/fr"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            Italienisch:
            <xsl:value-of select="requestPurpose/it"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Datenanbieter System:
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            Das im Antrag definierte Quellsystem des Datenanbieters
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Gewünschte Datenprodukte:
        </fo:block>
        <fo:block>
            Gegenstand dieses Datenaustauschvertrag sind diejenigen Datenprodukte, welche im elektronischen Datenantrag
            auf agridata.ch unter der Antrags-ID
            <xsl:value-of select="requestHumanFriendlyId"/> geführt und freigegeben sind.
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Angabe zu Zielgruppe:
        </fo:block>
        <fo:block>
            <xsl:value-of select="targetGroup"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3. Pflichten der Vertragspartner
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3.1 Datenanbieter
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenanbieter spezifiziert und beschreibt die von ihm bereitgestellten Datenprodukte
                        verbindlich bezüglich Inhalts und Format. Die Datenproduktbeschreibung des Datenanbieters muss
                        öffentlich zugänglich und über agridata.ch aufrufbar sein.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Für die Bereitstellung von Datenprodukten nutzt der Datenanbieter die automatisierte Steuerung
                        von agridata.ch, welche sicherstellt, dass die Übermittlung von Datenprodukten strikt an das
                        Vorliegen einer aktiven Zustimmung des Datenproduzenten in der Zustimmungsprüfung gekoppelt ist.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenanbieter stellt die Datenprodukte über die geschützte Schnittstelle direkt an den
                        Datenübertragungsdienst agridata.ch bereit. Er stellt sicher, dass seinerseits nur
                        datenschutzkonforme und vom Datenübertragungsdienst agridata.ch angeforderte Datenprodukte
                        übergeben werden. Die anschliessende Absicherung des Abrufs und die Bereitstellung für den
                        Datenbezüger erfolgen durch agridata.ch gemäss den jeweils anwendbaren
                        Bereitstellungsvoraussetzungen (bei zustimmungspflichtigen Datenprodukten das Vorliegen einer
                        aktiven und gültigen Zustimmung des Datenproduzenten; bei zustimmungsfreien Datenprodukten das
                        Vorliegen der gesetzlichen Grundlage und der Produzentendefinition). Abweichende, bilaterale
                        Bereitstellungswege (z. B. die direkte Datenübergabe durch den Datenanbieter via
                        Applikations-GUI, E-Mail oder manuellen Datei-Export) bleiben vorbehalten. In diesen Fällen
                        obliegt es der alleinigen Verantwortung des Datenanbieters, vor jeder Datenauslieferung
                        eigenständig zu überprüfen und sicherzustellen, dass die entsprechenden
                        Bereitstellungsvoraussetzungen (aktive Zustimmung bzw. gesetzliche Transparenzanzeige) in
                        agridata.ch erfüllt sind. Die Leistung von agridata.ch beschränkt sich hierbei auf das
                        Bereitstellen der Legitimationsinformationen; die technische Übermittlung und Datensicherheit
                        des gewählten Transportwegs liegen vollumfänglich beim Datenanbieter.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenanbieter stellt auf eigene Kosten eine geeignete technische Schnittstelle zu
                        agridata.ch zur Verfügung und unterhält diese. Er trägt die alleinige Verantwortung für die
                        funktions- und spezifikationskonforme Umsetzung der Schnittstelle auf seinen eigenen Systemen.
                        Der sichere und automatisierte Austausch von Datenprodukten sowie die Fristen und
                        Informationspflichten bei Schnittstellenanpassungen richten sich nach den jeweils gültigen
                        Nutzungsbedingungen (AGB) für Datenanbieter von agridata.ch.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenanbieter informiert über die Einstellung oder wesentliche Änderung von Datenprodukten
                        rechtzeitig im Voraus über den Datenübertragungsdienst agridata.ch. Die Information der
                        betroffenen Datenbezüger erfolgt über agridata.ch. Das Risiko und die Verantwortung für die
                        rechtzeitige Anpassung der eigenen Systeme an Produktänderungen (Holschuld) liegen beim
                        Datenbezüger. Details zu den Ankündigungsfristen, Übergangsphasen sowie der Ausserdienststellung
                        von Datenprodukten sind in den Nutzungsbedingungen (AGB) geregelt.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Entstehende Kosten für die Einrichtung und Anpassung der Datenbereitstellung beim Datenanbieter
                        (z.B. für neue oder angepasste IT-Infrastruktur oder Anpassungen an der Schnittstelle) können
                        nicht dem Datenbezüger oder agridata.ch in Rechnung gestellt werden.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>


        <fo:block xsl:use-attribute-sets="section-heading">
            3.2 Datenbezüger
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Die Einholung der erforderlichen Zustimmungen bei den Datenproduzenten ist die ausschliessliche
                        Aufgabe und Verantwortung des Datenbezügers. Den Datenanbieter trifft keinerlei Pflicht zur
                        Information, Aufklärung oder Aktivierung der Datenproduzenten. Der Datenanbieter haftet nicht
                        für das Vorliegen oder die Gültigkeit der Zustimmungen; die technische Überprüfung und
                        Absicherung der Zustimmungen vor der Datenbereitstellung erfolgen vollautomatisiert durch
                        agridata.ch.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenbezüger verpflichtet sich, Datenanfragen ausschliesslich an diejenigen Datenproduzenten
                        zu richten, die im vorliegenden Datenaustauschvertrag als Zielgruppe definiert sind. Eine
                        wahllose oder zweckfremde Kontaktaufnahme ist untersagt.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenbezüger trägt sämtliche Kosten, die ihm im Zusammenhang mit der Einrichtung, Anpassung,
                        Nutzung oder dem Unterhalt des Datenbezugs entstehen (z. B. für die eigene IT-Infrastruktur,
                        Schnittstellenanpassungen oder Drittsoftware). Jegliche Kostenabwälzung oder Rechnungsstellung
                        an den Datenanbieter oder an agridata.ch ist ausgeschlossen.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3.3 Transparenz gegenüber Datenproduzenten
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Die Vertragspartner erklären sich ausdrücklich damit einverstanden, dass dieser
                        Datenaustauschvertrag den angefragten Datenproduzenten auf dem Datenübertragungsdienst
                        agridata.ch zur Einsichtnahme bereitgestellt werden kann. Diese Offenlegung dient der Förderung
                        der digitalen Selbstbestimmung, der Stärkung des Vertrauens sowie der Gewährleistung von
                        Transparenz über die stattfindenden Datenflüsse.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            4. Gewährleistung / Haftungsausschluss
        </fo:block>

        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Berechtigung zur Datennutzung und -bereitstellung: Unabhängig von ihrem rechtlichen Status
                        sichern die Vertragsparteien mit der digitalen Unterschrift dieses Datenaustauschvertrages zu,
                        dass sie zur Erfüllung ihrer jeweiligen Rolle vollumfänglich berechtigt sind:
                    </fo:block>
                    <fo:list-block>
                        <fo:list-item>
                            <fo:list-item-label end-indent="label-end()">
                                <fo:block>•</fo:block>
                            </fo:list-item-label>
                            <fo:list-item-body start-indent="body-start()">
                                <fo:block>
                                    Der Datenanbieter sichert zu, zur Bereitstellung und Offenlegung der
                                    vertragsgegenständlichen Daten vollumfänglich berechtigt zu sein.
                                </fo:block>
                            </fo:list-item-body>
                        </fo:list-item>
                        <fo:list-item>
                            <fo:list-item-label end-indent="label-end()">
                                <fo:block>•</fo:block>
                            </fo:list-item-label>
                            <fo:list-item-body start-indent="body-start()">
                                <fo:block>
                                    Der Datenbezüger sichert zu, zum Bezug und zur Bearbeitung dieser Daten für den
                                    vereinbarten Nutzungszweck vollumfänglich berechtigt zu sein.
                                </fo:block>
                            </fo:list-item-body>
                        </fo:list-item>
                    </fo:list-block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Produzentendefinition durch den Datenbezüger: Die technische Datenbereitstellung setzt voraus,
                        dass der Datenbezüger die abzufragenden Datenproduzenten über die Systemfunktionen von
                        agridata.ch eindeutig definiert hat.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Bereitstellungsvoraussetzungen und Datenbezug: Der Datenbezüger kann Daten für einen konkreten
                        Datenproduzenten nur beziehen, wenn die Bereitstellungsvoraussetzungen in agridata.ch erfüllt
                        sind (aktive Zustimmung bei zustimmungspflichtigen bzw. gesetzliche Grundlage bei
                        zustimmungsfreien Datenprodukten). Die Überprüfung erfolgt automatisiert durch den
                        Datenübertragungsdienst. Fehlen die Voraussetzungen (z. B. fehlende Zustimmung), besteht kein
                        Anspruch auf Datenbereitstellung, und jegliche Haftung des Datenanbieters sowie des Betreibers
                        von agridata.ch für daraus entstehende Folgen ist ausgeschlossen. Bei bilateralen
                        Bereitstellungswegen (Ziffer 3.1) prüft der Datenanbieter das Vorliegen der Voraussetzungen
                        eigenverantwortlich vor der Datenübergabe.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Datenqualität: Der Datenanbieter übernimmt keine Gewähr für die inhaltliche Richtigkeit,
                        Aktualität und Vollständigkeit der bereitgestellten Daten. Insbesondere garantiert der
                        Datenanbieter nicht, dass Angaben zu sämtlichen Personen bzw. Betrieben bezogen werden können,
                        für welche der Datenbezüger Daten beantragt.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Technische Verfügbarkeit (Best-Effort): Die Bereitstellung der Daten über die Schnittstellen
                        erfolgt ohne Gewährleistung einer ununterbrochenen oder störungsfreien Verfügbarkeit. Die
                        Datenanbieter bemühen sich im Rahmen ihrer technischen Möglichkeiten um einen verlässlichen
                        Betrieb (Best-Effort), garantieren jedoch keine spezifischen Systemlaufzeiten oder Service
                        Levels (SLAs).
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Haftungsausschluss: Der Datenanbieter schliesst, soweit rechtlich zulässig, jegliche
                        Haftungsansprüche aus. Dies gilt insbesondere für Schäden aus einer zeitweisen oder dauerhaften
                        Nichtverfügbarkeit der Datenbereitstellung (z. B. bei Systemunterbrüchen oder technischen
                        Störungen) sowie für Schäden aufgrund mangelhafter Daten.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            5. Kontrolle
        </fo:block>

        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenanbieter ist berechtigt, die Einhaltung der Bestimmungen dieses Datenaustauschvertrages
                        durch den Datenbezüger selbst oder durch einen zur Verschwiegenheit verpflichteten, unabhängigen
                        Dritten überprüfen zu lassen (Audit). Der Datenbezüger verpflichtet sich, dem Datenanbieter auf
                        begründete Anfrage hin die für die Überprüfung der vertragsgemässen Datennutzung erforderlichen
                        Auskünfte zu erteilen und Einsicht in die relevanten Dokumente zu gewähren. Die Kosten der
                        Prüfung trägt der Datenanbieter, es sei denn, es wird ein wesentlicher Verstoss gegen diesen
                        Datenaustauschvertrag festgestellt.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            6. Vertragsdauer
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der vorliegende Datenaustauschvertrag tritt mit der erfolgreichen zweiseitigen digitalen
                        Bestätigung (Authentifizierung via 2FA) durch die zeichnungsberechtigten Personen des
                        Datenanbieters und des Datenbezügers auf dem Datenübertragungsdienst agridata.ch in Kraft.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenaustauschvertrag wird auf unbestimmte Zeit abgeschlossen. Er kann von jeder
                        Vertragspartei unter Einhaltung einer Frist von drei Monaten auf das Ende eines Kalendermonats
                        in Textform (z. B. via E-Mail oder über den Datenübertragungsdienst) gekündigt werden.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenaustauschvertrag ist untrennbar an die Existenz des zugrundeliegenden Datenantrags
                        gekoppelt. Wird der zugehörige Datenantrag auf dem Datenübertragungsdienst agridata.ch gelöscht
                        oder dauerhaft deaktiviert, endet dieser Datenaustauschvertrag automatisch mit dem Zeitpunkt der
                        Löschung oder Deaktivierung, ohne dass es einer separaten Kündigung bedarf (Kopplungsklausel).
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            7. Vertragsänderungen
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Materieller Inhaltsschutz: Der Nutzungszweck, die Kurzbeschreibung sowie die übergeordnete
                        Zielgruppe bilden die wesentliche Informationsbasis für die Datenproduzenten. Sie können nach
                        der Aktivierung des Datenantrags nicht mehr geändert werden. Jegliche Erweiterung oder
                        materielle Anpassung des Nutzungszwecks, der Beschreibung oder der Zielgruppe erfordert das
                        Einreichen eines neuen Datenantrags sowie, soweit erforderlich, das erneute Einholen der
                        Zustimmung der Datenproduzenten.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Redaktionelle Anpassung des Titels: Erweist sich der Titel des Datenantrags im Nachgang als
                        unvollständig, missverständlich oder rein sprachlich korrekturbedürftig, kann dieser
                        redaktionell angepasst werden, sofern der fundamentale Sinngehalt sowie der Nutzungszweck und
                        die Kurzbeschreibung unverändert bleiben.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Begleitdaten: Angaben im Datenantrag, die nicht Teil der zentralen Vertragsinhalte sind, können
                        über die Systemfunktionen des Datenübertragungsdienstes agridata.ch aktualisiert werden. Solche
                        Anpassungen verändern den vorliegenden Datenaustauschvertrag nicht.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Auf Anfrage des Datenbezügers und im gegenseitigen Einvernehmen der Vertragspartner können zu
                        diesem Datenaustauschvertrag über agridata.ch nachträglich neue, erweiterte oder geänderte
                        Datenprodukte hinzugefügt werden, sofern diese inhaltlich dem vereinbarten Nutzungszweck
                        entsprechen. Es besteht kein Anspruch des Datenbezügers auf die Freigabe oder Bereitstellung
                        solcher Anpassungen. Die Erfassung und Freigabe erfolgen rein digital im System und erfordern
                        keine manuelle Vertragsanpassung. Die einmal erteilte Zustimmung der Datenproduzenten gilt auch
                        für diese Anpassungen, sofern es sich um untergeordnete Ergänzungen der bisherigen Datenprodukte
                        handelt und der Betreiber von agridata.ch die betroffenen Datenproduzenten transparent darüber
                        informiert und ihnen eine einfache Möglichkeit zum Widerspruch (Opt-out) bietet. Handelt es sich
                        um neuartige oder wesentlich erweiterte Datenkategorien, ist über die Datenanfrage auf
                        agridata.ch eine erneute aktive Zustimmung (Opt-in) der Datenproduzenten einzuholen.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            8. Datenschutz
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Die Vertragspartner verpflichten sich, Verletzungen der Datensicherheit oder entsprechende
                        begründete Verdachtsmomente unverzüglich gegenseitig zu informieren.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Da der Datenaustausch über agridata.ch abgewickelt wird, ist der betroffene Vertragspartner
                        verpflichtet, gleichzeitig eine Meldung zur Kenntnisnahme an die auf agridata.ch ausgewiesene
                        Supportstelle zu senden. Aus dieser Meldung erwachsen für den Betreiber von agridata.ch keine
                        eigenständigen Pflichten, Haftungen oder Überwachungspflichten; sie dient rein der operativen
                        Information. Die gesetzlichen Meldepflichten (z. B. gegenüber dem EDÖB) verbleiben
                        vollumfänglich bei den Vertragspartnern.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            9. Vertragsverletzung
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Sperrung bei Missbrauch oder Verdacht: Besteht ein begründeter Verdacht auf einen Missbrauch der
                        Daten oder eine sonstige erhebliche Verletzung der Vertragsbestimmungen, ist der Datenanbieter
                        sowie der Betreiber von agridata.ch berechtigt, den Datenfluss über den Datenübertragungsdienst
                        als Schutzmassnahme temporär zu sperren.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Fristlose Kündigung und Ausschluss: Bei schwerwiegenden oder wiederholten Verstössen gegen
                        diesen Datenaustauschvertrag oder die anwendbaren Rechtsvorschriften kann dieser
                        Datenaustauschvertrag von der verletzten Partei fristlos gekündigt werden. In diesem Fall wird
                        der Datenantrag auf agridata.ch dauerhaft deaktiviert und der Datenbezüger vom weiteren
                        Datenbezug bezüglich des betroffenen Datenprodukts ausgeschlossen.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            10. Streitigkeiten aus diesem Datenaustauschvertrag
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Bei allfälligen Meinungsverschiedenheiten bemühen sich die Vertragspartner nach Treu und Glauben
                        um eine möglichst rasche und gütliche Einigung.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Kann innert 30 Arbeitstagen keine Einigung erzielt werden, steht den Vertragspartnern der
                        Rechtsweg offen. Der Gerichtsstand bestimmt sich wie folgt:
                    </fo:block>
                    <fo:list-block>
                        <fo:list-item>
                            <fo:list-item-label end-indent="label-end()">
                                <fo:block>•</fo:block>
                            </fo:list-item-label>
                            <fo:list-item-body start-indent="body-start()">
                                <fo:block>
                                    Handelt es sich bei mindestens einem Vertragspartner um eine Bundesbehörde, ist das
                                    Bundesverwaltungsgericht zuständig.
                                </fo:block>
                            </fo:list-item-body>
                        </fo:list-item>
                        <fo:list-item>
                            <fo:list-item-label end-indent="label-end()">
                                <fo:block>•</fo:block>
                            </fo:list-item-label>
                            <fo:list-item-body start-indent="body-start()">
                                <fo:block>
                                    Handelt es sich bei einem Vertragspartner um eine kantonale Behörde (ohne
                                    Beteiligung des Bundes), liegt der Gerichtsstand am Sitz der kantonalen Behörde.
                                </fo:block>
                            </fo:list-item-body>
                        </fo:list-item>
                        <fo:list-item>
                            <fo:list-item-label end-indent="label-end()">
                                <fo:block>•</fo:block>
                            </fo:list-item-label>
                            <fo:list-item-body start-indent="body-start()">
                                <fo:block>
                                    Handelt es sich ausschliesslich um private Vertragspartner, liegt der Gerichtsstand
                                    am Sitz des Datenanbieters, unter Ausschluss allfälliger kollisionsrechtlicher
                                    Normen (IPRG).
                                </fo:block>
                            </fo:list-item-body>
                        </fo:list-item>
                    </fo:list-block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Sollten sich einzelne Bestimmungen dieses Datenaustauschvertrages als ungültig, unwirksam oder
                        unerfüllbar erweisen, so wird dadurch die Gültigkeit der übrigen Teile des
                        Datenaustauschvertrages nicht beeinträchtigt. Die Vertragspartner verpflichten sich in diesem
                        Fall, die ungültige Bestimmung durch eine rechtskonforme Regelung zu ersetzen, die dem
                        angestrebten fachlichen und rechtlichen Zweck der ursprünglichen Bestimmung am nächsten kommt.
                        Da eine nachträgliche Anpassung dieses Datenaustauschvertrages systemseitig ausgeschlossen ist,
                        ist in diesem Fall der bestehende Vertrag über den Datenübertragungsdienst agridata.ch zu
                        beenden und ein neuer, korrigierter Datenantrag zu stellen. Erfordert die Ersetzung einer
                        ungültigen Bestimmung die Anpassung von zentralen Vertragsinhalten, ist der bestehende Vertrag
                        über den Datenübertragungsdienst agridata.ch zu beenden und ein neuer Datenantrag zu stellen.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Auf diesen Datenaustauschvertrag ist ausschliesslich materielles Schweizer Recht anwendbar.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

    </xsl:template>

</xsl:stylesheet>