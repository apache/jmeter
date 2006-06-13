// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import org.apache.jorphan.util.JOrphanUtils;

/**
 * @author Michael Stover
 * @version $Revision$
 */
public class MultipartUrlConfig implements Serializable {

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

	public void addEncodedArgument(String name, String value) {
		Arguments myArgs = getArguments();
		HTTPArgument arg = new HTTPArgument(name, value, true);
		if (arg.getName().equals(arg.getEncodedName()) && arg.getValue().equals(arg.getEncodedValue())) {
			arg.setAlwaysEncoded(false);
		}
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
			if (parts[i].indexOf("filename=") > -1) {
				int index = parts[i].indexOf("name=\"") + 6;
				String name = parts[i].substring(index, parts[i].indexOf("\"", index));
				index = parts[i].indexOf("filename=\"") + 10;
				String fn = parts[i].substring(index, parts[i].indexOf("\"", index));
				index = parts[i].indexOf("\n", index);
				index = parts[i].indexOf(":", index) + 1;
				String mt = parts[i].substring(index, parts[i].indexOf("\n", index)).trim();
				this.setFileFieldName(name);
				this.setFilename(fn);
				this.setMimeType(mt);
			} else if (parts[i].indexOf("name=") > -1) {
				int index = parts[i].indexOf("name=\"") + 6;
				String name = parts[i].substring(index, parts[i].indexOf("\"", index));
				index = parts[i].indexOf("\n", index) + 2;
				String value = parts[i].substring(index).trim();
				this.addEncodedArgument(name, value);
			}
		}
	}
}
