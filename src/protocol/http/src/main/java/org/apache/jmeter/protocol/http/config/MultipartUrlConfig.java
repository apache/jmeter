/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.http.config;

import java.io.Serializable;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPFileArgs;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration element which handles HTTP Parameters and files to be uploaded
 */
public class MultipartUrlConfig implements Serializable {

    private static final long serialVersionUID = 240L;

    private static final String CRLF = "\r\n";
    private static final String CRLFCRLF = "\r\n\r\n";
    // Code also allows for LF only (not sure why - perhaps because the test code uses it?)
    private static final String LF = "\n";
    private static final String LFLF = "\n\n";

    private final String boundary;

    private final Arguments args;

    private static final Logger log = LoggerFactory.getLogger(MultipartUrlConfig.class);

    private static final boolean USE_JAVA_REGEX = !JMeterUtils.getPropDefault(
            "jmeter.regex.engine", "oro").equalsIgnoreCase("oro");

    /**
     * HTTPFileArgs list to be uploaded with http request.
     */
    private final HTTPFileArgs files;

    /**
     * @deprecated only for use by unit tests
     */
    @Deprecated
    public MultipartUrlConfig(){
        this(null);
    }

    // called by HttpRequestHdr
    public MultipartUrlConfig(String boundary) {
        args = new Arguments();
        files = new HTTPFileArgs();
        this.boundary = boundary;
    }

    public String getBoundary() {
        return boundary;
    }

    public Arguments getArguments() {
        return args;
    }

    public void addArgument(String name, String value) {
        Arguments myArgs = this.getArguments();
        myArgs.addArgument(new HTTPArgument(name, value));
    }

    public void addArgument(String name, String value, String metadata) {
        Arguments myArgs = this.getArguments();
        myArgs.addArgument(new HTTPArgument(name, value, metadata));
    }

    public HTTPFileArgs getHTTPFileArgs() {
        return files;
    }

    /**
     * Add a value that is not URL encoded, and make sure it
     * appears in the GUI that it will not be encoded when
     * the request is sent.
     *
     * @param name
     * @param value
     * @param contentType can include charset or not, for example:  "application/json; charset=UTF-8" or  "application/json"
     */
    private void addNonEncodedArgument(String name, String value, String contentType) {
        Arguments myArgs = getArguments();
        // The value is not encoded
        HTTPArgument arg = new HTTPArgument(name, value, false);
        if(!StringUtils.isEmpty(contentType)) {
            int indexOfSemiColon = contentType.indexOf(';');
            if(indexOfSemiColon > 0) {
                arg.setContentType(contentType.substring(0, indexOfSemiColon));
            } else {
                arg.setContentType(contentType);
            }
        }
        // Let the GUI show that it will not be encoded
        arg.setAlwaysEncoded(false);
        myArgs.addArgument(arg);
    }

    /**
     * This method allows a proxy server to send over the raw text from a
     * browser's output stream to be parsed and stored correctly into the
     * UrlConfig object.
     *
     * @param queryString text to parse
     */
    public void parseArguments(String queryString) {
        String[] parts = JOrphanUtils.split(queryString, "--" + getBoundary()); //$NON-NLS-1$
        for (String part : parts) {
            String contentDisposition = getHeaderValue("Content-disposition", part); //$NON-NLS-1$
            String contentType = getHeaderValue("Content-type", part); //$NON-NLS-1$
            // Check if it is form data
            if (contentDisposition != null && contentDisposition.contains("form-data")) { //$NON-NLS-1$
                // Get the form field name
                HeaderElement[] headerElements = null;
                try {
                    headerElements = BasicHeaderValueParser.parseElements(
                            contentDisposition,
                            BasicHeaderValueParser.INSTANCE);
                } catch (ParseException e) {
                    log.info("Can't parse header {}", contentDisposition, e);
                }
                String name = "";
                String path = null;
                if (headerElements != null) {
                    for (HeaderElement element : headerElements) {
                        name = getParameterValue(element, "name", "");
                        path = getParameterValue(element, "filename", null);
                    }
                }
                if (path != null && !path.isEmpty()) {
                    // Set the values retrieved for the file upload
                    files.addHTTPFileArg(path, name, contentType);
                } else {
                    // Find the first empty line of the multipart, it signals end of headers for multipart
                    // Agents are supposed to terminate lines in CRLF:
                    int indexEmptyCrLfCrLfLinePos = part.indexOf(CRLFCRLF); //$NON-NLS-1$
                    int indexEmptyLfLfLinePos = part.indexOf(LFLF); //$NON-NLS-1$
                    String value = null;
                    if (indexEmptyCrLfCrLfLinePos > -1) {// CRLF blank line found
                        value = part.substring(indexEmptyCrLfCrLfLinePos + CRLFCRLF.length(), part.lastIndexOf(CRLF));
                    } else if (indexEmptyLfLfLinePos > -1) { // LF blank line found
                        value = part.substring(indexEmptyLfLfLinePos + LFLF.length(), part.lastIndexOf(LF));
                    }
                    this.addNonEncodedArgument(name, value, contentType);
                }
            }
        }
    }

    private static String getParameterValue(HeaderElement element, String name, String defaultValue) {
        NameValuePair parameter = element.getParameterByName(name);
        if (parameter == null) {
            return defaultValue;
        }
        return parameter.getValue();
    }

    private static String getHeaderValue(String headerName, String multiPart) {
        String regularExpression = headerName + "\\s*:\\s*(.*)$"; //$NON-NLS-1$
        if (USE_JAVA_REGEX) {
            return getHeaderValueWithJavaRegex(multiPart, regularExpression);
        }
        return getHeaderValueWithOroRegex(multiPart, regularExpression);
    }

    private static String getHeaderValueWithJavaRegex(String multiPart, String regularExpression) {
        java.util.regex.Pattern pattern = JMeterUtils.compilePattern(regularExpression,
                 java.util.regex.Pattern.CASE_INSENSITIVE
                        | java.util.regex.Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(multiPart);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private static String getHeaderValueWithOroRegex(String multiPart, String regularExpression) {
        Perl5Matcher localMatcher = JMeterUtils.getMatcher();
        Pattern pattern = JMeterUtils.getPattern(regularExpression,
                Perl5Compiler.READ_ONLY_MASK
                        | Perl5Compiler.CASE_INSENSITIVE_MASK
                        | Perl5Compiler.MULTILINE_MASK);
        if(localMatcher.contains(multiPart, pattern)) {
            return localMatcher.getMatch().group(1).trim();
        }
        return null;
    }
}
