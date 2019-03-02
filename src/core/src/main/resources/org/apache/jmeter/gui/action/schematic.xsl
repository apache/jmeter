<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:jmeter="http://jmeter.apache.org/"
>

<xsl:strip-space elements="*"/>
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
    Stylesheet to display details of a JMX test plan for HTTP
-->
<xsl:template match="jmeterTestPlan">
  <html>
  <title>Schematic view of Test Plan</title>
  <head>
  <style>
ul.tree, ul.tree ul {
    list-style-type: none;
    background: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAAKAQMAAABPHKYJAAAAA1BMVEWIiIhYZW6zAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH1ggGExMZBky19AAAAAtJREFUCNdjYMAEAAAUAAHlhrBKAAAAAElFTkSuQmCC') repeat-y;
    margin: 0;
    padding: 0;
}
ul.tree ul {
    margin-left: 10px;
}
ul.tree li {
    margin: 0;
    padding: 0 12px;
    line-height: 20px;
    background:  url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAUAQMAAACK1e4oAAAABlBMVEUAAwCIiIgd2JB2AAAAAXRSTlMAQObYZgAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9YIBhQIJYVaFGwAAAARSURBVAjXY2hgQIf/GTDFGgDSkwqATqpCHAAAAABJRU5ErkJggg==') no-repeat;
    color: #369;
}
ul.tree li:last-child { 
    background: #fff url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAUAQMAAACK1e4oAAAABlBMVEUAAwCIiIgd2JB2AAAAAXRSTlMAQObYZgAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9YIBhQIIhs+gc8AAAAQSURBVAjXY2hgQIf/GbAAAKCTBYBUjWvCAAAAAElFTkSuQmCC') no-repeat; 
}
</style>
</head>
<body>
    <xsl:apply-templates/>
</body>
</html>
</xsl:template>

<!-- Remove empty nodes -->
<xsl:template match="hashTree[count(child::*) = 0]">
</xsl:template>

<xsl:template match="jmeterTestPlan/hashTree[1]">
    <ul class="tree" id="tree">
        <xsl:apply-templates/>
    </ul>
</xsl:template>

<xsl:template match="hashTree">
  <li><ul><xsl:apply-templates /></ul></li>
</xsl:template>

<xsl:template match="TestPlan">
    <li>
    <xsl:call-template name="header"/>
    (globalVars:[<xsl:for-each select='elementProp/collectionProp/elementProp'>
        "<xsl:value-of select='stringProp[@name="Argument.name"]'/>"
        <xsl:value-of select='stringProp[@name="Argument.metadata"]'/>
        "<xsl:value-of select='stringProp[@name="Argument.value"]'/>"
        <xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>], 
    executeTearDownThreadsOnShutdown: <xsl:value-of select='boolProp[@name="TestPlan.tearDown_on_shutdown"]'/>)
    <xsl:call-template name="comment"/>
    </li>
</xsl:template>

<xsl:template match="Arguments">
    <li>
    <xsl:call-template name="header"/>
    ([<xsl:for-each select='collectionProp/elementProp'>
        "<xsl:value-of select='stringProp[@name="Argument.name"]'/>"
        <xsl:value-of select='stringProp[@name="Argument.metadata"]'/>
        "<xsl:value-of select='stringProp[@name="Argument.value"]'/>"
        <xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>])
    <xsl:call-template name="comment"/>
    </li>
</xsl:template>

<xsl:template match="ThreadGroup|SetupThreadGroup|PostThreadGroup">
<li>
    <xsl:call-template name="header"/>
    (
    threads: "<xsl:value-of select='stringProp[@name="ThreadGroup.num_threads"]'/>",
    loops: "<xsl:value-of select='elementProp/*[@name="LoopController.loops"]'/>",
    ramp-up: "<xsl:value-of select='stringProp[@name="ThreadGroup.ramp_time"]'/>"
    )
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="CSVDataSet">
<li>
    <xsl:call-template name="header"/>
    (file: "<xsl:value-of select='stringProp[@name="filename"]'/>", 
    vars:"<xsl:value-of select='stringProp[@name="variableNames"]'/>",
    sharing:"<xsl:value-of select='stringProp[@name="shareMode"]'/>",
    recycleOnEof:<xsl:value-of select='boolProp[@name="recycle"]'/>,
    stopThreadOnEof:<xsl:value-of select='boolProp[@name="stopThread"]'/>
    )
    <xsl:call-template name="comment"/>
    <br/>
