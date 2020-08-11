<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:err="http://www.w3.org/2005/xqt-errors" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:array="http://www.w3.org/2005/xpath-functions/array" xmlns:map="http://www.w3.org/2005/xpath-functions/map"
   xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs mgx" xmlns:mgx="http://www.mediagenix.tv">
   <xsl:output method="json" encoding="UTF-8" indent="yes" media-type="application/json"/>
   <xsl:template match="/">
      <xsl:map>
         <xsl:map-entry key="'environmentVariables'" select="array{available-environment-variables()}"/>
         <xsl:map-entry key="'systemProperties'" select="array{available-system-properties()}"/>
      </xsl:map>
   </xsl:template>
</xsl:stylesheet>
