package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeNode;


/**
 * @author Thad Smith
 *
 * Places a copied JMeterTreeNode under the selected node.
 */
public class Paste extends AbstractAction {

	public final static String PASTE = "Paste";	
	private static Set commands = new HashSet();
	static {
		commands.add(PASTE);
	}
	
	/**
	 * @see Command#getActionNames()
	 */
	public Set getActionNames() {
		return commands;
	}
	
	/**
	 * @see Command#doAction(ActionEvent)
	 */
	public void doAction(ActionEvent e) {
		JMeterTreeNode draggedNode = Copy.getCopiedNode();
		if ( draggedNode != null ) {
			JMeterTreeListener treeListener = GuiPackage.getInstance().getTreeListener();
			JMeterTreeNode currentNode = treeListener.getCurrentNode();
			GuiPackage.getInstance().getTreeModel().insertNodeInto(draggedNode,
				currentNode,currentNode.getChildCount());
		}
		GuiPackage.getInstance().getMainFrame().repaint();
	}

}
