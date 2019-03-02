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
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.avalon.CLArgsParser;
import org.apache.commons.cli.avalon.CLOption;
import org.apache.commons.cli.avalon.CLOptionDescriptor;
import org.apache.commons.cli.avalon.CLUtil;
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
 *
 */
public class HttpMirrorServer extends Thread implements Stoppable, NonTestElement {

    private static final int OPTIONS_OPT        = '?';// $NON-NLS-1$
    private static final int PORT_OPT           = 'P';// $NON-NLS-1$
    private static final int LOGLEVEL_OPT       = 'L';// $NON-NLS-1$

    /**
     * Define the understood options.
     */
    private static final CLOptionDescriptor D_OPTIONS_OPT =
            new CLOptionDescriptor("?", CLOptionDescriptor.ARGUMENT_DISALLOWED, OPTIONS_OPT,
                "print command line options and exit");
    private static final CLOptionDescriptor D_PORT_OPT =
            new CLOptionDescriptor("port", CLOptionDescriptor.ARGUMENT_REQUIRED, PORT_OPT,
                    "Set server port for HttpMirrorServer to use");
    private static final CLOptionDescriptor D_LOGLEVEL_OPT =
            new CLOptionDescriptor("loglevel", CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENTS_REQUIRED_2, LOGLEVEL_OPT,
                    "[category=]level e.g. INFO or DEBUG");

    private static final CLOptionDescriptor[] options = new CLOptionDescriptor[] {
            D_OPTIONS_OPT,
            D_PORT_OPT,
            D_LOGLEVEL_OPT,
    };

    /**
     * The time (in milliseconds) to wait when accepting a client connection.
     * The accept will be retried until the Daemon is told to stop. So this
     * interval is the longest time that the Daemon will have to wait after
     * being told to stop.
     */
    private static final int ACCEPT_TIMEOUT = 1000;

    private static final long KEEP_ALIVE_TIME = 10;

    /**
     * Initialization On Demand Holder pattern
     */
    private static class LazyHolder {
        public static final Logger LOGGER = LoggerFactory.getLogger(HttpMirrorServer.class);
    }

    /** The port to listen on. */
    private final int daemonPort;

    /** True if the Daemon is currently running. */
    private volatile boolean running;

    // Saves the error if one occurs
    private volatile Exception except;

    /**
     * Max Executor Pool size
     */
    private int maxThreadPoolSize;

    /**
     * Max Queue size
     */
    private int maxQueueSize;

    /**
     * Create a new Daemon with the specified port and target.
     *
     * @param port
     *            the port to listen on.
     */
    public HttpMirrorServer(int port) {
       this(port, HttpMirrorControl.DEFAULT_MAX_POOL_SIZE, HttpMirrorControl.DEFAULT_MAX_QUEUE_SIZE);
    }
    
    /**
     * Create a new Daemon with the specified port and target.
     *
     * @param port
     *            the port to listen on.
     * @param maxThreadPoolSize Max Thread pool size
     * @param maxQueueSize Max Queue size
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
        running = true;
        ServerSocket mainSocket = null;
        ThreadPoolExecutor threadPoolExecutor = null;
        if(maxThreadPoolSize>0) {
            final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(
                    maxQueueSize);
            threadPoolExecutor = new ThreadPoolExecutor(
                    maxThreadPoolSize/2, 
                    maxThreadPoolSize, KEEP_ALIVE_TIME, TimeUnit.SECONDS, queue);
            threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        }
        try {
            getLogger().info("Creating HttpMirror ... on port {}", Integer.valueOf(daemonPort));
            mainSocket = new ServerSocket(daemonPort);
            mainSocket.setSoTimeout(ACCEPT_TIMEOUT);
            getLogger().info("HttpMirror up and running!");
            while (running) {
                try {
                    // Listen on main socket
                    Socket clientSocket = mainSocket.accept();
                    if (running) {
                        // Pass request to new thread
                        if(threadPoolExecutor != null) {
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
            getLogger().warn("Could not bind HttpMirror to port {}. Maybe there is already a HttpMirror running?",
                    Integer.valueOf(daemonPort));
        } catch (Exception e) {
            except = e;
            getLogger().warn("HttpMirror Server stopped", e);
        } finally {
            if(threadPoolExecutor != null) {
                threadPoolExecutor.shutdownNow();
            }
            JOrphanUtils.closeQuietly(mainSocket);
        }
    }

    @Override
    public void stopServer() {
        running = false;
    }

    public Exception getException(){
        return except;
    }

    public static void main(String[] args) {
        CLArgsParser parser = new CLArgsParser(args, options);
        String error = parser.getErrorString();
        if (error != null) {
            System.err.println("Error: " + error);//NOSONAR
            System.out.println("Usage");//NOSONAR
            System.out.println(CLUtil.describeOptions(options).toString());//NOSONAR
            // repeat the error so no need to scroll back past the usage to see it
            System.out.println("Error: " + error);//NOSONAR
            return;
        }

        if (parser.getArgumentById(OPTIONS_OPT) != null) {
            System.out.println(CLUtil.describeOptions(options).toString());//NOSONAR
            return;
        }

        int port = HttpMirrorControl.DEFAULT_PORT;

        if (parser.getArgumentById(PORT_OPT) != null) {
            CLOption option = parser.getArgumentById(PORT_OPT);
            String value = option.getArgument(0);
            try {
                port = Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
            }
        } else if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }

        if (System.getProperty("log4j.configurationFile") == null) {// $NON-NLS-1$
            Configurator.setRootLevel(Level.INFO);
        }

        List<CLOption> clOptions = parser.getArguments();

        for (CLOption option : clOptions) {
            String name = option.getArgument(0);
            String value = option.getArgument(1);

            switch (option.getDescriptor().getId()) {
            case LOGLEVEL_OPT:
                if (!value.isEmpty()) { // Set category
                    final Level logLevel = Level.getLevel(value);
                    if (logLevel != null) {
                        String loggerName = name;
                        if (name.startsWith("jmeter") || name.startsWith("jorphan")) {
                            loggerName = "org.apache." + name;// $NON-NLS-1$
                        }
                        getLogger().info("Setting log level to '{}' for '{}'.", value, loggerName);// $NON-NLS-1$ // $NON-NLS-2$
                        Configurator.setAllLevels(loggerName, logLevel);
                    } else {
                        getLogger().warn("Invalid log level, '{}' for '{}'.", value, name);// $NON-NLS-1$ // $NON-NLS-2$
                    }
                } else { // Set root level
                    final Level logLevel = Level.getLevel(name);
                    if (logLevel != null) {
                        getLogger().info("Setting root log level to '{}'", name);// $NON-NLS-1$
                        Configurator.setRootLevel(logLevel);
                    } else {
                        getLogger().warn("Invalid log level, '{}' for the root logger.", name);// $NON-NLS-1$ // $NON-NLS-2$
                    }
                }
                break;
            default:
                break;
            }
        }

        HttpMirrorServer serv = new HttpMirrorServer(port);
        serv.start();
    }

    private static Logger getLogger() {
        return LazyHolder.LOGGER;
    }
}
