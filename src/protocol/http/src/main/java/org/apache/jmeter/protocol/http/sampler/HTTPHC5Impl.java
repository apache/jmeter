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
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;
import javax.security.auth.Subject;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.RouteInfo;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.async.methods.AbstractBinResponseConsumer;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.KerberosConfig;
import org.apache.hc.client5.http.auth.KerberosCredentials;
import org.apache.hc.client5.http.auth.StandardAuthScheme;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.entity.DecompressingEntity;
import org.apache.hc.client5.http.entity.DeflateInputStreamFactory;
import org.apache.hc.client5.http.entity.GZIPInputStreamFactory;
import org.apache.hc.client5.http.entity.InputStreamFactory;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicSchemeFactory;
import org.apache.hc.client5.http.impl.auth.BearerSchemeFactory;
import org.apache.hc.client5.http.impl.auth.DigestSchemeFactory;
import org.apache.hc.client5.http.impl.auth.KerberosSchemeFactory;
import org.apache.hc.client5.http.impl.auth.SPNegoSchemeFactory;
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
import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.client5.http.utils.URIUtils;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.io.entity.HttpEntityWrapper;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.message.BasicHeaderValueParser;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;
import org.apache.hc.core5.http.nio.entity.FileEntityProducer;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.core5.util.VersionInfo;
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
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.JsseSSLManager;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.io.CountingInputStream;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.jorphan.util.StringUtilities;
import org.brotli.dec.BrotliInputStream;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.Oid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP Sampler using Apache HttpClient 5.x.
 */
public class HTTPHC5Impl extends HTTPHCAbstractImpl {

    private static final Logger log = LoggerFactory.getLogger(HTTPHC5Impl.class);

    /** Key used to store the current {@link SampleResult} in the {@link HttpClientContext}. */
    static final String CONTEXT_ATTRIBUTE_SAMPLER_RESULT = "__jmeter.S_R__"; //$NON-NLS-1$
    private static final String CONTEXT_ATTRIBUTE_RESPONSE_LATENCY = "__jmeter.S_R_LATENCY__"; //$NON-NLS-1$
    private static final String CONTEXT_ATTRIBUTE_RESPONSE_BODY_SIZE = "__jmeter.S_R_BODY_SIZE__"; //$NON-NLS-1$
    private static final String CONTEXT_ATTRIBUTE_RESPONSE_BODY_COUNTER = "__jmeter.S_R_BODY_COUNTER__"; //$NON-NLS-1$

    private static final ThreadLocal<Map<HttpClientKey, CloseableHttpClient>> HTTP_CLIENTS =
            new InheritableThreadLocal<>() {
                @Override
                protected Map<HttpClientKey, CloseableHttpClient> initialValue() {
                    return new ConcurrentHashMap<>();
                }
            };

    /**
     * HTTP/2 clients are shared with the threads that download embedded resources in parallel,
     * so that concurrent requests to the same host can be multiplexed over a single connection.
     */
    private static final ThreadLocal<Map<HttpClientKey, CloseableHttpAsyncClient>> HTTP_2_CLIENTS =
            new InheritableThreadLocal<>() {
                @Override
                protected Map<HttpClientKey, CloseableHttpAsyncClient> initialValue() {
                    return new ConcurrentHashMap<>();
                }
            };

    /** Multiplex concurrent message exchanges over a single HTTP/2 connection. */
    private static final boolean HTTP_2_MULTIPLEXING =
            JMeterUtils.getPropDefault("httpclient5.h2.multiplexing", true);

    /** Size of the HPACK dynamic header table announced to the server, {@code 0} disables HPACK indexing. */
    private static final int HTTP_2_HEADER_TABLE_SIZE =
            JMeterUtils.getPropDefault("httpclient5.h2.header_table_size", H2Config.DEFAULT.getHeaderTableSize());

    /** Whether HPACK compression of the request headers is used. */
    private static final boolean HTTP_2_HEADER_COMPRESSION =
            JMeterUtils.getPropDefault("httpclient5.h2.header_compression", true);

    private static final int HTTP_2_MAX_CONCURRENT_STREAMS =
            JMeterUtils.getPropDefault("httpclient5.h2.max_concurrent_streams", H2Config.DEFAULT.getMaxConcurrentStreams());

    /**
     * HTTP/2 flow control window announced per stream. It caps how many bytes the server may send
     * before it has to wait for a window update, so the throughput of a single response can never
     * exceed this window divided by the round trip time. The default of HttpClient is the 64 kB the
     * HTTP/2 specification mandates, which throttles a download over a link with any noticeable
     * latency to a fraction of the available bandwidth. JMeter therefore announces the same 16 MB
     * the JDK client uses by default, see {@code http.java.h2.initial_window_size}. The window is
     * only a promise to the server, the body is consumed as it arrives and not buffered up to this
     * size.
     */
    private static final int HTTP_2_INITIAL_WINDOW_SIZE =
            JMeterUtils.getPropDefault("httpclient5.h2.initial_window_size", 16 * 1024 * 1024);

    private static final int HTTP_2_MAX_FRAME_SIZE =
            JMeterUtils.getPropDefault("httpclient5.h2.max_frame_size", H2Config.DEFAULT.getMaxFrameSize());

    /**
     * JMeter has no way of reporting pushed resources, so server push is switched off to avoid
     * the server wasting bandwidth on responses that are dropped.
     */
    private static final boolean HTTP_2_PUSH_ENABLED =
            JMeterUtils.getPropDefault("httpclient5.h2.push_enabled", false);

    /**
     * Use HTTP/2 without TLS (h2c with prior knowledge) when HTTP/2 is selected for a {@code http://} URL.
     * Without prior knowledge such requests silently fall back to HTTP/1.1, as ALPN is unavailable.
     */
    private static final boolean HTTP_2_PRIOR_KNOWLEDGE =
            JMeterUtils.getPropDefault("httpclient5.h2.prior_knowledge", false);

    private static final int HTTP_2_MAX_CONNECTIONS_PER_ROUTE =
            JMeterUtils.getPropDefault("httpclient5.h2.max_connections_per_route", 6);

    /**
     * Number of bytes of an HTTP/2 request body that are buffered in memory before the body is
     * spilled to a temporary file. Keeping the common small bodies in heap avoids a temporary file
     * per sample, while a larger body is streamed from disk so that a big upload is never
     * materialized as a byte array.
     */
    private static final int HTTP_2_BODY_SPILL_THRESHOLD =
            JMeterUtils.getPropDefault("httpclient5.h2.body_spill_threshold", 256 * 1024);

