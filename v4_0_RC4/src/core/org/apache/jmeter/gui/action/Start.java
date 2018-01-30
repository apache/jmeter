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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.engine.TreeClonerNoTimer;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.validation.TreeClonerForValidation;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set of Actions to:
 * <ul>
 *      <li>Start a Test Plan</li>
 *      <li>Start a Test Plan without sleeping on the timers</li>
 *      <li>Stop a Test Plan</li>
 *      <li>Shutdown a Test plan</li>
 *      <li>Run a set of Thread Groups</li>
 *      <li>Run a set of Thread Groups without sleeping on the timers</li>
 *      <li>Validate a set of Thread Groups with/without sleeping on the timers depending on jmeter properties</li>
 * </ul>
 */
public class Start extends AbstractAction {
    
    private static final Logger log = LoggerFactory.getLogger(Start.class);

    private static final Set<String> commands = new HashSet<>();

    private static final String VALIDATION_CLONER_CLASS_PROPERTY_NAME = 
            "testplan_validation.tree_cloner_class"; //$NON-NLS-1$
    /**
     * Implementation of {@link TreeCloner} used to clone the tree before running validation
     */
    private static final String CLONER_FOR_VALIDATION_CLASS_NAME = 
            JMeterUtils.getPropDefault(VALIDATION_CLONER_CLASS_PROPERTY_NAME, //$NON-NLS-1$ 
                    "org.apache.jmeter.validation.ComponentTreeClonerForValidation");

