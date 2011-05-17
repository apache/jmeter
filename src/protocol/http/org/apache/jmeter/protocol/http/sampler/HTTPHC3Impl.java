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
 */
package org.apache.jmeter.protocol.http.sampler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.ProtocolException;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.TraceMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartBase;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.EncoderCache;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.http.util.LoopbackHttpClientSocketFactory;
import org.apache.jmeter.protocol.http.util.SlowHttpClientSocketFactory;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * HTTP sampler using Apache (Jakarta) Commons HttpClient 3.1.
 */
public class HTTPHC3Impl extends HTTPHCAbstractImpl {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 241L;

    private static final String HTTP_AUTHENTICATION_PREEMPTIVE = "http.authentication.preemptive"; // $NON-NLS-1$

    private static final boolean canSetPreEmptive; // OK to set pre-emptive auth?

    private static final ThreadLocal<Map<HostConfiguration, HttpClient>> httpClients = 
        new ThreadLocal<Map<HostConfiguration, HttpClient>>(){
        @Override
        protected Map<HostConfiguration, HttpClient> initialValue() {
            return new HashMap<HostConfiguration, HttpClient>();
        }
    };

    // Needs to be accessible by HTTPSampler2
    volatile HttpClient savedClient;

    static {
        if (CPS_HTTP > 0) {
            log.info("Setting up HTTP SlowProtocol, cps="+CPS_HTTP);
            Protocol.registerProtocol(PROTOCOL_HTTP,
                    new Protocol(PROTOCOL_HTTP,new SlowHttpClientSocketFactory(CPS_HTTP),DEFAULT_HTTP_PORT));
        }

        // Now done in JsseSSLManager (which needs to register the protocol)
//        cps =
//            JMeterUtils.getPropDefault("httpclient.socket.https.cps", 0); // $NON-NLS-1$
//
//        if (cps > 0) {
//            log.info("Setting up HTTPS SlowProtocol, cps="+cps);
//            Protocol.registerProtocol(PROTOCOL_HTTPS,
//                    new Protocol(PROTOCOL_HTTPS,new SlowHttpClientSocketFactory(cps),DEFAULT_HTTPS_PORT));
//        }

        // Set default parameters as needed
        HttpParams params = DefaultHttpParams.getDefaultParams();

        // Process Commons HttpClient parameters file
        String file=JMeterUtils.getProperty("httpclient.parameters.file"); // $NON-NLS-1$
        if (file != null) {
            HttpClientDefaultParameters.load(file, params);
        }

        // If the pre-emptive parameter is undefined, then we can set it as needed
        // otherwise we should do what the user requested.
        canSetPreEmptive =  params.getParameter(HTTP_AUTHENTICATION_PREEMPTIVE) == null;

        // Handle old-style JMeter properties
        try {
            params.setParameter(HttpMethodParams.PROTOCOL_VERSION, HttpVersion.parse("HTTP/"+HTTP_VERSION));
        } catch (ProtocolException e) {
            log.warn("Problem setting protocol version "+e.getLocalizedMessage());
        }

        if (SO_TIMEOUT >= 0){
            params.setIntParameter(HttpMethodParams.SO_TIMEOUT, SO_TIMEOUT);
        }

        // This must be done last, as must not be overridden
        params.setParameter(HttpMethodParams.COOKIE_POLICY,CookiePolicy.IGNORE_COOKIES);
        // We do our own cookie handling

        if (USE_LOOPBACK){
            LoopbackHttpClientSocketFactory.setup();
        }
    }

    protected HTTPHC3Impl(HTTPSamplerBase base) {
        super(base);
    }


