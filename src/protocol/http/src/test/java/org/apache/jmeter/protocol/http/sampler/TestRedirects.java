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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

class TestRedirects {

    public static List<Arguments> redirectionParams() {
        List<Arguments> res = new ArrayList<>();
        // Nested for depth is 2 (max allowed is 1). [NestedForDepth]
        List<String> httpMethods = Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE");
        Arrays.stream(HTTPSamplerFactory.getImplementations()).forEach(httpImpl -> {
            for (int statusCode : Arrays.asList(301, 302, 303, 307, 308)) {
                for (String method : httpMethods) {
                    boolean shouldRedirect = statusCode != 307 || ("GET".equals(method) || "HEAD".equals(method));
                    res.add(Arguments.of(httpImpl, statusCode, shouldRedirect, method));
                }
            }
            for (int statusCode : Arrays.asList(300, 304, 305, 306)) {
                for (String method : httpMethods) {
                    res.add(Arguments.of(httpImpl, statusCode, false, method));
                }
            }
        });
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
                Assertions.assertEquals(null, res.getRedirectLocation());
            }
            Assertions.assertEquals("" + redirectCode, res.getResponseCode());
        } finally {
            server.stop();
        }
    }

    private WireMockServer createServer() {
        WireMockConfiguration configuration = WireMockConfiguration.wireMockConfig().dynamicPort();
        return new WireMockServer(configuration);
    }

}
