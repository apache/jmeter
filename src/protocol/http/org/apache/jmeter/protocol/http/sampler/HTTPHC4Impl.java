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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.SlowHC4SocketFactory;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * HTTP Sampler using Apache HttpClient 4.x.
 * 
 *                                    WARNING NOT YET COMPLETE (e.g. does not support PUT/POST yet) 
 *                                                     MAY CHANGE
 */
public class HTTPHC4Impl extends HTTPHCAbstractImpl {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 240L;

    private static final ThreadLocal<Map<HttpClientKey, HttpClient>> HTTPCLIENTS = 
        new ThreadLocal<Map<HttpClientKey, HttpClient>>(){
        @Override
        protected Map<HttpClientKey, HttpClient> initialValue() {
            return new HashMap<HttpClientKey, HttpClient>();
        }
    };

    // Scheme used for slow sockets. Cannot be set as a default, because must be set on an HttpClient instance.
    private static final Scheme SLOW_HTTP;
    private static final Scheme SLOW_HTTPS;
    
    /*
     * Create a set of default parameters from the ones initially created.
     * This allows the defaults to be overridden if necessary from the properties file.
     */
    private static final HttpParams DEFAULT_HTTP_PARAMS = new DefaultHttpClient().getParams();
    
    static {
        if (CPS_HTTP > 0) {
            log.info("Setting up HTTP SlowProtocol, cps="+CPS_HTTP);
            SLOW_HTTP = new Scheme(PROTOCOL_HTTP, DEFAULT_HTTP_PORT, new SlowHC4SocketFactory(CPS_HTTP));
        } else {
            SLOW_HTTP = null;
        }
        if (CPS_HTTPS > 0) {
            SLOW_HTTPS = new Scheme(PROTOCOL_HTTPS, DEFAULT_HTTPS_PORT, new SlowHC4SocketFactory(CPS_HTTPS));
        } else {
            SLOW_HTTPS = null;
        }
        if (localAddress != null){
            DEFAULT_HTTP_PARAMS.setParameter(ConnRoutePNames.LOCAL_ADDRESS, localAddress);
        }
        // Process Apache HttpClient parameters file
        String file=JMeterUtils.getProperty("hc.parameters.file"); // $NON-NLS-1$
        if (file != null) {
            HttpClientDefaultParameters.load(file, DEFAULT_HTTP_PARAMS);
        }
    }

    private volatile HttpUriRequest currentRequest; // Accessed from multiple threads

    protected HTTPHC4Impl(HTTPSamplerBase testElement) {
        super(testElement);
    }

