<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:map="http://www.w3.org/2005/xpath-functions/map" version="3.0">
    <xsl:output method="text"/>
    <xsl:template match="/">
        <xsl:value-of select="head(map:find(json-doc('test.json'), 'key'))"/>
    </xsl:template>
</xsl:stylesheet>
