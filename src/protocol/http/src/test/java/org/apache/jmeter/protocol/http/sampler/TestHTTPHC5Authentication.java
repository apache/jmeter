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
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * Authentication of the HttpClient5 sampler implementation with the credentials of the HTTP Authorization
 * Manager and of the proxy, which has to work the way it does with HttpClient4: a server or a proxy that asks
 * for NTLM is answered with the user, the password and the domain, and an entry with the BASIC mechanism
 * sends its credentials pre-emptively, but can still answer a challenge for another scheme.
 */
class TestHTTPHC5Authentication {

    /** Start of the Base64 encoding of an NTLM type 1 (negotiate) message. */
    private static final String NTLM_TYPE_1 = "NTLM TlRMTVNTUAAB.*";

    /** Start of the Base64 encoding of an NTLM type 3 (authenticate) message. */
    private static final String NTLM_TYPE_3 = "NTLM TlRMTVNTUAAD.*";

    @AfterEach
    void closeClientsOfThisThread() {
        HTTPHC5Impl.closeClientsOfCurrentThread();
    }

    /**
     * Like an IIS site with Windows authentication. The entry keeps the BASIC mechanism new entries default to,
     * so its credentials are sent pre-emptively as Basic first, which the server rejects.
     */
    @ParameterizedTest
    @ValueSource(strings = {"HTTP/1.1", "HTTP/2"})
    void answersAnNtlmChallengeWithTheDomainOfTheAuthorizationManager(String httpVersion) throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            stubNtlm(server, "/ntlm", HTTPConstants.HEADER_AUTHORIZATION, "WWW-Authenticate", 401);
            AuthManager authManager = new AuthManager();
            authManager.set(-1, server.url("/"), "user", "pass", "EXAMPLE", "", AuthManager.Mechanism.BASIC);
            HTTPSamplerBase sampler = newSampler(httpVersion);
            sampler.setAuthManager(authManager);