    /**
     * Samples the URL passed in and stores the result in
     * <code>HTTPSampleResult</code>, following redirects and downloading
     * page resources as appropriate.
     * <p>
     * When getting a redirect target, redirects are not followed and resources
     * are not downloaded. The caller will take care of this.
     *
     * @param url
     *            URL to sample
     * @param method
     *            HTTP method: GET, POST,...
     * @param areFollowingRedirect
     *            whether we're getting a redirect target
     * @param frameDepth
     *            Depth of this target in the frame structure. Used only to
     *            prevent infinite recursion.
     * @return results of the sampling
     */
    @Override
    protected HTTPSampleResult sample(URL url, String method, boolean areFollowingRedirect, int frameDepth) {

        String urlStr = url.toString();

        log.debug("Start : sample " + urlStr);
        log.debug("method " + method);

        HttpMethodBase httpMethod = null;

        HTTPSampleResult res = new HTTPSampleResult();
        res.setMonitor(isMonitor());

        res.setSampleLabel(urlStr); // May be replaced later
        res.setHTTPMethod(method);
        res.setURL(url);

        res.sampleStart(); // Count the retries as well in the time
        try {
            // May generate IllegalArgumentException
            if (method.equals(POST)) {
                httpMethod = new PostMethod(urlStr);
            } else if (method.equals(PUT)){
                httpMethod = new PutMethod(urlStr);
            } else if (method.equals(HEAD)){
                httpMethod = new HeadMethod(urlStr);
            } else if (method.equals(TRACE)){
                httpMethod = new TraceMethod(urlStr);
            } else if (method.equals(OPTIONS)){
                httpMethod = new OptionsMethod(urlStr);
            } else if (method.equals(DELETE)){
                httpMethod = new DeleteMethod(urlStr);
            } else if (method.equals(GET)){
                httpMethod = new GetMethod(urlStr);
            } else {
                throw new IllegalArgumentException("Unexpected method: "+method);
            }

            final CacheManager cacheManager = getCacheManager();
            if (cacheManager != null && GET.equalsIgnoreCase(method)) {
               if (cacheManager.inCache(url)) {
                   res.sampleEnd();
                   res.setResponseNoContent();
                   res.setSuccessful(true);
                   return res;
               }
            }

            // Set any default request headers
            setDefaultRequestHeaders(httpMethod);

            // Setup connection
            HttpClient client = setupConnection(url, httpMethod, res);
            savedClient = client;

            // Handle the various methods
            if (method.equals(POST)) {
                String postBody = sendPostData((PostMethod)httpMethod);
                res.setQueryString(postBody);
            } else if (method.equals(PUT)) {
                String putBody = sendPutData((PutMethod)httpMethod);
                res.setQueryString(putBody);
            }

            int statusCode = client.executeMethod(httpMethod);

            // Needs to be done after execute to pick up all the headers
            res.setRequestHeaders(getConnectionHeaders(httpMethod));

            // Request sent. Now get the response:
            InputStream instream = httpMethod.getResponseBodyAsStream();

            if (instream != null) {// will be null for HEAD
                instream = new CountingInputStream(instream);
                try {
                    Header responseHeader = httpMethod.getResponseHeader(HEADER_CONTENT_ENCODING);
                    if (responseHeader!= null && ENCODING_GZIP.equals(responseHeader.getValue())) {
                        InputStream tmpInput = new GZIPInputStream(instream); // tmp inputstream needs to have a good counting
                        res.setResponseData(readResponse(res, tmpInput, (int) httpMethod.getResponseContentLength()));                        
                    } else {
                        res.setResponseData(readResponse(res, instream, (int) httpMethod.getResponseContentLength()));
                    }
                } finally {
                    JOrphanUtils.closeQuietly(instream);
                }
            }

            res.sampleEnd();
            // Done with the sampling proper.

            // Now collect the results into the HTTPSampleResult:

            res.setSampleLabel(httpMethod.getURI().toString());
            // Pick up Actual path (after redirects)

            res.setResponseCode(Integer.toString(statusCode));
            res.setSuccessful(isSuccessCode(statusCode));

            res.setResponseMessage(httpMethod.getStatusText());

            String ct = null;
            Header h = httpMethod.getResponseHeader(HEADER_CONTENT_TYPE);
            if (h != null)// Can be missing, e.g. on redirect
            {
                ct = h.getValue();
                res.setContentType(ct);// e.g. text/html; charset=ISO-8859-1
                res.setEncodingAndType(ct);
            }

            res.setResponseHeaders(getResponseHeaders(httpMethod));
            if (res.isRedirect()) {
                final Header headerLocation = httpMethod.getResponseHeader(HEADER_LOCATION);
                if (headerLocation == null) { // HTTP protocol violation, but avoids NPE
                    throw new IllegalArgumentException("Missing location header");
                }
                res.setRedirectLocation(headerLocation.getValue());
            }

            // record some sizes to allow HTTPSampleResult.getBytes() with different options
            if (instream != null) {
                res.setBodySize(((CountingInputStream) instream).getCount());
            }
            res.setHeadersSize(calculateHeadersSize(httpMethod));
            if (log.isDebugEnabled()) {
                log.debug("Response headersSize=" + res.getHeadersSize() + " bodySize=" + res.getBodySize()
                        + " Total=" + (res.getHeadersSize() + res.getBodySize()));
            }
            
            // If we redirected automatically, the URL may have changed
            if (getAutoRedirects()){
                res.setURL(new URL(httpMethod.getURI().toString()));
            }

            // Store any cookies received in the cookie manager:
            saveConnectionCookies(httpMethod, res.getURL(), getCookieManager());

            // Save cache information
            if (cacheManager != null){
                cacheManager.saveDetails(httpMethod, res);
            }

            // Follow redirects and download page resources if appropriate:
            res = resultProcessing(areFollowingRedirect, frameDepth, res);

            log.debug("End : sample");
            return res;
        } catch (IllegalArgumentException e)// e.g. some kinds of invalid URL
        {
            res.sampleEnd();
            HTTPSampleResult err = errorResult(e, res);
            err.setSampleLabel("Error: " + url.toString());
            return err;
        } catch (IOException e) {
            res.sampleEnd();
            HTTPSampleResult err = errorResult(e, res);
            err.setSampleLabel("Error: " + url.toString());
            return err;
        } finally {
            savedClient = null;
            if (httpMethod != null) {
                httpMethod.releaseConnection();
            }
        }
    }
    
