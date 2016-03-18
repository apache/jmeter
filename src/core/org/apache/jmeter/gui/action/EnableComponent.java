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
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Implements the Enable menu item.
 */
public class EnableComponent implements Command {
    private static final Logger LOGGER = LoggingManager.getLoggerForClass();

    private static final Set<String> COMMANDS = new HashSet<>();

    static {
        COMMANDS.add(ActionNames.ENABLE);
        COMMANDS.add(ActionNames.DISABLE);
        COMMANDS.add(ActionNames.TOGGLE);
    }

    /**
     * @see org.apache.jmeter.gui.action.Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        JMeterTreeNode[] nodes = GuiPackage.getInstance().getTreeListener().getSelectedNodes();

        if (e.getActionCommand().equals(ActionNames.ENABLE)) {
            LOGGER.debug("enabling currently selected gui objects");
            enableComponents(nodes, true);
        } else if (e.getActionCommand().equals(ActionNames.DISABLE)) {
            LOGGER.debug("disabling currently selected gui objects");
            enableComponents(nodes, false);
        } else if (e.getActionCommand().equals(ActionNames.TOGGLE)) {
            LOGGER.debug("toggling currently selected gui objects");
            toggleComponents(nodes);
        }
    }

    private void enableComponents(JMeterTreeNode[] nodes, boolean enable) {
        GuiPackage pack = GuiPackage.getInstance();
        for (JMeterTreeNode node : nodes) {
            node.setEnabled(enable);
            pack.getGui(node.getTestElement()).setEnabled(enable);
        }
    }

    private void toggleComponents(JMeterTreeNode[] nodes) {
        GuiPackage pack = GuiPackage.getInstance();
        for (JMeterTreeNode node : nodes) {
            boolean enable = !node.isEnabled();
            node.setEnabled(enable);
            pack.getGui(node.getTestElement()).setEnabled(enable);
        }
    }

    /**
     * @see org.apache.jmeter.gui.action.Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return COMMANDS;
    }
}
