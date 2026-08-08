<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:my="my" version="3.0">
  <xsl:include href="included.xsl"/>
  <xsl:template name="xsl:initial-template">
    <xsl:value-of select="my:copy('Hello, world!')"/>
  </xsl:template>
</xsl:stylesheet>
