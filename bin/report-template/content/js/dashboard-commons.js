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
