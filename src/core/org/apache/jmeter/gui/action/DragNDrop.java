package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeNode;


/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class DragNDrop extends AbstractAction {

	public final static String ADD = "drag_n_drop.add";
	public final static String INSERT_BEFORE = "drag_n_drop.insert_before";
	public final static String INSERT_AFTER = "drag_n_drop.insert_after";
	
	private static Set commands = new HashSet();
	static
	{
		commands.add(ADD);
		commands.add(INSERT_BEFORE);
		commands.add(INSERT_AFTER);
	}
	/**
	 * @see Command#doAction(ActionEvent)
	 */
	public void doAction(ActionEvent e) {
		String action = e.getActionCommand();
		JMeterTreeNode draggedNode = GuiPackage.getInstance().getTreeListener().getDraggedNode();
		JMeterTreeListener treeListener = GuiPackage.getInstance().getTreeListener();
		JMeterTreeNode currentNode = treeListener.getCurrentNode();
		GuiPackage.getInstance().getTreeModel().removeNodeFromParent(
				GuiPackage.getInstance().getTreeListener().getDraggedNode());
		if(ADD.equals(action))
		{
			GuiPackage.getInstance().getTreeModel().insertNodeInto(draggedNode,
				currentNode,currentNode.getChildCount());
		}
		else if(INSERT_BEFORE.equals(action))
		{
			JMeterTreeNode parentNode = (JMeterTreeNode)currentNode.getParent();
			int index = parentNode.getIndex(currentNode);
			GuiPackage.getInstance().getTreeModel().insertNodeInto(draggedNode,
					parentNode, index);
		}
		else if(INSERT_AFTER.equals(action))
		{
			JMeterTreeNode parentNode = (JMeterTreeNode)currentNode.getParent();
			int index = parentNode.getIndex(currentNode)+1;
			GuiPackage.getInstance().getTreeModel().insertNodeInto(draggedNode,
					parentNode, index);
		}
		GuiPackage.getInstance().getMainFrame().repaint();
	}

	/**
	 * @see Command#getActionNames()
	 */
	public Set getActionNames() {
		return commands;
	}

}
