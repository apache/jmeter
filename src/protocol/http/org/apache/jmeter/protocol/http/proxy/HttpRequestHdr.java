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

package org.apache.jmeter.protocol.http.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.jmeter.protocol.http.config.MultipartUrlConfig;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

//For unit tests, @see TestHttpRequestHdr

/**
 * The headers of the client HTTP request.
 * 
 */
public class HttpRequestHdr {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String HTTP = "http"; // $NON-NLS-1$
    private static final String HTTPS = "https"; // $NON-NLS-1$
    private static final String PROXY_CONNECTION = "proxy-connection"; // $NON-NLS-1$
    private static final String CONTENT_TYPE = "content-type"; // $NON-NLS-1$
    private static final String CONTENT_LENGTH = "content-length"; // $NON-NLS-1$

	/**
	 * Http Request method. Such as get or post.
	 */
	private String method = ""; // $NON-NLS-1$

	/**
	 * The requested url. The universal resource locator that hopefully uniquely
	 * describes the object or service the client is requesting.
	 */
	private String url = ""; // $NON-NLS-1$

	/**
	 * Version of http being used. Such as HTTP/1.0.
	 */
	private String version = ""; // NOTREAD // $NON-NLS-1$

	private String postData = ""; // $NON-NLS-1$

	private Map headers = new HashMap();

	/*
	 * Optionally number the requests
	 */
	private static final boolean numberRequests = 
        JMeterUtils.getPropDefault("proxy.number.requests", false); // $NON-NLS-1$

	private static int requestNumber = 0;// running number

	/**
	 * Parses a http header from a stream.
	 * 
	 * @param in
	 *            the stream to parse.
	 * @return array of bytes from client.
	 */
	public byte[] parse(InputStream in) throws IOException {
		boolean inHeaders = true;
		int readLength = 0;
		int dataLength = 0;
		boolean first = true;
		ByteArrayOutputStream clientRequest = new ByteArrayOutputStream();
		ByteArrayOutputStream line = new ByteArrayOutputStream();
		int x;
		while ((inHeaders || readLength < dataLength) && ((x = in.read()) != -1)) {
			line.write(x);
			clientRequest.write(x);
			if (inHeaders && (byte) x == (byte) '\n') { // $NON-NLS-1$
				if (line.size() < 3) {
					inHeaders = false;
				}
				if (first) {
					parseFirstLine(line.toString());
					first = false;
				} else {
					dataLength = Math.max(parseLine(line.toString()), dataLength);
				}
                if (log.isDebugEnabled()){
    				log.debug("Client Request Line: " + line.toString());
                }
				line.reset();
			} else if (!inHeaders) {
				readLength++;
			}
		}
		postData = line.toString().trim();
        if (log.isDebugEnabled()){
    		log.debug("postData: " + postData);
    		log.debug("Request: " + clientRequest.toString());
        }
		return clientRequest.toByteArray();
	}

	private void parseFirstLine(String firstLine) {
        if (log.isDebugEnabled())
    		log.debug("browser request: " + firstLine);
		StringTokenizer tz = new StringTokenizer(firstLine);
		method = getToken(tz).toUpperCase();
		url = getToken(tz);
        if (log.isDebugEnabled())
    		log.debug("parsed url: " + url);
		version = getToken(tz);
	}

    /*
     * Split line into name/value pairs and store in headers if relevant
     * If name = "content-length", then return value as int, else return 0
     */
	private int parseLine(String nextLine) {
		StringTokenizer tz;
		tz = new StringTokenizer(nextLine);
		String token = getToken(tz);
		// look for termination of HTTP command
		if (0 == token.length()) {
			return 0;
		} else {
			String trimmed = token.trim();
            String name = trimmed.substring(0, trimmed.length() - 1);// drop ':'
			String value = getRemainder(tz);
			headers.put(name.toLowerCase(), new Header(name, value));
			if (name.equalsIgnoreCase(CONTENT_LENGTH)) {
				return Integer.parseInt(value);
			}
		}
		return 0;
	}

