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
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

class TestRedirects {

    public static List<Arguments> redirectionParams() {
        List<Arguments> res = new ArrayList<>();
        List<String> httpMethods = Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE");
        for (String httpImpl : HTTPSamplerFactory.getImplementations()) {
            for (int statusCode : Arrays.asList(301, 302, 303, 307, 308)) {
                for (String method : httpMethods) {
                    res.add(Arguments.of(httpImpl, statusCode, true, method));
                }
            }
            for (int statusCode : Arrays.asList(300, 304, 305, 306)) {
                for (String method : httpMethods) {
                    res.add(Arguments.of(httpImpl, statusCode, false, method));
                }
            }
        }
        return res;
    }

    @ParameterizedTest
    @MethodSource("redirectionParams")
    void testRedirect(String httpImpl, int redirectCode, boolean shouldRedirect, String method)
            throws MalformedURLException {
        WireMockServer server = createServer();
        server.start();
        try {
            HTTPSamplerBase http = HTTPSamplerFactory.newInstance(httpImpl);
            http.setAutoRedirects(false);
            server.stubFor(any(urlPathEqualTo("/some-location")).willReturn(
                    aResponse().withHeader("Location", server.url("/redirected")).withStatus(redirectCode)));
            HTTPSampleResult res = http.sample(new URL(server.url("/some-location")), method, false, 1);
            if (shouldRedirect) {
                Assertions.assertEquals(server.url("/redirected"), res.getRedirectLocation());
            } else {
                Assertions.assertNull(res.getRedirectLocation());
            }
            Assertions.assertEquals("" + redirectCode, res.getResponseCode());
        } finally {
            server.stop();
        }
    }

    public static List<Arguments> methodPreservationParams() {
        List<Arguments> res = new ArrayList<>();
        List<String> httpMethods = Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE");
        for (String httpImpl : HTTPSamplerFactory.getImplementations()) {
            for (int statusCode : Arrays.asList(301, 302, 303, 307, 308)) {
                for (String method : httpMethods) {
                    String expectedMethod = switch (statusCode) {
                        case 307, 308 -> method;
                        default -> "HEAD".equals(method) ? "HEAD" : "GET";
                    };
                    res.add(Arguments.of(httpImpl, statusCode, method, expectedMethod));
                }
            }
        }
        return res;
    }

    @ParameterizedTest
    @MethodSource("methodPreservationParams")
    void testMethodPreservationOnRedirect(String httpImpl, int redirectCode,
            String originalMethod, String expectedMethod) throws MalformedURLException {
        WireMockServer server = createServer();
        server.start();
        try {
            HTTPSamplerBase http = HTTPSamplerFactory.newInstance(httpImpl);
            http.setAutoRedirects(false);
            http.setFollowRedirects(true);

            server.stubFor(any(urlPathEqualTo("/original")).willReturn(
                    aResponse().withHeader("Location", server.url("/target"))
                               .withStatus(redirectCode)));
            server.stubFor(WireMock.request(expectedMethod, urlPathEqualTo("/target"))
                    .atPriority(1)
                    .willReturn(aResponse().withStatus(200)));
            server.stubFor(any(urlPathEqualTo("/target"))
                    .atPriority(10)
                    .willReturn(aResponse().withStatus(405)));

            HTTPSampleResult res = http.sample(new URL(server.url("/original")),
                    originalMethod, false, 1);

            Assertions.assertEquals("200", res.getResponseCode(),
                    String.format("[%s] %s %d: expected final 200", httpImpl, originalMethod, redirectCode));
            Assertions.assertEquals(expectedMethod, res.getHTTPMethod(),
                    String.format("[%s] %s %d: expected method %s", httpImpl, originalMethod, redirectCode, expectedMethod));

            server.verify(1, new RequestPatternBuilder(
                    RequestMethod.fromString(expectedMethod), urlPathEqualTo("/target")));
        } finally {
            server.stop();
        }
    }

    public static List<Arguments> redirectChainParams() {
        return Arrays.stream(HTTPSamplerFactory.getImplementations())
                .map(Arguments::of)
                .collect(Collectors.toList());
    }

    @ParameterizedTest
    @MethodSource("redirectChainParams")
    void testRedirectChain307Then301(String httpImpl) throws MalformedURLException {
        WireMockServer server = createServer();
        server.start();
        try {
            HTTPSamplerBase http = HTTPSamplerFactory.newInstance(httpImpl);
            http.setAutoRedirects(false);
            http.setFollowRedirects(true);

            // POST /a -> 307 -> POST /b -> 301 -> GET /c
            server.stubFor(any(urlPathEqualTo("/a")).willReturn(
                    aResponse().withHeader("Location", server.url("/b")).withStatus(307)));
            server.stubFor(any(urlPathEqualTo("/b")).willReturn(
                    aResponse().withHeader("Location", server.url("/c")).withStatus(301)));
            server.stubFor(any(urlPathEqualTo("/c")).willReturn(
                    aResponse().withStatus(200)));

            HTTPSampleResult res = http.sample(new URL(server.url("/a")), "POST", false, 1);

            Assertions.assertEquals("200", res.getResponseCode());
            Assertions.assertEquals("GET", res.getHTTPMethod());

            // /b should receive POST (307 preserves method), /c should receive GET (301 converts)
            server.verify(1, new RequestPatternBuilder(RequestMethod.POST, urlPathEqualTo("/b")));
            server.verify(1, new RequestPatternBuilder(RequestMethod.GET, urlPathEqualTo("/c")));
        } finally {
            server.stop();
        }
    }

    private static WireMockServer createServer() {
        WireMockConfiguration configuration = WireMockConfiguration.wireMockConfig().dynamicPort();
        return new WireMockServer(configuration);
    }

}
