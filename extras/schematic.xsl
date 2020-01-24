<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to you under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
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

<!--
    Stylesheet to display the basic details of a JMX test plan
-->
<xsl:template match="jmeterTestPlan">
  <html>
  <title>Test Plan Schematic</title>
  <body>
  <xsl:apply-templates/>
  </body>
  </html>
</xsl:template>

<xsl:template match="hashTree">
  <ul>
     <xsl:apply-templates/>
  </ul>
</xsl:template>

<xsl:template match="TestPlan">
    <xsl:call-template name="header"/>
    <xsl:call-template name="comment"/>
    <xsl:for-each select='elementProp/collectionProp/elementProp'>
        <br/>
        <xsl:value-of select='stringProp[@name="Argument.name"]'/>
        <xsl:value-of select='stringProp[@name="Argument.metadata"]'/>
        <xsl:value-of select='stringProp[@name="Argument.value"]'/>
    </xsl:for-each>
</xsl:template>

<xsl:template match="ThreadGroup">
    <xsl:call-template name="header"/>
    <xsl:call-template name="comment"/>
    <br/>
    <xsl:text>Threads: </xsl:text>
    <xsl:value-of select='stringProp[@name="ThreadGroup.num_threads"]'/>
    <xsl:text> Loops: </xsl:text>
    <xsl:value-of select='elementProp/*[@name="LoopController.loops"]'/>
    <xsl:text> Ramp up: </xsl:text>
    <xsl:value-of select='stringProp[@name="ThreadGroup.ramp_time"]'/>
</xsl:template>

<xsl:template match="HTTPSampler|HTTPSampler2|ConfigTestElement[@guiclass='HttpDefaultsGui']">
    <xsl:call-template name="header"/>
    <xsl:call-template name="comment"/>
    <br/>
    <xsl:value-of select='stringProp[@name="HTTPSampler.method"]'/>
    <xsl:text> </xsl:text>
    <xsl:value-of select='stringProp[@name="HTTPSampler.protocol"]'/>
    <xsl:text>://</xsl:text>
    <xsl:value-of select='stringProp[@name="HTTPSampler.domain"]'/>
    <xsl:text>:</xsl:text>
    <xsl:value-of select='stringProp[@name="HTTPSampler.port"]'/>
    <xsl:text>/</xsl:text>
    <xsl:value-of select='stringProp[@name="HTTPSampler.path"]'/>
</xsl:template>

<xsl:template match="ResultCollector">
    <xsl:call-template name="header"/>
    <xsl:call-template name="comment"/>
    <xsl:if test='stringProp[@name="filename"]!=""'>
        <br/>
        Output: <xsl:value-of select='stringProp[@name="filename"]'/>
        XML: <xsl:value-of select='objProp/value/xml'/>
    </xsl:if>
</xsl:template>

<xsl:template match="*">
    <xsl:call-template name="header"/>
    <xsl:call-template name="comment"/>
</xsl:template>

<xsl:template name="comment">
    <xsl:if test='stringProp/@name="TestPlan.comments"'>
         <br/>
         <i>
          <xsl:value-of select='stringProp[@name="TestPlan.comments"]'/>
        </i>
    </xsl:if>
</xsl:template>

<xsl:template name="header">
    <xsl:if test="@enabled = 'false'">
(
    </xsl:if>
    <b>
    <xsl:choose>
        <xsl:when test="name() = 'GenericController'">
            <xsl:text>SimpleController</xsl:text>
        </xsl:when>
        <xsl:otherwise>
             <xsl:value-of select="name()"/>
        </xsl:otherwise>
    </xsl:choose>
</b> : <xsl:value-of select="@testname"/>
    <xsl:if test="@enabled = 'false'">
)
    </xsl:if>
</xsl:template>

</xsl:stylesheet>
