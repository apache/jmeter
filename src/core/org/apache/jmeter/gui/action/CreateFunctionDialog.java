package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.functions.gui.FunctionHelper;
import org.apache.jmeter.gui.GuiPackage;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class CreateFunctionDialog extends AbstractAction {
	
	private static Set commands;
	private FunctionHelper helper = null;

    static {
        commands = new HashSet();
        commands.add("functions");
    }
    
    public CreateFunctionDialog()
    {
			helper = new FunctionHelper();
    }
    
    /**
     * Provide the list of Action names that are available in this command.
     */
    public Set getActionNames() {
        return commands;
    }

	/**
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void doAction(ActionEvent arg0) 
	{   
		helper.show();
	}

}
