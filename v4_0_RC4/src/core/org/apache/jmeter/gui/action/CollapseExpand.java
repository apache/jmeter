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

import javax.swing.JTree;

import org.apache.jmeter.gui.GuiPackage;

/**
 * Processes the Collapse All and Expand All options.
 *
 */
public class CollapseExpand extends AbstractAction {

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.COLLAPSE_ALL);
        commands.add(ActionNames.EXPAND_ALL);
    }

    /**
     * Constructor for the CollapseExpand object.
     */
    public CollapseExpand() {
    }

    /**
     * Gets the ActionNames attribute of the CollapseExpand object.
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
     * @param e
     *            the generic UI action event
     */
    @Override
    public void doAction(ActionEvent e) {
        boolean collapse=ActionNames.COLLAPSE_ALL.equals(e.getActionCommand());
        GuiPackage guiInstance = GuiPackage.getInstance();
        JTree jTree = guiInstance.getMainFrame().getTree();
        if (collapse) {
            for (int i = jTree.getRowCount() - 1; i >= 0; i--) {
                jTree.collapseRow(i);
            }
            return;
        }
        for(int i = 0; i < jTree.getRowCount(); i++) {
            jTree.expandRow(i);
        }
    }
}
