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

package org.apache.jmeter.protocol.http.config;

import java.io.Serializable;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPFileArgs;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * Configuration element which handles HTTP Parameters and files to be uploaded
 */
public class MultipartUrlConfig implements Serializable {

    private static final long serialVersionUID = 240L;

    private final String boundary;

    private final Arguments args;

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

// NOT USED
//    /**
//     * @deprecated values in a multipart/form-data are not urlencoded,
//     * so it does not make sense to add a value as a encoded value
//     */
//  public void addEncodedArgument(String name, String value) {
//      Arguments myArgs = getArguments();
//      HTTPArgument arg = new HTTPArgument(name, value, true);
//      if (arg.getName().equals(arg.getEncodedName()) && arg.getValue().equals(arg.getEncodedValue())) {
//          arg.setAlwaysEncoded(false);
//      }
//      myArgs.addArgument(arg);
//  }

    /**
     * Add a value that is not URL encoded, and make sure it
     * appears in the GUI that it will not be encoded when
     * the request is sent.
     *
     * @param name
     * @param value
     */
    private void addNonEncodedArgument(String name, String value) {
        Arguments myArgs = getArguments();
        // The value is not encoded
        HTTPArgument arg = new HTTPArgument(name, value, false);
        // Let the GUI show that it will not be encoded
        arg.setAlwaysEncoded(false);
        myArgs.addArgument(arg);
    }

    /**
     * This method allows a proxy server to send over the raw text from a
     * browser's output stream to be parsed and stored correctly into the
     * UrlConfig object.
     */
    public void parseArguments(String queryString) {
        String[] parts = JOrphanUtils.split(queryString, "--" + getBoundary()); //$NON-NLS-1$
        for (int i = 0; i < parts.length; i++) {
            String contentDisposition = getHeaderValue("Content-disposition", parts[i]); //$NON-NLS-1$
            String contentType = getHeaderValue("Content-type", parts[i]); //$NON-NLS-1$
            // Check if it is form data
            if (contentDisposition != null && contentDisposition.indexOf("form-data") > -1) { //$NON-NLS-1$
                // Get the form field name
                final String namePrefix = "name=\""; //$NON-NLS-1$
                int index = contentDisposition.indexOf(namePrefix) + namePrefix.length();
                String name = contentDisposition.substring(index, contentDisposition.indexOf("\"", index)); //$NON-NLS-1$

                // Check if it is a file being uploaded
                final String filenamePrefix = "filename=\""; //$NON-NLS-1$
                if (contentDisposition.indexOf(filenamePrefix) > -1) {
                    // Get the filename
                    index = contentDisposition.indexOf(filenamePrefix) + filenamePrefix.length();
                    String path = contentDisposition.substring(index, contentDisposition.indexOf("\"", index)); //$NON-NLS-1$
                    if(path != null && path.length() > 0) {
                        // Set the values retrieved for the file upload
                        files.addHTTPFileArg(path, name, contentType);
                    }
                }
                else {
                    // Find the first empty line of the multipart, it signals end of headers for multipart
                    // Agents are supposed to terminate lines in CRLF:
                    final String CRLF = "\r\n";
                    final String CRLFCRLF = "\r\n\r\n";
                    // Code also allows for LF only (not sure why - perhaps because the test code uses it?)
                    final String LF = "\n";
                    final String LFLF = "\n\n";
                    int indexEmptyCrLfCrLfLinePos = parts[i].indexOf(CRLFCRLF); //$NON-NLS-1$
                    int indexEmptyLfLfLinePos = parts[i].indexOf(LFLF); //$NON-NLS-1$
                    String value = null;
                    if(indexEmptyCrLfCrLfLinePos > -1) {// CRLF blank line found
                        value = parts[i].substring(indexEmptyCrLfCrLfLinePos+CRLFCRLF.length(),parts[i].lastIndexOf(CRLF));
                    } else if(indexEmptyLfLfLinePos > -1) { // LF blank line found
                        value = parts[i].substring(indexEmptyLfLfLinePos+LFLF.length(),parts[i].lastIndexOf(LF));
                    }
                    this.addNonEncodedArgument(name, value);
                }
            }
        }
    }

    private String getHeaderValue(String headerName, String multiPart) {
        String regularExpression = headerName + "\\s*:\\s*(.*)$"; //$NON-NLS-1$
        Perl5Matcher localMatcher = JMeterUtils.getMatcher();
        Pattern pattern = JMeterUtils.getPattern(regularExpression, Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.MULTILINE_MASK);
        if(localMatcher.contains(multiPart, pattern)) {
            return localMatcher.getMatch().group(1).trim();
        }
        else {
            return null;
        }
    }
}
