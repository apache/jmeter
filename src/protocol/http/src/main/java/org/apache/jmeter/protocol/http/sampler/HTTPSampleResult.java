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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;

/**
 * This is a specialisation of the SampleResult class for the HTTP protocol.
 *
 */
public class HTTPSampleResult extends SampleResult {

    private static final long serialVersionUID = 241L;

    /** Set of all HTTP methods, that have no body */
    private static final Set<String> METHODS_WITHOUT_BODY = new HashSet<>(
            Arrays.asList(
                    HTTPConstants.HEAD,
                    HTTPConstants.OPTIONS,
                    HTTPConstants.TRACE));

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
     * Returns true for: 301,302,303 and 307(GET or HEAD)
     * @return true iff res is an HTTP redirect response
     */
    public boolean isRedirect() {
        /*
         * Don't redirect the following:
         * 300 = Multiple choice
         * 304 = Not Modified
         * 305 = Use Proxy
         * 306 = (Unused)
         */
        final String[] redirectCodes = { HTTPConstants.SC_MOVED_PERMANENTLY,
                HTTPConstants.SC_MOVED_TEMPORARILY,
                HTTPConstants.SC_SEE_OTHER };
        String code = getResponseCode();
        for (String redirectCode : redirectCodes) {
            if (redirectCode.equals(code)) {
                return true;
            }
        }
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
        // If the 307 status code is received in response to a request other than GET or HEAD, 
        // the user agent MUST NOT automatically redirect the request unless it can be confirmed by the user,
        // since this might change the conditions under which the request was issued.
        // See Bug 54119
        return HTTPConstants.SC_TEMPORARY_REDIRECT.equals(code) && 
                (HTTPConstants.GET.equals(getHTTPMethod()) || HTTPConstants.HEAD.equals(getHTTPMethod()));
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
            sb.append('\n');
            // Include request body if it can have one
            if (!METHODS_WITHOUT_BODY.contains(method)) {
                sb.append("\n").append(method).append(" data:\n");
                sb.append(queryString);
                sb.append('\n');
            }
            if (cookies.length()>0){
                sb.append("\nCookie Data:\n");
                sb.append(cookies);
            } else {
                sb.append("\n[no cookies]");
            }
            sb.append('\n');
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
     * @param defaultEncoding Default encoding used if there is no data encoding
     * @return the dataEncoding value as a String
     */
    @Override
    public String getDataEncodingWithDefault(String defaultEncoding) {
        String dataEncodingNoDefault = getDataEncodingNoDefault();
        if(dataEncodingNoDefault != null && dataEncodingNoDefault.length()> 0) {
            return dataEncodingNoDefault;
        }
        return defaultEncoding;
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
    public String getDataEncodingNoDefault() {
        if (super.getDataEncodingNoDefault() == null && getContentType().startsWith("text/html")){ // $NON-NLS-1$
            byte[] bytes=getResponseData();
            // get the start of the file
            String prefix = new String(bytes, 0, Math.min(bytes.length, 2000), Charset.forName(DEFAULT_HTTP_ENCODING));
            // Preserve original case
            String matchAgainst = prefix.toLowerCase(java.util.Locale.ENGLISH);
            // Extract the content-type if present
            final String metaTag = "<meta http-equiv=\"content-type\" content=\""; // $NON-NLS-1$
            int tagstart=matchAgainst.indexOf(metaTag);
            if (tagstart!=-1){
                tagstart += metaTag.length();
                int tagend = prefix.indexOf('\"', tagstart); // $NON-NLS-1$
                if (tagend!=-1){
                    final String ct = prefix.substring(tagstart,tagend);
                    setEncodingAndType(ct);// Update the dataEncoding
                }
            }
        }
        return super.getDataEncodingNoDefault();
    }

    public void setResponseNoContent(){
        setResponseCode(HTTP_NO_CONTENT_CODE);
        setResponseMessage(HTTP_NO_CONTENT_MSG);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.samplers.SampleResult#getSearchableTokens()
     */
    @Override
    public List<String> getSearchableTokens() throws Exception {
        List<String> list = new ArrayList<>(super.getSearchableTokens());
        list.add(getQueryString());
        list.add(getCookies());
        list.add(getUrlAsString());
        return list;
    }
}
