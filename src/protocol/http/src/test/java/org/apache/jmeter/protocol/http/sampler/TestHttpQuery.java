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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;


@WireMockTest
class TestHttpQuery extends JMeterTestCase {

        private static HTTPSamplerProxy querySampler(String impl, WireMockRuntimeInfo server) {
            HTTPSamplerProxy sampler = new HTTPSamplerProxy(impl);
            sampler.setProtocol("http");
            sampler.setDomain("localhost");
            sampler.setPort(server.getHttpPort());
            sampler.setPath("/search");
            sampler.setMethod(HTTPConstants.QUERY);
            sampler.setConnectTimeout("5000");
            sampler.setResponseTimeout("5000");
            return sampler;
        }

        @Test
        void queryIsOfferedInMethodList() {
            assertTrue(Arrays.asList(HTTPSamplerBase.getValidMethodsAsArray()).contains(HTTPConstants.QUERY));
        }

        @ParameterizedTest
        @ValueSource(strings = {HTTPSamplerFactory.IMPL_HTTP_CLIENT4, HTTPSamplerFactory.IMPL_HTTP_CLIENT5})
        void querySendsRawBody(String impl, WireMockRuntimeInfo server) {
            server.getWireMock().register(request("QUERY", urlEqualTo("/search"))
                    .willReturn(aResponse().withStatus(200).withBody("OK")));
            HTTPSamplerProxy sampler = querySampler(impl, server);
            sampler.setPostBodyRaw(true);
            sampler.addNonEncodedArgument("", "{\"q\":\"jmeter\"}", "");
            SampleResult res = sampler.sample();
            assertTrue(res.isSuccessful(), res.getResponseMessage());
            server.getWireMock().verifyThat(1, requestedFor("QUERY", urlEqualTo("/search"))
                    .withRequestBody(equalTo("{\"q\":\"jmeter\"}")));
        }

        @ParameterizedTest
        @ValueSource(strings = {HTTPSamplerFactory.IMPL_HTTP_CLIENT4, HTTPSamplerFactory.IMPL_HTTP_CLIENT5})
        void querySendsFormParameters(String impl, WireMockRuntimeInfo server) {
            server.getWireMock().register(request("QUERY", urlEqualTo("/search"))
                    .willReturn(aResponse().withStatus(200)));
            HTTPSamplerProxy sampler = querySampler(impl, server);
            sampler.addArgument("a", "1");
            sampler.addArgument("b", "x y");
            SampleResult res = sampler.sample();
            assertTrue(res.isSuccessful(), res.getResponseMessage());
            server.getWireMock().verifyThat(1, requestedFor("QUERY", urlEqualTo("/search"))
                    .withRequestBody(equalTo("a=1&b=x+y")));
        }

        @Test
        void javaImplementationRejectsQuery(WireMockRuntimeInfo server) {
            SampleResult res = querySampler(HTTPSamplerFactory.IMPL_JAVA, server).sample();
            assertFalse(res.isSuccessful());
        }
}
