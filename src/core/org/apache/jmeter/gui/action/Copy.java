// $Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

/*
 * Created on Apr 9, 2003
 *
 * Clones a JMeterTreeNode
 */
package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;

/**
 * @author Thad Smith
 * @version $Revision$
 */
public class Copy extends AbstractAction
{
    private static JMeterTreeNode copiedNode = null;

    private static String COPY = "Copy";
    private static HashSet commands = new HashSet();
    static {
        commands.add(COPY);
    }

    /*
     * @see org.apache.jmeter.gui.action.Command#getActionNames()
     */
    public Set getActionNames()
    {
        return commands;
    }

    public void doAction(ActionEvent e)
    {
        JMeterTreeListener treeListener =
            GuiPackage.getInstance().getTreeListener();
        setCopiedNode((JMeterTreeNode) treeListener.getCurrentNode());
    }

    public static JMeterTreeNode getCopiedNode()
    {
        if (copiedNode == null)
        {
            return null;
        }
        return cloneTreeNode(copiedNode);
    }

    public static void setCopiedNode(JMeterTreeNode node)
    {
        copiedNode = cloneTreeNode(node);
    }

    public static JMeterTreeNode cloneTreeNode(JMeterTreeNode node)
    {
        JMeterTreeNode treeNode = (JMeterTreeNode) node.clone();
        treeNode.setUserObject(((TestElement) node.getUserObject()).clone());
        cloneChildren(treeNode, node);
        return treeNode;
    }

    private static void cloneChildren(JMeterTreeNode to, JMeterTreeNode from)
    {
        Enumeration enum = from.children();
        while (enum.hasMoreElements())
        {
            JMeterTreeNode child = (JMeterTreeNode) enum.nextElement();
            JMeterTreeNode childClone = (JMeterTreeNode) child.clone();
            childClone.setUserObject(
                ((TestElement) child.getUserObject()).clone());
            to.add(childClone);
            cloneChildren((JMeterTreeNode) to.getLastChild(), child);
        }
    }
}
