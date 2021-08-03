<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:err="http://www.w3.org/2005/xqt-errors" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:array="http://www.w3.org/2005/xpath-functions/array" xmlns:map="http://www.w3.org/2005/xpath-functions/map"
   xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs mgx" xmlns:mgx="http://www.mediagenix.tv">
   <xsl:output method="json" encoding="UTF-8" indent="yes" media-type="application/json"/>
   <xsl:template match="/">
      <xsl:map>
         <xsl:for-each select="(unparsed-text#1, doc#1, json-doc#1)">
            <xsl:map-entry key="function-name(.)" select="mgx:url(.)"/>   
         </xsl:for-each>
      </xsl:map>
   </xsl:template>
   
   <xsl:function name="mgx:url">
      <xsl:param name="function"/>
      <xsl:try>
         <xsl:sequence select="apply($function, array{'file:////home'})"/>
         <xsl:catch>
            <xsl:sequence select="$err:code"/>
         </xsl:catch>
      </xsl:try>
      
      
   </xsl:function>
</xsl:stylesheet>
