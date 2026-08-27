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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.GZIPOutputStream;

import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import kotlin.text.StringsKt;

public class TestDecompression {
    enum ClientGzip {
        REQUESTED, NOT_REQUESTED
    }

    enum ServerGzip {
        SUPPORTED, NOT_SUPPORTED
    }

    public static List<Arguments> mockServerParams() {
        List<Arguments> res = new ArrayList<>();
        for (String httpImpl : HTTPSamplerFactory.getImplementations()) {
            for (ClientGzip clientGzip : ClientGzip.values()) {
                for (ServerGzip serverGzip : ServerGzip.values()) {
                    res.add(Arguments.of(httpImpl, clientGzip, serverGzip));
                }
            }
        }
        return res;
    }

    public static List<Arguments> implementations() {
        List<Arguments> res = new ArrayList<>();
        for (String httpImpl : HTTPSamplerFactory.getImplementations()) {
            res.add(Arguments.of(httpImpl));
        }
        return res;
    }

    /**
     * A compressed response must be accounted for with the bytes that crossed the wire, not with the
     * size of the decoded body, otherwise switching the implementation of a plan changes the reported
     * bytes and any bandwidth SLA by the compression ratio.
     */
    @ParameterizedTest
    @MethodSource("implementations")
    public void reportsCompressedSizeForHighlyCompressibleBody(String httpImpl) throws IOException {
        WireMockServer server = createServer(c -> c.gzipDisabled(true));
        server.start();
        try {
            String expectedResponse = "a".repeat(400 * 1024);
            byte[] responseBody = encodedResponse(expectedResponse, ClientGzip.REQUESTED, ServerGzip.SUPPORTED);
            Assertions.assertTrue(responseBody.length * 20L < expectedResponse.length(),
                    () -> "the gzipped body of " + responseBody.length
                            + " bytes should be far smaller than the decoded " + expectedResponse.length());

            HTTPSamplerBase http = HTTPSamplerFactory.newInstance(httpImpl);
            HeaderManager hm = new HeaderManager();
            hm.add(new Header("Accept-Encoding", "gzip"));
            http.setHeaderManager(hm);
            server.stubFor(WireMock.get("/gzip")
                    .withHeader("Accept-Encoding", WireMock.equalTo("gzip"))
                    .willReturn(WireMock.aResponse()
                            .withBody(responseBody)
                            .withHeader("Content-Type", "text/plain;charset=utf-8")
                            .withHeader("Content-Length", Long.toString(responseBody.length))
                            .withHeader("Content-Encoding", "gzip")));

            HTTPSampleResult res = http.sample(new URL(server.url("/gzip")), "GET", false, 1);

            Assertions.assertAll(
                    () -> assertEquals(expectedResponse, res.getResponseDataAsString(), "decoded response body"),
                    () -> assertEquals(responseBody.length, res.getBodySizeAsLong(), "wire body size"),
                    () -> assertEquals(responseBody.length + res.getHeadersSize(), res.getBytesAsLong(),
                            "wire response bytes")
            );
        } finally {
            server.stop();
        }
    }

    @ParameterizedTest
    @MethodSource("mockServerParams")
    public void mockServer(String httpImpl, ClientGzip clientGzip, ServerGzip serverGzip) throws IOException {
        WireMockServer server = createServer(c -> c.gzipDisabled(true));
        server.start();
        try {
            HTTPSamplerBase http = HTTPSamplerFactory.newInstance(httpImpl);
            String expectedResponse = "Hello, 丈, \uD83D\uDE03, and नि";
            byte[] responseBody = encodedResponse(expectedResponse, clientGzip, serverGzip);
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
            var response = WireMock.aResponse()
                    .withBody(responseBody)
                    .withHeader("Content-Type", "text/plain;charset=utf-8")
                    .withHeader("Content-Length", Long.toString(responseBody.length));
            if (clientGzip == ClientGzip.REQUESTED && serverGzip == ServerGzip.SUPPORTED) {
                response.withHeader("Content-Encoding", "gzip");
            }
            server.stubFor(mappingBuilder.willReturn(response));

            HTTPSampleResult res = http.sample(new URL(server.url("/gzip")), "GET", false, 1);

            Assertions.assertAll(
                    () -> assertEquals(expectedResponse, res.getResponseDataAsString(), "response body"),
                    () -> assertEquals(responseBody.length + res.getHeadersSize(), res.getBytesAsLong(),
                            "wire response bytes"),
                    () -> {
                        if (clientGzip == ClientGzip.NOT_REQUESTED || serverGzip == ServerGzip.NOT_SUPPORTED) {
                            assertFalse(
                                    StringsKt.contains(res.getResponseHeaders(), "Content-Encoding:", false),
                                    () -> "clientGzip is " + clientGzip + ", so Content-Encoding header should NOT be present"
                            );
                        } else {
                            assertTrue(
                                    StringsKt.contains(res.getResponseHeaders(), "Content-Encoding: gzip", false),
                                    () -> "clientGzip is " + clientGzip + ", so Content-Encoding: gzip header should be present"
                            );
                        }
                    }
            );
        } finally {
            server.stop();
        }
    }