            HTTPSampleResult result = sampler.sample(new URL(server.url("/ntlm")), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode(), result::getResponseMessage);
            List<String> authorizations = headerValues(server, "/ntlm", HTTPConstants.HEADER_AUTHORIZATION);
            assertTrue(authorizations.contains(basic("user", "pass")),
                    () -> "the credentials should have been sent pre-emptively as Basic first: " + authorizations);
            byte[] authenticate = ntlmMessage(authorizations, 3);
            assertEquals("user", securityBufferString(authenticate, 36), "user of the NTLM authenticate message");
            assertEquals("EXAMPLE", securityBufferString(authenticate, 28), "domain of the NTLM authenticate message");
        } finally {
            server.stop();
        }
    }

    /** Like a corporate proxy that only offers NTLM, answered with the proxy user of the sampler. */
    @ParameterizedTest
    @ValueSource(strings = {"HTTP/1.1", "HTTP/2"})
    void answersAnNtlmChallengeOfTheProxy(String httpVersion) throws Exception {
        WireMockServer proxy = createServer();
        proxy.start();
        try {
            stubNtlm(proxy, "/through-proxy", "Proxy-Authorization", "Proxy-Authenticate", 407);
            HTTPSamplerBase sampler = newSampler(httpVersion);
            sampler.setProxyHost("localhost");
            sampler.setProxyPortInt(Integer.toString(proxy.port()));
            sampler.setProxyUser("proxyuser");
            sampler.setProxyPass("proxypass");

            // The target has to be another host than the proxy, as HttpClient keeps the state of an
            // authentication per host, which the NTLM handshakes with the two would otherwise share
            HTTPSampleResult result = sampler.sample(
                    new URL("http://127.0.0.1:" + proxy.port() + "/through-proxy"), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode(), result::getResponseMessage);
            byte[] authenticate = ntlmMessage(headerValues(proxy, "/through-proxy", "Proxy-Authorization"), 3);
            assertEquals("proxyuser", securityBufferString(authenticate, 36), "user of the NTLM authenticate message");
        } finally {
            proxy.stop();
        }
    }

    /**
     * The pre-emptive Basic credentials of an entry with the BASIC mechanism must not keep it from answering a
     * challenge for another scheme, like Digest here.
     */
    @ParameterizedTest
    @ValueSource(strings = {"HTTP/1.1", "HTTP/2"})
    void answersADigestChallengeForAnEntryWithTheBasicMechanism(String httpVersion) throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/digest")).atPriority(1)
                    .withHeader(HTTPConstants.HEADER_AUTHORIZATION, matching("Digest .*username=\"user\".*"))
                    .willReturn(aResponse().withStatus(200)));
            server.stubFor(get(urlEqualTo("/digest")).atPriority(10)
                    .willReturn(aResponse().withStatus(401).withHeader("WWW-Authenticate",
                            "Digest realm=\"test\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", qop=\"auth\"")));
            AuthManager authManager = new AuthManager();
            authManager.set(-1, server.url("/"), "user", "pass", "", "", AuthManager.Mechanism.BASIC);
            HTTPSamplerBase sampler = newSampler(httpVersion);
            sampler.setAuthManager(authManager);

            HTTPSampleResult result = sampler.sample(new URL(server.url("/digest")), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode(), result::getResponseMessage);
        } finally {
            server.stop();
        }
    }

    /** The credentials of an entry with a realm only answer a challenge of that realm, as with HttpClient4. */
    @ParameterizedTest
    @CsvSource({"jmeter, 200", "other, 401"})
    void answersOnlyChallengesOfTheRealmOfTheEntry(String challengedRealm, String expectedCode) throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/realm")).atPriority(1)
                    .withHeader(HTTPConstants.HEADER_AUTHORIZATION, matching("Basic .*"))
                    .willReturn(aResponse().withStatus(200)));
            server.stubFor(get(urlEqualTo("/realm")).atPriority(10)
                    .willReturn(aResponse().withStatus(401)
                            .withHeader("WWW-Authenticate", "Basic realm=\"" + challengedRealm + "\"")));
            AuthManager authManager = new AuthManager();
            // DIGEST, as an entry with the BASIC mechanism sends its credentials before any challenge
            authManager.set(-1, server.url("/"), "user", "pass", "", "jmeter", AuthManager.Mechanism.DIGEST);
            HTTPSamplerBase sampler = newSampler("HTTP/1.1");
            sampler.setAuthManager(authManager);

            HTTPSampleResult result = sampler.sample(new URL(server.url("/realm")), HTTPConstants.GET, false, 1);

            assertEquals(expectedCode, result.getResponseCode(), result::getResponseMessage);
        } finally {
            server.stop();
        }
    }

    /** The pre-emptive Basic credentials are reported with the request, and accounted for in the sent bytes. */
    @Test
    void reportsThePreemptiveBasicCredentials() throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/basic")).willReturn(aResponse().withStatus(200)));
            URL url = new URL(server.url("/basic"));
            HTTPSampleResult anonymous = newSampler("HTTP/1.1").sample(url, HTTPConstants.GET, false, 1);
            AuthManager authManager = new AuthManager();
            authManager.set(-1, server.url("/"), "user", "pass", "", "", AuthManager.Mechanism.BASIC);
            HTTPSamplerBase sampler = newSampler("HTTP/1.1");
            sampler.setAuthManager(authManager);

            HTTPSampleResult authenticated = sampler.sample(url, HTTPConstants.GET, false, 1);

            assertEquals("200", authenticated.getResponseCode());
            assertEquals(List.of(basic("user", "pass")),
                    server.findAll(anyRequestedFor(urlEqualTo("/basic"))).stream()
                            .filter(request -> request.containsHeader(HTTPConstants.HEADER_AUTHORIZATION))
                            .map(request -> request.getHeader(HTTPConstants.HEADER_AUTHORIZATION))
                            .toList());
            assertTrue(authenticated.getRequestHeaders().contains("Authorization: " + basic("user", "pass") + "\n"),
                    authenticated::getRequestHeaders);
            assertEquals(anonymous.getSentBytes()
                            + HTTPMessageSizes.headerLength(HTTPConstants.HEADER_AUTHORIZATION, basic("user", "pass")),
                    authenticated.getSentBytes());
        } finally {
            server.stop();
        }
    }

    /**
     * Scripts the NTLM handshake: anything but an NTLM message is challenged, a type 1 message is answered with
     * a type 2 message, and a type 3 message is accepted. The messages are not verified.
     */
    private static void stubNtlm(WireMockServer server, String path, String authorization, String authenticate,
            int challengeStatus) {
        server.stubFor(get(urlEqualTo(path)).atPriority(1)
                .withHeader(authorization, matching(NTLM_TYPE_3))
                .willReturn(aResponse().withStatus(200)));
        server.stubFor(get(urlEqualTo(path)).atPriority(2)
                .withHeader(authorization, matching(NTLM_TYPE_1))
                .willReturn(aResponse().withStatus(challengeStatus)
                        .withHeader(authenticate, "NTLM " + Base64.getEncoder().encodeToString(ntlmChallenge()))));
        server.stubFor(get(urlEqualTo(path)).atPriority(10)
                .willReturn(aResponse().withStatus(challengeStatus).withHeader(authenticate, "Negotiate", "NTLM")));
    }

    /** An NTLM type 2 (challenge) message, negotiating Unicode and NTLM, without target information. */
    private static byte[] ntlmChallenge() {
        ByteBuffer message = ByteBuffer.allocate(48).order(ByteOrder.LITTLE_ENDIAN);
        message.put("NTLMSSP\0".getBytes(StandardCharsets.US_ASCII));
        message.putInt(2);
        // empty target name, the security buffer points at the end of the message
        message.putShort((short) 0).putShort((short) 0).putInt(48);
        // NTLMSSP_NEGOTIATE_UNICODE | NTLMSSP_NEGOTIATE_NTLM
        message.putInt(0x00000201);
        message.put(new byte[] {1, 2, 3, 4, 5, 6, 7, 8});
        message.put(new byte[8]);
        // empty target information
        message.putShort((short) 0).putShort((short) 0).putInt(48);
        return message.array();
    }

    private static byte[] ntlmMessage(List<String> headerValues, int type) {
        for (String value : headerValues) {
            if (value.startsWith("NTLM ")) {
                byte[] message = Base64.getDecoder().decode(value.substring("NTLM ".length()));
                if (ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN).getInt(8) == type) {
                    return message;
                }
            }
        }
        throw new AssertionError("no NTLM type " + type + " message was sent: " + headerValues);
    }

    /** Reads the Unicode string a security buffer at the given offset of an NTLM message points to. */
    private static String securityBufferString(byte[] message, int offset) {
        ByteBuffer buffer = ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN);
        int length = Short.toUnsignedInt(buffer.getShort(offset));
        int start = buffer.getInt(offset + 4);
        return new String(message, start, length, StandardCharsets.UTF_16LE);
    }

    private static List<String> headerValues(WireMockServer server, String path, String header) {
        return server.findAll(anyRequestedFor(urlEqualTo(path))).stream()
                .filter(request -> request.containsHeader(header))
                .map(request -> request.getHeader(header))
                .toList();
    }

    private static String basic(String user, String password) {
        return "Basic " + Base64.getEncoder()
                .encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    private static HTTPSamplerBase newSampler(String httpVersion) {
        HTTPSamplerBase sampler = HTTPSamplerFactory.newInstance("HttpClient5");
        sampler.setHttpVersion(httpVersion);
        return sampler;
    }

    /**
     * The request journal is bounded, so that an authentication that does not end fails the test on its
     * timeout, rather than filling the heap with the requests it sends.
     */
    private static WireMockServer createServer() {
        return new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort().maxRequestJournalEntries(100));
    }
}
