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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.Searchable;
import org.apache.jmeter.gui.tree.JMeterTreeNode;

/**
 * Reset Search
 */
public class ResetSearchCommand extends AbstractAction {

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.SEARCH_RESET);
    }

    /**
     * @see Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        GuiPackage guiPackage = GuiPackage.getInstance();
        try {
            guiPackage.beginUndoTransaction();
            guiPackage.getTreeModel()
                    .getNodesOfType(Searchable.class).stream()
                    .filter(node -> node.getUserObject() instanceof Searchable)
                    .map(JMeterTreeNode::getPathToThreadGroup)
                    .flatMap(Collection::stream)
                    .forEach(matchingNode ->  matchingNode.setMarkedBySearch(false));
        } finally {
            guiPackage.endUndoTransaction();
        }
        GuiPackage.getInstance().getMainFrame().repaint();
    }


    /**
     * @see Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
