package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class EnableComponent implements Command {
	
	public static final String ENABLE = "enable";
	public static final String DISABLE = "disable";
	
	private static Set commands = new HashSet();
	static
	{
		commands.add(ENABLE);
		commands.add(DISABLE);
	}

	/**
	 * @see org.apache.jmeter.gui.action.Command#doAction(ActionEvent)
	 */
	public void doAction(ActionEvent e) {
		if(e.getActionCommand().equals(ENABLE))
		{
			GuiPackage.getInstance().getTreeListener().getCurrentNode().setEnabled(true);
		}
		else if(e.getActionCommand().equals(DISABLE))
		{
			GuiPackage.getInstance().getTreeListener().getCurrentNode().setEnabled(false);
		}
	}

	/**
	 * @see org.apache.jmeter.gui.action.Command#getActionNames()
	 */
	public Set getActionNames() {
		return commands;
	}

}
