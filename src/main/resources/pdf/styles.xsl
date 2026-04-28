<xsl:stylesheet
        version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:fo="http://www.w3.org/1999/XSL/Format"
>
    <!-- ========================================================= -->
    <!-- Reusable attribute sets                                   -->
    <!-- ========================================================= -->

    <xsl:attribute-set name="font-helvetica">
        <xsl:attribute name="font-family">Helvetica</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="font-helvetica-8" use-attribute-sets="font-helvetica">
        <xsl:attribute name="font-size">8pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="font-helvetica-9" use-attribute-sets="font-helvetica">
        <xsl:attribute name="font-size">9pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="font-helvetica-10" use-attribute-sets="font-helvetica">
        <xsl:attribute name="font-size">10pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="font-helvetica-11" use-attribute-sets="font-helvetica">
        <xsl:attribute name="font-size">11pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="font-helvetica-12" use-attribute-sets="font-helvetica">
        <xsl:attribute name="font-size">12pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="font-helvetica-14" use-attribute-sets="font-helvetica">
        <xsl:attribute name="font-size">14pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="document-title" use-attribute-sets="font-helvetica-14">
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="space-after">2mm</xsl:attribute>
        <xsl:attribute name="margin-top">5mm</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="body-text" use-attribute-sets="font-helvetica-10">
        <xsl:attribute name="space-after">5mm</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="body-text-justify" use-attribute-sets="body-text">
        <xsl:attribute name="text-align">justify</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="body-text-spacious" use-attribute-sets="font-helvetica-10">
        <xsl:attribute name="space-after">6mm</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="bold-paragraph" use-attribute-sets="font-helvetica-11">
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="space-after">6mm</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="section-heading" use-attribute-sets="font-helvetica-11">
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="space-before">3mm</xsl:attribute>
        <xsl:attribute name="space-after">3mm</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="footer-address" use-attribute-sets="font-helvetica-8">
        <xsl:attribute name="margin-left">100mm</xsl:attribute>
        <xsl:attribute name="text-align">left</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="page-number-footer" use-attribute-sets="font-helvetica-10">
        <xsl:attribute name="text-align">right</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="signature-table" use-attribute-sets="font-helvetica-10">
        <xsl:attribute name="table-layout">fixed</xsl:attribute>
        <xsl:attribute name="width">100%</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="signature-block-container">
        <xsl:attribute name="margin-left">2mm</xsl:attribute>
        <xsl:attribute name="height">12mm</xsl:attribute>
        <xsl:attribute name="display-align">after</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="signature-placeholder-inline">
        <xsl:attribute name="background-color">#BBB</xsl:attribute>
        <xsl:attribute name="padding">1mm</xsl:attribute>
        <xsl:attribute name="font-family">Courier</xsl:attribute>
    </xsl:attribute-set>

    <!-- ========================================================= -->
    <!-- Helper templates                                          -->
    <!-- ========================================================= -->

    <xsl:template name="horizontal-rule">
        <xsl:param name="space-after">0mm</xsl:param>
        <fo:block space-after="{$space-after}">
            <fo:leader leader-pattern="rule"
                       rule-thickness="0.5pt"
                       leader-length="100%"/>
        </fo:block>
    </xsl:template>

</xsl:stylesheet>