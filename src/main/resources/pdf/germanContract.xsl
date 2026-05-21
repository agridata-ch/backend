<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:template name="germanContract">

        <fo:block xsl:use-attribute-sets="document-title">
            Vertrag
        </fo:block>

        <xsl:call-template name="horizontal-rule">
            <xsl:with-param name="space-after">10mm</xsl:with-param>
        </xsl:call-template>

        <fo:block xsl:use-attribute-sets="body-text-spacious">
            zwischen
        </fo:block>

        <fo:block xsl:use-attribute-sets="bold-paragraph">
            <xsl:value-of select="consumerAddressInline"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text-spacious">
            und
        </fo:block>

        <fo:block xsl:use-attribute-sets="bold-paragraph">
            <xsl:value-of select="providerAddressInline"/>
        </fo:block>

        <xsl:call-template name="horizontal-rule"/>

        <fo:block xsl:use-attribute-sets="section-heading">
            Rechtsgrundlage für Datenbereitstellung
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text-justify">
            <fo:inline font-weight="bold">
                <xsl:value-of select="consumerName"/>
            </fo:inline>
            (hiernach: Datenanbieter) und
            <fo:inline font-weight="bold">
                <xsl:value-of select="providerName"/>
            </fo:inline>
            (hiernach: Datenbezüger) vereinbaren gestützt auf Art. 165c ff. des Bundesgesetzes vom 29. April
            1998 über die Landwirtschaft (SR. 910.1), Art. 36 des Bundesgesetzes vom 25. September 2020 über
            den Datenschutz (SR 235.1) und Art. 27 Abs. 9 der Verordnung vom 23. Oktober 2013 über
            Informationssysteme im Bereich der Landwirtschaft (SR 919.117.71), was folgt:
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Vertragsgegenstand
        </fo:block>
        <fo:block>
            Der vorliegende Vertrag regelt die Bereitstellung von Daten vom Datenanbieter an den
            Datenbezüger.
            Er legt die diesbezüglichen Rechte und Pflichten beider Vertragspartner fest.
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            <fo:inline font-weight="bold">Nicht</fo:inline>
            Gegenstand dieses Vertrags sind mögliche Vergütungen zwischen den Vertragspartnern für
            z.B. Datenaufbereitung oder Datennutzung.
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Dieser Vertrag wird in deutscher und französischer Sprache ausgefertigt. Im Falle von
            Widersprüchen oder Auslegungszweifeln gilt ausschliesslich die deutsche Version.
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            1. Definition und Erklärung von Begriffen
        </fo:block>

        <fo:list-block provisional-distance-between-starts="5mm"
                       margin-left="10mm"
                       font-family="Helvetica"
                       font-size="10pt"
                       line-height="15pt">

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">agridata.ch</fo:inline>:
                        Die Vertragspartner nutzen für die Datenübertragung und die Abfrage der
                        Zustimmung der Datenproduzenten zur Datenweitergabe den Datenübertragungsdienst
                        agridata.ch. Die für die Nutzung von agridata.ch geltenden Allgemeinen
                        Geschäftsbedingungen (AGB) sind von den Vertragspartnern unabhängig vom vorliegenden
                        Vertrag einzuhalten.
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
                        Als Datenproduzenten gelten natürliche oder juristische Personen, die im
                        Rahmen ihrer Tätigkeit Daten erzeugen, erfassen oder bereitstellen, welche über
                        agridata.ch übermittelt werden können. Dazu gehören insbesondere, aber nicht
                        abschliessend, Bewirtschafterinnen und Bewirtschafter, Tierhalterinnen und
                        Tierhalter, Eigentümerinnen und Eigentümer von Equiden sowie Unternehmen,
                        Organisationen oder Institutionen, die landwirtschaftsrelevante Daten erzeugen oder
                        verwalten.
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
                        Als Datenbezüger gelten natürliche oder juristische Personen, oder Behörden,
                        die über agridata.ch auf die Daten der Datenproduzenten zugreifen oder entsprechende
                        Datenanfragen stellen, um diese ausschliesslich zu den im Datenantrag festgelegten
                        Zwecken zu verwenden. Der Zugriff auf Daten ist nur möglich, wenn die Zustimmung der jeweiligen
                        Datenproduzenten vorliegt.
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
                        Stellt dem Datenbezüger die von ihm beantragten Daten der zustimmenden
                        Datenproduzenten über agridata.ch bereit.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Datenantrag / Datenanfrage</fo:inline>:
                        Antrag des Datenbezügers, um definierte Datenprodukte zu
                        einem angegebenen Zweck im Sinne des Art. 27 Abs. 9 ISLV vom Datenanbieter zu
                        erhalten.
                        Datenproduzenten erhalten im Datenübertragungsdienst agridata.ch Einsicht in den
                        Datenantrag und können dort ihre Zustimmung zur Datenweitergabe vom Datenanbieter an
                        den Datenbezüger erteilen.
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
                        Datenanbieter stellen Datenprodukte mit klar umrissenen Inhalten für den
                        Datenbezüger zur Auswahl. Ein Datenbezüger kann mehrere Datenprodukte eines
                        Datenanbieters beantragen.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Zustimmungsabfrage</fo:inline>:
                        Dienst in agridata.ch, mit dem Datenproduzenten bestehende
                        Datenanträge einsehen und ihre Zustimmung zur Datenweitergabe bearbeiten können.
                        Datenproduzenten können pro Datenanfrage der Datenweitergabe zustimmen oder diese
                        ablehnen.
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
                        Geschützte technische Schnittstelle mit der Datenbezüger pro Datenantrag
                        alle Daten der zustimmenden Datenproduzenten beziehen können.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            2. Angaben zum Datenantrag
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
            Kontakt: <xsl:value-of select="consumerPhoneNumber"/>,
            <xsl:value-of select="consumerEmailAddress"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Name Datenantrag:
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            DE:
            <xsl:value-of select="requestTitle/de"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            FR:
            <xsl:value-of select="requestTitle/fr"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            IT:
            <xsl:value-of select="requestTitle/it"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Beschreibung des Datenantrags:
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            DE:
            <xsl:value-of select="requestDescription/de"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            FR:
            <xsl:value-of select="requestDescription/fr"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            IT:
            <xsl:value-of select="requestDescription/it"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Zweck der Datennutzung durch den Datenbezüger:
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            DE:
            <xsl:value-of select="requestPurpose/de"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            FR :
            <xsl:value-of select="requestPurpose/fr"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            IT:
            <xsl:value-of select="requestPurpose/it"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Name System beim Datenanbieter:
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            DE:
            <xsl:value-of select="providerSystemName/de"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            FR :
            <xsl:value-of select="providerSystemName/fr"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            IT:
            <xsl:value-of select="providerSystemName/it"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Gewünschte Datenprodukte:
        </fo:block>
        <fo:list-block>
            <xsl:for-each select="products">
                <fo:list-item>
                    <fo:list-item-label end-indent="label-end()">
                        <fo:block>•</fo:block>
                    </fo:list-item-label>

                    <fo:list-item-body start-indent="body-start()">
                        <fo:block>
                            <xsl:value-of select="de"/>
                        </fo:block>
                    </fo:list-item-body>
                </fo:list-item>
            </xsl:for-each>
        </fo:list-block>

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
            3.1 AGB für die Nutzung von agridata.ch
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Die für die Nutzung von agridata.ch geltenden Allgemeinen Geschäftsbedingungen (AGB)
                        sind unabhängig von diesem vorliegenden Vertrag von den Vertragspartnern
                        einzuhalten.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3.2 Datenanbieter
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenanbieter stellt über agridata.ch die im Datenantrag beschriebenen
                        Datenprodukte zur Verfügung. Dabei stellt agridata.ch sicher, dass nur Daten vom
                        Datenanbieter an den Datenbezüger weitergegeben werden, bei denen die Zustimmung der
                        Datenproduzenten vorliegt.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenanbieter verpflichtet sich, auf eigene Kosten eine geeignete technische
                        Schnittstelle zu agridata.ch einzurichten und zu unterhalten. Diese Schnittstelle
                        muss den sicheren, vollständigen und automatisierten Datenaustausch gemäss
                        Spezifikationen
                        <fo:basic-link
                                external-destination="url('https://github.com/agridata-ch/.github/wiki')"
                                color="blue"
                                text-decoration="underline">
                            https://github.com/agridata-ch/.github/wiki
                        </fo:basic-link>
                        gewährleisten.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenanbieter stellt dem Datenbezüger die Daten über eine geschützte
                        Schnittstelle zum selbständigen Bezug bereit. Der Zugriff auf die Daten muss sowohl
                        beim Datenanbieter als auch beim Datenbezüger beschränkt, kontrolliert und geschützt
                        sein.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Wenn bisher bereitgestellte Datenprodukte nicht mehr angeboten oder geändert werden,
                        informiert der Datenanbieter agridata.ch und den Datenbezüger.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Die für die Abwicklung der Datenbereitstellung beim Datenanbieter allenfalls
                        entstehenden Kosten (z.B. für neue oder angepasste IT-Infrastruktur) können nicht
                        dem Datenbezüger oder agridata.ch in Rechnung gestellt werden.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3.3 Datenbezüger
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Die gelieferten Daten werden ausschliesslich für den Zweck bearbeitet, welcher den
                        zustimmenden Datenproduzenten und dem Datenanbieter angegeben wurde.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenbezüger verpflichtet sich, dem Datenproduzenten eine beabsichtigte
                        Weitergabe von Daten an Dritte in der Beschreibung des Nutzungszwecks mitzuteilen.
                        Davon nicht berührt ist eine allfällige Herausgabe an Behörden, welche gestützt auf
                        eine gesetzliche Grundlage erfolgt.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenbezüger verpflichtet sich, dem Datenproduzenten mitzuteilen, sowohl wenn
                        Datenbezüger als auch wenn Dritte die Daten im Ausland beziehen.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Zulässig ist der Beizug von Auftragsbearbeitern durch Datenbezüger (in seinem
                        Auftrag und seinem Interesse).
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenbezüger ist für den Datenschutz verantwortlich und verpflichtet sich die
                        Datenschutzgesetzgebung, inklusive einer allfälligen Einholung der Zustimmung zur
                        Datenweitergabe bei den Datenproduzenten, einzuhalten. Der Zugriff auf die Daten
                        muss sowohl beim Datenanbieter als auch beim Datenbezüger beschränkt, kontrolliert
                        und geschützt sein.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Die Datenbezüger sind selbst dafür verantwortlich, ihre jeweiligen Datenproduzenten
                        über die entsprechenden Datenanfragen zu informieren und deren Zustimmung einzuholen
                        bzw. sicherzustellen, dass diese Zustimmung vorliegt.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenbezüger nutzt für die aktive Kommunikation des Datenantrags / der
                        Datenanfrage gegenüber den Datenproduzenten Vorlagen von agridata.ch, um gegenüber
                        den Datenproduzenten eine durchgängige Kommunikation im Zusammenhang mit agridata.ch
                        einzuhalten.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenbezüger darf den Link auf die Datenanfrage nur an solche Datenproduzenten
                        versenden, die unter den Angaben zum Vertrag als Zielgruppe beschrieben wurden.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Sofern der Datenbezüger in der Lage ist, in agridata.ch eine Liste mit den
                        UID-Nummern der betroffenen Datenproduzenten zur Verfügung zu stellen, kann die
                        Datenanfrage auf agridata.ch für die entsprechenden Datenproduzenten aktiviert
                        werden, und es können Informations-E-Mails an die betreffenden Datenproduzenten
                        versendet werden.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Die für die Abwicklung des Datenbezugs beim Datenbezüger allenfalls entstehenden
                        Kosten (z.B. für neue oder angepasste IT-Infrastruktur) können nicht dem
                        Datenanbieter oder agridata.ch in Rechnung gestellt werden.
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
                        Der Datenanbieter und agridata.ch übernehmen keine Gewähr für die inhaltliche
                        Richtigkeit und Vollständigkeit der gelieferten Daten. Insbesondere garantieren
                        Datenanbieter und agridata.ch nicht, dass Angaben zu sämtlichen Personen bzw.
                        Betrieben geliefert werden können, für welche der Datenbezüger die Datenweitergabe
                        beantragt.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Datenanbieter und agridata.ch schliessen, soweit rechtlich zulässig, jegliche
                        Haftungsansprüche, insbesondere wegen nicht funktionierendem Datenbezug oder
                        mangelhaften Daten, aus.
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
                        Sowohl der Eidgenössischen Finanzkontrolle als auch agridata.ch steht jederzeit ein
                        Kontroll- und ein Auskunftsrecht über alle Teile des Vertrages zu; sie können diese
                        Rechte auch durch ausserhalb der Bundesverwaltung stehende Sachverständige
                        wahrnehmen lassen.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Die Vertragspartnerin hat den Kontrollorganen jederzeit Einsicht in sämtliche Akten
                        und Zutritt zu den Anlagen, die Gegenstand des vorliegenden Vertragsverhältnisses
                        sind, zu gewähren sowie für Auskünfte zur Verfügung zu stehen.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Die Kontrollorgane sind an das Amtsgeheimnis gebunden und haben bei der Bearbeitung
                        von Personendaten die Datenschutzvorschriften zu beachten.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            6. Vertragsdatuer
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der vorliegende Vertrag tritt ab der beidseitigen Unterzeichnung durch die
                        Vertragspartner in Kraft. Ab diesem Zeitpunkt können Datenproduzenten ihre
                        Zustimmung zur Datenweitergabe bearbeiten.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Vertrag gilt bis zur schriftlichen Beendigung durch einen Vertragspartner.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            7. Vertragsänderung
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Änderungen am Vertrag können nach Vertragsabschluss nicht vorgenommen werden, da
                        diese
                        sich auf alle bis zur Änderung erteilten Zustimmungen zur Datenweitergabe auswirken.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Der Vertrag kann durch beide Vertragspartner beendet werden.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Eine schriftliche Beendigung ist mit einer Frist von drei Monaten auf ein Monatsende
                        möglich.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            8. Datenschutz / Geheimhaltungspflicht
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Die Vertragspartner verpflichten sich, Daten der Datenproduzenten gemäss den
                        geltenden
                        Datenschutzbestimmungen zu schützen, insbesondere ergreifen sie technische und
                        organisatorische Massnahmen für eine angemessene Datensicherheit in ihrer Sphäre der
                        Verantwortung und Zuständigkeit.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Die Vertragspartner gewährleisten – auch nach Beendigung des vorliegenden
                        Vertragsverhältnisses – den vollen Daten- und Geheimnisschutz für alle Daten
                        (Informationen), die ihnen im Rahmen des vorliegenden Vertrags zur Kenntnis gelangen
                        oder
                        von ihnen in irgendeiner Weise bearbeitet werden. Sie treffen hierzu alle
                        erforderlichen
                        organisatorischen, technischen und personellen Schutzmassnahmen.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Die Vertragspartner garantieren insbesondere, dass alle ihnen zur Kenntnis
                        gelangenden
                        Daten ausschliesslich für die vertragliche vereinbarte Zweckbestimmung bearbeitet
                        und
                        verwendet werden und stellen sicher, dass sämtliche Mitarbeitenden und
                        (gegebenenfalls)
                        externe Dienstleister (und deren Mitarbeitenden) die einschlägigen Geheimhaltungs-
                        und
                        Datenschutzvorschriften ebenfalls strikte einhalten.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Die Vertragspartner informieren sich gegenseitig bei datenschutzrelevanten Vorfällen
                        oder
                        begründetem Verdacht unverzüglich.
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
                        In Fällen des Missbrauchs der Daten oder anderen Verletzungen der Bestimmungen
                        dieses
                        Vertrages kann der vorliegende Vertrag von den Vertragspartnern fristlos gekündigt
                        werden. Kündigt ein Vertragspartner den Vertrag fristlos, so wird der bestehende
                        Datenantrag der
                        Vertragspartnerin deaktiviert und es werden fortan keine der beantragten Daten
                        bereitgestellt.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            10. Streitigkeiten aus diesem Vertrag
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Bei allfälligen Meinungsverschiedenheiten bemühen sich die Vertragspartner nach Treu
                        und
                        Glauben um eine möglichst rasche und gütliche Einigung.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Kann innert 30 Arbeitstagen weder die Meinungsdifferenz bereinigt noch ein
                        Bereinigungsplan
                        vereinbart werden, ist jeder Vertragspartner berechtigt, die Angelegenheit bei
                        Gericht
                        anhängig zu machen.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Über Streitigkeiten aus diesem Vertrag entscheidet auf Klage hin das
                        Bundesverwaltungsgericht als erste Instanz.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Sollten sich einzelne Bestimmungen dieses Vertrages als ungültig, unwirksam oder
                        unerfüllbar erweisen, so wird dadurch die Gültigkeit, Wirksamkeit und Erfüllbarkeit
                        der übrigen
                        Teile des Vertrages nicht beeinträchtigt. Die Vertragspartner verpflichten sich in
                        diesem Fall,
                        den ungültigen, unwirksamen oder unerfüllbaren Teil des Vertrages durch eine
                        gültige,
                        wirksame und erfüllbare Bestimmung zu ersetzen, die inhaltlich der ursprünglichen
                        Absicht
                        der Vertragspartner am nächsten kommt.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

    </xsl:template>

</xsl:stylesheet>