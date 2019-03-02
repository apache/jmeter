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
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.DistributedRunner;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.threads.RemoteThreadsListenerTestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

public class RemoteStart extends AbstractAction {

    private static final String LOCAL_HOST = "127.0.0.1"; // NOSONAR $NON-NLS-1$

    private static final String REMOTE_HOSTS = "remote_hosts"; // $NON-NLS-1$ jmeter.properties

    private static final String REMOTE_HOSTS_SEPARATOR = ","; // $NON-NLS-1$

    private static final Set<String> commands = new HashSet<>();

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

    private DistributedRunner distributedRunner = new DistributedRunner();

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
            GuiPackage.getInstance().getMainFrame().showStoppingMessage(name);
            distributedRunner.stop(Arrays.asList(name));
        } else if (action.equals(ActionNames.REMOTE_SHUT)) {
            GuiPackage.getInstance().getMainFrame().showStoppingMessage(name);
            distributedRunner.shutdown(Arrays.asList(name));
        } else if (action.equals(ActionNames.REMOTE_START)) {
            popupShouldSave(e);
            HashTree testTree = getTestTree();
            if ( popupCheckExistingFileListener(testTree) ) {
                distributedRunner.init(Arrays.asList(name), testTree);
                distributedRunner.start(Arrays.asList(name));
            }
        } else if (action.equals(ActionNames.REMOTE_START_ALL)) {
            popupShouldSave(e);
            HashTree testTree = getTestTree();
            if ( popupCheckExistingFileListener(testTree) ) {
                distributedRunner.init(getRemoteHosts(), testTree);
                distributedRunner.start();
            }
        } else if (action.equals(ActionNames.REMOTE_STOP_ALL)) {
            distributedRunner.stop(getRemoteHosts());
        } else if (action.equals(ActionNames.REMOTE_SHUT_ALL)) {
            distributedRunner.shutdown(getRemoteHosts());
        } else if (action.equals(ActionNames.REMOTE_EXIT)) {
            distributedRunner.exit(Arrays.asList(name));
        } else if (action.equals(ActionNames.REMOTE_EXIT_ALL)) {
            distributedRunner.exit(getRemoteHosts());
        }
    }

    private List<String> getRemoteHosts() {
        String remoteHostsString = JMeterUtils.getPropDefault(REMOTE_HOSTS, LOCAL_HOST);
        StringTokenizer st = new StringTokenizer(remoteHostsString, REMOTE_HOSTS_SEPARATOR);
        List<String> list = new LinkedList<>();
        while (st.hasMoreElements()) {
            list.add((String) st.nextElement());
        }
        return list;
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    private HashTree getTestTree() {
        GuiPackage gui = GuiPackage.getInstance();
        HashTree testTree = gui.getTreeModel().getTestPlan();
        HashTree tree = JMeter.convertSubTree(testTree, true);
        tree.add(tree.getArray()[0], gui.getMainFrame());
        // Used for remote notification of threads start/stop,see BUG 54152
        tree.add(tree.getArray()[0], new RemoteThreadsListenerTestElement());
        return tree;
    }
}
