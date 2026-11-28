/*jslint white: true */
/*jslint es6 */

// xml.helpers.js
/*global get_xml_firstChild */
/*global get_xml_nextSibling */
/*global sanitizeValue */

// jmeter.elements.js
/*global printArgument */
/*global printArguments */
/*global printGenericController */
/*global printHTTPSamplerProxy */
/*global printHeader */
/*global printHeaderManager */
/*global printJDBCSampler */
/*global printJSR223Sampler */
/*global printModuleController */
/*global printQueryDataParam */
/*global printReqArgsGet */
/*global printReqArgsPostDataParams */
/*global printReqArgsPostDataRaw */
/*global printTestPlan */
/*global printThreadGroup */
/*global printViewResultsTree*/
/*global printXPath2Assertion */
/*global printXPath2Extractor */

// jmeter.elements.js
/*global encodeXml */

// utils.helpers.js
/*global util_copyAA */

// browser
/*global alert */
/*global window */
/*global DOMParser */
/*global ActiveXObject */

"use strict";

var defaultComment = "created with soapui2jmx";

//----------------------------------------------------

function check_enabled (object){

 var enabled = "false";
 if (! object.attributes.disabled){
  enabled = "true";
 }else{
  enabled = "false";
 }
 return enabled;
}

//----------------------------------------------------

function printSoapuiID(object) {
 var soapuiID = "";
 if (object.attributes.id){
  soapuiID = object.attributes.id.value;
 }else{
  soapuiID = "null";
 }
 return "soapuiID [" + soapuiID +"]";
}

//----------------------------------------------------

// update default parameters for current node
function updateDefaultParameters(object,defaultParameters){
 var parameters=null;

 parameters = object.querySelectorAll("parameters > parameter");
 parameters.forEach ( parameter => {
  if (parameter.getElementsByTagName("con:default")[0]) {
   if (parameter.getElementsByTagName("con:default")[0].textContent !== "") {
    defaultParameters[parameter.getElementsByTagName("con:name")[0].textContent]=parameter.getElementsByTagName("con:default")[0].textContent;
   }
  }
 });

 return defaultParameters;
}

//----------------------------------------------------

function process_restMethodGetReq (object,resourcePath,resourceDefaults){
 var i=0,
     aKey="",
     outputTxt="",
     outputJmx="",
     endpoint="",
     entries=null,
     params={},
     paramsPrepared="";

 //alert (object.attributes.name.value + "-" + Object.keys(resourceDefaults).length);
 outputTxt += "      * [ request ] "
              + object.attributes.name.value + " ["
              + check_enabled(object) + "]\n";
 endpoint = resourcePath;

 // copy defaults     
 params = util_copyAA(resourceDefaults,params);

 // overwrite if needed
 entries = object.querySelectorAll("parameters > entry");
 entries.forEach ( entry => {
  params[entry.attributes.key.value] = entry.attributes.value.value;
 });

 // create params
 Object.keys(params).forEach(function (index) {
  paramsPrepared = paramsPrepared + printQueryDataParam(index, params[index]);
 });

 outputJmx=printHTTPSamplerProxy(object.attributes.name.value,
                                 check_enabled(object),
                                 "",
                                 "",
                                 "",
                                 endpoint,
                                 "",
                                 "GET",
                                 printReqArgsGet(paramsPrepared), 
                                 "",
                                 printSoapuiID(object)
                                );
 return [outputTxt,outputJmx];
}

//----------------------------------------------------

