/*
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jmeter.control;

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.testelement.TestElement;

/**
 * A controller that runs its children each at most once, but in a random order.
 * 
 * @author Mike Verdone
 * @version $Revision$ updated on $Date$
 */
public class TestRandomOrderController extends JMeterTestCase {

		public TestRandomOrderController(String name) {
			super(name);
		}

		public void testRandomOrder() {
			testLog.debug("Testing RandomOrderController");
			RandomOrderController roc = new RandomOrderController();
			roc.addTestElement(new TestSampler("zero"));
			roc.addTestElement(new TestSampler("one"));
			roc.addTestElement(new TestSampler("two"));
			roc.addTestElement(new TestSampler("three"));
			TestElement sampler = null;
			List usedSamplers = new ArrayList();
			roc.initialize();
			while ((sampler = roc.next()) != null) {
				String samplerName = sampler.getPropertyAsString(TestSampler.NAME);
				if (usedSamplers.contains(samplerName)) {
					assertTrue("Duplicate sampler returned from next()", false);
				}
				usedSamplers.add(samplerName);
			}
			assertTrue("All samplers were returned", usedSamplers.size() == 4);
		}

		public void testRandomOrderNoElements() {
			RandomOrderController roc = new RandomOrderController();
			roc.initialize();
			assertTrue(roc.next() == null);
		}

		public void testRandomOrderOneElement() {
			RandomOrderController roc = new RandomOrderController();
			roc.addTestElement(new TestSampler("zero"));
			TestElement sampler = null;
			List usedSamplers = new ArrayList();
			roc.initialize();
			while ((sampler = roc.next()) != null) {
				String samplerName = sampler.getPropertyAsString(TestSampler.NAME);
				if (usedSamplers.contains(samplerName)) {
					assertTrue("Duplicate sampler returned from next()", false);
				}
				usedSamplers.add(samplerName);
			}
			assertTrue("All samplers were returned", usedSamplers.size() == 1);
		}
}
