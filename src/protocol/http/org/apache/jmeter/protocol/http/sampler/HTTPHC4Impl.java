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
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.security.auth.Subject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpConnection;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.DeflateInputStream;
import org.apache.http.client.entity.InputStreamFactory;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultClientConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.BufferedHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.CharArrayBuffer;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.AuthManager.Mechanism;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.EncoderCache;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.http.util.SlowHC4SocketFactory;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.JsseSSLManager;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.brotli.dec.BrotliInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP Sampler using Apache HttpClient 4.x.
 *
 */
public class HTTPHC4Impl extends HTTPHCAbstractImpl {

    private static final int MAX_BODY_RETAIN_SIZE = JMeterUtils.getPropDefault("httpclient4.max_body_retain_size", 32 * 1024);

    private static final Logger log = LoggerFactory.getLogger(HTTPHC4Impl.class);
    
    private static final InputStreamFactory GZIP = new InputStreamFactory() {
        @Override
        public InputStream create(final InputStream instream) throws IOException {
            return new GZIPInputStream(instream);
        }
    };

    private static final InputStreamFactory DEFLATE = new InputStreamFactory() {
        @Override
        public InputStream create(final InputStream instream) throws IOException {
            return new DeflateInputStream(instream);
        }

    };
    
    private static final InputStreamFactory BROTLI = new InputStreamFactory() {
        @Override
        public InputStream create(final InputStream instream) throws IOException {
            return new BrotliInputStream(instream);
        }
    };

    /** retry count to be used (default 0); 0 = disable retries */
    private static final int RETRY_COUNT = JMeterUtils.getPropDefault("httpclient4.retrycount", 0);
    
    /** true if it's OK to retry requests that have been sent */
    private static final boolean REQUEST_SENT_RETRY_ENABLED = 
            JMeterUtils.getPropDefault("httpclient4.request_sent_retry_enabled", false);

    /** Idle timeout to be applied to connections if no Keep-Alive header is sent by the server (default 0 = disable) */
    private static final int IDLE_TIMEOUT = JMeterUtils.getPropDefault("httpclient4.idletimeout", 0);
    
    private static final int VALIDITY_AFTER_INACTIVITY_TIMEOUT = JMeterUtils.getPropDefault("httpclient4.validate_after_inactivity", 1700);
    
    private static final int TIME_TO_LIVE = JMeterUtils.getPropDefault("httpclient4.time_to_live", 2000);

    /** Preemptive Basic Auth */
    private static final boolean BASIC_AUTH_PREEMPTIVE = JMeterUtils.getPropDefault("httpclient4.auth.preemptive", true);

    private static final String CONTEXT_METRICS = "jmeter_metrics"; // TODO hack for metrics related to HTTPCLIENT-1081, to be removed later
    
    private static final Pattern PORT_PATTERN = Pattern.compile("\\d+"); // only used in .matches(), no need for anchors

