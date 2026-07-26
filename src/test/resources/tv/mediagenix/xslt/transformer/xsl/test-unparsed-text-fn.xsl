<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0">
    <xsl:output method="text"/>
    <xsl:template match="/">
        <xsl:value-of select="unparsed-text('test.txt')"/>
    </xsl:template>
</xsl:stylesheet>
