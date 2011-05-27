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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpConnection;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
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
import org.apache.jmeter.protocol.http.util.EncoderCache;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.http.util.SlowHC4SSLSocketFactory;
import org.apache.jmeter.protocol.http.util.SlowHC4SocketFactory;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * HTTP Sampler using Apache HttpClient 4.x.
 *
 */
public class HTTPHC4Impl extends HTTPHCAbstractImpl {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 241L;

    private static final String CONTEXT_METRICS = "jmeter_metrics"; // TODO hack, to be removed later

    private static final HttpResponseInterceptor METRICS_SAVER = new HttpResponseInterceptor(){
        public void process(HttpResponse response, HttpContext context)
                throws HttpException, IOException {
            HttpConnection conn = (HttpConnection) context.getAttribute(ExecutionContext.HTTP_CONNECTION);
            HttpConnectionMetrics metrics = conn.getMetrics();
            context.setAttribute(CONTEXT_METRICS, metrics);
        }
    };

    private static final ThreadLocal<Map<HttpClientKey, HttpClient>> HTTPCLIENTS = 
        new ThreadLocal<Map<HttpClientKey, HttpClient>>(){
        @Override
        protected Map<HttpClientKey, HttpClient> initialValue() {
            return new HashMap<HttpClientKey, HttpClient>();
        }
    };

    // Trust all certificates
    private static final TrustStrategy TRUSTALL = new TrustStrategy(){
        public boolean isTrusted(X509Certificate[] chain, String authType) {
            return true;
        }
    };

    // Allow all host names
    private static final AllowAllHostnameVerifier ALLOW_ALL_HOSTNAMES = new AllowAllHostnameVerifier();

    // Scheme used for slow sockets. Cannot be set as a default, because must be set on an HttpClient instance.
    private static final Scheme SLOW_HTTP;
    private static final Scheme SLOW_HTTPS;

    /*
     * Create a set of default parameters from the ones initially created.
     * This allows the defaults to be overridden if necessary from the properties file.
     */
    private static final HttpParams DEFAULT_HTTP_PARAMS;
    
    static {
        
        // TODO use new setDefaultHttpParams(HttpParams params) static method when 4.1 is available
        final DefaultHttpClient dhc = new DefaultHttpClient();
        DEFAULT_HTTP_PARAMS = dhc.getParams(); // Get the default params
        dhc.getConnectionManager().shutdown(); // Tidy up
        
        // Process Apache HttpClient parameters file
        String file=JMeterUtils.getProperty("hc.parameters.file"); // $NON-NLS-1$
        if (file != null) {
            HttpClientDefaultParameters.load(file, DEFAULT_HTTP_PARAMS);
        }

        if (CPS_HTTP > 0) {
            log.info("Setting up HTTP SlowProtocol, cps="+CPS_HTTP);
            SLOW_HTTP = new Scheme(PROTOCOL_HTTP, DEFAULT_HTTP_PORT, new SlowHC4SocketFactory(CPS_HTTP));
        } else {
            SLOW_HTTP = null;
        }
        if (CPS_HTTPS > 0) {
            log.info("Setting up HTTPS SlowProtocol, cps="+CPS_HTTPS);
            Scheme s = null;
            try {
                s = new Scheme(PROTOCOL_HTTPS, DEFAULT_HTTPS_PORT, new SlowHC4SSLSocketFactory(CPS_HTTPS));
            } catch (GeneralSecurityException e) {
                log.warn("Failed to initialise SLOW_HTTPS scheme", e);
            }
            SLOW_HTTPS = s;
        } else {
            SLOW_HTTPS = null;
        }
        if (localAddress != null){
            DEFAULT_HTTP_PARAMS.setParameter(ConnRoutePNames.LOCAL_ADDRESS, localAddress);
        }
        
    }

    private volatile HttpUriRequest currentRequest; // Accessed from multiple threads

    protected HTTPHC4Impl(HTTPSamplerBase testElement) {
        super(testElement);
    }

