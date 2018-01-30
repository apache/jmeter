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

/*
 * Created on Apr 9, 2003
 *
 * Clones a JMeterTreeNode
 */
package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;

/**
 * Implements the Duplicate menu command
 */
public class Duplicate extends AbstractAction {

    private static final HashSet<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.DUPLICATE);
    }

    /*
     * @see org.apache.jmeter.gui.action.Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public void doAction(ActionEvent e) {
        GuiPackage instance = GuiPackage.getInstance();
        JMeterTreeListener treeListener = instance.getTreeListener();
        JMeterTreeNode[] copiedNodes = Copy.cloneTreeNodes(treeListener.getSelectedNodes());
        JMeterTreeNode currentNode = treeListener.getCurrentNode();
        JMeterTreeNode parentNode = (JMeterTreeNode) currentNode.getParent();
        JMeterTreeModel treeModel = instance.getTreeModel();
        for (JMeterTreeNode copiedNode : copiedNodes) {
            int index = parentNode.getIndex(currentNode) + 1;
            treeModel.insertNodeInto(copiedNode, parentNode, index);
        }
        instance.getMainFrame().repaint();
    }
}
