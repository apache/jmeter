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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.StandardAuthScheme;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

class TestHTTPHC5Features {

    @AfterEach
    void closeClientsOfThisThread() {
        // the clients are cached per thread and keep their connections and I/O reactor threads alive
        HTTPHC5Impl.closeThreadLocalClients();
    }

    @Test
    void usesHttpClientVersionWhenSamplerVersionIsEmpty() {
        assertEquals(HttpVersionPolicy.NEGOTIATE, HTTPHC5Impl.getHttpVersionPolicy("", "HTTP/2"));
    }

    @Test
    void usesSamplerHttpVersionWhenSpecified() {
        assertEquals(HttpVersionPolicy.FORCE_HTTP_1, HTTPHC5Impl.getHttpVersionPolicy("HTTP/1.1", "HTTP/2"));
        assertEquals(HttpVersionPolicy.NEGOTIATE, HTTPHC5Impl.getHttpVersionPolicy("HTTP/2", "HTTP/1.1"));
    }

    @Test
    void defaultsToHttp11ForUnsupportedHttpVersion() {
        assertEquals(HttpVersionPolicy.FORCE_HTTP_1, HTTPHC5Impl.getHttpVersionPolicy("HTTP/3", "HTTP/2"));
    }

    @Test
    void negotiatesHttp2OverTls() {
        assertEquals(HttpVersionPolicy.NEGOTIATE, HTTPHC5Impl.getHttpVersionPolicy("HTTP/2", "HTTP/1.1", "https"));
    }

    @Test
    void doesNotUsePriorKnowledgeForCleartextByDefault() {
        assertEquals(HttpVersionPolicy.NEGOTIATE, HTTPHC5Impl.getHttpVersionPolicy("HTTP/2", "HTTP/1.1", "http"));
    }

    @Test
    void requiresHttp2ForStrictHttp2RegardlessOfScheme() {
        assertEquals(HttpVersionPolicy.FORCE_HTTP_2,
                HTTPHC5Impl.getHttpVersionPolicy("HTTP/2 Strict", "HTTP/1.1"));
        assertEquals(HttpVersionPolicy.FORCE_HTTP_2,
                HTTPHC5Impl.getHttpVersionPolicy("HTTP/2 Strict", "HTTP/1.1", "https"));
        assertEquals(HttpVersionPolicy.FORCE_HTTP_2,
                HTTPHC5Impl.getHttpVersionPolicy("HTTP/2 Strict", "HTTP/1.1", "http"));
        assertEquals(HttpVersionPolicy.FORCE_HTTP_2,
                HTTPHC5Impl.getHttpVersionPolicy("", "HTTP/2 Strict", "https"));
    }

