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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to apply naming convention on nodes
 * @since 3.2
 */
public class ApplyNamingConvention extends AbstractAction {
    private static final Logger log = LoggerFactory.getLogger(ApplyNamingConvention.class);

    private static final Set<String> commands = new HashSet<>();
    

    static {
        commands.add(ActionNames.APPLY_NAMING_CONVENTION);
    }

    public ApplyNamingConvention() {
        super();
    }

    @Override
    public void doAction(ActionEvent e) {
        GuiPackage guiPackage = GuiPackage.getInstance();
        JMeterTreeNode[] currentNodes = guiPackage.getTreeListener().getSelectedNodes();
        List<JMeterTreeNode> filteredNodes = new ArrayList<>();
        for (JMeterTreeNode jMeterTreeNode : currentNodes) {
            if (jMeterTreeNode.getUserObject() instanceof Controller) {
                filteredNodes.add(jMeterTreeNode);
            } else {
                log.warn("Applying naming policy, selected node {} is not a Controller, will ignore it", jMeterTreeNode.getName());
            }
        }
        try {
            for (JMeterTreeNode currentNode : filteredNodes) {
                applyNamingPolicyToCurrentNode(guiPackage, currentNode);
            }
            GuiPackage.getInstance().getMainFrame().repaint();
        } catch (Exception err) {
            Toolkit.getDefaultToolkit().beep();
            log.error("Failed to apply naming policy", err);
            JMeterUtils.reportErrorToUser("Failed to apply naming policy", err);
        }
    }

    /**
     * Apply the naming policy of currentNode children
     * @param guiPackage {@link GuiPackage}
     * @param currentNode Parent node of elements on which we apply naming policy
     */
    private void applyNamingPolicyToCurrentNode(GuiPackage guiPackage, 
            JMeterTreeNode currentNode) {
        TreeNodeNamingPolicy namingPolicy = guiPackage.getNamingPolicy();
        guiPackage.updateCurrentNode();
        Enumeration<?> enumeration = currentNode.children();
        int index = 0;
        namingPolicy.resetState(currentNode);
        while(enumeration.hasMoreElements()) {
            JMeterTreeNode childNode = (JMeterTreeNode)enumeration.nextElement();
            namingPolicy.rename(currentNode, childNode, index);
            index++;
        }
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
