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

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.entity.AsyncEntityProducerWrapper;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Timeout;

/**
 * Response timeout of a request that {@link HTTPHC5Impl} executes with the asynchronous HttpClient
 * transport, which it uses for HTTP/2.
 * <p>
 * HttpClient applies the response timeout of the request config as socket timeout of the
 * connection, which it skips once HTTP/2 was negotiated, as all streams share the connection.
 * Without this, a server that accepts the stream but never answers, or stalls in the middle of the
 * response, would block the sampler thread until the test is stopped. Like the socket timeout of the
 * HTTP/1.1 transport, the timeout applies to each wait for the server, so a response that keeps
 * arriving in chunks may take longer than the timeout in total.
 * <p>
 * The timeout starts when a request is handed to the connection, and every part of the request
 * body sent and of the response received restarts it. This covers every exchange HttpClient
 * executes for a sample, including those of redirects and authentication challenges. In between,
 * while an exchange waits for its connection to be leased or connected, the timeout is suspended,
 * as it does not cover that with the HTTP/1.1 transport either. HttpClient hands over the response
 * an HTTP/2 frame at a time, so a single frame which takes longer than the timeout to arrive counts
 * as inactivity, which only matters for an extremely slow link given the default maximum frame size
 * of 16 kB.
 */
final class AsyncResponseTimeout {

    private static final String CONTEXT_ATTRIBUTE = "__jmeter.RESPONSE_TIMEOUT__"; //$NON-NLS-1$

    /**
     * Starts the timeout of an exchange. HttpClient runs the request interceptors right before it
     * sends the request on the connection, so that leasing and connecting is not counted.
     */
    private static final HttpRequestInterceptor START_ON_REQUEST = (request, entity, context) -> {
        AsyncResponseTimeout responseTimeout = (AsyncResponseTimeout) context.getAttribute(CONTEXT_ATTRIBUTE);
        if (responseTimeout != null) {
            responseTimeout.exchangeStarted();
        }
    };

    /**
     * Records the activity of an exchange. As the last element of the execution chain it sees the
     * request body and the response of every exchange as they pass the connection.
     */
    private static final AsyncExecChainHandler TRACK_ACTIVITY = (request, entityProducer, scope, chain, callback) -> {
        AsyncResponseTimeout responseTimeout =
                (AsyncResponseTimeout) scope.clientContext.getAttribute(CONTEXT_ATTRIBUTE);
        if (responseTimeout == null) {
            chain.proceed(request, entityProducer, scope, callback);
            return;
        }
        chain.proceed(request,
                entityProducer == null ? null : new ActivityTrackingEntityProducer(entityProducer, responseTimeout),
                scope, new ActivityTrackingCallback(callback, responseTimeout));
    };

    private static final AsyncResponseTimeout DISABLED = new AsyncResponseTimeout(null);

    private final Timeout timeout;
    private volatile long lastActivityNanos;
    private volatile boolean exchangeInProgress;

    private AsyncResponseTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    /**
     * Installs the interceptors that track the activity of the requests executed by the client.
     *
     * @param builder builder of the client
     */
    static void install(HttpAsyncClientBuilder builder) {
        builder.addRequestInterceptorLast(START_ON_REQUEST)
                .addExecInterceptorLast("jmeter-response-timeout", TRACK_ACTIVITY);
    }

    /**
     * Sets up the response timeout of the request config for the request executed with the given
     * context.
     *
     * @param requestConfig config of the request, may be {@code null}
     * @param context       context the request is executed with
     * @return the response timeout to wait for the response with
     */
    static AsyncResponseTimeout attach(RequestConfig requestConfig, HttpContext context) {
        Timeout timeout = requestConfig == null ? null : requestConfig.getResponseTimeout();
        if (timeout == null || timeout.isDisabled()) {
            return DISABLED;
        }
        AsyncResponseTimeout responseTimeout = new AsyncResponseTimeout(timeout);
        context.setAttribute(CONTEXT_ATTRIBUTE, responseTimeout);
        return responseTimeout;
    }