function process_restMethodPostReq(object,resourcePath,resourceDefaults){
 var outputTxt="",
     outputJmx="",
     endpoint="",
     entries=null,
     params={},
     paramsPrepared="";

 //alert (object.attributes.name.value + "-" + Object.keys(resourceDefaults).length);
 outputTxt += "      * [ request ] "
              + object.attributes.name.value + " ["
              + check_enabled(object) + "]\n";
 endpoint = resourcePath;

 // copy defaults     
 params = util_copyAA(resourceDefaults,params);

 // overwrite if needed
 entries = object.querySelectorAll("parameters > entry");
 entries.forEach( entry => {
  params[entry.attributes.key.value]=entry.attributes.value.value;
 });

 // create params
 Object.keys(params).forEach(function (index) {
  paramsPrepared = paramsPrepared + printQueryDataParam(index, params[index]);
 });

 outputJmx=printHTTPSamplerProxy(object.attributes.name.value,
                                 check_enabled(object),
                                 "",
                                 "",
                                 "",
                                 endpoint,
                                 "",
                                 "POST",
                                 printReqArgsPostDataParams(paramsPrepared), 
                                 "",
                                 printSoapuiID(object)
                                );
 return [outputTxt,outputJmx];
}


//----------------------------------------------------

function process_restMethod (object,resourcePath,resourceDefaults){
 //alert('process_method');
 var output="",
     outputTxt="",
     outputJmx="",
     endpoint="",
     method="",
     requests=null;
 outputTxt += "    * [ method ] "
              + object.attributes.name.value + " ["
              + check_enabled(object) + "]\n";
 endpoint = object.getElementsByTagName("con:endpoint")[0].textContent;
 method = object.attributes.method.value;
     
 requests = object.querySelectorAll("request");
 requests.forEach ( request => {
  // all the glory to xml and nested tags with same names...
  if (request.attributes.name) {
   if (method === "GET" ){
    output = process_restMethodGetReq(request,endpoint + resourcePath,resourceDefaults);
    outputTxt += output[0];
    outputJmx += output[1];
   }else if (method === "POST") {
    output = process_restMethodPostReq(request,endpoint + resourcePath,resourceDefaults);
    outputTxt += output[0];
    outputJmx += output[1];
   }
  }
 });

 outputTxt += "      * edp:[" + endpoint + resourcePath + "]\n";
 outputTxt += "      * mtd:[" + method + "]\n";
 outputJmx = printGenericController (object.attributes.name.value, "true", outputJmx, printSoapuiID(object));

 return [outputTxt,outputJmx];
}


//----------------------------------------------------

function process_restResource (object,resourcePath,resourceDefaults){

 var i=0,
     objChildren = object.childNodes,
     outputPrepared = null,
     outputLog = " * [resource] " + object.attributes.name.value + "\n",
     outputJmx = "",
     resrcPath = resourcePath + "/" + object.attributes.path.value,
     resrcDefaults = updateDefaultParameters(object,resourceDefaults);

 objChildren.forEach ( objChild => {
  if (objChild.nodeName === "con:resource") {
   outputPrepared = process_restResource(objChild,resrcPath,resrcDefaults);
   outputLog += outputPrepared[0];
   //controllers
   outputJmx += outputPrepared[1];

  }else if (objChild.nodeName === "con:method") {
   outputPrepared = process_restMethod(objChild,resrcPath,resrcDefaults);
   outputLog += outputPrepared[0];
   //samplers
   outputJmx += outputPrepared[1];
  }
 });

 outputJmx = printGenericController (object.attributes.name.value,"true", outputJmx, printSoapuiID(object));
 return [outputLog,outputJmx];
}

//----------------------------------------------------


function process_soapReqWithAction (object,soapAction){
 var outputTxt="",
     headersPrepared="",
     outputJmx="",
     requestNode = null,
     endpoint = "",
     encoding = "",
     request = "";

 if (object.getElementsByTagName("con:endpoint")[0]){
  // REQUEST
  requestNode = object.getElementsByTagName("con:endpoint")[0].parentNode;
     
  endpoint =  requestNode.getElementsByTagName("con:endpoint")[0].textContent;
  encoding = requestNode.getElementsByTagName("con:encoding")[0].textContent;
  request = requestNode.getElementsByTagName("con:request")[0].textContent;

  headersPrepared += printHeader("Content-Type", "text/xml");
  headersPrepared += printHeader("SOAPAction", soapAction);
  outputJmx=printHTTPSamplerProxy(object.attributes.name.value,
                                      check_enabled(object),
                                      "",
                                      "",
                                      "",
                                      endpoint,
                                      encoding,
                                      "POST",
                                      printReqArgsPostDataRaw(request),
                                      printHeaderManager(headersPrepared,defaultComment),
                                      printSoapuiID(object)
                                     );
  
 }
 return [outputTxt,outputJmx];
}

