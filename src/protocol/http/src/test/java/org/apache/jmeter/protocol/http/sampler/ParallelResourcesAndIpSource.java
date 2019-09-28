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

package org.apache.jmeter.protocol.http.sampler;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Optional;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.protocol.http.parser.LagartoBasedHtmlParser;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.FunctionProperty;
import org.apache.jmeter.testkit.BugId;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterContextExtension;
import org.apache.jmeter.wiremock.WireMockExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

@BugId("52310")
@ExtendWith(WireMockExtension.class)
@ExtendWith(JMeterContextExtension.class)
public class ParallelResourcesAndIpSource {

    private void configureStubs(WireMockServer server) {
        server.stubFor(WireMock.get("/index.html")
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "text/html; encoding=utf-8")
                        .withBody("<html><body><img src='image1.png'><img src='image2.png'><img src='image3.png'></body>")));
        for (int i = 1; i <= 3; i++) {
            server.stubFor(WireMock.get("/image" + i + ".png")
                    .willReturn(WireMock.aResponse()
                            .withHeader("Content-Type", "image/png")
                            .withBody("content" + i)));
        }
    }

    @ParameterizedTest
    @MethodSource("org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory#getImplementations")
    public void test(String httpImplementation, WireMockServer server, JMeterVariables vars) throws SocketException {
        configureStubs(server);

        HTTPSamplerBase http = HTTPSamplerFactory.newInstance(httpImplementation);
        Optional<InterfaceAddress> localIp =
                Collections.list(NetworkInterface.getNetworkInterfaces())
                        .stream()
                        .filter(networkInterface -> {
                            try {
                                return networkInterface.isLoopback();
                            } catch (SocketException e) {
                                return false;
                            }
                        })
                        .flatMap(ni -> ni.getInterfaceAddresses().stream())
                        .findFirst();

        if (!localIp.isPresent()) {
            // It should not happen
            Assertions.fail("Unable to find local IP to use as a source IP");
        }

        vars.put("IP_ADDR", localIp.get().getAddress().getHostAddress());

        // Typically it is registered with jmeter.properties, however we don't run full JMeter in the test
        HTTPSamplerBase.registerParser("text/html", LagartoBasedHtmlParser.class.getName());

        http.setConnectTimeout("2000");
        http.setResponseTimeout("4000");
        http.setMethod(HTTPConstants.GET);
        http.setDomain("localhost");
        http.setPort(server.port());
        http.setPath("/index.html");
        http.setFollowRedirects(true);
        http.setUseKeepAlive(true);
        http.setProperty(new FunctionProperty(HTTPSampler.IP_SOURCE, new CompoundVariable("${IP_ADDR}")));
        http.setImageParser(true);
        http.setConcurrentDwn(true);
        http.setConcurrentPool("6");
        http.setEmbeddedUrlRE(".*image.*");

        http.setRunningVersion(true);

        SampleResult result = http.sample();

        String expected =
                "url: http://wiremock/index.html\n" +
                        "response: OK\n" +
                        "data.size: 85\n" +
                        "data: <html><body><img src='image1.png'><img src='image2.png'><img src='image3.png'></body>\n" +
                        "- url: http://wiremock/image1.png\n" +
                        "  response: OK\n" +
                        "  data.size: 8\n" +
                        "  data: content1\n" +
                        "- url: http://wiremock/image2.png\n" +
                        "  response: OK\n" +
                        "  data.size: 8\n" +
                        "  data: content2\n" +
                        "- url: http://wiremock/image3.png\n" +
                        "  response: OK\n" +
                        "  data.size: 8\n" +
                        "  data: content3\n" +
                        "- url: http://wiremock/index.html\n" +
                        "  response: OK\n" +
                        "  data.size: 85\n" +
                        "  data: <html><body><img src='image1.png'><img src='image2.png'><img src='image3.png'></body>\n";
        Assertions.assertEquals(
                expected.replaceAll("\n", System.lineSeparator()),
                ResultAsString.toString(result)
                    .replaceAll(server.baseUrl(), "http://wiremock"));
    }
}
