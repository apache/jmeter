package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeNode;

/**
 * @author Thad Smith
 * @version $Revision$
 */
public class Cut extends AbstractAction
{
    public final static String CUT = "Cut";
    private static Set commands = new HashSet();
    static {
        commands.add(CUT);
    }

    /**
     * @see Command#getActionNames()
     */
    public Set getActionNames()
    {
        return commands;
    }

    /**
     * @see Command#doAction(ActionEvent)
     */
    public void doAction(ActionEvent e)
    {
        Copy.setCopiedNode(
            GuiPackage.getInstance().getTreeListener().getDraggedNode());
        JMeterTreeListener treeListener =
            GuiPackage.getInstance().getTreeListener();
        JMeterTreeNode currentNode = treeListener.getCurrentNode();
        GuiPackage.getInstance().getTreeModel().removeNodeFromParent(
            GuiPackage.getInstance().getTreeListener().getDraggedNode());
        GuiPackage.getInstance().getMainFrame().repaint();
    }
}