    /**
     * Calculate response headers size
     * 
     * @return the size response headers (in bytes)
     */
    private static int calculateHeadersSize(HttpMethodBase httpMethod) {
        int headerSize = httpMethod.getStatusLine().toString().length()+2; // add a \r\n
        Header[] rh = httpMethod.getResponseHeaders();
        for (int i = 0; i < rh.length; i++) {
            headerSize += (rh[i]).toString().length(); // already include the \r\n
        }
        headerSize += 2; // last \r\n before response data
        return headerSize;
    }

    /**
     * Returns an <code>HttpConnection</code> fully ready to attempt
     * connection. This means it sets the request method (GET or POST), headers,
     * cookies, and authorization for the URL request.
     * <p>
     * The request infos are saved into the sample result if one is provided.
     *
     * @param u
     *            <code>URL</code> of the URL request
     * @param httpMethod
     *            GET/PUT/HEAD etc
     * @param res
     *            sample result to save request infos to
     * @return <code>HttpConnection</code> ready for .connect
     * @exception IOException
     *                if an I/O Exception occurs
     */
    protected HttpClient setupConnection(URL u, HttpMethodBase httpMethod, HTTPSampleResult res) throws IOException {

        String urlStr = u.toString();

        org.apache.commons.httpclient.URI uri = new org.apache.commons.httpclient.URI(urlStr,false);

        String schema = uri.getScheme();
        if ((schema == null) || (schema.length()==0)) {
            schema = PROTOCOL_HTTP;
        }

        if (PROTOCOL_HTTPS.equalsIgnoreCase(schema)){
            SSLManager.getInstance(); // ensure the manager is initialised
            // we don't currently need to do anything further, as this sets the default https protocol
        }

        Protocol protocol = Protocol.getProtocol(schema);

        String host = uri.getHost();
        int port = uri.getPort();

        /*
         *  We use the HostConfiguration as the key to retrieve the HttpClient,
         *  so need to ensure that any items used in its equals/hashcode methods are
         *  not changed after use, i.e.:
         *  host, port, protocol, localAddress, proxy
         *
        */
        HostConfiguration hc = new HostConfiguration();
        hc.setHost(host, port, protocol); // All needed to ensure re-usablility

        // Set up the local address if one exists
        if (localAddress != null){
            hc.setLocalAddress(localAddress);
        } else {
            final String ipSource = getIpSource();
            if (ipSource.length() > 0) {// Use special field ip source address (for pseudo 'ip spoofing')
                InetAddress inetAddr = InetAddress.getByName(ipSource);
                hc.setLocalAddress(inetAddr);
            }
        }

        final String proxyHost = getProxyHost();
        final int proxyPort = getProxyPortInt();

        boolean useStaticProxy = isStaticProxy(host);
        boolean useDynamicProxy = isDynamicProxy(proxyHost, proxyPort);

        if (useDynamicProxy){
            hc.setProxy(proxyHost, proxyPort);
            useStaticProxy = false; // Dynamic proxy overrules static proxy
        } else if (useStaticProxy) {
            if (log.isDebugEnabled()){
                log.debug("Setting proxy: "+PROXY_HOST+":"+PROXY_PORT);
            }
            hc.setProxy(PROXY_HOST, PROXY_PORT);
        }

        Map<HostConfiguration, HttpClient> map = httpClients.get();
        // N.B. HostConfiguration.equals() includes proxy settings in the compare.
        HttpClient httpClient = map.get(hc);

        if ( httpClient == null )
        {
            httpClient = new HttpClient(new SimpleHttpConnectionManager());
            if (log.isDebugEnabled()) {
                log.debug("Created new HttpClient: @"+System.identityHashCode(httpClient));
            }
            httpClient.setHostConfiguration(hc);
            map.put(hc, httpClient);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Reusing the HttpClient: @"+System.identityHashCode(httpClient));
            }
        }

