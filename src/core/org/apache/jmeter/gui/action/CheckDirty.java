package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class CheckDirty extends AbstractAction implements 
		HashTreeTraverser 
{
	private Map previousGuiItems;
	public static final String CHECK_DIRTY = "check_dirty";
	public static final String SUB_TREE_SAVED = "sub_tree_saved";
	public static final String SUB_TREE_LOADED = "sub_tree_loaded";
	public static final String ADD_ALL = "add_all";
	public static final String SAVE = "save_as";
	public static final String SAVE_ALL = "save_all";
	public static final String SAVE_TO_PREVIOUS = "save";
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
		commands.add(SAVE);
		commands.add(SAVE_ALL);
		commands.add(REMOVE);
		commands.add(SAVE_TO_PREVIOUS);
	}
	
	public CheckDirty()
	{
		previousGuiItems = new HashMap();
	}
		

	/**
	 * @see Command#doAction(ActionEvent)
	 */
	public void doAction(ActionEvent e) {
		String action = e.getActionCommand();
		if(action.equals(SUB_TREE_SAVED) || action.equals(SAVE))
		{
			HashTree subTree = GuiPackage.getInstance().getCurrentSubTree();
			subTree.traverse(this);
		}
		else if(action.equals(SAVE_ALL) || action.equals(SAVE_TO_PREVIOUS))
		{
			HashTree subTree = GuiPackage.getInstance().getTreeModel().getTestPlan();
			subTree.traverse(this);
		}
		else if(action.equals(SUB_TREE_LOADED))
		{
			ListedHashTree addTree = (ListedHashTree)e.getSource();
			addTree.traverse(this);
		}
		else if(action.equals(ADD_ALL))
		{
			previousGuiItems.clear();
			GuiPackage.getInstance().getTreeModel().getTestPlan().traverse(this);
		}
		else if(action.equals(REMOVE))
		{
			GuiPackage guiPackage = GuiPackage.getInstance();
			JMeterTreeNode[] nodes = guiPackage.getTreeListener().getSelectedNodes();
			removeMode = true;
			for (int i = nodes.length - 1; i >= 0; i--)
			{
				guiPackage.getTreeModel().getCurrentSubTree(nodes[i]).traverse(this);
			}
			removeMode = false;
		}
		else if(action.equals(CHECK_DIRTY))
		{
			checkMode = true;
			dirty = false;
			HashTree wholeTree = GuiPackage.getInstance().getTreeModel().getTestPlan();
			wholeTree.traverse(this);
			GuiPackage.getInstance().setDirty(dirty);
			checkMode = false;
		}		
	}
	
	/**
	 * The tree traverses itself depth-first, calling processNode for each object
	 * it encounters as it goes.
	 */
	public void addNode(Object node,HashTree subTree)
	{
		JMeterGUIComponent treeNode = (JMeterGUIComponent)node;
		if(checkMode)
		{
			if(previousGuiItems.containsKey(treeNode))
			{
				if(!previousGuiItems.get(treeNode).equals(
						treeNode.createTestElement()))
				{
					dirty = true;
				}
			}
			else
			{
				dirty = true;
			}
		}
		else if(removeMode)
		{
			previousGuiItems.remove(treeNode);
		}
		else
		{
			previousGuiItems.put(treeNode,treeNode.createTestElement());
		}
	}

	/**
	 * Indicates traversal has moved up a step, and the visitor should remove the
	 * top node from it's stack structure.
	 */
	public void subtractNode()
	{
	}

	/**
	 * Process path is called when a leaf is reached.  If a visitor wishes to generate
	 * Lists of path elements to each leaf, it should keep a Stack data structure of
	 * nodes passed to it with addNode, and removing top items for every subtractNode()
	 * call.
	 */
	public void processPath()
	{
	}

	/**
	 * @see Command#getActionNames()
	 */
	public Set getActionNames() {
		return commands;
	}

}