    private static final ConnectionKeepAliveStrategy IDLE_STRATEGY = new DefaultConnectionKeepAliveStrategy(){
        @Override
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            long duration = super.getKeepAliveDuration(response, context);
            if (duration <= 0 && IDLE_TIMEOUT > 0) {// none found by the superclass
                log.debug("Setting keepalive to {}", IDLE_TIMEOUT);
                return IDLE_TIMEOUT;
            } 
            return duration; // return the super-class value
        }
        
    };

    /**
     * Special interceptor made to keep metrics when connection is released for some method like HEAD
     * Otherwise calling directly ((HttpConnection) localContext.getAttribute(HttpCoreContext.HTTP_CONNECTION)).getMetrics();
     * would throw org.apache.http.impl.conn.ConnectionShutdownException
     * See <a href="https://bz.apache.org/jira/browse/HTTPCLIENT-1081">HTTPCLIENT-1081</a>
     */
    private static final HttpResponseInterceptor METRICS_SAVER = (HttpResponse response, HttpContext context) -> {
        HttpConnectionMetrics metrics = ((HttpConnection) context.getAttribute(HttpCoreContext.HTTP_CONNECTION)).getMetrics();
        context.setAttribute(CONTEXT_METRICS, metrics);
    };
    private static final HttpRequestInterceptor METRICS_RESETTER = (HttpRequest request, HttpContext context) -> {
        HttpConnectionMetrics metrics = ((HttpConnection) context.getAttribute(HttpCoreContext.HTTP_CONNECTION)).getMetrics();
        metrics.reset();
    };


    /**
     * Headers to save
     */
    private static final String[] HEADERS_TO_SAVE = new String[]{
                    "content-length",
                    "content-encoding",
                    "content-md5"
            };
    
    /**
     * Custom implementation that backups headers related to Compressed responses 
     * that HC core {@link ResponseContentEncoding} removes after uncompressing
     * See Bug 59401
     */
    private static final HttpResponseInterceptor RESPONSE_CONTENT_ENCODING = new ResponseContentEncoding(createLookupRegistry()) {
        @Override
        public void process(HttpResponse response, HttpContext context)
                throws HttpException, IOException {
            ArrayList<Header[]> headersToSave = null;
            
            final HttpEntity entity = response.getEntity();
            final HttpClientContext clientContext = HttpClientContext.adapt(context);
            final RequestConfig requestConfig = clientContext.getRequestConfig();
            // store the headers if necessary
            if (requestConfig.isContentCompressionEnabled() && entity != null && entity.getContentLength() != 0) {
                final Header ceheader = entity.getContentEncoding();
                if (ceheader != null) {
                    headersToSave = new ArrayList<>(3);
                    for(String name : HEADERS_TO_SAVE) {
                        Header[] hdr = response.getHeaders(name); // empty if none
                        headersToSave.add(hdr);
                    }
                }
            }

            // Now invoke original parent code
            super.process(response, clientContext);
            // Should this be in a finally ? 
            if(headersToSave != null) {
                for (Header[] headers : headersToSave) {
                    for (Header headerToRestore : headers) {
                        if (response.containsHeader(headerToRestore.getName())) {
                            break;
                        }
                        response.addHeader(headerToRestore);
                    }
                }
            }
        }
    };
    
    /**
     * 1 HttpClient instance per combination of (HttpClient,HttpClientKey)
     */
    private static final ThreadLocal<Map<HttpClientKey, CloseableHttpClient>> HTTPCLIENTS_CACHE_PER_THREAD_AND_HTTPCLIENTKEY = 
        InheritableThreadLocal.withInitial(() -> new HashMap<>(5));

    // Scheme used for slow HTTP sockets. Cannot be set as a default, because must be set on an HttpClient instance.
    private static final Scheme SLOW_HTTP;
    
    /*
     * Create a set of default parameters from the ones initially created.
     * This allows the defaults to be overridden if necessary from the properties file.
     */
    private static final HttpParams DEFAULT_HTTP_PARAMS;

    private static final String USER_TOKEN = "__jmeter.USER_TOKEN__"; //$NON-NLS-1$
    
    static final String SAMPLER_RESULT_TOKEN = "__jmeter.SAMPLER_RESULT__"; //$NON-NLS-1$
    
    private static final String HTTPCLIENT_TOKEN = "__jmeter.HTTPCLIENT_TOKEN__";

    static {
        log.info("HTTP request retry count = {}", RETRY_COUNT);

        DEFAULT_HTTP_PARAMS = new SyncBasicHttpParams(); // Could we drop the Sync here?
        DEFAULT_HTTP_PARAMS.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
        DEFAULT_HTTP_PARAMS.setIntParameter(ClientPNames.MAX_REDIRECTS, HTTPSamplerBase.MAX_REDIRECTS);
        DefaultHttpClient.setDefaultHttpParams(DEFAULT_HTTP_PARAMS);
        
        // Process Apache HttpClient parameters file
        String file=JMeterUtils.getProperty("hc.parameters.file"); // $NON-NLS-1$
        if (file != null) {
            HttpClientDefaultParameters.load(file, DEFAULT_HTTP_PARAMS);
        }

        // Set up HTTP scheme override if necessary
        if (CPS_HTTP > 0) {
            log.info("Setting up HTTP SlowProtocol, cps={}", CPS_HTTP);
            SLOW_HTTP = new Scheme(HTTPConstants.PROTOCOL_HTTP, HTTPConstants.DEFAULT_HTTP_PORT, new SlowHC4SocketFactory(CPS_HTTP));
        } else {
            SLOW_HTTP = null;
        }
        
        if (localAddress != null){
            DEFAULT_HTTP_PARAMS.setParameter(ConnRoutePNames.LOCAL_ADDRESS, localAddress);
        }
        
    }

    private volatile HttpUriRequest currentRequest; // Accessed from multiple threads

    private volatile boolean resetSSLContext;

    protected HTTPHC4Impl(HTTPSamplerBase testElement) {
        super(testElement);
    }
    
    /**
     * Customize to plug Brotli
     * @return {@link Lookup}
     */
    private static Lookup<InputStreamFactory> createLookupRegistry() {
        return
                RegistryBuilder.<InputStreamFactory>create()
                .register("gzip", GZIP)
                .register("br", BROTLI)
                .register("x-gzip", GZIP)
                .register("deflate", DEFLATE).build();
    }

    /**
     * Implementation that allows GET method to have a body
     */
    public static final class HttpGetWithEntity extends HttpEntityEnclosingRequestBase {

        public HttpGetWithEntity(final URI uri) {
            super();
            setURI(uri);
        }

        @Override
        public String getMethod() {
            return HTTPConstants.GET;
        }
    }

    public static final class HttpDelete extends HttpEntityEnclosingRequestBase {

        public HttpDelete(final URI uri) {
            super();
            setURI(uri);
        }

        @Override
        public String getMethod() {
            return HTTPConstants.DELETE;
        }
    }
    
    @Override
    protected HTTPSampleResult sample(URL url, String method,
            boolean areFollowingRedirect, int frameDepth) {

        if (log.isDebugEnabled()) {
            log.debug("Start : sample {} method {} followingRedirect {} depth {}", 
                    url, method, areFollowingRedirect, frameDepth);            
        }

        HTTPSampleResult res = createSampleResult(url, method);

        CloseableHttpClient httpClient = setupClient(url);

        HttpRequestBase httpRequest = null;
        try {
            URI uri = url.toURI();
            if (method.equals(HTTPConstants.POST)) {
                httpRequest = new HttpPost(uri);
            } else if (method.equals(HTTPConstants.GET)) {
                // Some servers fail if Content-Length is equal to 0
                // so to avoid this we use HttpGet when there is no body (Content-Length will not be set)
                // otherwise we use HttpGetWithEntity
                if ( !areFollowingRedirect 
                        && ((!hasArguments() && getSendFileAsPostBody()) 
                        || getSendParameterValuesAsPostBody()) ) {
                    httpRequest = new HttpGetWithEntity(uri);
                } else {
                    httpRequest = new HttpGet(uri);
                }
            } else if (method.equals(HTTPConstants.PUT)) {
                httpRequest = new HttpPut(uri);
            } else if (method.equals(HTTPConstants.HEAD)) {
                httpRequest = new HttpHead(uri);
            } else if (method.equals(HTTPConstants.TRACE)) {
                httpRequest = new HttpTrace(uri);
            } else if (method.equals(HTTPConstants.OPTIONS)) {
                httpRequest = new HttpOptions(uri);
            } else if (method.equals(HTTPConstants.DELETE)) {
                httpRequest = new HttpDelete(uri);
            } else if (method.equals(HTTPConstants.PATCH)) {
                httpRequest = new HttpPatch(uri);
            } else if (HttpWebdav.isWebdavMethod(method)) {
                httpRequest = new HttpWebdav(method, uri);
            } else {
                throw new IllegalArgumentException("Unexpected method: '"+method+"'");
            }
            setupRequest(url, httpRequest, res); // can throw IOException
        } catch (Exception e) {
            res.sampleStart();
            res.sampleEnd();
            errorResult(e, res);
            return res;
        }

        HttpContext localContext = new BasicHttpContext();
        setupClientContextBeforeSample(localContext);
        
        res.sampleStart();

        final CacheManager cacheManager = getCacheManager();
        if (cacheManager != null && HTTPConstants.GET.equalsIgnoreCase(method) && cacheManager.inCache(url, httpRequest.getAllHeaders())) {
            return updateSampleResultForResourceInCache(res);
        }
        CloseableHttpResponse httpResponse = null;
        try {
            currentRequest = httpRequest;
            handleMethod(method, res, httpRequest, localContext);
            // store the SampleResult in LocalContext to compute connect time
            localContext.setAttribute(SAMPLER_RESULT_TOKEN, res);
            // perform the sample
            httpResponse = 
                    executeRequest(httpClient, httpRequest, localContext, url);

            // Needs to be done after execute to pick up all the headers
            final HttpRequest request = (HttpRequest) localContext.getAttribute(HttpCoreContext.HTTP_REQUEST);
            extractClientContextAfterSample(localContext);
            // We've finished with the request, so we can add the LocalAddress to it for display
            final InetAddress localAddr = (InetAddress) httpRequest.getParams().getParameter(ConnRoutePNames.LOCAL_ADDRESS);
            if (localAddr != null) {
                request.addHeader(HEADER_LOCAL_ADDRESS, localAddr.toString());
            }
            res.setRequestHeaders(getAllHeadersExceptCookie(request));

            Header contentType = httpResponse.getLastHeader(HTTPConstants.HEADER_CONTENT_TYPE);
            if (contentType != null){
                String ct = contentType.getValue();
                res.setContentType(ct);
                res.setEncodingAndType(ct);                    
            }
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                res.setResponseData(readResponse(res, entity.getContent(), entity.getContentLength()));
            }
            
            res.sampleEnd(); // Done with the sampling proper.
            currentRequest = null;

            // Now collect the results into the HTTPSampleResult:
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            res.setResponseCode(Integer.toString(statusCode));
            res.setResponseMessage(statusLine.getReasonPhrase());
            res.setSuccessful(isSuccessCode(statusCode));

            res.setResponseHeaders(getResponseHeaders(httpResponse, localContext));
            if (res.isRedirect()) {
                final Header headerLocation = httpResponse.getLastHeader(HTTPConstants.HEADER_LOCATION);
                if (headerLocation == null) { // HTTP protocol violation, but avoids NPE
                    throw new IllegalArgumentException("Missing location header in redirect for " + httpRequest.getRequestLine());
                }
                String redirectLocation = headerLocation.getValue();
                res.setRedirectLocation(redirectLocation);
            }

            // record some sizes to allow HTTPSampleResult.getBytes() with different options
            HttpConnectionMetrics  metrics = (HttpConnectionMetrics) localContext.getAttribute(CONTEXT_METRICS);
            long headerBytes = 
                (long)res.getResponseHeaders().length()   // condensed length (without \r)
              + (long) httpResponse.getAllHeaders().length // Add \r for each header
              + 1L // Add \r for initial header
              + 2L; // final \r\n before data
            long totalBytes = metrics.getReceivedBytesCount();
            res.setHeadersSize((int)headerBytes);
            res.setBodySize(totalBytes - headerBytes);
            res.setSentBytes(metrics.getSentBytesCount());
            if (log.isDebugEnabled()) {
                long total = res.getHeadersSize() + res.getBodySizeAsLong();
                log.debug("ResponseHeadersSize={} Content-Length={} Total={}",
                        res.getHeadersSize(), res.getBodySizeAsLong(), total);
            }

            // If we redirected automatically, the URL may have changed
            if (getAutoRedirects()) {
                HttpUriRequest req = (HttpUriRequest) localContext.getAttribute(HttpCoreContext.HTTP_REQUEST);
                HttpHost target = (HttpHost) localContext.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
                URI redirectURI = req.getURI();
                if (redirectURI.isAbsolute()) {
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
            log.debug("IOException", e);
            if (res.getEndTime() == 0) {
                res.sampleEnd();
            }
           // pick up headers if failed to execute the request
            if (res.getRequestHeaders() != null) {
                log.debug("Overwriting request old headers: {}", res.getRequestHeaders());
            }
            res.setRequestHeaders(getAllHeadersExceptCookie((HttpRequest) localContext.getAttribute(HttpCoreContext.HTTP_REQUEST)));
            errorResult(e, res);
            return res;
        } catch (RuntimeException e) {
            log.debug("RuntimeException", e);
            if (res.getEndTime() == 0) {
                res.sampleEnd();
            }
            errorResult(e, res);
            return res;
        } finally {
            JOrphanUtils.closeQuietly(httpResponse);
            currentRequest = null;
            JMeterContextService.getContext().getSamplerContext().remove(HTTPCLIENT_TOKEN);
        }
        return res;
    }

    /**
     * Store in JMeter Variables the UserToken so that the SSL context is reused
     * See <a href="https://bz.apache.org/bugzilla/show_bug.cgi?id=57804">Bug 57804</a>
     * @param localContext {@link HttpContext}
     */
    private void extractClientContextAfterSample(HttpContext localContext) {
        Object userToken = localContext.getAttribute(HttpClientContext.USER_TOKEN);
        if(userToken != null) {
            log.debug("Extracted from HttpContext user token:{} storing it as JMeter variable:{}", userToken, USER_TOKEN);
            // During recording JMeterContextService.getContext().getVariables() is null
            JMeterVariables jMeterVariables = JMeterContextService.getContext().getVariables();
            if (jMeterVariables != null) {
                jMeterVariables.putObject(USER_TOKEN, userToken); 
            }
        }
    }

    /**
     * Configure the UserToken so that the SSL context is reused
     * See <a href="https://bz.apache.org/bugzilla/show_bug.cgi?id=57804">Bug 57804</a>
     * @param localContext {@link HttpContext}
     */
    private void setupClientContextBeforeSample(HttpContext localContext) {
        Object userToken = null;
        // During recording JMeterContextService.getContext().getVariables() is null
        JMeterVariables jMeterVariables = JMeterContextService.getContext().getVariables();
        if(jMeterVariables != null) {
            userToken = jMeterVariables.getObject(USER_TOKEN);            
        }
        if(userToken != null) {
            log.debug("Found user token:{} as JMeter variable:{}, storing it in HttpContext", userToken, USER_TOKEN);
            localContext.setAttribute(HttpClientContext.USER_TOKEN, userToken);
        } else {
            // It would be better to create a ClientSessionManager that would compute this value
            // for now it can be Thread.currentThread().getName() but must be changed when we would change 
            // the Thread per User model
            String userId = Thread.currentThread().getName();
            log.debug("Storing in HttpContext the user token: {}", userId);
            localContext.setAttribute(HttpClientContext.USER_TOKEN, userId);
        }
    }

    /**
     * Calls {@link #sendPostData(HttpPost)} if method is <code>POST</code> and
     * {@link #sendEntityData(HttpEntityEnclosingRequestBase)} if method is
     * <code>PUT</code> or <code>PATCH</code>
     * <p>
     * Field HTTPSampleResult#queryString of result is modified in the 2 cases
     * 
     * @param method
     *            String HTTP method
     * @param result
     *            {@link HTTPSampleResult}
     * @param httpRequest
     *            {@link HttpRequestBase}
     * @param localContext
     *            {@link HttpContext}
     * @throws IOException
     *             when posting data fails due to I/O
     */
    protected void handleMethod(String method, HTTPSampleResult result,
            HttpRequestBase httpRequest, HttpContext localContext) throws IOException {
        // Handle the various methods
        if (httpRequest instanceof HttpPost) {
            String postBody = sendPostData((HttpPost)httpRequest);
            result.setQueryString(postBody);
        } else if (httpRequest instanceof HttpEntityEnclosingRequestBase) {
            String entityBody = sendEntityData((HttpEntityEnclosingRequestBase) httpRequest);
            result.setQueryString(entityBody);
        }
    }

    /**
     * Create HTTPSampleResult filling url, method and SampleLabel.
     * Monitor field is computed calling isMonitor()
     * @param url URL
     * @param method HTTP Method
     * @return {@link HTTPSampleResult}
     */
    protected HTTPSampleResult createSampleResult(URL url, String method) {
        HTTPSampleResult res = new HTTPSampleResult();

        res.setSampleLabel(url.toString()); // May be replaced later
        res.setHTTPMethod(method);
        res.setURL(url);
        
        return res;
    }

    /**
     * Execute request either as is or under PrivilegedAction 
     * if a Subject is available for url
     * @param httpClient the {@link CloseableHttpClient} to be used to execute the httpRequest
     * @param httpRequest the {@link HttpRequest} to be executed
     * @param localContext th {@link HttpContext} to be used for execution
     * @param url the target url (will be used to look up a possible subject for the execution)
     * @return the result of the execution of the httpRequest
     * @throws IOException
     * @throws ClientProtocolException
     */
    private CloseableHttpResponse executeRequest(final CloseableHttpClient httpClient,
            final HttpRequestBase httpRequest, final HttpContext localContext, final URL url)
            throws IOException, ClientProtocolException {
        AuthManager authManager = getAuthManager();
        if (authManager != null) {
            Subject subject = authManager.getSubjectForUrl(url);
            if (subject != null) {
                try {
                    return Subject.doAs(subject,
                            (PrivilegedExceptionAction<CloseableHttpResponse>) () ->
                                    httpClient.execute(httpRequest, localContext));
                } catch (PrivilegedActionException e) {
                    log.error("Can't execute httpRequest with subject: {}", subject, e);
                    throw new RuntimeException("Can't execute httpRequest with subject:" + subject, e);
                }
            }

            if(BASIC_AUTH_PREEMPTIVE) {
                Authorization authorization = authManager.getAuthForURL(url);
                if(authorization != null && Mechanism.BASIC_DIGEST.equals(authorization.getMechanism())) {
                    HttpHost target = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
                    // Create AuthCache instance
                    AuthCache authCache = new BasicAuthCache();
                    // Generate BASIC scheme object and 
                    // add it to the local auth cache
                    BasicScheme basicAuth = new BasicScheme();
                    authCache.put(target, basicAuth);
                    // Add AuthCache to the execution context
                    localContext.setAttribute(HttpClientContext.AUTH_CACHE, authCache);
                }
            }
        }
        return httpClient.execute(httpRequest, localContext);
    }

    /**
     * Holder class for all fields that define an HttpClient instance;
     * used as the key to the ThreadLocal map of HttpClient instances.
     */
    private static final class HttpClientKey {

        private final String target; // protocol://[user:pass@]host:[port]
        private final boolean hasProxy;
        private final String proxyHost;
        private final int proxyPort;
        private final String proxyUser;
        private final String proxyPass;
        
        private final int hashCode; // Always create hash because we will always need it

        /**
         * @param url URL Only protocol and url authority are used (protocol://[user:pass@]host:[port])
         * @param hasProxy has proxy
         * @param proxyHost proxy host
         * @param proxyPort proxy port
         * @param proxyUser proxy user
         * @param proxyPass proxy password
         */
        public HttpClientKey(URL url, boolean hasProxy, String proxyHost,
                int proxyPort, String proxyUser, String proxyPass) {
            // N.B. need to separate protocol from authority otherwise http://server would match https://erver (<= sic, not typo error)
            // could use separate fields, but simpler to combine them
            this.target = url.getProtocol()+"://"+url.getAuthority();
            this.hasProxy = hasProxy;
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
            hash = hash*31 + target.hashCode();
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
            if (!(obj instanceof HttpClientKey)) {
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
                this.target.equals(other.target);
            }
            // No proxy, so don't check proxy fields
            return 
                this.hasProxy == other.hasProxy &&
                this.target.equals(other.target);
        }

        @Override
        public int hashCode(){
            return hashCode;
        }

        // For debugging
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(target);
            if (hasProxy) {
                sb.append(" via ");
                sb.append(proxyUser);
                sb.append('@');
                sb.append(proxyHost);
                sb.append(':');
                sb.append(proxyPort);
            }
            return sb.toString();
        }
    }

    private CloseableHttpClient setupClient(URL url) {

        Map<HttpClientKey, CloseableHttpClient> mapHttpClientPerHttpClientKey = HTTPCLIENTS_CACHE_PER_THREAD_AND_HTTPCLIENTKEY.get();
        
        final String host = url.getHost();
        String proxyHost = getProxyHost();
        int proxyPort = getProxyPortInt();
        String proxyPass = getProxyPass();
        String proxyUser = getProxyUser();

        // static proxy is the globally define proxy eg command line or properties
        boolean useStaticProxy = isStaticProxy(host);
        // dynamic proxy is the proxy defined for this sampler
        boolean useDynamicProxy = isDynamicProxy(proxyHost, proxyPort);
        boolean useProxy = useStaticProxy || useDynamicProxy;
        
        // if both dynamic and static are used, the dynamic proxy has priority over static
        if(!useDynamicProxy) {
            proxyHost = PROXY_HOST;
            proxyPort = PROXY_PORT;
            proxyUser = PROXY_USER;
            proxyPass = PROXY_PASS;
        }

        // Lookup key - must agree with all the values used to create the HttpClient.
        HttpClientKey key = new HttpClientKey(url, useProxy, proxyHost, proxyPort, proxyUser, proxyPass);
        
        CloseableHttpClient httpClient = null;
        boolean concurrentDwn = this.testElement.isConcurrentDwn();
        if(concurrentDwn) {
            httpClient = (CloseableHttpClient) JMeterContextService.getContext().getSamplerContext().get(HTTPCLIENT_TOKEN);
        }
        
        if (httpClient == null) {
            httpClient = mapHttpClientPerHttpClientKey.get(key);
        }

        if (httpClient != null && resetSSLContext && HTTPConstants.PROTOCOL_HTTPS.equalsIgnoreCase(url.getProtocol())) {
            ((AbstractHttpClient) httpClient).clearRequestInterceptors(); 
            ((AbstractHttpClient) httpClient).clearResponseInterceptors(); 
            httpClient.getConnectionManager().closeIdleConnections(1L, TimeUnit.MICROSECONDS);
            httpClient = null;
            JsseSSLManager sslMgr = (JsseSSLManager) SSLManager.getInstance();
            sslMgr.resetContext();
            resetSSLContext = false;
        }

        if (httpClient == null) { // One-time init for this client

            HttpParams clientParams = new DefaultedHttpParams(new BasicHttpParams(), DEFAULT_HTTP_PARAMS);

            DnsResolver resolver = this.testElement.getDNSResolver();
            if (resolver == null) {
                resolver = SystemDefaultDnsResolver.INSTANCE;
            }
            MeasuringConnectionManager connManager = new MeasuringConnectionManager(
                    createSchemeRegistry(), 
                    resolver, 
                    TIME_TO_LIVE,
                    VALIDITY_AFTER_INACTIVITY_TIMEOUT);
            
            // Modern browsers use more connections per host than the current httpclient default (2)
            // when using parallel download the httpclient and connection manager are shared by the downloads threads
            // to be realistic JMeter must set an higher value to DefaultMaxPerRoute
            if(concurrentDwn) {
                try {
                    int maxConcurrentDownloads = Integer.parseInt(this.testElement.getConcurrentPool());
                    connManager.setDefaultMaxPerRoute(Math.max(maxConcurrentDownloads, connManager.getDefaultMaxPerRoute()));                
                } catch (NumberFormatException nfe) {
                   // no need to log -> will be done by the sampler
                }
            }
            
            httpClient = new DefaultHttpClient(connManager, clientParams) {
                @Override
                protected HttpRequestRetryHandler createHttpRequestRetryHandler() {
                    return new StandardHttpRequestRetryHandler(RETRY_COUNT, 
                            REQUEST_SENT_RETRY_ENABLED);
                }
            };
            
            if (IDLE_TIMEOUT > 0) {
                ((AbstractHttpClient) httpClient).setKeepAliveStrategy(IDLE_STRATEGY );
            }
            // see https://issues.apache.org/jira/browse/HTTPCORE-397
            ((AbstractHttpClient) httpClient).setReuseStrategy(DefaultClientConnectionReuseStrategy.INSTANCE);
            ((AbstractHttpClient) httpClient).addResponseInterceptor(RESPONSE_CONTENT_ENCODING);
            ((AbstractHttpClient) httpClient).addResponseInterceptor(METRICS_SAVER); // HACK
            ((AbstractHttpClient) httpClient).addRequestInterceptor(METRICS_RESETTER); 
            
            // Override the default schemes as necessary
            SchemeRegistry schemeRegistry = httpClient.getConnectionManager().getSchemeRegistry();

            if (SLOW_HTTP != null){
                schemeRegistry.register(SLOW_HTTP);
            }

            // Set up proxy details
            if(useProxy) {

                HttpHost proxy = new HttpHost(proxyHost, proxyPort);
                clientParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                
                if (proxyUser.length() > 0) {                   
                    ((AbstractHttpClient) httpClient).getCredentialsProvider().setCredentials(
                            new AuthScope(proxyHost, proxyPort),
                            new NTCredentials(proxyUser, proxyPass, LOCALHOST, PROXY_DOMAIN));
                }
            }

            // Bug 52126 - we do our own cookie handling
            clientParams.setParameter(ClientPNames.COOKIE_POLICY, CookieSpecs.IGNORE_COOKIES);

            if (log.isDebugEnabled()) {
                log.debug("Created new HttpClient: @"+System.identityHashCode(httpClient) + " " + key.toString());
            }

            mapHttpClientPerHttpClientKey.put(key, httpClient); // save the agent for next time round
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Reusing the HttpClient: @"+System.identityHashCode(httpClient) + " " + key.toString());
            }
        }

        if(concurrentDwn) {
            JMeterContextService.getContext().getSamplerContext().put(HTTPCLIENT_TOKEN, httpClient);
        }

        // TODO - should this be done when the client is created?
        // If so, then the details need to be added as part of HttpClientKey
        setConnectionAuthorization(httpClient, url, getAuthManager(), key);

        return httpClient;
    }

    /**
     * Setup LazySchemeSocketFactory
     * @see "https://bz.apache.org/bugzilla/show_bug.cgi?id=58099"
     */
    private static SchemeRegistry createSchemeRegistry() {
        final SchemeRegistry registry = new SchemeRegistry();
        registry.register(
                new Scheme("http", 80, PlainSocketFactory.getSocketFactory())); //$NON-NLS-1$
        registry.register(
                new Scheme("https", 443, new LazySchemeSocketFactory())); //$NON-NLS-1$
        return registry;
    }

    /**
     * Setup following elements on httpRequest:
     * <ul>
     * <li>ConnRoutePNames.LOCAL_ADDRESS enabling IP-SPOOFING</li>
     * <li>Socket and connection timeout</li>
     * <li>Redirect handling</li>
     * <li>Keep Alive header or Connection Close</li>
     * <li>Calls setConnectionHeaders to setup headers</li>
     * <li>Calls setConnectionCookie to setup Cookie</li>
     * </ul>
     * 
     * @param url
     *            {@link URL} of the request
     * @param httpRequest
     *            http request for the request
     * @param res
     *            sample result to set cookies on
     * @throws IOException
     *             if hostname/ip to use could not be figured out
     */
    protected void setupRequest(URL url, HttpRequestBase httpRequest, HTTPSampleResult res)
        throws IOException {
    
        HttpParams requestParams = httpRequest.getParams();
        // Set up the local address if one exists
        final InetAddress inetAddr = getIpSourceAddress();
        if (inetAddr != null) {// Use special field ip source address (for pseudo 'ip spoofing')
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
            httpRequest.setHeader(HTTPConstants.HEADER_CONNECTION, HTTPConstants.KEEP_ALIVE);
        } else {
            httpRequest.setHeader(HTTPConstants.HEADER_CONNECTION, HTTPConstants.CONNECTION_CLOSE);
        }
    
        setConnectionHeaders(httpRequest, url, getHeaderManager(), getCacheManager());
        String cookies = setConnectionCookie(httpRequest, url, getCookieManager());
    
        if (res != null) {
            if(cookies != null && !cookies.isEmpty()) {
                res.setCookies(cookies);
            } else {
                // During recording Cookie Manager doesn't handle cookies
                res.setCookies(getOnlyCookieFromHeaders(httpRequest));
            }
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
     * @param localContext {@link HttpContext}
     * @return string containing the headers, one per line
     */
    private String getResponseHeaders(HttpResponse response, HttpContext localContext) {
        Header[] rh = response.getAllHeaders();

        StringBuilder headerBuf = new StringBuilder(40 * (rh.length+1));
        headerBuf.append(response.getStatusLine());// header[0] is not the status line...
        headerBuf.append("\n"); // $NON-NLS-1$

        for (Header responseHeader : rh) {
            writeHeader(headerBuf, responseHeader);
        }
        return headerBuf.toString();
    }

    /**
     * Write header to headerBuffer in an optimized way
     * @param headerBuffer {@link StringBuilder}
     * @param header {@link Header}
     */
    private void writeHeader(StringBuilder headerBuffer, Header header) {
        if(header instanceof BufferedHeader) {
            CharArrayBuffer buffer = ((BufferedHeader)header).getBuffer();
            headerBuffer.append(buffer.buffer(), 0, buffer.length()).append('\n'); // $NON-NLS-1$
        }
        else {
            headerBuffer.append(header.getName())
            .append(": ") // $NON-NLS-1$
            .append(header.getValue())
            .append('\n'); // $NON-NLS-1$
        }
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
    protected String setConnectionCookie(HttpRequest request, URL url, CookieManager cookieManager) {
        String cookieHeader = null;
        if (cookieManager != null) {
            cookieHeader = cookieManager.getCookieHeaderForURL(url);
            if (cookieHeader != null) {
                request.setHeader(HTTPConstants.HEADER_COOKIE, cookieHeader);
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
    protected void setConnectionHeaders(HttpRequestBase request, URL url, HeaderManager headerManager, CacheManager cacheManager) {
        if (headerManager != null) {
            CollectionProperty headers = headerManager.getHeaders();
            if (headers != null) {
                for (JMeterProperty jMeterProperty : headers) {
                    org.apache.jmeter.protocol.http.control.Header header
                            = (org.apache.jmeter.protocol.http.control.Header)
                            jMeterProperty.getObjectValue();
                    String headerName = header.getName();
                    // Don't allow override of Content-Length
                    // TODO - what other headers are not allowed?
                    if (!HTTPConstants.HEADER_CONTENT_LENGTH.equalsIgnoreCase(headerName)) {
                        String headerValue = header.getValue();
                        if (HTTPConstants.HEADER_HOST.equalsIgnoreCase(headerName)) {
                            int port = getPortFromHostHeader(headerValue, url.getPort());
                            // remove any port specification
                            headerValue = headerValue.replaceFirst(":\\d+$", ""); // $NON-NLS-1$ $NON-NLS-2$
                            if (port != -1 && port == url.getDefaultPort()) {
                                port = -1; // no need to specify the port if it is the default
                            }
                            request.getParams().setParameter(ClientPNames.VIRTUAL_HOST, new HttpHost(headerValue, port));
                        } else {
                            request.addHeader(headerName, headerValue);
                        }
                    }
                }
            }
        }
        if (cacheManager != null) {
            cacheManager.setHeaders(url, request);
        }
    }

    /**
     * Get port from the value of the Host header, or return the given
     * defaultValue
     *
     * @param hostHeaderValue
     *            value of the http Host header
     * @param defaultValue
     *            value to be used, when no port could be extracted from
     *            hostHeaderValue
     * @return integer representing the port for the host header
     */
    private int getPortFromHostHeader(String hostHeaderValue, int defaultValue) {
        String[] hostParts = hostHeaderValue.split(":");
        if (hostParts.length > 1) {
            String portString = hostParts[hostParts.length - 1];
            if (PORT_PATTERN.matcher(portString).matches()) {
                return Integer.parseInt(portString);
            }
        }
        return defaultValue;
    }

    /**
     * Get all the request headers except Cookie for the <code>HttpRequest</code>
     *
     * @param method
     *            <code>HttpMethod</code> which represents the request
     * @return the headers as a string
     */
    private String getAllHeadersExceptCookie(HttpRequest method) {
        return getFromHeadersMatchingPredicate(method, ALL_EXCEPT_COOKIE);
    }
    
    /**
     * Get only Cookie header for the <code>HttpRequest</code>
     *
     * @param method
     *            <code>HttpMethod</code> which represents the request
     * @return the headers as a string
     */
    private String getOnlyCookieFromHeaders(HttpRequest method) {
        String cookieHeader= getFromHeadersMatchingPredicate(method, ONLY_COOKIE).trim();
        if(!cookieHeader.isEmpty()) {
            return cookieHeader.substring((HTTPConstants.HEADER_COOKIE_IN_REQUEST).length(), cookieHeader.length()).trim();
        }
        return "";
    }

    
    /**
     * Get only cookies from request headers for the <code>HttpRequest</code>
     *
     * @param method
     *            <code>HttpMethod</code> which represents the request
     * @return the headers as a string
     */
    private String getFromHeadersMatchingPredicate(HttpRequest method, Predicate<String> predicate) {
        if(method != null) {
            // Get all the request headers
            StringBuilder hdrs = new StringBuilder(150);
            Header[] requestHeaders = method.getAllHeaders();
            for (Header requestHeader : requestHeaders) {
                // Get header if it matches predicate
                if (predicate.test(requestHeader.getName())) {
                    writeHeader(hdrs, requestHeader);
                }
            }
    
            return hdrs.toString();
        }
        return ""; ////$NON-NLS-1$
    }

    /**
     * Setup credentials for url AuthScope but keeps Proxy AuthScope credentials
     * @param client HttpClient
     * @param url URL
     * @param authManager {@link AuthManager}
     * @param key key
     */
    private void setConnectionAuthorization(CloseableHttpClient client, URL url, AuthManager authManager, HttpClientKey key) {
        CredentialsProvider credentialsProvider = 
            ((AbstractHttpClient) client).getCredentialsProvider();
        if (authManager != null) {
            if(authManager.hasAuthForURL(url)) {
                authManager.setupCredentials(client, url, credentialsProvider, LOCALHOST);
            } else {
                credentialsProvider.clear();
            }
        } else {
            Credentials credentials = null;
            AuthScope authScope = null;
            if(key.hasProxy && !StringUtils.isEmpty(key.proxyUser)) {
                authScope = new AuthScope(key.proxyHost, key.proxyPort);
                credentials = credentialsProvider.getCredentials(authScope);
            }
            credentialsProvider.clear(); 
            if(credentials != null) {
                credentialsProvider.setCredentials(authScope, credentials);
            }
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
    /**
     * 
     * @param post {@link HttpPost}
     * @return String posted body if computable
     * @throws IOException if sending the data fails due to I/O
     */
    protected String sendPostData(HttpPost post)  throws IOException {
        // Buffer to hold the post body, except file content
        StringBuilder postedBody = new StringBuilder(1000);
        HTTPFileArg[] files = getHTTPFiles();

        final String contentEncoding = getContentEncodingOrNull();
        final boolean haveContentEncoding = contentEncoding != null;

        // Check if we should do a multipart/form-data or an
        // application/x-www-form-urlencoded post request
        if(getUseMultipartForPost()) {
            // If a content encoding is specified, we use that as the
            // encoding of any parameter values
            Charset charset;
            if(haveContentEncoding) {
                charset = Charset.forName(contentEncoding);
            } else {
                charset = MIME.DEFAULT_CHARSET;
            }
            
            if(log.isDebugEnabled()) {
                log.debug("Building multipart with:getDoBrowserCompatibleMultipart(): {}, with charset:{}, haveContentEncoding:{}", 
                        getDoBrowserCompatibleMultipart(), charset, haveContentEncoding);
            }
            // Write the request to our own stream
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            if(getDoBrowserCompatibleMultipart()) {
                multipartEntityBuilder.setLaxMode();
            } else {
                multipartEntityBuilder.setStrictMode();
            }
            // Create the parts
            // Add any parameters
            for (JMeterProperty jMeterProperty : getArguments()) {
                HTTPArgument arg = (HTTPArgument) jMeterProperty.getObjectValue();
                String parameterName = arg.getName();
                if (arg.isSkippable(parameterName)) {
                    continue;
                }
                StringBody stringBody = new StringBody(arg.getValue(), ContentType.create("text/plain", charset));
                FormBodyPart formPart = FormBodyPartBuilder.create(
                        parameterName, stringBody).build();
                multipartEntityBuilder.addPart(formPart);
            }

            // Add any files
            // Cannot retrieve parts once added to the MultiPartEntity, so have to save them here.
            ViewableFileBody[] fileBodies = new ViewableFileBody[files.length];
            for (int i=0; i < files.length; i++) {
                HTTPFileArg file = files[i];
                
                File reservedFile = FileServer.getFileServer().getResolvedFile(file.getPath());
                fileBodies[i] = new ViewableFileBody(reservedFile, file.getMimeType());
                multipartEntityBuilder.addPart(file.getParamName(), fileBodies[i] );
            }

            HttpEntity entity = multipartEntityBuilder.build();
            post.setEntity(entity);

            if (entity.isRepeatable()){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                for(ViewableFileBody fileBody : fileBodies){
                    fileBody.hideFileData = true;
                }
                entity.writeTo(bos);
                for(ViewableFileBody fileBody : fileBodies){
                    fileBody.hideFileData = false;
                }
                bos.flush();
                // We get the posted bytes using the encoding used to create it
                postedBody.append(bos.toString(
                        contentEncoding == null ? "US-ASCII" // $NON-NLS-1$ this is the default used by HttpClient
                        : contentEncoding));
                bos.close();
            } else {
                postedBody.append("<Multipart was not repeatable, cannot view what was sent>"); // $NON-NLS-1$
            }
        } else { // not multipart
            // Check if the header manager had a content type header
            // This allows the user to specify his own content-type for a POST request
            Header contentTypeHeader = post.getFirstHeader(HTTPConstants.HEADER_CONTENT_TYPE);
            boolean hasContentTypeHeader = contentTypeHeader != null && contentTypeHeader.getValue() != null && contentTypeHeader.getValue().length() > 0;
            // If there are no arguments, we can send a file as the body of the request
            // TODO: needs a multiple file upload scenerio
            if(!hasArguments() && getSendFileAsPostBody()) {
                // If getSendFileAsPostBody returned true, it's sure that file is not null
                HTTPFileArg file = files[0];
                if(!hasContentTypeHeader) {
                    // Allow the mimetype of the file to control the content type
                    if(file.getMimeType() != null && file.getMimeType().length() > 0) {
                        post.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, file.getMimeType());
                    }
                    else {
                        post.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED);
                    }
                }

                FileEntity fileRequestEntity = new FileEntity(new File(file.getPath()),(ContentType) null);// TODO is null correct?
                post.setEntity(fileRequestEntity);

                // We just add placeholder text for file content
                postedBody.append("<actual file content, not shown here>");
            } else {
                // In a post request which is not multipart, we only support
                // parameters, no file upload is allowed

                // If a content encoding is specified, we set it as http parameter, so that
                // the post body will be encoded in the specified content encoding
                if(haveContentEncoding) {
                    post.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, contentEncoding);
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
                            post.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, file.getMimeType());
                        }
                        else {
                             // TODO - is this the correct default?
                            post.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED);
                        }
                    }

                    // Just append all the parameter values, and use that as the post body
                    StringBuilder postBody = new StringBuilder();
                    for (JMeterProperty jMeterProperty : getArguments()) {
                        HTTPArgument arg = (HTTPArgument) jMeterProperty.getObjectValue();
                        // Note: if "Encoded?" is not selected, arg.getEncodedValue is equivalent to arg.getValue
                        if (haveContentEncoding) {
                            postBody.append(arg.getEncodedValue(contentEncoding));
                        } else {
                            postBody.append(arg.getEncodedValue());
                        }
                    }
                    // Let StringEntity perform the encoding
                    StringEntity requestEntity = new StringEntity(postBody.toString(), contentEncoding);
                    post.setEntity(requestEntity);
                    postedBody.append(postBody.toString());
                } else {
                    // It is a normal post request, with parameter names and values

                    // Set the content type
                    if(!hasContentTypeHeader) {
                        post.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED);
                    }
                    // Add the parameters
                    PropertyIterator args = getArguments().iterator();
                    List <NameValuePair> nvps = new ArrayList<>();
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
                        postedBody.append(bos.toString(contentEncoding != null?contentEncoding:SampleResult.DEFAULT_HTTP_ENCODING));
                        
                        bos.close();
                    }  else {
                        postedBody.append("<RequestEntity was not repeatable, cannot view what was sent>");
                    }
                }
            }
        }
        return postedBody.toString();
    }

    // TODO merge put and post methods as far as possible.
    // e.g. post checks for multipart form/files, and if not, invokes sendData(HttpEntityEnclosingRequestBase)


    /**
     * Creates the entity data to be sent.
     * <p>
     * If there is a file entry with a non-empty MIME type we use that to
     * set the request Content-Type header, otherwise we default to whatever
     * header is present from a Header Manager.
     * <p>
     * If the content charset {@link #getContentEncoding()} is null or empty 
     * we use the HC4 default provided by {@link HTTP#DEF_CONTENT_CHARSET} which is
     * ISO-8859-1.
     * 
     * @param entity to be processed, e.g. PUT or PATCH
     * @return the entity content, may be empty
     * @throws  UnsupportedEncodingException for invalid charset name
     * @throws IOException cannot really occur for ByteArrayOutputStream methods
     */
    protected String sendEntityData( HttpEntityEnclosingRequestBase entity) throws IOException {
        boolean hasEntityBody = false;

        final HTTPFileArg[] files = getHTTPFiles();
        // Allow the mimetype of the file to control the content type
        // This is not obvious in GUI if you are not uploading any files,
        // but just sending the content of nameless parameters
        final HTTPFileArg file = files.length > 0? files[0] : null;
        String contentTypeValue = null;
        if(file != null && file.getMimeType() != null && file.getMimeType().length() > 0) {
            contentTypeValue = file.getMimeType();
            entity.setHeader(HEADER_CONTENT_TYPE, contentTypeValue); // we provide the MIME type here
        }

        // Check for local contentEncoding (charset) override; fall back to default for content body
        // we do this here rather so we can use the same charset to retrieve the data
        final String charset = getContentEncoding(HTTP.DEF_CONTENT_CHARSET.name());

        // Only create this if we are overriding whatever default there may be
        // If there are no arguments, we can send a file as the body of the request

        if(!hasArguments() && getSendFileAsPostBody()) {
            hasEntityBody = true;

            // If getSendFileAsPostBody returned true, it's sure that file is not null
            File reservedFile = FileServer.getFileServer().getResolvedFile(files[0].getPath());
            FileEntity fileRequestEntity = new FileEntity(reservedFile); // no need for content-type here
            entity.setEntity(fileRequestEntity);
        }
        // If none of the arguments have a name specified, we
        // just send all the values as the entity body
        else if(getSendParameterValuesAsPostBody()) {
            hasEntityBody = true;

            // Just append all the parameter values, and use that as the entity body
            Arguments arguments = getArguments();
            StringBuilder entityBodyContent = new StringBuilder(arguments.getArgumentCount()*15);
            for (JMeterProperty jMeterProperty : arguments) {
                HTTPArgument arg = (HTTPArgument) jMeterProperty.getObjectValue();
                // Note: if "Encoded?" is not selected, arg.getEncodedValue is equivalent to arg.getValue
                if (charset != null) {
                    entityBodyContent.append(arg.getEncodedValue(charset));
                } else {
                    entityBodyContent.append(arg.getEncodedValue());
                }
            }
            StringEntity requestEntity = new StringEntity(entityBodyContent.toString(), charset);
            entity.setEntity(requestEntity);
        }
        // Check if we have any content to send for body
        if(hasEntityBody) {
            // If the request entity is repeatable, we can send it first to
            // our own stream, so we can return it
            final HttpEntity entityEntry = entity.getEntity();
            // Buffer to hold the entity body
            StringBuilder entityBody = null;
            if(entityEntry.isRepeatable()) {
                entityBody = new StringBuilder(1000);
                // FIXME Charset
                try (InputStream in = entityEntry.getContent();
                        InputStream bounded = new BoundedInputStream(in, MAX_BODY_RETAIN_SIZE)) {
                    entityBody.append(IOUtils.toString(bounded));
                }
                if (entityEntry.getContentLength() > MAX_BODY_RETAIN_SIZE) {
                    entityBody.append("<actual file content shortened>");
                }
            }
            else { 
                entityBody = new StringBuilder(65);
                // this probably cannot happen
                entityBody.append("<RequestEntity was not repeatable, cannot view what was sent>");
            }
            return entityBody.toString();
        }
        return ""; // may be the empty string
    }

    /**
     * 
     * @return the value of {@link #getContentEncoding()}; forced to null if empty
     */
    private String getContentEncodingOrNull() {
        return getContentEncoding(null);
    }

    /**
     * @param dflt the default to be used
     * @return the value of {@link #getContentEncoding()}; default if null or empty
     */
    private String getContentEncoding(String dflt) {
        String ce = getContentEncoding();
        if (isNullOrEmptyTrimmed(ce)) {
            return dflt;
        } else {
            return ce;
        }
    }

    private void saveConnectionCookies(HttpResponse method, URL u, CookieManager cookieManager) {
        if (cookieManager != null) {
            Header[] hdrs = method.getHeaders(HTTPConstants.HEADER_SET_COOKIE);
            for (Header hdr : hdrs) {
                cookieManager.addCookieFromHeader(hdr.getValue(),u);
            }
        }
    }

    @Override
    protected void notifyFirstSampleAfterLoopRestart() {
        log.debug("notifyFirstSampleAfterLoopRestart");
        resetSSLContext = !USE_CACHED_SSL_CONTEXT;
    }

    @Override
    protected void threadFinished() {
        log.debug("Thread Finished");
        closeThreadLocalConnections();
    }

    /**
     * 
     */
    private void closeThreadLocalConnections() {
        // Does not need to be synchronised, as all access is from same thread
        Map<HttpClientKey, CloseableHttpClient> mapHttpClientPerHttpClientKey = HTTPCLIENTS_CACHE_PER_THREAD_AND_HTTPCLIENTKEY.get();
        if ( mapHttpClientPerHttpClientKey != null ) {
            for ( CloseableHttpClient cl : mapHttpClientPerHttpClientKey.values() ) {
                ((AbstractHttpClient) cl).clearRequestInterceptors(); 
                ((AbstractHttpClient) cl).clearResponseInterceptors();
                JOrphanUtils.closeQuietly(cl);
                cl.getConnectionManager().shutdown();
            }
            mapHttpClientPerHttpClientKey.clear();
        }
    }

    @Override
    public boolean interrupt() {
        HttpUriRequest request = currentRequest;
        if (request != null) {
            currentRequest = null; // don't try twice
            try {
                request.abort();
            } catch (UnsupportedOperationException e) {
                log.warn("Could not abort pending request", e);
            }
        }
        return request != null;
    }
    
}
