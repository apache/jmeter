package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.functions.gui.FunctionHelper;

/**
 * @version $Revision$
 */
public class CreateFunctionDialog extends AbstractAction
{
    private FunctionHelper helper = null;

    private static Set commands;
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
    public Set getActionNames()
    {
        return commands;
    }

    public void doAction(ActionEvent arg0)
    {
        helper.show();
    }
}
