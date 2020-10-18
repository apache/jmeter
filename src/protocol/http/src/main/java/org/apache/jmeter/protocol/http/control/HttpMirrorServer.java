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

import java.io.InterruptedIOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.avalon.CLArgsParser;
import org.apache.commons.cli.avalon.CLOption;
import org.apache.commons.cli.avalon.CLOptionDescriptor;
import org.apache.commons.cli.avalon.CLUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.Stoppable;
import org.apache.jmeter.testelement.NonTestElement;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server daemon thread.
 * Creates main socket and listens on it.
 * For each client request, creates a thread to handle the request.
 */
public class HttpMirrorServer extends Thread implements Stoppable, NonTestElement {

    private static final int HELP_OPT_ID = '?';// $NON-NLS-1$
    private static final int PORT_OPT_ID = 'P';// $NON-NLS-1$
    private static final int LOGLEVEL_OPT_ID = 'L';// $NON-NLS-1$

    /* Define the understood command line flags. */
    private static final CLOptionDescriptor HELP_OPT =
            new CLOptionDescriptor("?",
                    CLOptionDescriptor.ARGUMENT_DISALLOWED,
                    HELP_OPT_ID,
                    "print command line options and exit");
    private static final CLOptionDescriptor PORT_OPT =
            new CLOptionDescriptor("port",
                    CLOptionDescriptor.ARGUMENT_REQUIRED,
                    PORT_OPT_ID,
                    "Set server port for HttpMirrorServer to use");
    private static final CLOptionDescriptor LOGLEVEL_OPT =
            new CLOptionDescriptor("loglevel",
                    CLOptionDescriptor.DUPLICATES_ALLOWED | CLOptionDescriptor.ARGUMENTS_REQUIRED_2,
                    LOGLEVEL_OPT_ID,
                    "[category=]level e.g. INFO or DEBUG");

    private static final CLOptionDescriptor[] options = new CLOptionDescriptor[]{
            HELP_OPT,
            PORT_OPT,
            LOGLEVEL_OPT,
    };

    /**
     * The time (in milliseconds) to wait when accepting a client connection.
     * The accept will be retried until the Daemon is told to stop. So this
     * is the longest time that the Daemon will wait after being told to stop.
     */
    private static final int ACCEPT_TIMEOUT = 1000;

    private static final long KEEP_ALIVE_TIME = 10;

    /** Initialization On Demand Holder pattern */
    private static class LazyHolder {
        public static final Logger LOGGER = LoggerFactory.getLogger(HttpMirrorServer.class);
    }

    private volatile boolean isRunning;

    // Saves the error if one occurs
    private volatile Exception except;

    private final int daemonPort;
    private int maxThreadPoolSize;
    private int maxQueueSize;

    /**
     * Create a new Daemon with the specified port and target.
     *
     * @param port the port to listen on.
     */
    public HttpMirrorServer(int port) {
        this(port, HttpMirrorControl.DEFAULT_MAX_POOL_SIZE, HttpMirrorControl.DEFAULT_MAX_QUEUE_SIZE);
    }

    /**
     * Create a new Daemon with the specified port and target.
     *
     * @param port              the port to listen on.
     * @param maxThreadPoolSize Max Thread pool size
     * @param maxQueueSize      Max Queue size
     */
    public HttpMirrorServer(int port, int maxThreadPoolSize, int maxQueueSize) {
        super("HttpMirrorServer");
        this.daemonPort = port;
        this.maxThreadPoolSize = maxThreadPoolSize;
        this.maxQueueSize = maxQueueSize;
    }

