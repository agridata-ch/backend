<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:template name="frenchContract">

        <fo:block xsl:use-attribute-sets="document-title" break-before="page">
            Contrat
        </fo:block>

        <xsl:call-template name="horizontal-rule">
            <xsl:with-param name="space-after">10mm</xsl:with-param>
        </xsl:call-template>

        <fo:block xsl:use-attribute-sets="body-text-spacious">
            entre
        </fo:block>

        <fo:block xsl:use-attribute-sets="bold-paragraph">
            <xsl:value-of select="consumerAddressInline"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text-spacious">
            et
        </fo:block>

        <fo:block xsl:use-attribute-sets="bold-paragraph">
            <xsl:value-of select="providerAddressInline"/>
        </fo:block>

        <xsl:call-template name="horizontal-rule"/>

        <fo:block xsl:use-attribute-sets="section-heading">
            Bases juridiques pour la mise à disposition des données
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text-justify">
            Le/La
            <fo:inline font-weight="bold">
                <xsl:value-of select="consumerName"/>
            </fo:inline>
            (ci-après : fournisseur de données) et
            <fo:inline font-weight="bold">
                <xsl:value-of select="providerName"/>
            </fo:inline>
            (ci-après : utilisateur de données) conviennent ce qui suit, sur la base des art. 165c ss de la
            loi du 29 avril 1998 sur l’agriculture (RS 910.1), de l’art. 36 de la loi fédérale du 25
            septembre 2020 sur la protection des données (RS 235.1) et de l’art. 27, al. 9, de l’ordonnance
            du 23 octobre 2013 sur les systèmes d’information dans le domaine de l’agriculture (RS
            919.117.71) :
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Objet du contrat
        </fo:block>
        <fo:block>
            Le présent contrat règle la mise à disposition des données par le fournisseur à l’utilisateur
            des données. Il définit les droits et les devoirs des deux parties en la matière.
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            <fo:inline font-weight="bold">Ne font pas</fo:inline>
            l’objet du présent contrat les éventuelles rémunérations convenues entre les partenaires
            contractuels, par exemple pour la préparation ou l’utilisation des données.
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Le présent contrat est établi en allemand et en français. En cas de contradiction ou de doute
            sur l’interprétation, seule la version allemande fait foi.
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Définition et explication des termes
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
                        les partenaires contractuels utilisent le service de transfert de données
                        agridata.ch pour la transmission des données et les demandes de consentement
                        adressées aux producteurs de données pour le partage de leurs données. Les
                        conditions générales (CG) applicables pour l’utilisation d’agridata.ch doivent être
                        respectées par les parties, indépendamment du présent contrat.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Producteur de données</fo:inline>:
                        les producteurs de données sont des personnes physiques ou morales qui, dans le
                        cadre de leur activité quotidienne, produisent, saisissent ou préparent des données
                        qui peuvent être transmises via agridata.ch. Il s’agit notamment, mais pas
                        exclusivement, des exploitants, des détenteurs d’animaux, des propriétaires
                        d’équidés ainsi que des entreprises, organisations ou institutions qui produisent ou
                        gèrent des données relatives à l’agriculture.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Utilisateur de données</fo:inline>
                        : les utilisateurs de données sont des personnes physiques ou morales, ou des
                        autorités, qui accèdent aux données des producteurs via agridata.ch ou qui
                        soumettent des demandes de données. Ils utilisent exclusivement ces données pour les
                        buts fixés dans la demande. L’accès n’est possible qu’avec le consentement explicite
                        des producteurs de données concernés.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Fournisseur des données</fo:inline>:
                        les fournisseurs de données mettent à disposition via agridata.ch les données
                        demandées par les utilisateurs. Ils ne transmettent ces données que si les
                        producteurs de données concernés ont donné leur consentement.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Demande de données</fo:inline>:
                        demande de l’utilisateur de données visant à obtenir du fournisseur des produits de
                        données prédéfinis pour un usage visé à l’art. 27, al. 9, OSIAgr. Les producteurs de
                        données peuvent consulter la demande de données dans le service agridata.ch et y
                        donner leur consentement à la transmission de données du fournisseur à
                        l’utilisateur.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Produit de données</fo:inline>:
                        les fournisseurs de données proposent aux utilisateurs des produits de données dont
                        le contenu est clairement défini. Un utilisateur peut demander plusieurs produits de
                        données d’un fournisseur.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Demande de consentement</fo:inline>:
                        service dans agridata.ch à l’aide duquel les producteurs de données peuvent
                        consulter les demandes de données en attente et donner leur consentement à la
                        transmission des données. Ils peuvent accepter ou refuser la transmission pour
                        chaque demande.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item space-after="2mm">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Interface d’échange de données</fo:inline>:
                        interface technique protégée sur laquelle les utilisateurs de données peuvent, pour
                        chaque demande, obtenir toutes les données des producteurs qui ont donné leur
                        consentement.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            2. Indications sur la demande
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Utilisateur de données :
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
            Nom de la demande :
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
            Description de la demande de données :
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            DE:
            <xsl:value-of select="requestDescription/De"/>
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
            Objectif visé par l’utilisateur pour ces données et indication des éventuelles transmissions
            prévues à des tiers (ou exclusion de celles-ci) :
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            DE:
            <xsl:value-of select="requestPurpose/de"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            FR:
            <xsl:value-of select="requestPurpose/fr"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            IT:
            <xsl:value-of select="requestPurpose/it"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Nom du système du fournisseur de données :
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            DE:
            <xsl:value-of select="providerSystemName/de"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            FR:
            <xsl:value-of select="providerSystemName/fr"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            IT:
            <xsl:value-of select="providerSystemName/it"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Produit de données souhaité :
        </fo:block>
        <fo:list-block>
            <xsl:for-each select="products">
                <fo:list-item>
                    <fo:list-item-label end-indent="label-end()">
                        <fo:block>•</fo:block>
                    </fo:list-item-label>

                    <fo:list-item-body start-indent="body-start()">
                        <fo:block>
                            <xsl:value-of select="fr"/>
                        </fo:block>
                    </fo:list-item-body>
                </fo:list-item>
            </xsl:for-each>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Indication du groupe cible :
        </fo:block>
        <fo:block>
            <xsl:value-of select="targetGroup"/>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3. Obligations des partenaires contractuels
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3.1 CG applicables pour l’utilisation d’agridata.ch
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Les partenaires contractuels doivent respecter les conditions générales (CG)
                        applicables pour l’utilisation d’agridata.ch, indépendamment du présent contrat.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3.2 Fournisseur de données
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le fournisseur de données met à disposition les produits de données décrits dans
                        la demande via agridata.ch. Ce faisant, agridata.ch garantit que seules sont
                        transmises les données du fournisseur pour lesquelles le producteur de données a
                        donné son consentement.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le fournisseur de données s’engage à mettre en place et à maintenir à ses frais
                        une interface technique appropriée avec agridata.ch. Cette interface doit assurer un
                        échange de données sûr, complet et automatisé, conformément aux spécifications
                        suivantes :
                        <fo:basic-link
                                external-destination="url('https://github.com/agridata-ch/.github/wiki')"
                                color="blue"
                                text-decoration="underline">
                            https://github.com/agridata-ch/.github/wiki
                        </fo:basic-link>
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le fournisseur de données met les données à la disposition de l’utilisateur via
                        une interface sécurisée, afin que celui-ci puisse les consulter de manière autonome.
                        Aussi bien chez le fournisseur que chez l’utilisateur, l’accès aux données doit être
                        restreint, contrôlé et protégé.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Si des produits de données déjà fournis ne sont plus proposés ou sont modifiés, le
                        fournisseur en informe agridata.ch et l’utilisateur des données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Les frais éventuels encourus par le fournisseur pour la préparation des données
                        (par exemple pour une infrastructure informatique nouvelle ou adaptée ou pour une
                        modification de l’interface) ne peuvent être facturés à l’utilisateur ou à
                        agridata.ch.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3.3 Utilisateur de données
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Les données livrées sont traitées exclusivement dans les buts qui ont été indiqués
                        aux producteurs ayant donné leur consentement et au fournisseur.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        L’utilisateur s’engage à informer le producteur des données de toute intention de
                        transmettre ces données à des tiers dans la description de l’usage prévu. Cela ne
                        concerne pas une éventuelle communication aux autorités qui se fonde sur une base
                        légale.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        L’utilisateur s’engage à informer le producteur des données lorsque lui-même ou
                        des tiers consultent les données depuis l’étranger.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        L’utilisateur de données est autorisé à faire appel à des sous-traitants (pour son
                        compte et dans son intérêt).
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        L’utilisateur des données est responsable de la protection des données et s’engage
                        à respecter la législation en la matière, y compris l’obligation d’obtenir, le cas
                        échéant, le consentement des producteurs de données pour la transmission de
                        celles-ci. Aussi bien chez le fournisseur que chez l’utilisateur, l’accès aux
                        données doit être restreint, contrôlé et protégé.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Il incombe aux utilisateurs de données d’informer eux-mêmes les producteurs des
                        demandes de données qui les concernent et d’obtenir leur consentement ou de
                        s’assurer que ce consentement a bien été donné.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Pour communiquer activement avec les producteurs de données au sujet de la
                        demande, l’utilisateur des données utilise les modèles fournis par agridata.ch, afin
                        de garantir une communication de bout en bout avec les producteurs dans le cadre
                        d’agridata.ch.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        L’utilisateur des données ne peut envoyer le lien vers la demande de données
                        qu’aux producteurs de données décrits comme groupe cible dans les informations
                        relatives au contrat.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Si l’utilisateur des données est en mesure de fournir sur agridata.ch une liste
                        des numéros IDE des producteurs de données concernés, la demande de données peut
                        être activée sur agridata.ch pour ces producteurs de données et des courriels
                        d’information peuvent leur être envoyés.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Les frais éventuels encourus par l’utilisateur pour l’acquisition des données (par
                        exemple pour une infrastructure informatique nouvelle ou adaptée ou pour une
                        modification de l’interface) ne peuvent être facturés au fournisseur de données ou à
                        agridata.ch.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

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
                        Le fournisseur de données et agridata.ch ne garantissent pas l’exactitude ni
                        l’exhaustivité des données fournies. En particulier, ils ne garantissent pas que des
                        données puissent être livrées sur l’ensemble des personnes et des exploitations pour
                        lesquelles l’utilisateur a fait une demande.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Dans la mesure où cela est juridiquement admissible, le fournisseur de données et
                        agridata.ch excluent toute action en responsabilité, notamment en cas de
                        non-fonctionnement de l’accès aux données ou de données défectueuses.
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
                        Tant le Contrôle fédéral des finances qu’agridata.ch sont habilités en tout temps
                        à effectuer des contrôles et à obtenir des informations sur toutes les parties du
                        présent contrat ; ils peuvent également faire exercer ces droits par des experts
                        extérieurs à l’Administration fédérale.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le partenaire contractuel doit conférer aux organes de contrôle en tout temps un
                        droit, respectivement, de regard et d’accès sur l’ensemble des documents et des
                        installations qui font l’objet du présent contrat ; il doit par ailleurs se tenir à
                        leur disposition pour tout renseignement.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Les organes de contrôle sont tenus au secret de fonction et doivent respecter les
                        dispositions relatives à la protection des données lorsqu’ils traitent des données à
                        caractère personnel.
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
                        Le présent contrat prend effet par la signature des deux partenaires contractuels.
                        À partir de ce moment, les producteurs de données peuvent modifier le consentement
                        donné à la transmission des données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le contrat reste en vigueur jusqu’à sa résiliation écrite par l’un des partenaires
                        contractuels.
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
                        Il n’est pas possible de modifier le contrat après sa conclusion, car toute
                        modification affecte également l’ensemble des consentements relatifs à la
                        transmission des données qui ont été accordés jusque-là.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le contrat peut être résilié par les deux partenaires contractuels.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        La résiliation par écrit est possible, dans un délai de trois mois à compter de la
                        fin
                        du mois.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            8. Protection des données / obligation de garder le secret
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Les partenaires contractuels s’engagent à protéger les données des producteurs
                        conformément aux dispositions en vigueur, et en particulier à prendre des mesures
                        techniques et organisationnelles pour assurer une sécurité appropriée des données
                        dans leur sphère de responsabilité et de compétence.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Même au terme du présent contrat, les partenaires contractuels garantissent
                        intégralement la protection des données et du secret de toutes les données
                        (informations) dont elles ont eu connaissance dans le cadre du présent contrat ou
                        qu’elles ont traitées d’une manière ou d’une autre. À cette fin, ils prennent toutes
                        les mesures de protection nécessaires sur le plan de l’organisation, de la
                        technique, du personnel et de l’informatique.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Les partenaires contractuels garantissent en particulier que toutes les données
                        dont ils ont eu connaissance seront traitées et utilisées dans le seul but fixé par
                        le contrat et ils s’assurent que tous les collaborateurs et (le cas échéant) les
                        prestataires externes (et leurs collaborateurs) respectent également strictement les
                        dispositions applicables en matière de confidentialité et de protection des données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Les partenaires contractuels s’informent mutuellement et sans délai de tout
                        incident touchant à la protection des données ou de tout soupçon fondé.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            9. Infraction au contrat
        </fo:block>
        <fo:list-block>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        En cas d’utilisation abusive des données ou en cas d’autre violation des
                        dispositions du présent contrat, les partenaires contractuel peuvent résilier
                        celui-ci avec effet immédiat. Le cas échéant, les demandes de données en cours du
                        partenaire contractuel sont désactivées et aucune des données demandées ne sera plus
                        fournie.
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
                        En cas de divergences d’opinions, les partenaires contractuels s’efforcent de
                        bonne foi de trouver un accord à l’amiable aussi rapidement que possible.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Si les divergences d’opinion ne peuvent être éliminées et qu’un plan visant à
                        résoudre la question ne peut être convenu dans un délai de 30 jours ouvrables,
                        chaque partenaire contractuel est libre d’engager une action en justice.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Lorsqu’une action est introduite, le Tribunal administratif fédéral statue en
                        première instance sur un litige découlant du présent contrat.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Si certaines dispositions de ce contrat devaient s’avérer invalides, inefficaces
                        ou inapplicables, cela n’affecterait pas la validité, l’efficacité et l’application
                        des autres parties du contrat. Dans ce cas, les partenaires contractuels s’engagent
                        à remplacer la disposition invalide, inefficace ou inapplicable par une disposition
                        valable, efficace et applicable, dont le contenu se rapproche au plus près de la
                        volonté initiale des partenaires contractuels.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

    </xsl:template>

</xsl:stylesheet>