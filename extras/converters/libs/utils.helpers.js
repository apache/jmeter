/*jslint white: true */
/*jslint es6 */

"use strict";

//--------------------------------------------------------

function debug_printObjKeysAndValues(obj) {
 var debugTxt = "";
 
 Object.keys(obj).forEach(function (index) {
  debugTxt += index + " | " + obj[index] + "\n";
 });

 return debugTxt;
}

//--------------------------------------------------------

function util_copyAA(src, dst) {

 Object.keys(src).forEach(function (index) {
  dst[index] = src[index];
 });

 return dst;
}

//--------------------------------------------------------

function handleOutput(filename,input){
 document.getElementById('outputLog').innerText=input[0];

 var blob = new Blob([input[1]], {type: 'text/plain'});
 if(window.navigator.msSaveOrOpenBlob) {
  window.navigator.msSaveBlob(blob, filename);
 }else{
  var elem = window.document.createElement('a');
  elem.href = window.URL.createObjectURL(blob);
  elem.download = filename;
  document.body.appendChild(elem);
  alert("Your file [" + filename + "] will be downloaded");
  elem.click();
  document.body.removeChild(elem);
 }
}
//--------------------------------------------------------

function loadPage() {
 var fileInput = document.getElementById('files');

 fileInput.addEventListener('change', function(e) {
     var file = fileInput.files[0];
     var timestamp = new Date().toISOString();

     var reader = new FileReader();
     reader.onload = function(e) {
      handleOutput(timestamp + "." + fileInput.files[0].name, soapSampler2http(reader.result));
     }
     reader.readAsText(file);
 })
}

