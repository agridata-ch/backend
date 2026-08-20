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
            <xsl:value-of select="consumerAddressInline"/> (ci-après le « destinataire des données »)
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text-spacious">
            et
        </fo:block>

        <fo:block xsl:use-attribute-sets="bold-paragraph">
            <xsl:value-of select="providerAddressInline"/> (ci-après le « fournisseur de données »)
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text-spacious">
            (ci-après nommés conjointement les « partenaires contractuels »)
        </fo:block>

        <xsl:call-template name="horizontal-rule"/>

        <fo:block xsl:use-attribute-sets="section-heading">
            Base légale pour la transmission de données
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            <fo:list-block xsl:use-attribute-sets="list-block">
                <fo:list-item xsl:use-attribute-sets="list-item">
                    <fo:list-item-label end-indent="label-end()">
                        <fo:block>1.</fo:block>
                    </fo:list-item-label>
                    <fo:list-item-body start-indent="body-start()">
                        <fo:block>
                            Utilisation du service de transfert de données : la mise à disposition et l’utilisation du
                            service technique agridata.ch reposent sur les art. 165c ss LAgr et l’art. 27, al. 9,
                            OSIAgr.
                        </fo:block>
                    </fo:list-item-body>
                </fo:list-item>
                <fo:list-item xsl:use-attribute-sets="list-item">
                    <fo:list-item-label end-indent="label-end()">
                        <fo:block>2.</fo:block>
                    </fo:list-item-label>
                    <fo:list-item-body start-indent="body-start()">
                        <fo:block>
                            <xsl:value-of select="providerSystemLegalBasis/fr"/>
                        </fo:block>
                    </fo:list-item-body>
                </fo:list-item>
                <fo:list-item xsl:use-attribute-sets="list-item">
                    <fo:list-item-label end-indent="label-end()">
                        <fo:block>3.</fo:block>
                    </fo:list-item-label>
                    <fo:list-item-body start-indent="body-start()">
                        <fo:block>
                            Si le destinataire des données a son siège ou son domicile hors de Suisse, il s’engage à
                            respecter les dispositions de la loi suisse sur la protection des données (LPD). Si le pays
                            dans lequel le destinataire des données est établi ne présente pas un niveau de protection
                            des données reconnu comme adéquat par le Conseil fédéral suisse, le destinataire des données
                            garantit la protection des données transmises en adoptant les clauses contractuelles types
                            (CCT) ou des garanties équivalentes reconnues par le préposé fédéral à la protection des
                            données et à la transparence (PFPDT) (selon l’art. 16, al. 2, LPD).
                        </fo:block>
                    </fo:list-item-body>
                </fo:list-item>
            </fo:list-block>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            Objet du contrat
        </fo:block>
        <fo:block xsl:use-attribute-sets="body-text">
            Le présent contrat relatif à l’échange de données règle la mise à disposition des données par le fournisseur
            de données sur demande du destinataire des données par l’intermédiaire du service de transfert de données
            agridata.ch. Il définit les droits et les devoirs des partenaires contractuels en la matière.
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Le présent contrat relatif à l’échange de données est établi en allemand et en français. En cas de
            contradiction ou de doute sur l’interprétation, seule la version allemande fait foi.
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            1 Définition et explication des termes
        </fo:block>

        <fo:list-block xsl:use-attribute-sets="list-block">
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block xsl:use-attribute-sets="body-text">
                        <fo:inline text-decoration="underline">agridata.ch</fo:inline>
                        : les partenaires contractuels utilisent le service de transfert de données agridata.ch pour la
                        transmission des données. Ce service sert à informer les producteurs de données, qui peuvent
                        également accorder, par cet intermédiaire, leur consentement à l’utilisation de produits de
                        données lorsque cela est requis. Il incombe au seul destinataire des données, et sous sa seule
                        responsabilité, de solliciter ce consentement auprès des producteurs de données ; le fournisseur
                        de données n’est pas impliqué dans le processus de demande de consentement. Les partenaires
                        contractuels utilisent agridata.ch sur une base volontaire et respectent les conditions
                        générales (CG) applicables, indépendamment du présent contrat relatif à l’échange de données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Producteur de données</fo:inline>
                        : les producteurs de données sont des personnes physiques ou morales qui, dans le cadre de leur
                        activité quotidienne, produisent, saisissent ou mettent à disposition des données qui peuvent
                        être transmises via agridata.ch. Il s’agit notamment, mais pas exclusivement, des exploitants,
                        des détenteurs d’animaux, des propriétaires d’équidés ainsi que des entreprises, organisations
                        ou institutions qui produisent ou gèrent des données relatives à l’agriculture.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Destinataire des données</fo:inline>
                        : les destinataires des données sont des personnes morales, des autorités ou des personnes
                        physiques qui, dans l’exercice de leur activité professionnelle ou commerciale (p. ex.
                        entreprises individuelles), accèdent aux données des producteurs via agridata.ch ou soumettent
                        des demandes de mise à disposition des données pour le faire. Ils utilisent ces données
                        exclusivement pour l’usage prévu dans la demande de données. L’accès n’est possible que si les
                        conditions relatives à la mise à disposition des données prévues par le présent contrat sont
                        remplies.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>

            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Fournisseur de données</fo:inline>
                        : les fournisseurs de données sont des personnes physiques ou morales, ou des autorités, qui
                        mettent à la disposition d’un destinataire des données, via agridata.ch, les données que ce
                        dernier a demandées. Le fournisseur décide en toute indépendance, dans le respect des
                        dispositions légales applicables, s’il veut communiquer ses données et à quel destinataire il
                        veut le faire.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Produit de données</fo:inline>
                        : jeu de données prédéfini par le fournisseur de données, avec des contenus et des formats
                        clairement définis, mis à disposition par l’intermédiaire du service de transfert de données
                        agridata.ch. Le nom et la description du produit de données sont présentés de manière
                        transparente au producteur de données lors de la demande de consentement. Le destinataire des
                        données peut bénéficier du produit de données dès que les conditions requises à cet effet sont
                        remplies.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Demande de données</fo:inline>
                        : demande adressée au fournisseur de données par un destinataire des données via le service de
                        transfert de données agridata.ch, en vue d’obtenir des produits de données définis pour un usage
                        précis. Le présent contrat relatif à l’échange de données repose sur la demande de données
                        approuvée.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Demande de mise à disposition des données</fo:inline>
                        : notification, sur agridata.ch, basée sur une demande de données approuvée. Adressée aux
                        producteurs de données, elle vise la transparence et permet aux producteurs de données d’avoir
                        connaissance de l’usage prévu des données et, dans le cas de produits de données nécessitant
                        leur consentement, de consentir ou non à la transmission de leurs données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Vérification du consentement</fo:inline>
                        : fonction automatisée du service de transfert de données agridata.ch, qui, avant toute
                        transmission de données à l’interface, vérifie et s’assure que les conditions juridiques
                        relatives à la mise à disposition des données sont respectées (consentement actif et valable des
                        producteurs de données concernées pour les produits de données requérant leur consentement).
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        <fo:inline text-decoration="underline">Interface d’échange de données</fo:inline>
                        : interface technique sécurisée connectant le destinataire des données ou le fournisseur de
                        données à agridata.ch et qui permet d’accéder, pour chaque demande de données, aux données des
                        producteurs définis, dans le respect des conditions applicables.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            2 Indications définies par le destinataire des données relatives à la demande de données
        </fo:block>

        <fo:block xsl:use-attribute-sets="body-text">
            Les indications ci-après ont été définies de manière autonome par le destinataire des données dans sa
            demande de données sur agridata.ch et reprises automatiquement dans le présent contrat. Elles s’affichent
            pour les producteurs de données dans la demande de mise à disposition des données et leur servent de base
            décisionnelle pour donner leur consentement.
        </fo:block>

        <fo:block margin-left="5mm">
            <fo:block xsl:use-attribute-sets="section-heading">
                Destinataire des données :
            </fo:block>

            <fo:block>
                <xsl:value-of select="consumerName"/>
            </fo:block>
            <fo:block>
                UID :
                <xsl:value-of select="consumerUid"/>
            </fo:block>
            <fo:block>
                <xsl:value-of select="consumerStreet"/>
            </fo:block>
            <fo:block>
                <xsl:value-of select="consumerZipCity"/>
            </fo:block>
            <fo:block>
                <xsl:value-of select="consumerCountry/fr"/>
            </fo:block>

            <fo:block>Contact :
                <xsl:value-of select="consumerPhoneNumber"/>,
                <xsl:value-of select="consumerEmailAddress"/>
            </fo:block>

            <fo:block xsl:use-attribute-sets="section-heading">
                Nom de la demande de données :
            </fo:block>

            <fo:block xsl:use-attribute-sets="body-text">
                Nom de la demande de données tel que défini dans la demande
            </fo:block>

            <fo:block xsl:use-attribute-sets="section-heading">
                Description de la demande de données :
            </fo:block>

            <fo:block xsl:use-attribute-sets="body-text">
                Allemand :
                <fo:inline xml:lang="de">
                    <xsl:value-of select="requestDescription/de"/>
                </fo:inline>
            </fo:block>
            <fo:block xsl:use-attribute-sets="body-text">
                Français :
                <xsl:value-of select="requestDescription/fr"/>
            </fo:block>
            <fo:block xsl:use-attribute-sets="body-text">
                Italien :
                <fo:inline xml:lang="it">
                    <xsl:value-of select="requestDescription/it"/>
                </fo:inline>
            </fo:block>

            <fo:block xsl:use-attribute-sets="section-heading">
                Usage prévu des données par le destinataire des données :
            </fo:block>

            <fo:block xsl:use-attribute-sets="body-text">
                Allemand :
                <fo:inline xml:lang="de">
                    <xsl:value-of select="requestPurpose/de"/>
                </fo:inline>
            </fo:block>
            <fo:block xsl:use-attribute-sets="body-text">
                Français :
                <xsl:value-of select="requestPurpose/fr"/>
            </fo:block>
            <fo:block xsl:use-attribute-sets="body-text">
                Italien :
                <fo:inline xml:lang="it">
                    <xsl:value-of select="requestPurpose/it"/>
                </fo:inline>
            </fo:block>

            <fo:block xsl:use-attribute-sets="section-heading">
                Système du fournisseur de données :
            </fo:block>
            <fo:block xsl:use-attribute-sets="body-text">
                Système source du fournisseur de données défini dans la demande
            </fo:block>

            <fo:block xsl:use-attribute-sets="section-heading">
                Produits de données souhaités :
            </fo:block>
            <fo:block xsl:use-attribute-sets="body-text">
                Le présent contrat relatif à l’échange de données porte sur les produits de données qui sont cités et
                validés dans la demande de données électronique sur agridata.ch, portant l’ID
                <xsl:value-of select="requestHumanFriendlyId"/>.
            </fo:block>

            <fo:block xsl:use-attribute-sets="section-heading">
                Indication du groupe cible :
            </fo:block>
            <fo:block>
                <xsl:value-of select="targetGroup"/>
            </fo:block>
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3 Obligations des partenaires contractuels
        </fo:block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3.1 Fournisseur de données
        </fo:block>
        <fo:list-block xsl:use-attribute-sets="list-block">
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le fournisseur de données décrit les produits de données qu’il met à disposition en termes de
                        contenu et de format. La description des produits de données peut être consultée par le
                        destinataire des données sur agridata.ch. Le nom et la description doivent représenter
                        correctement et de manière transparente le contenu effectif du produit de données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le fournisseur de données met les produits de données à la disposition du service de transfert
                        agridata.ch via l’interface sécurisée. Il s’assure que, de son côté, seuls les produits de
                        données respectant la protection des données et demandés par le service de transfert de données
                        agridata.ch soient transmis. Par défaut, la demande et la transmission sont assurées
                        automatiquement par agridata.ch, conformément aux conditions applicables relatives à la mise à
                        disposition de données (consentement actif et valable des producteurs de données concernées pour
                        les produits de données qui le requièrent ; base légale et définition du cercle des producteurs
                        pour les produits de données ne requérant pas le consentement des producteurs). Demeurent
                        réservés d’autres modes de mise à disposition bilatéraux (p. ex. transmission directe des
                        données par le fournisseur via une application à interface utilisateur graphique [GUI], e-mail
                        ou exportation manuelle de fichiers). Dans ces cas, il est du ressort du fournisseur de données
                        uniquement de vérifier et de s’assurer avant toute livraison de données que les conditions
                        relatives à la mise à disposition de données dans agridata.ch sont respectées (consentement
                        actif ou déclaration légale de transparence). Les services d’agridata.ch se limitent ici à la
                        mise à disposition d’informations d’identification ; la transmission technique et la sécurité
                        des données du mode de transmission choisi sont entièrement du ressort du fournisseur de
                        données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le fournisseur de données met en place à ses frais une interface technique appropriée avec
                        agridata.ch et l’entretient. Il est seul responsable de la mise en œuvre de celle-ci sur ses
                        propres systèmes, conformément à son fonctionnement et à ses spécifications. Les exigences
                        techniques et relatives à la sécurité de l’échange de données sont définies en fonction des
                        différentes conditions générales d’agridata.ch s’appliquant aux fournisseurs de données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le fournisseur de données s’engage à signaler le plus rapidement possible à agridata.ch toute
                        modification substantielle, restriction ou suppression de produits de données et les
                        modifications des interfaces qui en découlent.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Les éventuels coûts relatifs à l’installation et à l’adaptation de la mise à disposition des
                        données chez le fournisseur (p. ex. pour une nouvelle infrastructure informatique ou sa
                        modification, ou pour s’adapter à l’interface) ne peuvent pas être facturés au destinataire des
                        données ni à agridata.ch.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3.2 Destinataire des données
        </fo:block>
        <fo:list-block xsl:use-attribute-sets="list-block">
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Obtenir le consentement des producteurs de données relève de la seule responsabilité du
                        destinataire des données. Le fournisseur de données n’est soumis à aucune obligation
                        d’information, d’explication ou d’intervention auprès des producteurs de données. Il ne peut
                        être tenu responsable de l’existence ou de la validité des consentements ; la vérification
                        technique des consentements avant la mise à disposition des données est effectuée de manière
                        entièrement automatisée par agridata.ch.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le destinataire des données s’engage à envoyer des demandes de mise à disposition des données
                        uniquement aux producteurs de données définis comme groupe cible dans le présent contrat relatif
                        à l’échange de données. Toute prise de contact aléatoire ou sans rapport avec l’objectif est
                        interdite.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le destinataire des données prend en charge tous les coûts relatifs à la mise en place, à
                        l’adaptation, à l’utilisation ou à l’entretien de l’infrastructure permettant d’accéder aux
                        données (p. ex. pour sa propre infrastructure informatique, la modification des interfaces ou
                        des logiciels tiers). Toute répercussion des coûts ou facturation à la charge du fournisseur de
                        données ou d’agridata.ch est exclue.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            3.3 Transparence vis-à-vis des producteurs de données
        </fo:block>
        <fo:list-block xsl:use-attribute-sets="list-block">
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Les partenaires contractuels acceptent expressément que le présent contrat relatif à l’échange
                        de données
                        puisse être consulté par les producteurs de données concernés sur le service de transfert de
                        données
                        agridata.ch. Cette possibilité de consulter le présent contrat favorise l’autodétermination
                        numérique,
                        renforce la confiance et garantit la transparence des flux de données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            4 Garantie / Exclusion de responsabilité
        </fo:block>

        <fo:list-block xsl:use-attribute-sets="list-block">
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Droit à l’utilisation et à la mise à disposition de données : indépendamment de leur statut
                        légal, les partenaires contractuels confirment, par la signature électronique du présent contrat
                        relatif à l’échange de données, qu’ils sont pleinement habilités à remplir leur rôle respectif :
                    </fo:block>
                    <fo:list-block xsl:use-attribute-sets="list-block">
                        <fo:list-item xsl:use-attribute-sets="list-item">
                            <fo:list-item-label end-indent="label-end()">
                                <fo:block>•</fo:block>
                            </fo:list-item-label>
                            <fo:list-item-body start-indent="body-start()">
                                <fo:block>
                                    Le fournisseur de données confirme être pleinement habilité à mettre à
                                    disposition et à publier les données qui font l’objet du présent contrat.
                                </fo:block>
                            </fo:list-item-body>
                        </fo:list-item>
                        <fo:list-item xsl:use-attribute-sets="list-item">
                            <fo:list-item-label end-indent="label-end()">
                                <fo:block>•</fo:block>
                            </fo:list-item-label>
                            <fo:list-item-body start-indent="body-start()">
                                <fo:block>
                                    Le destinataire des données confirme être pleinement habilité à recevoir et à
                                    traiter ces données aux fins convenues.
                                </fo:block>
                            </fo:list-item-body>
                        </fo:list-item>
                    </fo:list-block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Définition du cercle des producteurs par le destinataire des données : la mise à disposition
                        technique des données présuppose la définition claire, par le destinataire des données, des
                        producteurs de données à solliciter par l’intermédiaire des fonctions système d’agridata.ch.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Conditions relatives à la mise à disposition et accès aux données : pour pouvoir accéder aux
                        données d’un producteur de données concret, le destinataire des données doit impérativement
                        respecter les conditions relatives à la mise à disposition des données dans agridata.ch
                        (consentement actif pour les produits de données qui le requièrent et base légale pour les
                        produits de données ne requérant pas le consentement des producteurs). L’examen se fait
                        automatiquement par le service de transfert de données. Si les conditions ne sont pas
                        satisfaites (p. ex. absence de consentement), on ne peut prétendre à aucun droit à la
                        communication des données, et toute responsabilité du fournisseur de données et de l’exploitant
                        d’agridata.ch quant aux conséquences qui en découlent est exclue. Dans le cas des modes de
                        transmission bilatérale (ch. 3.1), le fournisseur de données est responsable de vérifier que les
                        conditions préalables sont remplies avant de transmettre les données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Qualité des données : le fournisseur de données ne garantit pas l’exactitude, l’actualité ou
                        l’exhaustivité du contenu des données mises à disposition. Le fournisseur de données ne garantit
                        en particulier pas qu’il soit possible d’obtenir des données sur toutes les personnes et
                        exploitations pour lesquelles le destinataire demande des données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Disponibilité technique (best effort) : les données sont mises à disposition via les interfaces
                        sans garantie d’une disponibilité continue ou sans perturbations. Les fournisseurs de données
                        s’efforcent, dans la mesure de leurs capacités techniques, d’assurer un fonctionnement fiable
                        (best effort), sans toutefois garantir de durées d’exécution spécifiques du système ni de
                        niveaux de service (SLA).
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Exclusion de responsabilité : dans la mesure où cela est juridiquement admissible, le
                        fournisseur de données exclut toute action en responsabilité, notamment en cas de dommages
                        résultant de l’indisponibilité temporaire ou permanente de la communication des données (p. ex.
                        en cas d’interruptions du système ou de dysfonctionnements techniques) ou en cas de dommages dus
                        à des données défectueuses.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            5 Contrôle
        </fo:block>

        <fo:list-block xsl:use-attribute-sets="list-block">
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le fournisseur de données est habilité à faire vérifier, par un tiers indépendant tenu au secret
                        professionnel, que le destinataire des données respecte bien les dispositions du présent contrat
                        relatif à l’échange de données, ou à procéder lui-même à cette vérification (audit). Le
                        destinataire des données s’engage à présenter au fournisseur de données, sur demande justifiée,
                        les informations nécessaires pour vérifier que les données sont bien utilisées conformément au
                        présent contrat, et à lui permettre de consulter les documents pertinents. Les coûts relatifs à
                        la vérification sont à la charge du fournisseur de données, sauf en cas de constatation d’une
                        violation grave du présent contrat relatif à l’échange de données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            6 Durée du contrat
        </fo:block>
        <fo:list-block xsl:use-attribute-sets="list-block">
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le présent contrat relatif à l’échange de données entre en vigueur après la confirmation
                        numérique bilatérale (authentification via 2FA) effectuée sur le service de transfert de données
                        agridata.ch par les personnes ayant le droit de signature du côté du fournisseur de données et
                        du côté du destinataire des données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le contrat relatif à l’échange de données est conclu pour une durée indéterminée. Chacun des
                        partenaires contractuels peut résilier le contrat à la fin d’un mois, par écrit (p. ex. par
                        e-mail ou par l’intermédiaire du service de transfert de données), moyennant un préavis de trois
                        mois.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Le contrat relatif à l’échange de données est indissociable de l’existence de la demande de
                        données sur laquelle il se fonde. Si cette demande de données est supprimée ou désactivée de
                        façon permanente sur agridata.ch, le contrat relatif à l’échange de données qui y est rattaché
                        est automatiquement résilié, avec effet à la date de la suppression ou de la désactivation de la
                        demande, sans qu’une résiliation séparée soit nécessaire (clause de liaison).
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            7 Modifications du contrat
        </fo:block>
        <fo:list-block xsl:use-attribute-sets="list-block">
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Protection matérielle du contenu : l’usage prévu, la description succincte et le groupe cible
                        principal forment la base d’informations pour les producteurs de données. Une fois la demande de
                        données activée, ces éléments ne peuvent plus être modifiés. Toute extension ou modification
                        matérielle de l’usage prévu, de la description ou du groupe cible nécessite le dépôt d’une
                        nouvelle demande de données, et, le cas échéant, l’octroi du consentement des producteurs de
                        données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Modification textuelle du nom : si le nom de la demande de données se révèle a posteriori
                        incomplet, ambigu ou linguistiquement erroné, son texte peut être corrigé, à condition que le
                        sens fondamental, l’usage prévu et la description succincte restent intacts.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Données de suivi : les indications de la demande de données qui ne font pas partie du contenu
                        central du contrat peuvent être mises à jour via les fonctions système du service de transfert
                        de données agridata.ch. De telles adaptations ne modifient pas le présent contrat relatif à
                        l’échange de données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Modifications ultérieures ou complément des produits de données : les produits de données
                        peuvent être modifiés, complétés ou supprimés a posteriori sans influencer la validité du
                        contrat pour les autres produits de données. Les conditions et la procédure relatives à de
                        telles modifications sont régies par les CG.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            8 Protection des données
        </fo:block>
        <fo:list-block xsl:use-attribute-sets="list-block">
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Les partenaires contractuels s’informent mutuellement et immédiatement de toute violation de la
                        sécurité des données ou de tout soupçon fondé à cet égard.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Comme les données sont échangées par l’intermédiaire d’agridata.ch, le partenaire contractuel
                        concerné avertit en même temps, pour information, le service d’assistance indiqué sur
                        agridata.ch. Cette notification n’entraîne aucun devoir de surveillance, obligation ou
                        responsabilité spécifiques pour l’exploitant d’agridata.ch ; elle sert uniquement à des fins
                        d’information opérationnelle. Les obligations légales de déclaration (p. ex. auprès du PFPDT)
                        sont uniquement du ressort des partenaires contractuels.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            9 Infraction au contrat
        </fo:block>
        <fo:list-block xsl:use-attribute-sets="list-block">
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Blocage en cas d’utilisation abusive ou de soupçon : en cas de soupçon fondé d’utilisation
                        abusive des données ou de toute autre violation grave des dispositions contractuelles, le
                        fournisseur de données et l’exploitant d’agridata.ch sont habilités à prendre des mesures de
                        protection en bloquant temporairement le flux de données qui passe par le service de transfert
                        de données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Résiliation immédiate et exclusion : en cas de violation grave ou répétée du présent contrat
                        relatif à l’échange de données ou du droit applicable, le partenaire lésé peut résilier le
                        présent contrat sans préavis. Dans ce cas, la demande de données est désactivée de manière
                        permanente sur agridata.ch et le destinataire des données est exclu de tout accès ultérieur au
                        produit de données concerné.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>

        <fo:block xsl:use-attribute-sets="section-heading">
            10 Litiges découlant du présent contrat relatif à l’échange de données
        </fo:block>
        <fo:list-block xsl:use-attribute-sets="list-block">
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        En cas de divergences d’opinions, les partenaires contractuels s’efforcent de bonne foi de
                        trouver un accord à l’amiable aussi rapidement que possible.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Si aucun accord n’est trouvé dans un délai de 30 jours ouvrables, les partenaires contractuels
                        peuvent engager une action en justice. Le tribunal compétent est déterminé comme suit :
                    </fo:block>
                    <fo:list-block xsl:use-attribute-sets="list-block">
                        <fo:list-item xsl:use-attribute-sets="list-item">
                            <fo:list-item-label end-indent="label-end()">
                                <fo:block>•</fo:block>
                            </fo:list-item-label>
                            <fo:list-item-body start-indent="body-start()">
                                <fo:block>
                                    Si au moins un des partenaires contractuels est une autorité fédérale, c’est le
                                    Tribunal administratif fédéral qui est compétent.
                                </fo:block>
                            </fo:list-item-body>
                        </fo:list-item>
                        <fo:list-item xsl:use-attribute-sets="list-item">
                            <fo:list-item-label end-indent="label-end()">
                                <fo:block>•</fo:block>
                            </fo:list-item-label>
                            <fo:list-item-body start-indent="body-start()">
                                <fo:block>
                                    Si au moins un des partenaires contractuels est une autorité cantonale (sans
                                    participation de la Confédération), le for se trouve au siège de l’autorité
                                    cantonale.
                                </fo:block>
                            </fo:list-item-body>
                        </fo:list-item>
                        <fo:list-item xsl:use-attribute-sets="list-item">
                            <fo:list-item-label end-indent="label-end()">
                                <fo:block>•</fo:block>
                            </fo:list-item-label>
                            <fo:list-item-body start-indent="body-start()">
                                <fo:block>
                                    S’il s’agit uniquement de partenaires contractuels privés, le for se trouve au
                                    siège du fournisseur de données, en excluant toutes règles de conflits de lois
                                    (LDIP).
                                </fo:block>
                            </fo:list-item-body>
                        </fo:list-item>
                    </fo:list-block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Si certaines dispositions de ce contrat relatif à l’échange de données devaient s’avérer
                        invalides, inefficaces ou inapplicables, cela n’affecterait pas la validité des autres parties
                        du contrat relatif à l’échange de données. Dans ce cas, les partenaires contractuels s’engagent
                        à remplacer la disposition invalide par une réglementation conforme au droit qui se rapproche le
                        plus possible de l’objectif technique et juridique visé par la disposition initiale. Étant donné
                        qu’il n’est pas possible, dans le système, de modifier a posteriori le présent contrat relatif à
                        l’échange de données, il convient dans ce cas de résilier le contrat existant par
                        l’intermédiaire du service de transmission de données agridata.ch et de déposer une nouvelle
                        demande de données corrigée. Si le remplacement d’une disposition invalide nécessite la
                        modification d’éléments essentiels du contrat, le contrat existant doit être résilié par
                        l’intermédiaire du service de transmission de données agridata.ch et une nouvelle demande de
                        données doit être déposée.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item xsl:use-attribute-sets="list-item">
                <fo:list-item-label end-indent="label-end()">
                    <fo:block>•</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                        Seul le droit suisse matériel s’applique au présent contrat relatif à l’échange de données.
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>
    </xsl:template>
</xsl:stylesheet>