    /**
     * Milliseconds of inactivity after which a pooled connection is re-validated before it is used
     * again, {@code -1} disables the check. JMeter keeps a client per thread and reuses its
     * connections across iterations, so a connection the server (or an idle timeout of a load
     * balancer) closed in between would otherwise be handed out as-is and fail the next sample.
     * HttpClient leaves this undefined by default, which skips the check altogether, and the
     * automatic retries that would hide such a failure are deliberately disabled.
     */
    private static final long VALIDATE_AFTER_INACTIVITY =
            JMeterUtils.getPropDefault("httpclient5.validate_after_inactivity", 2000L);

    /**
     * Name of the property that suppresses the {@code User-Agent} header HttpClient sends when the
     * test plan does not define one itself.
     */
    private static final String DISABLE_DEFAULT_UA_PROPERTY = "httpclient5.default_user_agent_disabled";

    /**
     * {@code User-Agent} JMeter adds to requests without one, so it shows up in the sample result and
     * is accounted for in the sent bytes, instead of being added invisibly by HttpClient.
     */
    private static final String DEFAULT_USER_AGENT = VersionInfo.getSoftwareInfo("Apache-HttpClient",
            "org.apache.hc.client5", org.apache.hc.client5.http.impl.classic.HttpClientBuilder.class);

    /** Remembers whether the request had a {@code User-Agent} header before HttpClient added its own. */
    private static final String CONTEXT_ATTRIBUTE_USER_AGENT_PRESENT = "__jmeter.U_A__";

    /** Shown instead of the request body when the entity cannot be read a second time. */
    private static final String NON_REPEATABLE_ENTITY_PREVIEW =
            "<Entity was not repeatable, cannot view what was sent>"; // $NON-NLS-1$

    private static final HttpRequestInterceptor RECORD_USER_AGENT_PRESENCE = (request, entity, context) ->
            context.setAttribute(CONTEXT_ATTRIBUTE_USER_AGENT_PRESENT, request.containsHeader(HttpHeaders.USER_AGENT));

    private static final HttpRequestInterceptor REMOVE_DEFAULT_USER_AGENT = (request, entity, context) -> {
        if (!Boolean.TRUE.equals(context.getAttribute(CONTEXT_ATTRIBUTE_USER_AGENT_PRESENT))) {
            request.removeHeaders(HttpHeaders.USER_AGENT);
        }
    };

    private static final H2Config HTTP_2_CONFIG = createHttp2Config();

    /**
     * Request that the Kerberos credentials of the JMeter user are delegated to the server, so
     * that it can act on behalf of the user, see the {@code kerberos.spnego.delegate_cred}
     * property.
     */
    private static final boolean KERBEROS_DELEGATE_CRED =
            JMeterUtils.getPropDefault("kerberos.spnego.delegate_cred", false);

    /**
     * HttpClient 5 only offers {@code Bearer}, {@code Digest} and {@code Basic} by default, so the
     * negotiation based schemes have to be requested explicitly for a Kerberos authorization.
     */
    @SuppressWarnings("deprecation") // The GSS based auth schemes of HttpClient 5 have no replacement yet
    private static final List<String> KERBEROS_PREFERRED_AUTH_SCHEMES =
            List.of(StandardAuthScheme.SPNEGO, StandardAuthScheme.KERBEROS);

    /**
     * Auth schemes offered to a proxy that can be authenticated with Kerberos. Unlike the target
     * schemes these keep the password based schemes as a fallback, so that a proxy which offers
     * {@code Negotiate} next to {@code Basic} or {@code Digest} can still be used.
     */
    @SuppressWarnings("deprecation") // The GSS based auth schemes of HttpClient 5 have no replacement yet
    private static final List<String> KERBEROS_PROXY_PREFERRED_AUTH_SCHEMES =
            List.of(StandardAuthScheme.SPNEGO, StandardAuthScheme.KERBEROS, StandardAuthScheme.BEARER,
                    StandardAuthScheme.DIGEST, StandardAuthScheme.BASIC);

    private static final Oid SPNEGO_OID = createOid("1.3.6.1.5.5.2");

    private static final Oid KERBEROS_OID = createOid("1.2.840.113554.1.2.2");

    /**
     * Credentials without a principal, telling the negotiation based auth schemes to use the
     * credentials of the JAAS {@link Subject} the request is executed with.
     */
    private static final Credentials USE_JAAS_CREDENTIALS = new Credentials() {
        @Override
        public Principal getUserPrincipal() {
            return null;
        }

        @Override
        @SuppressWarnings("deprecation") // Credentials.getPassword is deprecated, but has to be implemented
        public char[] getPassword() {
            return new char[0];
        }
    };

    private static final Method MESSAGE_MULTIPLEXING_SETTER = findMessageMultiplexingSetter();

    private static final String[] HEADERS_TO_SAVE = {HttpHeaders.CONTENT_LENGTH, HttpHeaders.CONTENT_ENCODING,
            HttpHeaders.CONTENT_MD5};

    private static final String[] SOCKET_PROTOCOL_ARRAY =
            JMeterUtils.getArrayPropDefault("https.socket.protocols", null);

    private static final String[] SOCKET_CIPHER_ARRAY =
            JMeterUtils.getArrayPropDefault("https.socket.ciphers", null);

    private static final String[] CIPHER_SUITE_ARRAY =
            JMeterUtils.getArrayPropDefault("https.cipherSuites", SOCKET_CIPHER_ARRAY);

    // HttpClient 5.6 switched BrotliInputStreamFactory to the optional brotli4j library, so decode "br"
    // with the org.brotli:dec library JMeter ships, like HTTPHC4Impl does.
    @SuppressWarnings("deprecation")
    private static final InputStreamFactory BROTLI = BrotliInputStream::new;

    // The InputStreamFactory-based decoders are deprecated in favour of the @Internal ContentCodecRegistry,
    // so keep using them until a public replacement is available.
    @SuppressWarnings("deprecation")
    private static final Lookup<InputStreamFactory> CONTENT_DECODERS = RegistryBuilder.<InputStreamFactory>create()
            .register("br", BROTLI)
            .register("gzip", GZIPInputStreamFactory.getInstance())
            .register("x-gzip", GZIPInputStreamFactory.getInstance())
            .register("deflate", DeflateInputStreamFactory.getInstance())
            .build();

    private static final ExecChainHandler RESPONSE_CONTENT_ENCODING = (request, scope, chain) -> {
        HttpClientContext context = scope.clientContext;
        RequestConfig requestConfig = context.getRequestConfig();
        if (requestConfig == null) {
            requestConfig = RequestConfig.DEFAULT;
        }
        ClassicHttpResponse response = chain.proceed(request, scope);
        if (!requestConfig.isContentCompressionEnabled()) {
            return response;
        }
        return decompressResponse(response, context);
    };

