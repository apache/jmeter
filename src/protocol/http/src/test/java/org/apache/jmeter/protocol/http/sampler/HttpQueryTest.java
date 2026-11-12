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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

public class HttpQueryTest extends JMeterTestCase implements JMeterSerialTest {

    @Test
    void querySendsRequestBody() throws Exception {
        String expectedBody = "{\"name\":\"Ori\"}";
        AtomicReference<String> receivedMethod = new AtomicReference<>();
        AtomicReference<String> receivedBody = new AtomicReference<>();

        // Port 0 lets the operating system select an available port.
        HttpServer server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);

        server.createContext("/search", exchange -> {
            try {
                receivedMethod.set(exchange.getRequestMethod());
                receivedBody.set(new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8));

                byte[] response = "OK".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, response.length);
                try (var output = exchange.getResponseBody()) {
                    output.write(response);
                }
            } finally {
                exchange.close();
            }
        });

        server.start();
        try {
            HTTPSamplerProxy sampler =
                    new HTTPSamplerProxy(HTTPSamplerFactory.IMPL_HTTP_CLIENT4);
            sampler.setProtocol("http");
            sampler.setDomain("127.0.0.1");
            sampler.setPort(server.getAddress().getPort());
            sampler.setPath("/search");
            sampler.setMethod("QUERY");
            sampler.setConnectTimeout("5000");
            sampler.setResponseTimeout("5000");
            sampler.setPostBodyRaw(true);
            sampler.addNonEncodedArgument("", expectedBody, "");

            SampleResult result = sampler.sample();

            assertTrue(result.isSuccessful(),
                    "Sampler failed: " + result.getResponseMessage());
            assertEquals("200", result.getResponseCode());
            assertEquals("QUERY", receivedMethod.get());
            assertEquals(expectedBody, receivedBody.get());
        } finally {
            server.stop(0);
        }
    }
}
