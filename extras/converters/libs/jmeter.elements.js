/*jslint white: true */
/*jslint es6 */

/*global encodeXml */

"use strict";

//----------------------------------------------------
// JMETER ELEMENTS
//----------------------------------------------------


//----------------------------------------------------
// TEST PLAN
//----------------------------------------------------
function printTestPlan (threadGroupsPrepared,comment) {
 var outputTxt=`<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.1.1 r1855137">

  <hashTree> <!-- under jmeterTestPlan -->
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="Test Plan" enabled="true">
      <stringProp name="TestPlan.comments">${comment}</stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.tearDown_on_shutdown">true</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments" guiclass="ArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
    </TestPlan>

  <hashTree> <!-- under TestPlan -->
${threadGroupsPrepared}
  </hashTree> <!-- end of tree under TestPlan -->

</hashTree> <!-- end of tree under jmeterTestPlan -->

</jmeterTestPlan>

 `;
 return outputTxt;
}


//----------------------------------------------------
// THREAD GROUP
//----------------------------------------------------
function printThreadGroup(name, enabled, threadGroupBody, comment) {
 var outputTxt=`
    <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="${name}" enabled="${enabled}">
      <stringProp name="TestPlan.comments">${comment}</stringProp>
      <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
      <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" testname="Loop Controller" enabled="true">
        <boolProp name="LoopController.continue_forever">false</boolProp>
        <stringProp name="LoopController.loops">1</stringProp>
      </elementProp>
      <stringProp name="ThreadGroup.num_threads">1</stringProp>
      <stringProp name="ThreadGroup.ramp_time">1</stringProp>
      <boolProp name="ThreadGroup.scheduler">false</boolProp>
      <stringProp name="ThreadGroup.duration"></stringProp>
      <stringProp name="ThreadGroup.delay"></stringProp>
    </ThreadGroup>
      <hashTree> <!-- under ThreadGroup -->

${threadGroupBody}

     </hashTree> <!-- end of tree under ThreadGroup -->
`;
 return outputTxt;
}


//----------------------------------------------------
// USER DEFINED VARIABLES
//----------------------------------------------------
function printArguments(name, enabled, argumentsPrepared, comment) {
 var outputTxt=`
      <Arguments guiclass="ArgumentsPanel" testclass="Arguments" testname="${name}" enabled="${enabled}">
        <stringProp name="TestPlan.comments">${comment}</stringProp>
        <collectionProp name="Arguments.arguments">
${argumentsPrepared}
        </collectionProp>
      </Arguments>
      <hashTree/> <!-- Arguments -->
`;
 return outputTxt;
}


//----------------------------------------------------
// ARGUMENTS (USER DEFINED VARIABLES)
//----------------------------------------------------
function printArgument(argumentName, argumentValue) {
 var outputTxt=`
          <elementProp name="${argumentName}" elementType="Argument">
            <stringProp name="Argument.name">${argumentName}</stringProp>
            <stringProp name="Argument.value">${argumentValue}</stringProp>
            <stringProp name="Argument.metadata">=</stringProp>
          </elementProp>
`;
 return outputTxt;
}


//----------------------------------------------------
// GENERIC CONTROLLER
//----------------------------------------------------
function printGenericController(name, enabled, samplersPrepared,comment) {
 var outputTxt=`
        <GenericController guiclass="LogicControllerGui" testclass="GenericController" testname="${name}" enabled="${enabled}">
          <stringProp name="TestPlan.comments">${comment}</stringProp>
>
        </GenericController>

        <hashTree>
${samplersPrepared}
        </hashTree>
`;
 return outputTxt;
}


//----------------------------------------------------
// JSR SAMPLER
//----------------------------------------------------
function printJSR223Sampler(name, enabled, script, comment) {
 name=encodeXml(name);
 script=encodeXml(script);
 var outputTxt=`
      <JSR223Sampler guiclass="TestBeanGUI" testclass="JSR223Sampler" testname="${name}" enabled="${enabled}">
        <stringProp name="TestPlan.comments">${comment}</stringProp>
        <stringProp name="cacheKey">true</stringProp>
        <stringProp name="filename"/>
        <stringProp name="parameters"/>
        <stringProp name="script">
/*
${script}
*/
        </stringProp>
        <stringProp name="scriptLanguage">groovy</stringProp>
      </JSR223Sampler>
      <hashTree/> <!-- JSR223Sampler -->
`;
 return outputTxt;
}


