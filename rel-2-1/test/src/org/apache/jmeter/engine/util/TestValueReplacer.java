/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

package org.apache.jmeter.engine.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

/**
 * @author Michael Stover
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Revision: 325648 $ updated on $Date: 2005-08-18 21:38:49 +0100 (Thu, 18 Aug 2005) $
 */
public class TestValueReplacer extends TestCase {
		TestPlan variables;

		public TestValueReplacer(String name) {
			super(name);
		}

		public void setUp() {
			variables = new TestPlan();
			variables.addParameter("server", "jakarta.apache.org");
			variables.addParameter("username", "jack");
			variables.addParameter("password", "jacks_password");
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
			List args = new ArrayList();
			args.add("username is jack");
			args.add("jacks_password");
			element.setProperty(new CollectionProperty("args", args));
			replacer.reverseReplace(element);
			assertEquals("${server}", element.getPropertyAsString("domain"));
			args = (List) element.getProperty("args").getObjectValue();
			assertEquals("${password}", ((JMeterProperty) args.get(1)).getStringValue());
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see junit.framework.TestCase#tearDown()
		 */
		protected void tearDown() throws Exception {
			JMeterContextService.getContext().setSamplingStarted(false);
		}
}
