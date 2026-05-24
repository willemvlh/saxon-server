<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0">
    <xsl:output method="text"/>
    <xsl:param name="myParam"/>
    <xsl:template match="/">
        <xsl:value-of select="$myParam"/>
    </xsl:template>
</xsl:stylesheet>
