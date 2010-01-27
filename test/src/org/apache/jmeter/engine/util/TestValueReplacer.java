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

package org.apache.jmeter.engine.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

public class TestValueReplacer extends JMeterTestCase {
        private TestPlan variables;

        public TestValueReplacer(String name) {
            super(name);
        }

        /** {@inheritDoc} */
        @Override
        public void setUp() {
            variables = new TestPlan();
            variables.addParameter("server", "jakarta.apache.org");
            variables.addParameter("username", "jack");
            // The following used to be jacks_password, but the Arguments class uses
            // HashMap for which the order is not defined.
            variables.addParameter("password", "his_password");
            variables.addParameter("regex", ".*");
            JMeterVariables vars = new JMeterVariables();
            vars.put("server", "jakarta.apache.org");
            JMeterContextService.getContext().setVariables(vars);
            JMeterContextService.getContext().setSamplingStarted(true);
        }

        public void testReverseReplacement() throws Exception {
            ValueReplacer replacer = new ValueReplacer(variables);
            assertTrue(variables.getUserDefinedVariables().containsKey("server"));
            assertTrue(replacer.containsKey("server"));
            TestElement element = new TestPlan();
            element.setProperty(new StringProperty("domain", "jakarta.apache.org"));
            List<Object> argsin = new ArrayList<Object>();
            argsin.add("username is jack");
            argsin.add("his_password");
            element.setProperty(new CollectionProperty("args", argsin));
            replacer.reverseReplace(element);
            assertEquals("${server}", element.getPropertyAsString("domain"));
            @SuppressWarnings("unchecked")
            List<JMeterProperty> args = (List<JMeterProperty>) element.getProperty("args").getObjectValue();
            assertEquals("username is ${username}", args.get(0).getStringValue());
            assertEquals("${password}", args.get(1).getStringValue());
        }

        public void testReplace() throws Exception {
            ValueReplacer replacer = new ValueReplacer();
            replacer.setUserDefinedVariables(variables.getUserDefinedVariables());
            TestElement element = new ConfigTestElement();
            element.setProperty(new StringProperty("domain", "${server}"));
            replacer.replaceValues(element);
            //log.debug("domain property = " + element.getProperty("domain"));
            element.setRunningVersion(true);
            assertEquals("jakarta.apache.org", element.getPropertyAsString("domain"));
        }

        /** {@inheritDoc} */
        @Override
        protected void tearDown() throws Exception {
            JMeterContextService.getContext().setSamplingStarted(false);
        }
}
