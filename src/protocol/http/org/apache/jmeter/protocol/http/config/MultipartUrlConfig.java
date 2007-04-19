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
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * @author Michael Stover
 */
public class MultipartUrlConfig implements Serializable {

	/** @deprecated use HTTPSamplerBase.MULTIPART_FORM_DATA instead */
	public static final String MULTIPART_FORM = "multipart/form-data";

	private String boundary, filename, fileField, mimetype;

	private Arguments args;

	public MultipartUrlConfig() {
		args = new Arguments();
	}

	public MultipartUrlConfig(String boundary) {
		this();
		this.boundary = boundary;
	}

	public void setBoundary(String boundary) {
		this.boundary = boundary;
	}

	public String getBoundary() {
		return boundary;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	public Arguments getArguments() {
		return args;
	}

	public void setFileFieldName(String name) {
		this.fileField = name;
	}

	public String getFileFieldName() {
		return fileField;
	}

	public void setMimeType(String type) {
		mimetype = type;
	}

	public String getMimeType() {
		return mimetype;
	}

	public void addArgument(String name, String value) {
		Arguments myArgs = this.getArguments();
		myArgs.addArgument(new HTTPArgument(name, value));
	}

	public void addArgument(String name, String value, String metadata) {
		Arguments myArgs = this.getArguments();
		myArgs.addArgument(new HTTPArgument(name, value, metadata));
	}

    /**
     * @deprecated values in a multipart/form-data are not urlencoded,
     * so it does not make sense to add a value as a encoded value
     */
	public void addEncodedArgument(String name, String value) {
		Arguments myArgs = getArguments();
		HTTPArgument arg = new HTTPArgument(name, value, true);
		if (arg.getName().equals(arg.getEncodedName()) && arg.getValue().equals(arg.getEncodedValue())) {
			arg.setAlwaysEncoded(false);
		}
		myArgs.addArgument(arg);
	}
    
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
		String[] parts = JOrphanUtils.split(queryString, "--" + getBoundary());
		for (int i = 0; i < parts.length; i++) {
            String contentDisposition = getHeaderValue("Content-disposition", parts[i]);
            String contentType = getHeaderValue("Content-type", parts[i]);
            // Check if it is form data
            if (contentDisposition != null && contentDisposition.indexOf("form-data") > -1) {
                // Get the form field name
                int index = contentDisposition.indexOf("name=\"") + 6;
                String name = contentDisposition.substring(index, contentDisposition.indexOf("\"", index));

                // Check if it is a file being uploaded
                if (contentDisposition.indexOf("filename=") > -1) {
                    // Get the filename
                    index = contentDisposition.indexOf("filename=\"") + 10;
                    String fn = contentDisposition.substring(index, contentDisposition.indexOf("\"", index));
                    if(fn != null && fn.length() > 0) {
                        // Set the values retrieves for the file upload
                        this.setFileFieldName(name);
                        this.setFilename(fn);
                        this.setMimeType(contentType);
                    }
                }
                else {
                    // Find the first empty line of the multipart, it signals end of headers for multipart
                    int indexEmptyLfCrLfLinePos = parts[i].indexOf("\n\r\n");
                    int indexEmptyLfLfLinePos = parts[i].indexOf("\n\n");
                    String value = null;
                    if(indexEmptyLfCrLfLinePos > -1) {
                        value = parts[i].substring(indexEmptyLfCrLfLinePos).trim();
                    }
                    else if(indexEmptyLfLfLinePos > -1) {
                        value = parts[i].substring(indexEmptyLfLfLinePos).trim();
                    }
                    this.addNonEncodedArgument(name, value);
                }
            }
		}
	}
    
    private String getHeaderValue(String headerName, String multiPart) {
        String regularExpression = headerName + "\\s*:\\s*(.*)$";
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
