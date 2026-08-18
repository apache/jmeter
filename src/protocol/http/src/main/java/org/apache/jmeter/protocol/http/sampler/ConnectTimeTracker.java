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

import java.net.URL;
import java.net.http.HttpClient;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.jmeter.samplers.SampleResult;

/**
 * Measures how long it takes to establish the connections of a JDK {@link HttpClient} and reports the
 * result in the {@link SampleResult}s which are waiting for them.
 * <p>
 * The JDK client does not expose a hook for connection establishment, so the wrapped {@code SSLContext}
 * is used instead: it creates one {@code SSLEngine} per connection, and that engine reports when the TLS
 * handshake of its connection has been finished. Only the samples which are waiting for a connection to
 * the origin of that very connection are attached to it, so a sample of another origin is never affected,
 * and a sample which is served by an already established connection keeps a connect time of {@code 0}.
 * Plain text connections carry no {@code SSLEngine}, so their connect time cannot be measured at all.
 * <p>
 * A client is shared by a JMeter thread and the threads which download its embedded resources, and by all
 * JMeter threads when {@code http.java.h2.share_connections_between_threads} is enabled, so the measurement
 * happens on the client threads, but the value is only written to the {@link SampleResult} by the thread
 * which owns the sample.
 */
final class ConnectTimeTracker {

    /** Samples which are currently in flight, together with the connection they are waiting for. */
    private final Map<SampleResult, PendingSample> activeSamples = new ConcurrentHashMap<>();

    /**
     * Registers a sample which is about to be sent.
     *
     * @param result the sample result, it must have been started
     * @param origin the origin the request is sent to, see {@link #origin(URL)}
     */
    void sampleStarted(SampleResult result, String origin) {
        activeSamples.put(result, new PendingSample(result, origin));
    }

    /**
     * Unregisters the sample and stores the connect time of the connection it had to wait for, if any.
     * This is called by the thread which owns the sample, so the {@link SampleResult} is never written
     * by a client thread.
     */
    void sampleFinished(SampleResult result) {
        PendingSample pending = activeSamples.remove(result);
        if (pending != null) {
            pending.storeConnectTime();
        }
    }

    /**
     * Announces that a new connection to {@code origin} is being opened, and attaches the samples which
     * are currently waiting for a connection to that origin to it.
     *
     * @return the connection, which has to be told when it has been established
     */
    Connection connectionOpened(String origin) {
        List<PendingSample> waiting = new ArrayList<>();
        for (PendingSample pending : activeSamples.values()) {
            if (pending.waitsFor(origin) && pending.attach()) {
                waiting.add(pending);
            }
        }
        return new Connection(waiting);
    }

    /** Returns the origin a sample of the given URL connects to. */
    static String origin(URL url) {
        int port = url.getPort() != -1 ? url.getPort() : url.getDefaultPort();
        return origin(url.getHost(), port);
    }

    /** Returns the origin a connection to the given host and port belongs to. */
    static String origin(String host, int port) {
        return (host == null ? "" : host.toLowerCase(Locale.ROOT)) + ":" + port; // $NON-NLS-1$ $NON-NLS-2$
    }

    /**
     * A single connection of an HTTP/2 {@link HttpClient}, together with the samples which are waiting for it
     * to be established.
     */
    static final class Connection {
        /** Set to {@code null} once the connection has been established, which also releases the samples. */
        private final AtomicReference<List<PendingSample>> waitingSamples;

        Connection(List<PendingSample> waitingSamples) {
            this.waitingSamples = new AtomicReference<>(waitingSamples);
        }

        /** Invoked when the connection is ready to carry requests, that is when its handshake is done. */
        void established() {
            List<PendingSample> samples = waitingSamples.getAndSet(null);
            if (samples != null) {
                for (PendingSample sample : samples) {
                    sample.connectEnd();
                }
            }
        }
    }

    /** A sample which is in flight, and the connect time measured for it, if it had to open a connection. */
    private static final class PendingSample {
        private static final long NOT_MEASURED = -1;

        private final SampleResult result;
        private final String origin;
        private final AtomicBoolean attached = new AtomicBoolean();
        private volatile long connectTime = NOT_MEASURED;

        PendingSample(SampleResult result, String origin) {
            this.result = result;
            this.origin = origin;
        }

        boolean waitsFor(String otherOrigin) {
            return origin.equals(otherOrigin);
        }

        /** Attaches the sample to a connection, a sample can only wait for a single connection. */
        boolean attach() {
            return attached.compareAndSet(false, true);
        }

        /** Measures the connect time, called on a client thread, so the sample itself is not modified. */
        void connectEnd() {
            connectTime = Math.max(0,
                    result.currentTimeInMillis() - result.getStartTime() - result.getIdleTime());
        }

        /** Stores the measured connect time, called on the thread which owns the sample. */
        void storeConnectTime() {
            long measured = connectTime;
            if (measured != NOT_MEASURED) {
                result.setConnectTime(measured);
            }
        }
    }

    /**
     * {@link SSLContext} which creates {@link SSLEngine}s that report the end of the TLS handshake.
     */
    static final class MeasuringSSLContext extends SSLContext {
        MeasuringSSLContext(SSLContext delegate, ConnectTimeTracker tracker) {
            super(new MeasuringSSLContextSpi(delegate, tracker), delegate.getProvider(),
                    delegate.getProtocol());
        }
    }

    private static final class MeasuringSSLContextSpi extends SSLContextSpi {
        private final SSLContext delegate;
        private final ConnectTimeTracker tracker;

        MeasuringSSLContextSpi(SSLContext delegate, ConnectTimeTracker tracker) {
            this.delegate = delegate;
            this.tracker = tracker;
        }