</li>
</xsl:template>

<xsl:template match="ThroughputController">
<li>
    <xsl:call-template name="header"/>
    (pct: "<xsl:value-of select='stringProp[@name="ThroughputController.percentThroughput"]'/>%")
    <xsl:call-template name="comment"/>
    <br/>
</li>
</xsl:template>

<xsl:template match="IfController">
<li>
    <xsl:call-template name="header"/>
    (condition: "<xsl:value-of select='stringProp[@name="IfController.condition"]'/>")
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="HTTPSamplerProxy">
<li>
    <xsl:call-template name="header"/>
    (method: "<xsl:value-of select='stringProp[@name="HTTPSampler.method"]'/>",
    url: "
    <xsl:value-of select='stringProp[@name="HTTPSampler.protocol"]'/>
    <xsl:text>://</xsl:text>
    <xsl:value-of select='stringProp[@name="HTTPSampler.domain"]'/>
    <xsl:text>:</xsl:text>
    <xsl:value-of select='stringProp[@name="HTTPSampler.port"]'/>
    <xsl:text>/</xsl:text>
    <xsl:value-of select='stringProp[@name="HTTPSampler.path"]'/>",
    <xsl:choose>
        <xsl:when test='boolProp[@name="HTTPSampler.postBodyRaw"] = "true"'>
            body: "<xsl:value-of select='elementProp/collectionProp/elementProp[@elementType="HTTPArgument"][1]/stringProp[@name="Argument.value"]'/>"
        </xsl:when>
        <xsl:otherwise>
            body: [
            <xsl:for-each select='elementProp[@name="HTTPsampler.Arguments"]/collectionProp/elementProp'>
              "<xsl:value-of select='stringProp[@name="Argument.name"]'/>"=
              "<xsl:value-of select='stringProp[@name="Argument.value"]'/>"
              <xsl:if test="position() != last()">,</xsl:if>
           </xsl:for-each>
           ]
        </xsl:otherwise>
    </xsl:choose>,
    upload-files: [
        <xsl:for-each select='elementProp[@name="HTTPsampler.Files"]/collectionProp/elementProp'>
            {param="<xsl:value-of select='stringProp[@name="File.paramname"]'/>",
            path="<xsl:value-of select='stringProp[@name="File.path"]'/>",
            mime-type="<xsl:value-of select='stringProp[@name="File.mimetype"]'/>"}
            <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each>
    ]
    )
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="CacheManager">
<li>
    <xsl:call-template name="header"/>
    (clearOnEachIteration: <xsl:value-of select='boolProp[@name="clearEachIteration"]'/>,
    useCacheControlAndExpires: <xsl:value-of select='boolProp[@name="useExpires"]'/>,
    maxSize: <xsl:value-of select='intProp[@name="maxSize"]'/>
    )
    <xsl:call-template name="comment"/>
    <xsl:call-template name="comment"/>
