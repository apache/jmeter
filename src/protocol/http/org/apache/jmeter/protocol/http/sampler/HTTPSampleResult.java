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

package org.apache.jmeter.protocol.http.sampler;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

/**
 * This is a specialisation of the SampleResult class for the HTTP protocol.
 *
 */
public class HTTPSampleResult extends SampleResult {

    private static final long serialVersionUID = 240L;
    
    private static final String GETBYTES_TYPE_DEFAULT = "default";
    
    private static final String GETBYTES_TYPE_HEAD_CONTENTLENGTH = "calculate_headers_size+content-length_value";
    
    private static final String GETBYTES_TYPE_HEAD_DEFAULT = "calculate_headers_size+default";

    private static final String GETBYTES_TYPE = 
        JMeterUtils.getPropDefault("http.getbytes.type", GETBYTES_TYPE_DEFAULT); // $NON-NLS-1$

    private String cookies = ""; // never null

    private String method;

    /**
     * The raw value of the Location: header; may be null.
     * This is supposed to be an absolute URL:
     * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.30">RFC2616 sec14.30</a>
     * but is often relative.
     */
    private String redirectLocation;

    private String queryString = ""; // never null

    private static final String HTTP_NO_CONTENT_CODE = Integer.toString(HttpURLConnection.HTTP_NO_CONTENT);
    private static final String HTTP_NO_CONTENT_MSG = "No Content"; // $NON-NLS-1$

    public HTTPSampleResult() {
        super();
    }

    public HTTPSampleResult(long elapsed) {
        super(elapsed, true);
    }

    /**
     * Construct a 'parent' result for an already-existing result, essentially
     * cloning it
     *
     * @param res
     *            existing sample result
     */
    public HTTPSampleResult(HTTPSampleResult res) {
        super(res);
        method=res.method;
        cookies=res.cookies;
        queryString=res.queryString;
        redirectLocation=res.redirectLocation;
    }

    public void setHTTPMethod(String method) {
        this.method = method;
    }

    public String getHTTPMethod() {
        return method;
    }

    public void setRedirectLocation(String redirectLocation) {
        this.redirectLocation = redirectLocation;
    }

    public String getRedirectLocation() {
        return redirectLocation;
    }

    /**
     * Determine whether this result is a redirect.
     *
     * @return true iif res is an HTTP redirect response
     */
    public boolean isRedirect() {
        final String[] REDIRECT_CODES = { "301", "302", "303" }; // NOT 304!
        String code = getResponseCode();
        for (int i = 0; i < REDIRECT_CODES.length; i++) {
            if (REDIRECT_CODES[i].equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Overrides version in Sampler data to provide more details
     * <p>
     * {@inheritDoc}
     */
    @Override
    public String getSamplerData() {
        StringBuilder sb = new StringBuilder();
        sb.append(method);
        URL u = super.getURL();
        if (u != null) {
            sb.append(' ');
            sb.append(u.toString());
            sb.append("\n");
            // Include request body if it is a post or put
            if (HTTPConstants.POST.equals(method) || HTTPConstants.PUT.equals(method)) {
                sb.append("\n"+method+" data:\n");
                sb.append(queryString);
                sb.append("\n");
            }
            if (cookies.length()>0){
                sb.append("\nCookie Data:\n");
                sb.append(cookies);
            } else {
                sb.append("\n[no cookies]");
            }
            sb.append("\n");
        }
        final String sampData = super.getSamplerData();
        if (sampData != null){
            sb.append(sampData);
        }
        return sb.toString();
    }

    /**
     * @return cookies as a string
     */
    public String getCookies() {
        return cookies;
    }

    /**
     * @param string
     *            representing the cookies
     */
    public void setCookies(String string) {
        if (string == null) {
            cookies="";// $NON-NLS-1$
        } else {
            cookies = string;
        }
    }

    /**
     * Fetch the query string
     *
     * @return the query string
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * Save the query string
     *
     * @param string
     *            the query string
     */
    public void setQueryString(String string) {
        if (string == null ) {
            queryString="";// $NON-NLS-1$
        } else {
            queryString = string;
        }
    }
    /**
     * Overrides the method from SampleResult - so the encoding can be extracted from
     * the Meta content-type if necessary.
     *
     * Updates the dataEncoding field if the content-type is found.
     *
     * @return the dataEncoding value as a String
     */
    @Override
    public String getDataEncodingWithDefault() {
        if (getDataEncodingNoDefault() == null && getContentType().startsWith("text/html")){ // $NON-NLS-1$
            byte[] bytes=getResponseData();
            // get the start of the file
            // TODO - charset?
            String prefix = new String(bytes,0,Math.min(bytes.length, 1000)).toLowerCase(java.util.Locale.ENGLISH);
            // Extract the content-type if present
            final String METATAG = "<meta http-equiv=\"content-type\" content=\""; // $NON-NLS-1$
            int tagstart=prefix.indexOf(METATAG);
            if (tagstart!=-1){
                tagstart += METATAG.length();
                int tagend = prefix.indexOf("\"", tagstart); // $NON-NLS-1$
                if (tagend!=-1){
                    // TODO use fixed charset:
                    final String ct = new String(bytes,tagstart,tagend-tagstart); // TODO - charset?
                    setEncodingAndType(ct);// Update the dataEncoding
                }
            }
        }
        return super.getDataEncodingWithDefault(DEFAULT_HTTP_ENCODING);
    }

    public void setResponseNoContent(){
        setResponseCode(HTTP_NO_CONTENT_CODE);
        setResponseMessage(HTTP_NO_CONTENT_MSG);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.samplers.SampleResult#getBytes()
     */
    @Override
    public int getBytes() {
        if (GETBYTES_TYPE.equals(GETBYTES_TYPE_HEAD_CONTENTLENGTH)) {
            return calculateHeadersSize()
                    + JMeterUtils.getHeaderContentLength(this.getResponseHeaders());
        }
        if (GETBYTES_TYPE.equals(GETBYTES_TYPE_HEAD_DEFAULT)) {
            return calculateHeadersSize() + super.getBytes();
        }
        return super.getBytes(); // Default
    }

    /**
     * Calculate response headers size
     * 
     * @return the size response headers (in bytes)
     */
    private int calculateHeadersSize() {
        int headersSize = 0;
        headersSize += 9 // Http proto length + 1 space (i.e.: "HTTP/1.x ")
                + String.valueOf(this.getResponseCode()).length()
                + this.getResponseMessage().length();
        headersSize += this.getResponseHeaders().length();
        return headersSize;
    }
    
}
