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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.CharUtils;
import org.apache.jmeter.protocol.http.config.MultipartUrlConfig;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.util.ConversionUtils;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The headers of the client HTTP request.
 */
public class HttpRequestHdr {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestHdr.class);

    private static final String HTTP = "http"; // $NON-NLS-1$
    private static final String HTTPS = "https"; // $NON-NLS-1$
    private static final String PROXY_CONNECTION = "proxy-connection"; // $NON-NLS-1$
    public static final String CONTENT_TYPE = "content-type"; // $NON-NLS-1$
    public static final String CONTENT_LENGTH = "content-length"; // $NON-NLS-1$


    /**
     * Http Request method, uppercased, e.g. GET or POST.
     */
    private String method = ""; // $NON-NLS-1$

    /** CONNECT url. */
    private String paramHttps = ""; // $NON-NLS-1$

    /**
     * The requested url. The universal resource locator that hopefully uniquely
     * describes the object or service the client is requesting.
     */
    private String url = ""; // $NON-NLS-1$

    /**
     * Version of http being used. Such as HTTP/1.0.
     */
    private String version = ""; // NOTREAD // $NON-NLS-1$

    private byte[] rawPostData;

    private final Map<String, Header> headers = new HashMap<>();

    private final String httpSamplerName;

    private HeaderManager headerManager;

    private String firstLine; // saved copy of first line for error reports

    private String prefix;
    
    private int httpSampleNameMode;

    public HttpRequestHdr() {
        this("", "");
    }

    /**
     * @param httpSamplerName the http sampler name
     */
    public HttpRequestHdr(String httpSamplerName) {
        this("", httpSamplerName);
    }
    
    /**
     * @param prefix Sampler prefix
     * @param httpSamplerName the http sampler name
     */
    public HttpRequestHdr(String prefix, String httpSamplerName) {
        this(prefix, httpSamplerName,0);
    }

    /**
     * @param prefix Sampler prefix
     * @param httpSamplerName the http sampler name
     * @param httpSampleNameMode the naming mode of sampler name
     */
    public HttpRequestHdr(String prefix, String httpSamplerName, int httpSampleNameMode) {
        this.prefix = prefix;
        this.httpSamplerName = httpSamplerName;
        this.firstLine = "" ; // $NON-NLS-1$
        this.httpSampleNameMode = httpSampleNameMode;
    }

    /**
     * Parses a http header from a stream.
     *
     * @param in
     *            the stream to parse.
     * @return array of bytes from client.
     * @throws IOException when reading the input stream fails
     */
    public byte[] parse(InputStream in) throws IOException {
        boolean inHeaders = true;
        int readLength = 0;
        int dataLength = 0;
        boolean firstLine = true;
        ByteArrayOutputStream clientRequest = new ByteArrayOutputStream();
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int x;
        while ((inHeaders || readLength < dataLength) && ((x = in.read()) != -1)) {
            line.write(x);
            clientRequest.write(x);
            if (firstLine && !CharUtils.isAscii((char) x)){// includes \n
                throw new IllegalArgumentException("Only ASCII supported in headers (perhaps SSL was used?)");
            }
            if (inHeaders && (byte) x == (byte) '\n') { // $NON-NLS-1$
                if (line.size() < 3) {
                    inHeaders = false;
                    firstLine = false; // cannot be first line either
                }
                final String reqLine = line.toString();
                if (firstLine) {
                    parseFirstLine(reqLine);
                    firstLine = false;
                } else {
                    // parse other header lines, looking for Content-Length
                    final int contentLen = parseLine(reqLine);
                    if (contentLen > 0) {
                        dataLength = contentLen; // Save the last valid content length one
                    }
                }
                if (log.isDebugEnabled()){
                    log.debug("Client Request Line: '" + reqLine.replaceFirst("\r\n$", "<CRLF>") + "'");
                }
                line.reset();
            } else if (!inHeaders) {
                readLength++;
            }
        }
        // Keep the raw post data
        rawPostData = line.toByteArray();

        if (log.isDebugEnabled()){
            log.debug("rawPostData in default JRE encoding: " + new String(rawPostData)); // TODO - charset?
            log.debug("Request: '" + clientRequest.toString().replaceAll("\r\n", "<CRLF>") + "'");
        }
        return clientRequest.toByteArray();
    }

    private void parseFirstLine(String firstLine) {
        this.firstLine = firstLine;
        if (log.isDebugEnabled()) {
            log.debug("browser request: " + firstLine.replaceFirst("\r\n$", "<CRLF>"));
        }
        StringTokenizer tz = new StringTokenizer(firstLine);
        method = getToken(tz).toUpperCase(java.util.Locale.ENGLISH);
        url = getToken(tz);
        version = getToken(tz);
        if (log.isDebugEnabled()) {
            log.debug("parsed method:   " + method);
            log.debug("parsed url/host: " + url); // will be host:port for CONNECT
            log.debug("parsed version:  " + version);
        }
        // SSL connection
        if (getMethod().startsWith(HTTPConstants.CONNECT)) {
            paramHttps = url;
            return; // Don't try to adjust the host name
        }
        /* The next line looks odd, but proxied HTTP requests look like:
         * GET http://www.apache.org/foundation/ HTTP/1.1
         * i.e. url starts with "http:", not "/"
         * whereas HTTPS proxy requests look like:
         * CONNECT www.google.co.uk:443 HTTP/1.1
         * followed by
         * GET /?gws_rd=cr HTTP/1.1
         */
        if (url.startsWith("/")) { // it must be a proxied HTTPS request
            url = HTTPS + "://" + paramHttps + url; // $NON-NLS-1$
        }
        // JAVA Impl accepts URLs with unsafe characters so don't do anything
        if(HTTPSamplerFactory.IMPL_JAVA.equals(httpSamplerName)) {
            log.debug("First Line url: " + url);
            return;
        }
        try {
            // See Bug 54482
            URI testCleanUri = new URI(url);
            if(log.isDebugEnabled()) {
                log.debug("Successfully built URI from url:"+url+" => " + testCleanUri.toString());
            }
        } catch (URISyntaxException e) {
            log.warn("Url '" + url + "' contains unsafe characters, will escape it, message:"+e.getMessage());
            try {
                String escapedUrl = ConversionUtils.escapeIllegalURLCharacters(url);
                if(log.isDebugEnabled()) {
                    log.debug("Successfully escaped url:'"+url +"' to:'"+escapedUrl+"'");
                }
                url = escapedUrl;
            } catch (Exception e1) {
                log.error("Error escaping URL:'"+url+"', message:"+e1.getMessage());
            }
        }
        log.debug("First Line url: " + url);
    }

    /*
     * Split line into name/value pairs and store in headers if relevant
     * If name = "content-length", then return value as int, else return 0
     */
    private int parseLine(String nextLine) {
        int colon = nextLine.indexOf(':');
        if (colon <= 0){
            return 0; // Nothing to do
        }
        String name = nextLine.substring(0, colon).trim();
        String value = nextLine.substring(colon+1).trim();
        headers.put(name.toLowerCase(java.util.Locale.ENGLISH), new Header(name, value));
        if (name.equalsIgnoreCase(CONTENT_LENGTH)) {
            return Integer.parseInt(value);
        }
        return 0;
    }

    private HeaderManager createHeaderManager() {
        HeaderManager manager = new HeaderManager();
        for (Map.Entry<String, Header> entry : headers.entrySet()) {
            final String key = entry.getKey();
            if (!key.equals(PROXY_CONNECTION)
             && !key.equals(CONTENT_LENGTH)
             && !key.equalsIgnoreCase(HTTPConstants.HEADER_CONNECTION)) {
                manager.add(entry.getValue());
            }
        }
        manager.setName(JMeterUtils.getResString("header_manager_title")); // $NON-NLS-1$
        manager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        manager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
        return manager;
    }

    public HeaderManager getHeaderManager() {
        if(headerManager == null) {
            headerManager = createHeaderManager();
        }
        return headerManager;
    }

    public String getContentType() {
        Header contentTypeHeader = headers.get(CONTENT_TYPE);
        if (contentTypeHeader != null) {
            return contentTypeHeader.getValue();
        }
        return null;
    }

    private boolean isMultipart(String contentType) {
        if (contentType != null && contentType.startsWith(HTTPConstants.MULTIPART_FORM_DATA)) {
            return true;
        }
        return false;
    }

    public MultipartUrlConfig getMultipartConfig(String contentType) {
        if(isMultipart(contentType)) {
            // Get the boundary string for the multiparts from the content type
            String boundaryString = contentType.substring(contentType.toLowerCase(java.util.Locale.ENGLISH).indexOf("boundary=") + "boundary=".length());
            return new MultipartUrlConfig(boundaryString);
        }
        return null;
    }

    //
    // Parsing Methods
    //

    /**
     * Find the //server.name from an url.
     *
     * @return server's internet name
     */
    public String serverName() {
        // chop to "server.name:x/thing"
        String str = url;
        int i = str.indexOf("//"); // $NON-NLS-1$
        if (i > 0) {
            str = str.substring(i + 2);
        }
        // chop to server.name:xx
        i = str.indexOf('/'); // $NON-NLS-1$
        if (0 < i) {
            str = str.substring(0, i);
        }
        // chop to server.name
        i = str.lastIndexOf(':'); // $NON-NLS-1$
        if (0 < i) {
            str = str.substring(0, i);
        }
        // Handle IPv6 urls
        if(str.startsWith("[")&& str.endsWith("]")) {
            return str.substring(1, str.length()-1);
        }
        return str;
    }

    // TODO replace repeated substr() above and below with more efficient method.

    /**
     * Find the :PORT from http://server.ect:PORT/some/file.xxx
     *
     * @return server's port (or UNSPECIFIED if not found)
     */
    public int serverPort() {
        String str = url;
        // chop to "server.name:xhing"
        int i = str.indexOf("//");
        if (i > 0) {
            str = str.substring(i + 2);
        }
        // chop to server.name:xx
        i = str.indexOf('/');
        if (0 < i) {
            str = str.substring(0, i);
        }
        // chop to server.name
        i = str.lastIndexOf(':');
        if (0 < i) {
            return Integer.parseInt(str.substring(i + 1).trim());
        }
        return HTTPSamplerBase.UNSPECIFIED_PORT;
    }

    /**
     * Find the /some/file.xxxx from http://server.ect:PORT/some/file.xxx
     *
     * @return the path
     */
    public String getPath() {
        String str = url;
        int i = str.indexOf("//");
        if (i > 0) {
            str = str.substring(i + 2);
        }
        i = str.indexOf('/');
        if (i < 0) {
            return "";
        }
        return str.substring(i);
    }

    /**
     * Returns the url string extracted from the first line of the client request.
     *
     * @return the url
     */
    public String getUrl(){
        return url;
    }

    /**
     * Returns the method string extracted from the first line of the client request.
     *
     * @return the method (will always be upper case)
     */
    public String getMethod(){
        return method;
    }

    public String getFirstLine() {
        return firstLine;
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


    public String getUrlWithoutQuery(URL _url) {
        String fullUrl = _url.toString();
        String urlWithoutQuery = fullUrl;
        String query = _url.getQuery();
        if(query != null) {
            // Get rid of the query and the ?
            urlWithoutQuery = urlWithoutQuery.substring(0, urlWithoutQuery.length() - query.length() - 1);
        }
        return urlWithoutQuery;
    }

    /**
     * @return the httpSamplerName
     */
    public String getHttpSamplerName() {
        return httpSamplerName;
    }

    /**
     * @return byte[] Raw post data
     */
    public byte[] getRawPostData() {
        return rawPostData;
    }

    /**
     * @param sampler {@link HTTPSamplerBase}
     * @return String Protocol (http or https)
     */
    public String getProtocol(HTTPSamplerBase sampler) {
        if (url.contains("//")) {
            String protocol = url.substring(0, url.indexOf(':'));
            if (log.isDebugEnabled()) {
                log.debug("Proxy: setting protocol to : " + protocol);
            }
            return protocol;
        } else if (sampler.getPort() == HTTPConstants.DEFAULT_HTTPS_PORT) {
            if (log.isDebugEnabled()) {
                log.debug("Proxy: setting protocol to https");
            }
            return HTTPS;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Proxy setting default protocol to: http");
            }
            return HTTP;
        }
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }
    
    /**
     * @return the httpSampleNameMode
     */
    public int getHttpSampleNameMode() {
        return httpSampleNameMode;
    }
}
