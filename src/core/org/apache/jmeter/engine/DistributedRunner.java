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

package org.apache.jmeter.engine;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class serves all responsibility of starting and stopping distributed tests.
 * It was refactored from JMeter and RemoteStart classes to unify retry behavior.
 *
 * @see org.apache.jmeter.JMeter
 * @see org.apache.jmeter.gui.action.RemoteStart
 */
public class DistributedRunner {
    private static final String HOST_NOT_FOUND_MESSAGE = "Host not found in list of active engines: {}";

    private static final Logger log = LoggerFactory.getLogger(DistributedRunner.class);

    public static final String RETRIES_NUMBER = "client.tries"; // $NON-NLS-1$
    public static final String RETRIES_DELAY = "client.retries_delay"; // $NON-NLS-1$
    public static final String CONTINUE_ON_FAIL = "client.continue_on_fail"; // $NON-NLS-1$

    private final Properties remoteProps;
    private final boolean continueOnFail;
    private final int retriesDelay;
    private final int retriesNumber;
    private PrintStream stdout = new PrintStream(new SilentOutputStream());
    private PrintStream stderr = new PrintStream(new SilentOutputStream());
    private final Map<String, JMeterEngine> engines = new HashMap<>();


    public DistributedRunner() {
        this(new Properties());
    }

    public DistributedRunner(Properties props) {
        remoteProps = props;
        retriesNumber = JMeterUtils.getPropDefault(RETRIES_NUMBER, 1);
        continueOnFail = JMeterUtils.getPropDefault(CONTINUE_ON_FAIL, false);
        retriesDelay = JMeterUtils.getPropDefault(RETRIES_DELAY, 5000);
    }

    public void init(List<String> addresses, HashTree tree) {
        // converting list into mutable version
        List<String> addrs = new LinkedList<>(addresses);

        for (int tryNo = 0; tryNo < retriesNumber; tryNo++) {
            if (tryNo > 0) {
                println("Following remote engines will retry configuring: " + addrs+", pausing before retry for " + retriesDelay + "ms");
                try {
                    Thread.sleep(retriesDelay);
                } catch (InterruptedException e) {  // NOSONAR
                    throw new IllegalStateException("Interrupted while initializing remote engines:"+addrs, e);
                }
            }

            int idx = 0;
            while (idx < addrs.size()) {
                String address = addrs.get(idx);
                println("Configuring remote engine: " + address);
                JMeterEngine engine = getClientEngine(address.trim(), tree);
                if (engine != null) {
                    engines.put(address, engine);
                    addrs.remove(address);
                } else {
                    println("Failed to configure " + address);
                    idx++;
                }
            }

            if (addrs.isEmpty()) {
                break;
            }
        }

        if (!addrs.isEmpty()) {
            String msg = "Following remote engines could not be configured:" + addrs;
            if (!continueOnFail || engines.isEmpty()) {
                stop();
                throw new RuntimeException(msg); // NOSONAR
            } else {
                println(msg);
                println("Continuing without failed engines...");
            }
        }
    }

    /**
     * Starts a remote testing engines
     *
     * @param addresses list of the DNS names or IP addresses of the remote testing engines
     */
    public void start(List<String> addresses) {
        println("Starting remote engines");
        long now = System.currentTimeMillis();
        println("Starting the test @ " + new Date(now) + " (" + now + ")");
        for (String address : addresses) {
            try {
                if (engines.containsKey(address)) {
                    engines.get(address).runTest();
                } else {
                    log.warn(HOST_NOT_FOUND_MESSAGE, address);
                }
            } catch (IllegalStateException | JMeterEngineException e) { // NOSONAR already reported to user
                JMeterUtils.reportErrorToUser(e.getMessage(), JMeterUtils.getResString("remote_error_starting")); // $NON-NLS-1$  
            }
        }
        println("Remote engines have been started");
    }

    /**
     * Start all engines that were previously initiated
     */
    public void start() {
        List<String> addresses = new LinkedList<>();
        addresses.addAll(engines.keySet());
        start(addresses);
    }

    public void stop(List<String> addresses) {
        println("Stopping remote engines");
        for (String address : addresses) {
            try {
                if (engines.containsKey(address)) {
                    engines.get(address).stopTest(true);
                } else {
                    log.warn(HOST_NOT_FOUND_MESSAGE, address);
                }
            } catch (RuntimeException e) {
                errln("Failed to stop test on " + address, e);
            }
        }
        println("Remote engines have been stopped");
    }

    /**
     * Stop all engines that were previously initiated
     */
    public void stop() {
        List<String> addresses = new LinkedList<>();
        addresses.addAll(engines.keySet());
        stop(addresses);
    }

    public void shutdown(List<String> addresses) {
        println("Shutting down remote engines");
        for (String address : addresses) {
            try {
                if (engines.containsKey(address)) {
                    engines.get(address).stopTest(false);
                } else {
                    log.warn(HOST_NOT_FOUND_MESSAGE, address);
                }

            } catch (RuntimeException e) {
                errln("Failed to shutdown test on " + address, e);
            }
        }
        println("Remote engines have been shut down");
    }

    public void exit(List<String> addresses) {
        println("Exiting remote engines");
        for (String address : addresses) {
            try {
                if (engines.containsKey(address)) {
                    engines.get(address).exit();
                } else {
                    log.warn(HOST_NOT_FOUND_MESSAGE, address);
                }
            } catch (RuntimeException e) {
                errln("Failed to exit on " + address, e);
            }
        }
        println("Remote engines have been exited");
    }

    private JMeterEngine getClientEngine(String address, HashTree testTree) {
        JMeterEngine engine;
        try {
            engine = createEngine(address);
            engine.configure(testTree);
            if (!remoteProps.isEmpty()) {
                engine.setProperties(remoteProps);
            }
            return engine;
        } catch (Exception ex) {
            log.error("Failed to create engine at {}", address, ex);
            JMeterUtils.reportErrorToUser(ex.getMessage(),
                    JMeterUtils.getResString("remote_error_init") + ": " + address); // $NON-NLS-1$ $NON-NLS-2$
            return null;
        }
    }

    /**
     * A factory method that might be overridden for unit testing
     *
     * @param address address for engine
     * @return engine instance
     * @throws RemoteException if registry can't be contacted
     * @throws NotBoundException when name for address can't be found
     */
    protected JMeterEngine createEngine(String address) throws RemoteException, NotBoundException {
        return new ClientJMeterEngine(address);
    }

    private void println(String s) {
        log.info(s);
        stdout.println(s);
    }

    private void errln(String s, Exception e) {
        log.error(s, e);
        stderr.println(s + ": ");
        e.printStackTrace(stderr); // NOSONAR
    }

    public void setStdout(PrintStream stdout) {
        this.stdout = stdout;
    }

    public void setStdErr(PrintStream stdErr) {
        this.stderr = stdErr;
    }

    private static class SilentOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            // enjoy the silence
        }
    }

    /**
     * @return {@link Collection} of {@link JMeterEngine}
     */
    public Collection<? extends JMeterEngine> getEngines() {
        return engines.values();
    }
}