    @Override
    protected HTTPSampleResult sample(URL url, String method,
            boolean areFollowingRedirect, int frameDepth) {

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
            // Handle the various methods
            if (method.equals(POST)) {
                String postBody = sendPostData((HttpPost)httpRequest);
                res.setQueryString(postBody);
            } else if (method.equals(PUT)) {
                String putBody = sendPutData((HttpPut)httpRequest);
                res.setQueryString(putBody);
            }
            HttpResponse httpResponse = httpClient.execute(httpRequest, localContext); // perform the sample

            // Needs to be done after execute to pick up all the headers
            res.setRequestHeaders(getConnectionHeaders((HttpRequest) localContext.getAttribute(ExecutionContext.HTTP_REQUEST)));

            Header contentType = httpResponse.getLastHeader(HEADER_CONTENT_TYPE);
            if (contentType != null){
                String ct = contentType.getValue();
                res.setContentType(ct);
                res.setEncodingAndType(ct);                    
            }
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                res.setResponseData(readResponse(res, instream, (int) entity.getContentLength()));
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

            // record some sizes to allow HTTPSampleResult.getBytes() with different options
            HttpConnectionMetrics  metrics = (HttpConnectionMetrics) localContext.getAttribute(CONTEXT_METRICS);
            long headerBytes = 
                res.getResponseHeaders().length()   // condensed length (without \r)
              + httpResponse.getAllHeaders().length // Add \r for each header
              + 1 // Add \r for initial header
              + 2; // final \r\n before data
            long totalBytes = metrics.getReceivedBytesCount();
            res.setHeadersSize((int) headerBytes);
            res.setBodySize((int)(totalBytes - headerBytes));
            if (log.isDebugEnabled()) {
                log.debug("ResponseHeadersSize=" + res.getHeadersSize() + " Content-Length=" + res.getBodySize()
                        + " Total=" + (res.getHeadersSize() + res.getBodySize()));
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
        } catch (RuntimeException e) {
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
            ((AbstractHttpClient) httpClient).addResponseInterceptor(new ResponseContentEncoding());
            ((AbstractHttpClient) httpClient).addResponseInterceptor(METRICS_SAVER); // HACK
            
            SchemeRegistry schemeRegistry = httpClient.getConnectionManager().getSchemeRegistry();

            // Allow all hostnames and all certificates
            try {
                SSLSocketFactory socketFactory = new SSLSocketFactory(TRUSTALL, ALLOW_ALL_HOSTNAMES);
                Scheme sch = new Scheme(PROTOCOL_HTTPS, DEFAULT_HTTPS_PORT, socketFactory);
                schemeRegistry.register(sch);
            } catch (GeneralSecurityException e) {
                log.warn("Failed to register trust-all socket factory", e);
            }
            
            if (SLOW_HTTP != null){
                schemeRegistry.register(SLOW_HTTP);
            }
            if (SLOW_HTTPS != null){
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
        if (headerManager != null) {
            CollectionProperty headers = headerManager.getHeaders();
            if (headers != null) {
                PropertyIterator i = headers.iterator();
                while (i.hasNext()) {
                    org.apache.jmeter.protocol.http.control.Header header
                    = (org.apache.jmeter.protocol.http.control.Header)
                       i.next().getObjectValue();
                    String n = header.getName();
                    // Don't allow override of Content-Length
                    // TODO - what other headers are not allowed?
                    if (! HEADER_CONTENT_LENGTH.equalsIgnoreCase(n)){
                        String v = header.getValue();
                        if (HEADER_HOST.equalsIgnoreCase(n)) {
                            // TODO is it a bug that HC 4.x does not add the correct port to the generated Host header?
                            int port = url.getPort();
                            if (port != -1) {
                                if (port == url.getDefaultPort()) {
                                    port = -1; // no need to specify the port if it is the default
                                }
                            }
                            request.getParams().setParameter(ClientPNames.VIRTUAL_HOST, new HttpHost(v, port));
                        } else {
                            request.addHeader(n, v);
                        }
                    }
                }
            }
        }
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

    // Helper class so we can generate request data without dumping entire file contents
    private static class ViewableFileBody extends FileBody {
        private boolean hideFileData;
        
        public ViewableFileBody(File file, String mimeType) {
            super(file, mimeType);
            hideFileData = false;
        }

        @Override
        public void writeTo(final OutputStream out) throws IOException {
            if (hideFileData) {
                out.write("<actual file content, not shown here>".getBytes());// encoding does not really matter here
            } else {
                super.writeTo(out);
            }
        }
    }

    // TODO needs cleaning up
    private String sendPostData(HttpPost post)  throws IOException {
        // Buffer to hold the post body, except file content
        StringBuilder postedBody = new StringBuilder(1000);
        HTTPFileArg files[] = getHTTPFiles();
        // Check if we should do a multipart/form-data or an
        // application/x-www-form-urlencoded post request
        if(getUseMultipartForPost()) {
            // If a content encoding is specified, we use that as the
            // encoding of any parameter values
            String contentEncoding = getContentEncoding();
            if(contentEncoding != null && contentEncoding.length() == 0) {
                contentEncoding = null;
            }

            // Write the request to our own stream
            MultipartEntity multiPart = new MultipartEntity(
                    getDoBrowserCompatibleMultipart() ? HttpMultipartMode.BROWSER_COMPATIBLE : HttpMultipartMode.STRICT);
            // Create the parts
            // Add any parameters
            PropertyIterator args = getArguments().iterator();
            while (args.hasNext()) {
               HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
               String parameterName = arg.getName();
               if (arg.isSkippable(parameterName)){
                   continue;
               }
               FormBodyPart formPart;
               StringBody stringBody = new StringBody(arg.getValue(),
                       Charset.forName(contentEncoding == null ? "US-ASCII" : contentEncoding));
               formPart = new FormBodyPart(arg.getName(), stringBody);                   
               multiPart.addPart(formPart);
            }

            // Add any files
            // Cannot retrieve parts once added to the MultiPartEntity, so have to save them here.
            ViewableFileBody[] fileBodies = new ViewableFileBody[files.length];
            for (int i=0; i < files.length; i++) {
                HTTPFileArg file = files[i];
                fileBodies[i] = new ViewableFileBody(new File(file.getPath()), file.getMimeType());
                multiPart.addPart(file.getParamName(),fileBodies[i]);
            }

            post.setEntity(multiPart);

            if (multiPart.isRepeatable()){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                for(ViewableFileBody fileBody : fileBodies){
                    fileBody.hideFileData = true;
                }
                multiPart.writeTo(bos);
                for(ViewableFileBody fileBody : fileBodies){
                    fileBody.hideFileData = false;
                }
                bos.flush();
                // We get the posted bytes using the encoding used to create it
                postedBody.append(new String(bos.toByteArray(),
                        contentEncoding == null ? "US-ASCII" // $NON-NLS-1$ this is the default used by HttpClient
                        : contentEncoding));
                bos.close();
            } else {
                postedBody.append("<Multipart was not repeatable, cannot view what was sent>"); // $NON-NLS-1$
            }

//            // Set the content type TODO - needed?
//            String multiPartContentType = multiPart.getContentType().getValue();
//            post.setHeader(HEADER_CONTENT_TYPE, multiPartContentType);

        } else { // not multipart
            // Check if the header manager had a content type header
            // This allows the user to specify his own content-type for a POST request
            Header contentTypeHeader = post.getFirstHeader(HEADER_CONTENT_TYPE);
            boolean hasContentTypeHeader = contentTypeHeader != null && contentTypeHeader.getValue() != null && contentTypeHeader.getValue().length() > 0;
            // If there are no arguments, we can send a file as the body of the request
            // TODO: needs a multiple file upload scenerio
            if(!hasArguments() && getSendFileAsPostBody()) {
                // If getSendFileAsPostBody returned true, it's sure that file is not null
                HTTPFileArg file = files[0];
                if(!hasContentTypeHeader) {
                    // Allow the mimetype of the file to control the content type
                    if(file.getMimeType() != null && file.getMimeType().length() > 0) {
                        post.setHeader(HEADER_CONTENT_TYPE, file.getMimeType());
                    }
                    else {
                        post.setHeader(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
                    }
                }

                FileEntity fileRequestEntity = new FileEntity(new File(file.getPath()),null);
                post.setEntity(fileRequestEntity);

                // We just add placeholder text for file content
                postedBody.append("<actual file content, not shown here>");
            } else {
                // In a post request which is not multipart, we only support
                // parameters, no file upload is allowed

                // If a content encoding is specified, we set it as http parameter, so that
                // the post body will be encoded in the specified content encoding
                String contentEncoding = getContentEncoding();
                boolean haveContentEncoding = false;
                if(contentEncoding != null && contentEncoding.trim().length() > 0) {
                    post.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, contentEncoding);
                    haveContentEncoding = true;
                } else if (contentEncoding != null && contentEncoding.trim().length() == 0){
                    contentEncoding=null;
                }

                // If none of the arguments have a name specified, we
                // just send all the values as the post body
                if(getSendParameterValuesAsPostBody()) {
                    // Allow the mimetype of the file to control the content type
                    // This is not obvious in GUI if you are not uploading any files,
                    // but just sending the content of nameless parameters
                    // TODO: needs a multiple file upload scenerio
                    if(!hasContentTypeHeader) {
                        HTTPFileArg file = files.length > 0? files[0] : null;
                        if(file != null && file.getMimeType() != null && file.getMimeType().length() > 0) {
                            post.setHeader(HEADER_CONTENT_TYPE, file.getMimeType());
                        }
                        else {
                             // TODO - is this the correct default?
                            post.setHeader(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
                        }
                    }

                    // Just append all the parameter values, and use that as the post body
                    StringBuilder postBody = new StringBuilder();
                    PropertyIterator args = getArguments().iterator();
                    while (args.hasNext()) {
                        HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
                        String value;
                        if (haveContentEncoding){
                            value = arg.getEncodedValue(contentEncoding);
                        } else {
                            value = arg.getEncodedValue();
                        }
                        postBody.append(value);
                    }
                    StringEntity requestEntity = new StringEntity(postBody.toString(), post.getFirstHeader(HEADER_CONTENT_TYPE).getValue(), contentEncoding);
                    post.setEntity(requestEntity);
                    postedBody.append(postBody.toString()); // TODO OK?
                } else {
                    // It is a normal post request, with parameter names and values

                    // Set the content type
                    if(!hasContentTypeHeader) {
                        post.setHeader(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
                    }
                    // Add the parameters
                    PropertyIterator args = getArguments().iterator();
                    List <NameValuePair> nvps = new ArrayList <NameValuePair>();
                    String urlContentEncoding = contentEncoding;
                    if(urlContentEncoding == null || urlContentEncoding.length() == 0) {
                        // Use the default encoding for urls
                        urlContentEncoding = EncoderCache.URL_ARGUMENT_ENCODING;
                    }
                    while (args.hasNext()) {
                        HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
                        // The HTTPClient always urlencodes both name and value,
                        // so if the argument is already encoded, we have to decode
                        // it before adding it to the post request
                        String parameterName = arg.getName();
                        if (arg.isSkippable(parameterName)){
                            continue;
                        }
                        String parameterValue = arg.getValue();
                        if(!arg.isAlwaysEncoded()) {
                            // The value is already encoded by the user
                            // Must decode the value now, so that when the
                            // httpclient encodes it, we end up with the same value
                            // as the user had entered.
                            parameterName = URLDecoder.decode(parameterName, urlContentEncoding);
                            parameterValue = URLDecoder.decode(parameterValue, urlContentEncoding);
                        }
                        // Add the parameter, httpclient will urlencode it
                        nvps.add(new BasicNameValuePair(parameterName, parameterValue));
                    }
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvps, urlContentEncoding);
                    post.setEntity(entity);
                    if (entity.isRepeatable()){
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        post.getEntity().writeTo(bos);
                        bos.flush();
                        // We get the posted bytes using the encoding used to create it
                        if (contentEncoding != null) {
                            postedBody.append(new String(bos.toByteArray(), contentEncoding));
                        } else {
                            postedBody.append(new String(bos.toByteArray()));
                        }
                        bos.close();
                    }  else {
                        postedBody.append("<RequestEntity was not repeatable, cannot view what was sent>");
                    }
                }

//                // If the request entity is repeatable, we can send it first to
//                // our own stream, so we can return it
//                if(post.getEntity().isRepeatable()) {
//                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                    post.getEntity().writeTo(bos);
//                    bos.flush();
//                    // We get the posted bytes using the encoding used to create it
//                    if (contentEncoding != null) {
//                        postedBody.append(new String(bos.toByteArray(), contentEncoding));
//                    } else {
//                        postedBody.append(new String(bos.toByteArray()));
//                    }
//                    bos.close();
//                }
//                else {
//                    postedBody.append("<RequestEntity was not repeatable, cannot view what was sent>");
//                }
            }
        }
        return postedBody.toString();
    }

    // TODO - implementation not fully tested
    private String sendPutData(HttpPut put) throws IOException {
        // Buffer to hold the put body, except file content
        StringBuilder putBody = new StringBuilder(1000);
        boolean hasPutBody = false;

        // Check if the header manager had a content type header
        // This allows the user to specify his own content-type
        Header contentTypeHeader = put.getFirstHeader(HEADER_CONTENT_TYPE);
        boolean hasContentTypeHeader = contentTypeHeader != null && contentTypeHeader.getValue() != null && contentTypeHeader.getValue().length() > 0;

        // Check for local contentEncoding override
        final String contentEncoding = getContentEncoding();
        boolean haveContentEncoding = (contentEncoding != null && contentEncoding.trim().length() > 0);
        
        HttpParams putParams = put.getParams();
        HTTPFileArg files[] = getHTTPFiles();

        // If there are no arguments, we can send a file as the body of the request

        if(!hasArguments() && getSendFileAsPostBody()) {
            hasPutBody = true;

            // If getSendFileAsPostBody returned true, it's sure that file is not null
            FileEntity fileRequestEntity = new FileEntity(new File(files[0].getPath()),null);
            put.setEntity(fileRequestEntity);

            // We just add placeholder text for file content
            putBody.append("<actual file content, not shown here>");
        }
        // If none of the arguments have a name specified, we
        // just send all the values as the put body
        else if(getSendParameterValuesAsPostBody()) {
            hasPutBody = true;

            // If a content encoding is specified, we set it as http parameter, so that
            // the post body will be encoded in the specified content encoding
            if(haveContentEncoding) {
                putParams.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET,contentEncoding);
            }

            // Just append all the parameter values, and use that as the post body
            StringBuilder putBodyContent = new StringBuilder();
            PropertyIterator args = getArguments().iterator();
            while (args.hasNext()) {
                HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
                String value = null;
                if (haveContentEncoding){
                    value = arg.getEncodedValue(contentEncoding);
                } else {
                    value = arg.getEncodedValue();
                }
                putBodyContent.append(value);
            }
            String contentTypeValue = null;
            if(hasContentTypeHeader) {
                contentTypeValue = put.getFirstHeader(HEADER_CONTENT_TYPE).getValue();
            }
            StringEntity requestEntity = new StringEntity(putBodyContent.toString(), contentTypeValue, 
                    (String) putParams.getParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET));
            put.setEntity(requestEntity);
        }
        // Check if we have any content to send for body
        if(hasPutBody) {
            // If the request entity is repeatable, we can send it first to
            // our own stream, so we can return it
            if(put.getEntity().isRepeatable()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                put.getEntity().writeTo(bos);
                bos.flush();
                // We get the posted bytes using the charset that was used to create them
                putBody.append(new String(bos.toByteArray(),
                        (String) putParams.getParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET)));
                bos.close();
            }
            else {
                putBody.append("<RequestEntity was not repeatable, cannot view what was sent>");
            }
            if(!hasContentTypeHeader) {
                // Allow the mimetype of the file to control the content type
                // This is not obvious in GUI if you are not uploading any files,
                // but just sending the content of nameless parameters
                // TODO: needs a multiple file upload scenerio
                HTTPFileArg file = files.length > 0? files[0] : null;
                if(file != null && file.getMimeType() != null && file.getMimeType().length() > 0) {
                    put.setHeader(HEADER_CONTENT_TYPE, file.getMimeType());
                }
            }
            return putBody.toString();
        }
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
