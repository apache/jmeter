// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/

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
    private static final Logger log = LoggingManager.getLoggerForClass();

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
                        (ReplaceableController) item.getTestElement();
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
                    TestElement testElement = item.getTestElement();
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
