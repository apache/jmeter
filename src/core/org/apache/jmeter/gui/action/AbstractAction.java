package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.jmeter.control.ReplaceableController;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author mstover
 * @version $Revision$
 */
public abstract class AbstractAction implements Command
{
    protected static Logger log = LoggingManager.getLoggerForClass();

    /**
     * @see Command#doAction(ActionEvent)
     */
    public void doAction(ActionEvent e)
    {
    }

    /**
     * @see Command#getActionNames()
     */
    abstract public Set getActionNames();

    protected void convertSubTree(HashTree tree)
    {
        Iterator iter = new LinkedList(tree.list()).iterator();
        while (iter.hasNext())
        {
            JMeterTreeNode item = (JMeterTreeNode) iter.next();
            if (item.isEnabled())
            {
                if (item.getUserObject() instanceof ReplaceableController)
                {
                    ReplaceableController rc =
                        (ReplaceableController) item.createTestElement();
                    HashTree subTree = tree.getTree(item);

                    if (subTree != null)
                    {
                        rc.replace(subTree);
                        convertSubTree(subTree);
                        tree.replace(item, rc.getReplacement());
                    }
                }
                else
                {
                    convertSubTree(tree.getTree(item));
                    TestElement testElement = item.createTestElement();
                    tree.replace(item, testElement);
                }
            }
            else
            {
                tree.remove(item);
            }

        }
    }
}
