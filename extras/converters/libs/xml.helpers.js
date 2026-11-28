/*jslint white: true */
/*jslint es6 */


"use strict";

function get_xml_firstChild(n) {
    var x = n.firstChild;
    while (x.nodeType !== 1) {
        x = x.nextSibling;
    }
    return x;
}

function get_xml_nextSibling(n) {
    var x = n.nextSibling;
    if (x !== null) {
        if (x.nodeType !== 1) {
            x = get_xml_nextSibling(x);
        }
    }
    return x;
}


//----------------------------------------------------
// SANITIZE NULL CONTENT 
//----------------------------------------------------
function sanitizeValue(object) {
 if (object){
  return object.textContent;
 }
 return "null";
}

