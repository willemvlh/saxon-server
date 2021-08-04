<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0">
   <xsl:output encoding="UTF-8" method="text"/>
   <xsl:template match="/" name="xsl:initial-template">
      <xsl:result-document href="pong.txt" method="text">
         bla
      </xsl:result-document>
   </xsl:template>
</xsl:stylesheet>