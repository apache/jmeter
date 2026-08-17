/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.http.sampler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;

import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.JsseSSLManager;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.io.CountingInputStream;
import org.apache.jorphan.util.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sampler which understands all the parts necessary to read statistics about
 * HTTP requests, including cookies and authentication.
 *
 */
public class HTTPJavaImpl extends HTTPAbstractImpl {
    protected static final class ConnectionSetup {
        private final HttpURLConnection connection;
        private final Map<String, String> securityHeaders;

        ConnectionSetup(HttpURLConnection connection, Map<String, String> securityHeaders) {
            this.connection = connection;
            this.securityHeaders = securityHeaders;
        }

        public HttpURLConnection getConnection() {
            return connection;
        }

        public Map<String, String> getSecurityHeaders() {
            return securityHeaders;
        }
    }

    private static final boolean OBEY_CONTENT_LENGTH =
        JMeterUtils.getPropDefault("httpsampler.obey_contentlength", false); // $NON-NLS-1$

    private static final String DEFAULT_HTTP_VERSION =
        JMeterUtils.getPropDefault("httpclient.version", HTTPConstants.HTTP_1_1); // $NON-NLS-1$

    /** Name of the {@code User-Agent} request header. */
    private static final String HEADER_USER_AGENT = "User-Agent"; // $NON-NLS-1$

    /**
     * {@code User-Agent} of {@link HttpURLConnection}, which is used for HTTP/1.1. JMeter adds it to the
     * request itself, so it shows up in the sample result and is accounted for in the sent bytes, instead of
     * being added invisibly by the JDK.
     */
    private static final String HTTP_1_DEFAULT_USER_AGENT = createHttp1DefaultUserAgent();

    /** {@code User-Agent} of {@link HttpClient}, which is used for HTTP/2. */
    private static final String HTTP_2_DEFAULT_USER_AGENT =
        "Java-http-client/" + System.getProperty("java.version"); // $NON-NLS-1$

    private static String createHttp1DefaultUserAgent() {
        String javaAgent = "Java/" + System.getProperty("java.version"); // $NON-NLS-1$
        String agent = System.getProperty("http.agent"); // $NON-NLS-1$
        return agent == null ? javaAgent : agent + " " + javaAgent;
    }

    private static final ThreadLocal<Map<HttpClientKey, Http2Client>> HTTP_2_CLIENTS =
        ThreadLocal.withInitial(ConcurrentHashMap::new);

    /**
     * HTTP/2 clients used when multiplexing is enabled. The JDK client sends every exchange of a client
     * over a single connection per origin, so one shared instance lets the requests of all JMeter threads
     * and of the threads which download embedded resources in parallel share a connection.
     */
    private static final Map<HttpClientKey, Http2Client> SHARED_HTTP_2_CLIENTS = new ConcurrentHashMap<>();

    private static final AtomicReference<ExecutorService> HTTP_2_EXECUTOR = new AtomicReference<>();

    private static final Logger log = LoggerFactory.getLogger(HTTPJavaImpl.class);

    // Available since Java 21; on older JVMs the client is released once it is no longer referenced.
    private static final Method HTTP_CLIENT_SHUTDOWN = findHttpClientShutdownMethod();

    /** Multiplex concurrent HTTP/2 message exchanges over a single connection per origin. */
    private static final boolean HTTP_2_MULTIPLEXING =
        JMeterUtils.getPropDefault("http.java.h2.multiplexing", true); // $NON-NLS-1$

    /**
     * Accept HTTP/2 server push. JMeter has no way of reporting pushed resources, so push is switched off
     * to avoid the server wasting bandwidth on responses that are dropped.
     */
    private static final String HTTP_2_PUSH_ENABLED_PROPERTY = "http.java.h2.push_enabled"; // $NON-NLS-1$

    /** System property the JDK client reads to decide whether it announces {@code SETTINGS_ENABLE_PUSH}. */
    private static final String JDK_PUSH_ENABLED_PROPERTY = "jdk.httpclient.enablepush"; // $NON-NLS-1$

    /**
     * Maps the JMeter HTTP/2 properties to the {@code jdk.httpclient.*} system properties, which are the only
     * way to configure the HTTP/2 protocol settings of the JDK client. They are read whenever a client is
     * built, so they have to be in place before the first HTTP/2 sample is taken.
     */
    private static final Map<String, String> HTTP_2_SYSTEM_PROPERTIES = createHttp2SystemPropertyMapping();

    static {
        applyHttp2SystemProperties(JMeterUtils.getJMeterProperties(), System.getProperties());
    }

