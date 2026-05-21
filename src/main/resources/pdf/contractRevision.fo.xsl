<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:include href="styles.xsl"/>
    <xsl:include href="frenchContract.xsl"/>
    <xsl:include href="germanContract.xsl"/>

    <!-- ========================================================= -->
    <!-- Helper templates                                          -->
    <!-- ========================================================= -->

    <xsl:template name="signature-cell">
        <xsl:param name="side"/>
        <xsl:param name="signatureDate"/>
        <xsl:param name="signatureName"/>
        <xsl:param name="bottomName" select="$signatureName"/>
        <xsl:param name="signatureText"/>

        <fo:table-cell padding-top="12mm">
            <xsl:choose>
                <xsl:when test="$side = 'left'">
                    <xsl:attribute name="padding-right">8mm</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="padding-left">8mm</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>

            <fo:block-container xsl:use-attribute-sets="signature-block-container">
                <fo:block margin-left="2mm">

                    <xsl:choose>
                        <xsl:when test="$signatureText and $signatureText != ''">
                            <xsl:value-of select="$signatureText"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <fo:inline xsl:use-attribute-sets="signature-placeholder-inline">
                                <xsl:if test="$signatureName and $signatureName != ''">
                                    <xsl:value-of select="$signatureDate"/>,
                                    <xsl:value-of select="$signatureName"/>
                                </xsl:if>
                            </fo:inline>
                        </xsl:otherwise>
                    </xsl:choose>

                </fo:block>
            </fo:block-container>

            <fo:block space-after="2mm">
                <fo:leader leader-pattern="dots" leader-length="100%"/>
            </fo:block>

            <fo:block>
                <xsl:value-of select="$bottomName"/>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <!-- ========================================================= -->
    <!-- Main template                                             -->
    <!-- ========================================================= -->

    <xsl:template match="/ContractRevision">
        <fo:root>
            <fo:layout-master-set>
                <fo:simple-page-master master-name="first-page"
                                       page-height="297mm"
                                       page-width="210mm"
                                       margin="20mm">
                    <fo:region-body margin-top="35mm" margin-bottom="20mm"/>
                    <fo:region-before extent="50mm" region-name="first.before"/>
                    <fo:region-after extent="15mm"/>
                </fo:simple-page-master>

                <fo:simple-page-master master-name="rest-pages"
                                       page-height="297mm"
                                       page-width="210mm"
                                       margin="20mm">
                    <fo:region-body margin-top="20mm" margin-bottom="20mm"/>
                    <fo:region-after extent="15mm"/>
                </fo:simple-page-master>

                <fo:page-sequence-master master-name="document-sequence">
                    <fo:single-page-master-reference master-reference="first-page"/>
                    <fo:repeatable-page-master-reference master-reference="rest-pages"/>
                </fo:page-sequence-master>
            </fo:layout-master-set>

            <fo:page-sequence master-reference="document-sequence">

                <!-- First page header -->
                <fo:static-content flow-name="first.before">
                    <fo:block xsl:use-attribute-sets="font-helvetica-8">
                        <fo:table table-layout="fixed" width="100%">
                            <fo:table-column column-width="50%"/>
                            <fo:table-column column-width="50%"/>
                            <fo:table-body>
                                <fo:table-row>
                                    <fo:table-cell display-align="before">
                                        <fo:block>
                                            <fo:external-graphic src="url('swiss-logo.png')"
                                                                 content-height="15mm"
                                                                 content-width="auto"
                                                                 scaling="uniform"/>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell display-align="before" text-align="left">
                                        <fo:block>
                                            Eidgenössisches Departement für Wirtschaft, Bildung und Forschung WBF
                                        </fo:block>
                                        <fo:block font-weight="bold">
                                            Bundesamt für Landwirtschaft BLW
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                </fo:static-content>

                <!-- Page footer -->
                <fo:static-content flow-name="xsl-region-after">
                    <fo:block xsl:use-attribute-sets="page-number-footer">
                        <fo:page-number/>
                    </fo:block>
                </fo:static-content>

                <!-- Body -->
                <fo:flow flow-name="xsl-region-body" xsl:use-attribute-sets="body-text">

                    <xsl:call-template name="germanContract"/>

                    <xsl:call-template name="frenchContract"/>

                    <fo:block page-break-after="always"/>

                    <!-- Signature area -->
                    <fo:table xsl:use-attribute-sets="signature-table">
                        <fo:table-column column-width="50%"/>
                        <fo:table-column column-width="50%"/>
                        <fo:table-body>
                            <fo:table-row>
                                <fo:table-cell padding-top="8mm" padding-right="8mm">
                                    <fo:block>Für
                                        <xsl:value-of select="consumerName"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding-top="8mm" padding-left="8mm">
                                    <fo:block>Für
                                        <xsl:value-of select="providerName"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>

                            <fo:table-row>
                                <xsl:call-template name="signature-cell">
                                    <xsl:with-param name="side">left</xsl:with-param>
                                    <xsl:with-param name="signatureDate">
                                        <xsl:value-of select="consumerSignatureDate1"/>
                                    </xsl:with-param>
                                    <xsl:with-param name="signatureName">
                                        <xsl:value-of select="consumerSignatureName1"/>
                                    </xsl:with-param>
                                    <xsl:with-param name="signatureText"/>
                                </xsl:call-template>

                                <xsl:call-template name="signature-cell">
                                    <xsl:with-param name="side">right</xsl:with-param>
                                    <xsl:with-param name="signatureDate">
                                        <xsl:value-of select="providerSignatureDate1"/>
                                    </xsl:with-param>
                                    <xsl:with-param name="signatureName">
                                        <xsl:value-of select="providerSignatureName1"/>
                                    </xsl:with-param>
                                    <xsl:with-param name="signatureText"/>
                                </xsl:call-template>
                            </fo:table-row>

                            <fo:table-row>
                                <xsl:call-template name="signature-cell">
                                    <xsl:with-param name="side">left</xsl:with-param>
                                    <xsl:with-param name="signatureDate">
                                        <xsl:value-of select="consumerSignatureDate2"/>
                                    </xsl:with-param>
                                    <xsl:with-param name="signatureName">
                                        <xsl:value-of select="consumerSignatureName2"/>
                                    </xsl:with-param>
                                    <xsl:with-param name="signatureText">
                                        <xsl:if test="consumerSignatureType = 'INDIVIDUAL_SIGNATURE'">
                                            <xsl:text>Unterzeichnet durch </xsl:text>
                                            <xsl:value-of select="consumerSignatureName1"/>
                                            <xsl:text> in der Funktion als einzelzeichnungsberechtigte Person</xsl:text>
                                        </xsl:if>
                                    </xsl:with-param>
                                </xsl:call-template>

                                <xsl:call-template name="signature-cell">
                                    <xsl:with-param name="side">right</xsl:with-param>
                                    <xsl:with-param name="signatureName">
                                        <xsl:value-of select="providerSignatureName2"/>
                                    </xsl:with-param>
                                    <xsl:with-param name="signatureDate">
                                        <xsl:value-of select="providerSignatureDate2"/>
                                    </xsl:with-param>
                                    <xsl:with-param name="signatureText">
                                        <xsl:if test="providerSignatureType = 'INDIVIDUAL_SIGNATURE'">
                                            <xsl:text>Unterzeichnet durch </xsl:text>
                                            <xsl:value-of select="providerSignatureName1"/>
                                            <xsl:text> in der Funktion als einzelzeichnungsberechtigte Person</xsl:text>
                                        </xsl:if>
                                    </xsl:with-param>
                                </xsl:call-template>
                            </fo:table-row>
                        </fo:table-body>
                    </fo:table>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>