    @Override
    protected HTTPSampleResult sample(URL url, String method,
            boolean areFollowingRedirect, int frameDepth) {

        // TODO cookie handling
        
        HTTPSampleResult res = new HTTPSampleResult();
        res.setMonitor(isMonitor());

        res.setSampleLabel(url.toString()); // May be replaced later
        res.setHTTPMethod(method);
        res.setURL(url);

        HttpClient httpClient = setupClient(url);
        
        HttpRequestBase httpRequest = null;
        try {
            URI uri = url.toURI();
            if (method.equals(POST)) {
                httpRequest = new HttpPost(uri);
            } else if (method.equals(PUT)) {
                httpRequest = new HttpPut(uri);
            } else if (method.equals(HEAD)) {
                httpRequest = new HttpHead(uri);
            } else if (method.equals(TRACE)) {
                httpRequest = new HttpTrace(uri);
            } else if (method.equals(OPTIONS)) {
                httpRequest = new HttpOptions(uri);
            } else if (method.equals(DELETE)) {
                httpRequest = new HttpDelete(uri);
            } else if (method.equals(GET)) {
                httpRequest = new HttpGet(uri);
            } else {
                throw new IllegalArgumentException("Unexpected method: "+method);
            }
            setupRequest(url, httpRequest, res); // can throw IOException
        } catch (Exception e) {
            res.sampleStart();
            res.sampleEnd();
            HTTPSampleResult err = errorResult(e, res);
            err.setSampleLabel("Error: " + url.toString());
            return err;
        }

        HttpContext localContext = new BasicHttpContext();

        res.sampleStart();

        final CacheManager cacheManager = getCacheManager();
        if (cacheManager != null && GET.equalsIgnoreCase(method)) {
           if (cacheManager.inCache(url)) {
               res.sampleEnd();
               res.setResponseNoContent();
               res.setSuccessful(true);
               return res;
           }
        }

        try {
            currentRequest = httpRequest;
            HttpResponse httpResponse = httpClient.execute(httpRequest, localContext); // perform the sample

            // Needs to be done after execute to pick up all the headers
            res.setRequestHeaders(getConnectionHeaders((HttpRequest) localContext.getAttribute(ExecutionContext.HTTP_REQUEST)));

            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                res.setResponseData(readResponse(res, instream, (int) entity.getContentLength()));
                Header contentType = entity.getContentType();
                if (contentType != null){
                    String ct = contentType.getValue();
                    res.setContentType(ct);
                    res.setEncodingAndType(ct);                    
                }
            }
            
            res.sampleEnd(); // Done with the sampling proper.
            currentRequest = null;

            // Now collect the results into the HTTPSampleResult:
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            res.setResponseCode(Integer.toString(statusCode));
            res.setResponseMessage(statusLine.getReasonPhrase());
            res.setSuccessful(isSuccessCode(statusCode));

            res.setResponseHeaders(getResponseHeaders(httpResponse));
            if (res.isRedirect()) {
                final Header headerLocation = httpResponse.getLastHeader(HEADER_LOCATION);
                if (headerLocation == null) { // HTTP protocol violation, but avoids NPE
                    throw new IllegalArgumentException("Missing location header");
                }
                res.setRedirectLocation(headerLocation.getValue());
            }

            // If we redirected automatically, the URL may have changed
            if (getAutoRedirects()){
                HttpUriRequest req = (HttpUriRequest) localContext.getAttribute(ExecutionContext.HTTP_REQUEST);
                HttpHost target = (HttpHost) localContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                URI redirectURI = req.getURI();
                if (redirectURI.isAbsolute()){
                    res.setURL(redirectURI.toURL());
                } else {
                    res.setURL(new URL(new URL(target.toURI()),redirectURI.toString()));
                }
            }

            // Store any cookies received in the cookie manager:
            saveConnectionCookies(httpResponse, res.getURL(), getCookieManager());

            // Save cache information
            if (cacheManager != null){
                cacheManager.saveDetails(httpResponse, res);
            }

            // Follow redirects and download page resources if appropriate:
            res = resultProcessing(areFollowingRedirect, frameDepth, res);

        } catch (IOException e) {
            res.sampleEnd();
            HTTPSampleResult err = errorResult(e, res);
            err.setSampleLabel("Error: " + url.toString());
            return err;
        } finally {
            currentRequest = null;
        }
        return res;
    }

    /**
     * Holder class for all fields that define an HttpClient instance;
     * used as the key to the ThreadLocal map of HttpClient instances.
     */
    private static final class HttpClientKey {

        private final URL url;
        private final boolean hasProxy;
        private final String proxyHost;
        private final int proxyPort;
        private final String proxyUser;
        private final String proxyPass;
        
        private final int hashCode; // Always create hash because we will always need it

        public HttpClientKey(URL url, boolean b, String proxyHost,
                int proxyPort, String proxyUser, String proxyPass) {
            this.url = url;
            this.hasProxy = b;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
            this.proxyUser = proxyUser;
            this.proxyPass = proxyPass;
            this.hashCode = getHash();
        }
        
        private int getHash() {
            int hash = 17;
            hash = hash*31 + (hasProxy ? 1 : 0);
            if (hasProxy) {
                hash = hash*31 + getHash(proxyHost);
                hash = hash*31 + proxyPort;
                hash = hash*31 + getHash(proxyUser);
                hash = hash*31 + getHash(proxyPass);
            }
            hash = hash*31 + url.toString().hashCode();
            return hash;
        }

        // Allow for null strings
        private int getHash(String s) {
            return s == null ? 0 : s.hashCode(); 
        }
        
        @Override
        public boolean equals (Object obj){
            if (this == obj) {
                return true;
            }
            if (obj instanceof HttpClientKey) {
                return false;
            }
            HttpClientKey other = (HttpClientKey) obj;
            if (this.hasProxy) { // otherwise proxy String fields may be null
                return 
                this.hasProxy == other.hasProxy &&
                this.proxyPort == other.proxyPort &&
                this.proxyHost.equals(other.proxyHost) &&
                this.proxyUser.equals(other.proxyUser) &&
                this.proxyPass.equals(other.proxyPass) &&
                this.url.toString().equals(other.url.toString());                
            }
            // No proxy, so don't check proxy fields
            return 
                this.hasProxy == other.hasProxy &&
                this.url.toString().equals(other.url.toString())
            ;
            
        }

        @Override
        public int hashCode(){
            return hashCode;
        }
    }

    private HttpClient setupClient(URL url) {

        Map<HttpClientKey, HttpClient> map = HTTPCLIENTS.get();
        
        final String host = url.getHost();
        final String proxyHost = getProxyHost();
        final int proxyPort = getProxyPortInt();

        boolean useStaticProxy = isStaticProxy(host);
        boolean useDynamicProxy = isDynamicProxy(proxyHost, proxyPort);

        // Lookup key - must agree with all the values used to create the HttpClient.
        HttpClientKey key = new HttpClientKey(url, (useStaticProxy || useDynamicProxy), 
                useDynamicProxy ? proxyHost : PROXY_HOST,
                useDynamicProxy ? proxyPort : PROXY_PORT,
                useDynamicProxy ? getProxyUser() : PROXY_USER,
                useDynamicProxy ? getProxyPass() : PROXY_PASS);
        
        HttpClient httpClient = map.get(key);

        if (httpClient == null){

            HttpParams clientParams = new DefaultedHttpParams(new BasicHttpParams(), DEFAULT_HTTP_PARAMS);
            
            httpClient = new DefaultHttpClient(clientParams);
            
            if (SLOW_HTTP != null){
                SchemeRegistry schemeRegistry = httpClient.getConnectionManager().getSchemeRegistry();
                schemeRegistry.register(SLOW_HTTP);
            }
            if (SLOW_HTTPS != null){
                SchemeRegistry schemeRegistry = httpClient.getConnectionManager().getSchemeRegistry();
                schemeRegistry.register(SLOW_HTTPS);
            }

            // Set up proxy details
            if (useDynamicProxy){
                HttpHost proxy = new HttpHost(proxyHost, proxyPort);
                clientParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                String proxyUser = getProxyUser();
                if (proxyUser.length() > 0) {
                    ((AbstractHttpClient) httpClient).getCredentialsProvider().setCredentials(
                            new AuthScope(proxyHost, proxyPort),
                            new UsernamePasswordCredentials(proxyUser, getProxyPass()));
                }
            } else if (useStaticProxy) {
                HttpHost proxy = new HttpHost(PROXY_HOST, PROXY_PORT);
                clientParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                if (PROXY_USER.length() > 0)
                    ((AbstractHttpClient) httpClient).getCredentialsProvider().setCredentials(
                            new AuthScope(PROXY_HOST, PROXY_PORT),
                            new UsernamePasswordCredentials(PROXY_USER, PROXY_PASS));
            }
            
            // TODO set up SSL manager etc.
            
            if (log.isDebugEnabled()) {
                log.debug("Created new HttpClient: @"+System.identityHashCode(httpClient));
            }

            map.put(key, httpClient); // save the agent for next time round
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Reusing the HttpClient: @"+System.identityHashCode(httpClient));
            }
        }

        // TODO - should this be done when the client is created?
        // If so, then the details need to be added as part of HttpClientKey
        setConnectionAuthorization(httpClient, url, getAuthManager());

        return httpClient;
    }

    private void setupRequest(URL url, HttpRequestBase httpRequest, HTTPSampleResult res)
        throws IOException {

    HttpParams requestParams = httpRequest.getParams();
    
    // Set up the local address if one exists
    final String ipSource = getIpSource();
    if (ipSource.length() > 0) {// Use special field ip source address (for pseudo 'ip spoofing')
        InetAddress inetAddr = InetAddress.getByName(ipSource);
        requestParams.setParameter(ConnRoutePNames.LOCAL_ADDRESS, inetAddr);
    } else if (localAddress != null){
        requestParams.setParameter(ConnRoutePNames.LOCAL_ADDRESS, localAddress);
    } else { // reset in case was set previously
        requestParams.removeParameter(ConnRoutePNames.LOCAL_ADDRESS);
    }

    int rto = getResponseTimeout();
    if (rto > 0){
        requestParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, rto);
    }

    int cto = getConnectTimeout();
    if (cto > 0){
        requestParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, cto);
    }

    requestParams.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, getAutoRedirects());
    
    // a well-behaved browser is supposed to send 'Connection: close'
    // with the last request to an HTTP server. Instead, most browsers
    // leave it to the server to close the connection after their
    // timeout period. Leave it to the JMeter user to decide.
    if (getUseKeepAlive()) {
        httpRequest.setHeader(HEADER_CONNECTION, KEEP_ALIVE);
    } else {
        httpRequest.setHeader(HEADER_CONNECTION, CONNECTION_CLOSE);
    }

    setConnectionHeaders(httpRequest, url, getHeaderManager(), getCacheManager());

    String cookies = setConnectionCookie(httpRequest, url, getCookieManager());

    if (res != null) {
        res.setCookies(cookies);
    }

}

    
    /**
     * Set any default request headers to include
     *
     * @param request the HttpRequest to be used
     */
    protected void setDefaultRequestHeaders(HttpRequest request) {
     // Method left empty here, but allows subclasses to override
    }

    /**
     * Gets the ResponseHeaders
     *
     * @param response
     *            containing the headers
     * @return string containing the headers, one per line
     */
    private String getResponseHeaders(HttpResponse response) {
        StringBuilder headerBuf = new StringBuilder();
        Header[] rh = response.getAllHeaders();
        headerBuf.append(response.getStatusLine());// header[0] is not the status line...
        headerBuf.append("\n"); // $NON-NLS-1$

        for (int i = 0; i < rh.length; i++) {
            headerBuf.append(rh[i].getName());
            headerBuf.append(": "); // $NON-NLS-1$
            headerBuf.append(rh[i].getValue());
            headerBuf.append("\n"); // $NON-NLS-1$
        }
        return headerBuf.toString();
    }

    /**
     * Extracts all the required cookies for that particular URL request and
     * sets them in the <code>HttpMethod</code> passed in.
     *
     * @param request <code>HttpRequest</code> for the request
     * @param url <code>URL</code> of the request
     * @param cookieManager the <code>CookieManager</code> containing all the cookies
     * @return a String containing the cookie details (for the response)
     * May be null
     */
    private String setConnectionCookie(HttpRequest request, URL url, CookieManager cookieManager) {
        String cookieHeader = null;
        if (cookieManager != null) {
            cookieHeader = cookieManager.getCookieHeaderForURL(url);
            if (cookieHeader != null) {
                request.setHeader(HEADER_COOKIE, cookieHeader);
            }
        }
        return cookieHeader;
    }
    
    /**
     * Extracts all the required non-cookie headers for that particular URL request and
     * sets them in the <code>HttpMethod</code> passed in
     *
     * @param request
     *            <code>HttpRequest</code> which represents the request
     * @param url
     *            <code>URL</code> of the URL request
     * @param headerManager
     *            the <code>HeaderManager</code> containing all the cookies
     *            for this <code>UrlConfig</code>
     * @param cacheManager the CacheManager (may be null)
     */
    private void setConnectionHeaders(HttpRequestBase request, URL url, HeaderManager headerManager, CacheManager cacheManager) {
        if (cacheManager != null){
            cacheManager.setHeaders(url, request);
        }
    }

    /**
     * Get all the request headers for the <code>HttpMethod</code>
     *
     * @param method
     *            <code>HttpMethod</code> which represents the request
     * @return the headers as a string
     */
    private String getConnectionHeaders(HttpRequest method) {
        // Get all the request headers
        StringBuilder hdrs = new StringBuilder(100);
        Header[] requestHeaders = method.getAllHeaders();
        for(int i = 0; i < requestHeaders.length; i++) {
            // Exclude the COOKIE header, since cookie is reported separately in the sample
            if(!HEADER_COOKIE.equalsIgnoreCase(requestHeaders[i].getName())) {
                hdrs.append(requestHeaders[i].getName());
                hdrs.append(": "); // $NON-NLS-1$
                hdrs.append(requestHeaders[i].getValue());
                hdrs.append("\n"); // $NON-NLS-1$
            }
        }

        return hdrs.toString();
    }

    private void setConnectionAuthorization(HttpClient client, URL url, AuthManager authManager) {
        CredentialsProvider credentialsProvider = 
            ((AbstractHttpClient) client).getCredentialsProvider();
        if (authManager != null) {
            Authorization auth = authManager.getAuthForURL(url);
            if (auth != null) {
                    String username = auth.getUser();
                    String realm = auth.getRealm();
                    String domain = auth.getDomain();
                    if (log.isDebugEnabled()){
                        log.debug(username + " > D="+domain+" R="+realm);
                    }
                    credentialsProvider.setCredentials(
                            new AuthScope(url.getHost(), url.getPort(), realm.length()==0 ? null : realm),
                            new NTCredentials(username, auth.getPass(), localHost, domain));
            } else {
                credentialsProvider.clear();
            }
        } else {
            credentialsProvider.clear();            
        }
    }

    private String sendPostData(){
        return null;
    }

    private String sendPutData(){
        return null;
    }

    private void saveConnectionCookies(HttpResponse method, URL u, CookieManager cookieManager) {
        if (cookieManager != null) {
            Header[] hdrs = method.getHeaders(HEADER_SET_COOKIE);
            for (Header hdr : hdrs) {
                cookieManager.addCookieFromHeader(hdr.getValue(),u);
            }
        }
    }

    @Override
    public void threadFinished() {
        log.debug("Thread Finished");
        // Does not need to be synchronised, as all access is from same thread
        Map<HttpClientKey, HttpClient> map = HTTPCLIENTS.get();
        if ( map != null ) {
            for ( HttpClient cl : map.values() ) {
                cl.getConnectionManager().shutdown();
            }
            map.clear();
        }
    }

    public boolean interrupt() {
        HttpUriRequest request = currentRequest;
        if (request != null) {
            currentRequest = null;
            try {
                request.abort();
            } catch (UnsupportedOperationException e) {
                log.warn("Could not abort pending request", e);
            }
        }
        return request != null;
    }

}