    private static Map<String, String> createHttp2SystemPropertyMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        // Size of the HPACK dynamic header table announced to the server, 0 disables HPACK indexing
        mapping.put("http.java.h2.header_table_size", "jdk.httpclient.hpack.maxheadertablesize"); // $NON-NLS-1$ $NON-NLS-2$
        // Number of server initiated (pushed) streams the client accepts on a connection
        mapping.put("http.java.h2.max_concurrent_streams", "jdk.httpclient.maxstreams"); // $NON-NLS-1$ $NON-NLS-2$
        mapping.put("http.java.h2.initial_window_size", "jdk.httpclient.windowsize"); // $NON-NLS-1$ $NON-NLS-2$
        mapping.put("http.java.h2.connection_window_size", "jdk.httpclient.connectionWindowSize"); // $NON-NLS-1$ $NON-NLS-2$
        mapping.put("http.java.h2.max_frame_size", "jdk.httpclient.maxframesize"); // $NON-NLS-1$ $NON-NLS-2$
        mapping.put("http.java.h2.keep_alive_timeout", "jdk.httpclient.keepalive.timeout.h2"); // $NON-NLS-1$ $NON-NLS-2$
        return Collections.unmodifiableMap(mapping);
    }

    /**
     * Copies the HTTP/2 settings from the JMeter properties to the system properties of the JDK client.
     * Values which are already defined, for example on the command line, are never overwritten.
     *
     * @param jmeterProperties the JMeter properties, may be {@code null} when they have not been loaded
     * @param systemProperties the system properties to configure
     */
    static void applyHttp2SystemProperties(Properties jmeterProperties, Properties systemProperties) {
        Properties source = jmeterProperties != null ? jmeterProperties : new Properties();
        for (Map.Entry<String, String> entry : HTTP_2_SYSTEM_PROPERTIES.entrySet()) {
            String value = source.getProperty(entry.getKey());
            if (StringUtilities.isNotBlank(value)) {
                setUnlessDefined(systemProperties, entry.getValue(), value.trim());
            }
        }
        boolean pushEnabled = Boolean.parseBoolean(source.getProperty(HTTP_2_PUSH_ENABLED_PROPERTY, "false")); // $NON-NLS-1$
        setUnlessDefined(systemProperties, JDK_PUSH_ENABLED_PROPERTY, pushEnabled ? "1" : "0"); // $NON-NLS-1$ $NON-NLS-2$
    }

    private static void setUnlessDefined(Properties systemProperties, String name, String value) {
        String current = systemProperties.getProperty(name);
        if (current == null) {
            systemProperties.setProperty(name, value);
        } else if (!current.equals(value)) {
            log.info("Keeping system property {}={}, it takes precedence over the JMeter property", name, current); // $NON-NLS-1$
        }
    }

    static Map<HttpClientKey, Http2Client> getHttp2Clients() {
        return HTTP_2_MULTIPLEXING ? SHARED_HTTP_2_CLIENTS : HTTP_2_CLIENTS.get();
    }

    /** Returns the thread pool of the HTTP/2 clients, it is created when the first client needs it. */
    private static ExecutorService http2Executor() {
        ExecutorService executor = HTTP_2_EXECUTOR.get();
        if (executor != null) {
            return executor;
        }
        ExecutorService created = Executors.newCachedThreadPool(new Http2ThreadFactory());
        if (HTTP_2_EXECUTOR.compareAndSet(null, created)) {
            return created;
        }
        created.shutdownNow();
        return HTTP_2_EXECUTOR.get();
    }

    private static Method findHttpClientShutdownMethod() {
        for (String name : new String[] { "shutdownNow", "close" }) { // $NON-NLS-1$ $NON-NLS-2$
            try {
                return HttpClient.class.getMethod(name);
            } catch (NoSuchMethodException e) { // NOSONAR Java 17 cannot close an HttpClient explicitly
                log.debug("HttpClient has no {}() method", name); // $NON-NLS-1$
            }
        }
        return null;
    }

    /** Releases the resources of the client, in particular its selector thread and its connections. */
    private static void closeQuietly(Http2Client client) {
        if (client == null || HTTP_CLIENT_SHUTDOWN == null) {
            return;
        }
        try {
            HTTP_CLIENT_SHUTDOWN.invoke(client.httpClient);
        } catch (Exception e) { // NOSONAR closing must never fail a sample
            log.debug("Problem closing HTTP/2 HttpClient", e); // $NON-NLS-1$
        }
    }

    private static void closeHttp2Clients(Map<HttpClientKey, Http2Client> clients) {
        synchronized (clients) {
            for (Http2Client client : clients.values()) {
                closeQuietly(client);
            }
            clients.clear();
        }
    }

    @Override
    protected void threadFinished() {
        closeHttp2Clients(HTTP_2_CLIENTS.get());
        HTTP_2_CLIENTS.remove();
    }

    @Override
    protected void testEnded() {
        closeHttp2Clients(SHARED_HTTP_2_CLIENTS);
        ExecutorService executor = HTTP_2_EXECUTOR.getAndSet(null);
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * HTTP/2 does not transmit a reason phrase (see RFC 9113, section 8.3.2), so the response message is
     * derived from the status code. The phrases are the ones registered in the IANA HTTP status code registry.
     */
    private static final Map<Integer, String> HTTP_REASON_PHRASES = createReasonPhrases();

    private static Map<Integer, String> createReasonPhrases() {
        Map<Integer, String> phrases = new HashMap<>();
        phrases.put(100, "Continue"); // $NON-NLS-1$
        phrases.put(101, "Switching Protocols"); // $NON-NLS-1$
        phrases.put(102, "Processing"); // $NON-NLS-1$
        phrases.put(103, "Early Hints"); // $NON-NLS-1$
        phrases.put(200, "OK"); // $NON-NLS-1$
        phrases.put(201, "Created"); // $NON-NLS-1$
        phrases.put(202, "Accepted"); // $NON-NLS-1$
        phrases.put(203, "Non-Authoritative Information"); // $NON-NLS-1$
        phrases.put(204, "No Content"); // $NON-NLS-1$
        phrases.put(205, "Reset Content"); // $NON-NLS-1$
        phrases.put(206, "Partial Content"); // $NON-NLS-1$
        phrases.put(207, "Multi-Status"); // $NON-NLS-1$
        phrases.put(208, "Already Reported"); // $NON-NLS-1$
        phrases.put(226, "IM Used"); // $NON-NLS-1$
        phrases.put(300, "Multiple Choices"); // $NON-NLS-1$
        phrases.put(301, "Moved Permanently"); // $NON-NLS-1$
        phrases.put(302, "Found"); // $NON-NLS-1$
        phrases.put(303, "See Other"); // $NON-NLS-1$
        phrases.put(304, "Not Modified"); // $NON-NLS-1$
        phrases.put(305, "Use Proxy"); // $NON-NLS-1$
        phrases.put(307, "Temporary Redirect"); // $NON-NLS-1$
        phrases.put(308, "Permanent Redirect"); // $NON-NLS-1$
        phrases.put(400, "Bad Request"); // $NON-NLS-1$
        phrases.put(401, "Unauthorized"); // $NON-NLS-1$
        phrases.put(402, "Payment Required"); // $NON-NLS-1$
        phrases.put(403, "Forbidden"); // $NON-NLS-1$
        phrases.put(404, "Not Found"); // $NON-NLS-1$
        phrases.put(405, "Method Not Allowed"); // $NON-NLS-1$
        phrases.put(406, "Not Acceptable"); // $NON-NLS-1$
        phrases.put(407, "Proxy Authentication Required"); // $NON-NLS-1$
        phrases.put(408, "Request Timeout"); // $NON-NLS-1$
        phrases.put(409, "Conflict"); // $NON-NLS-1$
        phrases.put(410, "Gone"); // $NON-NLS-1$
        phrases.put(411, "Length Required"); // $NON-NLS-1$
        phrases.put(412, "Precondition Failed"); // $NON-NLS-1$
        phrases.put(413, "Content Too Large"); // $NON-NLS-1$
        phrases.put(414, "URI Too Long"); // $NON-NLS-1$
        phrases.put(415, "Unsupported Media Type"); // $NON-NLS-1$
        phrases.put(416, "Range Not Satisfiable"); // $NON-NLS-1$
        phrases.put(417, "Expectation Failed"); // $NON-NLS-1$
        phrases.put(421, "Misdirected Request"); // $NON-NLS-1$
        phrases.put(422, "Unprocessable Content"); // $NON-NLS-1$
        phrases.put(423, "Locked"); // $NON-NLS-1$
        phrases.put(424, "Failed Dependency"); // $NON-NLS-1$
        phrases.put(425, "Too Early"); // $NON-NLS-1$
        phrases.put(426, "Upgrade Required"); // $NON-NLS-1$
        phrases.put(428, "Precondition Required"); // $NON-NLS-1$
        phrases.put(429, "Too Many Requests"); // $NON-NLS-1$
        phrases.put(431, "Request Header Fields Too Large"); // $NON-NLS-1$
        phrases.put(451, "Unavailable For Legal Reasons"); // $NON-NLS-1$
        phrases.put(500, "Internal Server Error"); // $NON-NLS-1$
        phrases.put(501, "Not Implemented"); // $NON-NLS-1$
        phrases.put(502, "Bad Gateway"); // $NON-NLS-1$
        phrases.put(503, "Service Unavailable"); // $NON-NLS-1$
        phrases.put(504, "Gateway Timeout"); // $NON-NLS-1$
        phrases.put(505, "HTTP Version Not Supported"); // $NON-NLS-1$
        phrases.put(506, "Variant Also Negotiates"); // $NON-NLS-1$
        phrases.put(507, "Insufficient Storage"); // $NON-NLS-1$
        phrases.put(508, "Loop Detected"); // $NON-NLS-1$
        phrases.put(510, "Not Extended"); // $NON-NLS-1$
        phrases.put(511, "Network Authentication Required"); // $NON-NLS-1$
        return Collections.unmodifiableMap(phrases);
    }

    /**
     * Returns the reason phrase belonging to the given HTTP status code.
     *
     * @param statusCode the HTTP status code
     * @return the registered reason phrase, or an empty string if the status code is unknown
     */
    static String getReasonPhrase(int statusCode) {
        return HTTP_REASON_PHRASES.getOrDefault(statusCode, ""); // $NON-NLS-1$
    }

    static boolean isHttp2(String samplerHttpVersion, String defaultHttpVersion) {
        String httpVersion = StringUtilities.isBlank(samplerHttpVersion) ? defaultHttpVersion : samplerHttpVersion;
        // java.net.http.HttpClient always negotiates, so a strict HTTP/2 request is negotiated as well
        return HTTPConstants.HTTP_VERSION_2.equalsIgnoreCase(httpVersion)
                || HTTPConstants.HTTP_VERSION_2_STRICT.equalsIgnoreCase(httpVersion);
    }

    private boolean isHttp2() {
        return isHttp2(testElement.getHttpVersion(), DEFAULT_HTTP_VERSION);
    }

    private static final int MAX_CONN_RETRIES =
        JMeterUtils.getPropDefault("http.java.sampler.retries" // $NON-NLS-1$
                ,0); // Maximum connection retries

    static {
        log.info("Maximum connection retries = {}", MAX_CONN_RETRIES); // $NON-NLS-1$
    }

    private static final byte[] NULL_BA = new byte[0];// can share these

    /** Handles writing of a post or put request */
    private transient PostWriter postOrPutWriter;

    private volatile HttpURLConnection savedConn;
    private volatile CompletableFuture<HttpResponse<InputStream>> currentResponseFuture;

    protected HTTPJavaImpl(HTTPSamplerBase base) {
        super(base);
    }

    /**
     * Set request headers in preparation to opening a connection.
     *
     * @param conn
     *            <code>URLConnection</code> to set headers on
     * @exception IOException
     *                if an I/O exception occurs
     */
    protected void setPostHeaders(URLConnection conn) throws IOException {
        postOrPutWriter = new PostWriter();
        postOrPutWriter.setHeaders(conn, testElement);
    }

    private void setPutHeaders(URLConnection conn) throws IOException {
        postOrPutWriter = new PutWriter();
        postOrPutWriter.setHeaders(conn, testElement);
    }

    /**
     * Send POST data from <code>Entry</code> to the open connection.
     * This also handles sending data for PUT requests
     *
     * @param connection
     *            <code>URLConnection</code> where POST data should be sent
     * @return a String show what was posted. Will not contain actual file upload content
     * @exception IOException
     *                if an I/O exception occurs
     */
    protected String sendPostData(URLConnection connection) throws IOException {
        return postOrPutWriter.sendPostData(connection, testElement);
    }

    private String sendPutData(URLConnection connection) throws IOException {
        return postOrPutWriter.sendPostData(connection, testElement);
    }

    /**
     * Returns an <code>HttpURLConnection</code> fully ready to attempt
     * connection. This means it sets the request method (GET or POST), headers,
     * cookies, and authorization for the URL request.
     * <p>
     * The request infos are saved into the sample result if one is provided.
     *
     * @param u
     *            <code>URL</code> of the URL request
     * @param method
     *            GET, POST etc
     * @param res
     *            sample result to save request infos to
     * @return connection and security headers ready for .connect
     * @exception IOException
     *                if an I/O Exception occurs
     */
    protected ConnectionSetup setupConnection(URL u, String method, HTTPSampleResult res) throws IOException {
        SSLManager sslmgr = null;
        if (HTTPConstants.PROTOCOL_HTTPS.equalsIgnoreCase(u.getProtocol())) {
            try {
                sslmgr=SSLManager.getInstance(); // N.B. this needs to be done before opening the connection
            } catch (Exception e) {
                log.warn("Problem creating the SSLManager: ", e);
            }
        }

        final HttpURLConnection conn;
        final String proxyHost = getProxyHost();
        final int proxyPort = getProxyPortInt();
        if (!proxyHost.isEmpty() && proxyPort > 0){
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            //TODO - how to define proxy authentication for a single connection?
            conn = (HttpURLConnection) u.openConnection(proxy);
        } else {
            conn = (HttpURLConnection) u.openConnection();
        }

        // Update follow redirects setting just for this connection
        conn.setInstanceFollowRedirects(getAutoRedirects());

        int cto = getConnectTimeout();
        if (cto > 0){
            conn.setConnectTimeout(cto);
        }

        int rto = getResponseTimeout();
        if (rto > 0){
            conn.setReadTimeout(rto);
        }

        if (HTTPConstants.PROTOCOL_HTTPS.equalsIgnoreCase(u.getProtocol())) {
            try {
                if (null != sslmgr){
                    sslmgr.setContext(conn); // N.B. must be done after opening connection
                }
            } catch (Exception e) {
                log.warn("Problem setting the SSLManager for the connection: ", e);
            }
        }

        // a well-behaved browser is supposed to send 'Connection: close'
        // with the last request to an HTTP server. Instead, most browsers
        // leave it to the server to close the connection after their
        // timeout period. Leave it to the JMeter user to decide.
        // Ensure System property "sun.net.http.allowRestrictedHeaders=true" is set to true to allow headers
        // such as "Host" and "Connection" to be passed through.
        // See http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6996110
        if (getUseKeepAlive()) {
            conn.setRequestProperty(HTTPConstants.HEADER_CONNECTION, HTTPConstants.KEEP_ALIVE);
        } else {
            conn.setRequestProperty(HTTPConstants.HEADER_CONNECTION, HTTPConstants.CONNECTION_CLOSE);
        }

        conn.setRequestMethod(method);
        Map<String, String> securityHeaders = setConnectionHeaders(conn, u, getHeaderManager(), getCacheManager());
        String cookies = setConnectionCookie(conn, u, getCookieManager());

        setConnectionAuthorization(conn, u, getAuthManager(), securityHeaders);
        setDefaultUserAgent(conn, HTTP_1_DEFAULT_USER_AGENT);

        if (method.equals(HTTPConstants.POST)) {
            setPostHeaders(conn);
        } else if (method.equals(HTTPConstants.PUT)) {
            setPutHeaders(conn);
        }

        if (res != null) {
            res.setRequestHeaders(getAllHeadersExceptCookie(conn, securityHeaders));
            if (StringUtilities.isNotEmpty(cookies)) {
                res.setCookies(cookies);
            } else {
                // During recording Cookie Manager doesn't handle cookies
                res.setCookies(getOnlyCookieFromHeaders(conn, securityHeaders));

            }
        }

        return new ConnectionSetup(conn, securityHeaders);
    }

    /**
     * Reads the response from the URL connection.
     *
     * @param conn
     *            URL from which to read response
     * @param res
     *            {@link SampleResult} to read response into
     * @return response content
     * @exception IOException
     *                if an I/O exception occurs
     */
    protected byte[] readResponse(HttpURLConnection conn, SampleResult res) throws IOException {
        InputStream in;

        final long contentLength = conn.getContentLength();
        if ((contentLength == 0)
            && OBEY_CONTENT_LENGTH) {
            log.info("Content-Length: 0, not reading http-body");
            res.setResponseHeaders(getResponseHeaders(conn));
            res.latencyEnd();
            return NULL_BA;
        }

        // works OK even if ContentEncoding is null
        boolean gzipped = HTTPConstants.ENCODING_GZIP.equals(conn.getContentEncoding());

        CountingInputStream instream = null;
        try {
            instream = new CountingInputStream(conn.getInputStream());
            if (gzipped) {
                in = new GZIPInputStream(instream);
            } else {
                in = instream;
            }
        } catch (IOException e) {
            if (! (e.getCause() instanceof FileNotFoundException))
            {
                log.error("readResponse: {}", e.toString());
                Throwable cause = e.getCause();
                if (cause != null){
                    log.error("Cause: {}", cause.toString());
                    if(cause instanceof Error error) {
                        throw error;
                    }
                }
            }
            // Normal InputStream is not available
            InputStream errorStream = conn.getErrorStream();
            if (errorStream == null) {
                if(log.isInfoEnabled()) {
                    log.info("Error Response Code: {}, Server sent no Errorpage", conn.getResponseCode());
                }
                res.setResponseHeaders(getResponseHeaders(conn));
                res.latencyEnd();
                return NULL_BA;
            }

            if(log.isInfoEnabled()) {
                log.info("Error Response Code: {}", conn.getResponseCode());
            }

            if (gzipped) {
                in = new GZIPInputStream(errorStream);
            } else {
                in = errorStream;
            }
        } catch (Exception e) {
            log.error("readResponse: {}", e.toString());
            Throwable cause = e.getCause();
            if (cause != null){
                log.error("Cause: {}", cause.toString());
                if(cause instanceof Error error) {
                    throw error;
                }
            }
            in = conn.getErrorStream();
        }
        // N.B. this closes 'in'
        byte[] responseData = readResponse(res, in, contentLength);
        if (instream != null) {
            res.setBodySize(instream.getBytesRead());
            instream.close();
        }
        return responseData;
    }

    /**
     * Gets the ResponseHeaders from the URLConnection
     *
     * @param conn
     *            connection from which the headers are read
     * @return string containing the headers, one per line
     */
    protected String getResponseHeaders(HttpURLConnection conn) {
        StringBuilder headerBuf = new StringBuilder();
        headerBuf.append(conn.getHeaderField(0));// Leave header as is
        headerBuf.append("\n"); //$NON-NLS-1$

        String hfk;
        for (int i = 1; (hfk=conn.getHeaderFieldKey(i)) != null; i++) {
            headerBuf.append(hfk);
            headerBuf.append(": "); // $NON-NLS-1$
            headerBuf.append(conn.getHeaderField(i));
            headerBuf.append("\n"); // $NON-NLS-1$
        }
        return headerBuf.toString();
    }

    /**
     * Extracts all the required cookies for that particular URL request and
     * sets them in the <code>HttpURLConnection</code> passed in.
     *
     * @param conn
     *            <code>HttpUrlConnection</code> which represents the URL
     *            request
     * @param u
     *            <code>URL</code> of the URL request
     * @param cookieManager
     *            the <code>CookieManager</code> containing all the cookies
     *            for this <code>UrlConfig</code>
     */
    private static String setConnectionCookie(HttpURLConnection conn, URL u, CookieManager cookieManager) {
        String cookieHeader = null;
        if (cookieManager != null) {
            cookieHeader = cookieManager.getCookieHeaderForURL(u);
            if (cookieHeader != null) {
                conn.setRequestProperty(HTTPConstants.HEADER_COOKIE, cookieHeader);
            }
        }
        return cookieHeader;
    }

    private static boolean isSecurityHeader(String name) {
        return name != null && (HTTPConstants.HEADER_AUTHORIZATION.equalsIgnoreCase(name)
                || "Proxy-Authorization".equalsIgnoreCase(name));
    }

    /**
     * Extracts all the required headers for that particular URL request and
     * sets them in the <code>HttpURLConnection</code> passed in
     *
     * @param conn
     *            <code>HttpUrlConnection</code> which represents the URL
     *            request
     * @param u
     *            <code>URL</code> of the URL request
     * @param headerManager
     *            the <code>HeaderManager</code> containing all the cookies
     *            for this <code>UrlConfig</code>
     * @param cacheManager the CacheManager (may be null)
     * @return Map of security headers set from HeaderManager
     */
    private static Map<String, String> setConnectionHeaders(HttpURLConnection conn, URL u,
            HeaderManager headerManager, CacheManager cacheManager) {
        // Add all the headers from the HeaderManager
        Header[] arrayOfHeaders = null;
        Map<String, String> securityHeaders = new LinkedHashMap<>();
        if (headerManager != null) {
            CollectionProperty headers = headerManager.getHeaders();
            if (headers != null) {
                int i=0;
                arrayOfHeaders = new Header[headers.size()];
                for (JMeterProperty jMeterProperty : headers) {
                    Header header = (Header) jMeterProperty.getObjectValue();
                    String n = header.getName();
                    String v = header.getValue();
                    arrayOfHeaders[i++] = header;
                    conn.addRequestProperty(n, v);
                    if (isSecurityHeader(n)) {
                        securityHeaders.put(n, v);
                    }
                }
            }
        }
        if (cacheManager != null){
            cacheManager.setHeaders(conn, arrayOfHeaders, u);
        }
        return securityHeaders;
    }

    /**
     * Get only the Cookie headers for the <code>HttpURLConnection</code> passed in
     *
     * @param conn
     *            <code>HttpUrlConnection</code> which represents the URL
     *            request
     * @param securityHeaders Map of security Header
     * @return the headers as a string
     */
    private static String getOnlyCookieFromHeaders(HttpURLConnection conn, Map<String, String> securityHeaders) {
        String cookieHeader= getFromConnectionHeaders(conn, securityHeaders, ONLY_COOKIE, false).trim();
        if(!cookieHeader.isEmpty()) {
            return cookieHeader.substring(HTTPConstants.HEADER_COOKIE_IN_REQUEST.length()).trim();
        }
        return "";
    }

    /**
     * Get all the headers for the <code>HttpURLConnection</code> passed in
     *
     * @param conn
     *            <code>HttpUrlConnection</code> which represents the URL
     *            request
     * @param securityHeaders Map of security Header
     * @return the headers as a string
     */
    private static String getAllHeadersExceptCookie(HttpURLConnection conn, Map<String, String> securityHeaders) {
        return getFromConnectionHeaders(conn, securityHeaders, ALL_EXCEPT_COOKIE, true);
    }

    /**
     * Get all the headers for the <code>HttpURLConnection</code> passed in
     *
     * @param conn
     *            <code>HttpUrlConnection</code> which represents the URL
     *            request
     * @param securityHeaders Map of security Header
     * @param predicate {@link Predicate}
     * @return the headers as a string
     */
    private static String getFromConnectionHeaders(HttpURLConnection conn, Map<String, String> securityHeaders,
            Predicate<? super String> predicate, boolean addSecurityHeaders) {
        // Get all the request properties, which are the headers set on the connection
        StringBuilder hdrs = new StringBuilder(100);
        Map<String, List<String>> requestHeaders = conn.getRequestProperties();
        for(Map.Entry<String, List<String>> entry : requestHeaders.entrySet()) {
            String headerKey=entry.getKey();
            // Exclude the COOKIE header, since cookie is reported separately in the sample
            if(predicate.test(headerKey)) {
                // value is a List of Strings
                for (String value : entry.getValue()){
                    hdrs.append(headerKey);
                    hdrs.append(": "); // $NON-NLS-1$
                    hdrs.append(value);
                    hdrs.append("\n"); // $NON-NLS-1$
                }
            }
        }
        if(addSecurityHeaders) {
            for(Map.Entry<String, String> entry : securityHeaders.entrySet()) {
                if (!hasHeader(requestHeaders, entry.getKey())) {
                    hdrs.append(entry.getKey()).append(": ") // $NON-NLS-1$
                        .append(entry.getValue()).append("\n"); // $NON-NLS-1$
                }
            }
        }
        return hdrs.toString();
    }

    /**
     * Adds the {@code User-Agent} the JDK would add on its own, so it is visible in the sample result and
     * counted in the sent bytes. Nothing is added when the test plan defines a {@code User-Agent} itself.
     *
     * @param conn connection the header is added to
     * @param defaultUserAgent {@code User-Agent} used when the request has none
     */
    private static void setDefaultUserAgent(HttpURLConnection conn, String defaultUserAgent) {
        if (conn.getRequestProperty(HEADER_USER_AGENT) == null) {
            conn.setRequestProperty(HEADER_USER_AGENT, defaultUserAgent);
        }
    }

    /**
     * Extracts all the required authorization for that particular URL request
     * and sets it in the <code>HttpURLConnection</code> passed in.
     *
     * @param conn
     *            <code>HttpUrlConnection</code> which represents the URL
     *            request
     * @param u
     *            <code>URL</code> of the URL request
     * @param authManager
     *            the <code>AuthManager</code> containing all the cookies for
     *            this <code>UrlConfig</code>
     * @param securityHeaders
     *            Map to collect security headers
     */
    private static void setConnectionAuthorization(HttpURLConnection conn, URL u, AuthManager authManager, Map<String, String> securityHeaders) {
        if (authManager != null) {
            Authorization auth = authManager.getAuthForURL(u);
            if (auth != null) {
                String headerValue = auth.toBasicHeader();
                conn.setRequestProperty(HTTPConstants.HEADER_AUTHORIZATION, headerValue);
                // Java hides request properties so we have to
                // keep trace of it
                securityHeaders.put(HTTPConstants.HEADER_AUTHORIZATION, headerValue);
            }
        }
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
        if (isHttp2()) {
            return sampleHttp2(url, method, areFollowingRedirect, frameDepth);
        }
        HttpURLConnection conn = null;

        String urlStr = url.toString();
        if (log.isDebugEnabled()) {
            log.debug("Start : sample {}, method {}, followingRedirect {}, depth {}",
                    urlStr, method, areFollowingRedirect, frameDepth);
        }

        HTTPSampleResult res = new HTTPSampleResult();
        configureSampleLabel(res, url);
        res.setURL(url);
        res.setHTTPMethod(method);

        res.sampleStart(); // Count the retries as well in the time

        // Check cache for an entry with an Expires header in the future
        final CacheManager cacheManager = getCacheManager();
        if (cacheManager != null && HTTPConstants.GET.equalsIgnoreCase(method)) {
           if (cacheManager.inCache(url, getHeaders(getHeaderManager()))) {
               return updateSampleResultForResourceInCache(res);
           }
        }

        Map<String, List<String>> requestHeaders = null;
        Map<String, String> securityHeaders = Collections.emptyMap();
        byte[] postBodyBytes = null;

        try {
            // Sampling proper - establish the connection and read the response:
            // Repeatedly try to connect:
            int retry = -1;
            // Start with -1 so tries at least once, and retries at most MAX_CONN_RETRIES times
            for (; retry < MAX_CONN_RETRIES; retry++) {
                try {
                    ConnectionSetup connectionSetup = setupConnection(url, method, res);
                    conn = connectionSetup.getConnection();
                    requestHeaders = new LinkedHashMap<>(conn.getRequestProperties());
                    securityHeaders = connectionSetup.getSecurityHeaders();
                    // Attempt the connection:
                    savedConn = conn;
                    conn.connect();
                    res.connectEnd();
                    break;
                } catch (BindException e) {
                    if (retry >= MAX_CONN_RETRIES) {
                        log.error("Can't connect after {} retries, message: {}", retry, e.toString());
                        throw e;
                    }
                    log.debug("Bind exception, try again");
                    if (conn!=null) {
                        savedConn = null; // we don't want interrupt to try disconnection again
                        conn.disconnect();
                    }
                    setUseKeepAlive(false);
                } catch (IOException e) {
                    log.debug("Connection failed, giving up");
                    throw e;
                }
            }
            if (retry > MAX_CONN_RETRIES) {
                // This should never happen, but...
                throw new BindException();
            }
            // Nice, we've got a connection. Finish sending the request:
            if (method.equals(HTTPConstants.POST)) {
                String postBody = sendPostData(conn);
                res.setQueryString(postBody);
                if (postBody != null) {
                    postBodyBytes = getBytes(postBody);
                }
            } else if (method.equals(HTTPConstants.PUT)) {
                String putBody = sendPutData(conn);
                res.setQueryString(putBody);
                if (putBody != null) {
                    postBodyBytes = getBytes(putBody);
                }
            }
            // Request sent. Now get the response:
            byte[] responseData = readResponse(conn, res);

            res.sampleEnd();
            // Done with the sampling proper.

            // Now collect the results into the HTTPSampleResult:

            res.setResponseData(responseData);

            int errorLevel = conn.getResponseCode();
            String respMsg = conn.getResponseMessage();
            String hdr=conn.getHeaderField(0);
            if (hdr == null) {
                hdr="(null)";  // $NON-NLS-1$
            }
            if (errorLevel == -1){// Bug 38902 - sometimes -1 seems to be returned unnecessarily
                if (respMsg != null) {// Bug 41902 - NPE
                    try {
                        errorLevel = Integer.parseInt(respMsg.substring(0, 3));
                        log.warn("ResponseCode==-1; parsed {} as {}", respMsg, errorLevel);
                      } catch (NumberFormatException e) {
                        log.warn("ResponseCode==-1; could not parse {} hdr: {}", respMsg, hdr);
                      }
                } else {
                    respMsg=hdr; // for result
                    log.warn("ResponseCode==-1 & null ResponseMessage. Header(0)= {} ", hdr);
                }
            }
            if (errorLevel == -1) {
                res.setResponseCode("(null)"); // $NON-NLS-1$
            } else {
                res.setResponseCode(Integer.toString(errorLevel));
            }
            res.setSuccessful(isSuccessCode(errorLevel));

            if (respMsg == null) {// has been seen in a redirect
                respMsg=hdr; // use header (if possible) if no message found
            }
            res.setResponseMessage(respMsg);

            String ct = conn.getContentType();
            if (ct != null){
                res.setContentType(ct);// e.g. text/html; charset=ISO-8859-1
                res.setEncodingAndType(ct);
            }

            String responseHeaders = getResponseHeaders(conn);
            res.setResponseHeaders(responseHeaders);
            if (res.isRedirect()) {
                res.setRedirectLocation(conn.getHeaderField(HTTPConstants.HEADER_LOCATION));
            }

            // record headers size to allow HTTPSampleResult.getBytes() with different options
            // It used to be responseHeaders.replaceAll("\n", "\r\n").length(),
            // however we don't need the resulting string, just the length
            // So we add the number of \n in the string to account for \n
            res.setHeadersSize(
                    responseHeaders.length()
                            + StringUtilities.count(responseHeaders, '\n')
                            + 2); // add 2 for a '\r\n' at end of headers (before data)
            if (log.isDebugEnabled()) {
                log.debug("Response headersSize={}, bodySize={}, Total={}",
                        res.getHeadersSize(),  res.getBodySizeAsLong(),
                        res.getHeadersSize() + res.getBodySizeAsLong());
            }

            // If we redirected automatically, the URL may have changed
            if (getAutoRedirects()){
                res.setURL(conn.getURL());
            }

            // Store any cookies received in the cookie manager:
            saveConnectionCookies(conn, url, getCookieManager());

            // Save cache information
            if (cacheManager != null){
                cacheManager.saveDetails(conn, res);
            }

            res.setSentBytes(calculateSentBytes(url, method, testElement.getHttpVersion(), requestHeaders, securityHeaders, postBodyBytes));

            res = resultProcessing(areFollowingRedirect, frameDepth, res);

            log.debug("End : sample");
            return res;
        } catch (IOException e) {
            if (res.getEndTime() == 0) {
                res.sampleEnd();
            }
            res.setSentBytes(calculateSentBytes(url, method, testElement.getHttpVersion(), requestHeaders, securityHeaders, postBodyBytes));
            savedConn = null; // we don't want interrupt to try disconnection again
            // We don't want to continue using this connection, even if KeepAlive is set
            if (conn != null) { // May not exist
                conn.disconnect();
            }
            conn=null; // Don't process again
            return errorResult(e, res);
        } finally {
            // calling disconnect doesn't close the connection immediately,
            // but indicates we're through with it. The JVM should close
            // it when necessary.
            savedConn = null; // we don't want interrupt to try disconnection again
            disconnect(conn); // Disconnect unless using KeepAlive
        }
    }

    private static Header[] getHeaders(HeaderManager headerManager) {
        if (headerManager != null) {
            final CollectionProperty headers = headerManager.getHeaders();
            if (headers != null) {
                final List<Header> allHeaders = new ArrayList<>(headers.size());
                for (final JMeterProperty jMeterProperty : headers) {
                    allHeaders.add((Header) jMeterProperty.getObjectValue());
                }
                return allHeaders.toArray(new Header[allHeaders.size()]);
            }
        }
        return new Header[0];
    }

    protected void disconnect(HttpURLConnection conn) {
        if (conn != null) {
            String connection = conn.getHeaderField(HTTPConstants.HEADER_CONNECTION);
            String protocol = conn.getHeaderField(0);
            if ((connection == null && (protocol == null || !protocol.startsWith(HTTPConstants.HTTP_1_1)))
                    || (connection != null && connection.equalsIgnoreCase(HTTPConstants.CONNECTION_CLOSE))) {
                conn.disconnect();
            } // TODO ? perhaps note connection so it can be disconnected at end of test?
        }
    }

    /**
     * From the <code>HttpURLConnection</code>, store all the "set-cookie"
     * key-pair values in the cookieManager of the <code>UrlConfig</code>.
     *
     * @param conn
     *            <code>HttpUrlConnection</code> which represents the URL
     *            request
     * @param u
     *            <code>URL</code> of the URL request
     * @param cookieManager
     *            the <code>CookieManager</code> containing all the cookies
     *            for this <code>UrlConfig</code>
     */
    private static void saveConnectionCookies(HttpURLConnection conn, URL u, CookieManager cookieManager) {
        if (cookieManager != null) {
            for (int i = 1; conn.getHeaderFieldKey(i) != null; i++) {
                if (conn.getHeaderFieldKey(i).equalsIgnoreCase(HTTPConstants.HEADER_SET_COOKIE)) {
                    cookieManager.addCookieFromHeader(conn.getHeaderField(i), u);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean interrupt() {
        HttpURLConnection conn = savedConn;
        CompletableFuture<HttpResponse<InputStream>> responseFuture = currentResponseFuture;
        savedConn = null;
        currentResponseFuture = null;
        if (conn != null) {
            conn.disconnect();
        }
        if (responseFuture != null) {
            responseFuture.cancel(true);
        }
        return conn != null || responseFuture != null;
    }

    private HTTPSampleResult sampleHttp2(URL url, String method, boolean areFollowingRedirect, int frameDepth) {
        if (log.isDebugEnabled()) {
            log.debug("Start : sampleHttp2 {}, method {}, followingRedirect {}, depth {}",
                    url, method, areFollowingRedirect, frameDepth);
        }
        HTTPSampleResult res = new HTTPSampleResult();
        configureSampleLabel(res, url);
        res.setURL(url);
        res.setHTTPMethod(method);
        res.sampleStart();
        final CacheManager cacheManager = getCacheManager();
        if (cacheManager != null && HTTPConstants.GET.equalsIgnoreCase(method)) {
            if (cacheManager.inCache(url, getHeaders(getHeaderManager()))) {
                return updateSampleResultForResourceInCache(res);
            }
        }
        Http2CapturingHttpURLConnection capturingConn = null;
        Path requestBody = null;
        byte[] postBodyBytes = null;
        long requestBodyLength = -1;
        Map<String, String> securityHeaders = Collections.emptyMap();
        try {
            capturingConn = new Http2CapturingHttpURLConnection(url, method);
            securityHeaders = setConnectionHeaders(capturingConn, url, getHeaderManager(), getCacheManager());
            String cookies = setConnectionCookie(capturingConn, url, getCookieManager());
            setConnectionAuthorization(capturingConn, url, getAuthManager(), securityHeaders);
            setDefaultUserAgent(capturingConn, HTTP_2_DEFAULT_USER_AGENT);
            if (method.equals(HTTPConstants.POST)) {
                setPostHeaders(capturingConn);
                String postBody = sendPostData(capturingConn);
                res.setQueryString(postBody);
            } else if (method.equals(HTTPConstants.PUT)) {
                setPutHeaders(capturingConn);
                String putBody = sendPutData(capturingConn);
                res.setQueryString(putBody);
            }
            capturingConn.finishCapture();
            requestBodyLength = capturingConn.getCapturedBodyLength();
            if (capturingConn.isSpilled()) {
                requestBody = capturingConn.getCapturedBody();
            } else {
                postBodyBytes = capturingConn.getCapturedByteArray();
            }
            res.setRequestHeaders(getAllHeadersExceptCookie(capturingConn, securityHeaders));
            if (StringUtilities.isNotEmpty(cookies)) {
                res.setCookies(cookies);
            } else {
                res.setCookies(getOnlyCookieFromHeaders(capturingConn, securityHeaders));
            }
            URI uri = url.toURI();
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder(uri);
            if (method.equalsIgnoreCase(HTTPConstants.POST) || method.equalsIgnoreCase(HTTPConstants.PUT)
                    || method.equalsIgnoreCase(HTTPConstants.PATCH)) {
                HttpRequest.BodyPublisher publisher;
                if (requestBody != null) {
                    publisher = HttpRequest.BodyPublishers.ofFile(requestBody);
                } else if (postBodyBytes != null) {
                    publisher = HttpRequest.BodyPublishers.ofByteArray(postBodyBytes);
                } else {
                    publisher = HttpRequest.BodyPublishers.noBody();
                }
                reqBuilder.method(method, publisher);
            } else if (method.equalsIgnoreCase(HTTPConstants.GET)) {
                reqBuilder.GET();
            } else if (method.equalsIgnoreCase(HTTPConstants.DELETE)) {
                reqBuilder.DELETE();
            } else {
                reqBuilder.method(method, HttpRequest.BodyPublishers.noBody());
            }
            int rto = getResponseTimeout();
            if (rto > 0) {
                reqBuilder.timeout(Duration.ofMillis(rto));
            }
            Map<String, List<String>> props = capturingConn.getRequestProperties();
            for (Map.Entry<String, List<String>> entry : props.entrySet()) {
                String headerName = entry.getKey();
                if (headerName == null || isRestrictedHeader(headerName)) {
                    continue;
                }
                for (String value : entry.getValue()) {
                    reqBuilder.header(headerName, value);
                }
            }
            Http2Client client = getHttpClient(url);
            HttpRequest httpRequest = reqBuilder.build();
            HttpResponse<InputStream> response;
            ConnectTimeTracker connectTimeTracker = client.connectTimeTracker;
            connectTimeTracker.sampleStarted(res, ConnectTimeTracker.origin(url));
            try {
                CompletableFuture<HttpResponse<InputStream>> responseFuture =
                        client.httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
                currentResponseFuture = responseFuture;
                try {
                    response = responseFuture.get();
                } catch (InterruptedException e) {
                    responseFuture.cancel(true);
                    Thread.currentThread().interrupt();
                    throw e;
                } finally {
                    currentResponseFuture = null;
                }
            } finally {
                connectTimeTracker.sampleFinished(res);
            }
            res.latencyEnd();

            byte[] responseData = readResponse(response, res);

            res.sampleEnd();

            res.setResponseData(responseData);
            int statusCode = response.statusCode();
            res.setResponseCode(Integer.toString(statusCode));
            res.setSuccessful(isSuccessCode(statusCode));
            res.setResponseMessage(getReasonPhrase(statusCode));
            String responseHeaders = getResponseHeaders(response);
            res.setResponseHeaders(responseHeaders);
            String ct = response.headers().firstValue(HTTPConstants.HEADER_CONTENT_TYPE).orElse(null);
            if (ct != null) {
                res.setContentType(ct);
                res.setEncodingAndType(ct);
            }
            if (res.isRedirect()) {
                String location = response.headers().firstValue(HTTPConstants.HEADER_LOCATION).orElse(null);
                if (location != null) {
                    res.setRedirectLocation(location);
                }
            }

            res.setHeadersSize(
                    responseHeaders.length()
                            + StringUtilities.count(responseHeaders, '\n')
                            + 2);

            if (getAutoRedirects()) {
                res.setURL(response.uri().toURL());
            }

            saveConnectionCookies(response, url, getCookieManager());

            if (cacheManager != null) {
                cacheManager.saveDetails(response, res);
            }

            res.setSentBytes(calculateSentBytes(url, method, HTTPConstants.HTTP_2,
                    capturingConn != null ? capturingConn.getRequestProperties() : null,
                    securityHeaders, requestBodyLength));

            res = resultProcessing(areFollowingRedirect, frameDepth, res);

            log.debug("End : sampleHttp2");
            return res;
        } catch (Exception e) {
            if (res.getEndTime() == 0) {
                res.sampleEnd();
            }
            res.setSentBytes(calculateSentBytes(url, method, HTTPConstants.HTTP_2,
                    capturingConn != null ? capturingConn.getRequestProperties() : null,
                    securityHeaders, requestBodyLength));
            return errorResult(e, res);
        } finally {
            if (capturingConn != null) {
                capturingConn.deleteCapturedBody();
            }
        }
    }

    private byte[] readResponse(HttpResponse<InputStream> response, SampleResult res) throws IOException {
        InputStream in = response.body();
        if (in == null) {
            return NULL_BA;
        }

        boolean gzipped = response.headers().firstValue(HTTPConstants.HEADER_CONTENT_ENCODING)
                .map(HTTPConstants.ENCODING_GZIP::equalsIgnoreCase)
                .orElse(false);

        long contentLength = response.headers().firstValueAsLong(HTTPConstants.HEADER_CONTENT_LENGTH).orElse(-1L);

        if (contentLength == 0 && OBEY_CONTENT_LENGTH) {
            log.info("Content-Length: 0, not reading http-body");
            res.setResponseHeaders(getResponseHeaders(response));
            res.latencyEnd();
            return NULL_BA;
        }

        CountingInputStream instream = new CountingInputStream(in);
        InputStream stream = gzipped ? new GZIPInputStream(instream) : instream;

        try {
            byte[] responseData = readResponse(res, stream, contentLength);
            res.setBodySize(instream.getBytesRead());
            return responseData;
        } finally {
            instream.close();
        }
    }

    private static String getResponseHeaders(HttpResponse<?> response) {
        StringBuilder headerBuf = new StringBuilder();
        String versionStr = (response.version() == HttpClient.Version.HTTP_2)
                ? HTTPConstants.HTTP_2 : HTTPConstants.HTTP_1_1;
        headerBuf.append(versionStr).append(" ").append(response.statusCode()).append("\n"); // $NON-NLS-1$ $NON-NLS-2$

        response.headers().map().forEach((key, values) -> {
            if (key != null) {
                for (String val : values) {
                    headerBuf.append(key).append(": ").append(val).append("\n"); // $NON-NLS-1$ $NON-NLS-2$
                }
            }
        });
        return headerBuf.toString();
    }

    private static void saveConnectionCookies(HttpResponse<?> response, URL u, CookieManager cookieManager) {
        if (cookieManager != null) {
            List<String> setCookies = response.headers().allValues(HTTPConstants.HEADER_SET_COOKIE);
            for (String setCookie : setCookies) {
                cookieManager.addCookieFromHeader(setCookie, u);
            }
        }
    }

    private static boolean isRestrictedHeader(String name) {
        return HTTPConstants.HEADER_CONNECTION.equalsIgnoreCase(name)
                || HTTPConstants.HEADER_CONTENT_LENGTH.equalsIgnoreCase(name)
                || "Host".equalsIgnoreCase(name) // $NON-NLS-1$
                || "Expect".equalsIgnoreCase(name) // $NON-NLS-1$
                || "Upgrade".equalsIgnoreCase(name); // $NON-NLS-1$
    }

    private static long calculateSentBytes(
            URL u,
            String method,
            String version,
            Map<String, List<String>> requestHeaders,
            Map<String, String> securityHeaders,
            byte[] postBodyBytes) {
        return calculateSentBytes(u, method, version, requestHeaders, securityHeaders,
                postBodyBytes == null ? -1 : postBodyBytes.length);
    }

    private static long calculateSentBytes(
            URL u,
            String method,
            String version,
            Map<String, List<String>> requestHeaders,
            Map<String, String> securityHeaders,
            long postBodyLength) {
        long sentBytes = 0;

        if (StringUtilities.isBlank(method)) {
            method = HTTPConstants.GET;
        }

        String uri = u != null ? u.getFile() : "";
        if (StringUtilities.isBlank(uri)) {
            uri = "/"; // $NON-NLS-1$
        }

        if (StringUtilities.isBlank(version)) {
            version = HTTPConstants.HTTP_1_1;
        }

        // Request line: METHOD URI VERSION\r\n
        sentBytes += method.getBytes(StandardCharsets.UTF_8).length;
        sentBytes += 1;
        sentBytes += uri.getBytes(StandardCharsets.UTF_8).length;
        sentBytes += 1;
        sentBytes += version.getBytes(StandardCharsets.UTF_8).length;
        sentBytes += 2;

        // Request headers
        if (requestHeaders != null) {
            for (Map.Entry<String, List<String>> entry : requestHeaders.entrySet()) {
                String key = entry.getKey();
                if (key != null) {
                    List<String> values = entry.getValue();
                    if (values != null) {
                        for (String val : values) {
                            sentBytes += key.getBytes(StandardCharsets.UTF_8).length;
                            sentBytes += 2; // ": "
                            if (val != null) {
                                sentBytes += val.getBytes(StandardCharsets.UTF_8).length;
                            }
                            sentBytes += 2; // "\r\n"
                        }
                    }
                }
            }
        }

        if (securityHeaders != null && !securityHeaders.isEmpty()) {
            for (Map.Entry<String, String> secEntry : securityHeaders.entrySet()) {
                String secKey = secEntry.getKey();
                String secVal = secEntry.getValue();
                if (secKey != null && !hasHeader(requestHeaders, secKey)) {
                    sentBytes += secKey.getBytes(StandardCharsets.UTF_8).length;
                    sentBytes += 2; // ": "
                    if (secVal != null) {
                        sentBytes += secVal.getBytes(StandardCharsets.UTF_8).length;
                    }
                    sentBytes += 2; // "\r\n"
                }
            }
        }

        // Header/Body separator \r\n
        sentBytes += 2;

        // Request body
        if (postBodyLength >= 0) {
            sentBytes += postBodyLength;
        } else if (requestHeaders != null) {
            String contentLengthStr = getHeaderValue(requestHeaders, HTTPConstants.HEADER_CONTENT_LENGTH);
            if (StringUtilities.isNotEmpty(contentLengthStr)) {
                try {
                    sentBytes += Long.parseLong(contentLengthStr);
                } catch (NumberFormatException e) {
                    log.debug("Could not parse Content-Length header: {}", contentLengthStr, e);
                }
            }
        }

        return sentBytes;
    }

    private static boolean hasHeader(Map<String, List<String>> headers, String headerName) {
        if (headers == null || headerName == null) {
            return false;
        }
        for (String key : headers.keySet()) {
            if (headerName.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    private static String getHeaderValue(Map<String, List<String>> headers, String headerName) {
        if (headers == null || headerName == null) {
            return null;
        }
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (headerName.equalsIgnoreCase(entry.getKey())) {
                List<String> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    return values.get(0);
                }
            }
        }
        return null;
    }

    private byte[] getBytes(String postBody) {
        String enc = testElement != null ? testElement.getContentEncoding() : null;
        if (StringUtilities.isBlank(enc)) {
            enc = StandardCharsets.UTF_8.name();
        }
        try {
            return postBody.getBytes(enc);
        } catch (Exception e) {
            return postBody.getBytes(StandardCharsets.UTF_8);
        }
    }

    Http2Client getHttpClient(URL url) {
        int connectTimeout = getConnectTimeout();
        String proxyHost = getProxyHost();
        int proxyPort = getProxyPortInt();
        String proxyUser = getProxyUser();
        String proxyPass = getProxyPass();
        boolean autoRedirects = getAutoRedirects();
        SSLContext sslContext = null;

        if (HTTPConstants.PROTOCOL_HTTPS.equalsIgnoreCase(url.getProtocol())) {
            try {
                SSLManager sslmgr = SSLManager.getInstance();
                if (sslmgr instanceof JsseSSLManager jsseSSLManager) {
                    sslContext = jsseSSLManager.getContext();
                }
            } catch (Exception e) {
                log.warn("Problem getting SSLContext for HTTP/2 HttpClient: ", e); // $NON-NLS-1$
            }
            if (sslContext == null) {
                // Use the default context explicitly, so the connect time of the TLS handshake can be measured
                try {
                    sslContext = SSLContext.getDefault();
                } catch (NoSuchAlgorithmException e) {
                    log.warn("Problem getting default SSLContext for HTTP/2 HttpClient: ", e); // $NON-NLS-1$
                }
            }
        }

        HttpClientKey key = new HttpClientKey(connectTimeout, proxyHost, proxyPort,
                proxyUser, proxyPass, autoRedirects, sslContext != null);

        Map<HttpClientKey, Http2Client> clients = getHttp2Clients();
        Http2Client client = clients.get(key);
        if (client != null && (HTTP_2_MULTIPLEXING || client.usesSslContext(sslContext))) {
            // The SSLContext must not be part of the key, it does not implement equals(), and JMeter hands out
            // a new instance per thread and after every reset, which would make the cache grow without bound.
            // When the clients are shared, the SSLContext of the first caller is kept, so all threads keep on
            // multiplexing their exchanges over the same connection.
            return client;
        }
        synchronized (clients) {
            client = clients.get(key);
            if (client != null && (HTTP_2_MULTIPLEXING || client.usesSslContext(sslContext))) {
                return client;
            }
            Http2Client created = createHttpClient(key, sslContext);
            closeQuietly(clients.put(key, created));
            return created;
        }
    }

    private static Http2Client createHttpClient(HttpClientKey key, SSLContext sslContext) {
        ConnectTimeTracker connectTimeTracker = new ConnectTimeTracker();
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .executor(http2Executor())
                .followRedirects(key.autoRedirects ? HttpClient.Redirect.NORMAL : HttpClient.Redirect.NEVER);

        if (key.connectTimeout > 0) {
            builder.connectTimeout(Duration.ofMillis(key.connectTimeout));
        }

        if (StringUtilities.isNotEmpty(key.proxyHost) && key.proxyPort > 0) {
            builder.proxy(ProxySelector.of(new InetSocketAddress(key.proxyHost, key.proxyPort)));
            if (StringUtilities.isNotEmpty(key.proxyUser)) {
                builder.authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        if (getRequestorType() == RequestorType.PROXY) {
                            return new PasswordAuthentication(key.proxyUser,
                                    key.proxyPass != null ? key.proxyPass.toCharArray() : new char[0]);
                        }
                        return super.getPasswordAuthentication();
                    }
                });
            }
        }

        if (sslContext != null) {
            builder.sslContext(new ConnectTimeTracker.MeasuringSSLContext(sslContext, connectTimeTracker));
        }

        return new Http2Client(builder.build(), connectTimeTracker, sslContext);
    }

    /**
     * Holder for an HTTP/2 {@link HttpClient} and the tracker which measures the connect time of its connections.
     */
    static final class Http2Client {
        private final HttpClient httpClient;
        private final ConnectTimeTracker connectTimeTracker;
        private final SSLContext sslContext;

        Http2Client(HttpClient httpClient, ConnectTimeTracker connectTimeTracker, SSLContext sslContext) {
            this.httpClient = httpClient;
            this.connectTimeTracker = connectTimeTracker;
            this.sslContext = sslContext;
        }

        boolean usesSslContext(SSLContext other) {
            return sslContext == other;
        }
    }

    private static final class Http2ThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "JMeter-HTTP2-" + counter.incrementAndGet()); // $NON-NLS-1$
            thread.setDaemon(true);
            return thread;
        }
    }

    private static class HttpClientKey {
        private final int connectTimeout;
        private final String proxyHost;
        private final int proxyPort;
        private final String proxyUser;
        private final String proxyPass;
        private final boolean autoRedirects;
        private final boolean secure;

        HttpClientKey(int connectTimeout, String proxyHost, int proxyPort,
                      String proxyUser, String proxyPass, boolean autoRedirects,
                      boolean secure) {
            this.connectTimeout = connectTimeout;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
            this.proxyUser = proxyUser;
            this.proxyPass = proxyPass;
            this.autoRedirects = autoRedirects;
            this.secure = secure;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof HttpClientKey that)) {
                return false;
            }
            return connectTimeout == that.connectTimeout
                    && proxyPort == that.proxyPort
                    && autoRedirects == that.autoRedirects
                    && secure == that.secure
                    && Objects.equals(proxyHost, that.proxyHost)
                    && Objects.equals(proxyUser, that.proxyUser)
                    && Objects.equals(proxyPass, that.proxyPass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(connectTimeout, proxyHost, proxyPort, proxyUser, proxyPass, autoRedirects, secure);
        }
    }

}
