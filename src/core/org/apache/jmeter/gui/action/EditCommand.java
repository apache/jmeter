package org.apache.jmeter.gui.action;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.NamePanel;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class EditCommand implements Command
{

	private static Set commands = new HashSet();
	static
	{
		commands.add("edit");
	}

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public EditCommand() { }

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param e  !ToDo (Parameter description)
	 ***************************************/
	public void doAction(ActionEvent e)
	{
		GuiPackage guiPackage = GuiPackage.getInstance();
		guiPackage.getMainFrame().setMainPanel((javax.swing.JComponent)
					guiPackage.getCurrentGui());
		guiPackage.getMainFrame().setEditMenu(
				((JMeterGUIComponent)guiPackage.getTreeListener().getCurrentNode()).createPopupMenu());
		if(!(guiPackage.getCurrentGui() instanceof NamePanel))
		{
			guiPackage.getMainFrame().setFileLoadEnabled(true);
			guiPackage.getMainFrame().setFileSaveEnabled(true);
		}
		else
		{
			guiPackage.getMainFrame().setFileLoadEnabled(false);
			guiPackage.getMainFrame().setFileSaveEnabled(false);
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
}
