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
            variables.addParameter("normal_regex", "Hello .*");
            variables.addParameter("bounded_regex", "(<.*>)");
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

        public void testReverseReplacementXml() throws Exception {
            ValueReplacer replacer = new ValueReplacer(variables);
            assertTrue(variables.getUserDefinedVariables().containsKey("bounded_regex"));
            assertTrue(variables.getUserDefinedVariables().containsKey("normal_regex"));
            assertTrue(replacer.containsKey("bounded_regex"));
            assertTrue(replacer.containsKey("normal_regex"));
            TestElement element = new TestPlan();
            element.setProperty(new StringProperty("domain", "<this><is>xml</this></is>"));
            List<Object> argsin = new ArrayList<Object>();
            argsin.add("<this><is>xml</this></is>");
            argsin.add("And I say: Hello World.");
            element.setProperty(new CollectionProperty("args", argsin));
            replacer.reverseReplace(element, true);
            @SuppressWarnings("unchecked")
            List<JMeterProperty> args = (List<JMeterProperty>) element.getProperty("args").getObjectValue();
            assertEquals("${bounded_regex}", element.getPropertyAsString("domain"));
            assertEquals("${bounded_regex}", args.get(0).getStringValue());
        }

        public void testOverlappingMatches() throws Exception {
            TestPlan plan = new TestPlan();
            plan.addParameter("longMatch", "servername");
            plan.addParameter("shortMatch", ".*");
            ValueReplacer replacer = new ValueReplacer(plan);
            TestElement element = new TestPlan();
            element.setProperty(new StringProperty("domain", "servername.domain"));
            replacer.reverseReplace(element, true);
            String replacedDomain = element.getPropertyAsString("domain");
            assertEquals("${${shortMatch}", replacedDomain);
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

        public void testReplaceStringWithBackslash() throws Exception {
            ValueReplacer replacer = new ValueReplacer();
            replacer.setUserDefinedVariables(variables.getUserDefinedVariables());
            TestElement element = new ConfigTestElement();
            String input = "\\${server} \\ \\\\ \\\\\\ \\, ";
            element.setProperty(new StringProperty("domain", input));
            replacer.replaceValues(element);
            //log.debug("domain property = " + element.getProperty("domain"));
            element.setRunningVersion(true);
            assertEquals(input, element.getPropertyAsString("domain"));
        }

        /*
         * This test should be compared with the one above.
         * Here, the string contains a valid variable reference, so all
         * backslashes are also processed.
         * 
         * See https://issues.apache.org/bugzilla/show_bug.cgi?id=53534
         */
        public void testReplaceFunctionWithBackslash() throws Exception {
            ValueReplacer replacer = new ValueReplacer();
            replacer.setUserDefinedVariables(variables.getUserDefinedVariables());
            TestElement element = new ConfigTestElement();
            String input = "${server} \\ \\\\ \\\\\\ \\, ";
            element.setProperty(new StringProperty("domain", input));
            replacer.replaceValues(element);
            //log.debug("domain property = " + element.getProperty("domain"));
            element.setRunningVersion(true);
            assertEquals("jakarta.apache.org \\ \\ \\\\ , ", element.getPropertyAsString("domain"));
        }

        /** {@inheritDoc} */
        @Override
        protected void tearDown() throws Exception {
            JMeterContextService.getContext().setSamplingStarted(false);
        }
}
