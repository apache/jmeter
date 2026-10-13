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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

/**
 * Redirects the HttpClient5 sampler implementation follows on its own, when "Redirect Automatically" is
 * selected. Each hop is sent with the cookies and credentials the Cookie and the Authorization Manager hold
 * for its URL, like the samples of "Follow Redirects" are, so a hop to another origin is followed instead of
 * being refused for the headers of the previous origin, and those headers do not leak to it.
 */
class TestHTTPHC5Redirects {

    @AfterEach
    void closeClientsOfThisThread() {
        HTTPHC5Impl.closeClientsOfCurrentThread();
    }

    /**
     * A login flow through an identity provider: the application redirects to the identity provider, which
     * redirects back to the application. The two origins are told apart by their host name, so they also
     * keep separate cookies.
     */
    @ParameterizedTest
    @CsvSource({"HTTP/1.1, http", "HTTP/2, https"})
    void followsRedirectsAcrossOriginsWithTheCookiesAndCredentialsOfEachOrigin(String httpVersion, String scheme)
            throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            String app = origin(server, scheme, "localhost");
            String idp = origin(server, scheme, "127.0.0.1");
            server.stubFor(get(urlEqualTo("/app/start")).willReturn(aResponse().withStatus(302)
                    .withHeader(HTTPConstants.HEADER_LOCATION, idp + "/idp/login")
                    .withHeader(HTTPConstants.HEADER_SET_COOKIE, "state=xyz; Path=/")));
            server.stubFor(get(urlEqualTo("/idp/login")).willReturn(aResponse().withStatus(302)
                    .withHeader(HTTPConstants.HEADER_LOCATION, app + "/app/callback")
                    .withHeader(HTTPConstants.HEADER_SET_COOKIE, "idp-session=1; Path=/")));
            server.stubFor(get(urlEqualTo("/app/callback")).willReturn(aResponse().withStatus(200)));
            CookieManager cookieManager = new CookieManager();
            cookieManager.testStarted();
            cookieManager.addCookieFromHeader("app-session=1; Path=/", new URL(app + "/"));
            cookieManager.addCookieFromHeader("idp-remembered=1; Path=/", new URL(idp + "/"));
            AuthManager authManager = new AuthManager();
            authManager.set(-1, app + "/", "appuser", "apppass", "", "", AuthManager.Mechanism.BASIC);
            authManager.set(-1, idp + "/", "idpuser", "idppass", "", "", AuthManager.Mechanism.BASIC);
            HeaderManager headerManager = new HeaderManager();
            headerManager.add(new Header("X-Test", "kept"));
            HTTPSamplerBase sampler = newSampler(httpVersion);
            sampler.setCookieManager(cookieManager);
            sampler.setAuthManager(authManager);
            sampler.setHeaderManager(headerManager);

            HTTPSampleResult result = sampler.sample(new URL(app + "/app/start"), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode(), result::getResponseMessage);
            assertEquals(app + "/app/callback", result.getUrlAsString());
            assertTrue(result.getResponseHeaders().startsWith(httpVersion),
                    () -> "expected " + httpVersion + ", got " + result.getResponseHeaders());

            LoggedRequest login = singleRequest(server, "/idp/login");
            assertEquals("idp-remembered=1", cookies(login),
                    "the identity provider must get its own cookies only, not those of the application");
            assertEquals(basic("idpuser", "idppass"), login.getHeader(HTTPConstants.HEADER_AUTHORIZATION),
                    "the identity provider must get its own credentials, not those of the application");
            assertEquals("kept", login.getHeader("X-Test"));

            LoggedRequest callback = singleRequest(server, "/app/callback");
            assertTrue(cookies(callback).contains("app-session=1"), () -> cookies(callback));
            assertTrue(cookies(callback).contains("state=xyz"),
                    () -> "the cookie the application set on its redirect must be sent back, got "
                            + cookies(callback));
            assertFalse(cookies(callback).contains("idp-"), () -> cookies(callback));
            assertEquals(basic("appuser", "apppass"), callback.getHeader(HTTPConstants.HEADER_AUTHORIZATION));

