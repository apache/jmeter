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

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class AddToTree implements Command {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final Set<String> commandSet;

    static {
        HashSet<String> commands = new HashSet<String>();
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
            guiPackage.getMainFrame().getTree().setSelectionPath(new TreePath(node.getPath()));
        }
        catch (IllegalUserActionException err) {
            log.error("", err); // $NON-NLS-1$
            String msg = err.getMessage();
            if (msg == null) {
                msg=err.toString();
            }
            JMeterUtils.reportErrorToUser(msg);
        }
        catch (Exception err) {
            log.error("", err); // $NON-NLS-1$
            String msg = err.getMessage();
            if (msg == null) {
                msg=err.toString();
            }
            JMeterUtils.reportErrorToUser(msg);
        }
    }
}