        // Set up any required Proxy credentials
        if (useDynamicProxy){
            String user = getProxyUser();
            if (user.length() > 0){
                httpClient.getState().setProxyCredentials(
                        new AuthScope(proxyHost,proxyPort,null,AuthScope.ANY_SCHEME),
                        new NTCredentials(user,getProxyPass(),localHost,PROXY_DOMAIN)
                    );
            } else {
                httpClient.getState().clearProxyCredentials();
            }
        } else {
            if (useStaticProxy) {
                if (PROXY_USER.length() > 0){
                    httpClient.getState().setProxyCredentials(
                        new AuthScope(PROXY_HOST,PROXY_PORT,null,AuthScope.ANY_SCHEME),
                        new NTCredentials(PROXY_USER,PROXY_PASS,localHost,PROXY_DOMAIN)
                    );
                }
            } else {
                httpClient.getState().clearProxyCredentials();
            }
        }

        int rto = getResponseTimeout();
        if (rto > 0){
            httpMethod.getParams().setSoTimeout(rto);
        }

        int cto = getConnectTimeout();
        if (cto > 0){
            httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(cto);
        }


        // Allow HttpClient to handle the redirects:
        httpMethod.setFollowRedirects(getAutoRedirects());

        // a well-behaved browser is supposed to send 'Connection: close'
        // with the last request to an HTTP server. Instead, most browsers
        // leave it to the server to close the connection after their
        // timeout period. Leave it to the JMeter user to decide.
        if (getUseKeepAlive()) {
            httpMethod.setRequestHeader(HEADER_CONNECTION, KEEP_ALIVE);
        } else {
            httpMethod.setRequestHeader(HEADER_CONNECTION, CONNECTION_CLOSE);
        }

        setConnectionHeaders(httpMethod, u, getHeaderManager(), getCacheManager());
        String cookies = setConnectionCookie(httpMethod, u, getCookieManager());

        setConnectionAuthorization(httpClient, u, getAuthManager());

        if (res != null) {
            res.setCookies(cookies);
        }

