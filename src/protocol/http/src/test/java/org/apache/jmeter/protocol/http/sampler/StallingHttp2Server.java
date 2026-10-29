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

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.impl.BasicEntityDetails;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncServer;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.impl.nio.bootstrap.H2ServerBootstrap;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.ListenerEndpoint;

/**
 * Cleartext HTTP/2 server (h2c with prior knowledge) that accepts every stream but never completes
 * the response. Depending on how it is set up it either does not answer at all, or it sends the
 * response headers and then stalls without sending a single byte of the body. That is what a hung
 * backend looks like on a connection, which is still alive and keeps answering HTTP/2 pings.
 */
final class StallingHttp2Server implements Closeable {

    private final HttpAsyncServer server;
    private final int port;

    StallingHttp2Server(boolean sendResponseHeaders) throws Exception {
        server = H2ServerBootstrap.bootstrap()
                .setCanonicalHostName("localhost")
                .setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_2)
                .register("*", () -> new StallingExchangeHandler(sendResponseHeaders))
                .create();
        server.start();
        ListenerEndpoint endpoint = server.listen(
                new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), URIScheme.HTTP).get(10, TimeUnit.SECONDS);
        port = ((InetSocketAddress) endpoint.getAddress()).getPort();
    }

    int getPort() {
        return port;
    }

    @Override
    public void close() {
        server.close(CloseMode.IMMEDIATE);
    }

    private static final class StallingExchangeHandler implements AsyncServerExchangeHandler {

        private final boolean sendResponseHeaders;

        private StallingExchangeHandler(boolean sendResponseHeaders) {
            this.sendResponseHeaders = sendResponseHeaders;
        }

        @Override
        public void handleRequest(HttpRequest request, EntityDetails entityDetails, ResponseChannel responseChannel,
                HttpContext context) throws HttpException, IOException {
            if (sendResponseHeaders) {
                // a body of unknown length is announced, so the client keeps waiting for it
                responseChannel.sendResponse(new BasicHttpResponse(HttpStatus.SC_OK),
                        new BasicEntityDetails(-1, ContentType.TEXT_PLAIN), context);
            }
        }

        @Override
        public void updateCapacity(CapacityChannel capacityChannel) throws IOException {
            capacityChannel.update(Integer.MAX_VALUE);
        }

        @Override
        public void consume(ByteBuffer src) {
            src.position(src.limit());
        }

        @Override
        public void streamEnd(List<? extends Header> trailers) {
            // the request is complete, the response is deliberately never finished
        }

        @Override
        public int available() {
            return 0;
        }

        @Override
        public void produce(DataStreamChannel channel) {
            // never sends any of the announced body
        }

        @Override
        public void failed(Exception cause) {
            // the client giving up on the stream is expected
        }

        @Override
        public void releaseResources() {
            // nothing to release
        }
    }
}
