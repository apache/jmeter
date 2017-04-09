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

import javax.swing.JSplitPane;
import javax.swing.UIManager;

import org.apache.jmeter.gui.GuiPackage;

/**
 * Hide / unhide LoggerPanel.
 *
 */
public class LoggerPanelEnableDisable extends AbstractAction {

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.LOGGER_PANEL_ENABLE_DISABLE);
    }

    /**
     * Constructor for object.
     */
    public LoggerPanelEnableDisable() {
    }

    /**
     * Gets the ActionNames attribute of the action
     *
     * @return the ActionNames value
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    /**
     * This method performs the actual command processing.
     *
     * @param e the generic UI action event
     */
    @Override
    public void doAction(ActionEvent e) {
        if (ActionNames.LOGGER_PANEL_ENABLE_DISABLE.equals(e.getActionCommand())) {
            GuiPackage guiInstance = GuiPackage.getInstance();
            JSplitPane splitPane = (JSplitPane) guiInstance.getLoggerPanel().getParent();
            if (!guiInstance.getLoggerPanel().isVisible()) {
                splitPane.setDividerSize(UIManager.getInt("SplitPane.dividerSize"));
                guiInstance.getLoggerPanel().setVisible(true);
                splitPane.setDividerLocation(0.8);
                guiInstance.getMenuItemLoggerPanel().getModel().setSelected(true);
            } else {
                guiInstance.getLoggerPanel().clear();
                guiInstance.getLoggerPanel().setVisible(false);
                splitPane.setDividerSize(0);
                guiInstance.getMenuItemLoggerPanel().getModel().setSelected(false);
            }
        }
    }
}