</li>
</xsl:template>
<xsl:template match="ConfigTestElement[@guiclass='HttpDefaultsGui']">
<li>
    <xsl:call-template name="header"/>
    (protocol: "<xsl:value-of select='stringProp[@name="HTTPSampler.protocol"]'/>",
    domain: "<xsl:value-of select='stringProp[@name="HTTPSampler.domain"]'/>",
    port: "<xsl:value-of select='stringProp[@name="HTTPSampler.port"]'/>",
    path: "<xsl:value-of select='stringProp[@name="HTTPSampler.path"]'/>",
    connectTimeout: "<xsl:value-of select='stringProp[@name="HTTPSampler.connect_timeout"]'/>"ms,
    responseTimeout: "<xsl:value-of select='stringProp[@name="HTTPSampler.response_timeout"]'/>"ms
    )
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="ResultCollector">
<li>
    <xsl:call-template name="header"/>
    <xsl:if test='stringProp[@name="filename"]!=""'>
        (filename: "<xsl:value-of select='stringProp[@name="filename"]'/>",
        xml: <xsl:value-of select='objProp/value/xml'/>,
        errorsOnly: <xsl:value-of select='boolProp[@name="ResultCollector.error_logging"]'/>,
        successOnly: <xsl:value-of select='boolProp[@name="ResultCollector.success_only_logging"]'/>)
    </xsl:if>
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="ResponseAssertion">
<li>
    <xsl:call-template name="header"/>
    <b> that (<xsl:value-of select='substring(stringProp[@name="Assertion.test_field"], 11)'/>)</b>
    <b>
    <xsl:choose>
        <xsl:when test='intProp[@name="Assertion.test_type"] = 1'>
            matches
        </xsl:when>
        <xsl:when test='intProp[@name="Assertion.test_type"] = 2'>
            contains
        </xsl:when>
        <xsl:when test='intProp[@name="Assertion.test_type"] = 5'>
            does not match
        </xsl:when>
        <xsl:when test='intProp[@name="Assertion.test_type"] = 6'>
            does not contain
        </xsl:when>
        <xsl:when test='intProp[@name="Assertion.test_type"] = 8'>
            is equal to
        </xsl:when>
        <xsl:when test='intProp[@name="Assertion.test_type"] = 12'>
            is not equal to
        </xsl:when>
        <xsl:when test='intProp[@name="Assertion.test_type"] = 16'>
            contains 
        </xsl:when>
        <xsl:when test='intProp[@name="Assertion.test_type"] = 20'>
            does not contain as substring
        </xsl:when>
        <xsl:when test='intProp[@name="Assertion.test_type"] = 37'>
            does not matches one of
        </xsl:when>
        <xsl:when test='intProp[@name="Assertion.test_type"] = 38'>
            does not contain one of
        </xsl:when>
        <xsl:when test='intProp[@name="Assertion.test_type"] = 44'>
            is not equal to one of
        </xsl:when>
        <xsl:when test='intProp[@name="Assertion.test_type"] = 48'>
            contains one of
        </xsl:when>
        <xsl:when test='intProp[@name="Assertion.test_type"] = 52'>
            does not contain as substring one of
        </xsl:when>
    </xsl:choose>
    </b>
    [
    <xsl:for-each select='collectionProp[@name="Asserion.test_strings"]/stringProp'>
      "<xsl:value-of select='.'/>"
      <xsl:if test="position() != last()">,</xsl:if>
   </xsl:for-each>
    ])
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="TestAction">
<li>
    <xsl:call-template name="header"/>
    <b>
    <xsl:choose>
        <xsl:when test='intProp[@name="ActionProcessor.action"] = 0'>
            stop 
        </xsl:when>
        <xsl:when test='intProp[@name="ActionProcessor.action"] = 1'>
            pause for <xsl:value-of select='stringProp[@name="ActionProcessor.duration"]'/>ms
        </xsl:when>
        <xsl:when test='intProp[@name="ActionProcessor.action"] = 2'>
            stop now
        </xsl:when>
        <xsl:when test='intProp[@name="ActionProcessor.action"] = 3'>
            go to next iteration of thread loop
        </xsl:when>
        <xsl:when test='intProp[@name="ActionProcessor.action"] = 4'>
            go to next iteration of current loop
        </xsl:when>
        <xsl:when test='intProp[@name="ActionProcessor.action"] = 5'>
            break current loop
        </xsl:when>
    </xsl:choose>
    <xsl:choose>
        <xsl:when test='intProp[@name="ActionProcessor.target"] = 0'>
            current thread
        </xsl:when>
        <xsl:when test='intProp[@name="ActionProcessor.target"] = 2'>
            test
        </xsl:when>
    </xsl:choose>
    </b>
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="ResultAction">
<li>
    <xsl:call-template name="header"/>
    <b>
    <xsl:choose>
        <xsl:when test='intProp[@name="OnError.action"] = 0'>
            continue 
        </xsl:when>
        <xsl:when test='intProp[@name="OnError.action"] = 1'>
            stop thread now
        </xsl:when>
        <xsl:when test='intProp[@name="OnError.action"] = 2'>
            shutdown test
        </xsl:when>
        <xsl:when test='intProp[@name="OnError.action"] = 3'>
            stop test now
        </xsl:when>
        <xsl:when test='intProp[@name="OnError.action"] = 4'>
            go to next iteration of thread loop
        </xsl:when>
        <xsl:when test='intProp[@name="OnError.action"] = 5'>
            go to next iteration of current loop
        </xsl:when>
        <xsl:when test='intProp[@name="OnError.action"] = 6'>
            break current loop
        </xsl:when>
    </xsl:choose>
    </b>
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="UserParameters">
<li>
    <xsl:call-template name="header"/>
    (names=[
   <xsl:for-each select='collectionProp[@name="UserParameters.names"]/stringProp'>
        "<xsl:value-of select='.'/>"
         <xsl:if test="position() != last()">,</xsl:if>
   </xsl:for-each>],values=[
   <xsl:for-each select='collectionProp[@name="UserParameters.thread_values"]/collectionProp/stringProp'>
        "<xsl:value-of select='.'/>"
         <xsl:if test="position() != last()">,</xsl:if>
   </xsl:for-each>]
    )
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="ModuleController">
<li>
    <xsl:call-template name="header"/>
    (
    <xsl:for-each select='collectionProp/stringProp'>
        <xsl:if test="position()!=1 and position() != last()">
            <xsl:value-of select='.'/> 
        </xsl:if>
        <xsl:if test="position() = last()">
            <a><xsl:attribute name="href">#<xsl:value-of select='.'/></xsl:attribute><xsl:value-of select='.'/></a>
        </xsl:if>
        <xsl:if test="position()!=1 and position() != last()">
            &gt;
        </xsl:if>
   </xsl:for-each>
    )
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="CookieManager">
<li>
    <xsl:call-template name="header"/>
    (clearOnEachIteration: <xsl:value-of select='boolProp[@name="CookieManager.clearEachIteration"]'/>,
    policy: "<xsl:value-of select='stringProp[@name="CookieManager.policy"]'/>",
    cookies: [
    <xsl:for-each select='collectionProp[@name="CookieManager.cookies"]/elementProp[@elementType="Cookie"]'>
      { name:"<xsl:value-of select='@name'/>", 
      value:"<xsl:value-of select='stringProp[@name="Cookie.value"]'/>", 
      domain:"<xsl:value-of select='stringProp[@name="Cookie.domain"]'/>",
      path:"<xsl:value-of select='stringProp[@name="Cookie.path"]'/>",
      secure:<xsl:value-of select='boolProp[@name="Cookie.secury"]'/>}
      <xsl:if test="position() != last()">,</xsl:if>
   </xsl:for-each>])
   <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="HeaderManager">
