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

package org.apache.jmeter.protocol.http.control;

import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.jmeter.gui.Stoppable;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * Server daemon thread.
 * Creates main socket and listens on it.
 * For each client request, creates a thread to handle the request.
 *
 */
public class HttpMirrorServer extends Thread implements Stoppable {
    private static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * The time (in milliseconds) to wait when accepting a client connection.
     * The accept will be retried until the Daemon is told to stop. So this
     * interval is the longest time that the Daemon will have to wait after
     * being told to stop.
     */
    private static final int ACCEPT_TIMEOUT = 1000;

    /** The port to listen on. */
    private final int daemonPort;

    /** True if the Daemon is currently running. */
    private volatile boolean running;

    // Saves the error if one occurs
    private volatile Exception except;

    /**
     * Create a new Daemon with the specified port and target.
     *
     * @param port
     *            the port to listen on.
     */
    public HttpMirrorServer(int port) {
        super("HttpMirrorServer");
        this.daemonPort = port;
    }

    /**
     * Listen on the daemon port and handle incoming requests. This method will
     * not exit until {@link #stopServer()} is called or an error occurs.
     */
    @Override
    public void run() {
        except = null;
        running = true;
        ServerSocket mainSocket = null;

        try {
            log.info("Creating HttpMirror ... on port " + daemonPort);
            mainSocket = new ServerSocket(daemonPort);
            mainSocket.setSoTimeout(ACCEPT_TIMEOUT);
            log.info("HttpMirror up and running!");

            while (running) {
                try {
                    // Listen on main socket
                    Socket clientSocket = mainSocket.accept();
                    if (running) {
                        // Pass request to new thread
                        HttpMirrorThread thd = new HttpMirrorThread(clientSocket);
                        log.debug("Starting new Mirror thread");
                        thd.start();
                    } else {
                        log.warn("Server not running");
                        JOrphanUtils.closeQuietly(clientSocket);
                    }
                } catch (InterruptedIOException e) {
                    // Timeout occurred. Ignore, and keep looping until we're
                    // told to stop running.
                }
            }
            log.info("HttpMirror Server stopped");
        } catch (Exception e) {
            except = e;
            log.warn("HttpMirror Server stopped", e);
        } finally {
            JOrphanUtils.closeQuietly(mainSocket);
        }
    }

    public void stopServer() {
        running = false;
    }

    public Exception getException(){
        return except;
    }

    public static void main(String args[]){
        int port = 8080;
        if (args.length > 0){
            port = Integer.parseInt(args[0]);
        }
        LoggingManager.setPriority("INFO"); // default level
        LoggingManager.setLoggingLevels(System.getProperties() ); // allow override by system properties
        HttpMirrorServer serv = new HttpMirrorServer(port);
        serv.start();
    }
}
