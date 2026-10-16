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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jorphan.util.JOrphanUtils;

/**
 * Relays TCP traffic to a backend server and counts the connections the client opens, which is how a
 * test tells apart requests multiplexed over one connection from requests that each got their own.
 *
 * <p>It can also drop a single relayed connection as soon as the client writes to it again, which is
 * what an idle connection closed by the server (or by a load balancer) looks like to a client that has
 * not noticed the close yet. New connections are relayed as usual, so the backend stays reachable.
 */
final class TcpRelay implements Closeable {

    private final ServerSocket serverSocket;
    private final int backendPort;
    private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "relay");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicInteger acceptedConnections = new AtomicInteger();
    private volatile RelayedConnection currentConnection;

    TcpRelay(int backendPort) throws IOException {
        this.backendPort = backendPort;
        this.serverSocket = new ServerSocket(0, 0, InetAddress.getLoopbackAddress());
        executor.execute(this::acceptConnections);
    }

    int getPort() {
        return serverSocket.getLocalPort();
    }

    int getAcceptedConnections() {
        return acceptedConnections.get();
    }

    void dropConnectionOnNextRequest() {
        RelayedConnection connection = currentConnection;
        if (connection == null) {
            throw new IllegalStateException("No connection has been relayed yet");
        }
        connection.doomed = true;
    }

    private void acceptConnections() {
        while (!serverSocket.isClosed()) {
            RelayedConnection connection;
            try {
                Socket client = serverSocket.accept();
                connection = new RelayedConnection(client,
                        new Socket(InetAddress.getLoopbackAddress(), backendPort));
            } catch (IOException e) {
                return;
            }
            acceptedConnections.incrementAndGet();
            currentConnection = connection;
            executor.execute(connection::relayRequests);
            executor.execute(connection::relayResponses);
        }
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
        executor.shutdownNow();
    }

    private static final class RelayedConnection {

        private final Socket client;
        private final Socket backend;
        private volatile boolean doomed;

        RelayedConnection(Socket client, Socket backend) {
            this.client = client;
            this.backend = backend;
        }

        void relayRequests() {
            try {
                InputStream input = client.getInputStream();
                OutputStream output = backend.getOutputStream();
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1 && !doomed) {
                    output.write(buffer, 0, read);
                    output.flush();
                }
            } catch (IOException ignored) { // NOSONAR the connection is closed below in any case
                // the peer went away, which ends the relaying just as well
            }
            close();
        }

        void relayResponses() {
            try {
                InputStream input = backend.getInputStream();
                OutputStream output = client.getOutputStream();
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    output.flush();
                }
            } catch (IOException ignored) { // NOSONAR the connection is closed below in any case
                // the peer went away, which ends the relaying just as well
            }
            close();
        }

        private void close() {
            JOrphanUtils.closeQuietly(client);
            JOrphanUtils.closeQuietly(backend);
        }
    }
}
