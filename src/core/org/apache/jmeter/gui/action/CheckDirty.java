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
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author mstover
 * @version $Revision$
 */
public class CheckDirty
    extends AbstractAction
    implements HashTreeTraverser, ActionListener
{
	private static final Logger log = LoggingManager.getLoggerForClass();

    private static Map previousGuiItems;
    public static final String CHECK_DIRTY = "check_dirty";
    public static final String SUB_TREE_SAVED = "sub_tree_saved";
    public static final String SUB_TREE_LOADED = "sub_tree_loaded";
    public static final String ADD_ALL = "add_all";
    //Not implemented: public static final String SAVE = "save_as";
    //Not implemented: public static final String SAVE_ALL = "save_all";
    //Not implemented: public static final String SAVE_TO_PREVIOUS = "save";
    public static final String REMOVE = "check_remove";

    boolean checkMode = false;
    boolean removeMode = false;
    boolean dirty = false;

    private static Set commands = new HashSet();
    static
    {
        commands.add(CHECK_DIRTY);
        commands.add(SUB_TREE_SAVED);
        commands.add(SUB_TREE_LOADED);
        commands.add(ADD_ALL);
        commands.add(REMOVE);
    }

    public CheckDirty()
    {
        previousGuiItems = new HashMap();
        ActionRouter.getInstance().addPreActionListener(
            ExitCommand.class,
            this);
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals(ExitCommand.EXIT))
        {
            doAction(e);
        }
    }

    /**
     * @see Command#doAction(ActionEvent)
     */
    public void doAction(ActionEvent e)
    {
        String action = e.getActionCommand();
        if (action.equals(SUB_TREE_SAVED))
        {
            HashTree subTree = (HashTree) e.getSource();
            subTree.traverse(this);
        }
        else if (action.equals(SUB_TREE_LOADED))
        {
            ListedHashTree addTree = (ListedHashTree) e.getSource();
            addTree.traverse(this);
        }
        else if (action.equals(ADD_ALL))
        {
            previousGuiItems.clear();
            GuiPackage.getInstance().getTreeModel().getTestPlan().traverse(
                this);
        }
        else if (action.equals(REMOVE))
        {
            GuiPackage guiPackage = GuiPackage.getInstance();
            JMeterTreeNode[] nodes =
                guiPackage.getTreeListener().getSelectedNodes();
            removeMode = true;
            for (int i = nodes.length - 1; i >= 0; i--)
            {
                guiPackage.getTreeModel().getCurrentSubTree(nodes[i]).traverse(
                    this);
            }
            removeMode = false;
        }
        checkMode = true;
        dirty = false;
        HashTree wholeTree =
            GuiPackage.getInstance().getTreeModel().getTestPlan();
        wholeTree.traverse(this);
        GuiPackage.getInstance().setDirty(dirty);
        checkMode = false;
    }

    /**
     * The tree traverses itself depth-first, calling processNode for each
     * object it encounters as it goes.
     */
    public void addNode(Object node, HashTree subTree)
    {
        log.debug("Node is class:" + node.getClass());
        JMeterTreeNode treeNode = (JMeterTreeNode) node;
        if (checkMode)
        {
            if (previousGuiItems.containsKey(treeNode))
            {
                if (!previousGuiItems
                    .get(treeNode)
                    .equals(treeNode.getTestElement()))
                {
                    dirty = true;
                }
            }
            else
            {
                dirty = true;
            }
        }
        else if (removeMode)
        {
            previousGuiItems.remove(treeNode);
        }
        else
        {
            previousGuiItems.put(
                treeNode,
                treeNode.getTestElement().clone());
        }
    }

    /**
     * Indicates traversal has moved up a step, and the visitor should remove
     * the top node from it's stack structure.
     */
    public void subtractNode()
    {
    }

    /**
     * Process path is called when a leaf is reached.  If a visitor wishes to
     * generate Lists of path elements to each leaf, it should keep a Stack
     * data structure of nodes passed to it with addNode, and removing top
     * items for every subtractNode() call.
     */
    public void processPath()
    {
    }

    /**
     * @see Command#getActionNames()
     */
    public Set getActionNames()
    {
        return commands;
    }
}
