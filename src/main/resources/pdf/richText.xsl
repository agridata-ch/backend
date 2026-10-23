<?xml version="1.0" encoding="UTF-8"?>
<!--
    Converts the supported rich-text (HTML) subset stored in the request purpose
    (p, br, strong, em, u, ul, ol, li) into XSL-FO, so the markup renders as formatted
    text (paragraphs, line breaks, bold, italic, underline, bulleted and numbered lists) instead of being
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

    <!--
        A hard line break (<br/>): emitted as U+2028 LINE SEPARATOR, which XSL-FO treats as a forced
        line break. Unlike an empty fo:block, consecutive breaks do not collapse, so <br><br> yields
        an empty line as in the editor.
    -->
    <xsl:template match="br" mode="richtext">
        <xsl:text>&#x2028;</xsl:text>
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

    <!--
        Honours the editor's start (first number) and type (1, a, A, i, I) attributes. The HTML type
        values double as xsl:number format tokens; anything else falls back to decimal from 1.
    -->
    <xsl:template match="ol" mode="richtext">
        <xsl:variable name="start">
            <xsl:choose>
                <xsl:when test="number(@start) &gt;= 1">
                    <xsl:value-of select="floor(@start)"/>
                </xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="format">
            <xsl:choose>
                <xsl:when test="@type = 'a' or @type = 'A' or @type = 'i' or @type = 'I'">
                    <xsl:value-of select="@type"/>
                </xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <fo:list-block xsl:use-attribute-sets="list-block">
            <xsl:for-each select="li">
                <fo:list-item xsl:use-attribute-sets="list-item">
                    <fo:list-item-label end-indent="label-end()">
                        <fo:block>
                            <xsl:number value="$start + position() - 1" format="{$format}."/>
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