    /**
     * Waits for the response, failing the exchange once no data was sent or received for longer
     * than the timeout.
     *
     * @param responseFuture future of the response
     * @param <T>            type of the response
     * @return the response
     * @throws SocketTimeoutException when the timeout expired, the exchange is cancelled then
     * @throws InterruptedException   when the thread was interrupted while waiting
     * @throws ExecutionException     when the exchange failed
     */
    <T> T await(Future<T> responseFuture)
            throws InterruptedException, ExecutionException, SocketTimeoutException {
        if (timeout == null) {
            return responseFuture.get();
        }
        long timeoutNanos = timeout.toNanoseconds();
        while (true) {
            long remainingNanos = remainingNanos(timeoutNanos);
            if (remainingNanos <= 0) {
                // HttpClient discards the connection of a cancelled exchange, which also fails the
                // requests multiplexed over it, just like it does when a sample is interrupted. It
                // discards the connection of any failed exchange and offers no way to reset only
                // the stream, see the HTTP Version of the HTTP Request in component_reference.xml
                if (responseFuture.cancel(true)) {
                    throw new SocketTimeoutException("Response timeout of " + timeout.toMilliseconds()
                            + " ms exceeded while waiting for the server");
                }
                // The exchange completed in the meantime
                return responseFuture.get();
            }
            try {
                return responseFuture.get(remainingNanos, TimeUnit.NANOSECONDS);
            } catch (TimeoutException e) {
                // Check again, as data that arrived while waiting restarts the timeout
            }
        }
    }

    /**
     * Nanoseconds until the exchange in progress has been idle for longer than the timeout, the
     * full timeout if no exchange is in progress.
     */
    private long remainingNanos(long timeoutNanos) {
        if (!exchangeInProgress) {
            return timeoutNanos;
        }
        return timeoutNanos - (System.nanoTime() - lastActivityNanos);
    }

    private void exchangeStarted() {
        lastActivityNanos = System.nanoTime();
        exchangeInProgress = true;
    }

    private void touch() {
        lastActivityNanos = System.nanoTime();
    }

    private void exchangeFinished() {
        exchangeInProgress = false;
    }

    /**
     * Counts every part of the request body handed to the connection as activity, so that an upload
     * which takes longer than the timeout is not cut short, while a server that stops accepting it
     * is still timed out.
     */
    private static final class ActivityTrackingEntityProducer extends AsyncEntityProducerWrapper {
        private final AsyncResponseTimeout responseTimeout;

        private ActivityTrackingEntityProducer(AsyncEntityProducer entityProducer,
                AsyncResponseTimeout responseTimeout) {
            super(entityProducer);
            this.responseTimeout = responseTimeout;
        }

        @Override
        public void produce(DataStreamChannel channel) throws IOException {
            super.produce(new DataStreamChannel() {
                @Override
                public void requestOutput() {
                    channel.requestOutput();
                }

                @Override
                public int write(ByteBuffer src) throws IOException {
                    int written = channel.write(src);
                    if (written > 0) {
                        responseTimeout.touch();
                    }
                    return written;
                }

                @Override
                public void endStream(List<? extends Header> trailers) throws IOException {
                    channel.endStream(trailers);
                    responseTimeout.touch();
                }

                @Override
                public void endStream() throws IOException {
                    channel.endStream();
                    responseTimeout.touch();
                }
            });
        }
    }

    /** Counts the response head and every part of the response body as activity. */
    private static final class ActivityTrackingCallback implements AsyncExecCallback {
        private final AsyncExecCallback delegate;
        private final AsyncResponseTimeout responseTimeout;

        private ActivityTrackingCallback(AsyncExecCallback delegate, AsyncResponseTimeout responseTimeout) {
            this.delegate = delegate;
            this.responseTimeout = responseTimeout;
        }

        @Override
        public AsyncDataConsumer handleResponse(HttpResponse response, EntityDetails entityDetails)
                throws HttpException, IOException {
            responseTimeout.touch();
            AsyncDataConsumer dataConsumer = delegate.handleResponse(response, entityDetails);
            return dataConsumer == null ? null : new ActivityTrackingDataConsumer(dataConsumer, responseTimeout);
        }

        @Override
        public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
            responseTimeout.touch();
            delegate.handleInformationResponse(response);
        }

        // The exchange is marked finished before the delegate is notified, as a redirect or an
        // authentication challenge starts the next exchange from in there

        @Override
        public void completed() {
            responseTimeout.exchangeFinished();
            delegate.completed();
        }

        @Override
        public void failed(Exception cause) {
            responseTimeout.exchangeFinished();
            delegate.failed(cause);
        }
    }

    private static final class ActivityTrackingDataConsumer implements AsyncDataConsumer {
        private final AsyncDataConsumer delegate;
        private final AsyncResponseTimeout responseTimeout;

        private ActivityTrackingDataConsumer(AsyncDataConsumer delegate, AsyncResponseTimeout responseTimeout) {
            this.delegate = delegate;
            this.responseTimeout = responseTimeout;
        }

        @Override
        public void updateCapacity(CapacityChannel capacityChannel) throws IOException {
            delegate.updateCapacity(capacityChannel);
        }

        @Override
        public void consume(ByteBuffer src) throws IOException {
            responseTimeout.touch();
            delegate.consume(src);
        }

        @Override
        public void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
            responseTimeout.touch();
            delegate.streamEnd(trailers);
        }

        @Override
        public void releaseResources() {
            delegate.releaseResources();
        }
    }
}
