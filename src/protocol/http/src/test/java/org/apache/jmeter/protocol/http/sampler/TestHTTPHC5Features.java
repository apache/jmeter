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
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
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
