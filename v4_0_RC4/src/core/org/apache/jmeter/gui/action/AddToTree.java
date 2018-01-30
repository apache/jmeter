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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.tree.TreePath;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddToTree extends AbstractAction {
    private static final Logger log = LoggerFactory.getLogger(AddToTree.class);

    private static final Set<String> commandSet;

    static {
        Set<String> commands = new HashSet<>();
        commands.add(ActionNames.ADD);
        commandSet = Collections.unmodifiableSet(commands);
    }

    public AddToTree() {
    }

    /**
     * Gets the Set of actions this Command class responds to.
     *
     * @return the ActionNames value
     */
    @Override
    public Set<String> getActionNames() {
        return commandSet;
    }

    /**
     * Adds the specified class to the current node of the tree.
     */
    @Override
    public void doAction(ActionEvent e) {
        GuiPackage guiPackage = GuiPackage.getInstance();
        try {
            guiPackage.updateCurrentNode();
            TestElement testElement = guiPackage.createTestElement(((JComponent) e.getSource()).getName());
            JMeterTreeNode parentNode = guiPackage.getCurrentNode();
            JMeterTreeNode node = guiPackage.getTreeModel().addComponent(testElement, parentNode);
            guiPackage.getNamingPolicy().nameOnCreation(node);
            guiPackage.getMainFrame().getTree().setSelectionPath(new TreePath(node.getPath()));
        } catch (Exception err) {
            log.error("Exception while adding a component to tree.", err); // $NON-NLS-1$
            String msg = err.getMessage();
            if (msg == null) {
                msg = err.toString();
            }
            JMeterUtils.reportErrorToUser(msg);
        }
    }
}
