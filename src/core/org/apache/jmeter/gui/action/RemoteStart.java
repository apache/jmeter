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

package org.apache.jmeter.gui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.ClientJMeterEngine;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.threads.RemoteThreadsListenerTestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class RemoteStart extends AbstractAction {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String LOCAL_HOST = "127.0.0.1"; // $NON-NLS-1$

    private static final String REMOTE_HOSTS = "remote_hosts"; // $NON-NLS-1$ jmeter.properties

    private static final String REMOTE_HOSTS_SEPARATOR = ","; // $NON-NLS-1$

    private static final Set<String> commands = new HashSet<String>();

    static {
        commands.add(ActionNames.REMOTE_START);
        commands.add(ActionNames.REMOTE_STOP);
        commands.add(ActionNames.REMOTE_SHUT);
        commands.add(ActionNames.REMOTE_START_ALL);
        commands.add(ActionNames.REMOTE_STOP_ALL);
        commands.add(ActionNames.REMOTE_SHUT_ALL);
        commands.add(ActionNames.REMOTE_EXIT);
        commands.add(ActionNames.REMOTE_EXIT_ALL);
    }

    private final Map<String, JMeterEngine> remoteEngines = new HashMap<String, JMeterEngine>();

    public RemoteStart() {
    }

    @Override
    public void doAction(ActionEvent e) {
        String name = ((Component) e.getSource()).getName();
        if (name != null) {
            name = name.trim();
        }
        String action = e.getActionCommand();
        if (action.equals(ActionNames.REMOTE_STOP)) {
            doRemoteStop(name, true);
        } else if (action.equals(ActionNames.REMOTE_SHUT)) {
            doRemoteStop(name, false);
        } else if (action.equals(ActionNames.REMOTE_START)) {
            popupShouldSave(e);
            doRemoteInit(name);
            doRemoteStart(name);
        } else if (action.equals(ActionNames.REMOTE_START_ALL)) {
            popupShouldSave(e);
            String remote_hosts_string = JMeterUtils.getPropDefault(REMOTE_HOSTS, LOCAL_HOST);
            java.util.StringTokenizer st = new java.util.StringTokenizer(remote_hosts_string, REMOTE_HOSTS_SEPARATOR);
            while (st.hasMoreElements()) {
                String el = (String) st.nextElement();
                doRemoteInit(el.trim());
            }
            st = new java.util.StringTokenizer(remote_hosts_string, REMOTE_HOSTS_SEPARATOR);
            while (st.hasMoreElements()) {
                String el = (String) st.nextElement();
                doRemoteStart(el.trim());
            }
        } else if (action.equals(ActionNames.REMOTE_STOP_ALL)) {
            doRemoteStopAll(true);
        } else if (action.equals(ActionNames.REMOTE_SHUT_ALL)) {
            doRemoteStopAll(false);
        } else if (action.equals(ActionNames.REMOTE_EXIT)) {
            doRemoteExit(name);
        } else if (action.equals(ActionNames.REMOTE_EXIT_ALL)) {
            String remote_hosts_string = JMeterUtils.getPropDefault(REMOTE_HOSTS, LOCAL_HOST);
            java.util.StringTokenizer st = new java.util.StringTokenizer(remote_hosts_string, REMOTE_HOSTS_SEPARATOR);
            while (st.hasMoreElements()) {
                String el = (String) st.nextElement();
                doRemoteExit(el.trim());
            }
        }
    }

    private void doRemoteStopAll(boolean now) {
        String remote_hosts_string = JMeterUtils.getPropDefault(REMOTE_HOSTS, LOCAL_HOST);
        java.util.StringTokenizer st = new java.util.StringTokenizer(remote_hosts_string, REMOTE_HOSTS_SEPARATOR);
        while (st.hasMoreElements()) {
            String el = (String) st.nextElement();
            doRemoteStop(el.trim(), now);
        }
    }

    /**
     * Stops a remote testing engine
     *
     * @param name
     *            the DNS name or IP address of the remote testing engine
     *
     */
    private void doRemoteStop(String name, boolean now) {
        GuiPackage.getInstance().getMainFrame().showStoppingMessage(name);
        JMeterEngine engine = remoteEngines.get(name);
        // Engine may be null if it has not correctly started
        if(engine != null) {
            engine.stopTest(now);
        }
    }

    /**
     * Exits a remote testing engine
     *
     * @param name
     *            the DNS name or IP address of the remote testing engine
     *
     */
    private void doRemoteExit(String name) {
        JMeterEngine engine = remoteEngines.get(name);
        if (engine == null) {
            return;
        }
        // GuiPackage.getInstance().getMainFrame().showStoppingMessage(name);
        engine.exit();
    }

    /**
     * Starts a remote testing engine
     *
     * @param name
     *            the DNS name or IP address of the remote testing engine
     *
     */
    private void doRemoteStart(String name) {
        JMeterEngine engine = remoteEngines.get(name);
        if (engine != null) {
            try {
                engine.runTest();
            } catch (IllegalStateException e) {
                JMeterUtils.reportErrorToUser(e.getMessage(),JMeterUtils.getResString("remote_error_starting")); // $NON-NLS-1$
            } catch (JMeterEngineException e) {
                JMeterUtils.reportErrorToUser(e.getMessage(),JMeterUtils.getResString("remote_error_starting")); // $NON-NLS-1$
            }
        }
    }

    /**
     * Initializes remote engines
     */
    private void doRemoteInit(String name) {
        JMeterEngine engine = remoteEngines.get(name);
        if (engine == null) {
            try {
                log.info("Initialising remote engine: "+name);
                engine = new ClientJMeterEngine(name);
                remoteEngines.put(name, engine);
            } catch (Exception ex) {
                log.error("Failed to initialise remote engine", ex);
                JMeterUtils.reportErrorToUser(ex.getMessage(),
                        JMeterUtils.getResString("remote_error_init") + ": " + name); // $NON-NLS-1$ $NON-NLS-2$
                return;
            }
        } else {
            engine.reset();
        }
        initEngine(engine);
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    /**
     * Initializes test on engine.
     *
     * @param engine
     *            remote engine object
     */
    private void initEngine(JMeterEngine engine) {
        GuiPackage gui = GuiPackage.getInstance();
        HashTree testTree = gui.getTreeModel().getTestPlan();
        JMeter.convertSubTree(testTree);
        testTree.add(testTree.getArray()[0], gui.getMainFrame());
        // Used for remote notification of threads start/stop,see BUG 54152
        testTree.add(testTree.getArray()[0], new RemoteThreadsListenerTestElement());
        engine.configure(testTree);
    }
}