    /**
     * Decodes the response body while keeping the {@code Content-Encoding}, {@code Content-Length} and
     * {@code Content-MD5} headers, so the sample result still reports what the server actually sent.
     * JMeter therefore disables the transparent decompression of HttpClient, which drops those headers,
     * and decodes the response itself for both the HTTP/1.1 and the HTTP/2 transport.
     */
    @SuppressWarnings("deprecation") // DecompressingEntity is superseded by the @Internal ContentCodecRegistry
    private static ClassicHttpResponse decompressResponse(ClassicHttpResponse response, HttpContext context) {
        HttpEntity entity = response.getEntity();
        if (entity == null || entity.getContentLength() == 0) {
            return response;
        }

        String contentEncoding = entity.getContentEncoding();
        if (contentEncoding == null) {
            Header encodingHeader = response.getFirstHeader(HttpHeaders.CONTENT_ENCODING);
            if (encodingHeader != null) {
                contentEncoding = encodingHeader.getValue();
            }
        }
        if (contentEncoding == null) {
            return response;
        }

        Header[][] headersToSave = new Header[HEADERS_TO_SAVE.length][];
        for (int i = 0; i < HEADERS_TO_SAVE.length; i++) {
            headersToSave[i] = response.getHeaders(HEADERS_TO_SAVE[i]);
        }
        HeaderElement[] codecs = BasicHeaderValueParser.INSTANCE.parseElements(contentEncoding,
                new ParserCursor(0, contentEncoding.length()));
        CountingEntity countingEntity = new CountingEntity(entity);
        boolean decompressing = false;
        for (HeaderElement codec : codecs) {
            InputStreamFactory decoderFactory = CONTENT_DECODERS.lookup(codec.getName().toLowerCase(Locale.ROOT));
            if (decoderFactory != null) {
                if (!decompressing) {
                    response.setEntity(countingEntity);
                    context.setAttribute(CONTEXT_ATTRIBUTE_RESPONSE_BODY_COUNTER, countingEntity);
                    decompressing = true;
                }
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
    }

    private volatile org.apache.hc.client5.http.classic.methods.HttpUriRequestBase currentRequest;
    private volatile Future<?> currentResponseFuture;
    private volatile boolean interruptRequested;

    private static ClientTlsStrategyBuilder createTlsStrategyBuilder() {
        try {
            SSLContext sslContext = ((JsseSSLManager) SSLManager.getInstance()).getContext();
            ClientTlsStrategyBuilder builder = ClientTlsStrategyBuilder.create()
                    .setSslContext(sslContext)
                    // Leave hostname verification to the no-op verifier below. Without CLIENT the policy would
                    // default to BOTH, and the JSSE built-in endpoint identification would reject the
                    // self-signed certificates JMeter deliberately accepts when testing.
                    .setHostVerificationPolicy(HostnameVerificationPolicy.CLIENT)
                    .setHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            if (SOCKET_PROTOCOL_ARRAY != null) {
                builder.setTlsVersions(SOCKET_PROTOCOL_ARRAY);
            }
            if (CIPHER_SUITE_ARRAY != null) {
                builder.setCiphers(CIPHER_SUITE_ARRAY);
            }
            return builder;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not create TLS strategy", e);
        }
    }

    private static TlsSocketStrategy createTlsSocketStrategy() {
        return createTlsStrategyBuilder().buildClassic();
    }

    private static TlsStrategy createTlsStrategy() {
        return createTlsStrategyBuilder().buildAsync();
    }

    static H2Config createHttp2Config() {
        return H2Config.custom()
                .setHeaderTableSize(HTTP_2_HEADER_TABLE_SIZE)
                .setCompressionEnabled(HTTP_2_HEADER_COMPRESSION)
                .setMaxConcurrentStreams(HTTP_2_MAX_CONCURRENT_STREAMS)
                .setInitialWindowSize(HTTP_2_INITIAL_WINDOW_SIZE)
                .setMaxFrameSize(HTTP_2_MAX_FRAME_SIZE)
                .setPushEnabled(HTTP_2_PUSH_ENABLED)
                .build();
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

            interruptRequested = false;
            currentRequest = request;
            HttpClientKey clientKey = createHttpClientKey(url);
            HttpClientContext context = createHttpClientContext(url, clientKey, request);
            context.setAttribute(CONTEXT_ATTRIBUTE_SAMPLER_RESULT, result);
            response = executeRequest(url, clientKey, request, context);
            readResponse(response, result, context);
            Long responseLatency = (Long) context.getAttribute(CONTEXT_ATTRIBUTE_RESPONSE_LATENCY);
            if (responseLatency != null) {
                result.setLatency(responseLatency);
            }
            result.sampleEnd();
            currentRequest = null;

            updateResult(response, request, result);
            updateUrlAfterAutoRedirects(url, context, result);
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

    /**
     * Executes the request under the JAAS {@link Subject} of the {@link AuthManager}, if the URL or
     * the proxy is covered by a Kerberos authorization, so that the auth scheme can use its
     * credentials.
     */
    private ClassicHttpResponse executeRequest(URL url, HttpClientKey clientKey,
            org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request, HttpClientContext context)
            throws IOException {
        Subject subject = getSubjectForUrl(url);
        if (subject == null) {
            subject = getSubjectForProxy(clientKey);
        }
        if (subject == null) {
            return doExecuteRequest(clientKey, request, context);
        }
        try {
            return Subject.doAs(subject, (PrivilegedExceptionAction<ClassicHttpResponse>) () ->
                    doExecuteRequest(clientKey, request, context));
        } catch (PrivilegedActionException e) {
            Exception cause = e.getException();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException("Could not execute the request with subject " + subject, cause);
        }
    }

    private ClassicHttpResponse doExecuteRequest(HttpClientKey clientKey,
            org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request, HttpClientContext context)
            throws IOException {
        return clientKey.httpVersionPolicy != HttpVersionPolicy.FORCE_HTTP_1
                ? executeHttp2(getHttp2Client(clientKey), request, context)
                : getClient(clientKey).executeOpen(null, request, context);
    }

    private void setupRequest(URL url, org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request,
            HTTPSampleResult result, boolean areFollowingRedirect) throws IOException {
        HttpVersionPolicy httpVersionPolicy = getHttpVersionPolicy(testElement.getHttpVersion(), HTTP_VERSION,
                url.getProtocol());
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
        setDefaultUserAgent(request);
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
            List<ViewableFileBody> fileBodies = new ArrayList<>(files.length);
            for (HTTPFileArg file : files) {
                File resolvedFile = FileServer.getFileServer().getResolvedFile(file.getPath());
                ContentType contentType = StringUtilities.isNotEmpty(file.getMimeType())
                        ? ContentType.parse(file.getMimeType()) : ContentType.DEFAULT_BINARY;
                ViewableFileBody fileBody = new ViewableFileBody(resolvedFile, contentType, file.getName());
                fileBodies.add(fileBody);
                builder.addPart(file.getParamName(), fileBody);
            }
            entity = builder.build();
            requestData = getMultipartPreview(entity, contentEncoding, fileBodies);
        } else if (!hasArguments() && getSendFileAsPostBody()) {
            HTTPFileArg file = files[0];
            if (request.getFirstHeader(HTTPConstants.HEADER_CONTENT_TYPE) == null && StringUtilities.isNotEmpty(file.getMimeType())) {
                request.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, file.getMimeType());
            }
            entity = new PathEntity(FileServer.getFileServer().getResolvedFile(file.getPath()), null);
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
            return NON_REPEATABLE_ENTITY_PREVIEW;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        entity.writeTo(output);
        return output.toString(contentEncoding);
    }

    /**
     * Renders the multipart body for the sample result with the file contents replaced by a
     * placeholder, so that uploading a large file does not have to be held in memory just to be
     * shown in the request view.
     */
    private static String getMultipartPreview(HttpEntity entity, String contentEncoding,
            List<ViewableFileBody> fileBodies) throws IOException {
        for (ViewableFileBody fileBody : fileBodies) {
            fileBody.hideFileData = true;
        }
        try {
            return getEntityPreview(entity, contentEncoding);
        } finally {
            for (ViewableFileBody fileBody : fileBodies) {
                fileBody.hideFileData = false;
            }
        }
    }

    /**
     * File part of a multipart body that can write a placeholder instead of the file contents,
     * so the body can be rendered for the sample result without reading the file into memory.
     */
    private static final class ViewableFileBody extends FileBody {
        private static final byte[] CONTENTS_OMITTED =
                "<actual file content, not shown here>".getBytes(StandardCharsets.UTF_8); // $NON-NLS-1$

        private boolean hideFileData;

        private ViewableFileBody(File file, ContentType contentType, String filename) {
            super(file, contentType, filename);
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            if (hideFileData) {
                out.write(CONTENTS_OMITTED);
            } else {
                super.writeTo(out);
            }
        }
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

    private static void setDefaultUserAgent(org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request) {
        if (isDefaultUserAgentDisabled() || request.containsHeader(HttpHeaders.USER_AGENT)) {
            return;
        }
        request.setHeader(HttpHeaders.USER_AGENT, DEFAULT_USER_AGENT);
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

    HttpClientContext createHttpClientContext(URL url, HttpClientKey key,
            org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request) {
        HttpClientContext context = HttpClientContext.create();
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        Authorization authorization = getAuthorizationForUrl(url);
        configureTargetCredentials(url, request, credentialsProvider, authorization);
        configureProxyCredentials(key, credentialsProvider);
        boolean kerberosTarget = isKerberos(authorization);
        boolean kerberosProxy = isKerberosProxy(key);
        if (kerberosTarget || kerberosProxy) {
            configureKerberos(url, key, request, context, credentialsProvider, kerberosTarget, kerberosProxy);
        } else {
            context.setCredentialsProvider(credentialsProvider);
        }
        return context;
    }

    private Authorization getAuthorizationForUrl(URL url) {
        AuthManager authManager = getAuthManager();
        return authManager == null || url == null ? null : authManager.getAuthForURL(url);
    }

    /**
     * A proxy is authenticated with Kerberos, if the {@link AuthManager} holds a Kerberos
     * authorization for it, or if it has no credentials configured at all. Enterprise proxies
     * commonly challenge with {@code Proxy-Authenticate: Negotiate}, which can only be answered
     * with the Kerberos ticket of the JMeter user, as JMeter has no place to configure proxy
     * credentials for a negotiation based scheme.
     */
    private boolean isKerberosProxy(HttpClientKey key) {
        if (!key.hasProxy) {
            return false;
        }
        return StringUtilities.isEmpty(key.proxyUser) || isKerberos(getAuthorizationForUrl(getProxyUrl(key)));
    }

    private static URL getProxyUrl(HttpClientKey key) {
        if (!key.hasProxy) {
            return null;
        }
        String scheme = StringUtilities.isEmpty(key.proxyScheme) ? HTTPConstants.PROTOCOL_HTTP : key.proxyScheme;
        try {
            return new URL(scheme, key.proxyHost, key.proxyPort, "");
        } catch (MalformedURLException e) {
            log.debug("Could not build a URL for proxy {}://{}:{}", scheme, key.proxyHost, key.proxyPort, e);
            return null;
        }
    }

    private static boolean isKerberos(Authorization authorization) {
        return authorization != null && AuthManager.Mechanism.KERBEROS.equals(authorization.getMechanism());
    }

    private static void configureTargetCredentials(URL url,
            org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request,
            BasicCredentialsProvider credentialsProvider, Authorization authorization) {
        if (authorization == null || isKerberos(authorization)) {
            // The credentials of a Kerberos authorization are used to log in to the KDC, they must
            // not be sent to the server
            return;
        }
        credentialsProvider.setCredentials(new AuthScope(url.getHost(), getPort(url)),
                new UsernamePasswordCredentials(authorization.getUser(), authorization.getPass().toCharArray()));
        if (AuthManager.Mechanism.BASIC.equals(authorization.getMechanism())) {
            request.setHeader(HttpHeaders.AUTHORIZATION, authorization.toBasicHeader());
        }
    }

    /**
     * Sets up the {@code Negotiate} and {@code Kerberos} auth schemes for a single request, as
     * HttpClient 5 neither registers them nor prefers them by default. The credentials are taken
     * from the JAAS {@link Subject} the {@link AuthManager} logged the user in with. The schemes
     * are set up for the target, for the proxy, or for both, depending on which of them is
     * covered by a Kerberos authorization.
     */
    @SuppressWarnings("deprecation") // The GSS based auth schemes of HttpClient 5 have no replacement yet
    private void configureKerberos(URL url, HttpClientKey key,
            org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request,
            HttpClientContext context, CredentialsProvider credentialsProvider,
            boolean kerberosTarget, boolean kerberosProxy) {
        KerberosConfig kerberosConfig = KerberosConfig.custom()
                .setStripPort(isStripPort(url))
                .setUseCanonicalHostname(AuthManager.USE_CANONICAL_HOST_NAME)
                .setRequestDelegCreds(KERBEROS_DELEGATE_CRED
                        ? KerberosConfig.Option.ENABLE : KerberosConfig.Option.DEFAULT)
                .build();
        DnsResolver dnsResolver = SystemDefaultDnsResolver.INSTANCE;
        context.setAuthSchemeRegistry(RegistryBuilder.<AuthSchemeFactory>create()
                .register(StandardAuthScheme.BASIC, BasicSchemeFactory.INSTANCE)
                .register(StandardAuthScheme.DIGEST, DigestSchemeFactory.INSTANCE)
                .register(StandardAuthScheme.BEARER, BearerSchemeFactory.INSTANCE)
                .register(StandardAuthScheme.SPNEGO, new SPNegoSchemeFactory(kerberosConfig, dnsResolver))
                .register(StandardAuthScheme.KERBEROS, new KerberosSchemeFactory(kerberosConfig, dnsResolver))
                .build());
        context.setCredentialsProvider(new KerberosCredentialsProvider(
                kerberosTarget ? getSubjectForUrl(url) : null,
                kerberosProxy ? getProxyHost(key) : null,
                kerberosProxy ? getSubjectForProxy(key) : null,
                credentialsProvider));
        RequestConfig requestConfig = request.getConfig() == null ? RequestConfig.DEFAULT : request.getConfig();
        RequestConfig.Builder builder = RequestConfig.copy(requestConfig);
        if (kerberosTarget) {
            builder.setTargetPreferredAuthSchemes(KERBEROS_PREFERRED_AUTH_SCHEMES);
        }
        if (kerberosProxy) {
            builder.setProxyPreferredAuthSchemes(KERBEROS_PROXY_PREFERRED_AUTH_SCHEMES);
        }
        request.setConfig(builder.build());
    }

    private static HttpHost getProxyHost(HttpClientKey key) {
        return key.hasProxy ? new HttpHost(key.proxyScheme, key.proxyHost, key.proxyPort) : null;
    }

    /**
     * IE and Firefox always strip the port from the URL before they construct the SPN, see the
     * {@code kerberos.spnego.strip_port} property.
     */
    private static boolean isStripPort(URL url) {
        if (AuthManager.STRIP_PORT) {
            return true;
        }
        int port = url.getPort();
        return port == HTTPConstants.DEFAULT_HTTP_PORT || port == HTTPConstants.DEFAULT_HTTPS_PORT;
    }

    private Subject getSubjectForUrl(URL url) {
        AuthManager authManager = getAuthManager();
        return authManager == null || url == null ? null : authManager.getSubjectForUrl(url);
    }

    private Subject getSubjectForProxy(HttpClientKey key) {
        return getSubjectForUrl(getProxyUrl(key));
    }

    private static Oid createOid(String oid) {
        try {
            return new Oid(oid);
        } catch (GSSException e) {
            log.warn("Could not create OID {}", oid, e);
            return null;
        }
    }

    /**
     * Provides the GSS credentials of a JAAS {@link Subject} to the negotiation based auth schemes.
     * The HTTP/2 transport builds the token on an I/O reactor thread, which is not covered by the
     * {@link Subject#doAs(Subject, PrivilegedExceptionAction)} the HTTP/1.1 request is executed
     * with, so the credentials have to be passed to HttpClient explicitly.
     */
    private static final class KerberosCredentialsProvider implements CredentialsProvider {

        private final Subject targetSubject;
        private final HttpHost proxy;
        private final Subject proxySubject;
        private final CredentialsProvider delegate;
        private final Map<String, Credentials> credentialsCache = new HashMap<>();

        private KerberosCredentialsProvider(Subject targetSubject, HttpHost proxy, Subject proxySubject,
                CredentialsProvider delegate) {
            this.targetSubject = targetSubject;
            this.proxy = proxy;
            this.proxySubject = proxySubject;
            this.delegate = delegate;
        }

        @Override
        @SuppressWarnings("deprecation") // The GSS based auth schemes of HttpClient 5 have no replacement yet
        public Credentials getCredentials(AuthScope authScope, HttpContext context) {
            String schemeName = authScope == null ? null : authScope.getSchemeName();
            if (StandardAuthScheme.SPNEGO.equalsIgnoreCase(schemeName)) {
                return getKerberosCredentials(schemeName, SPNEGO_OID, authScope);
            }
            if (StandardAuthScheme.KERBEROS.equalsIgnoreCase(schemeName)) {
                return getKerberosCredentials(schemeName, KERBEROS_OID, authScope);
            }
            return delegate.getCredentials(authScope, context);
        }

        private Credentials getKerberosCredentials(String schemeName, Oid oid, AuthScope authScope) {
            boolean forProxy = isProxy(authScope);
            Subject subject = forProxy ? proxySubject : targetSubject;
            return credentialsCache.computeIfAbsent(schemeName + (forProxy ? "@proxy" : "@target"),
                    name -> createKerberosCredentials(subject, oid));
        }

        private boolean isProxy(AuthScope authScope) {
            return proxy != null && proxy.getHostName().equalsIgnoreCase(authScope.getHost())
                    && (authScope.getPort() <= 0 || authScope.getPort() == proxy.getPort());
        }

        @SuppressWarnings("deprecation") // KerberosCredentials is deprecated without a replacement
        private static Credentials createKerberosCredentials(Subject subject, Oid oid) {
            if (subject == null || oid == null) {
                return USE_JAAS_CREDENTIALS;
            }
            try {
                GSSCredential gssCredential = Subject.doAs(subject,
                        (PrivilegedExceptionAction<GSSCredential>) () -> GSSManager.getInstance()
                                .createCredential(null, GSSCredential.DEFAULT_LIFETIME, oid,
                                        GSSCredential.INITIATE_ONLY));
                return new KerberosCredentials(gssCredential);
            } catch (PrivilegedActionException e) {
                log.warn("Could not obtain the GSS credentials of subject {}, "
                        + "falling back to the credentials of the current context", subject, e.getException());
                return USE_JAAS_CREDENTIALS;
            }
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

    private void readResponse(ClassicHttpResponse response, HTTPSampleResult result, HttpContext context)
            throws IOException {
        Header contentType = response.getFirstHeader(HTTPConstants.HEADER_CONTENT_TYPE);
        if (contentType != null) {
            result.setContentType(contentType.getValue());
            result.setEncodingAndType(contentType.getValue());
        }
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            byte[] body = readResponse(result, entity.getContent(), entity.getContentLength());
            result.setResponseData(body);
            // The HTTP/2 transport counts the body while it streams it, so a truncated response
            // still reports the number of bytes the server actually sent. For HTTP/1.1 a compressed
            // body is counted before it is decoded, so the reported size is the one that crossed the
            // wire, like HTTPHC4Impl and HTTPJavaImpl report it
            Long bodySize = (Long) context.getAttribute(CONTEXT_ATTRIBUTE_RESPONSE_BODY_SIZE);
            CountingEntity bodyCounter =
                    (CountingEntity) context.getAttribute(CONTEXT_ATTRIBUTE_RESPONSE_BODY_COUNTER);
            result.setBodySize(bodySize != null ? bodySize
                    : bodyCounter != null ? bodyCounter.getBytesRead() : (long) body.length);
        }
    }

    private void updateResult(ClassicHttpResponse response,
            org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request, HTTPSampleResult result) {
        result.setRequestHeaders(getRequestHeaders(request));
        result.setSentBytes(calculateSentBytes(request));
        int statusCode = response.getCode();
        result.setResponseCode(Integer.toString(statusCode));
        result.setResponseMessage(response.getReasonPhrase());
        result.setSuccessful(isSuccessCode(statusCode));
        result.setResponseHeaders(getResponseHeaders(response));
        // Approximated the way HTTPHC4Impl does it, so both implementations report the same size:
        // the condensed headers (without \r), a \r per header, a \r for the status line and the
        // final \r\n before the body
        long headerBytes =
                (long) result.getResponseHeaders().length()
                + response.getHeaders().length
                + 1L
                + 2L;
        result.setHeadersSize((int) headerBytes);
        if (result.isRedirect()) {
            Header location = response.getFirstHeader(HTTPConstants.HEADER_LOCATION);
            if (location == null) { // HTTP protocol violation, but avoids NPE
                throw new IllegalArgumentException(
                        "Missing location header in redirect for " + request.getMethod() + " " + request.getRequestUri());
            }
            result.setRedirectLocation(location.getValue());
        }
    }

    /**
     * When HttpClient followed the redirects on its own, the sampled URL is the one of the last
     * request, not the one the sampler started with. The cookie and the cache manager as well as the
     * listeners need the effective URL.
     */
    private void updateUrlAfterAutoRedirects(URL url, HttpClientContext context, HTTPSampleResult result) {
        if (!getAutoRedirects()) {
            return;
        }
        RedirectLocations redirectLocations = context.getRedirectLocations();
        if (redirectLocations == null || redirectLocations.size() == 0) {
            return;
        }
        RouteInfo route = context.getHttpRoute();
        HttpHost target = route != null ? route.getTargetHost() : null;
        try {
            result.setURL(URIUtils.resolve(url.toURI(), target, redirectLocations.getAll()).toURL());
        } catch (URISyntaxException | MalformedURLException e) {
            log.warn("Could not resolve the effective URL after redirects for {}", url, e);
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
        String versionStr = version != null ? version.toString() : HTTPConstants.HTTP_VERSION_1_1;

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

    private static boolean isDefaultUserAgentDisabled() {
        return JMeterUtils.getPropDefault(DISABLE_DEFAULT_UA_PROPERTY, false);
    }

    private static CloseableHttpClient createClient(HttpClientKey key) {
        org.apache.hc.client5.http.impl.classic.HttpClientBuilder builder = HttpClients.custom().disableAutomaticRetries();
        if (isDefaultUserAgentDisabled()) {
            builder.disableDefaultUserAgent();
        }
        PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();
        connectionManagerBuilder.setTlsSocketStrategy(createTlsSocketStrategy());
        connectionManagerBuilder.setDefaultTlsConfig(TlsConfig.custom()
                .setVersionPolicy(key.httpVersionPolicy)
                .build());
        if (key.dnsCacheManager != null) {
            connectionManagerBuilder.setDnsResolver(createDnsResolver(key.dnsCacheManager));
        }
        ConnectionConfig.Builder connectionConfig = ConnectionConfig.custom()
                .setValidateAfterInactivity(TimeValue.ofMilliseconds(VALIDATE_AFTER_INACTIVITY));
        if (key.connectTimeout > 0) {
            connectionConfig.setConnectTimeout(Timeout.ofMilliseconds(key.connectTimeout));
        }
        connectionManagerBuilder.setDefaultConnectionConfig(connectionConfig.build());
        builder.setConnectionManager(new ConnectTimeMeasuringConnectionManager(connectionManagerBuilder.build()));
        builder.setRoutePlanner(createRoutePlanner(key));
        return builder.disableContentCompression()
                .addExecInterceptorFirst("response-content-encoding", RESPONSE_CONTENT_ENCODING)
                .build();
    }

    private static CloseableHttpAsyncClient createHttp2Client(HttpClientKey key) {
        boolean multiplexing = HTTP_2_MULTIPLEXING && MESSAGE_MULTIPLEXING_SETTER != null;
        HttpAsyncClientBuilder builder = HttpAsyncClients.custom()
                .disableAutomaticRetries()
                // HttpClient 5.6 added transparent content compression to the async transport. JMeter decodes
                // the response itself, so the HTTP/2 sampler reports the same headers as the HTTP/1.1 one and
                // does not depend on the optional codec libraries HttpClient detects on the classpath.
                .disableContentCompression()
                .setH2Config(HTTP_2_CONFIG)
                .setRoutePlanner(createRoutePlanner(key));
        if (multiplexing) {
            // A connection bound to a user token cannot be shared, and HTTP/2 has no connection scoped state anyway
            builder.disableConnectionState();
        }
        if (isDefaultUserAgentDisabled()) {
            // HttpAsyncClientBuilder has no disableDefaultUserAgent, so the generated header is dropped again
            builder.addRequestInterceptorFirst(RECORD_USER_AGENT_PRESENCE);
            builder.addRequestInterceptorLast(REMOVE_DEFAULT_USER_AGENT);
        }
        PoolingAsyncClientConnectionManagerBuilder connectionManagerBuilder = PoolingAsyncClientConnectionManagerBuilder.create();
        connectionManagerBuilder.setTlsStrategy(createTlsStrategy());
        connectionManagerBuilder.setDefaultTlsConfig(TlsConfig.custom()
                .setVersionPolicy(key.httpVersionPolicy)
                .build());
        if (multiplexing) {
            enableMessageMultiplexing(connectionManagerBuilder);
        }
        connectionManagerBuilder.setPoolConcurrencyPolicy(PoolConcurrencyPolicy.LAX);
        connectionManagerBuilder.setMaxConnPerRoute(HTTP_2_MAX_CONNECTIONS_PER_ROUTE);
        if (key.dnsCacheManager != null) {
            connectionManagerBuilder.setDnsResolver(createDnsResolver(key.dnsCacheManager));
        }
        // Without this an HTTP/2 connection is leased straight out of the pool, and a connection the
        // server closed while the thread was idle only shows up when the request is written to it,
        // which fails the sample with a ConnectionClosedException. The check sends an HTTP/2 PING
        // and replaces the connection if the peer does not answer.
        ConnectionConfig.Builder connectionConfig = ConnectionConfig.custom()
                .setValidateAfterInactivity(TimeValue.ofMilliseconds(VALIDATE_AFTER_INACTIVITY));
        if (key.connectTimeout > 0) {
            connectionConfig.setConnectTimeout(Timeout.ofMilliseconds(key.connectTimeout));
        }
        connectionManagerBuilder.setDefaultConnectionConfig(connectionConfig.build());
        builder.setConnectionManager(new ConnectTimeMeasuringAsyncConnectionManager(connectionManagerBuilder.build()));
        CloseableHttpAsyncClient asyncClient = builder.build();
        asyncClient.start();
        return asyncClient;
    }

    /**
     * Looks up {@code PoolingAsyncClientConnectionManagerBuilder#setMessageMultiplexing(boolean)},
     * which is only available from HttpClient 5.5 onwards. JMeter still runs with older HttpClient
     * versions, where HTTP/2 requests just cannot share a connection.
     */
    private static Method findMessageMultiplexingSetter() {
        try {
            return PoolingAsyncClientConnectionManagerBuilder.class.getMethod("setMessageMultiplexing", boolean.class);
        } catch (NoSuchMethodException e) {
            log.info("HTTP/2 message multiplexing is unavailable, HttpClient 5.5 or later is required");
            return null;
        }
    }

    private static void enableMessageMultiplexing(PoolingAsyncClientConnectionManagerBuilder connectionManagerBuilder) {
        try {
            MESSAGE_MULTIPLEXING_SETTER.invoke(connectionManagerBuilder, true);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("Could not enable HTTP/2 message multiplexing", e);
        }
    }

    private ClassicHttpResponse executeHttp2(CloseableHttpAsyncClient client,
            org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request, HttpClientContext context)
            throws IOException {
        RequestBody requestBody = createRequestBody(request);
        try {
            Future<ClassicHttpResponse> responseFuture = client.execute(requestBody.producer,
                    new LatencyMeasuringResponseConsumer(
                            (SampleResult) context.getAttribute(CONTEXT_ATTRIBUTE_SAMPLER_RESULT), context,
                            getMaxBytesToStore()),
                    context, null);
            currentResponseFuture = responseFuture;
            if (interruptRequested) {
                responseFuture.cancel(true);
            }
            try {
                return responseFuture.get();
            } catch (InterruptedException e) {
                responseFuture.cancel(true);
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while executing HTTP/2 request", e);
            } catch (ExecutionException e) {
                throw new IOException("Could not execute HTTP/2 request", e.getCause());
            } finally {
                currentResponseFuture = null;
            }
        } finally {
            // The exchange, including any redirect HttpClient followed itself, is over by now, so
            // the spilled request body is not needed anymore
            deleteQuietly(requestBody.temporaryFile);
        }
    }

    /**
     * Number of response body bytes worth keeping, {@code 0} meaning all of them. This has to be
     * determined on the sampler thread, as the recording flag lives in a thread local the HTTP/2
     * I/O reactor threads do not see. Truncation is also given up when an MD5 digest is requested,
     * because that is computed over the whole body.
     */
    private long getMaxBytesToStore() {
        if (testElement.useMD5() || JMeterContextService.getContext().isRecording()) {
            return 0;
        }
        return Math.max(0, HTTPSamplerBase.getMaxBytesToStorePerRequest());
    }

    /** A request body producer together with the temporary file it streams from, if any. */
    private static final class RequestBody {
        private final BasicRequestProducer producer;
        private final Path temporaryFile;

        private RequestBody(BasicRequestProducer producer, Path temporaryFile) {
            this.producer = producer;
            this.temporaryFile = temporaryFile;
        }
    }

    /**
     * Builds the producer that streams the request body to the server. A file that is sent as the
     * body is streamed straight from disk, other bodies are buffered in memory and only spilled to
     * a temporary file when they exceed {@link #HTTP_2_BODY_SPILL_THRESHOLD}, so that a large
     * upload is never materialized as a byte array.
     */
    private static RequestBody createRequestBody(
            org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request) throws IOException {
        HttpEntity entity = request.getEntity();
        if (entity == null) {
            return new RequestBody(new BasicRequestProducer(request, null), null);
        }

        // A null content type leaves the Content-Type header to the request, like the HTTP/1.1 transport does
        ContentType contentType = entity.getContentType() == null ? null
                : ContentType.parse(entity.getContentType());
        if (entity instanceof PathEntity pathEntity) {
            return new RequestBody(
                    new BasicRequestProducer(request, new FileEntityProducer(pathEntity.getFile(), contentType)), null);
        }

        SpillOutputStream spillOutput =
                new SpillOutputStream(HTTP_2_BODY_SPILL_THRESHOLD, "jmeter-http2-request-", ".tmp");
        try {
            entity.writeTo(spillOutput);
            spillOutput.close();
            if (spillOutput.isSpilled()) {
                Path temporaryFile = spillOutput.getTempFile();
                return new RequestBody(new BasicRequestProducer(request,
                        new FileEntityProducer(temporaryFile.toFile(), contentType)), temporaryFile);
            }
            return new RequestBody(new BasicRequestProducer(request,
                    new BasicAsyncEntityProducer(spillOutput.toByteArray(), contentType)), null);
        } catch (IOException e) {
            spillOutput.releaseResources();
            throw e;
        }
    }

    /**
     * Streams the HTTP/2 response body and captures the latency when the final response headers
     * arrive.
     * <p>
     * The body has to be buffered until the exchange is complete, because the asynchronous
     * transport hands it to the sampler thread as a whole. When
     * {@code httpsampler.max_bytes_to_store_per_request} limits what is kept, the body is truncated
     * right here, so the bytes beyond the limit are counted and dropped instead of being buffered
     * only for {@link HTTPSamplerBase#readResponse} to discard them. That gives the same peak heap
     * usage as the HTTP/1.1 transport, which reads and truncates the body as it arrives.
     */
    private static final class LatencyMeasuringResponseConsumer extends AbstractBinResponseConsumer<ClassicHttpResponse> {
        /** Buffer size used when the size of the body is unknown. */
        private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
        /**
         * Upper bound for the buffer that is allocated upfront from the announced content length,
         * so that a bogus or hostile {@code Content-Length} cannot exhaust the heap before a single
         * byte of the body has been received. A larger body grows the buffer as it arrives.
         */
        private static final int MAX_INITIAL_BUFFER_SIZE = 16 * 1024 * 1024;

        private final SampleResult sampleResult;
        private final HttpContext context;
        /** Bytes to keep of the body, {@code 0} keeps all of them. */
        private final long maxBytesToStore;
        private BasicClassicHttpResponse response;
        private ByteArrayOutputStream body;
        private ContentType contentType;
        private long storeLimit;
        private long totalBodyBytes;

        private LatencyMeasuringResponseConsumer(SampleResult sampleResult, HttpContext context,
                long maxBytesToStore) {
            this.sampleResult = sampleResult;
            this.context = context;
            this.maxBytesToStore = maxBytesToStore;
        }

        @Override
        protected void start(HttpResponse response, ContentType contentType) throws HttpException, IOException {
            sampleResult.latencyEnd();
            context.setAttribute(CONTEXT_ATTRIBUTE_RESPONSE_LATENCY, sampleResult.getLatency());
            this.response = new BasicClassicHttpResponse(response.getCode(), response.getReasonPhrase());
            this.response.setVersion(response.getVersion());
            for (Header header : response.getHeaders()) {
                this.response.addHeader(header);
            }
            this.contentType = contentType;
            // An encoded body has to be kept in full, as it can only be decoded from its first byte on
            storeLimit = response.containsHeader(HttpHeaders.CONTENT_ENCODING) ? 0 : maxBytesToStore;
            // The body is buffered whether or not the server sent a Content-Type header, as a
            // response without one still has to be reported with its body
            body = new ByteArrayOutputStream(initialBufferSize(response));
        }

        /**
         * Size of the buffer the body is collected in, derived from the announced content length
         * and the number of bytes that are kept of it, to avoid repeatedly growing and copying the
         * buffer while a large body is received.
         */
        private int initialBufferSize(HttpResponse response) {
            long expected = -1;
            String contentLength = getHeaderValue(response, HttpHeaders.CONTENT_LENGTH);
            if (contentLength != null) {
                try {
                    expected = Long.parseLong(contentLength.trim());
                } catch (NumberFormatException e) {
                    log.debug("Ignoring malformed Content-Length header {}", contentLength);
                }
            }
            if (storeLimit > 0) {
                expected = expected < 0 ? storeLimit : Math.min(expected, storeLimit);
            }
            if (expected <= DEFAULT_BUFFER_SIZE) {
                return DEFAULT_BUFFER_SIZE;
            }
            return (int) Math.min(expected, MAX_INITIAL_BUFFER_SIZE);
        }

        /**
         * Amount of data the consumer accepts before the flow control window is replenished. This
         * is the HTTP/2 window the connection was configured with, as the body is streamed straight
         * into the buffer and a smaller increment would only throttle large downloads.
         */
        @Override
        protected int capacityIncrement() {
            return HTTP_2_INITIAL_WINDOW_SIZE;
        }

        @Override
        protected void data(ByteBuffer src, boolean endOfStream) throws IOException {
            int available = src.remaining();
            totalBodyBytes += available;
            int toStore = available;
            if (storeLimit > 0) {
                long remainingCapacity = storeLimit - (totalBodyBytes - available);
                toStore = (int) Math.max(0, Math.min(available, remainingCapacity));
            }
            if (body != null && toStore > 0) {
                if (src.hasArray()) {
                    body.write(src.array(), src.arrayOffset() + src.position(), toStore);
                } else {
                    byte[] bytes = new byte[toStore];
                    src.duplicate().get(bytes);
                    body.write(bytes, 0, toStore);
                }
            }
            src.position(src.limit());
        }

        @Override
        protected ClassicHttpResponse buildResult() {
            if (body != null) {
                response.setEntity(new ByteArrayEntity(body.toByteArray(), contentType,
                        getHeaderValue(response, HttpHeaders.CONTENT_ENCODING)));
                body = null;
            }
            // The body may have been truncated, so report what the server actually sent
            context.setAttribute(CONTEXT_ATTRIBUTE_RESPONSE_BODY_SIZE, totalBodyBytes);
            return decompressResponse(response, context);
        }

        @Override
        public void releaseResources() {
            body = null;
        }
    }

    private static final class CountingEntity extends HttpEntityWrapper {
        private CountingInputStream inputStream;

        private CountingEntity(HttpEntity entity) {
            super(entity);
        }

        @Override
        public CountingInputStream getContent() throws IOException {
            inputStream = new CountingInputStream(super.getContent());
            return inputStream;
        }

        private long getBytesRead() {
            return inputStream == null ? 0 : inputStream.getBytesRead();
        }
    }

    private static class PathEntity extends FileEntity {
        private final File file;

        PathEntity(File file, ContentType contentType) {
            super(file, contentType);
            this.file = file;
        }

        File getFile() {
            return file;
        }
    }

    private static String getHeaderValue(HttpResponse response, String name) {
        Header header = response.getFirstHeader(name);
        return header == null ? null : header.getValue();
    }

    private static void deleteQuietly(Path file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                log.warn("Could not delete temporary HTTP/2 body file {}", file, e);
            }
        }
    }

    private static DefaultRoutePlanner createRoutePlanner(HttpClientKey key) {
        return new DefaultRoutePlanner(null) {
            @Override
            protected HttpHost determineProxy(HttpHost target, org.apache.hc.core5.http.protocol.HttpContext context) {
                return getProxyHost(key);
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

    HttpClientKey createHttpClientKey(URL url) throws IOException {
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
        HttpVersionPolicy httpVersionPolicy = getHttpVersionPolicy(testElement.getHttpVersion(), HTTP_VERSION,
                url.getProtocol());
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
        if (HTTPConstants.HTTP_VERSION_2_STRICT.equalsIgnoreCase(httpVersion)) {
            return HttpVersionPolicy.FORCE_HTTP_2;
        }
        return HTTPConstants.HTTP_VERSION_2.equals(httpVersion)
                ? HttpVersionPolicy.NEGOTIATE : HttpVersionPolicy.FORCE_HTTP_1;
    }

    /**
     * Determines the version policy for a request to the given scheme. Plain HTTP does not support
     * ALPN, so HTTP/2 can only be used over {@code http://} when the client assumes that the server
     * speaks HTTP/2 (h2c with prior knowledge). {@code HTTP/2 Strict} keeps its policy for every
     * scheme, as it requires HTTP/2 either way.
     */
    static HttpVersionPolicy getHttpVersionPolicy(String samplerHttpVersion, String defaultHttpVersion, String scheme) {
        HttpVersionPolicy policy = getHttpVersionPolicy(samplerHttpVersion, defaultHttpVersion);
        if (policy == HttpVersionPolicy.NEGOTIATE && HTTP_2_PRIOR_KNOWLEDGE
                && !HTTPConstants.PROTOCOL_HTTPS.equalsIgnoreCase(scheme)) {
            return HttpVersionPolicy.FORCE_HTTP_2;
        }
        return policy;
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
        Future<?> responseFuture = currentResponseFuture;
        if (request != null || responseFuture != null) {
            interruptRequested = true;
        }
        currentRequest = null;
        currentResponseFuture = null;
        if (request != null) {
            request.cancel();
        }
        if (responseFuture != null) {
            responseFuture.cancel(true);
        }
        return request != null || responseFuture != null;
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

    static final class HttpClientKey {
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
