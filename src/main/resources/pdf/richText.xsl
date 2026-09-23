<?xml version="1.0" encoding="UTF-8"?>
<!--
    Converts the supported rich-text (HTML) subset stored in the request purpose
    (p, strong, em, u, ul, ol, li) into XSL-FO, so the markup renders as formatted
    text (paragraphs, bold, italic, underline, bulleted and numbered lists) instead of being
    printed as escaped source. Unknown elements are unwrapped; text nodes are copied
    verbatim by the built-in templates, which keeps legacy plain-text purposes intact.
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <!-- Renders one localized purpose: a label followed by the transformed rich-text content. -->
    <xsl:template name="render-purpose-language">
        <xsl:param name="label"/>
        <xsl:param name="lang"/>
        <xsl:param name="content"/>

        <fo:block xsl:use-attribute-sets="body-text">
            <fo:block>
                <xsl:value-of select="$label"/>
            </fo:block>
            <fo:block xml:lang="{$lang}">
                <xsl:apply-templates select="$content" mode="richtext"/>
            </fo:block>
        </fo:block>
    </xsl:template>

    <xsl:template match="p" mode="richtext">
        <fo:block space-after="2mm">
            <xsl:apply-templates mode="richtext"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="strong" mode="richtext">
        <fo:inline font-weight="bold">
            <xsl:apply-templates mode="richtext"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="em" mode="richtext">
        <fo:inline font-style="italic">
            <xsl:apply-templates mode="richtext"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="u" mode="richtext">
        <fo:inline text-decoration="underline">
            <xsl:apply-templates mode="richtext"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="ul" mode="richtext">
        <fo:list-block xsl:use-attribute-sets="list-block">
            <xsl:for-each select="li">
                <fo:list-item xsl:use-attribute-sets="list-item">
                    <fo:list-item-label end-indent="label-end()">
                        <fo:block>•</fo:block>
                    </fo:list-item-label>
                    <fo:list-item-body start-indent="body-start()">
                        <fo:block>
                            <xsl:apply-templates select="node()" mode="richtext"/>
                        </fo:block>
                    </fo:list-item-body>
                </fo:list-item>
            </xsl:for-each>
        </fo:list-block>
    </xsl:template>

    <xsl:template match="ol" mode="richtext">
        <fo:list-block xsl:use-attribute-sets="list-block">
            <xsl:for-each select="li">
                <fo:list-item xsl:use-attribute-sets="list-item">
                    <fo:list-item-label end-indent="label-end()">
                        <fo:block>
                            <xsl:number value="position()" format="1."/>
                        </fo:block>
                    </fo:list-item-label>
                    <fo:list-item-body start-indent="body-start()">
                        <fo:block>
                            <xsl:apply-templates select="node()" mode="richtext"/>
                        </fo:block>
                    </fo:list-item-body>
                </fo:list-item>
            </xsl:for-each>
        </fo:list-block>
    </xsl:template>
</xsl:stylesheet>
