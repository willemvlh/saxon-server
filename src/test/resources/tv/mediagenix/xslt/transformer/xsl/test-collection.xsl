<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0">
    <xsl:template match="/">
      <xsl:value-of select="collection('/')"/>
    </xsl:template>
</xsl:stylesheet>
