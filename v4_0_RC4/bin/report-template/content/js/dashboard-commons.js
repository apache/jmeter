/*
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
*/

/**
 * From https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/Math/round
 * Licensed under https://creativecommons.org/licenses/by-sa/2.5/
 */
// Closure
(function() {
  /**
   * Decimal adjustment of a number.
   *
   * @param {String}  type  The type of adjustment.
   * @param {Number}  value The number.
   * @param {Integer} exp   The exponent (the 10 logarithm of the adjustment base).
   * @returns {Number} The adjusted value.
   */
  function decimalAdjust(type, value, exp) {
    // If the exp is undefined or zero...
    if (typeof exp === 'undefined' || +exp === 0) {
      return Math[type](value);
    }
    value = +value;
    exp = +exp;
    // If the value is not a number or the exp is not an integer...
    if (isNaN(value) || !(typeof exp === 'number' && exp % 1 === 0)) {
      return NaN;
    }
    // Shift
    value = value.toString().split('e');
    value = Math[type](+(value[0] + 'e' + (value[1] ? (+value[1] - exp) : -exp)));
    // Shift back
    value = value.toString().split('e');
    return +(value[0] + 'e' + (value[1] ? (+value[1] + exp) : exp));
  }

  // Decimal round
  if (!Math.round10) {
    Math.round10 = function(value, exp) {
      return decimalAdjust('round', value, exp);
    };
  }
  // Decimal floor
  if (!Math.floor10) {
    Math.floor10 = function(value, exp) {
      return decimalAdjust('floor', value, exp);
    };
  }
  // Decimal ceil
  if (!Math.ceil10) {
    Math.ceil10 = function(value, exp) {
      return decimalAdjust('ceil', value, exp);
    };
  }
})();

/*
 * Suffixes the specified value with a unit
 * The spaced argument defines whether a space character is introduced.
 */
function formatUnit(value, unit, spaced){
    return spaced ? value + " " + unit : value + unit;
}

/*
 * Gets a string representing the specified duration in milliseconds.
 * 
 * E.g : duration = 20000100, returns "45 min 20 sec 100 ms"
 */
function formatDuration(duration, spaced) {
    var type = $.type(duration);
    if (type === "string")
        return duration;

    // Calculate each part of the string
    var days = Math.floor(duration / 86400000); // 1000 * 60 * 60 * 24 = 1 day
    duration %= 8640000;

    var hours = Math.floor(duration / 3600000); // 1000 * 60 *60 = 1 hour
    duration %= 3600000;

    var minutes = Math.floor(duration / 60000); // 1000 * 60 = 1 minute
    duration %= 60000;

    var seconds = Math.floor(duration / 1000); // 1 second
    duration %= 1000;

    // Add non zero part.
    var formatArray = [];
    if (days > 0)
        formatArray.push(formatUnit(days, " day(s)", spaced));

    if (hours > 0)
        formatArray.push(formatUnit(hours, " hour(s)", spaced));

    if (minutes > 0)
        formatArray.push(formatUnit(minutes," min", spaced));

    if (seconds > 0)
        formatArray.push(formatUnit(seconds, " sec", spaced));

    if (duration > 0)
        formatArray.push(formatUnit(duration, " ms", spaced));

    // Build the string
    return formatArray.join(" ");
}

/*
 * Gets axis label for the specified granularity
 */
function getElapsedTimeLabel(granularity) {
    return "Elapsed Time (granularity: " + formatDuration(granularity) + ")";
}

/*
 * Gets axis label for the specified granularity
 */
function getConnectTimeLabel(granularity) {
    return "Connect Time (granularity: " + formatDuration(granularity) + ")";
}

//Get the property value of an object using the specified key
//Returns the property value if all properties in the key exist; undefined
//otherwise.
function getProperty(key, obj) {
    return key.split('.').reduce(function(prop, subprop){
        return prop && prop[subprop];
    }, obj);
}

/*
 * Removes quotes from the specified string 
 */
function unquote(str, quoteChar) {
    quoteChar = quoteChar || '"';
    if (str.length > 0 && str[0] === quoteChar && str[str.length - 1] === quoteChar)
        return str.slice(1, str.length - 1);
    else
        return str;
};

/*
 * This comparison function evaluates abscissas to sort array of coordinates.
 */
function compareByXCoordinate(coord1, coord2) {
    return coord2[0] - coord1[0];
}
