package org.apache.jmeter.gui.action;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class AddParent implements Command
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.gui");
	private static Set commands = new HashSet();
	static
	{
		commands.add("Add Parent");
	}

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public AddParent() { }

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param e  !ToDo (Parameter description)
	 ***************************************/
	public void doAction(ActionEvent e)
	{
		String name = ((Component)e.getSource()).getName();
		try
		{
			JMeterGUIComponent controller = (JMeterGUIComponent)Class.forName(name).newInstance();
			addParentToTree(controller);
		}
		catch(Exception err)
		{
			log.error("",err);
		}

	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Set getActionNames()
	{
		return commands;
	}

	/****************************************
	 * !ToDo
	 *
	 *@param newParent  !ToDo
	 ***************************************/
	protected void addParentToTree(JMeterGUIComponent newParent)
	{
		GuiPackage guiPackage = GuiPackage.getInstance();
		JMeterTreeNode newNode = new JMeterTreeNode(newParent);
		JMeterTreeNode currentNode = guiPackage.getTreeListener().getCurrentNode();
		JMeterTreeNode parentNode = (JMeterTreeNode)currentNode.getParent();
		int index = parentNode.getIndex(currentNode);
		guiPackage.getTreeModel().removeNodeFromParent(currentNode);
		guiPackage.getTreeModel().insertNodeInto(newNode,
				(JMeterTreeNode)parentNode, index);
		guiPackage.getTreeModel().insertNodeInto(currentNode, newNode,
				newNode.getChildCount());
	}
}
