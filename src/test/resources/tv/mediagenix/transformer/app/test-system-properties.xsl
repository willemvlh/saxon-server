<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs mgx" xmlns:mgx="http://www.mediagenix.tv">
   <xsl:output method="xml" encoding="UTF-8" indent="yes" media-type="application/json"/>
   <xsl:template match="/" name="xsl:initial-template">
      <result>
         <environment-variables>
            <xsl:for-each select="available-environment-variables()">
               <xsl:value-of select="system-property('xsl:vendor')"/>
               <var name="{.}"/>
            </xsl:for-each>
         </environment-variables>
         <system-properties>
            <xsl:for-each select="available-system-properties()">
               <prop name="{.}"/>
            </xsl:for-each>
         </system-properties>
      </result>
   </xsl:template>
</xsl:stylesheet>