    static {
        commands.add(ActionNames.ACTION_START);
        commands.add(ActionNames.ACTION_START_NO_TIMERS);
        commands.add(ActionNames.ACTION_STOP);
        commands.add(ActionNames.ACTION_SHUTDOWN);
        commands.add(ActionNames.RUN_TG);
        commands.add(ActionNames.RUN_TG_NO_TIMERS);
        commands.add(ActionNames.VALIDATE_TG);
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
        } else if (e.getActionCommand().equals(ActionNames.RUN_TG) 
                || e.getActionCommand().equals(ActionNames.RUN_TG_NO_TIMERS)
                || e.getActionCommand().equals(ActionNames.VALIDATE_TG)) {
            popupShouldSave(e);
            boolean noTimers = e.getActionCommand().equals(ActionNames.RUN_TG_NO_TIMERS);
            boolean isValidation = e.getActionCommand().equals(ActionNames.VALIDATE_TG);
            
            JMeterTreeListener treeListener = GuiPackage.getInstance().getTreeListener();
            JMeterTreeNode[] nodes = treeListener.getSelectedNodes();
            nodes = Copy.keepOnlyAncestors(nodes);
            AbstractThreadGroup[] tg = keepOnlyThreadGroups(nodes);
            if(nodes.length > 0) {
                startEngine(noTimers, isValidation, tg);
            }
            else {
                log.warn("No thread group selected the test will not be started");
            }
        } 
    }

    /**
     * filter the nodes to keep only the thread group
     * @param currentNodes jmeter tree nodes
     * @return the thread groups
     */
    private AbstractThreadGroup[] keepOnlyThreadGroups(JMeterTreeNode[] currentNodes) {
        List<AbstractThreadGroup> nodes = new ArrayList<>();
        for (JMeterTreeNode jMeterTreeNode : currentNodes) {
            if(jMeterTreeNode.getTestElement() instanceof AbstractThreadGroup) {
                nodes.add((AbstractThreadGroup) jMeterTreeNode.getTestElement());
            }
        }
        return nodes.toArray(new AbstractThreadGroup[nodes.size()]);
    }

    /**
     * Start JMeter engine
     * @param ignoreTimer flag to ignore timers
     */
    private void startEngine(boolean ignoreTimer) {
        startEngine(ignoreTimer, null);
    }
    
    /**
     * Start JMeter engine
     * @param ignoreTimer flag to ignore timers
     * @param threadGroupsToRun Array of AbstractThreadGroup to run
     */
    private void startEngine(boolean ignoreTimer, 
            AbstractThreadGroup[] threadGroupsToRun) {
        startEngine(ignoreTimer, false, threadGroupsToRun);
    }
    
    /**
     * Start JMeter engine
     * @param ignoreTimer flag to ignore timers
     * @param isValidationShot 
     * @param threadGroupsToRun Array of AbstractThreadGroup to run
     */
    private void startEngine(boolean ignoreTimer, 
            boolean isValidationShot,
            AbstractThreadGroup[] threadGroupsToRun) {
        GuiPackage gui = GuiPackage.getInstance();
        HashTree testTree = gui.getTreeModel().getTestPlan();
        
        JMeter.convertSubTree(testTree);
        if(threadGroupsToRun != null && threadGroupsToRun.length>0) {
            removeThreadGroupsFromHashTree(testTree, threadGroupsToRun);
        }
        testTree.add(testTree.getArray()[0], gui.getMainFrame());
        if (log.isDebugEnabled()) {
            log.debug("test plan before cloning is running version: {}",
                    ((TestPlan) testTree.getArray()[0]).isRunningVersion());
        }

        ListedHashTree clonedTree = null;
        if(isValidationShot) {
            TreeCloner cloner = createTreeClonerForValidation();
            testTree.traverse(cloner);
            clonedTree = cloner.getClonedTree();
        } else {
            TreeCloner cloner = cloneTree(testTree, ignoreTimer);      
            clonedTree = cloner.getClonedTree();
        }
        if ( popupCheckExistingFileListener(testTree) ) {
            engine = new StandardJMeterEngine();
            engine.configure(clonedTree);
            try {
                engine.runTest();
            } catch (JMeterEngineException e) {
                JOptionPane.showMessageDialog(gui.getMainFrame(), e.getMessage(), 
                        JMeterUtils.getResString("error_occurred"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            }
            if (log.isDebugEnabled()) {
                log.debug("test plan after cloning and running test is running version: {}",
                        ((TestPlan) testTree.getArray()[0]).isRunningVersion());
            }
        }
    }

    /**
     * 
     * @return {@link TreeCloner}
     */
    private static TreeCloner createTreeClonerForValidation() {
        Class<?> clazz;
        try {
            clazz = Class.forName(CLONER_FOR_VALIDATION_CLASS_NAME, true, Thread.currentThread().getContextClassLoader());
            return (TreeCloner) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            log.error("Error instantiating class:'{}' defined in property:'{}'", CLONER_FOR_VALIDATION_CLASS_NAME,
                    VALIDATION_CLONER_CLASS_PROPERTY_NAME, ex);
            return new TreeClonerForValidation();
        }
    }

    /**
     * Remove thread groups from testTree that are not in threadGroupsToKeep
     * @param testTree {@link HashTree}
     * @param threadGroupsToKeep Array of {@link AbstractThreadGroup} to keep
     */
    private void removeThreadGroupsFromHashTree(HashTree testTree, AbstractThreadGroup[] threadGroupsToKeep) {
        LinkedList<Object> copyList = new LinkedList<>(testTree.list());
        for (Object o  : copyList) {
            TestElement item = (TestElement) o;
            if (o instanceof AbstractThreadGroup) {
                if (!isInThreadGroups(item, threadGroupsToKeep)) {
                    // hack hack hack
                    // due to the bug of equals / hashcode on AbstractTestElement
                    // where 2 AbstractTestElement can be equals but have different hashcode
                    try {
                        item.setEnabled(false);
                        testTree.remove(item);
                    } finally {
                        item.setEnabled(true);                        
                    }
                }
                else {
                    removeThreadGroupsFromHashTree(testTree.getTree(item), threadGroupsToKeep);
                }
            }
            else {
                removeThreadGroupsFromHashTree(testTree.getTree(item), threadGroupsToKeep);
            }
        }
    }
    
    /**
     * @param item {@link TestElement}
     * @param threadGroups Array of {@link AbstractThreadGroup} 
     * @return true if item is in threadGroups array
     */
    private boolean isInThreadGroups(TestElement item, AbstractThreadGroup[] threadGroups) {
        for (AbstractThreadGroup abstractThreadGroup : threadGroups) {
            if(item == abstractThreadGroup) {
                return true;
            }
        }
        return false;
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
