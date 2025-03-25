/*jslint white: true */
/*jslint es6 */

// from jmeter elements
/*global printCookieManagerEmpty */
/*global printHTTPSamplerProxy */
/*global printHeader */
/*global printHeaderManager */
/*global printHeaderManagerEmpty */
/*global printPostDataParam */
/*global printQueryDataParam */
/*global printReqArgsGet */
/*global printReqArgsPostDataParams */
/*global printReqArgsPostDataRaw */
/*global printTestPlan */
/*global printThreadGroup */
/*global printViewResultsTree */

// browser
/*global document */

"use strict";

//----------------------------------------------------
// MAIN LOGIC
//----------------------------------------------------

//----------------------------------------------------
function har2jmx(input) {

 var har = JSON.parse(input),
     outputLog = "",
     outputJmx = "",
     startTime = "",
     method = "",
     protocol = "",
     url = document.createElement('a'),
     domain = "",
     port = "",
     path = "",
     samplersPrepared = "",
     threadGroupsPrepared = "",
     reqArgs = "",
     reqHeaders = "",
     paramsPrepared = "",
     postParamNum = 0,
     postParamName = "",
     postParamValue = "",
     queryParamName = "",
     queryParamValue = "",
     headersPrepared = "",
     headerName = "",
     headerValue = "",
     defaultComment = "generated with har2jmx javascript";

 har.log.entries.forEach( entry => {

  url.href = entry.request.url;

  startTime = entry.startedDateTime;
  outputLog = outputLog + "[" + startTime + "]\n";

  method = entry.request.method;
  protocol = url.protocol.substring(0, url.protocol.length - 1);
  domain = url.hostname;
  port = url.port;
  path = url.pathname;

  outputLog = outputLog + "[" + method  + "]";
  outputLog = outputLog + " [" + url.href + "]\n";

  paramsPrepared = "";
  if (entry.request.postData) {
   outputLog = outputLog + " post data:\n";

   // if both present, [Parameters] are prefered over [Body Data]
   if (entry.request.postData.params && entry.request.postData.params.length > 0) {
    outputLog = outputLog + "  post params:\n";
    for (postParamNum = 0; postParamNum < entry.request.postData.params.length; postParamNum += 1) {
     postParamName = entry.request.postData.params[postParamNum].name;
     postParamValue = entry.request.postData.params[postParamNum].value;
     outputLog = outputLog + "   [" + postParamName + "] [" + postParamValue + "]\n";
     paramsPrepared = paramsPrepared + printPostDataParam(postParamName,postParamValue);
    }
    reqArgs = printReqArgsPostDataParams(paramsPrepared);
   }else{
     if (entry.request.postData.text) {
      paramsPrepared = entry.request.postData.text;
      outputLog = outputLog + "  post body data:\n";
      outputLog = outputLog + "   [" + paramsPrepared + "]\n";
      reqArgs = printReqArgsPostDataRaw(paramsPrepared);
     }
   }
  }else{
   if (entry.request.queryString && entry.request.queryString.length>0) {
     console.table
     entry.request.queryString.forEach(query => {
      queryParamName = query.name;
      queryParamValue = query.value;
      outputLog = outputLog + "  [" + queryParamName + "] [" + queryParamValue + "]\n";
      paramsPrepared = paramsPrepared + printQueryDataParam(queryParamName,queryParamValue);
     });
     reqArgs = printReqArgsGet(paramsPrepared);
   }else{
     reqArgs = printReqArgsGet("");
   }
  }

  headersPrepared = "";
  if (entry.request.headers) {
   outputLog = outputLog + " headers data:\n";
   entry.request.headers.forEach( header => {
    headerName = header.name;
    headerValue = header.value;
    outputLog = outputLog + "  [" + headerName + "] [" + headerValue + "]\n";
    headersPrepared = headersPrepared + printHeader (headerName,headerValue);
   });
   reqHeaders = printHeaderManager(headersPrepared,defaultComment);
  }
  outputLog = outputLog + "\n";

  samplersPrepared = samplersPrepared + printHTTPSamplerProxy(startTime + " " + path, "true", domain, port, protocol, path, "", method, reqArgs, reqHeaders,defaultComment);

 });
 threadGroupsPrepared = printThreadGroup("generated.from.har","true",samplersPrepared,defaultComment);
 outputJmx = printTestPlan(printHeaderManagerEmpty(defaultComment) + printCookieManagerEmpty(defaultComment) + threadGroupsPrepared + printViewResultsTree(defaultComment),defaultComment);

 return [outputLog,outputJmx];
}
