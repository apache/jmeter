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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.entity.BrotliInputStreamFactory;
import org.apache.hc.client5.http.entity.DecompressingEntity;
import org.apache.hc.client5.http.entity.DeflateInputStreamFactory;
import org.apache.hc.client5.http.entity.GZIPInputStreamFactory;
import org.apache.hc.client5.http.entity.InputStreamFactory;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultRoutePlanner;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.client5.http.nio.AsyncConnectionEndpoint;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.message.BasicHeaderValueParser;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.DNSCacheManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JsseSSLManager;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.jorphan.util.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP Sampler using Apache HttpClient 5.x.
 */
public class HTTPHC5Impl extends HTTPHCAbstractImpl {

    private static final Logger log = LoggerFactory.getLogger(HTTPHC5Impl.class);

    /** Key used to store the current {@link SampleResult} in the {@link HttpClientContext}. */
    static final String CONTEXT_ATTRIBUTE_SAMPLER_RESULT = "__jmeter.S_R__"; //$NON-NLS-1$

    private static final ThreadLocal<Map<HttpClientKey, CloseableHttpClient>> HTTP_CLIENTS =
            ThreadLocal.withInitial(HashMap::new);

    private static final ThreadLocal<Map<HttpClientKey, CloseableHttpAsyncClient>> HTTP_2_CLIENTS =
            ThreadLocal.withInitial(HashMap::new);

    private static final String[] HEADERS_TO_SAVE = {HttpHeaders.CONTENT_LENGTH, HttpHeaders.CONTENT_ENCODING,
            HttpHeaders.CONTENT_MD5};

    private static final Lookup<InputStreamFactory> CONTENT_DECODERS = RegistryBuilder.<InputStreamFactory>create()
            .register("br", BrotliInputStreamFactory.getInstance())
            .register("gzip", GZIPInputStreamFactory.getInstance())
            .register("x-gzip", GZIPInputStreamFactory.getInstance())
            .register("deflate", DeflateInputStreamFactory.getInstance())
            .build();

    private static final TlsStrategy HTTP_2_TLS_STRATEGY = createHttp2TlsStrategy();

    private static final ExecChainHandler RESPONSE_CONTENT_ENCODING = (request, scope, chain) -> {
        HttpClientContext context = scope.clientContext;
        RequestConfig requestConfig = context.getRequestConfig();
        if (requestConfig == null) {
            requestConfig = RequestConfig.DEFAULT;
        }
        ClassicHttpResponse response = chain.proceed(request, scope);
        HttpEntity entity = response.getEntity();
        if (!requestConfig.isContentCompressionEnabled() || entity == null || entity.getContentLength() == 0
                || entity.getContentEncoding() == null) {
            return response;
        }

        Header[][] headersToSave = new Header[HEADERS_TO_SAVE.length][];
        for (int i = 0; i < HEADERS_TO_SAVE.length; i++) {
            headersToSave[i] = response.getHeaders(HEADERS_TO_SAVE[i]);
        }
        String contentEncoding = entity.getContentEncoding();
        HeaderElement[] codecs = BasicHeaderValueParser.INSTANCE.parseElements(contentEncoding,
                new ParserCursor(0, contentEncoding.length()));
        for (HeaderElement codec : codecs) {
            InputStreamFactory decoderFactory = CONTENT_DECODERS.lookup(codec.getName().toLowerCase(Locale.ROOT));
            if (decoderFactory != null) {
                response.setEntity(new DecompressingEntity(response.getEntity(), decoderFactory));
                response.removeHeaders(HttpHeaders.CONTENT_LENGTH);
                response.removeHeaders(HttpHeaders.CONTENT_ENCODING);
                response.removeHeaders(HttpHeaders.CONTENT_MD5);
            }
        }
        for (Header[] headers : headersToSave) {
            for (Header header : headers) {
                if (!response.containsHeader(header.getName())) {
                    response.addHeader(header);
                }
            }
        }
        return response;
    };

    private volatile org.apache.hc.client5.http.classic.methods.HttpUriRequestBase currentRequest;

