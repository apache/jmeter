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

package org.apache.jmeter.engine;

import java.util.LinkedList;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @version $Revision$
 */
public class TreeCloner implements HashTreeTraverser
{
    Logger log = LoggingManager.getLoggerForClass();

    ListedHashTree newTree;

    LinkedList objects = new LinkedList();

    boolean forThread = true;

    public TreeCloner()
    {
        this(true);
    }

    public TreeCloner(boolean forThread)
    {
        newTree = new ListedHashTree();
        this.forThread = forThread;
    }

    public void addNode(Object node, HashTree subTree)
    {
        if((!forThread || !(node instanceof NoThreadClone)) && (node instanceof TestElement))
        {
            node = ((TestElement)node).clone();
            newTree.add(objects, node);
        }
        else
        {
            newTree.add(objects, node);
        }
        objects.addLast(node);
    }

    public void subtractNode()
    {
        objects.removeLast();
    }

    public ListedHashTree getClonedTree()
    {
        return newTree;
    }

    public void processPath()
    {}

    public static class Test extends junit.framework.TestCase
    {
        public Test(String name)
        {
            super(name);
        }

        public void testCloning() throws Exception
        {
            ListedHashTree original = new ListedHashTree();
            GenericController controller = new GenericController();
            controller.setName("controller");
            Arguments args = new Arguments();
            args.setName("args");
            TestPlan plan = new TestPlan();
            plan.addParameter("server", "jakarta");
            original.add(controller, args);
            original.add(plan);
            ResultCollector listener = new ResultCollector();
            listener.setName("Collector");
            original.add(controller, listener);
            TreeCloner cloner = new TreeCloner();
            original.traverse(cloner);
            ListedHashTree newTree = cloner.getClonedTree();
            assertTrue(original != newTree);
            assertEquals(original.size(), newTree.size());
            assertEquals(original.getTree(original.getArray()[0]).size(), newTree.getTree(newTree.getArray()[0]).size());
            assertTrue(original.getArray()[0] != newTree.getArray()[0]);
            assertEquals(((GenericController)original.getArray()[0]).getName(),
                    ((GenericController)newTree.getArray()[0]).getName());
            assertSame(original.getTree(original.getArray()[0]).getArray()[1], newTree.getTree(newTree.getArray()[0])
                    .getArray()[1]);
            TestPlan clonedTestPlan = (TestPlan)newTree.getArray()[1];
            clonedTestPlan.setRunningVersion(true);
            clonedTestPlan.recoverRunningVersion();
            assertTrue(!plan.getProperty(TestPlan.USER_DEFINED_VARIABLES).isRunningVersion());
            assertTrue(clonedTestPlan.getProperty(TestPlan.USER_DEFINED_VARIABLES).isRunningVersion());
            Arguments vars = (Arguments)plan.getProperty(TestPlan.USER_DEFINED_VARIABLES).getObjectValue();
            PropertyIterator iter = ((CollectionProperty)vars.getProperty(Arguments.ARGUMENTS)).iterator();
            while(iter.hasNext())
            {
                JMeterProperty argProp = iter.next();
                assertTrue(!argProp.isRunningVersion());
                assertTrue(argProp.getObjectValue() instanceof Argument);
                Argument arg = (Argument)argProp.getObjectValue();
                arg.setValue("yahoo");
                assertEquals("yahoo", arg.getValue());
            }
            vars = (Arguments)clonedTestPlan.getProperty(TestPlan.USER_DEFINED_VARIABLES).getObjectValue();
            iter = vars.propertyIterator();
            while(iter.hasNext())
            {
                assertTrue(iter.next().isRunningVersion());
            }
        }

    }
}