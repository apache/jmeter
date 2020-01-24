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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class TestDecompression {
    enum ClientGzip {
        REQUESTED, NOT_REQUESTED
    }

    enum ServerGzip {
        SUPPORTED, NOT_SUPPORTED
    }

    public static List<Arguments> mockServerParams() {
        List<Arguments> res = new ArrayList<>();
        // Nested for depth is 2 (max allowed is 1). [NestedForDepth]
        Arrays.stream(HTTPSamplerFactory.getImplementations()).forEach(httpImpl -> {
            for (ClientGzip clientGzip : ClientGzip.values()) {
                for (ServerGzip serverGzip : ServerGzip.values()) {
                    res.add(Arguments.of(httpImpl, clientGzip, serverGzip));
                }
            }
        });
        return res;
    }

    @ParameterizedTest
    @MethodSource("mockServerParams")
    public void mockServer(String httpImpl, ClientGzip clientGzip, ServerGzip serverGzip) throws MalformedURLException {
        WireMockServer server = createServer(c -> c.gzipDisabled(serverGzip == ServerGzip.NOT_SUPPORTED));
        server.start();
        try {
            HTTPSamplerBase http = HTTPSamplerFactory.newInstance(httpImpl);
            String expectedResponse = "Hello, 丈, \uD83D\uDE03, and नि";
            HeaderManager hm = new HeaderManager();
            if (clientGzip == ClientGzip.REQUESTED) {
                hm.add(new Header("Accept-Encoding", "gzip"));
            }
            hm.add(new Header("Content-Encoding", "utf-8"));
            http.setHeaderManager(hm);
            MappingBuilder mappingBuilder = WireMock.get("/gzip");
            if (clientGzip == ClientGzip.REQUESTED) {
                mappingBuilder = mappingBuilder.withHeader("Accept-Encoding", WireMock.equalTo("gzip"));
            }
            server.stubFor(
                    mappingBuilder
                            .willReturn(
                                    WireMock.aResponse()
                                            .withBody(expectedResponse)
                                            .withHeader("Content-Type", "text/plain;charset=utf-8")
                            )
            );

            HTTPSampleResult res = http.sample(new URL(server.url("/gzip")), "GET", false, 1);

            Assertions.assertAll(
                    () -> {
                        Matcher<String> matcher;
                        if (clientGzip == ClientGzip.NOT_REQUESTED || serverGzip == ServerGzip.NOT_SUPPORTED) {
                            matcher = Matchers.not(Matchers.containsStringIgnoringCase("Content-Encoding:"));
                        } else {
                            matcher = Matchers.containsStringIgnoringCase("Content-Encoding: gzip");
                        }
                        MatcherAssert.assertThat("getResponseHeaders", res.getResponseHeaders(), matcher);
                    }, () -> {
                        Assertions.assertEquals(expectedResponse, res.getResponseDataAsString(), "response body");
                    }
            );
        } finally {
            server.stop();
        }
    }

    private WireMockServer createServer(Consumer<WireMockConfiguration> config) {
        WireMockConfiguration configuration =
                WireMockConfiguration
                        .wireMockConfig()
                        .dynamicPort();
        config.accept(configuration);
        return new WireMockServer(configuration);
    }
}
