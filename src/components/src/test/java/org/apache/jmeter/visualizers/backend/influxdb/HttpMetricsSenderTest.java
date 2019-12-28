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

package org.apache.jmeter.visualizers.backend.influxdb;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.wiremock.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

@ExtendWith(WireMockExtension.class)
public class HttpMetricsSenderTest {
    private static final String API_URL = "/api/v2/write";

    private MappingBuilder influxRequest(CountDownLatch latch) {
        return WireMock.post(API_URL)
                .willReturn(WireMock.aResponse().withStatus(HttpURLConnection.HTTP_NO_CONTENT))
                .withPostServeAction("countdown", Parameters.one("latch", latch));
    }

    static Collection<Arguments> emptyTokens() {
        return Arrays.asList(arguments((String) null), arguments(""), arguments(" "));
    }

    @ParameterizedTest(name = "[{index}] token=\"{0}\"")
    @MethodSource("emptyTokens")
    public void emptyTokenIsNotSentAsAuthorizedHeader(String token, WireMockServer server) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        server.stubFor(influxRequest(latch));

        setupSenderAndSendMetric(server.url(API_URL), token);

        latch.await(2, TimeUnit.SECONDS);
        assertAuthHeader(server, WireMock.absent());
    }

    @Test
    public void checkTokenPresentInHeader(WireMockServer server) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        server.stubFor(influxRequest(latch));

        setupSenderAndSendMetric(server.url(API_URL), "my-token");

        latch.await(2, TimeUnit.SECONDS);
        assertAuthHeader(server, WireMock.equalTo("Token my-token"));
    }

    private void setupSenderAndSendMetric(String influxdbUrl, String influxDBToken) throws Exception {
        HttpMetricsSender metricsSender = new HttpMetricsSender();
        metricsSender.setup(influxdbUrl, influxDBToken);
        metricsSender.addMetric("measurement", ",location=west", "size=10");
        metricsSender.writeAndSendMetrics();
        metricsSender.destroy();
    }

    private void assertAuthHeader(WireMockServer server, StringValuePattern authHeader) {
        server.verify(1, RequestPatternBuilder
                .newRequestPattern(RequestMethod.POST, WireMock.urlEqualTo(API_URL))
                .withRequestBody(WireMock.matching("measurement,location=west size=10 \\d{19}\\s*"))
                .withHeader("Authorization", authHeader));
    }
}
