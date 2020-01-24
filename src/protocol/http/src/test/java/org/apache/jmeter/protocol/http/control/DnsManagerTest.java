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

package org.apache.jmeter.protocol.http.control;

import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.sampler.ResultAsString;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.wiremock.WireMockExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.tomakehurst.wiremock.WireMockServer;

@ExtendWith(WireMockExtension.class)
public class DnsManagerTest {
    @ParameterizedTest
    @MethodSource("org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory#getImplementations")
    void badDnsInCustomResolverShouldFailHttpSampler(String httpImplementation, WireMockServer server) {
        Assumptions.assumeTrue(!HTTPSamplerFactory.IMPL_JAVA.equals(httpImplementation),
                "Java implementation does not support custom DNS resolver yet");
        DNSCacheManager dns = new DNSCacheManager();
        dns.setCustomResolver(true);
        dns.addServer("20.0.118.11");
        // By default it uses 3 retries (see org.xbill.DNS.ExtendedResolver#setRetries)
        dns.setTimeoutMs(2000);
        HTTPSamplerBase http = HTTPSamplerFactory.newInstance(httpImplementation);
        http.setDNSResolver(dns);
        http.setMethod(HTTPSampler.GET);
        http.setPort(server.port());
        http.setDomain("localhost");
        http.setPath("/index.html");

        http.setRunningVersion(true);

        SampleResult result = http.sample();
        Assertions.assertEquals(
                "Non HTTP response message: Failed to resolve host name: localhost",
                result.getResponseMessage(), () ->
                        "HTTP is using a custom DNS resolver, so it must fail resolving localhost \n" +
                                ResultAsString.toString(result));
    }
}
