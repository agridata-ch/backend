<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:template name="frenchContract">

        <fo:block xsl:use-attribute-sets="document-title" break-before="page">
            Contrat relatif à l’échange de données agridata.ch
        </fo:block>

        <xsl:call-template name="horizontal-rule">
            <xsl:with-param name="space-after">10mm</xsl:with-param>
        </xsl:call-template>

        <fo:block xsl:use-attribute-sets="body-text-spacious">
            entre
        </fo:block>

        <fo:block xsl:use-attribute-sets="bold-paragraph">
            <xsl:value-of select="consumerAddressInline"/> (ci-après « fournisseur de données »)
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text-spacious">
            et
        </fo:block>

        <fo:block xsl:use-attribute-sets="bold-paragraph">
            <xsl:value-of select="providerAddressInline"/> (ci-après « acquéreur de données »)
        </fo:block>

        <fo:block xsl:use-attribute-sets="bold-paragraph">
            (ci-après nommés conjointement « partenaires contractuels »)
        </fo:block>

        <xsl:call-template name="horizontal-rule"/>

        <fo:block xsl:use-attribute-sets="section-heading">
            Bases juridiques pour la transmission des données
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text-justify">
            <fo:list-block>
                <fo:list-item>
                    <fo:list-item-label end-indent="label-end()">
                        <fo:block>1.</fo:block>
                    </fo:list-item-label>
                    <fo:list-item-body start-indent="body-start()">
                        <fo:block>
                            Utilisation du service de transfert de données : La mise à disposition et l’utilisation du
                            service technique agridata.ch s’effectuent sur la base des art. 165c ss LwG et de l’art. 27,
                            al. 9, ISLV.
                        </fo:block>
                    </fo:list-item-body>
                </fo:list-item>
                <fo:list-item>
                    <fo:list-item-label end-indent="label-end()">
                        <fo:block>2.</fo:block>
                    </fo:list-item-label>
                    <fo:list-item-body start-indent="body-start()">
                        <fo:block>
                            <xsl:value-of select="providerSystemLegalBasis/fr"/>
                        </fo:block>
                    </fo:list-item-body>
                </fo:list-item>
                <fo:list-item>
                    <fo:list-item-label end-indent="label-end()">
                        <fo:block>3.</fo:block>
                    </fo:list-item-label>
                    <fo:list-item-body start-indent="body-start()">
                        <fo:block>
                            Si le siège ou le domicile de l’acquéreur de données se trouve hors de Suisse, celui-ci
                            s’engage à respecter les dispositions de la loi suisse sur la protection des données (LPD).
                            Si l’État du siège de l’acquéreur de données ne dispose pas d’un niveau de protection des
                            données reconnu comme adéquat par le Conseil fédéral suisse, l’acquéreur de données garantit
                            la protection des données transmises par la reprise des clauses contractuelles types (SCC)
                            ou de garanties équivalentes reconnues par le PFPDT (conformément à l’art. 16, al. 2, LPD).
                        </fo:block>
                    </fo:list-item-body>
                </fo:list-item>
            </fo:list-block>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Objet du contrat
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            Le présent contrat relatif à l’échange de données règle la mise à disposition de données par le fournisseur
            de données en vue de leur extraction par l’acquéreur de données via le service de transfert de données
            agridata.ch. Il définit les droits et obligations des partenaires contractuels à cet égard.
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Le présent contrat relatif à l’échange de données est établi en langues allemande et française. En cas de
            contradiction ou de doute sur l’interprétation, seule la version allemande fait foi.
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            1. Définition et explication des termes
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
                        <fo:inline text-decoration="underline">agridata.ch</fo:inline>
                        :
                        Les partenaires contractuels utilisent le service de transfert de données agridata.ch pour la
                        transmission des données. Celui-ci sert à informer les producteurs de données ainsi que, pour
                        les produits de données soumis à consentement, à recueillir leur consentement. L’utilisation
                        d’agridata.ch s’effectue sur une base volontaire pour les partenaires contractuels, et les
                        conditions générales (CG) applicables doivent être respectées par les partenaires contractuels
                        indépendamment du présent contrat relatif à l’échange de données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Producteur de données</fo:inline>
                        :
                        Sont considérés comme producteurs de données les personnes physiques ou morales qui, dans le
                        cadre de leur activité, produisent, saisissent ou mettent à disposition des données pouvant être
                        transmises via agridata.ch. Il s’agit notamment, mais pas exclusivement, des exploitants, des
                        détenteurs d’animaux, des propriétaires d’équidés ainsi que des entreprises, organisations ou
                        institutions qui produisent ou gèrent des données relatives à l’agriculture.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Acquéreur de données</fo:inline>
                        :
                        Sont considérés comme acquéreurs de données les personnes morales, les autorités ou les
                        personnes physiques dans l’exercice de leur activité professionnelle ou commerciale (p. ex.
                        entreprises individuelles) qui accèdent aux données des producteurs de données via agridata.ch
                        ou qui soumettent des demandes de données correspondantes, afin de les utiliser exclusivement
                        aux fins définies dans la demande de données. L’accès aux données n’est possible que si les
                        conditions de mise à disposition selon le présent contrat sont remplies.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Fournisseur de données</fo:inline>
                        :
                        Sont considérés comme fournisseurs de données les personnes physiques ou morales, ou les
                        autorités, qui mettent à disposition d’un acquéreur de données, via agridata.ch, les données
                        demandées par ce dernier. Le fournisseur de données décide en toute indépendance, dans le
                        respect des dispositions légales qui lui sont applicables, si et à quel acquéreur de données il
                        met ses données à disposition.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Produit de données</fo:inline>
                        :
                        Les fournisseurs de données proposent un choix de produits de données dont le contenu et le
                        format sont clairement définis à l’intention des acquéreurs de données. Un acquéreur de données
                        peut demander plusieurs produits de données d’un même fournisseur de données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Demande de données</fo:inline>
                        :
                        Requête soumise par un acquéreur de données via le service de transfert de données agridata.ch
                        au fournisseur de données afin d'obtenir des produits de données définis pour un usage précis.
                        La demande de données approuvée constitue la base du présent contrat relatif à l’échange de
                        données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Demande aux producteurs (Datenanfrage)</fo:inline>
                        :
                        L’information figurant sur agridata.ch et adressée aux différents producteurs de données, basée
                        sur une demande de données approuvée. Elle sert à la transparence et permet aux producteurs de
                        données de prendre connaissance du but de l’utilisation des données et, pour les produits de
                        données soumis à consentement, d’accorder ou de refuser leur consentement à la transmission des
                        données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Vérification du consentement</fo:inline>
                        :
                        Fonction système automatisée du service de transfert de données agridata.ch qui vérifie et
                        garantit, avant chaque transmission de données au niveau de l’interface, que les conditions
                        légales de mise à disposition (pour les produits de données soumis à consentement, l’existence
                        d’un consentement actif et valable du producteur de données concerné) sont remplies.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Interface de données</fo:inline>
                        :
                        Interface technique sécurisée connectant l’acquéreur de données ou le fournisseur de données à
                        agridata.ch et permettant d’extraire, pour chaque demande de données, les données des
                        producteurs de données définis, dans le respect des conditions de mise à disposition
                        respectives.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            2. Indications sur la demande de données
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Les indications ci-après sont basées sur la demande de données soumise par l’acquéreur de données sur
            agridata.ch. Elles sont affichées aux producteurs de données dans la demande adressée aux producteurs et
            servent de base de décision pour leur consentement:
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Acquéreur de données:
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

        <fo:block>Contact:
            <xsl:value-of select="consumerPhoneNumber"/>,
            <xsl:value-of select="consumerEmailAddress"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Nom de la demande de données:
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Le titre défini dans la demande du fournisseur de données
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Description de la demande de données:
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Allemand:
            <xsl:value-of select="requestDescription/de"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            Français:
            <xsl:value-of select="requestDescription/fr"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            Italien:
            <xsl:value-of select="requestDescription/it"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            But de l’utilisation des données par l’acquéreur de données:
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Allemand:
            <xsl:value-of select="requestPurpose/de"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            Français:
            <xsl:value-of select="requestPurpose/fr"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            Italien:
            <xsl:value-of select="requestPurpose/it"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Système du fournisseur de données:
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            Le système source du fournisseur de données défini dans la demande
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Produits de données souhaités:
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Faisant l’objet du présent contrat relatif à l’échange de données sont les produits de données figurant et
            validés dans la demande électronique de données sur agridata.ch sous l’ID de demande
            <xsl:value-of select="requestHumanFriendlyId"/> (Datenanbieter).
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Indication du groupe cible:
        </fo:block>
        <fo:block>
            <xsl:value-of select="targetGroup"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3. Obligations des partenaires contractuels
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3.1 Fournisseur de données
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le fournisseur de données spécifie et décrit les produits de données qu’il met à disposition de
                        manière contraignante quant au contenu et au format. La description des produits de données du
                        fournisseur de données doit être accessible publiquement et consultable via agridata.ch.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Pour la mise à disposition de produits de données, le fournisseur de données utilise le pilotage
                        automatisé d’agridata.ch, qui garantit que la transmission des produits de données est
                        strictement liée à l’existence d'un consentement actif du producteur de données lors de la
                        vérification du consentement.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le fournisseur de données met les produits de données à disposition directement auprès du
                        service de transfert de données agridata.ch via l’interface sécurisée. Il s’assure de ne
                        transmettre pour sa part que des produits de données conformes à la protection des données et
                        demandés par le service de transfert de données agridata.ch. La sécurisation ultérieure de
                        l’extraction et la mise à disposition pour l’acquéreur de données sont effectuées par
                        agridata.ch conformément aux conditions de mise à disposition applicables (pour les produits de
                        données soumis à consentement, l’existence d’un consentement actif et valable du producteur de
                        données ; pour les produits de données non soumis à consentement, l’existence de la base légale
                        et la définition des producteurs). Les voies de mise à disposition bilatérales dérogeantes (p.
                        ex. la remise directe des données par le fournisseur via l'interface graphique GUI de
                        l'application, par e-mail ou par export manuel de fichiers) restent réservées. Dans ces cas, il
                        incombe à la seule responsabilité du fournisseur de données de vérifier de manière autonome et
                        de s’assurer, avant toute livraison de données, que les conditions de mise à disposition
                        correspondantes (consentement actif ou affichage de transparence légal) sont remplies dans
                        agridata.ch. La prestation d’agridata.ch se limite ici à la fourniture des informations de
                        légitimation ; la transmission technique et la sécurité des données du canal de transport choisi
                        incombent entièrement au fournisseur de données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le fournisseur de données met à disposition et entretient à ses frais une interface technique
                        appropriée avec agridata.ch. Il porte la seule responsabilité de la mise en œuvre conforme au
                        fonctionnement et aux spécifications de l’interface sur ses propres systèmes. L’échange sécurisé
                        et automatisé de produits de données ainsi que les délais et obligations d’information lors des
                        adaptations d’interface sont régis par les conditions d'utilisation (CG) applicables aux
                        fournisseurs de données d’agridata.ch.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le fournisseur de données informe au préalable et en temps utile de la suppression ou de la
                        modification substantielle de produits de données via le service de transfert de données
                        agridata.ch. L’information des acquéreurs de données concernés s’effectue via agridata.ch. Le
                        risque et la responsabilité de l’adaptation en temps utile de ses propres systèmes aux
                        modifications de produits (obligation de recherche) incombent à l’acquéreur de données. Les
                        détails concernant les délais de préavis, les phases de transition ainsi que la mise hors
                        service de produits de données sont régis par les conditions d’utilisation (CG).
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Les coûts découlant de l’installation et de l’adaptation de la mise à disposition des données
                        chez le fournisseur de données (p. ex. pour une infrastructure informatique nouvelle ou adaptée
                        ou des adaptations de l’interface) ne peuvent pas être facturés à l’acquéreur de données ni à
                        agridata.ch.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3.2 Acquéreur de données
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        L’obtention des consentements requis auprès des producteurs de données est la tâche et la
                        responsabilité exclusives de l'acquéreur de données. Le fournisseur de données n’a aucune
                        obligation d’information, d’explication ou d'activation des producteurs de données. Le
                        fournisseur de données ne répond pas de l’existence ou de la validité des consentements ; le
                        contrôle technique et la sécurisation des consentements avant la mise à disposition des données
                        sont entièrement automatisés par agridata.ch.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        L’acquéreur de données s’engage à adresser des demandes aux producteurs exclusivement aux
                        producteurs de données définis comme groupe cible dans le présent contrat relatif à l’échange de
                        données. Tout contact indifférencié ou contraire au but visé est interdit.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        L’acquéreur de données supporte l’ensemble des coûts qui lui incombent en lien avec
                        l’installation, l’adaptation, l’utilisation ou la maintenance de l’acquisition des données (p.
                        ex. pour sa propre infrastructure informatique, les adaptations d’interface ou les logiciels
                        tiers). Toute répercussion de coûts ou facturation au fournisseur de données ou à agridata.ch
                        est exclue.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3.3 Transparence envers les producteurs de données
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Les partenaires contractuels déclarent expressément accepter que le présent contrat relatif à l’échange de
            données puisse être mis à la disposition des producteurs de données sollicités sur le service de transfert
            de données agridata.ch pour consultation. Cette divulgation sert à promouvoir l’autodétermination numérique,
            à renforcer la confiance et à garantir la transparence des flux de données qui ont lieu.
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            4. Garantie / Exclusion de responsabilité
        </fo:block>

        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Droit d'utilisation et de mise à disposition des données: Indépendamment de leur statut
                        juridique, les parties contractantes garantissent par la signature numérique du présent contrat
                        relatif à l’échange de données qu’elles sont pleinement habilitées à remplir leur rôle
                        respectif:
                        <fo:list-block>
                            <fo:list-item>
                                <fo:list-item-label end-indent="label-end()">
                                    <fo:block>•</fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()">
                                    <fo:block>
                                        Le fournisseur de données garantit être pleinement habilité à la mise à
                                        disposition et à la divulgation des données faisant l’objet du contrat.
                                    </fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                            <fo:list-item>
                                <fo:list-item-label end-indent="label-end()">
                                    <fo:block>•</fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()">
                                    <fo:block>
                                        L’acquéreur de données garantit être pleinement habilité à l’extraction et au
                                        traitement de ces données pour le but d’utilisation convenu.
                                    </fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                        </fo:list-block>
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Définition des producteurs par l'acquéreur de données: La mise à disposition technique des
                        données suppose que l’acquéreur de données a défini de manière univoque les producteurs de
                        données à interroger via les fonctions système d’agridata.ch.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Conditions de mise à disposition et extraction des données: L’acquéreur de données ne peut
                        obtenir des données pour un producteur de données concret que si les conditions de mise à
                        disposition dans agridata.ch sont remplies (consentement actif pour les produits de données
                        soumis à consentement ou base légale pour les produits de données non soumis à consentement). La
                        vérification est effectuée de manière automatisée par le service de transfert de données. Si les
                        conditions font défaut (p. ex. absence de consentement), il n’existe aucun droit à la mise à
                        disposition des données, et toute responsabilité du fournisseur de données ainsi que de
                        l’exploitant d’agridata.ch pour les conséquences qui en découlent est exclue. Dans le cas de
                        voies de mise à disposition bilatérales (chiffre 3.1), le fournisseur de données vérifie sous sa
                        propre responsabilité l’existence des conditions préalables avant la remise des données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Disponibilité technique (Best-Effort): La mise à disposition des données via les interfaces
                        s’effectue sans garantie d’une disponibilité ininterrompue ou exempt de dysfonctionnements. Les
                        fournisseurs de données s’efforcent, dans la mesure de leurs possibilités techniques, d’assurer
                        un fonctionnement fiable (Best-Effort), mais ne garantissent aucun temps de fonctionnement
                        spécifique du système ni aucun niveau de service (SLA).
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Exclusion de responsabilité: Le fournisseur de données exclut toute prétention en responsabilité
                        dans la mesure où la loi le permet. Cela s’applique en particulier aux dommages résultant d’une
                        indisponibilité temporaire ou permanente de la mise à disposition des données (p. ex. en cas
                        d’interruptions du système ou de pannes techniques) ainsi qu’aux dommages dus à des données
                        défectueuses.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            5. Contrôle
        </fo:block>

        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le fournisseur de données est en droit de faire vérifier le respect des dispositions du présent
                        contrat relatif à l’échange de données par l’acquéreur de données, soit lui-même, soit par un
                        tiers indépendant soumis au secret professionnel (audit). L’acquéreur de données s’engage à
                        fournir au fournisseur de données, sur demande motivée, les renseignements nécessaires à la
                        vérification de l’utilisation conforme des données et à lui accorder l’accès aux documents
                        pertinents. Les coûts du contrôle sont supportés par le fournisseur de données, à moins qu'une
                        violation substantielle du présent contrat relatif à l’échange de données ne soit constatée.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            6. Durée du contrat
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le présent contrat relatif à l’échange de données entre en vigueur avec la confirmation
                        numérique bilatérale réussie (authentification via 2FA) par les personnes habilitées à signer du
                        fournisseur de données et de l’acquéreur de données sur le service de transfert de données
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
                        Le contrat relatif à l’échange de données est conclu pour une durée indéterminée. Il peut être
                        résilié par chaque partie contractante moyennant un préavis de trois mois pour la fin d’un mois
                        civil, sous forme textuelle (p. ex. par e-mail ou via le service de transfert de données).
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le contrat relatif à l’échange de données est indissociablement lié à l’existence de la demande
                        de données sous-jacente. Si la demande de données correspondante est supprimée ou désactivée de
                        manière permanente sur le service de transfert de données agridata.ch, le présent contrat
                        relatif à l’échange de données prend fin automatiquement au moment de la suppression ou de la
                        désactivation, sans qu’une résiliation séparée ne soit nécessaire (clause de couplage).
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            7. Modifications du contrat
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Contenus essentiels &amp; Validité: La modification ultérieure des contenus essentiels du
                        présent contrat relatif à l’échange de données – en particulier la description, le but
                        d’utilisation ainsi que le groupe cible de la demande de données – est en principe exclue. Toute
                        adaptation substantielle de ces éléments clés exige impérativement la soumission et la
                        validation d’une nouvelle demande de données, afin de préserver la base légale ainsi que la
                        transparence envers les producteurs de données et, si nécessaire, d’obtenir à nouveau leur
                        consentement.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Adaptation rédactionnelle du titre: S’il s’avère ultérieurement que le titre de la demande de
                        données est incomplet, prêt à confusion ou nécessite une simple correction linguistique, il peut
                        être adapté sur le plan rédactionnel, pour autant que le sens fondamental, le but d’utilisation
                        et la description succincte restent inchangés.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Données d'accompagnement: Les indications figurant dans la demande de données qui ne font pas
                        partie des contenus essentiels du contrat (telles que les personnes de contact ou les avantages)
                        peuvent être mises à jour via les fonctions système du service de transfert de données
                        agridata.ch. De telles adaptations ne modifient pas le présent contrat relatif à l’échange de
                        données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        À la demande de l’acquéreur de données et d’un commun accord entre les partenaires contractuels,
                        des produits de données nouveaux, étendus ou modifiés peuvent être ajoutés ultérieurement au
                        présent contrat relatif à l’échange de données via agridata.ch, pour autant qu’ils correspondent
                        quant au fond au but d'utilisation convenu. L’acquéreur de données ne peut prétendre à la
                        validation ou à la mise à disposition de telles adaptations. La saisie et la validation
                        s’effectuent de manière purement numérique dans le système et ne nécessitent aucune adaptation
                        contractuelle manuelle. Le consentement une fois accordé par les producteurs de données
                        s'applique également à ces adaptations, pour autant qu’il s’agisse de compléments secondaires
                        aux produits de données précédents et que l’exploitant d’agridata.ch en informe de manière
                        transparente les producteurs de données concernés et leur offre une possibilité simple
                        d’opposition (Opt-out). S’il s’agit de catégories de données nouvelles ou considérablement
                        étendues, un nouveau consentement actif (Opt-in) des producteurs de données doit être recueilli
                        via la demande adressée aux producteurs sur agridata.ch.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Protection du contenu matériel: Le but d’utilisation, la description succincte ainsi que le
                        groupe cible prépondérant constituent la base d’information essentielle pour les producteurs de
                        données. Ils ne peuvent plus être modifiés après l’activation de la demande de données. Toute
                        extension ou modification matérielle du but d’utilisation, de la description ou du groupe cible
                        exige le dépôt d’une nouvelle demande de données ainsi que, si nécessaire, l’obtention d’un
                        nouveau consentement des producteurs de données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Ajustement rédactionnel du titre: S’il s’avère par la suite que le titre de la demande de
                        données est incomplet, équivoque ou nécessite une simple correction linguistique, celui-ci peut
                        être ajusté sur le plan rédactionnel, pour autant que la substance fondamentale ainsi que le but
                        d’utilisation et la description succincte restent inchangés.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Données d’accompagnement: Les indications figurant dans la demande de données qui ne font pas
                        partie du contenu essentiel du contrat peuvent être mises à jour via les fonctions système du
                        service de transfert de données agridata.ch. Ces ajustements ne modifient pas le présent contrat
                        relatif à l’échange de données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Ajout de nouveaux produits de données: À la demande de l’acquéreur de données et d’un commun
                        accord entre les partenaires contractuels, des produits de données nouveaux, étendus ou modifiés
                        peuvent être ajoutés ultérieurement au présent contrat relatif à l’échange de données via
                        agridata.ch, pour autant qu’ils correspondent quant au fond au but d'utilisation convenu.
                        L’acquéreur de données ne peut prétendre à la validation ou à la mise à disposition de telles
                        adaptations. La saisie et la validation s’effectuent de manière purement numérique dans le
                        système et ne nécessitent aucune adaptation contractuelle manuelle. Le consentement une fois
                        accordé par les producteurs de données s'applique également à ces adaptations, pour autant qu’il
                        s’agisse de compléments secondaires aux produits de données précédents et que l’exploitant
                        d’agridata.ch en informe de manière transparente les producteurs de données concernés et leur
                        offre une possibilité simple d’opposition (Opt-out). S’il s’agit de catégories de données
                        nouvelles ou considérablement étendues, un nouveau consentement actif (Opt-in) des producteurs
                        de données doit être recueilli via la demande adressée aux producteurs sur agridata.ch.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            8. Protection des données
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Les partenaires contractuels s’engagent à s’informer mutuellement et sans délai de toute
                        violation de la sécurité des données ou de tout soupçon fondé en ce sens.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Étant donné que l’échange de données est traité via agridata.ch, le partenaire contractuel
                        concerné est tenu d'envoyer simultanément une notification pour prise de connaissance au service
                        de support indiqué sur agridata.ch. De cette notification ne découle aucune obligation,
                        responsabilité ou devoir de surveillance autonomes pour l’exploitant d’agridata.ch ; elle sert
                        purement à l’information opérationnelle. Les obligations légales de notification (p. ex. envers
                        le PFPDT) demeurent entièrement du ressort des partenaires contractuels.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            9. Violation du contrat
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Blocage en cas d'abus ou de soupçon: En cas de soupçon fondé d’utilisation abusive des données
                        ou d’une autre violation substantielle des dispositions contractuelles, le fournisseur de
                        données ainsi que l’exploitant d’agridata.ch sont habilités à bloquer temporairement le flux de
                        données via le service de transfert de données à titre de mesure de protection.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Résiliation avec effet immédiat et exclusion: En cas de violations graves ou répétées du présent
                        contrat relatif à l’échange de données ou des dispositions légales applicables, le présent
                        contrat relatif à l’échange de données peut être résilié avec effet immédiat par la partie
                        lésée. Dans ce cas, la demande de données sur agridata.ch est désactivée de manière permanente
                        et l’acquéreur de données est exclu de toute extraction ultérieure de données concernant le
                        produit de données concerné.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            10. Litiges découlant du présent contrat
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        En cas de divergences d’opinions, les partenaires contractuels s’efforcent de bonne foi de
                        trouver un accord amiable aussi rapide que possible.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Si aucun accord ne peut être trouvé dans un délai de 30 jours ouvrables, la voie judiciaire est
                        ouverte aux partenaires contractuels. Le for juridique est déterminé comme suit:
                        <fo:list-block>
                            <fo:list-item>
                                <fo:list-item-label end-indent="label-end()">
                                    <fo:block>•</fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()">
                                    <fo:block>
                                        Si l’un au moins des partenaires contractuels est une autorité fédérale, le
                                        Tribunal
                                        administratif fédéral est compétent.
                                    </fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                            <fo:list-item>
                                <fo:list-item-label end-indent="label-end()">
                                    <fo:block>•</fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()">
                                    <fo:block>
                                        Si l’un des partenaires contractuels est une autorité cantonale (sans
                                        participation
                                        de la Confédération), le for est au siège de l’autorité cantonale.
                                    </fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                            <fo:list-item>
                                <fo:list-item-label end-indent="label-end()">
                                    <fo:block>•</fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()">
                                    <fo:block>
                                        S’il s'agit exclusivement de partenaires contractuels privés, le for est au
                                        siège du
                                        fournisseur de données, à l'exclusion de toute norme de conflit de lois (LDIP).
                                    </fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                        </fo:list-block>
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Si certaines dispositions du présent contrat relatif à l’échange de données devaient s’avérer
                        invalides, inefficaces ou inapplicables, la validité des autres parties du contrat relatif à
                        l’échange de données n’en serait pas affectée. Les partenaires contractuels s’engagent dans ce
                        cas à remplacer la disposition invalide par une réglementation conforme au droit qui se
                        rapproche le plus du but technique et juridique visé par la disposition initiale. Une adaptation
                        ultérieure du présent contrat relatif à l’échange de données étant exclue au niveau du système,
                        le contrat existant via le service de transfert de données agridata.ch doit dans ce cas être
                        résilié et une nouvelle demande de données corrigée doit être soumise. Si le remplacement d’une
                        disposition invalide exige l'adaptation de contenus essentiels du contrat, le contrat existant
                        via le service de transfert de données agridata.ch doit être résilié et une nouvelle demande de
                        données doit être soumise.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le présent contrat relatif à l’échange de données est exclusivement régi par le droit matériel
                        suisse.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>
    </xsl:template>
</xsl:stylesheet>