//----------------------------------------------------
// JDBC SAMPLER
//----------------------------------------------------
function printJDBCSampler(name, enabled, dataSource, query, queryResultVar, comment) {
 name=encodeXml(name);
 dataSource=encodeXml(dataSource);
 query=encodeXml(query);
 
 var outputTxt=`
          <JDBCSampler guiclass="TestBeanGUI" testclass="JDBCSampler" testname="${name}" enabled="${enabled}">
            <stringProp name="dataSource">${dataSource}</stringProp>
            <stringProp name="query">${query}</stringProp>
            <stringProp name="queryArguments"></stringProp>
            <stringProp name="queryArgumentsTypes"></stringProp>
            <stringProp name="queryTimeout"></stringProp>
            <stringProp name="queryType">Select Statement</stringProp>
            <stringProp name="resultSetHandler">Store as String</stringProp>
            <stringProp name="resultVariable">${queryResultVar}</stringProp>
            <stringProp name="variableNames"></stringProp>
            <stringProp name="TestPlan.comments">${comment}</stringProp>
          </JDBCSampler>
          <hashTree/> <!-- JDBCSampler -->
`;
 return outputTxt;
}


//----------------------------------------------------
// MODULE CONROLLER
//----------------------------------------------------
function printModuleController(name, enabled, path, comment) {
 name=encodeXml(name);
 var timestamp=Date.now();
 var outputTxt=`
        <ModuleController guiclass="ModuleControllerGui" testclass="ModuleController" testname="${name}" enabled="${enabled}">
          <stringProp name="TestPlan.comments">${comment}</stringProp>
          <collectionProp name="ModuleController.node_path">
            <stringProp name="${timestamp}">Test Plan</stringProp>
            <stringProp name="${timestamp}">Test Plan</stringProp>
            ${path}
          </collectionProp>
        </ModuleController>
        <hashTree/> <!-- ModuleController -->
`;
 return outputTxt;
}



//----------------------------------------------------
// INCLUDE CONROLLER
//----------------------------------------------------
function printIncludeController(name, enabled, path, comment) {
 name=encodeXml(name);
 path=encodeXml(path);
 var outputTxt=`
      <IncludeController guiclass="IncludeControllerGui" testclass="IncludeController" testname="${name}" enabled="${enabled}">
        <stringProp name="TestPlan.comments">${comment}</stringProp>
        <stringProp name="IncludeController.includepath">${path}</stringProp>
      </IncludeController>
      <hashTree/> <!-- IncludeController -->
`;
 return outputTxt;
}

//----------------------------------------------------
// XPATH 2 ASSERTION
//----------------------------------------------------
function printXPath2Assertion(name, enabled, xpathQuery, namespaces, comment) {
 name=encodeXml(name);
 xpathQuery=encodeXml(xpathQuery);
 var outputTxt=`
            <XPath2Assertion guiclass="XPath2AssertionGui" testclass="XPath2Assertion" testname="${name}" enabled="${enabled}">
              <boolProp name="XPath.negate">false</boolProp>
              <stringProp name="XPath.xpath">${xpathQuery}</stringProp>
              <stringProp name="XPath.namespaces">${namespaces}</stringProp>
              <stringProp name="TestPlan.comments">${comment}</stringProp>
            </XPath2Assertion>
            <hashTree/>
`;
 return outputTxt;
}


//----------------------------------------------------
// XPATH 2 EXTRACTOR
//----------------------------------------------------
function printXPath2Extractor(name, enabled, defaultVar, varName, xpathQuery, namespaces, comment) {
 name=encodeXml(name);
 var outputTxt=`
            <XPath2Extractor guiclass="XPath2ExtractorGui" testclass="XPath2Extractor" testname="${name}" enabled="${enabled}">
              <stringProp name="XPathExtractor2.default">${defaultVar}</stringProp>
              <stringProp name="XPathExtractor2.refname">${varName}</stringProp>
              <stringProp name="XPathExtractor2.matchNumber">0</stringProp>
              <stringProp name="XPathExtractor2.xpathQuery">${xpathQuery}</stringProp>
              <stringProp name="XPathExtractor2.namespaces">${namespaces}</stringProp>
              <stringProp name="TestPlan.comments">${comment}</stringProp>
            </XPath2Extractor>
            <hashTree/>
`;
 return outputTxt;
}

