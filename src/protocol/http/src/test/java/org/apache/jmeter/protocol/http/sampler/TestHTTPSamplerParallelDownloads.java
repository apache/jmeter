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
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.parser.LagartoBasedHtmlParser;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * The threads which download embedded resources in parallel are pooled and shared by all JMeter
 * threads, so a client which is bound to the thread that happens to create such a pooled thread ends
 * up being used, and closed, across JMeter threads.
 *
 * <p>These tests warm the pool up from one JMeter thread, which then keeps closing its clients while
 * the other JMeter threads sample, as a thread group does when its threads finish or start a new
 * iteration, and they check that no sample fails because of a client another JMeter thread closed.
 */
class TestHTTPSamplerParallelDownloads {

    private static final int EMBEDDED_RESOURCES = 12;
    private static final int PARALLEL_DOWNLOADS = 6;
    private static final int SAMPLING_THREADS = 2;
    private static final int ITERATIONS = 12;

    private static WireMockServer server;

    @BeforeAll
    static void startServer() {
        HTTPSamplerBase.registerParser("text/html", LagartoBasedHtmlParser.class.getName());
        server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicPort()
                .dynamicHttpsPort()
                .http2TlsDisabled(false));
        server.start();
        StringBuilder html = new StringBuilder("<html><body>");
        for (int i = 0; i < EMBEDDED_RESOURCES; i++) {
            html.append("<img src='image").append(i).append(".png'>");
            server.stubFor(get(urlEqualTo("/image" + i + ".png"))
                    .willReturn(aResponse().withStatus(200)
                            .withFixedDelay(100)
                            .withHeader("Cache-Control", "max-age=600")
                            .withHeader("ETag", "\"etag" + i + "\"")
                            .withBody("image" + i)));
        }
        server.stubFor(get(urlEqualTo("/index.html"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withHeader("Set-Cookie", "session=abc; Path=/")
                        .withBody(html.append("</body></html>").toString())));
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @ParameterizedTest(name = "{0} {1} over {2}")
    @CsvSource({
        "HttpClient4, HTTP/1.1, http",
        "HttpClient5, HTTP/1.1, http",
        "HttpClient5, HTTP/2,   https",
        "Java,        HTTP/1.1, http",
    })
    @Timeout(120)
    void downloadsEmbeddedResourcesWhileAnotherThreadClosesItsClients(
            String implementation, String version, String scheme) throws Exception {
        int port = "https".equals(scheme) ? server.httpsPort() : server.port();
        URL url = new URL(scheme + "://localhost:" + port + "/index.html");
        List<String> failures = new CopyOnWriteArrayList<>();
        CountDownLatch poolWarmedUp = new CountDownLatch(1);
        AtomicBoolean samplingDone = new AtomicBoolean();

        // creates the pooled downloader threads and then keeps closing the clients they may hold on to
        Thread closingThread = new Thread(() -> {
            prepareThread();
            HTTPSamplerBase sampler = sampleOnce(url, implementation, version, failures);
            poolWarmedUp.countDown();
            do {
                sampler.threadFinished();
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            } while (!samplingDone.get());
        }, "closing-jmeter-thread");
        closingThread.start();

        List<Thread> samplingThreads = new ArrayList<>();
        for (int i = 0; i < SAMPLING_THREADS; i++) {
            Thread thread = new Thread(() -> {
                prepareThread();
                try {
                    poolWarmedUp.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                HTTPSamplerBase sampler = null;
                for (int iteration = 0; iteration < ITERATIONS; iteration++) {
                    sampler = sampleOnce(url, implementation, version, failures);
                }
                sampler.threadFinished();
            }, "sampling-jmeter-thread-" + i);
            samplingThreads.add(thread);
            thread.start();
        }
        for (Thread thread : samplingThreads) {
            thread.join();
        }
        samplingDone.set(true);
        closingThread.join();

        assertEquals(List.of(), failures.stream().distinct().toList(),
                "samples must not fail because another JMeter thread closed its clients");
    }

    /** Gives the thread the {@code JMeterContext} a JMeter thread has. */
    private static void prepareThread() {
        JMeterContextService.getContext().setVariables(new JMeterVariables());
    }

    private static HTTPSamplerBase sampleOnce(URL url, String implementation, String version,
            List<String> failures) {
        CacheManager cacheManager = new CacheManager();
        cacheManager.setProperty(TestElement.GUI_CLASS, "CacheManagerGui");
        cacheManager.testStarted();
        CookieManager cookieManager = new CookieManager();
        cookieManager.setProperty(TestElement.GUI_CLASS, "CookiePanel");
        cookieManager.testStarted();
        HeaderManager headerManager = new HeaderManager();
        headerManager.add(new Header("Accept-Language", "en"));
        HTTPSamplerBase sampler = HTTPSamplerFactory.newInstance(implementation);
        sampler.setHttpVersion(version);
        sampler.setImageParser(true);
        sampler.setConcurrentDwn(true);
        sampler.setConcurrentPool(Integer.toString(PARALLEL_DOWNLOADS));
        sampler.setRunningVersion(true);
        sampler.setCacheManager(cacheManager);
        sampler.setCookieManager(cookieManager);
        sampler.setHeaderManager(headerManager);
        try {
            collectFailures(sampler.sample(url, HTTPConstants.GET, false, 0), failures);
        } catch (RuntimeException e) { // NOSONAR the failure is reported by the test
            failures.add("exception " + e);
        }
        sampler.testIterationStart(new LoopIterationEvent(sampler, 2));
        return sampler;
    }

    private static void collectFailures(HTTPSampleResult result, List<String> failures) {
        if (!"200".equals(result.getResponseCode())) {
            failures.add(result.getResponseCode() + " " + result.getResponseMessage());
        }
        for (SampleResult subResult : result.getSubResults()) {
            if (!"200".equals(subResult.getResponseCode())) {
                failures.add(subResult.getResponseCode() + " " + subResult.getResponseMessage());
            }
        }
    }
}
