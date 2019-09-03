/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.visualizers.backend.influxdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HttpMetricsSenderTest {

    private HttpServer server;
    private HttpRequest request;

    @Before
    public void startServer() {

        HttpRequestHandler requestHandler = (request, response, context) -> {
            HttpMetricsSenderTest.this.request = request;
            response.setStatusCode(HttpStatus.SC_NO_CONTENT);
        };

        // Start HttpServer on free port
        server = IntStream
                .range(8183, 8283)
                .mapToObj(port -> {
                    HttpServer httpServer = ServerBootstrap.bootstrap()
                            .setListenerPort(8183)
                            .registerHandler("*", requestHandler)
                            .create();
                    try {
                        httpServer.start();
                        return httpServer;
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Cannot start HttpServer"));
    }

    @After
    public void stopServer() {
        server.shutdown(1, TimeUnit.SECONDS);
    }

    @Test
    public void checkTokenDoesNotPresentInHeader() throws Exception {
        String influxdbUrl = String.format("http://localhost:%s/api/v2/write", server.getLocalPort());
        HttpMetricsSender metricsSender = new HttpMetricsSender();
        metricsSender.setup(influxdbUrl, null);
        metricsSender.addMetric("measurement", "location=west", "size=10");
        metricsSender.writeAndSendMetrics();

        do {
            Thread.sleep(100);
        } while (request == null);

        assertNull(
                "The authorization header shouldn't be defined.",
                request.getFirstHeader("Authorization"));
    }

    @Test
    public void checkEmptyTokenDoesNotPresentInHeader() throws Exception {
        String influxdbUrl = String.format("http://localhost:%s/api/v2/write", server.getLocalPort());
        HttpMetricsSender metricsSender = new HttpMetricsSender();
        metricsSender.setup(influxdbUrl, "");
        metricsSender.addMetric("measurement", "location=west", "size=10");
        metricsSender.writeAndSendMetrics();

        do {
            Thread.sleep(100);
        } while (request == null);

        assertNull(
                "The authorization header shouldn't be defined.",
                request.getFirstHeader("Authorization"));
    }

    @Test
    public void checkEmptyOnlyWhitespaceTokenDoesNotPresentInHeader() throws Exception {
        String influxdbUrl = String.format("http://localhost:%s/api/v2/write", server.getLocalPort());
        HttpMetricsSender metricsSender = new HttpMetricsSender();
        metricsSender.setup(influxdbUrl, "  ");
        metricsSender.addMetric("measurement", "location=west", "size=10");
        metricsSender.writeAndSendMetrics();

        do {
            Thread.sleep(100);
        } while (request == null);

        assertNull(
                "The authorization header shouldn't be defined.",
                request.getFirstHeader("Authorization"));
    }

    @Test
    public void checkTokenPresentInHeader() throws Exception {
        String influxdbUrl = String.format("http://localhost:%s/api/v2/write", server.getLocalPort());
        HttpMetricsSender metricsSender = new HttpMetricsSender();
        metricsSender.setup(influxdbUrl, "my-token");
        metricsSender.addMetric("measurement", "location=west", "size=10");
        metricsSender.writeAndSendMetrics();

        do {
            Thread.sleep(100);
        } while (request == null);

        assertEquals(
                "The authorization header should be: 'Token my-token'",
                request.getFirstHeader("Authorization").getValue(),
                "Token my-token");
    }
}