    @Test
    void multiplexesConcurrentRequestsOverASingleHttp2Connection() throws Exception {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(false));
        server.start();
        try (TcpRelay relay = new TcpRelay(server.httpsPort())) {
            server.stubFor(get(urlEqualTo("/multiplexed"))
                    .willReturn(aResponse().withStatus(200).withFixedDelay(200)));
            URL url = new URL("https://localhost:" + relay.getPort() + "/multiplexed");
            // warm up, so the connection that gets shared by the concurrent samples is already established
            HTTPSamplerBase warmUpSampler = newSampler();
            warmUpSampler.setHttpVersion("HTTP/2");
            assertEquals("200", warmUpSampler.sample(url, HTTPConstants.GET, false, 1).getResponseCode());
            assertEquals(1, relay.getAcceptedConnections(), "the warm up should have opened one connection");

            int requests = 4;
            Callable<HTTPSampleResult> sample = () -> {
                HTTPSamplerBase sampler = newSampler();
                sampler.setHttpVersion("HTTP/2");
                return sampler.sample(url, HTTPConstants.GET, false, 1);
            };
            ExecutorService executor = Executors.newFixedThreadPool(requests);
            try {
                List<Future<HTTPSampleResult>> results = new ArrayList<>();
                results.add(executor.submit(sample));
                // let the first sample lease the pooled connection before the others ask for one,
                // otherwise they race for it and the pool opens a second connection
                Thread.sleep(50);
                for (int i = 1; i < requests; i++) {
                    results.add(executor.submit(sample));
                }
                for (Future<HTTPSampleResult> result : results) {
                    HTTPSampleResult sampleResult = result.get(30, TimeUnit.SECONDS);
                    assertEquals("200", sampleResult.getResponseCode());
                    assertEquals("HTTP/2", sampleResult.getResponseHeaders().substring(0, "HTTP/2".length()));
                }
            } finally {
                executor.shutdownNow();
            }
            assertEquals(1, relay.getAcceptedConnections(),
                    "the concurrent samples should have been multiplexed over the established connection "
                            + "instead of opening a connection each");
        } finally {
            server.stop();
        }
    }

    @Test
    void appliesHttp2ProtocolSettings() {
        H2Config config = HTTPHC5Impl.createHttp2Config();

        assertTrue(config.isCompressionEnabled(), "HPACK header compression");
        assertEquals(HTTPHC5Impl.DEFAULT_HTTP_2_INITIAL_WINDOW_SIZE, config.getInitialWindowSize(),
                "the 64 kB default of HTTP/2 throttles a download to that window per round trip");
        assertFalse(config.isPushEnabled(), "server push is dropped by JMeter, so it must not be announced");
        // the settings JMeter has no opinion on are left at the defaults of HttpClient
        assertEquals(H2Config.DEFAULT.getHeaderTableSize(), config.getHeaderTableSize());
        assertEquals(H2Config.DEFAULT.getMaxConcurrentStreams(), config.getMaxConcurrentStreams());
        assertEquals(H2Config.DEFAULT.getMaxFrameSize(), config.getMaxFrameSize());
    }

    @Test
    void overridesHttp2ProtocolSettingsWithJMeterProperties() {
        Properties properties = new Properties();
        properties.setProperty("httpclient5.h2.header_table_size", "4096");
        properties.setProperty("httpclient5.h2.header_compression", "false");
        properties.setProperty("httpclient5.h2.max_concurrent_streams", "42");
        properties.setProperty("httpclient5.h2.initial_window_size", "131072");
        properties.setProperty("httpclient5.h2.max_frame_size", "32768");
        properties.setProperty("httpclient5.h2.push_enabled", "true");

        H2Config config = HTTPHC5Impl.createHttp2Config(properties);

        assertEquals(4096, config.getHeaderTableSize());
        assertFalse(config.isCompressionEnabled());
        assertEquals(42, config.getMaxConcurrentStreams());
        assertEquals(131072, config.getInitialWindowSize());
        assertEquals(32768, config.getMaxFrameSize());
        assertTrue(config.isPushEnabled());
    }

    @Test
    void keepsHttp2ProtocolSettingsWhenAPropertyIsNotANumber() {
        Properties properties = new Properties();
        properties.setProperty("httpclient5.h2.initial_window_size", "not a number");

        assertEquals(HTTPHC5Impl.DEFAULT_HTTP_2_INITIAL_WINDOW_SIZE,
                HTTPHC5Impl.createHttp2Config(properties).getInitialWindowSize());
    }

    @Test
    void usesHttp2WhenSelected() throws Exception {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(false));
        try {
            server.start();
            server.stubFor(get(urlEqualTo("/http2")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");

            HTTPSampleResult result = sampler.sample(
                    new URL("https://localhost:" + server.httpsPort() + "/http2"), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            assertEquals("HTTP/2", result.getResponseHeaders().substring(0, "HTTP/2".length()));
        } finally {
            server.stop();
        }
    }

    @Test
    void uploadsMultipartFileOverHttp2WithoutBufferingItInMemory() throws Exception {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(false));
        Path upload = Files.createTempFile("jmeter-http2-upload-", ".bin");
        try {
            Files.write(upload, new byte[1_000_000]);
            server.start();
            server.stubFor(post(urlEqualTo("/upload")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");
            sampler.setMethod(HTTPConstants.POST);
            sampler.setDoMultipart(true);
            sampler.setHTTPFiles(new HTTPFileArg[] {
                    new HTTPFileArg(upload.toString(), "upload", "application/octet-stream") });

            HTTPSampleResult result = sampler.sample(
                    new URL("https://localhost:" + server.httpsPort() + "/upload"), HTTPConstants.POST, false, 1);

            assertEquals("200", result.getResponseCode());
            assertEquals("HTTP/2", result.getResponseHeaders().substring(0, "HTTP/2".length()));
            assertTrue(result.getSentBytes() > 1_000_000,
                    () -> "the whole file should have been sent, but only " + result.getSentBytes() + " bytes were");
            // The request view must not hold the file contents, otherwise a large upload is in heap twice
            assertTrue(result.getQueryString().contains("<actual file content, not shown here>"),
                    () -> "file contents should be omitted from the request view: " + result.getQueryString());
            assertTrue(result.getQueryString().length() < 10_000,
                    () -> "request view should not contain the file, but is "
                            + result.getQueryString().length() + " characters long");
            server.verify(postRequestedFor(urlEqualTo("/upload")));
        } finally {
            Files.deleteIfExists(upload);
            server.stop();
        }
    }

    @Test
    void resendsSpilledHttp2RequestBodyOnRedirect() throws Exception {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(false));
        Path upload = Files.createTempFile("jmeter-http2-upload-", ".bin");
        try {
            Files.write(upload, new byte[1_000_000]);
            server.start();
            server.stubFor(post(urlEqualTo("/redirect")).willReturn(
                    aResponse().withStatus(307).withHeader(HTTPConstants.HEADER_LOCATION, "/upload")));
            server.stubFor(post(urlEqualTo("/upload")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");
            sampler.setMethod(HTTPConstants.POST);
            sampler.setAutoRedirects(true);
            sampler.setDoMultipart(true);
            sampler.setHTTPFiles(new HTTPFileArg[] {
                    new HTTPFileArg(upload.toString(), "upload", "application/octet-stream") });

            HTTPSampleResult result = sampler.sample(
                    new URL("https://localhost:" + server.httpsPort() + "/redirect"), HTTPConstants.POST, false, 1);

            assertEquals("200", result.getResponseCode(), result.getResponseMessage());
            server.verify(postRequestedFor(urlEqualTo("/upload")));
        } finally {
            Files.deleteIfExists(upload);
            server.stop();
        }
    }

    @Test
    void readsHttp2ResponseWithoutContentTypeHeader() throws Exception {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(false));
        try {
            server.start();
            server.stubFor(get(urlEqualTo("/no-content-type"))
                    .willReturn(aResponse().withStatus(200).withBody("no content type here")));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");

            HTTPSampleResult result = sampler.sample(
                    new URL("https://localhost:" + server.httpsPort() + "/no-content-type"),
                    HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            assertEquals("no content type here", result.getResponseDataAsString());
        } finally {
            server.stop();
        }
    }

    @Test
    void readsHttp2ResponseThatIsLargerThanTheInMemoryBuffer() throws Exception {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(false));
        try {
            server.start();
            byte[] body = new byte[2_000_000];
            for (int i = 0; i < body.length; i++) {
                body[i] = (byte) i;
            }
            server.stubFor(get(urlEqualTo("/large")).willReturn(aResponse().withStatus(200).withBody(body)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");

            HTTPSampleResult result = sampler.sample(
                    new URL("https://localhost:" + server.httpsPort() + "/large"), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            assertArrayEquals(body, result.getResponseData());
            assertEquals(body.length, result.getBodySizeAsLong());
        } finally {
            server.stop();
        }
    }

    @Test
    void interruptsPendingHttp2Request() throws Exception {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(false));
        server.start();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            server.stubFor(get(urlEqualTo("/interrupted")).willReturn(aResponse().withFixedDelay(10_000)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");
            HTTPHC5Impl implementation = new HTTPHC5Impl(sampler);

            Future<?> sample = executor.submit(() -> implementation.sample(
                    new URL("https://localhost:" + server.httpsPort() + "/interrupted"),
                    HTTPConstants.GET, false, 1));

            boolean interrupted = false;
            for (int attempt = 0; attempt < 100 && !interrupted; attempt++) {
                interrupted = implementation.interrupt();
                if (!interrupted) {
                    Thread.sleep(10);
                }
            }

            assertTrue(interrupted, "the HTTP/2 response future should be cancellable");
            sample.get(5, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
            server.stop();
        }
    }

    @Test
    void recordsHttp2LatencyBeforeDribbledResponseBodyCompletes() throws Exception {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(false));
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/http2dribbled"))
                    .willReturn(aResponse().withStatus(200).withBody(new byte[10_000]).withChunkedDribbleDelay(5, 500)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");

            HTTPSampleResult result = sampler.sample(
                    new URL("https://localhost:" + server.httpsPort() + "/http2dribbled"), HTTPConstants.GET, false, 1);

            assertTrue(result.getTime() - result.getLatency() >= 250,
                    "HTTP/2 latency should exclude the dribbled response body");
        } finally {
            server.stop();
        }
    }

    @Test
    void fallsBackToHttp11WhenServerDoesNotSupportHttp2() throws Exception {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(true));
        try {
            server.start();
            server.stubFor(get(urlEqualTo("/fallback")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");

            HTTPSampleResult result = sampler.sample(
                    new URL("https://localhost:" + server.httpsPort() + "/fallback"), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            assertEquals("HTTP/1.1", result.getResponseHeaders().substring(0, "HTTP/1.1".length()));
        } finally {
            server.stop();
        }
    }

    @Test
    void usesHttp2WithProxy() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/http2proxy")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");
            sampler.setProxyHost("localhost");
            sampler.setProxyPortInt(Integer.toString(server.port()));

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/http2proxy")), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
        } finally {
            server.stop();
        }
    }

    @Test
    void usesHttp11WhenSelected() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/http11")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/1.1");

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/http11")), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            assertEquals("HTTP/1.1", result.getResponseHeaders().substring(0, "HTTP/1.1".length()));
        } finally {
            server.stop();
        }
    }

    @Test
    void usesJMeterSslContextForHttp11() throws Exception {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(true));
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/https11")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();

            HTTPSampleResult result = sampler.sample(
                    new URL("https://localhost:" + server.httpsPort() + "/https11"), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            assertEquals("HTTP/1.1", result.getResponseHeaders().substring(0, "HTTP/1.1".length()));
        } finally {
            server.stop();
        }
    }

    @Test
    void latencyDoesNotExceedElapsedTimeWhenResponseBodyIsDribbled() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/dribbled"))
                    .willReturn(aResponse().withStatus(200).withBody(new byte[10_000]).withChunkedDribbleDelay(5, 500)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/1.1");

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/dribbled")), HTTPConstants.GET, false, 1);

            assertTrue(result.getLatency() <= result.getTime(),
                    "latency should not exceed elapsed time");
        } finally {
            server.stop();
        }
    }

    @Test
    void setsSentBytesCorrectlyForGetRequest() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/sentBytesGet")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/1.1");

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/sentBytesGet")), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            assertEquals(wireSize(singleRequest(server, "/sentBytesGet")), result.getSentBytes(),
                    "sentBytes should be the number of bytes the server received");
        } finally {
            server.stop();
        }
    }

    @Test
    void setsSentBytesCorrectlyForPostRequest() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(post(urlEqualTo("/sentBytesPost")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/1.1");
            sampler.setPostBodyRaw(true);
            sampler.addNonEncodedArgument("", "hello world", "");

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/sentBytesPost")), HTTPConstants.POST, false, 1);

            assertEquals("200", result.getResponseCode());
            LoggedRequest request = singleRequest(server, "/sentBytesPost");
            assertArrayEquals("hello world".getBytes(StandardCharsets.UTF_8), request.getBody());
            assertEquals(wireSize(request), result.getSentBytes(),
                    "sentBytes should cover the request line, the headers and the body");
        } finally {
            server.stop();
        }
    }

    /** Returns the only request the server received for the given path. */
    private static LoggedRequest singleRequest(WireMockServer server, String path) {
        List<LoggedRequest> requests = server.findAll(anyRequestedFor(urlEqualTo(path)));
        assertEquals(1, requests.size(), () -> "expected exactly one request for " + path + ", got " + requests);
        return requests.get(0);
    }

    /** Number of bytes the given request occupies on an HTTP/1.1 connection. */
    private static long wireSize(LoggedRequest request) {
        StringBuilder head = new StringBuilder()
                .append(request.getMethod().getName()).append(' ')
                .append(request.getUrl()).append(' ')
                .append(HTTPConstants.HTTP_VERSION_1_1).append("\r\n");
        for (HttpHeader header : request.getHeaders().all()) {
            for (String value : header.values()) {
                head.append(header.key()).append(": ").append(value).append("\r\n");
            }
        }
        head.append("\r\n");
        return head.toString().getBytes(StandardCharsets.ISO_8859_1).length + request.getBody().length;
    }

    @Test
    void sendsConditionalRequestForCachedResource() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/cache"))
                    .willReturn(aResponse().withHeader("ETag", "cache-tag").withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setCacheManager(new CacheManager());
            URL url = new URL(server.url("/cache"));

            assertEquals("200", sampler.sample(url, HTTPConstants.GET, false, 1).getResponseCode());
            assertEquals("200", sampler.sample(url, HTTPConstants.GET, false, 1).getResponseCode());

            server.verify(1, getRequestedFor(urlEqualTo("/cache"))
                    .withHeader("If-None-Match", WireMock.equalTo("cache-tag")));
        } finally {
            server.stop();
        }
    }

    @Test
    void updatesSampleUrlAfterAutomaticRedirect() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/start")).willReturn(
                    aResponse().withStatus(302).withHeader(HTTPConstants.HEADER_LOCATION, "/target/page")));
            server.stubFor(get(urlEqualTo("/target/page")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setAutoRedirects(true);

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/start")), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            assertEquals(server.url("/target/page"), result.getUrlAsString());
        } finally {
            server.stop();
        }
    }

    @Test
    void storesCookiesOfTheRedirectTargetAgainstTheRedirectUrl() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/login")).willReturn(
                    aResponse().withStatus(302).withHeader(HTTPConstants.HEADER_LOCATION, "/session/page")));
            server.stubFor(get(urlEqualTo("/session/page")).willReturn(
                    aResponse().withStatus(200).withHeader(HTTPConstants.HEADER_SET_COOKIE, "sid=42")));
            CookieManager cookieManager = new CookieManager();
            cookieManager.testStarted();
            HTTPSamplerBase sampler = newSampler();
            sampler.setCookieManager(cookieManager);
            sampler.setAutoRedirects(true);

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/login")), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            // the default path of the cookie is derived from the URL it was received from
            assertEquals("sid=42", cookieManager.getCookieHeaderForURL(new URL(server.url("/session/page"))));
            assertNull(cookieManager.getCookieHeaderForURL(new URL(server.url("/elsewhere"))));
        } finally {
            server.stop();
        }
    }

    @Test
    void failsRedirectWithoutLocationHeader() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/noLocation")).willReturn(aResponse().withStatus(302)));
            HTTPSamplerBase sampler = newSampler();

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/noLocation")), HTTPConstants.GET, false, 1);

            assertFalse(result.isSuccessful());
            assertTrue(result.getResponseCode().contains(IllegalArgumentException.class.getName()),
                    "Expected an IllegalArgumentException, but got " + result.getResponseCode());
        } finally {
            server.stop();
        }
    }

    @Test
    void sendsBasicCredentialsFromAuthorizationManager() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/auth"))
                    .withHeader("Authorization", WireMock.equalTo("Basic dXNlcjpwYXNz"))
                    .willReturn(aResponse().withStatus(200)));
            server.stubFor(get(urlEqualTo("/auth")).atPriority(10)
                    .willReturn(aResponse().withHeader("WWW-Authenticate", "Basic realm=\"test\"").withStatus(401)));
            AuthManager authManager = new AuthManager();
            authManager.set(-1, server.url("/"), "user", "pass", "", "", AuthManager.Mechanism.BASIC);
            HTTPSamplerBase sampler = newSampler();
            sampler.setAuthManager(authManager);

            assertEquals("200", sampler.sample(new URL(server.url("/auth")), HTTPConstants.GET, false, 1).getResponseCode());
        } finally {
            server.stop();
        }
    }

    @Test
    @SuppressWarnings("deprecation") // The GSS based auth schemes of HttpClient 5 have no replacement yet
    void sendsNegotiateCredentialsForKerberosAuthorization() throws Exception {
        AuthManager authManager = new AuthManager();
        authManager.set(-1, "http://kerberos.example.invalid/", "user", "pass", "", "", AuthManager.Mechanism.KERBEROS);
        HTTPSamplerBase sampler = newSampler();
        sampler.setAuthManager(authManager);
        HTTPHC5Impl implementation = new HTTPHC5Impl(sampler);
        URL url = new URL("http://kerberos.example.invalid/protected");
        HttpUriRequestBase request = new HttpUriRequestBase(HTTPConstants.GET, url.toURI());

        HttpClientContext context =
                implementation.createHttpClientContext(url, implementation.createHttpClientKey(url), request);

        Lookup<AuthSchemeFactory> authSchemes = context.getAuthSchemeRegistry();
        assertNotNull(authSchemes, "the Kerberos auth schemes have to be registered for the request");
        assertNotNull(authSchemes.lookup(StandardAuthScheme.SPNEGO), "Negotiate has to be supported");
        assertNotNull(authSchemes.lookup(StandardAuthScheme.KERBEROS), "Kerberos has to be supported");
        assertEquals(List.of(StandardAuthScheme.SPNEGO, StandardAuthScheme.KERBEROS),
                new ArrayList<>(request.getConfig().getTargetPreferredAuthSchemes()),
                "HttpClient 5 only prefers Bearer, Digest and Basic by default");
        assertNotNull(context.getCredentialsProvider().getCredentials(
                        new AuthScope(null, "kerberos.example.invalid", 80, null, StandardAuthScheme.SPNEGO), context),
                "the Negotiate scheme needs credentials to authenticate with");
        assertFalse(request.containsHeader(HttpHeaders.AUTHORIZATION),
                "the credentials of a Kerberos authorization are only used to log in to the KDC");
    }

    @Test
    void keepsDefaultAuthSchemesWithoutKerberosAuthorization() throws Exception {
        AuthManager authManager = new AuthManager();
        authManager.set(-1, "http://basic.example.invalid/", "user", "pass", "", "", AuthManager.Mechanism.BASIC);
        HTTPSamplerBase sampler = newSampler();
        sampler.setAuthManager(authManager);
        HTTPHC5Impl implementation = new HTTPHC5Impl(sampler);
        URL url = new URL("http://basic.example.invalid/protected");
        HttpUriRequestBase request = new HttpUriRequestBase(HTTPConstants.GET, url.toURI());

        HttpClientContext context =
                implementation.createHttpClientContext(url, implementation.createHttpClientKey(url), request);

        assertNull(context.getAuthSchemeRegistry(), "the client should use its default auth schemes");
        assertNull(request.getConfig(), "the request configuration should be left alone");
    }

    @Test
    @SuppressWarnings("deprecation") // The GSS based auth schemes of HttpClient 5 have no replacement yet
    void negotiatesWithAProxyThatHasNoCredentialsConfigured() throws Exception {
        HTTPSamplerBase sampler = newSampler();
        sampler.setProxyHost("proxy.example.invalid");
        sampler.setProxyPortInt("8080");
        HTTPHC5Impl implementation = new HTTPHC5Impl(sampler);
        URL url = new URL("http://target.example.invalid/resource");
        HttpUriRequestBase request = new HttpUriRequestBase(HTTPConstants.GET, url.toURI());

        HttpClientContext context =
                implementation.createHttpClientContext(url, implementation.createHttpClientKey(url), request);

        Lookup<AuthSchemeFactory> authSchemes = context.getAuthSchemeRegistry();
        assertNotNull(authSchemes, "the Kerberos auth schemes have to be registered for the request");
        assertNotNull(authSchemes.lookup(StandardAuthScheme.SPNEGO), "Negotiate has to be supported");
        assertEquals(List.of(StandardAuthScheme.SPNEGO, StandardAuthScheme.KERBEROS, StandardAuthScheme.BEARER,
                        StandardAuthScheme.DIGEST, StandardAuthScheme.BASIC),
                new ArrayList<>(request.getConfig().getProxyPreferredAuthSchemes()),
                "a proxy challenging with Negotiate has to be answered with a Kerberos token");
        assertNull(request.getConfig().getTargetPreferredAuthSchemes(),
                "the target is not covered by a Kerberos authorization");
        assertNotNull(context.getCredentialsProvider().getCredentials(
                        new AuthScope(null, "proxy.example.invalid", 8080, null, StandardAuthScheme.SPNEGO), context),
                "the Negotiate scheme needs credentials to authenticate with");
    }

    @Test
    @SuppressWarnings("deprecation") // The GSS based auth schemes of HttpClient 5 have no replacement yet
    void negotiatesWithAProxyCoveredByAKerberosAuthorization() throws Exception {
        AuthManager authManager = new AuthManager();
        authManager.set(-1, "http://proxy.example.invalid:8080", "user", "pass", "", "",
                AuthManager.Mechanism.KERBEROS);
        HTTPSamplerBase sampler = newSampler();
        sampler.setAuthManager(authManager);
        sampler.setProxyHost("proxy.example.invalid");
        sampler.setProxyPortInt("8080");
        sampler.setProxyUser("user");
        sampler.setProxyPass("pass");
        HTTPHC5Impl implementation = new HTTPHC5Impl(sampler);
        URL url = new URL("http://target.example.invalid/resource");
        HttpUriRequestBase request = new HttpUriRequestBase(HTTPConstants.GET, url.toURI());

        HttpClientContext context =
                implementation.createHttpClientContext(url, implementation.createHttpClientKey(url), request);

        assertNotNull(context.getAuthSchemeRegistry(), "the Kerberos auth schemes have to be registered");
        assertEquals(StandardAuthScheme.SPNEGO,
                new ArrayList<>(request.getConfig().getProxyPreferredAuthSchemes()).get(0),
                "a Kerberos authorization for the proxy has to be preferred over the password based schemes");
    }

    @Test
    void keepsDefaultAuthSchemesForAProxyWithCredentials() throws Exception {
        HTTPSamplerBase sampler = newSampler();
        sampler.setProxyHost("proxy.example.invalid");
        sampler.setProxyPortInt("8080");
        sampler.setProxyUser("user");
        sampler.setProxyPass("pass");
        HTTPHC5Impl implementation = new HTTPHC5Impl(sampler);
        URL url = new URL("http://target.example.invalid/resource");
        HttpUriRequestBase request = new HttpUriRequestBase(HTTPConstants.GET, url.toURI());

        HttpClientContext context =
                implementation.createHttpClientContext(url, implementation.createHttpClientKey(url), request);

        assertNull(context.getAuthSchemeRegistry(), "the client should use its default auth schemes");
        assertNull(request.getConfig(), "the request configuration should be left alone");
    }

    @Test
    void authenticatesWithConfiguredProxyCredentials() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/proxy"))
                    .withHeader("Proxy-Authorization", WireMock.equalTo("Basic dXNlcjpwYXNz"))
                    .willReturn(aResponse().withStatus(200)));
            server.stubFor(get(urlEqualTo("/proxy")).atPriority(10)
                    .willReturn(aResponse().withHeader("Proxy-Authenticate", "Basic realm=\"proxy\"").withStatus(407)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setProxyHost("localhost");
            sampler.setProxyPortInt(Integer.toString(server.port()));
            sampler.setProxyUser("user");
            sampler.setProxyPass("pass");

            assertEquals("200", sampler.sample(new URL(server.url("/proxy")), HTTPConstants.GET, false, 1).getResponseCode());
        } finally {
            server.stop();
        }
    }

    @Test
    void setsConnectTimeForHttp11() throws Exception {
        SampleResult result = new SampleResult();
        result.sampleStart();
        HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HTTPHC5Impl.CONTEXT_ATTRIBUTE_SAMPLER_RESULT, result);
        HttpClientConnectionManager connectionManager =
                new HTTPHC5Impl.ConnectTimeMeasuringConnectionManager(new SlowConnectingConnectionManager(50));

        connectionManager.connect(null, null, context);
        result.sampleEnd();

        assertTrue(result.getConnectTime() >= 50,
                "connectTime should cover the time spent connecting, but was " + result.getConnectTime());
        assertTrue(result.getConnectTime() <= result.getTime(),
                "connectTime should not exceed the elapsed time");
    }

    /** Connection manager that only supports {@code connect} and takes a well-known amount of time for it. */
    private static final class SlowConnectingConnectionManager implements HttpClientConnectionManager {

        private final long connectDurationMillis;

        SlowConnectingConnectionManager(long connectDurationMillis) {
            this.connectDurationMillis = connectDurationMillis;
        }

        @Override
        public LeaseRequest lease(String id, HttpRoute route, Timeout requestTimeout, Object state) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void release(ConnectionEndpoint endpoint, Object newState, TimeValue validDuration) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void connect(ConnectionEndpoint endpoint, TimeValue connectTimeout, HttpContext context) throws IOException {
            try {
                Thread.sleep(connectDurationMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            }
        }

        @Override
        public void upgrade(ConnectionEndpoint endpoint, HttpContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close(CloseMode closeMode) {
            // nothing to close
        }

        @Override
        public void close() {
            // nothing to close
        }
    }

    @Test
    void setsConnectTimeForHttp2() throws Exception {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(false));
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/connectTime2")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");

            HTTPSampleResult result = sampler.sample(
                    new URL("https://localhost:" + server.httpsPort() + "/connectTime2"), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            assertTrue(result.getConnectTime() > 0,
                    "connectTime should be greater than 0, but was " + result.getConnectTime());
            assertTrue(result.getConnectTime() <= result.getTime(),
                    "connectTime should not exceed the elapsed time");
        } finally {
            server.stop();
        }
    }

    /**
     * A pooled HTTP/2 connection that the server dropped while the thread was idle must not be handed
     * out as it is. What triggers this is the idle gap between two samples, not the number of requests,
     * so the test waits out {@code httpclient5.validate_after_inactivity} between the two samples and
     * only then lets the connection die, while the client still believes it is usable.
     *
     * <p>Without the re-validation the second request is written to that connection and the sample
     * fails with {@code ConnectionClosedException}, which is what
     * {@link #doesNotRevalidatePooledHttp2ConnectionWithoutAnIdleGap()} pins down.
     */
    @Test
    void revalidatesPooledHttp2ConnectionAfterIdleGap() throws Exception {
        long validateAfterInactivity = 250;
        long configuredValidateAfterInactivity = HTTPHC5Impl.validateAfterInactivityMillis;
        HTTPHC5Impl.validateAfterInactivityMillis = validateAfterInactivity;
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(false));
        server.start();
        try (TcpRelay relay = new TcpRelay(server.httpsPort())) {
            server.stubFor(get(urlEqualTo("/idle")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");
            sampler.setResponseTimeout("30000");
            URL url = new URL("https://localhost:" + relay.getPort() + "/idle");

            assertEquals("200", sampler.sample(url, HTTPConstants.GET, false, 1).getResponseCode());

            Thread.sleep(validateAfterInactivity * 2);
            relay.dropConnectionOnNextRequest();

            // the same sampler on the same thread, so the cached HTTP/2 client and its pool are reused
            HTTPSampleResult result = sampler.sample(url, HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode(),
                    "the stale pooled connection should have been replaced, but the sample failed with "
                            + result.getResponseMessage());
            assertEquals(2, relay.getAcceptedConnections(),
                    "the stale connection should have been discarded and replaced by a new one");
        } finally {
            HTTPHC5Impl.validateAfterInactivityMillis = configuredValidateAfterInactivity;
            server.stop();
        }
    }

    /**
     * Counterpart of {@link #revalidatesPooledHttp2ConnectionAfterIdleGap()} which documents the
     * behaviour the re-validation fixes: without an idle gap the connection is leased without being
     * checked, so the request itself runs into the connection the server has already given up on.
     */
    @Test
    void doesNotRevalidatePooledHttp2ConnectionWithoutAnIdleGap() throws Exception {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(false));
        server.start();
        try (TcpRelay relay = new TcpRelay(server.httpsPort())) {
            server.stubFor(get(urlEqualTo("/busy")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");
            sampler.setResponseTimeout("30000");
            URL url = new URL("https://localhost:" + relay.getPort() + "/busy");

            assertEquals("200", sampler.sample(url, HTTPConstants.GET, false, 1).getResponseCode());
            relay.dropConnectionOnNextRequest();

            HTTPSampleResult result = sampler.sample(url, HTTPConstants.GET, false, 1);

            assertFalse(result.isSuccessful(),
                    "a connection reused within the validation interval is not checked, so the request "
                            + "is expected to run into the dropped connection");
        } finally {
            server.stop();
        }
    }

    private static HTTPSamplerBase newSampler() {
        return HTTPSamplerFactory.newInstance("HttpClient5");
    }

    private static WireMockServer createServer() {
        return new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
    }
}
