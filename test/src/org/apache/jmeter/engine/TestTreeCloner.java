/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.collections.ListedHashTree;
import org.junit.Test;

public class TestTreeCloner extends JMeterTestCase {
        
        @Test
        public void testCloning() throws Exception {
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
            assertEquals(((GenericController) original.getArray()[0]).getName(), ((GenericController) newTree
                    .getArray()[0]).getName());
            assertSame(original.getTree(original.getArray()[0]).getArray()[1], newTree.getTree(newTree.getArray()[0])
                    .getArray()[1]);
            TestPlan clonedTestPlan = (TestPlan) newTree.getArray()[1];
            clonedTestPlan.setRunningVersion(true);
            clonedTestPlan.recoverRunningVersion();
            assertTrue(!plan.getUserDefinedVariablesAsProperty().isRunningVersion());
            assertTrue(clonedTestPlan.getUserDefinedVariablesAsProperty().isRunningVersion());
            Arguments vars = (Arguments) plan.getUserDefinedVariablesAsProperty().getObjectValue();
            PropertyIterator iter = ((CollectionProperty) vars.getProperty(Arguments.ARGUMENTS)).iterator();
            while (iter.hasNext()) {
                JMeterProperty argProp = iter.next();
                assertTrue(!argProp.isRunningVersion());
                assertTrue(argProp.getObjectValue() instanceof Argument);
                Argument arg = (Argument) argProp.getObjectValue();
                arg.setValue("yahoo");
                assertEquals("yahoo", arg.getValue());
            }
            vars = (Arguments) clonedTestPlan.getUserDefinedVariablesAsProperty().getObjectValue();
            iter = vars.propertyIterator();
            while (iter.hasNext()) {
                assertTrue(iter.next().isRunningVersion());
            }
        }
}
