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
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Copy extends AbstractAction {
	
	private static JMeterTreeNode copiedNode = null;
	
	private static String COPY = "Copy";
	private static HashSet commands = new HashSet();
	static {
		commands.add(COPY);
	}
	
	/*
	 * @see org.apache.jmeter.gui.action.Command#getActionNames()
	 */
	public Set getActionNames() {
		return commands;
	}

	public void doAction(ActionEvent e) 
	{
		String action = e.getActionCommand();
		JMeterTreeNode draggedNode = GuiPackage.getInstance().getTreeListener().getDraggedNode();
		JMeterTreeListener treeListener = GuiPackage.getInstance().getTreeListener();
		setCopiedNode( (JMeterTreeNode)treeListener.getCurrentNode() );
	}
	
	public static JMeterTreeNode getCopiedNode() 
	{
		return cloneTreeNode(copiedNode);
	}

	public static void setCopiedNode(JMeterTreeNode node) {
		copiedNode = cloneTreeNode(node);			
	}
	
	public static JMeterTreeNode cloneTreeNode(JMeterTreeNode node)
	{
		JMeterTreeNode treeNode = (JMeterTreeNode)node.clone();
		treeNode.setUserObject(((TestElement)node.getUserObject()).clone());
		cloneChildren(treeNode,node);
		return treeNode;
	}

	private static void cloneChildren(JMeterTreeNode to, JMeterTreeNode from) 
	{
		Enumeration enum = from.children();
		while (enum.hasMoreElements()) 
		{
			JMeterTreeNode child = (JMeterTreeNode)enum.nextElement();
			JMeterTreeNode childClone = (JMeterTreeNode)child.clone();
			childClone.setUserObject(((TestElement)child.getUserObject()).clone());
			to.add(childClone);	
			cloneChildren((JMeterTreeNode)to.getLastChild(),child);
		}
	}

}
