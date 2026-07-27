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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;

import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
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

            assertEquals("200", result.getResponseCode());
            assertEquals("HTTP/2", result.getResponseHeaders().substring(0, "HTTP/2".length()));
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

    private static HTTPSamplerBase newSampler() {
        return HTTPSamplerFactory.newInstance("Java");
    }

    private static WireMockServer createServer() {
        return new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
    }
}
