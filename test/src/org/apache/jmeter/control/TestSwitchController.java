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

package org.apache.jmeter.control;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;

public class TestSwitchController extends JMeterTestCase {
		static {
			// LoggingManager.setPriority("DEBUG","jmeter");
			// LoggingManager.setTarget(new java.io.PrintWriter(System.out));
		}

		public TestSwitchController(String name) {
			super(name);
		}

		// Get next sample and its name
		private String nextName(GenericController c) {
			Sampler s = c.next();
			String n;
			if (s == null) {
				return null;
			} else {
				n = s.getPropertyAsString(TestElement.NAME);
				return n;
			}
		}

		public void test() throws Exception {
			runSimpleTests("", "zero");
		}

		public void test0() throws Exception {
			runSimpleTests("0", "zero");
		}

		public void test1() throws Exception {
			runSimpleTests("1", "one");
		}

		public void test2() throws Exception {
			runSimpleTests("2", "two");
		}

		public void test3() throws Exception {
			runSimpleTests("3", "three");
		}

		public void test4() throws Exception {
			runSimpleTests("4", "zero");
		}

		public void testX() throws Exception {
			runSimpleTests("X", "zero");
		}

		public void runSimpleTests(String cond, String exp) throws Exception {
			runSimpleTest(cond, exp);
			runSimpleTest2(cond, exp);
		}

		// Simple test with single Selection controller
		public void runSimpleTest(String cond, String exp) throws Exception {
			GenericController controller = new GenericController();

			SwitchController switch_cont = new SwitchController();
			switch_cont.setSelection(cond);

			controller.addTestElement(new TestSampler("before"));
			controller.addTestElement(switch_cont);

			switch_cont.addTestElement(new TestSampler("zero"));
			switch_cont.addTestElement(new TestSampler("one"));
			switch_cont.addTestElement(new TestSampler("two"));
			switch_cont.addTestElement(new TestSampler("three"));

			controller.addTestElement(new TestSampler("after"));

			controller.initialize();

			for (int i = 1; i <= 3; i++) {
				assertEquals("Loop " + i, "before", nextName(controller));
				assertEquals("Loop " + i, exp, nextName(controller));
				assertEquals("Loop " + i, "after", nextName(controller));
				assertNull(nextName(controller));
			}
		}

		// Selection controller with two sub-controllers, but each has only 1
		// child
		public void runSimpleTest2(String cond, String exp) throws Exception {
			GenericController controller = new GenericController();
			GenericController sub_1 = new GenericController();
			GenericController sub_2 = new GenericController();

			SwitchController switch_cont = new SwitchController();
			switch_cont.setSelection(cond);

			switch_cont.addTestElement(new TestSampler("zero"));
			switch_cont.addTestElement(sub_1);
			sub_1.addTestElement(new TestSampler("one"));

			switch_cont.addTestElement(new TestSampler("two"));

			switch_cont.addTestElement(sub_2);
			sub_2.addTestElement(new TestSampler("three"));

			controller.addTestElement(new TestSampler("before"));
			controller.addTestElement(switch_cont);
			controller.addTestElement(new TestSampler("after"));
			controller.initialize();
			for (int i = 1; i <= 3; i++) {
				assertEquals("before", nextName(controller));
				assertEquals(exp, nextName(controller));
				assertEquals("after", nextName(controller));
				assertNull(nextName(controller));
			}
		}

		public void testTest2() throws Exception {
			runTest2("", new String[] { "zero" });
			runTest2("0", new String[] { "zero" });
			runTest2("7", new String[] { "zero" });
			runTest2("5", new String[] { "zero" });
			runTest2("4", new String[] { "six" });
			runTest2("3", new String[] { "five" });
			runTest2("1", new String[] { "one", "two" });
			runTest2("2", new String[] { "three", "four" });
		}

		/*
		 * Test: Before Selection Controller - zero (default) - simple
		 * controller 1 - - one - - two - simple controller 2 - - three - - four -
		 * five - six After
		 */
		public void runTest2(String cond, String exp[]) throws Exception {
			int loops = 3;
			LoopController controller = new LoopController();
			controller.setLoops(loops);
			controller.setContinueForever(false);
			GenericController sub_1 = new GenericController();
			GenericController sub_2 = new GenericController();

			SwitchController switch_cont = new SwitchController();
			switch_cont.setSelection(cond);

			switch_cont.addTestElement(new TestSampler("zero"));
			switch_cont.addTestElement(sub_1);
			sub_1.addTestElement(new TestSampler("one"));
			sub_1.addTestElement(new TestSampler("two"));

			switch_cont.addTestElement(sub_2);
			sub_2.addTestElement(new TestSampler("three"));
			sub_2.addTestElement(new TestSampler("four"));

			switch_cont.addTestElement(new TestSampler("five"));
			switch_cont.addTestElement(new TestSampler("six"));

			controller.addTestElement(new TestSampler("before"));
			controller.addTestElement(switch_cont);
			controller.addTestElement(new TestSampler("after"));
			controller.setRunningVersion(true);
			sub_1.setRunningVersion(true);
			sub_2.setRunningVersion(true);
			switch_cont.setRunningVersion(true);
			controller.initialize();
			for (int i = 1; i <= 3; i++) {
				assertEquals("Loop:" + i, "before", nextName(controller));
				for (int j = 0; j < exp.length; j++) {
					assertEquals("Loop:" + i, exp[j], nextName(controller));
				}
				assertEquals("Loop:" + i, "after", nextName(controller));
			}
			assertNull("Loops:" + loops, nextName(controller));
		}
}