        return httpClient;
    }

    /**
     * Set any default request headers to include
     *
     * @param httpMethod the HttpMethod used for the request
     */
    protected void setDefaultRequestHeaders(HttpMethod httpMethod) {
        // Method left empty here, but allows subclasses to override
    }

    /**
     * Gets the ResponseHeaders
     *
     * @param method the method used to perform the request
     * @return string containing the headers, one per line
     */
    protected String getResponseHeaders(HttpMethod method) {
        StringBuilder headerBuf = new StringBuilder();
        org.apache.commons.httpclient.Header rh[] = method.getResponseHeaders();
        headerBuf.append(method.getStatusLine());// header[0] is not the status line...
        headerBuf.append("\n"); // $NON-NLS-1$

        for (int i = 0; i < rh.length; i++) {
            String key = rh[i].getName();
            headerBuf.append(key);
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
     * @param method <code>HttpMethod</code> for the request
     * @param u <code>URL</code> of the request
     * @param cookieManager the <code>CookieManager</code> containing all the cookies
     * @return a String containing the cookie details (for the response)
     * May be null
     */
    private String setConnectionCookie(HttpMethod method, URL u, CookieManager cookieManager) {
        String cookieHeader = null;
        if (cookieManager != null) {
            cookieHeader = cookieManager.getCookieHeaderForURL(u);
            if (cookieHeader != null) {
                method.setRequestHeader(HEADER_COOKIE, cookieHeader);
            }
        }
        return cookieHeader;
    }

    /**
     * Extracts all the required non-cookie headers for that particular URL request and
     * sets them in the <code>HttpMethod</code> passed in
     *
     * @param method
     *            <code>HttpMethod</code> which represents the request
     * @param u
     *            <code>URL</code> of the URL request
     * @param headerManager
     *            the <code>HeaderManager</code> containing all the cookies
     *            for this <code>UrlConfig</code>
     * @param cacheManager the CacheManager (may be null)
     */
    private void setConnectionHeaders(HttpMethod method, URL u, HeaderManager headerManager, CacheManager cacheManager) {
        // Set all the headers from the HeaderManager
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
                    // This helps with SoapSampler hack too
                    // TODO - what other headers are not allowed?
                    if (! HEADER_CONTENT_LENGTH.equalsIgnoreCase(n)){
                        String v = header.getValue();
                        if (HEADER_HOST.equalsIgnoreCase(n)) {
                            method.getParams().setVirtualHost(v);
                        } else {
                            method.addRequestHeader(n, v);
                        }
                    }
                }
            }
        }
        if (cacheManager != null){
            cacheManager.setHeaders(u, method);
        }
    }

    /**
     * Get all the request headers for the <code>HttpMethod</code>
     *
     * @param method
     *            <code>HttpMethod</code> which represents the request
     * @return the headers as a string
     */
    protected String getConnectionHeaders(HttpMethod method) {
        // Get all the request headers
        StringBuilder hdrs = new StringBuilder(100);
        Header[] requestHeaders = method.getRequestHeaders();
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


    /**
     * Extracts all the required authorization for that particular URL request
     * and sets it in the <code>HttpMethod</code> passed in.
     *
     * @param client the HttpClient object
     *
     * @param u
     *            <code>URL</code> of the URL request
     * @param authManager
     *            the <code>AuthManager</code> containing all the authorisations for
     *            this <code>UrlConfig</code>
     */
    private void setConnectionAuthorization(HttpClient client, URL u, AuthManager authManager) {
        HttpState state = client.getState();
        if (authManager != null) {
            HttpClientParams params = client.getParams();
            Authorization auth = authManager.getAuthForURL(u);
            if (auth != null) {
                    String username = auth.getUser();
                    String realm = auth.getRealm();
                    String domain = auth.getDomain();
                    if (log.isDebugEnabled()){
                        log.debug(username + " >  D="+ username + " D="+domain+" R="+realm);
                    }
                    state.setCredentials(
                            new AuthScope(u.getHost(),u.getPort(),
                                    realm.length()==0 ? null : realm //"" is not the same as no realm
                                    ,AuthScope.ANY_SCHEME),
                            // NT Includes other types of Credentials
                            new NTCredentials(
                                    username,
                                    auth.getPass(),
                                    localHost,
                                    domain
                            ));
                    // We have credentials - should we set pre-emptive authentication?
                    if (canSetPreEmptive){
                        log.debug("Setting Pre-emptive authentication");
                        params.setAuthenticationPreemptive(true);
                    }
            } else {
                state.clearCredentials();
                if (canSetPreEmptive){
                    params.setAuthenticationPreemptive(false);
                }
            }
        } else {
            state.clearCredentials();
        }
    }


    /*
     * Send POST data from <code>Entry</code> to the open connection.
     *
     * @param connection
     *            <code>URLConnection</code> where POST data should be sent
     * @return a String show what was posted. Will not contain actual file upload content
     * @exception IOException
     *                if an I/O exception occurs
     */
    private String sendPostData(PostMethod post) throws IOException {
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

            final boolean browserCompatible = getDoBrowserCompatibleMultipart();
            // We don't know how many entries will be skipped
            ArrayList<PartBase> partlist = new ArrayList<PartBase>();
            // Create the parts
            // Add any parameters
            PropertyIterator args = getArguments().iterator();
            while (args.hasNext()) {
                HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
                String parameterName = arg.getName();
                if (arg.isSkippable(parameterName)){
                    continue;
                }
                StringPart part = new StringPart(arg.getName(), arg.getValue(), contentEncoding);
                if (browserCompatible) {
                    part.setTransferEncoding(null);
                    part.setContentType(null);
                }
                partlist.add(part);
            }

            // Add any files
            for (int i=0; i < files.length; i++) {
                HTTPFileArg file = files[i];
                File inputFile = new File(file.getPath());
                // We do not know the char set of the file to be uploaded, so we set it to null
                ViewableFilePart filePart = new ViewableFilePart(file.getParamName(), inputFile, file.getMimeType(), null);
                filePart.setCharSet(null); // We do not know what the char set of the file is
                partlist.add(filePart);
            }

            // Set the multipart for the post
            int partNo = partlist.size();
            Part[] parts = partlist.toArray(new Part[partNo]);
            MultipartRequestEntity multiPart = new MultipartRequestEntity(parts, post.getParams());
            post.setRequestEntity(multiPart);

            // Set the content type
            String multiPartContentType = multiPart.getContentType();
            post.setRequestHeader(HEADER_CONTENT_TYPE, multiPartContentType);

            // If the Multipart is repeatable, we can send it first to
            // our own stream, without the actual file content, so we can return it
            if(multiPart.isRepeatable()) {
                // For all the file multiparts, we must tell it to not include
                // the actual file content
                for(int i = 0; i < partNo; i++) {
                    if(parts[i] instanceof ViewableFilePart) {
                        ((ViewableFilePart) parts[i]).setHideFileData(true); // .sendMultipartWithoutFileContent(bos);
                    }
                }
                // Write the request to our own stream
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                multiPart.writeRequest(bos);
                bos.flush();
                // We get the posted bytes using the encoding used to create it
                postedBody.append(new String(bos.toByteArray(),
                        contentEncoding == null ? "US-ASCII" // $NON-NLS-1$ this is the default used by HttpClient
                        : contentEncoding));
                bos.close();

                // For all the file multiparts, we must revert the hiding of
                // the actual file content
                for(int i = 0; i < partNo; i++) {
                    if(parts[i] instanceof ViewableFilePart) {
                        ((ViewableFilePart) parts[i]).setHideFileData(false);
                    }
                }
            }
            else {
                postedBody.append("<Multipart was not repeatable, cannot view what was sent>"); // $NON-NLS-1$
            }
        }
        else {
            // Check if the header manager had a content type header
            // This allows the user to specify his own content-type for a POST request
            Header contentTypeHeader = post.getRequestHeader(HEADER_CONTENT_TYPE);
            boolean hasContentTypeHeader = contentTypeHeader != null && contentTypeHeader.getValue() != null && contentTypeHeader.getValue().length() > 0;
            // If there are no arguments, we can send a file as the body of the request
            // TODO: needs a multiple file upload scenerio
            if(!hasArguments() && getSendFileAsPostBody()) {
                // If getSendFileAsPostBody returned true, it's sure that file is not null
                HTTPFileArg file = files[0];
                if(!hasContentTypeHeader) {
                    // Allow the mimetype of the file to control the content type
                    if(file.getMimeType() != null && file.getMimeType().length() > 0) {
                        post.setRequestHeader(HEADER_CONTENT_TYPE, file.getMimeType());
                    }
                    else {
                        post.setRequestHeader(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
                    }
                }

                FileRequestEntity fileRequestEntity = new FileRequestEntity(new File(file.getPath()),null);
                post.setRequestEntity(fileRequestEntity);

                // We just add placeholder text for file content
                postedBody.append("<actual file content, not shown here>");
            }
            else {
                // In a post request which is not multipart, we only support
                // parameters, no file upload is allowed

                // If a content encoding is specified, we set it as http parameter, so that
                // the post body will be encoded in the specified content encoding
                String contentEncoding = getContentEncoding();
                boolean haveContentEncoding = false;
                if(contentEncoding != null && contentEncoding.trim().length() > 0) {
                    post.getParams().setContentCharset(contentEncoding);
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
                            post.setRequestHeader(HEADER_CONTENT_TYPE, file.getMimeType());
                        }
                        else {
                             // TODO - is this the correct default?
                            post.setRequestHeader(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
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
                    StringRequestEntity requestEntity = new StringRequestEntity(postBody.toString(), post.getRequestHeader(HEADER_CONTENT_TYPE).getValue(), contentEncoding);
                    post.setRequestEntity(requestEntity);
                }
                else {
                    // It is a normal post request, with parameter names and values

                    // Set the content type
                    if(!hasContentTypeHeader) {
                        post.setRequestHeader(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
                    }
                    // Add the parameters
                    PropertyIterator args = getArguments().iterator();
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
                            String urlContentEncoding = contentEncoding;
                            if(urlContentEncoding == null || urlContentEncoding.length() == 0) {
                                // Use the default encoding for urls
                                urlContentEncoding = EncoderCache.URL_ARGUMENT_ENCODING;
                            }
                            parameterName = URLDecoder.decode(parameterName, urlContentEncoding);
                            parameterValue = URLDecoder.decode(parameterValue, urlContentEncoding);
                        }
                        // Add the parameter, httpclient will urlencode it
                        post.addParameter(parameterName, parameterValue);
                    }

/*
//                    // Alternative implementation, to make sure that HTTPSampler and HTTPSampler2
//                    // sends the same post body.
//
//                    // Only include the content char set in the content-type header if it is not
//                    // an APPLICATION_X_WWW_FORM_URLENCODED content type
//                    String contentCharSet = null;
//                    if(!post.getRequestHeader(HEADER_CONTENT_TYPE).getValue().equals(APPLICATION_X_WWW_FORM_URLENCODED)) {
//                        contentCharSet = post.getRequestCharSet();
//                    }
//                    StringRequestEntity requestEntity = new StringRequestEntity(getQueryString(contentEncoding), post.getRequestHeader(HEADER_CONTENT_TYPE).getValue(), contentCharSet);
//                    post.setRequestEntity(requestEntity);
*/
                }

                // If the request entity is repeatable, we can send it first to
                // our own stream, so we can return it
                if(post.getRequestEntity().isRepeatable()) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    post.getRequestEntity().writeRequest(bos);
                    bos.flush();
                    // We get the posted bytes using the encoding used to create it
                    postedBody.append(new String(bos.toByteArray(),post.getRequestCharSet()));
                    bos.close();
                }
                else {
                    postedBody.append("<RequestEntity was not repeatable, cannot view what was sent>");
                }
            }
        }
        // Set the content length
        post.setRequestHeader(HEADER_CONTENT_LENGTH, Long.toString(post.getRequestEntity().getContentLength()));

        return postedBody.toString();
    }

    /**
     * Set up the PUT data
     */
    private String sendPutData(PutMethod put) throws IOException {
        // Buffer to hold the put body, except file content
        StringBuilder putBody = new StringBuilder(1000);
        boolean hasPutBody = false;

        // Check if the header manager had a content type header
        // This allows the user to specify his own content-type for a POST request
        Header contentTypeHeader = put.getRequestHeader(HEADER_CONTENT_TYPE);
        boolean hasContentTypeHeader = contentTypeHeader != null && contentTypeHeader.getValue() != null && contentTypeHeader.getValue().length() > 0;
        HTTPFileArg files[] = getHTTPFiles();

        // If there are no arguments, we can send a file as the body of the request

        if(!hasArguments() && getSendFileAsPostBody()) {
            hasPutBody = true;

            // If getSendFileAsPostBody returned true, it's sure that file is not null
            FileRequestEntity fileRequestEntity = new FileRequestEntity(new File(files[0].getPath()),null);
            put.setRequestEntity(fileRequestEntity);

            // We just add placeholder text for file content
            putBody.append("<actual file content, not shown here>");
        }
        // If none of the arguments have a name specified, we
        // just send all the values as the put body
        else if(getSendParameterValuesAsPostBody()) {
            hasPutBody = true;

            // If a content encoding is specified, we set it as http parameter, so that
            // the post body will be encoded in the specified content encoding
            final String contentEncoding = getContentEncoding();
            boolean haveContentEncoding = false;
            if(contentEncoding != null && contentEncoding.trim().length() > 0) {
                put.getParams().setContentCharset(contentEncoding);
                haveContentEncoding = true;
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
                contentTypeValue = put.getRequestHeader(HEADER_CONTENT_TYPE).getValue();
            }
            StringRequestEntity requestEntity = new StringRequestEntity(putBodyContent.toString(), contentTypeValue, put.getRequestCharSet());
            put.setRequestEntity(requestEntity);
        }
        // Check if we have any content to send for body
        if(hasPutBody) {
            // If the request entity is repeatable, we can send it first to
            // our own stream, so we can return it
            if(put.getRequestEntity().isRepeatable()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                put.getRequestEntity().writeRequest(bos);
                bos.flush();
                // We get the posted bytes using the charset that was used to create them
                putBody.append(new String(bos.toByteArray(),put.getRequestCharSet()));
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
                    put.setRequestHeader(HEADER_CONTENT_TYPE, file.getMimeType());
                }
            }
            // Set the content length
            put.setRequestHeader(HEADER_CONTENT_LENGTH, Long.toString(put.getRequestEntity().getContentLength()));
            return putBody.toString();
        }
        return null;
    }

    /**
     * Class extending FilePart, so that we can send placeholder text
     * instead of the actual file content
     */
    private static class ViewableFilePart extends FilePart {
        private boolean hideFileData;

        public ViewableFilePart(String name, File file, String contentType, String charset) throws FileNotFoundException {
            super(name, file, contentType, charset);
            this.hideFileData = false;
        }

        public void setHideFileData(boolean hideFileData) {
            this.hideFileData = hideFileData;
        }

        @Override
        protected void sendData(OutputStream out) throws IOException {
            // Check if we should send only placeholder text for the
            // file content, or the real file content
            if(hideFileData) {
                out.write("<actual file content, not shown here>".getBytes());// encoding does not really matter here
            }
            else {
                super.sendData(out);
            }
        }
    }

    /**
     * From the <code>HttpMethod</code>, store all the "set-cookie" key-pair
     * values in the cookieManager of the <code>UrlConfig</code>.
     *
     * @param method
     *            <code>HttpMethod</code> which represents the request
     * @param u
     *            <code>URL</code> of the URL request
     * @param cookieManager
     *            the <code>CookieManager</code> containing all the cookies
     */
    protected void saveConnectionCookies(HttpMethod method, URL u, CookieManager cookieManager) {
        if (cookieManager != null) {
            Header hdr[] = method.getResponseHeaders(HEADER_SET_COOKIE);
            for (int i = 0; i < hdr.length; i++) {
                cookieManager.addCookieFromHeader(hdr[i].getValue(),u);
            }
        }
    }


    @Override
    public void threadFinished() {
        log.debug("Thread Finished");

        // Does not need to be synchronised, as all access is from same thread
        Map<HostConfiguration, HttpClient> map = httpClients.get();

        if ( map != null ) {
            for (HttpClient cl : map.values())
            {
                // Can cause NPE in HttpClient 3.1
                //((SimpleHttpConnectionManager)cl.getHttpConnectionManager()).shutdown();// Closes the connection
                // Revert to original method:
                cl.getHttpConnectionManager().closeIdleConnections(-1000);// Closes the connection
            }
            map.clear();
        }
    }

    /** {@inheritDoc} */
    public boolean interrupt() {
        HttpClient client = savedClient;
        if (client != null) {
            savedClient = null;
            // TODO - not sure this is the best method
            final HttpConnectionManager httpConnectionManager = client.getHttpConnectionManager();
            if (httpConnectionManager instanceof SimpleHttpConnectionManager) {// Should be true
                ((SimpleHttpConnectionManager)httpConnectionManager).shutdown();
            }
        }
        return client != null;
    }

}
