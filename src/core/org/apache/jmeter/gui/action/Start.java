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

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.engine.TreeClonerNoTimer;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class Start extends AbstractAction {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final Set<String> commands = new HashSet<String>();

    static {
        commands.add(ActionNames.ACTION_START);
        commands.add(ActionNames.ACTION_START_NO_TIMERS);
        commands.add(ActionNames.ACTION_STOP);
        commands.add(ActionNames.ACTION_SHUTDOWN);
    }

    private StandardJMeterEngine engine;

    /**
     * Constructor for the Start object.
     */
    public Start() {
    }

    /**
     * Gets the ActionNames attribute of the Start object.
     *
     * @return the ActionNames value
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public void doAction(ActionEvent e) {
        if (e.getActionCommand().equals(ActionNames.ACTION_START)) {
            popupShouldSave(e);
            startEngine(false);
        } else if (e.getActionCommand().equals(ActionNames.ACTION_START_NO_TIMERS)) {
            popupShouldSave(e);
            startEngine(true);
        } else if (e.getActionCommand().equals(ActionNames.ACTION_STOP)) {
            if (engine != null) {
                log.info("Stopping test");
                GuiPackage.getInstance().getMainFrame().showStoppingMessage("");
                engine.stopTest();
            }
        } else if (e.getActionCommand().equals(ActionNames.ACTION_SHUTDOWN)) {
            if (engine != null) {
                log.info("Shutting test down");
                GuiPackage.getInstance().getMainFrame().showStoppingMessage("");
                engine.askThreadsToStop();
            }
        }
    }

    /**
     * Start JMeter engine
     * @param noTimer ignore timers 
     */
    private void startEngine(boolean ignoreTimer) {
        GuiPackage gui = GuiPackage.getInstance();
        HashTree testTree = gui.getTreeModel().getTestPlan();
        JMeter.convertSubTree(testTree);
        testTree.add(testTree.getArray()[0], gui.getMainFrame());
        log.debug("test plan before cloning is running version: "
                + ((TestPlan) testTree.getArray()[0]).isRunningVersion());
        TreeCloner cloner = cloneTree(testTree, ignoreTimer);
        engine = new StandardJMeterEngine();
        engine.configure(cloner.getClonedTree());
        try {
            engine.runTest();
        } catch (JMeterEngineException e) {
            JOptionPane.showMessageDialog(gui.getMainFrame(), e.getMessage(), JMeterUtils
                    .getResString("Error Occurred"), JOptionPane.ERROR_MESSAGE);
        }
        log.debug("test plan after cloning and running test is running version: "
                + ((TestPlan) testTree.getArray()[0]).isRunningVersion());
    }
    
    /**
     * Create a Cloner that ignores {@link Timer} if removeTimers is true
     * @param testTree {@link HashTree}
     * @param removeTimers boolean remove timers 
     * @return {@link TreeCloner}
     */
    private TreeCloner cloneTree(HashTree testTree, boolean removeTimers) {
        TreeCloner cloner = null;
        if(removeTimers) {
            cloner = new TreeClonerNoTimer(false);
        } else {
            cloner = new TreeCloner(false);     
        }
        testTree.traverse(cloner);
        return cloner;
    }
}