//----------------------------------------------------
// HTTP SAMPLER PROXY
//----------------------------------------------------
function printHTTPSamplerProxy(name, enabled, domain, port, protocol, path, encoding, method, reqArgs, reqSubelements, comment) {
 name=encodeXml(name);
 var outputTxt=`
      <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="${name}" enabled="${enabled}">
${reqArgs}
        <stringProp name="TestPlan.comments">${comment}</stringProp>
        <stringProp name="HTTPSampler.domain">${domain}</stringProp>
        <stringProp name="HTTPSampler.port">${port}</stringProp>
        <stringProp name="HTTPSampler.protocol">${protocol}</stringProp>
        <stringProp name="HTTPSampler.contentEncoding">${encoding}</stringProp>
        <stringProp name="HTTPSampler.path">${path}</stringProp>
        <stringProp name="HTTPSampler.method">${method}</stringProp>
        <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
        <boolProp name="HTTPSampler.auto_redirects">false</boolProp>
        <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
        <boolProp name="HTTPSampler.DO_MULTIPART_POST">false</boolProp>
        <stringProp name="HTTPSampler.embedded_url_re"></stringProp>
        <stringProp name="HTTPSampler.connect_timeout"></stringProp>
        <stringProp name="HTTPSampler.response_timeout"></stringProp>
      </HTTPSamplerProxy>
      <hashTree> <!-- under HTTPSamplerProxy -->
${reqSubelements}
      </hashTree> <!-- HTTPSamplerProxy -->
`;
 return outputTxt;
}


//----------------------------------------------------
// GET ARGUMENTS (HTTP SAMPLER PROXY)
//----------------------------------------------------
function printReqArgsGet(paramsPrepared) {
 var outputTxt=`
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments" guiclass="HTTPArgumentsPanel" testclass="Arguments" enabled="true">
            <collectionProp name="Arguments.arguments">
${paramsPrepared}
            </collectionProp>
          </elementProp>
`;
 return outputTxt;
}


//----------------------------------------------------
// POST (HTTP SAMPLER PROXY)
//----------------------------------------------------
function printQueryDataParam(paramName,paramValue) {
 paramName=encodeXml(paramName);
 paramValue=encodeXml(paramValue);
 var outputTxt=`
              <elementProp name="${paramName}" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.always_encode">true</boolProp>
                <stringProp name="Argument.name">${paramName}</stringProp>
                <stringProp name="Argument.value">${paramValue}</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
                <boolProp name="HTTPArgument.use_equals">true</boolProp>
              </elementProp>
`;
 return outputTxt;
}


//----------------------------------------------------
// POST RAW DATA BODY (HTTP SAMPLER PROXY)
//----------------------------------------------------
function printReqArgsPostDataRaw(postTxt) {
 postTxt=encodeXml(postTxt);
 var outputTxt=`
        <boolProp name="HTTPSampler.postBodyRaw">true</boolProp>
        <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
          <collectionProp name="Arguments.arguments">
            <elementProp name="" elementType="HTTPArgument">
              <boolProp name="HTTPArgument.always_encode">false</boolProp>
              <stringProp name="Argument.value">${postTxt}</stringProp>
              <stringProp name="Argument.metadata">=</stringProp>
            </elementProp>
          </collectionProp>
        </elementProp>
`;
 return outputTxt;
}


//----------------------------------------------------
// POST PARMETERS LIST (HTTP SAMPLER PROXY)
//----------------------------------------------------
function printReqArgsPostDataParams(paramsPrepared) {
 var outputTxt=`
         <elementProp name="HTTPsampler.Arguments" elementType="Arguments" guiclass="HTTPArgumentsPanel" testclass="Arguments" enabled="true">
            <collectionProp name="Arguments.arguments">
${paramsPrepared}
            </collectionProp>
          </elementProp>
`;
 return outputTxt;
}

