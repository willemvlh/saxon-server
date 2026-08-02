<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0">
   <xsl:template match="/" name="xsl:initial-template">
      <xsl:copy-of select="unparsed-text('http://www.example.com')"/>
   </xsl:template>
</xsl:stylesheet>
