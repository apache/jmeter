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

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Clearable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the following actions:
 * - Clear (Data)
 * - Clear All (Data)
 * - Reset (Clear GUI)
 */
public class Clear extends AbstractAction {
    private static final Logger log = LoggerFactory.getLogger(Clear.class);

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.CLEAR);
        commands.add(ActionNames.CLEAR_ALL);
    }

    public Clear() {
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public void doAction(ActionEvent e) {
        GuiPackage guiPackage = GuiPackage.getInstance();
        final String actionCommand = e.getActionCommand();
        if (actionCommand.equals(ActionNames.CLEAR)) {
            JMeterGUIComponent guiComp = guiPackage.getCurrentGui();
            if (guiComp instanceof Clearable){
                ((Clearable) guiComp).clearData();
            }
        } else {
            guiPackage.getMainFrame().clearData();
            for (JMeterTreeNode node : guiPackage.getTreeModel().getNodesOfType(Clearable.class)) {
                JMeterGUIComponent guiComp = guiPackage.getGui(node.getTestElement());
                if (guiComp instanceof Clearable){
                    Clearable item = (Clearable) guiComp;
                    try {
                        item.clearData();
                    } catch (Exception ex) {
                        log.error("Can't clear: {} {}", node, guiComp, ex);
                    }
                }
            }
        }
    }
}
