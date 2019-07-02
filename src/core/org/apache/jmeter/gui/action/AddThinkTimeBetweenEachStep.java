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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.thinktime.ThinkTimeCreator;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add ThinkTime (TestAction + UniformRandomTimer)
 * @since 3.2
 */
public class AddThinkTimeBetweenEachStep extends AbstractAction {
    private static final Logger log = LoggerFactory.getLogger(AddThinkTimeBetweenEachStep.class);

    private static final Set<String> commands = new HashSet<>();

    private static final String DEFAULT_IMPLEMENTATION =
            JMeterUtils.getPropDefault("think_time_creator.impl",
                    "org.apache.jmeter.thinktime.DefaultThinkTimeCreator");
    static {
        commands.add(ActionNames.ADD_THINK_TIME_BETWEEN_EACH_STEP);
    }

    /**
     *
     */
    public AddThinkTimeBetweenEachStep() {
        super();
    }

    @Override
    public void doAction(ActionEvent e) {
        GuiPackage guiPackage = GuiPackage.getInstance();
        JMeterTreeNode currentNode = guiPackage.getTreeListener().getCurrentNode();
        if (!
                (currentNode.getUserObject() instanceof Controller ||
                        currentNode.getUserObject() instanceof ThreadGroup)
                ) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try {
            addThinkTimeToChildren(guiPackage, currentNode);
        } catch (Exception err) {
            Toolkit.getDefaultToolkit().beep();
            log.error("Failed to add think times", err);
            JMeterUtils.reportErrorToUser("Failed to add think times", err);
        }
    }

    /**
     * Add Think Time to children of parentNode
     * @param guiPackage {@link GuiPackage}
     * @param parentNode Parent node of elements on which we add think times
     * @throws IllegalUserActionException
     */
    private void addThinkTimeToChildren(GuiPackage guiPackage,
            JMeterTreeNode parentNode) throws IllegalUserActionException {
        guiPackage.updateCurrentNode();
        boolean insertThinkTime;
        try {
            int index = 0;
            while(true) {
                if(index == parentNode.getChildCount()) {
                    break;
                }
                JMeterTreeNode childNode = (JMeterTreeNode) parentNode.getChildAt(index);
                Object userObject = childNode.getUserObject();
                insertThinkTime = childNode.isEnabled()
                        && (userObject instanceof Sampler || userObject instanceof Controller);
                if(insertThinkTime) {
                    JMeterTreeNode[] nodes = createThinkTime(guiPackage, parentNode);
                    if(nodes.length != 2) {
                        throw new IllegalArgumentException("Invalid Think Time, expected 2 nodes, got:"+nodes.length);
                    }
                    index++;
                    addNodesToTreeHierachically(guiPackage, parentNode, nodes, index);
                }
                index++;
            }
        } catch(Exception ex) {
            throw new IllegalUserActionException("Cannot add think times", ex);
        }
    }

    /**
     * add nodes to JMeter Tree
     * @param guiPackage {@link GuiPackage}
     * @param parentNode {@link JMeterTreeNode}
     * @param childNodes Child nodes
     * @param index insertion index
     */
    private void addNodesToTreeHierachically(GuiPackage guiPackage,
            JMeterTreeNode parentNode,
            JMeterTreeNode[] childNodes,
            int index) {
        guiPackage.getTreeModel().insertNodeInto(childNodes[0], parentNode, index);
        guiPackage.getTreeModel().insertNodeInto(childNodes[1], childNodes[0], 0);
    }

    /**
     *
     * @param guiPackage {@link GuiPackage}
     * @param parentNode {@link JMeterTreeNode}
     * @return array of {@link JMeterTreeNode}
     * @throws ReflectiveOperationException when class instantiation for {@value #DEFAULT_IMPLEMENTATION} fails
     * @throws IllegalUserActionException when {@link ThinkTimeCreator#createThinkTime(GuiPackage, JMeterTreeNode)} throws this
     */
    private JMeterTreeNode[] createThinkTime(GuiPackage guiPackage, JMeterTreeNode parentNode)
            throws ReflectiveOperationException, IllegalUserActionException  {
        Class<?> clazz = Class.forName(DEFAULT_IMPLEMENTATION);
        ThinkTimeCreator thinkTimeCreator = (ThinkTimeCreator) clazz.getDeclaredConstructor().newInstance();
        return thinkTimeCreator.createThinkTime(guiPackage, parentNode);
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