//----------------------------------------------------

function process_soapReq (object){
 //alert('process_soapReq');
 var outputTxt="",
     rootNodes=null,
     operations=null,
     soapAction="",
     headersPrepared="",
     outputJmx="",
     reqInterface = "",
     reqOperation = "",
     requestNode = null,
     endpoint = "",
     encoding = "",
     request = "";

 outputTxt += "    * [" + object.attributes.type.value + "] "
                    + object.attributes.name.value + " ["
                    + check_enabled(object) + "]\n";
 if (object.getElementsByTagName("con:endpoint")[0]){
  // REQUEST
  reqInterface = object.getElementsByTagName("con:interface")[0].textContent;
  reqOperation = object.getElementsByTagName("con:operation")[0].textContent;
  //alert(reqOperation+'|'+reqInterface);
  requestNode = object.getElementsByTagName("con:endpoint")[0].parentNode;
     
  endpoint =  requestNode.getElementsByTagName("con:endpoint")[0].textContent;
  encoding = requestNode.getElementsByTagName("con:encoding")[0].textContent;
  request = requestNode.getElementsByTagName("con:request")[0].textContent;

  outputTxt += "      * edp:[" + endpoint + "]\n";
  outputTxt += "      * enc:[" + encoding + "]\n";

  rootNodes = object.ownerDocument.childNodes[0].childNodes;
  rootNodes.forEach ( rootNode => {
  //alert (rootNode.nodeName);
   if(rootNode.nodeName === "con:interface") {
    if (rootNode.attributes.name.value === reqInterface){
     operations = rootNode.querySelectorAll("operation");
     operations.forEach ( operation => {
      //alert (operation.attributes.name.value);
      if (operation.attributes.name.value === reqOperation){
       soapAction = operation.attributes.action.value;
       //alert(i+'/'+rootNodes.length+'|'+j+'/'+operations.length+'|'+object.attributes.name.value+'|'+soapAction);
      }
     });
    }
   }
  });
  headersPrepared += printHeader("Content-Type", "text/xml");
  headersPrepared += printHeader("SOAPAction", soapAction);
  outputJmx=printHTTPSamplerProxy(object.attributes.name.value,
                                      check_enabled(object),
                                      "",
                                      "",
                                      "",
                                      endpoint,
                                      encoding,
                                      "POST",
                                      printReqArgsPostDataRaw(request),
                                      printHeaderManager(headersPrepared,defaultComment),
                                      printSoapuiID(object)
                                     );
  
 }
 return [outputTxt,outputJmx];
}

//----------------------------------------------------

function process_soapOperation (object){

 var objChildren = object.childNodes,
     outputPrepared = null,
     outputLog = " * [operation] " + object.attributes.name.value + "\n",
     outputJmx = "",
     soapAction = object.attributes.action.value;

 objChildren.forEach ( objChild => {
  if (objChild.nodeName === "con:call"){
   outputPrepared = process_soapReqWithAction(objChild,soapAction);
   outputLog += outputPrepared[0];
   outputJmx += outputPrepared[1];
  }
 });
 outputJmx = printGenericController (object.attributes.name.value,"true", outputJmx, printSoapuiID(object));
 return [outputLog,outputJmx];
}
 
//----------------------------------------------------