    public static List<Arguments> http2Params() {
        List<Arguments> res = new ArrayList<>();
        for (ClientGzip clientGzip : ClientGzip.values()) {
            for (ServerGzip serverGzip : ServerGzip.values()) {
                res.add(Arguments.of(clientGzip, serverGzip));
            }
        }
        return res;
    }

    /**
     * HttpClient5 uses the async transport for HTTP/2, which decompresses responses in a different
     * code path than the HTTP/1.1 transport, so both have to report the same headers and body.
     */
    @ParameterizedTest
    @MethodSource("http2Params")
    public void http2(ClientGzip clientGzip, ServerGzip serverGzip) throws IOException {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicHttpsPort()
                .http2TlsDisabled(false)
                .gzipDisabled(true));
        server.start();
        try {
            HTTPSamplerBase http = HTTPSamplerFactory.newInstance(HTTPSamplerFactory.IMPL_HTTP_CLIENT5);
            http.setHttpVersion("HTTP/2");
            String expectedResponse = "Hello, 丈, \uD83D\uDE03, and नि";
            byte[] responseBody = encodedResponse(expectedResponse, clientGzip, serverGzip);
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
            var response = WireMock.aResponse()
                    .withBody(responseBody)
                    .withHeader("Content-Type", "text/plain;charset=utf-8")
                    .withHeader("Content-Length", Long.toString(responseBody.length));
            if (clientGzip == ClientGzip.REQUESTED && serverGzip == ServerGzip.SUPPORTED) {
                response.withHeader("Content-Encoding", "gzip");
            }
            server.stubFor(mappingBuilder.willReturn(response));

            HTTPSampleResult res = http.sample(
                    new URL("https://localhost:" + server.httpsPort() + "/gzip"), "GET", false, 1);

            Assertions.assertAll(
                    () -> assertEquals(expectedResponse, res.getResponseDataAsString(), "response body"),
                    () -> assertEquals(responseBody.length + res.getHeadersSize(), res.getBytesAsLong(),
                            "wire response bytes"),
                    () -> {
                        // HTTP/2 header names are lower case, so compare them ignoring case
                        if (clientGzip == ClientGzip.NOT_REQUESTED || serverGzip == ServerGzip.NOT_SUPPORTED) {
                            assertFalse(
                                    StringsKt.contains(res.getResponseHeaders(), "Content-Encoding:", true),
                                    () -> "clientGzip is " + clientGzip + ", so Content-Encoding header should NOT be present"
                            );
                        } else {
                            assertTrue(
                                    StringsKt.contains(res.getResponseHeaders(), "Content-Encoding: gzip", true),
                                    () -> "clientGzip is " + clientGzip + ", so Content-Encoding: gzip header should be present"
                            );
                        }
                    }
            );
        } finally {
            server.stop();
        }
    }

    private static WireMockServer createServer(Consumer<WireMockConfiguration> config) {
        WireMockConfiguration configuration =
                WireMockConfiguration
                        .wireMockConfig()
                        .dynamicPort();
        config.accept(configuration);
        return new WireMockServer(configuration);
    }

    private static byte[] encodedResponse(String response, ClientGzip clientGzip, ServerGzip serverGzip)
            throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        if (clientGzip == ClientGzip.NOT_REQUESTED || serverGzip == ServerGzip.NOT_SUPPORTED) {
            return bytes;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
            gzip.write(bytes);
        }
        return output.toByteArray();
    }
}