    @SuppressWarnings("deprecation") // buildAsync is unavailable before HttpClient 5.5
    private static TlsStrategy createHttp2TlsStrategy() {
        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
            return ClientTlsStrategyBuilder.create()
                    .setSslContext(sslContext)
                    .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not create HTTP/2 TLS strategy", e);
        }
    }

    protected HTTPHC5Impl(HTTPSamplerBase testElement) {
        super(testElement);
    }

    @Override
    protected HTTPSampleResult sample(URL url, String method, boolean areFollowingRedirect, int frameDepth) {
        HTTPSampleResult result = createSampleResult(url, method);
        org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request = null;
        ClassicHttpResponse response = null;
        try {
            resetStateIfNeeded();
            request = createRequest(url.toURI(), method);
            setupRequest(url, request, result, areFollowingRedirect);
            result.sampleStart();

            CacheManager cacheManager = getCacheManager();
            if (cacheManager != null && HTTPConstants.GET.equalsIgnoreCase(method)) {
                if (cacheManager.inCache(url, request.getHeaders())) {
                    return updateSampleResultForResourceInCache(result);
                }
            }

            currentRequest = request;
            HttpClientKey clientKey = createHttpClientKey(url);
            HttpClientContext context = createHttpClientContext(url, clientKey, request);
            context.setAttribute(CONTEXT_ATTRIBUTE_SAMPLER_RESULT, result);
            response = clientKey.httpVersionPolicy != HttpVersionPolicy.FORCE_HTTP_1
                    ? executeHttp2(getHttp2Client(clientKey), request, context)
                    : getClient(clientKey).executeOpen(null, request, context);
            result.sampleEnd();
            currentRequest = null;

            updateResult(response, request, result);
            if (cacheManager != null) {
                cacheManager.saveDetails(response, result);
            }
            saveConnectionCookies(response, result.getURL(), getCookieManager());
            return resultProcessing(areFollowingRedirect, frameDepth, result);
        } catch (Exception e) {
            if (result.getEndTime() == 0) {
                result.sampleEnd();
            }
            if (request != null) {
                result.setRequestHeaders(getRequestHeaders(request));
                result.setSentBytes(calculateSentBytes(request));
            }
            return errorResult(e, result);
        } finally {
            JOrphanUtils.closeQuietly(response);
            currentRequest = null;
        }
    }

    private HTTPSampleResult createSampleResult(URL url, String method) {
        HTTPSampleResult result = new HTTPSampleResult();
        configureSampleLabel(result, url);
        result.setHTTPMethod(method);
        result.setURL(url);
        return result;
    }

    private static org.apache.hc.client5.http.classic.methods.HttpUriRequestBase createRequest(URI uri, String method) {
        return new org.apache.hc.client5.http.classic.methods.HttpUriRequestBase(method, uri);
    }

    private void setupRequest(URL url, org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request,
            HTTPSampleResult result, boolean areFollowingRedirect) throws IOException {
        HttpVersionPolicy httpVersionPolicy = getHttpVersionPolicy(testElement.getHttpVersion(), HTTP_VERSION);
        RequestConfig.Builder config = RequestConfig.custom()
                .setRedirectsEnabled(getAutoRedirects() && !areFollowingRedirect);
        int responseTimeout = getResponseTimeout();
        if (responseTimeout > 0) {
            config.setResponseTimeout(Timeout.ofMilliseconds(responseTimeout));
        }
        request.setConfig(config.build());
        if (httpVersionPolicy == HttpVersionPolicy.FORCE_HTTP_1) {
            request.setHeader(HTTPConstants.HEADER_CONNECTION,
                    getUseKeepAlive() ? HTTPConstants.KEEP_ALIVE : HTTPConstants.CONNECTION_CLOSE);
        } else if (httpVersionPolicy == HttpVersionPolicy.FORCE_HTTP_2) {
            request.setVersion(HttpVersion.HTTP_2);
        }
        setConnectionHeaders(request, getHeaderManager(), httpVersionPolicy);
        CacheManager cacheManager = getCacheManager();
        if (cacheManager != null) {
            cacheManager.setHeaders(url, request);
        }

        String cookies = setConnectionCookie(request, url, getCookieManager());
        if (StringUtilities.isNotEmpty(cookies)) {
            result.setCookies(cookies);
        } else {
            result.setCookies(getOnlyCookieFromHeaders(request));
        }

        if (canHaveBody(request.getMethod())) {
            result.setQueryString(setupRequestEntity(request));
        }
    }

    private static boolean canHaveBody(String method) {
        return !HTTPConstants.HEAD.equals(method) && !HTTPConstants.TRACE.equals(method);
    }

    private String setupRequestEntity(org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request) throws IOException {
        HTTPFileArg[] files = getHTTPFiles();
        String contentEncoding = getContentEncoding();
        Charset charset = Charset.forName(contentEncoding);
        HttpEntity entity;
        String requestData;
        if (getUseMultipart()) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create().setCharset(charset);
            for (JMeterProperty property : getArguments().getEnabledArguments()) {
                HTTPArgument argument = (HTTPArgument) property.getObjectValue();
                if (!argument.isSkippable(argument.getName())) {
                    ContentType contentType = StringUtilities.isNotEmpty(argument.getContentType())
                            ? ContentType.parse(argument.getContentType())
                            : ContentType.TEXT_PLAIN.withCharset(charset);
                    builder.addTextBody(argument.getName(), argument.getValue(), contentType);
                }
            }
            for (HTTPFileArg file : files) {
                File resolvedFile = FileServer.getFileServer().getResolvedFile(file.getPath());
                ContentType contentType = StringUtilities.isNotEmpty(file.getMimeType())
                        ? ContentType.parse(file.getMimeType()) : ContentType.DEFAULT_BINARY;
                builder.addBinaryBody(file.getParamName(), resolvedFile, contentType, file.getName());
            }
            entity = builder.build();
            requestData = getEntityPreview(entity, contentEncoding);
        } else if (!hasArguments() && getSendFileAsPostBody()) {
            HTTPFileArg file = files[0];
            if (request.getFirstHeader(HTTPConstants.HEADER_CONTENT_TYPE) == null && StringUtilities.isNotEmpty(file.getMimeType())) {
                request.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, file.getMimeType());
            }
            entity = new FileEntity(FileServer.getFileServer().getResolvedFile(file.getPath()), null);
            requestData = "<actual file content, not shown here>";
        } else if (getSendParameterValuesAsPostBody()) {
            StringBuilder body = new StringBuilder();
            for (JMeterProperty property : getArguments().getEnabledArguments()) {
                body.append(((HTTPArgument) property.getObjectValue()).getEncodedValue(contentEncoding));
            }
            entity = new StringEntity(body.toString(), charset);
            requestData = body.toString();
        } else if (hasArguments()) {
            entity = new UrlEncodedFormEntity(createNameValuePairs(contentEncoding), charset);
            requestData = getEntityPreview(entity, contentEncoding);
        } else {
            return "";
        }
        request.setEntity(entity);
        return requestData;
    }

    private List<NameValuePair> createNameValuePairs(String contentEncoding) throws IOException {
        List<NameValuePair> pairs = new ArrayList<>();
        for (JMeterProperty property : getArguments().getEnabledArguments()) {
            HTTPArgument argument = (HTTPArgument) property.getObjectValue();
            String name = argument.getName();
            if (argument.isSkippable(name)) {
                continue;
            }
            String value = argument.getValue();
            if (!argument.isAlwaysEncoded()) {
                name = URLDecoder.decode(name, contentEncoding);
                value = URLDecoder.decode(value, contentEncoding);
            }
            pairs.add(new BasicNameValuePair(name, value));
        }
        return pairs;
    }

    private static String getEntityPreview(HttpEntity entity, String contentEncoding) throws IOException {
        if (!entity.isRepeatable()) {
            return "<Entity was not repeatable, cannot view what was sent>";
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        entity.writeTo(output);
        return output.toString(contentEncoding);
    }

    private static void setConnectionHeaders(org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request,
            HeaderManager headerManager, HttpVersionPolicy httpVersionPolicy) {
        if (headerManager == null) {
            return;
        }
        CollectionProperty headers = headerManager.getHeaders();
        if (headers == null) {
            return;
        }
        for (JMeterProperty property : headers) {
            org.apache.jmeter.protocol.http.control.Header header =
                    (org.apache.jmeter.protocol.http.control.Header) property.getObjectValue();
            if (!HTTPConstants.HEADER_CONTENT_LENGTH.equalsIgnoreCase(header.getName())
                    && (httpVersionPolicy == HttpVersionPolicy.FORCE_HTTP_1
                    || !HTTPConstants.HEADER_CONNECTION.equalsIgnoreCase(header.getName()))) {
                request.addHeader(header.getName(), header.getValue());
            }
        }
    }

    private static String setConnectionCookie(org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request,
            URL url, CookieManager cookieManager) {
        if (cookieManager == null) {
            return null;
        }
        String cookies = cookieManager.getCookieHeaderForURL(url);
        if (cookies != null) {
            request.setHeader(HTTPConstants.HEADER_COOKIE, cookies);
        }
        return cookies;
    }

    private HttpClientContext createHttpClientContext(URL url, HttpClientKey key,
            org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request) {
        HttpClientContext context = HttpClientContext.create();
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        configureTargetCredentials(url, request, credentialsProvider);
        configureProxyCredentials(key, credentialsProvider);
        context.setCredentialsProvider(credentialsProvider);
        return context;
    }

    private void configureTargetCredentials(URL url,
            org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request,
            BasicCredentialsProvider credentialsProvider) {
        AuthManager authManager = getAuthManager();
        Authorization authorization = authManager == null ? null : authManager.getAuthForURL(url);
        if (authorization == null) {
            return;
        }
        credentialsProvider.setCredentials(new AuthScope(url.getHost(), getPort(url)),
                new UsernamePasswordCredentials(authorization.getUser(), authorization.getPass().toCharArray()));
        if (AuthManager.Mechanism.BASIC.equals(authorization.getMechanism())) {
            request.setHeader(HttpHeaders.AUTHORIZATION, authorization.toBasicHeader());
        }
    }

    private static void configureProxyCredentials(HttpClientKey key, BasicCredentialsProvider credentialsProvider) {
        if (key.hasProxy && StringUtilities.isNotEmpty(key.proxyUser)) {
            credentialsProvider.setCredentials(new AuthScope(key.proxyHost, key.proxyPort),
                    new UsernamePasswordCredentials(key.proxyUser, key.proxyPass.toCharArray()));
        }
    }

    private static int getPort(URL url) {
        return url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
    }

    private void updateResult(ClassicHttpResponse response,
            org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request, HTTPSampleResult result) throws IOException {
        result.setRequestHeaders(getRequestHeaders(request));
        result.setSentBytes(calculateSentBytes(request));
        Header contentType = response.getFirstHeader(HTTPConstants.HEADER_CONTENT_TYPE);
        if (contentType != null) {
            result.setContentType(contentType.getValue());
            result.setEncodingAndType(contentType.getValue());
        }
        HttpEntity entity = response.getEntity();
        long bodySize = 0;
        if (entity != null) {
            byte[] body = readResponse(result, entity.getContent(), entity.getContentLength());
            result.setResponseData(body);
            bodySize = body.length;
        }
        int statusCode = response.getCode();
        result.setResponseCode(Integer.toString(statusCode));
        result.setResponseMessage(response.getReasonPhrase());
        result.setSuccessful(isSuccessCode(statusCode));
        result.setResponseHeaders(getResponseHeaders(response));
        result.setHeadersSize(result.getResponseHeaders().length());
        result.setBodySize(bodySize);
        if (result.isRedirect()) {
            Header location = response.getFirstHeader(HTTPConstants.HEADER_LOCATION);
            if (location != null) {
                result.setRedirectLocation(location.getValue());
            }
        }
    }

    private static String getResponseHeaders(ClassicHttpResponse response) {
        StringBuilder headers = new StringBuilder();
        headers.append(response.getVersion()).append(' ').append(response.getCode()).append(' ')
                .append(response.getReasonPhrase()).append('\n');
        for (Header header : response.getHeaders()) {
            headers.append(header.getName()).append(": ").append(header.getValue()).append('\n');
        }
        return headers.toString();
    }

    private static String getRequestHeaders(org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request) {
        StringBuilder headers = new StringBuilder();
        for (Header header : request.getHeaders()) {
            if (ALL_EXCEPT_COOKIE.test(header.getName())) {
                headers.append(header.getName()).append(": ").append(header.getValue()).append('\n');
            }
        }
        return headers.toString();
    }

    private static long calculateSentBytes(org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request) {
        if (request == null) {
            return 0;
        }
        long sentBytes = 0;

        String method = request.getMethod();
        String uri = request.getRequestUri();
        if (uri == null) {
            uri = "";
        }
        org.apache.hc.core5.http.ProtocolVersion version = request.getVersion();
        String versionStr = version != null ? version.toString() : "HTTP/1.1";

        sentBytes += method.getBytes(Charset.defaultCharset()).length;
        sentBytes += 1;
        sentBytes += uri.getBytes(Charset.defaultCharset()).length;
        sentBytes += 1;
        sentBytes += versionStr.getBytes(Charset.defaultCharset()).length;
        sentBytes += 2;

        for (Header header : request.getHeaders()) {
            String name = header.getName();
            String value = header.getValue();
            if (name != null) {
                sentBytes += name.getBytes(Charset.defaultCharset()).length;
                sentBytes += 2;
            }
            if (value != null) {
                sentBytes += value.getBytes(Charset.defaultCharset()).length;
            }
            sentBytes += 2;
        }
        sentBytes += 2;

        HttpEntity entity = request.getEntity();
        if (entity != null) {
            long contentLength = entity.getContentLength();
            if (contentLength >= 0) {
                sentBytes += contentLength;
            } else if (entity.isRepeatable()) {
                CountingOutputStream counter = new CountingOutputStream();
                try {
                    entity.writeTo(counter);
                    sentBytes += counter.getCount();
                } catch (IOException e) {
                    log.debug("Exception measuring entity length", e);
                }
            }
        }

        return sentBytes;
    }

    private static class CountingOutputStream extends java.io.OutputStream {
        private long count = 0;

        @Override
        public void write(int b) {
            count++;
        }

        @Override
        public void write(byte[] b, int off, int len) {
            count += len;
        }

        long getCount() {
            return count;
        }
    }

    private static String getOnlyCookieFromHeaders(org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request) {
        Header cookie = request.getFirstHeader(HTTPConstants.HEADER_COOKIE);
        return cookie == null ? "" : cookie.getValue();
    }

    private static void saveConnectionCookies(ClassicHttpResponse response, URL url, CookieManager cookieManager) {
        if (cookieManager == null) {
            return;
        }
        for (Header header : response.getHeaders(HTTPConstants.HEADER_SET_COOKIE)) {
            cookieManager.addCookieFromHeader(header.getValue(), url);
        }
    }

    private static CloseableHttpClient getClient(HttpClientKey key) {
        Map<HttpClientKey, CloseableHttpClient> clients = HTTP_CLIENTS.get();
        return clients.computeIfAbsent(key, HTTPHC5Impl::createClient);
    }

    private static CloseableHttpAsyncClient getHttp2Client(HttpClientKey key) {
        Map<HttpClientKey, CloseableHttpAsyncClient> clients = HTTP_2_CLIENTS.get();
        return clients.computeIfAbsent(key, HTTPHC5Impl::createHttp2Client);
    }

    private static CloseableHttpClient createClient(HttpClientKey key) {
        org.apache.hc.client5.http.impl.classic.HttpClientBuilder builder = HttpClients.custom().disableAutomaticRetries();
        PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();
        connectionManagerBuilder.setDefaultTlsConfig(TlsConfig.custom()
                .setVersionPolicy(key.httpVersionPolicy)
                .build());
        if (key.dnsCacheManager != null) {
            connectionManagerBuilder.setDnsResolver(createDnsResolver(key.dnsCacheManager));
        }
        if (key.connectTimeout > 0) {
            connectionManagerBuilder
                    .setDefaultConnectionConfig(ConnectionConfig.custom()
                            .setConnectTimeout(Timeout.ofMilliseconds(key.connectTimeout))
                            .build());
        }
        builder.setConnectionManager(new ConnectTimeMeasuringConnectionManager(connectionManagerBuilder.build()));
        builder.setRoutePlanner(createRoutePlanner(key));
        return builder.disableContentCompression()
                .addExecInterceptorFirst("response-content-encoding", RESPONSE_CONTENT_ENCODING)
                .build();
    }

    private static CloseableHttpAsyncClient createHttp2Client(HttpClientKey key) {
        HttpAsyncClientBuilder builder = HttpAsyncClients.custom()
                .disableAutomaticRetries()
                .setRoutePlanner(createRoutePlanner(key));
        PoolingAsyncClientConnectionManagerBuilder connectionManagerBuilder = PoolingAsyncClientConnectionManagerBuilder.create();
        connectionManagerBuilder.setTlsStrategy(HTTP_2_TLS_STRATEGY);
        connectionManagerBuilder.setDefaultTlsConfig(TlsConfig.custom()
                .setVersionPolicy(key.httpVersionPolicy)
                .build());
        if (key.dnsCacheManager != null) {
            connectionManagerBuilder.setDnsResolver(createDnsResolver(key.dnsCacheManager));
        }
        if (key.connectTimeout > 0) {
            connectionManagerBuilder.setDefaultConnectionConfig(ConnectionConfig.custom()
                    .setConnectTimeout(Timeout.ofMilliseconds(key.connectTimeout))
                    .build());
        }
        builder.setConnectionManager(new ConnectTimeMeasuringAsyncConnectionManager(connectionManagerBuilder.build()));
        CloseableHttpAsyncClient asyncClient = builder.build();
        asyncClient.start();
        return asyncClient;
    }

    @SuppressWarnings("deprecation") // SimpleHttpRequest.copy is required for HttpClient 5.3 compatibility
    private static ClassicHttpResponse executeHttp2(CloseableHttpAsyncClient client,
            org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request, HttpClientContext context)
            throws IOException {
        SimpleHttpRequest asyncRequest = SimpleHttpRequest.copy(request);
        asyncRequest.setConfig(request.getConfig());
        HttpEntity requestEntity = request.getEntity();
        if (requestEntity != null) {
            asyncRequest.setBody(EntityUtils.toByteArray(requestEntity),
                    requestEntity.getContentType() == null ? ContentType.DEFAULT_BINARY
                            : ContentType.parse(requestEntity.getContentType()));
        }
        Future<SimpleHttpResponse> responseFuture = client.execute(asyncRequest, context, null);
        try {
            return createClassicResponse(responseFuture.get(1, java.util.concurrent.TimeUnit.MINUTES));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while executing HTTP/2 request", e);
        } catch (TimeoutException e) {
            responseFuture.cancel(true);
            throw new IOException("Timed out while executing HTTP/2 request", e);
        } catch (ExecutionException e) {
            throw new IOException("Could not execute HTTP/2 request", e.getCause());
        }
    }

    private static ClassicHttpResponse createClassicResponse(SimpleHttpResponse asyncResponse) {
        BasicClassicHttpResponse response = new BasicClassicHttpResponse(asyncResponse.getCode(),
                asyncResponse.getReasonPhrase());
        response.setVersion(asyncResponse.getVersion());
        for (Header header : asyncResponse.getHeaders()) {
            response.addHeader(header);
        }
        byte[] responseBody = asyncResponse.getBodyBytes();
        if (responseBody != null) {
            response.setEntity(new ByteArrayEntity(responseBody, asyncResponse.getContentType()));
        }
        return response;
    }

    private static DefaultRoutePlanner createRoutePlanner(HttpClientKey key) {
        return new DefaultRoutePlanner(null) {
            @Override
            protected HttpHost determineProxy(HttpHost target, org.apache.hc.core5.http.protocol.HttpContext context) {
                return key.hasProxy ? new HttpHost(key.proxyScheme, key.proxyHost, key.proxyPort) : null;
            }

            @Override
            protected InetAddress determineLocalAddress(HttpHost firstHop,
                    org.apache.hc.core5.http.protocol.HttpContext context) {
                return key.localAddress;
            }
        };
    }

    private static org.apache.hc.client5.http.DnsResolver createDnsResolver(DNSCacheManager dnsCacheManager) {
        return new org.apache.hc.client5.http.DnsResolver() {
            @Override
            public InetAddress[] resolve(String host) throws UnknownHostException {
                return dnsCacheManager.resolve(host);
            }

            @Override
            public String resolveCanonicalHostname(String host) throws UnknownHostException {
                InetAddress[] addresses = resolve(host);
                return addresses == null || addresses.length == 0 ? host : addresses[0].getCanonicalHostName();
            }
        };
    }

    private HttpClientKey createHttpClientKey(URL url) throws IOException {
        String proxyScheme = getProxyScheme();
        String proxyHost = getProxyHost();
        int proxyPort = getProxyPortInt();
        int connectTimeout = getConnectTimeout();
        String proxyUser = getProxyUser();
        String proxyPass = getProxyPass();
        DNSCacheManager dnsCacheManager = testElement.getDNSResolver();
        InetAddress localAddress = getIpSourceAddress();
        boolean useDynamicProxy = isDynamicProxy(proxyHost, proxyPort);
        boolean useStaticProxy = isStaticProxy(url.getHost());
        HttpVersionPolicy httpVersionPolicy = getHttpVersionPolicy(testElement.getHttpVersion(), HTTP_VERSION);
        if (!useDynamicProxy) {
            proxyScheme = PROXY_SCHEME;
            proxyHost = PROXY_HOST;
            proxyPort = PROXY_PORT;
        }
        return new HttpClientKey(url.getProtocol(), url.getAuthority(), useDynamicProxy || useStaticProxy,
                proxyScheme, proxyHost, proxyPort, proxyUser, proxyPass, connectTimeout, dnsCacheManager, localAddress,
                httpVersionPolicy);
    }

    static HttpVersionPolicy getHttpVersionPolicy(String samplerHttpVersion, String defaultHttpVersion) {
        String httpVersion = StringUtilities.isBlank(samplerHttpVersion) ? defaultHttpVersion : samplerHttpVersion;
        return "HTTP/2".equals(httpVersion) || "2".equals(httpVersion)
                ? HttpVersionPolicy.NEGOTIATE : HttpVersionPolicy.FORCE_HTTP_1;
    }

    @Override
    protected void notifyFirstSampleAfterLoopRestart() {
        JMeterVariables variables = JMeterContextService.getContext().getVariables();
        resetStateOnThreadGroupIteration.set(variables != null && !variables.isSameUserOnNextIteration()
                && RESET_STATE_ON_THREAD_GROUP_ITERATION);
    }

    private static void resetStateIfNeeded() {
        if (resetStateOnThreadGroupIteration.get()) {
            closeThreadLocalClients();
            ((JsseSSLManager) SSLManager.getInstance()).resetContext();
            resetStateOnThreadGroupIteration.set(false);
        }
    }

    @Override
    protected void threadFinished() {
        closeThreadLocalClients();
    }

    private static void closeThreadLocalClients() {
        Map<HttpClientKey, CloseableHttpClient> clients = HTTP_CLIENTS.get();
        for (CloseableHttpClient client : clients.values()) {
            JOrphanUtils.closeQuietly(client);
        }
        clients.clear();
        Map<HttpClientKey, CloseableHttpAsyncClient> http2Clients = HTTP_2_CLIENTS.get();
        for (CloseableHttpAsyncClient client : http2Clients.values()) {
            JOrphanUtils.closeQuietly(client);
        }
        http2Clients.clear();
    }

    @Override
    public boolean interrupt() {
        org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request = currentRequest;
        if (request != null) {
            currentRequest = null;
            request.cancel();
        }
        return request != null;
    }

    private static void recordConnectEnd(HttpContext context) {
        SampleResult sample = (SampleResult) context.getAttribute(CONTEXT_ATTRIBUTE_SAMPLER_RESULT);
        if (sample != null) {
            sample.connectEnd();
        }
    }

    /**
     * Delegating connection manager that records the connect time of the current sample
     * as soon as the connection to the first hop has been established.
     */
    static final class ConnectTimeMeasuringConnectionManager implements HttpClientConnectionManager {

        private final HttpClientConnectionManager delegate;

        ConnectTimeMeasuringConnectionManager(HttpClientConnectionManager delegate) {
            this.delegate = delegate;
        }

        @Override
        public LeaseRequest lease(String id, HttpRoute route, Timeout requestTimeout, Object state) {
            return delegate.lease(id, route, requestTimeout, state);
        }

        @Override
        public void release(ConnectionEndpoint endpoint, Object newState, TimeValue validDuration) {
            delegate.release(endpoint, newState, validDuration);
        }

        @Override
        public void connect(ConnectionEndpoint endpoint, TimeValue connectTimeout, HttpContext context) throws IOException {
            try {
                delegate.connect(endpoint, connectTimeout, context);
            } finally {
                recordConnectEnd(context);
            }
        }

        @Override
        public void upgrade(ConnectionEndpoint endpoint, HttpContext context) throws IOException {
            delegate.upgrade(endpoint, context);
        }

        @Override
        public void close(CloseMode closeMode) {
            delegate.close(closeMode);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    /**
     * Delegating asynchronous connection manager that records the connect time of the current
     * sample as soon as the connection to the first hop has been established.
     */
    private static final class ConnectTimeMeasuringAsyncConnectionManager implements AsyncClientConnectionManager {

        private final AsyncClientConnectionManager delegate;

        private ConnectTimeMeasuringAsyncConnectionManager(AsyncClientConnectionManager delegate) {
            this.delegate = delegate;
        }

        @Override
        public Future<AsyncConnectionEndpoint> lease(String id, HttpRoute route, Object state, Timeout requestTimeout,
                FutureCallback<AsyncConnectionEndpoint> callback) {
            return delegate.lease(id, route, state, requestTimeout, callback);
        }

        @Override
        public void release(AsyncConnectionEndpoint endpoint, Object newState, TimeValue validDuration) {
            delegate.release(endpoint, newState, validDuration);
        }

        @Override
        public Future<AsyncConnectionEndpoint> connect(AsyncConnectionEndpoint endpoint,
                ConnectionInitiator connectionInitiator, Timeout connectTimeout, Object attachment,
                HttpContext context, FutureCallback<AsyncConnectionEndpoint> callback) {
            return delegate.connect(endpoint, connectionInitiator, connectTimeout, attachment, context,
                    new FutureCallback<>() {
                        @Override
                        public void completed(AsyncConnectionEndpoint result) {
                            recordConnectEnd(context);
                            if (callback != null) {
                                callback.completed(result);
                            }
                        }

                        @Override
                        public void failed(Exception ex) {
                            recordConnectEnd(context);
                            if (callback != null) {
                                callback.failed(ex);
                            }
                        }

                        @Override
                        public void cancelled() {
                            recordConnectEnd(context);
                            if (callback != null) {
                                callback.cancelled();
                            }
                        }
                    });
        }

        @Override
        public void upgrade(AsyncConnectionEndpoint endpoint, Object attachment, HttpContext context) {
            delegate.upgrade(endpoint, attachment, context);
        }

        @Override
        public void upgrade(AsyncConnectionEndpoint endpoint, Object attachment, HttpContext context,
                FutureCallback<AsyncConnectionEndpoint> callback) {
            delegate.upgrade(endpoint, attachment, context, callback);
        }

        @Override
        public void close(CloseMode closeMode) {
            delegate.close(closeMode);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    private static final class HttpClientKey {
        private final String protocol;
        private final String authority;
        private final boolean hasProxy;
        private final String proxyScheme;
        private final String proxyHost;
        private final int proxyPort;
        private final String proxyUser;
        private final String proxyPass;
        private final int connectTimeout;
        private final DNSCacheManager dnsCacheManager;
        private final InetAddress localAddress;
        private final HttpVersionPolicy httpVersionPolicy;

        private HttpClientKey(String protocol, String authority, boolean hasProxy, String proxyScheme,
                String proxyHost, int proxyPort, String proxyUser, String proxyPass, int connectTimeout, DNSCacheManager dnsCacheManager,
                InetAddress localAddress, HttpVersionPolicy httpVersionPolicy) {
            this.protocol = protocol;
            this.authority = authority;
            this.hasProxy = hasProxy;
            this.proxyScheme = proxyScheme;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
            this.proxyUser = proxyUser;
            this.proxyPass = proxyPass;
            this.connectTimeout = connectTimeout;
            this.dnsCacheManager = dnsCacheManager;
            this.localAddress = localAddress;
            this.httpVersionPolicy = httpVersionPolicy;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof HttpClientKey other)) {
                return false;
            }
            return hasProxy == other.hasProxy && proxyPort == other.proxyPort && connectTimeout == other.connectTimeout
                    && Objects.equals(protocol, other.protocol) && Objects.equals(authority, other.authority)
                    && Objects.equals(proxyScheme, other.proxyScheme) && Objects.equals(proxyHost, other.proxyHost)
                    && Objects.equals(proxyUser, other.proxyUser) && Objects.equals(proxyPass, other.proxyPass)
                    && Objects.equals(dnsCacheManager, other.dnsCacheManager) && Objects.equals(localAddress, other.localAddress)
                    && httpVersionPolicy == other.httpVersionPolicy;
        }

        @Override
        public int hashCode() {
            return Objects.hash(protocol, authority, hasProxy, proxyScheme, proxyHost, proxyPort, proxyUser, proxyPass, connectTimeout, dnsCacheManager,
                    localAddress, httpVersionPolicy);
        }
    }
}
