// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeNode;

/**
 * @author mstover
 * @version $Revision$
 */
public class DragNDrop extends AbstractAction
{
    public final static String ADD = "drag_n_drop.add";
    public final static String INSERT_BEFORE = "drag_n_drop.insert_before";
    public final static String INSERT_AFTER = "drag_n_drop.insert_after";

    private static Set commands = new HashSet();
    static {
        commands.add(ADD);
        commands.add(INSERT_BEFORE);
        commands.add(INSERT_AFTER);
    }
    /**
     * @see Command#doAction(ActionEvent)
     */
    public void doAction(ActionEvent e)
    {
        String action = e.getActionCommand();
        GuiPackage guiPackage = GuiPackage.getInstance();
        JMeterTreeNode[] draggedNodes = guiPackage.getTreeListener().getDraggedNodes();
        JMeterTreeListener treeListener = guiPackage.getTreeListener();
        JMeterTreeNode currentNode = treeListener.getCurrentNode();
        removeNodesFromParents(draggedNodes);
        if (ADD.equals(action))
        {
            for (int i = 0; i < draggedNodes.length; i++)
            {
                GuiPackage.getInstance().getTreeModel().insertNodeInto(
                    draggedNodes[i],
                    currentNode,
                    currentNode.getChildCount());
            }
        }
        else if (INSERT_BEFORE.equals(action))
        {
            for (int i = 0; i < draggedNodes.length; i++)
            {
                JMeterTreeNode parentNode =
                    (JMeterTreeNode) currentNode.getParent();
                int index = parentNode.getIndex(currentNode);
                GuiPackage.getInstance().getTreeModel().insertNodeInto(
                    draggedNodes[i],
                    parentNode,
                    index);
            }
        }
        else if (INSERT_AFTER.equals(action))
        {
            for (int i = 0; i < draggedNodes.length; i++)
            {
                JMeterTreeNode parentNode =
                    (JMeterTreeNode) currentNode.getParent();
                int index = parentNode.getIndex(currentNode) + 1;
                GuiPackage.getInstance().getTreeModel().insertNodeInto(
                    draggedNodes[i],
                    parentNode,
                    index);
            }
        }
        GuiPackage.getInstance().getMainFrame().repaint();
    }
    
    protected void removeNodesFromParents(JMeterTreeNode[] nodes)
    {
        for (int i = 0; i < nodes.length; i++)
        {
            GuiPackage.getInstance().getTreeModel().removeNodeFromParent(nodes[i]);
        }        
    }

    /**
     * @see Command#getActionNames()
     */
    public Set getActionNames()
    {
        return commands;
    }
}
