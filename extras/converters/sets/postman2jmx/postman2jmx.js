//*jslint white: true */
//*jslint es6 */

// from jmeter elements
//*global printCookieManagerEmpty */
//*global printHTTPSamplerProxy */
//*global printHeader */
//*global printHeaderManager */
//*global printHeaderManagerEmpty */
//*global printPostDataParam */
//*global printQueryDataParam */
//*global printReqArgsGet */
//*global printReqArgsPostDataParams */
//*global printReqArgsPostDataRaw */
//*global printTestPlan */
//*global printThreadGroup */
//*global printViewResultsTree */

// browser
//*global document */

"use strict";

var defaultComment = "generated with postman2jmx";

//----------------------------------------------------
function process_request(obj,objName) {
  var reqArgs = "",
      reqHeaders = "",
      reqName = objName,
      reqUrl = "",
      reqTimestamp = "",
      reqDescription = "",
      paramsPrepared = "",
      headersPrepared = "",
      samplersPrepared = "",
      outputJmx = "",
      outputLog = "";

  if (typeof(obj.name) === "string") {
   reqName = obj.name;
  }
  if (typeof(obj.description) === "string") {
   reqDescription = obj.description;
  }

  if (typeof obj.url === 'string') {
   reqUrl = obj.url.replace(/\?.*/,"");
  }else if(obj.url.raw) {
   reqUrl = obj.url.raw.replace(/\?.*/,"");
  }

  if (typeof(obj.timestamp) === "string"){
   reqTimestamp = obj.timestamp + " ";
  }else{
   reqTimestamp = "";
  }

  outputLog = " [" + reqTimestamp + "]" 
              + " [" + obj.method + "]"
              + " [" + reqUrl + "]\n";
  //description = obj.description;
  //alert(outputLog);

  if (obj.method === "POST") {
   outputLog += " post data:\n";
   if(obj.rawModeData) {
     outputLog += "  post body data:\n";
     outputLog += "   [" + obj.rawModeData + "]\n";
     reqArgs = printReqArgsPostDataRaw(obj.rawModeData);
   }else if (obj.body) {
    if (obj.body.urlencoded){
     obj.body.urlencoded.forEach( param => {
      outputLog += "  post params: [" + param.key + "] [" + param.value + "]\n";
      paramsPrepared += printPostDataParam(param.key,param.value);
     });
     reqArgs = printReqArgsPostDataParams(paramsPrepared);
    }else if(obj.body.raw) {
     outputLog += "  post body data:\n";
     outputLog += "   [" + obj.body.raw + "]\n";
     reqArgs = printReqArgsPostDataRaw(obj.body.raw);
    }
   }
  }else if (obj.method === "GET") {
   if (obj.url.query) {
     obj.url.query.forEach(param => {
      outputLog += "  [" + param.key + "] [" + param.value + "]\n";
      paramsPrepared += printQueryDataParam(param.key, param.value);
     });
     reqArgs = printReqArgsGet(paramsPrepared);
   }else if (obj.data) {
     console.table(obj.data);
     if (obj.data.length > 0) {
      obj.data.forEach(param => {
       outputLog += "  [" + param.key + "] [" + param.value + "]\n";
       paramsPrepared += printQueryDataParam(param.key, param.value);
      });
      reqArgs = printReqArgsGet(paramsPrepared);
     }
   }
  }

  if (obj.header) {
   if (obj.header.length >0) {
    outputLog = outputLog + " headers data:\n";
    obj.header.forEach( header => {
     outputLog += "  [" + header.key + "] [" + header.value + "]\n";
     headersPrepared += printHeader (header.key, header.value);
    });
    reqHeaders = printHeaderManager(headersPrepared,defaultComment);
   }
  }
  outputLog = outputLog + "\n";

  samplersPrepared = samplersPrepared + printHTTPSamplerProxy(reqTimestamp + reqName, "true", "", "", "", reqUrl, "", obj.method, reqArgs, reqHeaders, reqDescription);

  return [outputLog, samplersPrepared];

}

//----------------------------------------------------
function process_items(obj) {
 var outputLog = "",
     outputPrepared = "",
     samplersPrepared = "";
 obj.forEach( item => {
  if (item.request) {
   outputLog += "\n\n[request] " + item.name;
   outputPrepared = process_request(item.request, item.name);
   outputLog += outputPrepared[0];
   samplersPrepared += outputPrepared[1];
  }
  if (item.item) {
   outputLog += "\n\n[item] " + item.name;
   outputPrepared = process_items(item.item, item.name);
   outputLog += outputPrepared[0];
   samplersPrepared += printGenericController(item.name, "true", outputPrepared[1], defaultComment);
  }
 });
   return [outputLog, samplersPrepared];
}

//----------------------------------------------------
function process_requests(obj,objName) {
 var outputLog = "",
     outputPrepared = "",
     samplersPrepared = "";

 obj.Each( request => {
  if (request.name) {
   outputPrepared = process_request(request, request.name);
  }else{
   outputPrepared = process_request(request, objName);
  }
  outputLog += outputPrepared[0];
  samplersPrepared += outputPrepared[1];
 });

 return [outputLog, samplersPrepared];
}

//----------------------------------------------------
// MAIN LOGIC
//----------------------------------------------------

//----------------------------------------------------
function postman2jmx(input) {

 var postman = JSON.parse(input),
     outputLog = "",
     outputJmx = "",
     outputPrepared = "",
     samplersPrepared = "",
     threadGroupsPrepared = "",
     threadName = "Thread Group",
     threadDescription = "generated with postman2jmx javascript";

 if (typeof(postman.name) === "string") {
  threadName = postman.name;
 }else if (postman.info) {
  if (typeof(postman.info.name) === "string") {
   threadName = postman.info.name;
  }
 }
 if (typeof(postman.description) === "string") {
  threadDescription = postman.description;
 }else if (postman.info) {
  if (typeof(postman.info.description) === "string") {
   threadDescription = postman.info.description;
  }
 }

 outputLog = "[collection] " + threadName + "\n\n";

 if (postman.item) {
  //alert("item->" + postman.item);
  outputPrepared = process_items(postman.item);
  outputLog += outputPrepared[0];
  samplersPrepared += outputPrepared[1];
 } 
 // not needed for 2.1+, but still keeping just in case
 if (postman.request) {
  //alert("req->" + postman.request);
  outputPrepared = process_requests(postman.request);
  outputLog += outputPrepared[0];
  samplersPrepared += outputPrepared[1];
 }

 threadGroupsPrepared = printThreadGroup(threadName, "true", samplersPrepared, threadDescription);
 outputJmx = printTestPlan(printHeaderManagerEmpty(defaultComment) + printCookieManagerEmpty(defaultComment) + threadGroupsPrepared + printViewResultsTree(defaultComment), defaultComment);
 // update postman vars to JMeter vars
 outputJmx = outputJmx.replace(/\{\{/mg,"${").replace(/\}\}/mg,"}");

 return [outputLog,outputJmx];
}