    /**
     * Listen on the daemon port and handle incoming requests. This method will
     * not exit until {@link #stopServer()} is called or an error occurs.
     */
    @Override
    public void run() {
        except = null;
        isRunning = true;
        ServerSocket mainSocket = null;
        ThreadPoolExecutor threadPoolExecutor = null;

        if (maxThreadPoolSize > 0) {
            threadPoolExecutor = new ThreadPoolExecutor(
                    maxThreadPoolSize / 2, maxThreadPoolSize,
                    KEEP_ALIVE_TIME,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(maxQueueSize));
            threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        }

        try {
            getLogger().info("Creating HttpMirror ... on port {}", daemonPort);
            mainSocket = new ServerSocket(daemonPort);
            mainSocket.setSoTimeout(ACCEPT_TIMEOUT);
            getLogger().info("HttpMirror up and running!");
            while (isRunning) {
                try {
                    // Listen on main socket
                    Socket clientSocket = mainSocket.accept();
                    if (isRunning) {
                        // Pass request to new thread
                        if (threadPoolExecutor != null) {
                            threadPoolExecutor.execute(new HttpMirrorThread(clientSocket));
                        } else {
                            Thread thd = new Thread(new HttpMirrorThread(clientSocket));
                            getLogger().debug("Starting new Mirror thread");
                            thd.start();
                        }
                    } else {
                        getLogger().warn("Server not running");
                        JOrphanUtils.closeQuietly(clientSocket);
                    }
                } catch (InterruptedIOException e) {
                    // Timeout occurred. Ignore, and keep looping until we're
                    // told to stop running.
                }
            }
            getLogger().info("HttpMirror Server stopped");
        } catch (BindException e) {
            except = e;
            getLogger().warn(
                    "Could not bind HttpMirror to port {}. Maybe there is already a HttpMirror running?",
                    daemonPort);
        } catch (Exception e) {
            except = e;
            getLogger().warn("HttpMirror Server stopped", e);
        } finally {
            if (threadPoolExecutor != null) {
                threadPoolExecutor.shutdownNow();
            }
            JOrphanUtils.closeQuietly(mainSocket);
        }
    }

    @Override
    public void stopServer() {
        isRunning = false;
    }

    public Exception getException() {
        return except;
    }

    public static void main(String[] args) {
        CLArgsParser clArgsParser = new CLArgsParser(args, options);
        String error = clArgsParser.getErrorString();
        if (error != null) {
            System.err.println("Error: " + error);//NOSONAR
            System.out.println("Usage");//NOSONAR
            System.out.println(CLUtil.describeOptions(options).toString());//NOSONAR
            // repeat the error so no need to scroll back past the usage to see it
            System.out.println("Error: " + error);//NOSONAR
            return;
        }

        if (clArgsParser.getArgumentById(HELP_OPT_ID) != null) {
            System.out.println(CLUtil.describeOptions(options).toString());//NOSONAR
            return;
        }

        int port = getHttpPort(args, clArgsParser);

        if (System.getProperty("log4j.configurationFile") == null) {// $NON-NLS-1$
            Configurator.setRootLevel(Level.INFO);
        }

        setLogLevel(clArgsParser);

        new HttpMirrorServer(port).start();
    }

    private static int getHttpPort(String[] args, CLArgsParser parser) {
        int port = HttpMirrorControl.DEFAULT_PORT;

        if (parser.getArgumentById(PORT_OPT_ID) != null) {
            CLOption option = parser.getArgumentById(PORT_OPT_ID);
            String value = option.getArgument(0);
            try {
                port = Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                // Intentionally left blank
            }
        } else if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                // Intentionally left blank
            }
        }
        return port;
    }

    private static void setLogLevel(CLArgsParser parser) {
        CLOption logLevelOption = parser.getArgumentById(LOGLEVEL_OPT_ID);

        if (logLevelOption == null) {
            return;
        }

        String name = logLevelOption.getArgument(0);
        final Level logLevel = Level.getLevel(name);

        if (logLevel == null) {
            getLogger().warn("Invalid log level '{}'.", name);
            return;
        }

        String value = logLevelOption.getArgument(1);
        if (StringUtils.isEmpty(value)) {
            // Set root level
            getLogger().info("Setting root log level to '{}'", name);// $NON-NLS-1$
            Configurator.setRootLevel(logLevel);
        } else {
            // Set category
            String loggerName = name;
            if (name.startsWith("jmeter") || name.startsWith("jorphan")) {
                loggerName = "org.apache." + name; // $NON-NLS-1$
            }
            getLogger().info("Setting log level to '{}' for '{}'.", value, loggerName); // $NON-NLS-1$
            Configurator.setAllLevels(loggerName, logLevel);
        }
    }

    private static Logger getLogger() {
        return LazyHolder.LOGGER;
    }
}