            assertTrue(cookieManager.getCookieHeaderForURL(new URL(idp + "/")).contains("idp-session=1"),
                    "the cookie the identity provider set on its redirect must be kept in the Cookie Manager");
        } finally {
            server.stop();
        }
    }

    @ParameterizedTest
    @CsvSource({"HTTP/1.1, http", "HTTP/2, https"})
    void followsAsManyRedirectsAsHttpsamplerMaxRedirectsAllows(String httpVersion, String scheme) throws Exception {
        int maxRedirects = HTTPSamplerBase.MAX_REDIRECTS;
        WireMockServer server = createServer();
        server.start();
        try {
            for (int hop = 0; hop <= maxRedirects; hop++) {
                server.stubFor(get(urlEqualTo("/hop/" + hop)).willReturn(aResponse().withStatus(302)
                        .withHeader(HTTPConstants.HEADER_LOCATION, "/hop/" + (hop + 1))));
            }
            server.stubFor(get(urlEqualTo("/hop/" + (maxRedirects + 1))).willReturn(aResponse().withStatus(200)));
            String base = origin(server, scheme, "localhost");

            HTTPSampleResult withinLimit = newSampler(httpVersion)
                    .sample(new URL(base + "/hop/1"), HTTPConstants.GET, false, 1);
            HTTPSampleResult beyondLimit = newSampler(httpVersion)
                    .sample(new URL(base + "/hop/0"), HTTPConstants.GET, false, 1);

            assertEquals("200", withinLimit.getResponseCode(), withinLimit::getResponseMessage);
            assertFalse(beyondLimit.isSuccessful(), "one redirect more than httpsampler.max_redirects must fail");
            assertTrue(beyondLimit.getResponseDataAsString()
                            .contains("Maximum redirects (" + maxRedirects + ") exceeded"),
                    beyondLimit::getResponseDataAsString);
        } finally {
            server.stop();
        }
    }

    /**
     * Cookies are handled by the Cookie Manager, so without one no cookie is sent, neither on a redirect
     * nor by a later sample, like with the other sampler implementations.
     */
    @ParameterizedTest
    @CsvSource({"HTTP/1.1, http", "HTTP/2, https"})
    void sendsNoCookiesWithoutCookieManager(String httpVersion, String scheme) throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/set")).willReturn(aResponse().withStatus(302)
                    .withHeader(HTTPConstants.HEADER_LOCATION, "/next")
                    .withHeader(HTTPConstants.HEADER_SET_COOKIE, "unmanaged=1; Path=/")));
            server.stubFor(get(urlEqualTo("/next")).willReturn(aResponse().withStatus(200)));
            String base = origin(server, scheme, "localhost");
            HTTPSamplerBase sampler = newSampler(httpVersion);

            assertEquals("200", sampler.sample(new URL(base + "/set"), HTTPConstants.GET, false, 1)
                    .getResponseCode());
            assertEquals("200", sampler.sample(new URL(base + "/next"), HTTPConstants.GET, false, 1)
                    .getResponseCode());

            List<LoggedRequest> requests = server.findAll(anyRequestedFor(urlEqualTo("/next")));
            assertEquals(2, requests.size());
            for (LoggedRequest request : requests) {
                assertEquals("", cookies(request), "no cookie must be sent without a Cookie Manager");
            }
        } finally {
            server.stop();
        }
    }

    /**
     * POST → 302 → GET: HttpClient changes the method and builds a GET with no headers. The Header Manager
     * headers must survive the redirect, except Cookie and Authorization which are handled by their managers.
     */
    @ParameterizedTest
    @CsvSource({"HTTP/1.1, http", "HTTP/2, https"})
    void postRedirectGetPreservesHeaderManagerHeaders(String httpVersion, String scheme) throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            String base = origin(server, scheme, "localhost");
            server.stubFor(post(urlEqualTo("/login")).willReturn(aResponse().withStatus(302)
                    .withHeader(HTTPConstants.HEADER_LOCATION, base + "/home")));
            server.stubFor(get(urlEqualTo("/home")).willReturn(aResponse().withStatus(200)));
            HeaderManager headerManager = new HeaderManager();
            headerManager.add(new Header("X-Custom", "value1"));
            headerManager.add(new Header("Accept", "application/json"));
            HTTPSamplerBase sampler = newSampler(httpVersion);
            sampler.setHeaderManager(headerManager);

            HTTPSampleResult result = sampler.sample(new URL(base + "/login"), HTTPConstants.POST, false, 1);

            assertEquals("200", result.getResponseCode(), result::getResponseMessage);
            LoggedRequest home = singleRequest(server, "/home");
            assertEquals("value1", home.getHeader("X-Custom"),
                    "Header Manager X-Custom header must survive a POST → 302 → GET redirect");
            assertEquals("application/json", home.getHeader("Accept"),
                    "Header Manager Accept header must survive a POST → 302 → GET redirect");
        } finally {
            server.stop();
        }
    }

    /**
     * A Header Manager {@code Authorization} header must not be sent to a different origin on redirect,
     * to prevent credential leaking.
     */
    @ParameterizedTest
    @CsvSource({"HTTP/1.1, http", "HTTP/2, https"})
    void headerManagerAuthorizationIsNotSentToAnotherOrigin(String httpVersion, String scheme) throws Exception {
        WireMockServer server = createServer();
        server.start();
        try {
            String app = origin(server, scheme, "localhost");
            String other = origin(server, scheme, "127.0.0.1");
            server.stubFor(get(urlEqualTo("/start")).willReturn(aResponse().withStatus(302)
                    .withHeader(HTTPConstants.HEADER_LOCATION, other + "/external")));
            server.stubFor(get(urlEqualTo("/external")).willReturn(aResponse().withStatus(200)));
            HeaderManager headerManager = new HeaderManager();
            headerManager.add(new Header(HTTPConstants.HEADER_AUTHORIZATION, "Bearer my-token"));
            headerManager.add(new Header("X-Test", "kept"));
            HTTPSamplerBase sampler = newSampler(httpVersion);
            sampler.setHeaderManager(headerManager);

            HTTPSampleResult result = sampler.sample(new URL(app + "/start"), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode(), result::getResponseMessage);
            LoggedRequest start = singleRequest(server, "/start");
            assertEquals("Bearer my-token", start.getHeader(HTTPConstants.HEADER_AUTHORIZATION),
                    "the Authorization header must be sent to the sampled origin");
            assertEquals("kept", start.getHeader("X-Test"));

            LoggedRequest external = singleRequest(server, "/external");
            assertFalse(external.containsHeader(HTTPConstants.HEADER_AUTHORIZATION),
                    "the Header Manager Authorization must not be sent to a different origin");
            assertEquals("kept", external.getHeader("X-Test"),
                    "non-sensitive Header Manager headers must still be sent to a different origin");
        } finally {
            server.stop();
        }
    }

    private static HTTPSamplerBase newSampler(String httpVersion) {
        HTTPSamplerBase sampler = HTTPSamplerFactory.newInstance("HttpClient5");
        sampler.setHttpVersion(httpVersion);
        sampler.setAutoRedirects(true);
        return sampler;
    }

    private static WireMockServer createServer() {
        return new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicPort()
                .dynamicHttpsPort()
                .http2TlsDisabled(false));
    }

    private static String origin(WireMockServer server, String scheme, String host) {
        int port = HTTPConstants.PROTOCOL_HTTPS.equals(scheme) ? server.httpsPort() : server.port();
        return scheme + "://" + host + ":" + port;
    }

    private static LoggedRequest singleRequest(WireMockServer server, String path) {
        List<LoggedRequest> requests = server.findAll(anyRequestedFor(urlEqualTo(path)));
        assertEquals(1, requests.size(), () -> "expected exactly one request for " + path + ", got " + requests);
        return requests.get(0);
    }

    /** All cookies of the request, which HTTP/2 allows to be split over several {@code Cookie} headers. */
    private static String cookies(LoggedRequest request) {
        if (!request.containsHeader(HTTPConstants.HEADER_COOKIE)) {
            return "";
        }
        return String.join("; ", request.header(HTTPConstants.HEADER_COOKIE).values());
    }

    private static String basic(String user, String password) {
        return "Basic " + Base64.getEncoder()
                .encodeToString((user + ":" + password).getBytes(StandardCharsets.ISO_8859_1));
    }
}
