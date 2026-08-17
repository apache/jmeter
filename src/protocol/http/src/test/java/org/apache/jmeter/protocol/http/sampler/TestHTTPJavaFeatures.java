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
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.Socket;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JsseSSLManager;
import org.apache.jmeter.util.SSLManager;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

class TestHTTPJavaFeatures {

    @Test
    void usesHttpClientVersionWhenSamplerVersionIsEmpty() {
        assertTrue(HTTPJavaImpl.isHttp2("", "HTTP/2"));
        assertFalse(HTTPJavaImpl.isHttp2("", "HTTP/1.1"));
    }

    @Test
    void usesSamplerHttpVersionWhenSpecified() {
        assertFalse(HTTPJavaImpl.isHttp2("HTTP/1.1", "HTTP/2"));
        assertTrue(HTTPJavaImpl.isHttp2("HTTP/2", "HTTP/1.1"));
    }

    @Test
    void defaultsToHttp11ForUnsupportedHttpVersion() {
        assertFalse(HTTPJavaImpl.isHttp2("HTTP/3", "HTTP/2"));
    }

    @Test
    void usesHttp2WhenSelected() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/http2")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/http2")), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode(), result.getResponseMessage());
            assertEquals("HTTP/2", result.getResponseHeaders().substring(0, "HTTP/2".length()));
        } finally {
            server.stop();
        }
    }

    @Test
    void uploadsMultipartFileOverHttp2() throws Exception {
        WireMockServer server = createServer();
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
                    new URL(server.url("/upload")), HTTPConstants.POST, false, 1);

            assertEquals("200", result.getResponseCode(), result.getResponseMessage());
            assertTrue(result.getSentBytes() > 1_000_000,
                    () -> "the whole file should have been sent, but only " + result.getSentBytes() + " bytes were");
            // The request view must not hold the file contents, otherwise a large upload is in heap twice
            assertTrue(result.getQueryString().contains("<actual file content, not shown here>"),
                    () -> "file contents should be omitted from the request view: " + result.getQueryString());
            server.verify(postRequestedFor(urlEqualTo("/upload")));
        } finally {
            Files.deleteIfExists(upload);
            server.stop();
        }
    }

    @Test
    void interruptsPendingHttp2Request() throws Exception {
        WireMockServer server = createServer();
        server.start();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            server.stubFor(get(urlEqualTo("/interrupted")).willReturn(aResponse().withFixedDelay(10_000)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");
            HTTPJavaImpl implementation = new HTTPJavaImpl(sampler);

            Future<?> sample = executor.submit(() -> implementation.sample(
                    new URL(server.url("/interrupted")), HTTPConstants.GET, false, 1));

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
    void setsResponseMessageForHttp2() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/http2ResponseMessage")).willReturn(aResponse().withStatus(404)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/http2ResponseMessage")), HTTPConstants.GET, false, 1);

            assertEquals("404", result.getResponseCode());
            assertEquals("Not Found", result.getResponseMessage());
        } finally {
            server.stop();
        }
    }

    @Test
    void returnsEmptyReasonPhraseForUnknownStatusCode() {
        assertEquals("OK", HTTPJavaImpl.getReasonPhrase(200));
        assertEquals("", HTTPJavaImpl.getReasonPhrase(599));
    }

    @Test
    void mapsHttp2SettingsToJdkSystemProperties() {
        Properties jmeterProperties = new Properties();
        jmeterProperties.setProperty("http.java.h2.header_table_size", "8192");
        jmeterProperties.setProperty("http.java.h2.max_concurrent_streams", "250");
        jmeterProperties.setProperty("http.java.h2.initial_window_size", "65535");
        jmeterProperties.setProperty("http.java.h2.connection_window_size", "1048576");
        jmeterProperties.setProperty("http.java.h2.max_frame_size", "16384");
        jmeterProperties.setProperty("http.java.h2.keep_alive_timeout", "60");
        jmeterProperties.setProperty("http.java.h2.push_enabled", "true");
        Properties systemProperties = new Properties();

        HTTPJavaImpl.applyHttp2SystemProperties(jmeterProperties, systemProperties);

        assertEquals("8192", systemProperties.getProperty("jdk.httpclient.hpack.maxheadertablesize"));
        assertEquals("250", systemProperties.getProperty("jdk.httpclient.maxstreams"));
        assertEquals("65535", systemProperties.getProperty("jdk.httpclient.windowsize"));
        assertEquals("1048576", systemProperties.getProperty("jdk.httpclient.connectionWindowSize"));
        assertEquals("16384", systemProperties.getProperty("jdk.httpclient.maxframesize"));
        assertEquals("60", systemProperties.getProperty("jdk.httpclient.keepalive.timeout.h2"));
        assertEquals("1", systemProperties.getProperty("jdk.httpclient.enablepush"));
    }

    @Test
    void disablesServerPushAndKeepsJdkDefaultsWhenNothingIsConfigured() {
        Properties systemProperties = new Properties();

        HTTPJavaImpl.applyHttp2SystemProperties(new Properties(), systemProperties);

        assertEquals("0", systemProperties.getProperty("jdk.httpclient.enablepush"));
        assertNull(systemProperties.getProperty("jdk.httpclient.hpack.maxheadertablesize"));
        assertNull(systemProperties.getProperty("jdk.httpclient.windowsize"));
    }

    @Test
    void doesNotOverrideHttp2SettingsGivenOnTheCommandLine() {
        Properties jmeterProperties = new Properties();
        jmeterProperties.setProperty("http.java.h2.header_table_size", "8192");
        Properties systemProperties = new Properties();
        systemProperties.setProperty("jdk.httpclient.hpack.maxheadertablesize", "4096");
        systemProperties.setProperty("jdk.httpclient.enablepush", "1");

        HTTPJavaImpl.applyHttp2SystemProperties(jmeterProperties, systemProperties);

        assertEquals("4096", systemProperties.getProperty("jdk.httpclient.hpack.maxheadertablesize"));
        assertEquals("1", systemProperties.getProperty("jdk.httpclient.enablepush"));
    }

    @Test
    void sharesHttp2ClientsBetweenThreadsForMultiplexing() throws Exception {
        Map<?, ?> clientsOfMainThread = HTTPJavaImpl.getHttp2Clients();
        AtomicReference<Map<?, ?>> clientsOfOtherThread = new AtomicReference<>();

        Thread thread = new Thread(() -> clientsOfOtherThread.set(HTTPJavaImpl.getHttp2Clients()));
        thread.start();
        thread.join();

        assertSame(clientsOfMainThread, clientsOfOtherThread.get(),
                "HTTP/2 clients should be shared, so concurrent requests are multiplexed over one connection");
    }

    @Test
    void multiplexesConcurrentHttp2RequestsOverOneConnection() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/multiplexed"))
                    .willReturn(aResponse().withStatus(200).withFixedDelay(200).withBody("multiplexed")));

            int requests = 4;
            ExecutorService executor = Executors.newFixedThreadPool(requests);
            try {
                List<Future<HTTPSampleResult>> results = new ArrayList<>();
                for (int i = 0; i < requests; i++) {
                    results.add(executor.submit(() -> {
                        HTTPSamplerBase sampler = newSampler();
                        sampler.setHttpVersion("HTTP/2");
                        return sampler.sample(
                                new URL(server.url("/multiplexed")), HTTPConstants.GET, false, 1);
                    }));
                }
                for (Future<HTTPSampleResult> result : results) {
                    HTTPSampleResult sampleResult = result.get(60, TimeUnit.SECONDS);
                    assertEquals("200", sampleResult.getResponseCode());
                    assertTrue(sampleResult.getResponseHeaders().startsWith("HTTP/2"),
                            "Response should have been received over HTTP/2, but was "
                                    + sampleResult.getResponseHeaders());
                }
            } finally {
                executor.shutdownNow();
            }
        } finally {
            server.stop();
        }
    }

    @Test
    void recordsConnectTimeForEverySampleWaitingForTheNewConnection() throws Exception {
        ConnectTimeTracker tracker = new ConnectTimeTracker();
        SampleResult first = new SampleResult();
        SampleResult second = new SampleResult();
        first.sampleStart();
        second.sampleStart();
        tracker.sampleStarted(first, "example.com:443");
        tracker.sampleStarted(second, "example.com:443");

        ConnectTimeTracker.Connection connection = tracker.connectionOpened("example.com:443");
        Thread.sleep(5);
        connection.established();
        Thread.sleep(5);

        first.sampleEnd();
        second.sampleEnd();
        tracker.sampleFinished(first);
        tracker.sampleFinished(second);

        assertTrue(first.getConnectTime() > 0, "connectTime of the first sample should have been recorded");
        assertTrue(second.getConnectTime() > 0, "connectTime of the second sample should have been recorded");
        assertTrue(first.getConnectTime() <= first.getTime());
        assertTrue(second.getConnectTime() <= second.getTime());
    }

    @Test
    void doesNotRecordConnectTimeOfAConnectionToAnotherOrigin() throws Exception {
        ConnectTimeTracker tracker = new ConnectTimeTracker();
        SampleResult result = new SampleResult();
        result.sampleStart();
        tracker.sampleStarted(result, "a.example:443");

        Thread.sleep(5);
        // Another JMeter thread opens a connection to a different host over the shared client
        tracker.connectionOpened("b.example:443").established();

        result.sampleEnd();
        tracker.sampleFinished(result);

        assertEquals(0, result.getConnectTime(),
                "the connect time of another origin must not be reported for this sample");
    }

    @Test
    void doesNotRecordConnectTimeForSampleServedByAnEstablishedConnection() throws Exception {
        ConnectTimeTracker tracker = new ConnectTimeTracker();
        tracker.connectionOpened("example.com:443").established();

        SampleResult result = new SampleResult();
        result.sampleStart();
        tracker.sampleStarted(result, "example.com:443");
        Thread.sleep(5);
        result.sampleEnd();
        tracker.sampleFinished(result);

        assertEquals(0, result.getConnectTime(),
                "a sample multiplexed over an established connection did not connect");
    }

    @Test
    void recordsConnectTimeOfOneConnectionPerSampleOnly() throws Exception {
        ConnectTimeTracker tracker = new ConnectTimeTracker();
        SampleResult result = new SampleResult();
        result.sampleStart();
        tracker.sampleStarted(result, "example.com:443");

        Thread.sleep(5);
        tracker.connectionOpened("example.com:443").established();
        long firstConnection = result.getConnectTime();
        Thread.sleep(300);
        // A connection opened afterwards belongs to another sample, it must not overwrite the measurement
        tracker.connectionOpened("example.com:443").established();

        result.sampleEnd();
        tracker.sampleFinished(result);

        assertEquals(0, firstConnection, "the sample result should only be written by its own thread");
        assertTrue(result.getConnectTime() > 0, "connectTime should have been recorded");
        assertTrue(result.getConnectTime() < 150,
                "connectTime should be the one of the first connection, but was " + result.getConnectTime());
    }

    @Test
    void derivesTheOriginOfASampleFromItsUrl() throws Exception {
        assertEquals("example.com:443", ConnectTimeTracker.origin(new URL("https://EXAMPLE.com/a")));
        assertEquals("example.com:80", ConnectTimeTracker.origin(new URL("http://example.com/a")));
        assertEquals("example.com:8443", ConnectTimeTracker.origin(new URL("https://example.com:8443/a")));
    }

    @Test
    void ignoresSamplesWhichAreNoLongerInFlight() {
        ConnectTimeTracker tracker = new ConnectTimeTracker();
        SampleResult result = new SampleResult();
        result.sampleStart();
        tracker.sampleStarted(result, "example.com:443");
        tracker.sampleFinished(result);

        tracker.connectionOpened("example.com:443").established();
        result.sampleEnd();

        assertEquals(0, result.getConnectTime());
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
            assertTrue(result.getSentBytes() > 0, "sentBytes should be greater than 0");
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
            assertTrue(result.getSentBytes() > "hello world".length(), "sentBytes should include request line, headers, and body");
        } finally {
            server.stop();
        }
    }

    @Test
    void setsSentBytesCorrectlyForHttp2GetRequest() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/http2SentBytesGet")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/http2SentBytesGet")), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            assertTrue(result.getSentBytes() > 0, "sentBytes should be greater than 0");
        } finally {
            server.stop();
        }
    }

    @Test
    void setsSentBytesCorrectlyForHttp2PostRequest() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(post(urlEqualTo("/http2SentBytesPost")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");
            sampler.setPostBodyRaw(true);
            sampler.addNonEncodedArgument("", "hello world http2", "");

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/http2SentBytesPost")), HTTPConstants.POST, false, 1);

            assertEquals("200", result.getResponseCode());
            assertTrue(result.getSentBytes() > "hello world http2".length(), "sentBytes should include request line, headers, and body");
        } finally {
            server.stop();
        }
    }

    @Test
    void displaysAuthorizationHeaderFromHeaderManagerInHttp11() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/authHttp11")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/1.1");
            HeaderManager headerManager = new HeaderManager();
            headerManager.add(new Header("Authorization", "Bearer my-secret-token"));
            sampler.setHeaderManager(headerManager);

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/authHttp11")), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            assertTrue(result.getRequestHeaders().contains("Authorization: Bearer my-secret-token"),
                    "Request headers should contain Authorization header set in HeaderManager");
        } finally {
            server.stop();
        }
    }

    @Test
    void sendsHeaderManagerHeaderOnceForHttp11() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/headerOnce")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/1.1");
            HeaderManager headerManager = new HeaderManager();
            headerManager.add(new Header("Accept", "application/json"));
            sampler.setHeaderManager(headerManager);

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/headerOnce")), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            server.verify(1, getRequestedFor(urlEqualTo("/headerOnce"))
                    .withHeader("Accept", equalTo("application/json")));
        } finally {
            server.stop();
        }
    }

    @Test
    void displaysAuthorizationHeaderFromHeaderManagerInHttp2() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/authHttp2")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");
            HeaderManager headerManager = new HeaderManager();
            headerManager.add(new Header("Authorization", "Bearer my-secret-token-http2"));
            sampler.setHeaderManager(headerManager);

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/authHttp2")), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            assertTrue(result.getRequestHeaders().contains("Authorization: Bearer my-secret-token-http2"),
                    "Request headers should contain Authorization header set in HeaderManager");
        } finally {
            server.stop();
        }
    }

    @Test
    void setsConnectTimeForHttp11() throws Exception {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicHttpsPort());
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/connectTime")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/1.1");

            HTTPSampleResult result = sampler.sample(
                    new URL("https://localhost:" + server.httpsPort() + "/connectTime"), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            assertTrue(result.getConnectTime() > 0,
                    "connectTime should be greater than 0, but was " + result.getConnectTime());
            assertTrue(result.getConnectTime() <= result.getTime(),
                    "connectTime should not exceed the elapsed time");
        } finally {
            server.stop();
        }
    }

    @Test
    void doesNotMeasureConnectTimeForPlainTextHttp2() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/http2ConnectTime")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/http2ConnectTime")), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode());
            // The JDK client only reveals connection establishment through the SSLEngine it creates per
            // connection, so the connect time of a plain text connection cannot be measured. Guessing it
            // would report the connect time of unrelated samples of the shared, multiplexed client.
            assertEquals(0, result.getConnectTime(),
                    "connect time cannot be measured for plain text HTTP/2, so it must not be guessed");
        } finally {
            server.stop();
        }
    }

    @Test
    void setsConnectTimeForHttp2OverTls() throws Exception {
        WireMockServer server = new WireMockServer(
                WireMockConfiguration.wireMockConfig().dynamicHttpsPort().http2TlsDisabled(false));
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/http2TlsConnectTime")).willReturn(aResponse().withStatus(200)));

            ConnectTimeTracker tracker = new ConnectTimeTracker();
            SSLContext sslContext = trustAllContext();
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .sslContext(new ConnectTimeTracker.MeasuringSSLContext(sslContext, tracker))
                    .build();
            URL url = new URL("https://localhost:" + server.httpsPort() + "/http2TlsConnectTime");
            HttpRequest request = HttpRequest.newBuilder(url.toURI()).build();

            SampleResult result = new SampleResult();
            result.sampleStart();
            tracker.sampleStarted(result, ConnectTimeTracker.origin(url));
            HttpResponse<String> response;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } finally {
                tracker.sampleFinished(result);
            }
            result.sampleEnd();

            assertEquals(200, response.statusCode());
            assertEquals(HttpClient.Version.HTTP_2, response.version());
            assertTrue(result.getConnectTime() > 0,
                    "connectTime should be greater than 0, but was " + result.getConnectTime());
            assertTrue(result.getConnectTime() <= result.getTime(),
                    "connectTime should not exceed the elapsed time");

            // The next sample reuses the established connection, so it did not have to connect
            SampleResult pooled = new SampleResult();
            pooled.sampleStart();
            tracker.sampleStarted(pooled, ConnectTimeTracker.origin(url));
            try {
                assertEquals(200, client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
            } finally {
                tracker.sampleFinished(pooled);
            }
            pooled.sampleEnd();

            assertEquals(0, pooled.getConnectTime(),
                    "a sample served by an established connection must not report a connect time");
        } finally {
            server.stop();
        }
    }

    @Test
    void doesNotCacheAnAdditionalHttp2ClientWhenTheSslContextIsReset() throws Exception {
        HTTPSamplerBase sampler = newSampler();
        sampler.setHttpVersion("HTTP/2");
        HTTPJavaImpl impl = new HTTPJavaImpl(sampler);
        Map<?, ?> clients = HTTPJavaImpl.getHttp2Clients();
        URL url = new URL("https://localhost:1234/http2SslReset");
        try {
            JsseSSLManager sslManager = (JsseSSLManager) SSLManager.getInstance();
            HTTPJavaImpl.Http2Client first = impl.getHttpClient(url);
            Map<Object, Object> clientsAfterFirstSample = new HashMap<>(clients);
            SSLContext contextOfFirstSample = sslManager.getContext();

            // Happens on every thread group iteration when "same user on next iteration" is switched off
            sslManager.resetContext();
            HTTPJavaImpl.Http2Client second = impl.getHttpClient(url);

            assertNotSame(contextOfFirstSample, sslManager.getContext(),
                    "resetContext() should hand out a new SSLContext, otherwise the test proves nothing");
            assertFalse(clientsAfterFirstSample.isEmpty(), "an HTTP/2 client should have been cached");
            assertSame(first, second, "the cached HTTP/2 client should be reused after the SSLContext was reset");
            assertEquals(clientsAfterFirstSample, new HashMap<Object, Object>(clients),
                    "a new SSLContext must not add another HttpClient to the cache, that would leak clients");
        } finally {
            impl.testEnded();
        }
    }

    @Test
    void closesSharedHttp2ClientsWhenTheTestEnds() throws Exception {
        WireMockServer server = createServer();
        server.start();
        HTTPSamplerBase sampler = newSampler();
        try {
            server.stubFor(get(urlEqualTo("/http2TestEnded")).willReturn(aResponse().withStatus(200)));
            sampler.setHttpVersion("HTTP/2");

            HTTPSampleResult result = sampler.sample(
                    new URL(server.url("/http2TestEnded")), HTTPConstants.GET, false, 1);
            assertEquals("200", result.getResponseCode());
            assertFalse(HTTPJavaImpl.getHttp2Clients().isEmpty(), "the sample should have cached an HTTP/2 client");

            sampler.testEnded();

            assertTrue(HTTPJavaImpl.getHttp2Clients().isEmpty(),
                    "HTTP/2 clients should be released when the test ends");
        } finally {
            server.stop();
        }
    }

    private static SSLContext trustAllContext() throws Exception {        TrustManager trustAll = new X509ExtendedTrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // trust everything in the test
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                // trust everything in the test
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
                // trust everything in the test
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
                // trust everything in the test
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
                // trust everything in the test
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
                // trust everything in the test
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[] { trustAll }, null);
        return context;
    }

    private static HTTPSamplerBase newSampler() {
        return HTTPSamplerFactory.newInstance("Java");
    }

    private static WireMockServer createServer() {
        return new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
    }
}
