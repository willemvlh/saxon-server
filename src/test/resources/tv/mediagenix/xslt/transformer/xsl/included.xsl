<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:my="my" version="3.0">
  <xsl:function name="my:copy">
    <xsl:param name="input"/>
    <xsl:sequence select="$input"/>
  </xsl:function>
</xsl:stylesheet>
