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

package org.apache.jmeter.functions;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

public class TestRegexFunction extends JMeterTestCase {
		RegexFunction variable;

		SampleResult result;

		Collection params;

		private JMeterVariables vars;

		private JMeterContext jmctx = null;

		public TestRegexFunction(String name) {
			super(name);
		}

		public void setUp() {
			variable = new RegexFunction();
			result = new SampleResult();
			jmctx = JMeterContextService.getContext();
			String data = "<company-xmlext-query-ret><row>" + "<value field=\"RetCode\">" + "LIS_OK</value><value"
					+ " field=\"RetCodeExtension\"></value>" + "<value field=\"alias\"></value><value"
					+ " field=\"positioncount\"></value>" + "<value field=\"invalidpincount\">0</value><value"
					+ " field=\"pinposition1\">1</value><value" + " field=\"pinpositionvalue1\"></value><value"
					+ " field=\"pinposition2\">5</value><value" + " field=\"pinpositionvalue2\"></value><value"
					+ " field=\"pinposition3\">6</value><value" + " field=\"pinpositionvalue3\"></value>"
					+ "</row></company-xmlext-query-ret>";
			result.setResponseData(data.getBytes());
			vars = new JMeterVariables();
			jmctx.setVariables(vars);
			jmctx.setPreviousResult(result);
		}

		public void testVariableExtraction() throws Exception {
			params = new LinkedList();
			params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
			params.add(new CompoundVariable("$2$"));
			params.add(new CompoundVariable("2"));
			variable.setParameters(params);
			String match = variable.execute(result, null);
			assertEquals("5", match);
		}

		public void testVariableExtraction2() throws Exception {
			params = new LinkedList();
			params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
			params.add(new CompoundVariable("$1$"));
			params.add(new CompoundVariable("3"));
			variable.setParameters(params);
			String match = variable.execute(result, null);
			assertEquals("pinposition3", match);
		}

		public void testVariableExtraction5() throws Exception {
			params = new LinkedList();
			params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
			params.add(new CompoundVariable("$1$"));
			params.add(new CompoundVariable("ALL"));
			params.add(new CompoundVariable("_"));
			variable.setParameters(params);
			String match = variable.execute(result, null);
			assertEquals("pinposition1_pinposition2_pinposition3", match);
		}

		public void testVariableExtraction6() throws Exception {
			params = new LinkedList();
			params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
			params.add(new CompoundVariable("$2$"));
			params.add(new CompoundVariable("4"));
			params.add(new CompoundVariable(""));
			params.add(new CompoundVariable("default"));
			variable.setParameters(params);
			String match = variable.execute(result, null);
			assertEquals("default", match);
		}

		public void testComma() throws Exception {
			params = new LinkedList();
			params.add(new CompoundVariable("<value,? field=\"(pinposition\\d+)\">(\\d+)</value>"));
			params.add(new CompoundVariable("$1$"));
			params.add(new CompoundVariable("3"));
			variable.setParameters(params);
			String match = variable.execute(result, null);
			assertEquals("pinposition3", match);
		}

		public void testVariableExtraction3() throws Exception {
			params = new LinkedList();
			params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
			params.add(new CompoundVariable("_$1$"));
			params.add(new CompoundVariable("2"));
			variable.setParameters(params);
			String match = variable.execute(result, null);
			assertEquals("_pinposition2", match);
		}

		public void testVariableExtraction4() throws Exception {
			params = new LinkedList();
			params.add(new CompoundVariable("<value field=\"(pinposition\\d+)\">(\\d+)</value>"));
			params.add(new CompoundVariable("$2$, "));
			params.add(new CompoundVariable(".333"));
			variable.setParameters(params);
			String match = variable.execute(result, null);
			assertEquals("1, ", match);
		}

		public void testDefaultValue() throws Exception {
			params = new LinkedList();
			params.add(new CompoundVariable("<value,, field=\"(pinposition\\d+)\">(\\d+)</value>"));
			params.add(new CompoundVariable("$2$, "));
			params.add(new CompoundVariable(".333"));
			params.add(new CompoundVariable(""));
			params.add(new CompoundVariable("No Value Found"));
			variable.setParameters(params);
			String match = variable.execute(result, null);
			assertEquals("No Value Found", match);
		}
}