	public HeaderManager getHeaderManager() {
		HeaderManager manager = new HeaderManager();
		Iterator keys = headers.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (!key.equals(PROXY_CONNECTION) && !key.equals(CONTENT_LENGTH)) {
				manager.add((Header) headers.get(key));
			}
		}
		manager.setName("Browser-derived headers");
		manager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
		manager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
		return manager;
	}

	public HTTPSamplerBase getSampler() throws MalformedURLException, IOException, ProtocolException {
		// Damn! A whole new GUI just to instantiate a test element?
		// Isn't there a beter way?
		HttpTestSampleGui tempGui = new HttpTestSampleGui();
		HTTPSamplerBase result = createSampler();
		tempGui.configure(result);
		tempGui.modifyTestElement(result);
		result.setFollowRedirects(false);
		result.setUseKeepAlive(true);
        if (log.isDebugEnabled())
    		log.debug("getSampler: sampler path = " + result.getPath());
		return result;
	}

	private String getContentType() {
		Header contentTypeHeader = (Header) headers.get(CONTENT_TYPE);
		if (contentTypeHeader != null) {
			return contentTypeHeader.getValue();
		}
		return ""; // $NON-NLS-1$
	}

	private MultipartUrlConfig isMultipart(String contentType) {
		if (contentType != null && contentType.startsWith(MultipartUrlConfig.MULTIPART_FORM)) {
			return new MultipartUrlConfig(contentType.substring(contentType.indexOf("oundary=") + 8));
		} else {
			return null;
		}
	}

	private HTTPSamplerBase createSampler() {
		MultipartUrlConfig urlConfig = null;
		HTTPSamplerBase sampler = HTTPSamplerFactory.newInstance();
		sampler.setDomain(serverName());
        if (log.isDebugEnabled())
    		log.debug("Proxy: setting server: " + sampler.getDomain());
		sampler.setMethod(method);
		log.debug("Proxy: method server: " + sampler.getMethod());
		sampler.setPath(serverUrl());
        if (log.isDebugEnabled())
    		log.debug("Proxy: setting path: " + sampler.getPath());
		if (numberRequests) {
			requestNumber++;
			sampler.setName(requestNumber + " " + sampler.getPath());
		} else {
			sampler.setName(sampler.getPath());
		}
		sampler.setPort(serverPort());
        if (log.isDebugEnabled())
            log.debug("Proxy: setting port: " + sampler.getPort());
		if (url.indexOf("//") > -1) {
			String protocol = url.substring(0, url.indexOf(":"));
            if (log.isDebugEnabled())
    			log.debug("Proxy: setting protocol to : " + protocol);
			sampler.setProtocol(protocol);
		} else if (sampler.getPort() == 443) {
			sampler.setProtocol(HTTPS);
            if (log.isDebugEnabled())
    			log.debug("Proxy: setting protocol to https");
		} else {
            if (log.isDebugEnabled())
    			log.debug("Proxy setting default protocol to: http");
			sampler.setProtocol(HTTP);
		}
		if ((urlConfig = isMultipart(getContentType())) != null) {
			urlConfig.parseArguments(postData);
			sampler.setArguments(urlConfig.getArguments());
			sampler.setFileField(urlConfig.getFileFieldName());
			sampler.setFilename(urlConfig.getFilename());
			sampler.setMimetype(urlConfig.getMimeType());
        } else if (postData != null && postData.trim().startsWith("<?")) {
            sampler.addNonEncodedArgument("", postData, ""); //used when postData is pure xml (ex. an xml-rpc call)
		} else {
			sampler.parseArguments(postData); //standard name=value postData
		}
        if (log.isDebugEnabled())
    		log.debug("sampler path = " + sampler.getPath());
		return sampler;
	}

	//
	// Parsing Methods
	//

	/**
	 * Find the //server.name from an url.
	 * 
	 * @return server's internet name
	 */
	private String serverName() {
		// chop to "server.name:x/thing"
		String str = url;
		int i = str.indexOf("//"); // $NON-NLS-1$
		if (i > 0) {
			str = str.substring(i + 2);
		}
		// chop to server.name:xx
		i = str.indexOf("/"); // $NON-NLS-1$
		if (0 < i) {
			str = str.substring(0, i);
		}
		// chop to server.name
		i = str.indexOf(":"); // $NON-NLS-1$
		if (0 < i) {
			str = str.substring(0, i);
		}
		return str;
	}

	/**
	 * Find the :PORT form http://server.ect:PORT/some/file.xxx
	 * 
	 * @return server's port
	 */
	public int serverPort() {
		String str = url;
		// chop to "server.name:x/thing"
		int i = str.indexOf("//");
		if (i > 0) {
			str = str.substring(i + 2);
		}
		// chop to server.name:xx
		i = str.indexOf("/");
		if (0 < i) {
			str = str.substring(0, i);
		}
		// chop XX
		i = str.indexOf(":");
		if (0 < i) {
			return Integer.parseInt(str.substring(i + 1).trim());
		}
		return 80;
	}

	/**
	 * Find the /some/file.xxxx form http://server.ect:PORT/some/file.xxx
	 * 
	 * @return the deproxied url
	 */
	public String serverUrl() {
		String str = url;
		int i = str.indexOf("//");
		if (i > 0) {
			str = str.substring(i + 2);
		}
		i = str.indexOf("/");
		if (i < 0) {
			return "";
		}
		return str.substring(i);
	}

	/**
	 * Returns the next token in a string.
	 * 
	 * @param tk
	 *            String that is partially tokenized.
	 * @return The remainder
	 */
	private String getToken(StringTokenizer tk) {
		if (tk.hasMoreTokens()) {
			return tk.nextToken();
		}
		return "";// $NON-NLS-1$
	}

	/**
	 * Returns the remainder of a tokenized string.
	 * 
	 * @param tk
	 *            String that is partially tokenized.
	 * @return The remainder
	 */
	private String getRemainder(StringTokenizer tk) {
		StringBuffer strBuff = new StringBuffer();
		if (tk.hasMoreTokens()) {
			strBuff.append(tk.nextToken());
		}
		while (tk.hasMoreTokens()) {
            strBuff.append(" "); // $NON-NLS-1$
            strBuff.append(tk.nextToken());
		}
		return strBuff.toString();
	}

}
