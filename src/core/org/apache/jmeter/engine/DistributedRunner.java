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
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This class serves all responsibility of starting and stopping distributed tests.
 * It was refactored from JMeter and RemoteStart classes to unify retry behavior.
 *
 * @see org.apache.jmeter.JMeter
 * @see org.apache.jmeter.gui.action.RemoteStart
 */
public class DistributedRunner {
    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String RETRIES_NUMBER = "rmi.retries_number"; // $NON-NLS-1$
    public static final String RETRIES_DELAY = "rmi.retries_delay"; // $NON-NLS-1$
    public static final String CONTINUE_ON_FAIL = "rmi.continue_on_fail"; // $NON-NLS-1$

    private final Properties remoteProps;
    private final boolean continueOnFail;
    private final int retriesDelay;
    private final int retriesNumber;
    private PrintStream stdout = new PrintStream(new SilentOutputStream());
    private PrintStream stderr = new PrintStream(new SilentOutputStream());
    private final Map<String, JMeterEngine> engines = new HashMap<String, JMeterEngine>();


    public DistributedRunner() {
        this(new Properties());
    }

    // NOTE: looks like this constructor is used only from non-Gui and Gui runs does not send props to remote...
    public DistributedRunner(Properties props) {
        remoteProps = props;
        retriesNumber = JMeterUtils.getPropDefault(RETRIES_NUMBER, 1);
        continueOnFail = JMeterUtils.getPropDefault(CONTINUE_ON_FAIL, false);
        retriesDelay = JMeterUtils.getPropDefault(RETRIES_DELAY, 5000);
    }

    public void init(List<String> hostNames, HashTree tree) {
        // converting list into mutable version
        List<String> hosts = new LinkedList<String>(hostNames);

        for (int tryNo = 0; tryNo < retriesNumber; tryNo++) {
            if (tryNo > 0) {
                println("Following remote engines will retry configuring: " + hosts);
                println("Pausing befor retry for " + retriesDelay + "ms");
                try {
                    Thread.sleep(retriesDelay);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted while initializing remote", e);
                }
            }

            int idx = 0;
            while (idx < hosts.size()) {
                String host = hosts.get(idx);
                println("Configuring remote engine: " + host);
                JMeterEngine engine = getClientEngine(host.trim(), tree);
                if (engine != null) {
                    engines.put(host, engine);
                    hosts.remove(host);
                } else {
                    println("Failed to configure " + host);
                    idx++;
                }
            }

            if (hosts.size() == 0) {
                break;
            }
        }

        if (hosts.size() > 0) {
            String msg = "Following remote engines could not be configured:" + hosts;
            if (!continueOnFail) {
                throw new RuntimeException(msg);
            } else {
                println(msg);
                println("Continuing without failed engines...");
            }
        }
    }

    /**
     * Starts a remote testing engines
     *
     * @param hosts list of the DNS names or IP addresses of the remote testing engines
     */
    public void start(List<String> hosts) {
        println("Starting remote engines");
        long now = System.currentTimeMillis();
        println("Starting the test @ " + new Date(now) + " (" + now + ")");
        for (String host : hosts) {
            try {
                engines.get(host).runTest();
            } catch (IllegalStateException e) {
                JMeterUtils.reportErrorToUser(e.getMessage(), JMeterUtils.getResString("remote_error_starting")); // $NON-NLS-1$
            } catch (JMeterEngineException e) {
                JMeterUtils.reportErrorToUser(e.getMessage(), JMeterUtils.getResString("remote_error_starting")); // $NON-NLS-1$
            }
        }
        println("Remote engines have been started");
    }

    /**
     * Start all engines that were previously initiated
     */
    public void start() {
        List<String> hosts = new LinkedList<String>();
        hosts.addAll(engines.keySet());
        start(hosts);
    }


    public void stop(List<String> hosts) {
        println("Stopping remote engines");
        for (String host : hosts) {
            try {
                engines.get(host).stopTest(true);
            } catch (RuntimeException e) {
                errln("Failed to stop test on " + host, e);
            }
        }
        println("Remote engines have been stopped");
    }

    public void shutdown(List<String> hosts) {
        println("Shutting down remote engines");
        for (String host : hosts) {
            try {
                engines.get(host).stopTest(false);
            } catch (RuntimeException e) {
                errln("Failed to shutdown test on " + host, e);
            }
        }
        println("Remote engines have been shut down");
    }

    public void exit(List<String> hosts) {
        println("Exiting remote engines");
        for (String host : hosts) {
            try {
                engines.get(host).exit();
            } catch (RuntimeException e) {
                errln("Failed to exit on " + host, e);
            }
        }
        println("Remote engines have been exited");
    }

    private JMeterEngine getClientEngine(String hostName, HashTree testTree) {
        JMeterEngine engine;
        try {
            engine = createEngine(hostName);
            engine.configure(testTree);
            if (!remoteProps.isEmpty()) {
                engine.setProperties(remoteProps);
            }
            return engine;
        } catch (Exception ex) {
            JMeterUtils.reportErrorToUser(ex.getMessage(),
                    JMeterUtils.getResString("remote_error_init") + ": " + hostName); // $NON-NLS-1$ $NON-NLS-2$
            return null;
        }
    }

    /**
     * A factory method that might be overridden for unit testing
     *
     * @param hostName address for engine
     * @return engine instance
     * @throws RemoteException
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    protected JMeterEngine createEngine(String hostName) throws RemoteException, NotBoundException, MalformedURLException {
        return new ClientJMeterEngine(hostName);
    }

    private void println(String s) {
        log.info(s);
        stdout.println(s);
    }

    private void errln(String s, Exception e) {
        log.error(s, e);
        stderr.println(s + ": ");
        e.printStackTrace(stderr);
    }

    public void setStdout(PrintStream stdout) {
        this.stdout = stdout;
    }

    public void setStdErr(PrintStream stdErr) {
        this.stderr = stdErr;
    }

    private class SilentOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            // enjoy the silence
        }
    }
}