        @Override
        protected void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom sr) throws KeyManagementException {
            delegate.init(km, tm, sr);
        }

        @Override
        protected SSLSocketFactory engineGetSocketFactory() {
            return delegate.getSocketFactory();
        }

        @Override
        protected SSLServerSocketFactory engineGetServerSocketFactory() {
            return delegate.getServerSocketFactory();
        }

        @Override
        protected SSLEngine engineCreateSSLEngine() {
            // Without a peer the connection cannot be attributed to the samples of an origin, so it is not measured
            return delegate.createSSLEngine();
        }

        @Override
        protected SSLEngine engineCreateSSLEngine(String host, int port) {
            return new MeasuringSSLEngine(delegate.createSSLEngine(host, port),
                    tracker.connectionOpened(ConnectTimeTracker.origin(host, port)));
        }

        @Override
        protected SSLSessionContext engineGetServerSessionContext() {
            return delegate.getServerSessionContext();
        }

        @Override
        protected SSLSessionContext engineGetClientSessionContext() {
            return delegate.getClientSessionContext();
        }

        @Override
        protected SSLParameters engineGetDefaultSSLParameters() {
            return delegate.getDefaultSSLParameters();
        }

        @Override
        protected SSLParameters engineGetSupportedSSLParameters() {
            return delegate.getSupportedSSLParameters();
        }
    }

    /**
     * {@link SSLEngine} which delegates all calls and reports the {@link Connection} it belongs to as
     * established as soon as its TLS handshake has been finished.
     */
    static final class MeasuringSSLEngine extends SSLEngine {
        private final SSLEngine delegate;
        private final Connection connection;

        MeasuringSSLEngine(SSLEngine delegate, Connection connection) {
            super(delegate.getPeerHost(), delegate.getPeerPort());
            this.delegate = delegate;
            this.connection = connection;
        }

        private void checkHandshakeFinished(SSLEngineResult result) {
            if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
                connection.established();
            }
        }

        @Override
        public SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int length, ByteBuffer dst) throws SSLException {
            SSLEngineResult result = delegate.wrap(srcs, offset, length, dst);
            checkHandshakeFinished(result);
            return result;
        }

        @Override
        public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts, int offset, int length) throws SSLException {
            SSLEngineResult result = delegate.unwrap(src, dsts, offset, length);
            checkHandshakeFinished(result);
            return result;
        }

        @Override
        public Runnable getDelegatedTask() {
            return delegate.getDelegatedTask();
        }

        @Override
        public void closeInbound() throws SSLException {
            delegate.closeInbound();
        }

        @Override
        public boolean isInboundDone() {
            return delegate.isInboundDone();
        }

        @Override
        public void closeOutbound() {
            delegate.closeOutbound();
        }

        @Override
        public boolean isOutboundDone() {
            return delegate.isOutboundDone();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public String[] getEnabledCipherSuites() {
            return delegate.getEnabledCipherSuites();
        }

        @Override
        public void setEnabledCipherSuites(String[] suites) {
            delegate.setEnabledCipherSuites(suites);
        }

        @Override
        public String[] getSupportedProtocols() {
            return delegate.getSupportedProtocols();
        }

        @Override
        public String[] getEnabledProtocols() {
            return delegate.getEnabledProtocols();
        }

        @Override
        public void setEnabledProtocols(String[] protocols) {
            delegate.setEnabledProtocols(protocols);
        }

        @Override
        public SSLSession getSession() {
            return delegate.getSession();
        }

        @Override
        public SSLSession getHandshakeSession() {
            return delegate.getHandshakeSession();
        }

        @Override
        public void beginHandshake() throws SSLException {
            delegate.beginHandshake();
        }

        @Override
        public SSLEngineResult.HandshakeStatus getHandshakeStatus() {
            return delegate.getHandshakeStatus();
        }

        @Override
        public void setUseClientMode(boolean mode) {
            delegate.setUseClientMode(mode);
        }

        @Override
        public boolean getUseClientMode() {
            return delegate.getUseClientMode();
        }

        @Override
        public void setNeedClientAuth(boolean need) {
            delegate.setNeedClientAuth(need);
        }

        @Override
        public boolean getNeedClientAuth() {
            return delegate.getNeedClientAuth();
        }

        @Override
        public void setWantClientAuth(boolean want) {
            delegate.setWantClientAuth(want);
        }

        @Override
        public boolean getWantClientAuth() {
            return delegate.getWantClientAuth();
        }

        @Override
        public void setEnableSessionCreation(boolean flag) {
            delegate.setEnableSessionCreation(flag);
        }

        @Override
        public boolean getEnableSessionCreation() {
            return delegate.getEnableSessionCreation();
        }

        @Override
        public SSLParameters getSSLParameters() {
            return delegate.getSSLParameters();
        }

        @Override
        public void setSSLParameters(SSLParameters params) {
            delegate.setSSLParameters(params);
        }

        @Override
        public String getApplicationProtocol() {
            return delegate.getApplicationProtocol();
        }

        @Override
        public String getHandshakeApplicationProtocol() {
            return delegate.getHandshakeApplicationProtocol();
        }

        @Override
        public void setHandshakeApplicationProtocolSelector(BiFunction<SSLEngine, List<String>, String> selector) {
            if (selector == null) {
                delegate.setHandshakeApplicationProtocolSelector(null);
            } else {
                delegate.setHandshakeApplicationProtocolSelector((engine, protocols) -> selector.apply(this, protocols));
            }
        }

        @Override
        public BiFunction<SSLEngine, List<String>, String> getHandshakeApplicationProtocolSelector() {
            return delegate.getHandshakeApplicationProtocolSelector();
        }
    }
}
