package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.ListedHashTree;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public abstract class AbstractAction implements Command {

	/**
	 * @see Command#doAction(ActionEvent)
	 */
	public void doAction(ActionEvent e) {
	}

	/**
	 * @see Command#getActionNames()
	 */
	abstract public Set getActionNames();

	protected void convertSubTree(ListedHashTree tree)
	{
		Iterator iter = new LinkedList(tree.list()).iterator();
		while (iter.hasNext())
		{
			JMeterGUIComponent item = (JMeterGUIComponent)iter.next();
			convertSubTree(tree.get(item));
			TestElement testElement = item.createTestElement();
			tree.replace(item,testElement);
			
		}
	}

	



}