<li>
    <xsl:call-template name="header"/>
    (headers:[
    <xsl:for-each select='collectionProp[@name="HeaderManager.headers"]/elementProp'>
        {"<xsl:value-of select='stringProp[@name="Header.name"]'/>"=
        "<xsl:value-of select='stringProp[@name="Header.value"]'/>"
        <xsl:if test="position() != last()">,</xsl:if>
    </xsl:for-each>])
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:function name="jmeter:showScope">
    <xsl:param name="scope"/>
    <xsl:choose>
        <xsl:when test="$scope = 'true'">
            using response headers
        </xsl:when>
        <xsl:when test="$scope = 'false'">
            using response body
        </xsl:when>
        <xsl:when test="$scope = 'request_headers'">
            using request headers
        </xsl:when>
        <xsl:when test="$scope = 'unescaped'">
            using response body unescaped
        </xsl:when>
        <xsl:when test="$scope = 'URL'">
            using URL
        </xsl:when>
        <xsl:when test="$scope = 'code'">
            using response code
        </xsl:when>
        <xsl:when test="$scope = 'message'">
            using response message
        </xsl:when>
    </xsl:choose>
</xsl:function>
  
<xsl:template match="XPath2Extractor">
<li>
    <xsl:call-template name="header"/>
    <b>using response body</b>
    (exportedVar: "<xsl:value-of select='stringProp[@name="XPathExtractor2.refname"]'/>",
    xpathQuery: "<xsl:value-of select='stringProp[@name="XPathExtractor2.xpathQuery"]'/>",
    fragment: "<xsl:value-of select='stringProp[@name="XPathExtractor2.fragment"]'/>",
    default: "<xsl:value-of select='stringProp[@name="XPathExtractor2.default"]'/>",
    matchNr: "<xsl:value-of select='stringProp[@name="XPathExtractor2.matchNumber"]'/>")
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="XPathExtractor">
<li>
    <xsl:call-template name="header"/>
    <b>using response body</b>
    (exportedVar: "<xsl:value-of select='stringProp[@name="XPathExtractor.refname"]'/>",
    xpathQuery: "<xsl:value-of select='stringProp[@name="XPathExtractor.xpathQuery"]'/>",
    fragment: "<xsl:value-of select='stringProp[@name="XPathExtractor.fragment"]'/>",
    default: "<xsl:value-of select='stringProp[@name="XPathExtractor.default"]'/>",
    matchNr: "<xsl:value-of select='stringProp[@name="XPathExtractor.matchNumber"]'/>")
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="HtmlExtractor">
<li>
    <xsl:call-template name="header"/>
    <b>using response body</b>
    (exportedVar: "<xsl:value-of select='stringProp[@name="HtmlExtractor.refname"]'/>",
    selector: "<xsl:value-of select='stringProp[@name="HtmlExtractor.expr"]'/>",
    attribute: "<xsl:value-of select='stringProp[@name="HtmlExtractor.attribute"]'/>",
    default: "<xsl:value-of select='stringProp[@name="HtmlExtractor.default"]'/>",
    matchNr: "<xsl:value-of select='stringProp[@name="HtmlExtractor.match_number"]'/>")
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="JSONPostProcessor">
<li>
    <xsl:call-template name="header"/>
    <b>using response body</b>
    (exportedVar: "<xsl:value-of select='stringProp[@name="JSONPostProcessor.referenceNames"]'/>",
    jsonPaths: "<xsl:value-of select='stringProp[@name="JSONPostProcessor.jsonPathExprs"]'/>",
    default: "<xsl:value-of select='stringProp[@name="JSONPostProcessor.defaultValues"]'/>",
    matchNr: "<xsl:value-of select='stringProp[@name="JSONPostProcessor.match_numbers"]'/>")
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="BoundaryExtractor">
<li>
    <xsl:call-template name="header"/>
    <b><xsl:value-of select='jmeter:showScope(stringProp[@name="BoundaryExtractor.useHeaders"])'/></b>
    (exportedVar: "<xsl:value-of select='stringProp[@name="BoundaryExtractor.refname"]'/>",
    lboundary: "<xsl:value-of select='stringProp[@name="BoundaryExtractor.lboundary"]'/>",
    rboundary: "<xsl:value-of select='stringProp[@name="BoundaryExtractor.rboundary"]'/>",
    default: "<xsl:value-of select='stringProp[@name="BoundaryExtractor.default"]'/>",
    matchNr: "<xsl:value-of select='stringProp[@name="BoundaryExtractor.match_number"]'/>")
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="RegexExtractor">
<li>
    <xsl:call-template name="header"/>
    <b><xsl:value-of select='jmeter:showScope(stringProp[@name="RegexExtractor.useHeaders"])'/></b>
    (exportedVar: "<xsl:value-of select='stringProp[@name="RegexExtractor.refname"]'/>"
    regex: "<xsl:value-of select='stringProp[@name="BoundaryExtractor.regex"]'/>",
    template: "<xsl:value-of select='stringProp[@name="BoundaryExtractor.template"]'/>",
    default: "<xsl:value-of select='stringProp[@name="BoundaryExtractor.default"]'/>",
    matchNr: "<xsl:value-of select='stringProp[@name="BoundaryExtractor.match_number"]'/>")
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="ConstantTimer">
<li>
    <xsl:call-template name="header"/>
    (const:<xsl:value-of select='stringProp[@name="ConstantTimer.delay"]'/>ms)
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="UniformRandomTimer|GaussianRandomTimer|PoissonRandomTimer">
<li>
    <xsl:call-template name="header"/>
    (const:<xsl:value-of select='stringProp[@name="ConstantTimer.delay"]'/>ms, 
    variation:<xsl:value-of select='stringProp[@name="RandomTimer.range"]'/>ms)
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template match="*">
<li>
    <xsl:call-template name="header"/>
    <xsl:call-template name="comment"/>
</li>
</xsl:template>

<xsl:template name="comment">
    <xsl:if test='stringProp/@name="TestPlan.comments"'>
         <br/>
         &#160;&#160;&#160;Comments: <i style="color:green">
          <xsl:value-of select='stringProp[@name="TestPlan.comments"]'/>
        </i>
    </xsl:if>
</xsl:template>

<xsl:template name="header">
    <xsl:choose>
        <xsl:when test="name() = 'GenericController'">
            <a> 
            <xsl:attribute name="id">
                <xsl:value-of select="@testname"/>
            </xsl:attribute>
            <b>container</b></a> 
        </xsl:when>
        <xsl:when test="name() = 'HTTPSamplerProxy'">
            <b>http request</b> 
        </xsl:when>
        <xsl:when test="name() = 'SetupThreadGroup'">
            <b>run before</b> 
        </xsl:when>
        <xsl:when test="name() = 'ThreadGroup'">
            <b>run</b> 
        </xsl:when>
        <xsl:when test="name() = 'PostThreadGroup'">
            <b>run after</b> 
        </xsl:when>
        <xsl:when test="name() = 'TestFragmentController'">
            <b>declare reusable Elements</b> 
        </xsl:when>
        <xsl:when test="name() = 'ResponseAssertion'">
            <b>assert as</b> 
        </xsl:when>
        <xsl:when test="name() = 'TransactionController'">
            <a>
            <xsl:attribute name="id">
                <xsl:value-of select="@testname"/>
            </xsl:attribute>
            <b>transaction</b></a> 
        </xsl:when>
        <xsl:when test="name() = 'ModuleController'">
            <b>reuse controller</b> 
        </xsl:when>
        <xsl:when test="name() = 'UserParameters'">
            <b>set variables for thread</b> 
        </xsl:when>
        <xsl:when test="name() = 'RandomController'">
            <a>
            <xsl:attribute name="id">
                <xsl:value-of select="@testname"/>
            </xsl:attribute>
            <b>randomly run children</b></a> 
        </xsl:when>
        <xsl:when test="name() = 'ThroughputController'">
            <a>
            <xsl:attribute name="id">
                <xsl:value-of select="@testname"/>
            </xsl:attribute>
            <b>run at percentage</b></a> 
        </xsl:when>
        <xsl:when test="name() = 'IfController'">
            <a>
            <xsl:attribute name="id">
                <xsl:value-of select="@testname"/>
            </xsl:attribute>
            <b>if</b></a> 
        </xsl:when>
        <xsl:when test="name() = 'ResultCollector'">
            <b>write samples</b> 
        </xsl:when>
        <xsl:when test="name() = 'ConstantTimer'">
            <b>think-time Constant</b> 
        </xsl:when>
        <xsl:when test="name() = 'UniformRandomTimer'
            or name() = 'GaussianRandomTimer'
            or name() = 'PoissonRandomTimer'">
            <b>think-time <xsl:value-of select="substring-before(name(),'RandomTimer')" /></b> 
        </xsl:when>
        <xsl:when test="name() = 'JSR223Sampler'">
            <b>jsr223 sampler</b> 
        </xsl:when>
        <xsl:when test="name() = 'JSR223PreProcessor'">
            <b>jsr223 pre-process</b> 
        </xsl:when>
        <xsl:when test="name() = 'JSR223PostProcessor'">
            <b>jsr223 post-process</b> 
        </xsl:when>
        <xsl:when test="name() = 'CSVDataSet'">
            <b>read csv into vars</b> 
        </xsl:when>
        <xsl:when test="name() = 'HeaderManager'">
            <b>add headers</b> 
        </xsl:when>
        <xsl:when test="name() = 'CookieManager'">
            <b>handle cookies</b> 
        </xsl:when>
        <xsl:when test="name() = 'Arguments'">
            <b>globalVars</b> 
        </xsl:when>
        <xsl:when test="name() = 'TestAction'">
            <b>flow control</b> 
        </xsl:when>
        <xsl:when test="name() = 'ResultAction'">
            <b>action after sampler error </b> 
        </xsl:when>
        <xsl:when test="name() = 'ConfigTestElement' and @guiclass = 'HttpDefaultsGui'">
            <b>http defaults</b> 
        </xsl:when>
        <xsl:when test="name() = 'CacheManager'">
            <b>simulate browser cache</b> 
        </xsl:when>
        <xsl:when test="name() = 'RegexExtractor'">
            <b>extract-regexp </b> 
        </xsl:when>
        <xsl:when test="name() = 'BoundaryExtractor'">
            <b>extract-boundary </b>
        </xsl:when>
        <xsl:when test="name() = 'HtmlExtractor'">
            <b>extract-css-selector </b>
        </xsl:when>
        <xsl:when test="name() = 'XPathExtractor'">
            <b>extract-xpath </b>
        </xsl:when>
        <xsl:when test="name() = 'XPath2Extractor'">
            <b>extract-xpath2 </b>
        </xsl:when>
        <xsl:when test="name() = 'JSONPostProcessor'">
            <b>extract-jsonpath </b>
        </xsl:when>
        <xsl:when test="name() = 'TestPlan'">
            <b>load test </b>
        </xsl:when>
        <xsl:otherwise>
             <b><xsl:value-of select="name()"/></b> 
        </xsl:otherwise>
    </xsl:choose>
    "<xsl:value-of select="@testname"/>" <xsl:call-template name="disabledElement"/>
</xsl:template>

<xsl:template name="disabledElement">
    <xsl:if test="@enabled = 'false'">
        <del>(disabled)</del>&#160;
    </xsl:if>
</xsl:template>
</xsl:stylesheet>