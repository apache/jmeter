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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.ProxyAuthenticator;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

public class TestProxyHttpSamplers {

    private final String userName = "admin";
    private final String userPass = "admin";

    private HttpProxyServer server;

    @Before
    public void setUp() throws Exception {
        server = DefaultHttpProxyServer.bootstrap().
                withPort(PortFinder.findFreePort()).
                withProxyAuthenticator(new ProxyAuthImpl()).
                start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testProxy() throws InterruptedException {
        InetSocketAddress listenAddress = server.getListenAddress();

        HTTPSamplerForProxyTest sampler = new HTTPSamplerForProxyTest();
        sampler.setPath("http://httpbin.org/get");
        sampler.setMethod("GET");
        sampler.setProperty(HTTPSamplerBase.PROXYHOST, listenAddress.getHostName());
        sampler.setProperty(HTTPSamplerBase.PROXYPORT, listenAddress.getPort());
        sampler.setProperty(HTTPSamplerBase.PROXYUSER, userName);
        sampler.setProperty(HTTPSamplerBase.PROXYPASS, userPass);


        RequestExecutor executor = new RequestExecutor(sampler, 3);
        executor.run(); // run in this thread
        assertEquals(1, sampler.responses.size());

        executor.start(); // run in 2nd thread
        executor.join();
        assertEquals(2, sampler.responses.size());

        for (HttpResponse response : sampler.responses) {
            assertEquals(407, response.getStatusLine().getStatusCode());
            assertEquals("Proxy Authentication Required", response.getStatusLine().getReasonPhrase());
        }

        List<SampleResult> results = executor.results;
        assertEquals(6, results.size());
    }

    static class RequestExecutor extends Thread {
        private HTTPSamplerForProxyTest sampler;
        private int requestCount;

        private List<SampleResult> results = new ArrayList<>();

        public RequestExecutor(HTTPSamplerForProxyTest sampler, int requestCount) {
            this.sampler = sampler;
            this.requestCount = requestCount;
        }

        @Override
        public void run() {
            SampleResult sample;
            for (int i = 0; i < requestCount; i++) {
                sample = sampler.sample();
                assertNotNull(sample);
                assertEquals("200", sample.getResponseCode());
                results.add(sample);
            }
        }
    }

    class HTTPSamplerForProxyTest extends HTTPSamplerBase implements Interruptible {

        List<HttpResponse> responses = new ArrayList<>();

        private final transient HTTPHC4Impl hc;

        public HTTPSamplerForProxyTest() {
            hc = new HTTPHC4Impl(this) {
                @Override
                protected AuthenticationStrategy getProxyAuthStrategy() {
                    return new ProxyAuthenticationStrategy() {
                        @Override
                        public Map<String, Header> getChallenges(HttpHost authhost, HttpResponse response, HttpContext context)
                                throws MalformedChallengeException {
                            responses.add(response);
                            return super.getChallenges(authhost, response, context);
                        }
                    };
                }
            };
        }


        @Override
        public boolean interrupt() {
            return hc.interrupt();
        }

        @Override
        protected HTTPSampleResult sample(java.net.URL u, String method,
                                          boolean areFollowingRedirect, int depth) {
            return hc.sample(u, method, areFollowingRedirect, depth);
        }

        /* (non-Javadoc)
         * @see org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase#testIterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
         */
        @Override
        public void testIterationStart(LoopIterationEvent event) {
            hc.notifyFirstSampleAfterLoopRestart();
        }
    }

    class ProxyAuthImpl implements ProxyAuthenticator {

        @Override
        public boolean authenticate(String s, String s1) {
            return userName.equals(s) && userPass.equals(s1);
        }

        @Override
        public String getRealm() {
            return null;
        }
    }

    public static class PortFinder {

        // the ports below 1024 are system ports
        private static final int MIN_PORT_NUMBER = 1024;

        // the ports above 49151 are dynamic and/or private
        private static final int MAX_PORT_NUMBER = 49151;

        /**
         * Finds a free port between
         * {@link #MIN_PORT_NUMBER} and {@link #MAX_PORT_NUMBER}.
         *
         * @return a free port
         * @throw RuntimeException if a port could not be found
         */
        public static int findFreePort() {
            for (int i = MIN_PORT_NUMBER; i <= MAX_PORT_NUMBER; i++) {
                if (available(i)) {
                    return i;
                }
            }
            throw new RuntimeException("Could not find an available port between " +
                    MIN_PORT_NUMBER + " and " + MAX_PORT_NUMBER);
        }

        /**
         * Returns true if the specified port is available on this host.
         *
         * @param port the port to check
         * @return true if the port is available, false otherwise
         */
        private static boolean available(final int port) {
            ServerSocket serverSocket = null;
            DatagramSocket dataSocket = null;
            try {
                serverSocket = new ServerSocket(port);
                serverSocket.setReuseAddress(true);
                dataSocket = new DatagramSocket(port);
                dataSocket.setReuseAddress(true);
                return true;
            } catch (final IOException e) {
                return false;
            } finally {
                if (dataSocket != null) {
                    dataSocket.close();
                }
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (final IOException e) {
                        // can never happen
                    }
                }
            }
        }
    }

}
