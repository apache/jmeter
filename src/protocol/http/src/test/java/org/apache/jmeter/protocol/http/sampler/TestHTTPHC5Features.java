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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

class TestHTTPHC5Features {

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
    void multiplexesConcurrentRequestsOverASingleHttp2Connection() throws Exception {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(false));
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/multiplexed"))
                    .willReturn(aResponse().withStatus(200).withFixedDelay(200)));
            URL url = new URL("https://localhost:" + server.httpsPort() + "/multiplexed");
            // warm up, so the connection that gets shared by the concurrent samples is already established
            HTTPSamplerBase warmUpSampler = newSampler();
            warmUpSampler.setHttpVersion("HTTP/2");
            assertEquals("200", warmUpSampler.sample(url, HTTPConstants.GET, false, 1).getResponseCode());

            int requests = 4;
            ExecutorService executor = Executors.newFixedThreadPool(requests);
            try {
                List<Future<HTTPSampleResult>> results = new ArrayList<>();
                for (int i = 0; i < requests; i++) {
                    results.add(executor.submit(() -> {
                        HTTPSamplerBase sampler = newSampler();
                        sampler.setHttpVersion("HTTP/2");
                        return sampler.sample(url, HTTPConstants.GET, false, 1);
                    }));
                }
                for (Future<HTTPSampleResult> result : results) {
                    HTTPSampleResult sampleResult = result.get(30, TimeUnit.SECONDS);
                    assertEquals("200", sampleResult.getResponseCode());
                    assertEquals("HTTP/2", sampleResult.getResponseHeaders().substring(0, "HTTP/2".length()));
                }
            } finally {
                executor.shutdownNow();
            }
        } finally {
            server.stop();
        }
    }

    @Test
    void enablesMessageMultiplexingWithoutRequiringHttpClient55() throws Exception {
        byte[] classBytes;
        try (InputStream classFile = HTTPHC5Impl.class.getResourceAsStream("HTTPHC5Impl.class")) {
            classBytes = classFile.readAllBytes();
        }
        assertTrue(new String(classBytes, StandardCharsets.ISO_8859_1).contains("setMessageMultiplexing"),
                "HTTP/2 message multiplexing should be enabled");
        assertFalse(hasMethodReference(classBytes,
                "org/apache/hc/client5/http/impl/nio/PoolingAsyncClientConnectionManagerBuilder",
                "setMessageMultiplexing"),
                "setMessageMultiplexing was added in HttpClient 5.5 and must not be linked directly");
    }

    @Test
    void appliesHttp2ProtocolSettings() {
        H2Config config = HTTPHC5Impl.createHttp2Config();

        assertEquals(8192, config.getHeaderTableSize(), "HPACK dynamic table size");
        assertTrue(config.isCompressionEnabled(), "HPACK header compression");
        assertEquals(250, config.getMaxConcurrentStreams());
        assertEquals(65535, config.getInitialWindowSize());
        assertEquals(65536, config.getMaxFrameSize());
        assertFalse(config.isPushEnabled(), "server push is dropped by JMeter, so it must not be announced");
    }

    @Test
    void doesNotRequireProtocolUpgradeConfiguration() throws Exception {
        try (InputStream classFile = HTTPHC5Impl.class.getResourceAsStream("HTTPHC5Impl.class")) {
            assertFalse(new String(classFile.readAllBytes(), StandardCharsets.ISO_8859_1)
                    .contains("setProtocolUpgradeEnabled"));
        }
    }

    @Test
    void doesNotRequireHttpAsyncClassicAdapter() throws Exception {
        try (InputStream classFile = HTTPHC5Impl.class.getResourceAsStream("HTTPHC5Impl.class")) {
            assertFalse(hasMethodReference(classFile.readAllBytes(),
                    "org/apache/hc/client5/http/impl/async/HttpAsyncClients", "classic"));
        }
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
        Thread.sleep(50);
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
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(false));
        server.start();
        try (ConnectionDroppingRelay relay = new ConnectionDroppingRelay(server.httpsPort())) {
            server.stubFor(get(urlEqualTo("/idle")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = newSampler();
            sampler.setHttpVersion("HTTP/2");
            sampler.setResponseTimeout("30000");
            URL url = new URL("https://localhost:" + relay.getPort() + "/idle");

            assertEquals("200", sampler.sample(url, HTTPConstants.GET, false, 1).getResponseCode());

            Thread.sleep(validateAfterInactivityMillis() + 500);
            relay.dropConnectionOnNextRequest();

            // the same sampler on the same thread, so the cached HTTP/2 client and its pool are reused
            HTTPSampleResult result = sampler.sample(url, HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode(),
                    "the stale pooled connection should have been replaced, but the sample failed with "
                            + result.getResponseMessage());
            assertEquals(2, relay.getAcceptedConnections(),
                    "the stale connection should have been discarded and replaced by a new one");
        } finally {
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
        try (ConnectionDroppingRelay relay = new ConnectionDroppingRelay(server.httpsPort())) {
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

    private static long validateAfterInactivityMillis() {
        return JMeterUtils.getPropDefault("httpclient5.validate_after_inactivity", 2000L);
    }

    /**
     * Relays TCP traffic to a backend server and can drop a single relayed connection as soon as the
     * client writes to it again, which is how an idle connection closed by the server (or by a load
     * balancer) looks to a client that has not noticed the close yet. New connections are relayed as
     * usual, so the backend stays reachable.
     */
    private static final class ConnectionDroppingRelay implements Closeable {

        private final ServerSocket serverSocket;
        private final int backendPort;
        private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "relay");
            thread.setDaemon(true);
            return thread;
        });
        private final AtomicInteger acceptedConnections = new AtomicInteger();
        private volatile RelayedConnection currentConnection;

        ConnectionDroppingRelay(int backendPort) throws IOException {
            this.backendPort = backendPort;
            this.serverSocket = new ServerSocket(0, 0, InetAddress.getLoopbackAddress());
            executor.execute(this::acceptConnections);
        }

        int getPort() {
            return serverSocket.getLocalPort();
        }

        int getAcceptedConnections() {
            return acceptedConnections.get();
        }

        void dropConnectionOnNextRequest() {
            RelayedConnection connection = currentConnection;
            if (connection == null) {
                throw new IllegalStateException("No connection has been relayed yet");
            }
            connection.doomed = true;
        }

        private void acceptConnections() {
            while (!serverSocket.isClosed()) {
                RelayedConnection connection;
                try {
                    Socket client = serverSocket.accept();
                    connection = new RelayedConnection(client,
                            new Socket(InetAddress.getLoopbackAddress(), backendPort));
                } catch (IOException e) {
                    return;
                }
                acceptedConnections.incrementAndGet();
                currentConnection = connection;
                executor.execute(connection::relayRequests);
                executor.execute(connection::relayResponses);
            }
        }

        @Override
        public void close() throws IOException {
            serverSocket.close();
            executor.shutdownNow();
        }
    }

    private static final class RelayedConnection {

        private final Socket client;
        private final Socket backend;
        private volatile boolean doomed;

        RelayedConnection(Socket client, Socket backend) {
            this.client = client;
            this.backend = backend;
        }

        void relayRequests() {
            try {
                InputStream input = client.getInputStream();
                OutputStream output = backend.getOutputStream();
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1 && !doomed) {
                    output.write(buffer, 0, read);
                    output.flush();
                }
            } catch (IOException ignored) { // NOSONAR the connection is closed below in any case
                // the peer went away, which ends the relaying just as well
            }
            close();
        }

        void relayResponses() {
            try {
                InputStream input = backend.getInputStream();
                OutputStream output = client.getOutputStream();
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    output.flush();
                }
            } catch (IOException ignored) { // NOSONAR the connection is closed below in any case
                // the peer went away, which ends the relaying just as well
            }
            close();
        }

        private void close() {
            JOrphanUtils.closeQuietly(client);
            JOrphanUtils.closeQuietly(backend);
        }
    }

    private static HTTPSamplerBase newSampler() {
        return HTTPSamplerFactory.newInstance("HttpClient5");
    }

    private static WireMockServer createServer() {
        return new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
    }

    private static boolean hasMethodReference(byte[] classBytes, String className, String methodName) throws IOException {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(classBytes))) {
            input.readInt();
            input.readUnsignedShort();
            input.readUnsignedShort();
            int constantPoolCount = input.readUnsignedShort();
            int[] tags = new int[constantPoolCount];
            int[] firstReferences = new int[constantPoolCount];
            int[] secondReferences = new int[constantPoolCount];
            String[] utf8Values = new String[constantPoolCount];
            int i = 1;
            while (i < constantPoolCount) {
                tags[i] = input.readUnsignedByte();
                switch (tags[i]) {
                case 1:
                    utf8Values[i] = input.readUTF();
                    break;
                case 3:
                case 4:
                    input.readInt();
                    break;
                case 5:
                case 6:
                    input.readLong();
                    i++;
                    break;
                case 7:
                case 8:
                case 16:
                case 19:
                case 20:
                    firstReferences[i] = input.readUnsignedShort();
                    break;
                case 9:
                case 10:
                case 11:
                case 12:
                case 17:
                case 18:
                    firstReferences[i] = input.readUnsignedShort();
                    secondReferences[i] = input.readUnsignedShort();
                    break;
                case 15:
                    input.readUnsignedByte();
                    firstReferences[i] = input.readUnsignedShort();
                    break;
                default:
                    throw new IOException("Unknown class-file constant-pool tag " + tags[i]);
                }
                i++;
            }
            for (int methodReferenceIndex = 1; methodReferenceIndex < constantPoolCount; methodReferenceIndex++) {
                if (tags[methodReferenceIndex] != 10) {
                    continue;
                }
                int classIndex = firstReferences[methodReferenceIndex];
                int nameAndTypeIndex = secondReferences[methodReferenceIndex];
                String referencedClass = utf8Values[firstReferences[classIndex]];
                String referencedMethod = utf8Values[firstReferences[nameAndTypeIndex]];
                if (className.equals(referencedClass) && methodName.equals(referencedMethod)) {
                    return true;
                }
            }
            return false;
        }
    }
}