function getSoapuiDefaultsOfRestReq(soapUiRootObj,reqService,reqMethod){
 var baseMethod = "",
     interfaces = soapUiRootObj.querySelectorAll("interface"),
     methods = null,
     defaultParams={},
     entries=null;

 interfaces.forEach ( soapInterface => {

  if (soapInterface.attributes.name.value === reqService){
   defaultParams = updateDefaultParameters(soapInterface,defaultParams);
   methods = soapInterface.querySelectorAll("resource > method");
   methods.forEach( method => {
    if (method.attributes.name.value === reqMethod){
     // overwrite if needed
     entries = method.querySelectorAll("parameters > entry");
     entries.forEach( entry => {
      defaultParams [entry.attributes.key.value] = entry.attributes.value.value;
     });
     baseMethod = method.attributes.method.value;
    }
   });
  }
 });

 return [baseMethod,defaultParams];
}
 
//----------------------------------------------------
 
function process_assertions(object) {
 var assertions=null,
     assertionDefintion="",
     assertionsPrepared="",
     namespaces="",
     xpathQuery="",
     assertionCheck="";
 
 assertions=object.querySelectorAll("config > restRequest > assertion");
 assertions.forEach( assertion => {
  if (assertion.attributes.type.value === "XPath Match"){

   assertionDefintion=sanitizeValue(assertion.getElementsByTagName("path")[0]);
   assertionCheck=sanitizeValue(assertion.getElementsByTagName("content")[0]);

   //alert (assertion.attributes.name.value+"\n---\n"+assertionDefintion+'\n---\n'+assertionCheck);
   if (assertionDefintion.match(/.*declare namespace.*/mig)) {
    namespaces = assertionDefintion.match(/.*declare namespace.*/mig).join("\n").replace(/declare namespace /g,"").replace(/[';]/g,"");
   }
   xpathQuery = assertionDefintion.replace(/.*declare namespace.*/mig,"").replace( /[\r\n]+/gm, "");
  }
  //alert (assertion.attributes.name.value+"\n---\n"+namespaces+'\n---\n'+xpathQuery+'\n---\n'+assertionCheck);
  if (xpathQuery.match(/.*count.*/mig)){
   if (! assertionCheck.match(/true/mig)){
    // it it was true, nothing to do, as in jmeter that should be true anyway
    // if not, got a number to compare
    xpathQuery += " = " + assertionCheck;
   }
  }else{
   // got a string to compare
   xpathQuery += " = '" + assertionCheck + "'";
  }
  assertionsPrepared += printXPath2Assertion(assertion.attributes.type.value, "true", xpathQuery, namespaces, printSoapuiID(assertion));
  //alert(assertionsPrepared);

 });

 return assertionsPrepared;

}

//----------------------------------------------------

function process_restReq (object){
 //alert('process_restReq');
 var outputTxt="",
     outputJmx="",
     endpoint="",
     config=object.getElementsByTagName("con:config")[0],
     method="",
     service="",
     path="",
     parameters=null,
     paramsPrepared="",
     defaults=null,
     reqMethod="",
     reqParams={},
     reqDefaultParams={};

 outputTxt += "    * [" + object.attributes.type.value + "] "
              + object.attributes.name.value + " ["
              + check_enabled(object) + "]\n";
 method=config.attributes.methodName.value;
 service=config.attributes.service.value;

 //determine method and default parameters of referenced object
 defaults=getSoapuiDefaultsOfRestReq(object.ownerDocument,service,method);
 reqMethod=defaults[0];
 reqDefaultParams=defaults[1];
 
 // copy defaults
 reqParams = util_copyAA(reqDefaultParams,reqParams);

 // update with case specific items
 parameters=config.querySelectorAll("restRequest > parameters > entry");
 parameters.forEach ( parameter => {
  reqParams[parameter.attributes.key.value] = parameter.attributes.value.value;
 });

 Object.keys(reqParams).forEach(function (index) {
  paramsPrepared = paramsPrepared + printQueryDataParam(index, reqParams[index]);
 });

 path=config.attributes.resourcePath.value;
 endpoint=config.getElementsByTagName("con:endpoint")[0].textContent;
 
 if (reqMethod === "GET"){
  outputJmx=printHTTPSamplerProxy(object.attributes.name.value,
                                  check_enabled(object),
                                  "",
                                  "",
                                  "",
                                  endpoint+path,
                                  "",
                                  reqMethod,
                                  printReqArgsGet(paramsPrepared),
                                  process_assertions(object),
                                  printSoapuiID(object)
                                );
 }else if (reqMethod === 'POST'){
  outputJmx=printHTTPSamplerProxy(object.attributes.name.value,
                                  check_enabled(object),
                                  "",
                                  "",
                                  "",
                                  endpoint+path,
                                  "",
                                  reqMethod,
                                  printReqArgsPostDataParams(paramsPrepared),
                                  process_assertions(object),
                                  printSoapuiID(object)
                                );
 }else{
  alert ('Unsuported method [' + reqMethod + '] in [' + object.attributes.name.value + ']\nCurrently supported are only [GET,POST]\nPlease fix in your soap file');
 }

 return [outputTxt,outputJmx];
}

//----------------------------------------------------


function process_httpReq (object){
 //alert('process_httpReq');
 var outputTxt="",
     outputJmx="",
     endpoint = "",
     method = "",
     httpParameters = null,
     paramsPrepared = "",
     paramName = "",
     paramValue = "";


 outputTxt += "    * [" + object.attributes.type.value + "] "
              + object.attributes.name.value + " ["
              + check_enabled(object) + "]\n";
 if (object.getElementsByTagName("con:endpoint")[0]){
  // REQUEST
  endpoint = object.getElementsByTagName("con:endpoint")[0].textContent;
  method = object.getElementsByTagName("con:config")[0].attributes.method.value;
  httpParameters = null;
  paramsPrepared = "";
  paramName = "";
  paramValue = "";

  outputTxt += "      * edp:[" + endpoint + "]\n";
  outputTxt += "      * mth:[" + method + "]\n";

  if ( method === "GET" ){
   httpParameter = object.querySelectorAll("config > parameters > parameter");
   httpParameters.forEach ( httpParameter => {
    paramName = httpParameter.getElementsByTagName("con:name")[0].textContent;
    paramValue = httpParameter.getElementsByTagName("con:value")[0].textContent;
    paramsPrepared = paramsPrepared + printQueryDataParam(paramName, paramValue);
   });

   outputJmx=printHTTPSamplerProxy(object.attributes.name.value,
                                      check_enabled(object),
                                      "",
                                      "",
                                      "",
                                      endpoint,
                                      "",
                                      method,
                                      printReqArgsGet(paramsPrepared),
                                      "",
                                      printSoapuiID(object)
                                     );
  }   
 }
 return [outputTxt,outputJmx];
}

//----------------------------------------------------

function process_jdbcReq(object){
 //alert('process_jdbcReq');
 var outputTxt="",
     outputJmx="",
     config=null,
     connectionName="",
     queryValue="",
     queryResultVar="";

 outputTxt += "    * [" + object.attributes.type.value + "] "
              + object.attributes.name.value + " ["
              + check_enabled(object) + "]\n";
 config = object.getElementsByTagName("con:configuration")[0];
 connectionName = sanitizeValue(config.getElementsByTagName("Connection")[0]);
 queryValue = sanitizeValue(config.getElementsByTagName("query")[0]);
 queryResultVar = sanitizeValue(object.getElementsByTagName("con:property")[0]);
 outputJmx = printJDBCSampler(object.attributes.name.value,
                              check_enabled(object),
                              connectionName,
                              queryValue,
                              queryResultVar,
                              printSoapuiID(object)
                             );
 return [outputTxt,outputJmx];
}

//----------------------------------------------------

function process_scriptReq(object){
 //alert('process_scriptReq');
 var outputTxt="",
     outputJmx="",
     script="";

 outputTxt = "    * [" + object.attributes.type.value + "] "
              + object.attributes.name.value + " ["
              + check_enabled(object) + "]\n";

 script = object.getElementsByTagName("script")[0].textContent;
 outputJmx = printJSR223Sampler(object.attributes.name.value,
                                check_enabled(object),
                                script,
                                printSoapuiID(object)
                               );
 return [outputTxt,outputJmx];
}

//----------------------------------------------------

function process_varsReq(object){
 //alert('vars_scriptReq');
 var outputTxt="",
     outputJmx="",
     properties=null,
     propertyName="",
     propertyValue="",
     script="";

 outputTxt += "    * [" + object.attributes.type.value + "] "
              + object.attributes.name.value + " ["
              + check_enabled(object) + "]\n";

 //properties = object.getElementsByTagName("con:parameters")[0].getElementsByTagName("con:parameter");
 properties = object.querySelectorAll("config > properties > property");
 properties.forEach ( property => {
  propertyName = property.getElementsByTagName("con:name")[0].textContent;
  propertyValue = property.getElementsByTagName("con:value")[0].textContent;
  script += 'vars.put("' + propertyName + '",' + propertyValue + ')' + ";\n";
 });

 outputJmx = printJSR223Sampler(object.attributes.name.value,
                                check_enabled(object),
                                script,
                                printSoapuiID(object)
                               );
 return [outputTxt,outputJmx];
}

//----------------------------------------------------

function process_includeReq(object){
 var i=0,
     outputTxt="",
     outputJmx="",
     id="",
     refObject=null,
     path="",
     properties=null,
     propertyName="",
     propertyValue="",
     incArguments="",
     name=Date.now();

 outputTxt += "    * [" + object.attributes.type.value + "] "
              + object.attributes.name.value + " ["
              + check_enabled(object) + "]\n";
 id = object.getElementsByTagName("con:targetTestCase")[0].textContent;
 refObject=object.ownerDocument.getElementById(id);

 if (refObject){
  while (refObject.nodeName !== "con:testSuite"){
   path = '<stringProp name="' + name + '_' + i + '">' + refObject.attributes.name.value + "</stringProp>" + path;
   refObject = refObject.parentNode;
   i += 1;
  }
  path = '<stringProp name="' + name + '_' + i + '">' + "TestSuite_" + refObject.attributes.name.value + "</stringProp>" + path;
 }else{
  //referenced test case is missing
  path="";
 }

 outputJmx = printModuleController(object.attributes.name.value,
                                    check_enabled(object),
                                    path,
                                    printSoapuiID(object) + ", referenced test case: [" + id + "]"
                                   );
 
 properties = object.querySelectorAll("config > properties > property");
 properties.forEach ( property => {
  propertyName = sanitizeValue(property.getElementsByTagName("con:name")[0]);
  propertyValue = sanitizeValue(property.getElementsByTagName("con:value")[0]);
  incArguments += printArgument(propertyName,propertyValue);
 });
 
 if (properties.length>0){
  outputJmx += printArguments(object.attributes.name.value + "_properties","false",incArguments,"arguments for included test case");
 }

 return [outputTxt,outputJmx];
}

//----------------------------------------------------

function process_transfer(object){
 //alert('process_transfer');
 var i=0,
     outputTxt="",
     outputJmx="",
     transferName="",
     srcStep="",
     srcType="",
     tgtStep="",
     tgtType="",
     extractorDefintion="",
     scriptDefintion="",
     transferUsageInfo="",
     namespaces="",
     xpathQuery="",
     updateVar="",
     transfers=object.querySelectorAll('config > transfers');

 outputTxt += "    * [" + object.attributes.type.value + "] "
              + object.attributes.name.value + " ["
              + check_enabled(object) + "]\n";

 transfers.forEach ( transfer => {
  transferName=sanitizeValue(transfer.getElementsByTagName("con:name")[0]);
  srcStep=sanitizeValue(transfer.getElementsByTagName("con:sourceStep")[0]);
  srcType=sanitizeValue(transfer.getElementsByTagName("con:sourceType")[0]);
  tgtStep=sanitizeValue(transfer.getElementsByTagName("con:targetStep")[0]);
  tgtType=sanitizeValue(transfer.getElementsByTagName("con:targetType")[0]);

  transferUsageInfo = "; src->dst = [" + srcStep + "/" + srcType + " -> " + tgtStep + "/" + tgtType + "]";
  updateVar='vars.put("' + tgtType + '_' + srcType + '",vars.get("' + srcType + '"));' + "\n";

  extractorDefintion=sanitizeValue(transfer.getElementsByTagName("con:sourcePath")[0]);
  scriptDefintion=sanitizeValue(transfer.getElementsByTagName("con:targetPath")[0]);

  outputTxt += "      * " + transferName + "\n";
  //XPath2 extractor to get the data
  if ( extractorDefintion && extractorDefintion !== "" ) {
   if (extractorDefintion.match(/.*declare namespace.*/mig)) {
    namespaces = extractorDefintion.match(/.*declare namespace.*/mig).join("\n").replace(/declare namespace /g,"").replace(/[';]/g,"");
   }
   xpathQuery = extractorDefintion.replace(/.*declare namespace.*/mig,"").replace( /[\r\n]+/gm, "");
   outputJmx += printXPath2Extractor(object.attributes.name.value + ": get " + transferName,
                                     check_enabled(object),
                                     "EMPTY",
                                     encodeXml(transferName).replace(" ",'_'),
                                     xpathQuery,
                                     namespaces,
                                     printSoapuiID(object) + transferUsageInfo
                                    );

  }
  //JSR script to re-write values
  if ( scriptDefintion && scriptDefintion !== ""){
   outputJmx += printJSR223Sampler(object.attributes.name.value,
                                  check_enabled(object),
                                  updateVar + "\n" + scriptDefintion,
                                  printSoapuiID(object) + transferUsageInfo
                                 );
 
  }
  if ( scriptDefintion === "null" && extractorDefintion === "null" ){
   outputJmx += printJSR223Sampler(object.attributes.name.value,
                                  check_enabled(object),
                                  updateVar,
                                  printSoapuiID(object) + transferUsageInfo
                                 );
 
  }
 });

 return [outputTxt,outputJmx];
}

//----------------------------------------------------

function printREADME() {
 var outputTxt="",
     script=`
This is generated content. As soapUI is quite a cross-referenced smart bear, not all things are easy to map to JMeter.

Notes:

 * this is best apporach conversion, that still requires your attention and MAJOR changes
 * soap requests from TestSuites are generated with empty soap actions, as they are not always present in soapUI files (as interfaces may be referenced instead), please use correct SoapActions from &apos;Interface_&apos; section
 * grovy scripts in JSR samplers are just a copy from soapUI and require fixes to make it working in most cases (e.g. change &apos;testRunner.testCase.setPropertyValue&apos; to &apos;vars.set&apos; and so on). They are commented out by default.
 * JDBC generated items also need your attention - check content and add JDBC config element
 * referneced test cases, seen as modules in JMeter, require extra attentions, as they have own input properties, and returns own output ptoperties sets
 `;
 outputTxt=printJSR223Sampler("README","true",script,defaultComment);
 return outputTxt;
}

//----------------------------------------------------

function process_testSteps (n){
    var outputTxt = "",
        outputJmx = "",
        output = "",
        x = get_xml_firstChild(n);
    while (x){ 
       output = "";
       if (x.nodeName === "con:testStep"){
           if (x.attributes.type.value === "datasource"){
               output = process_jdbcReq(x);
               outputTxt += output[0];
               outputJmx += output[1];
           }else if(x.attributes.type.value === "properties"){
               output = process_varsReq(x);
               outputTxt += output[0];
               outputJmx += output[1];
           }else if(x.attributes.type.value === "groovy"){
               output = process_scriptReq(x);
               outputTxt += output[0];
               outputJmx += output[1];
           }else if(x.attributes.type.value === "calltestcase"){
               // having inclde elements not defined properly gives JMeter errros, so skipping
               output = process_includeReq(x);
               outputTxt += output[0];
               outputJmx += output[1];
           }else if(x.attributes.type.value === "request"){
               output = process_soapReq(x);
               outputTxt += output[0];
               outputJmx += output[1];
           }else if(x.attributes.type.value === "restrequest"){
               output = process_restReq(x);
               outputTxt += output[0];
               outputJmx += output[1];
           }else if(x.attributes.type.value === "httprequest"){
               output = process_httpReq(x);
               outputTxt += output[0];
               outputJmx += output[1];
           }else if(x.attributes.type.value === "transfer"){
               output = process_transfer(x);
               outputTxt += output[0];
               outputJmx += output[1];
           }
       }
       x = get_xml_nextSibling(x);
    }
    return [outputTxt,outputJmx];
}

//----------------------------------------------------
// MAIN LOGIC
//----------------------------------------------------

//----------------------------------------------------
function soapui2jmx(input) {

 var soapui = "",
     parser = null,
     outputLog = "",
     outputJmx = "",
     threadGroupsPrepared = "",
     controllersPrepared = "",
     samplersPrepared = "",
     outputPrepared = "",
     testCaseNodes=null,
     resourceDefaults={},
     interfaceChildren=null,
     testSuiteChildren=null,
     mainNodes=null;

 if (window.DOMParser)
 {
     parser = new DOMParser();
     soapui = parser.parseFromString(input, "text/xml");
 }
 else // Internet Explorer
 {
     soapui = new ActiveXObject("Microsoft.XMLDOM");
     soapui.async = false;
     soapui.loadXML(input);
 }
 mainNodes=soapui.childNodes[0].childNodes;
 mainNodes.forEach( mainNode => {
  if (mainNode.nodeName === "con:interface") {

   // INTERFACE -> thread group
   outputLog += "\n[interface] " + mainNode.attributes.name.value + "\n";
   controllersPrepared = "";
   interfaceChildren=mainNode.childNodes;
   interfaceChildren.forEach( interfaceChild => {
    if (interfaceChild.nodeName === "con:resource") {
     // RESOURCE -> simple controller, rest projects
     outputPrepared = process_restResource(interfaceChild,"",resourceDefaults);
     outputLog += outputPrepared[0];
     controllersPrepared += outputPrepared[1];
    }else if (interfaceChild.nodeName === "con:operation") {
     // OPERATION -> simple controller, soap projects
     outputPrepared = process_soapOperation(interfaceChild);
     outputLog += outputPrepared[0];
     controllersPrepared += outputPrepared[1];
    }
   });

   threadGroupsPrepared += printThreadGroup("Interface_" + mainNode.attributes.name.value, "false", controllersPrepared, printSoapuiID(mainNode));

  }else if (mainNode.nodeName === "con:testSuite") {

   // TEST SUITE -> thread group
    outputLog += "\n[testSuite] " + mainNode.attributes.name.value + " [" + check_enabled(mainNode) + "]\n";
    controllersPrepared="";
    testSuiteChildren=mainNode.childNodes;
    testSuiteChildren.forEach( testSuiteChild => {
     if (testSuiteChild.nodeName === "con:testCase") {
      testSuiteChild
      outputLog += "\n  * [testCase] " + testSuiteChild.attributes.name.value + " [" + check_enabled(testSuiteChild) + "]\n";
      outputPrepared = process_testSteps(testSuiteChild);
      outputLog += outputPrepared[0];
      samplersPrepared = outputPrepared[1];
      controllersPrepared += printGenericController (testSuiteChild.attributes.name.value, check_enabled(testSuiteChild), samplersPrepared,printSoapuiID(testSuiteChild));
     } // TEST CASE
    }); // TEST SUITE

    threadGroupsPrepared += printThreadGroup("TestSuite_" + mainNode.attributes.name.value, check_enabled(mainNode), controllersPrepared,printSoapuiID(mainNode));
  }
 });

 outputJmx = printTestPlan(printREADME() + threadGroupsPrepared + printViewResultsTree(),defaultComment);
 return [outputLog,outputJmx];
}

//----------------------------------------------------

