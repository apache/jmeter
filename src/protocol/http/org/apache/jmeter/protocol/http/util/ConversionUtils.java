/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.protocol.http.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jorphan.util.JOrphanUtils;

// @see TestHTTPUtils for unit tests

/**
 * General purpose conversion utilities related to HTTP/HTML
 */
public class ConversionUtils {

    private static final String CHARSET_EQ = "charset="; // $NON-NLS-1$
    private static final int CHARSET_EQ_LEN = CHARSET_EQ.length();

    /**
     * Extract the encoding (charset) from the Content-Type,
     * e.g. "text/html; charset=utf-8".
     *
     * @param contentType
     * @return the charset encoding - or null, if none was found or the charset is not supported
     */
    public static String getEncodingFromContentType(String contentType){
        String charSet = null;
        if (contentType != null) {
            int charSetStartPos = contentType.toLowerCase(java.util.Locale.ENGLISH).indexOf(CHARSET_EQ);
            if (charSetStartPos >= 0) {
                charSet = contentType.substring(charSetStartPos + CHARSET_EQ_LEN);
                if (charSet != null) {
                    // Remove quotes from charset name
                    charSet = JOrphanUtils.replaceAllChars(charSet, '"', "");
                    charSet = charSet.trim();
                    if (charSet.length() > 0) {
                        // See Bug 44784
                        int semi = charSet.indexOf(";");
                        if (semi == 0){
                            return null;
                        }
                        if (semi != -1) {
                            charSet = charSet.substring(0,semi);
                        }
                        if (!Charset.isSupported(charSet)){
                            return null;
                        }
                        return charSet;
                    }
                    return null;
                }
            }
        }
        return charSet;
    }

    /**
     * Generate a relative URL, allowing for extraneous leading "../" segments.
     * The Java {@link URL#URL(URL, String)} constructor does not remove these.
     *
     * @param baseURL
     * @param location relative location, possibly with extraneous leading "../"
     * @return URL with extraneous ../ removed
     * @throws MalformedURLException
     */
    public static URL makeRelativeURL(URL baseURL, String location) throws MalformedURLException{
        URL initial = new URL(baseURL,location);
        // skip expensive processing if it cannot apply
        if (!location.startsWith("../")){// $NON-NLS-1$
            return initial;
        }
        String path = initial.getPath();
        // Match /../[../] etc.
        Pattern p = Pattern.compile("^/((?:\\.\\./)+)"); // $NON-NLS-1$
        Matcher m = p.matcher(path);
        if (m.lookingAt()){
            String prefix = m.group(1); // get ../ or ../../ etc.
            if (location.startsWith(prefix)){
                return new URL(baseURL, location.substring(prefix.length()));
            }
        }
        return initial;
    }
}
