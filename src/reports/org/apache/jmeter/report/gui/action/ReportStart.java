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

package org.apache.jmeter.report.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.gui.action.ActionNames;
//import org.apache.jorphan.logging.LoggingManager;
//import org.apache.log.Logger;

public class ReportStart extends AbstractAction {
    //private static final Logger log = LoggingManager.getLoggerForClass();

    private static final Set<String> commands = new HashSet<String>();
    static {
        commands.add(ActionNames.ACTION_START);
        commands.add(ActionNames.ACTION_STOP);
        commands.add(ActionNames.ACTION_SHUTDOWN);
    }
    // FIXME Due to startEngine being commented engine will always be null
    private StandardJMeterEngine engine;

    /**
     * Constructor for the Start object.
     */
    public ReportStart() {
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
            startEngine();
        } else if (e.getActionCommand().equals(ActionNames.ACTION_STOP)) {
            if (engine != null) {
                ReportGuiPackage.getInstance().getMainFrame().showStoppingMessage("");
                engine.stopTest();
                engine = null;
            }
        } else if (e.getActionCommand().equals(ActionNames.ACTION_SHUTDOWN)) {
            if (engine != null) {
                ReportGuiPackage.getInstance().getMainFrame().showStoppingMessage("");
                engine.askThreadsToStop();
                engine = null;
            }
        }
    }

    protected void startEngine() {
        /**
         * this will need to be changed
        ReportGuiPackage gui = ReportGuiPackage.getInstance();
        engine = new StandardJMeterEngine();
        HashTree testTree = gui.getTreeModel().getTestPlan();
        convertSubTree(testTree);
        DisabledComponentRemover remover = new DisabledComponentRemover(testTree);
        testTree.traverse(remover);
        testTree.add(testTree.getArray()[0], gui.getMainFrame());
        log.debug("test plan before cloning is running version: "
                + ((TestPlan) testTree.getArray()[0]).isRunningVersion());
        TreeCloner cloner = new TreeCloner(false);
        testTree.traverse(cloner);
        engine.configure(cloner.getClonedTree());
        try {
            engine.runTest();
        } catch (JMeterEngineException e) {
            JOptionPane.showMessageDialog(gui.getMainFrame(), e.getMessage(), JMeterUtils
                    .getResString("Error Occurred"), JOptionPane.ERROR_MESSAGE);
        }
        log.debug("test plan after cloning and running test is running version: "
                + ((TestPlan) testTree.getArray()[0]).isRunningVersion());
         */
    }
}