//----------------------------------------------------
// POST PARMETER (POST PARMETERS LIST)
//----------------------------------------------------
function printPostDataParam(paramName,paramValue) {
 paramName=encodeXml(paramName);
 paramValue=encodeXml(paramValue);
 var outputTxt=`
              <elementProp name="${paramName}" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.name">${paramName}</stringProp>
                <stringProp name="Argument.value">${paramValue}</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
                <boolProp name="HTTPArgument.use_equals">true</boolProp>
              </elementProp>
`;
 return outputTxt;
}

//----------------------------------------------------
// HEADER MANAGER - empty
//----------------------------------------------------
function printHeaderManagerEmpty(comment) {
 var outputTxt=`
      <HeaderManager guiclass="HeaderPanel" testclass="HeaderManager" testname="HTTP Header Manager" enabled="true">
        <stringProp name="TestPlan.comments">${comment}</stringProp>
        <collectionProp name="HeaderManager.headers"/>
      </HeaderManager>
      <hashTree/>
`;
 return outputTxt;
}


//----------------------------------------------------
// HEADER MANGER
//----------------------------------------------------
function printHeaderManager(headersPrepared, comment) {
 var outputTxt=`
        <HeaderManager guiclass="HeaderPanel" testclass="HeaderManager" testname="HTTP Header Manager" enabled="true">
          <stringProp name="TestPlan.comments">${comment}</stringProp>
          <collectionProp name="HeaderManager.headers">
${headersPrepared}
          </collectionProp>
        </HeaderManager>
      <hashTree/> <!-- end of tree under HeaderManager -->
`;
 return outputTxt;
}


//----------------------------------------------------
// HEADER (HEADER MANGER)
//----------------------------------------------------
function printHeader (headerName,headerValue) {
 headerName=encodeXml(headerName);
 headerValue=encodeXml(headerValue);
 var outputTxt=`
            <elementProp name="${headerName}" elementType="Header">
              <stringProp name="Header.name">${headerName}</stringProp>
              <stringProp name="Header.value">${headerValue}</stringProp>
            </elementProp>
`;
 return outputTxt;
}


//----------------------------------------------------
// COOKIE MANAGER - empty
//----------------------------------------------------
function printCookieManagerEmpty(comment) {
 var outputTxt=`
      <CookieManager guiclass="CookiePanel" testclass="CookieManager" testname="HTTP Cookie Manager" enabled="true">
        <stringProp name="TestPlan.comments">${comment}</stringProp>
        <collectionProp name="CookieManager.cookies"/>
        <boolProp name="CookieManager.clearEachIteration">false</boolProp>
      </CookieManager>
      <hashTree/>
`;
 return outputTxt;
}


//----------------------------------------------------
// VIEW RESULTS TREE
//----------------------------------------------------
function printViewResultsTree(comment) {
 var outputTxt=`
      <ResultCollector guiclass="ViewResultsFullVisualizer" testclass="ResultCollector" testname="View Results Tree" enabled="true">
        <stringProp name="TestPlan.comments">${comment}</stringProp>
        <boolProp name="ResultCollector.error_logging">false</boolProp>
        <objProp>
          <name>saveConfig</name>
          <value class="SampleSaveConfiguration">
            <time>true</time>
            <latency>true</latency>
            <timestamp>true</timestamp>
            <success>true</success>
            <label>true</label>
            <code>true</code>
            <message>true</message>
            <threadName>true</threadName>
            <dataType>true</dataType>
            <encoding>false</encoding>
            <assertions>true</assertions>
            <subresults>true</subresults>
            <responseData>true</responseData>
            <samplerData>true</samplerData>
            <xml>true</xml>
            <fieldNames>true</fieldNames>
            <responseHeaders>false</responseHeaders>
            <requestHeaders>false</requestHeaders>
            <responseDataOnError>false</responseDataOnError>
            <saveAssertionResultsFailureMessage>true</saveAssertionResultsFailureMessage>
            <assertionsResultsToSave>0</assertionsResultsToSave>
            <bytes>true</bytes>
            <sentBytes>true</sentBytes>
            <url>true</url>
            <threadCounts>true</threadCounts>
            <idleTime>true</idleTime>
            <connectTime>true</connectTime>
          </value>
        </objProp>
        <stringProp name="filename"></stringProp>
      </ResultCollector>
`;
 return outputTxt;
}
