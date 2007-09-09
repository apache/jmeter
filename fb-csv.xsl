<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

<xsl:output method="text"/>

<xsl:template match="/BugCollection" >
	<xsl:text>Priority,Type,Classname,Method,Field,SourceLine
</xsl:text>
  <xsl:for-each select="BugInstance">
   	<xsl:value-of select="@priority"/>
   	<xsl:text>,</xsl:text>

    <xsl:value-of select="@type"/>
    <xsl:text>,</xsl:text>
    
    <xsl:for-each select="Class">
   	  <xsl:value-of select="@classname"/>
    </xsl:for-each>
    <xsl:text>,</xsl:text>
    
    <xsl:for-each select="Method">
   	  <xsl:value-of select="@name"/>
   	  <xsl:value-of select="@signature"/>
    </xsl:for-each>
    <xsl:text>,</xsl:text>
    
    <xsl:for-each select="Field">
   	  <xsl:value-of select="@name"/>
    </xsl:for-each>
    <xsl:text>,</xsl:text>
    
    <xsl:for-each select="SourceLine">
   	  <xsl:value-of select="@sourcefile"/>
      <xsl:text>(start:</xsl:text><xsl:value-of select="@start"/><xsl:text>)</xsl:text>
    </xsl:for-each>
    
	<xsl:text>
</xsl:text>
  </xsl:for-each>
</xsl:template>

</xsl:stylesheet>
