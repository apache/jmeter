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

/**
 * @version $Revision$
 */
public class TestWhileController extends JMeterTestCase {
		static {
			// LoggingManager.setPriority("DEBUG","jmeter");
			// LoggingManager.setTarget(new java.io.PrintWriter(System.out));
		}

		public TestWhileController(String name) {
			super(name);
		}

		// Get next sample and its name
		private String nextName(GenericController c) {
			Sampler s = c.next();
			if (s == null) {
				return null;
			} else {
				return s.getPropertyAsString(TestElement.NAME);
			}
		}

		// While (blank), previous sample OK - should loop until false
		public void testBlankPrevOK() throws Exception {
//			log.info("testBlankPrevOK");
			runtestPrevOK("");
		}

		// While (LAST), previous sample OK - should loop until false
		public void testLastPrevOK() throws Exception {
//			log.info("testLASTPrevOK");
			runtestPrevOK("LAST");
		}

		private static final String OTHER = "X"; // Dummy for testing
													// functions

		// While (LAST), previous sample OK - should loop until false
		public void testOtherPrevOK() throws Exception {
//			log.info("testOtherPrevOK");
			runtestPrevOK(OTHER);
		}

		public void runtestPrevOK(String type) throws Exception {
            WhileController.testMode = true;
            WhileController.testModeResult = true;
			GenericController controller = new GenericController();
			WhileController while_cont = new WhileController();
			while_cont.setCondition(type);
			while_cont.addTestElement(new TestSampler("one"));
			while_cont.addTestElement(new TestSampler("two"));
			while_cont.addTestElement(new TestSampler("three"));
			controller.addTestElement(while_cont);
			controller.addTestElement(new TestSampler("four"));
			controller.initialize();
			assertEquals("one", nextName(controller));
			assertEquals("two", nextName(controller));
			assertEquals("three", nextName(controller));
			assertEquals("one", nextName(controller));
			assertEquals("two", nextName(controller));
			assertEquals("three", nextName(controller));
			assertEquals("one", nextName(controller));
			WhileController.testModeResult = false;// one and two fail
			if (type.equals(OTHER))
				while_cont.setCondition("false");
			assertEquals("two", nextName(controller));
			assertEquals("three", nextName(controller));
			WhileController.testModeResult = true;// but three OK, so does not exit
			if (type.equals(OTHER))
				while_cont.setCondition(OTHER);
			assertEquals("one", nextName(controller));
			assertEquals("two", nextName(controller));
			assertEquals("three", nextName(controller));
			WhileController.testModeResult = false;// three fails, so exits while
			if (type.equals(OTHER))
				while_cont.setCondition("false");
			assertEquals("four", nextName(controller));
			assertNull(nextName(controller));
			WhileController.testModeResult = true;
			if (type.equals(OTHER))
				while_cont.setCondition(OTHER);
			assertEquals("one", nextName(controller));
		}

		// While (blank), previous sample failed - should run once
		public void testBlankPrevFailed() throws Exception {
//			log.info("testBlankPrevFailed");
			WhileController.testMode = true;
			WhileController.testModeResult = false;
			GenericController controller = new GenericController();
			WhileController while_cont = new WhileController();
			while_cont.setCondition("");
			while_cont.addTestElement(new TestSampler("one"));
			while_cont.addTestElement(new TestSampler("two"));
			controller.addTestElement(while_cont);
			controller.addTestElement(new TestSampler("three"));
			controller.initialize();
			assertEquals("one", nextName(controller));
			assertEquals("two", nextName(controller));
			assertEquals("three", nextName(controller));
			assertNull(nextName(controller));
			assertEquals("one", nextName(controller));
			assertEquals("two", nextName(controller));
			assertEquals("three", nextName(controller));
			assertNull(nextName(controller));
		}

		// While LAST, previous sample failed - should not run
		public void testLASTPrevFailed() throws Exception {
//			log.info("testLastPrevFailed");
			runTestPrevFailed("LAST");
		}

		// While False, previous sample failed - should not run
		public void testfalsePrevFailed() throws Exception {
//			log.info("testFalsePrevFailed");
			runTestPrevFailed("False");
		}

		public void runTestPrevFailed(String s) throws Exception {
			WhileController.testMode = true;
			WhileController.testModeResult = false;
			GenericController controller = new GenericController();
			WhileController while_cont = new WhileController();
			while_cont.setCondition(s);
			while_cont.addTestElement(new TestSampler("one"));
			while_cont.addTestElement(new TestSampler("two"));
			controller.addTestElement(while_cont);
			controller.addTestElement(new TestSampler("three"));
			controller.initialize();
			assertEquals("three", nextName(controller));
			assertNull(nextName(controller));
			assertEquals("three", nextName(controller));
			assertNull(nextName(controller));
		}

		// Tests for Stack Overflow (bug 33954)
		public void testAlwaysFailOK() throws Exception {
			runTestAlwaysFail(true); // Should be OK
		}

		// TODO - re-enable when fix found
		public void disabletestAlwaysFailBAD() throws Exception {
			runTestAlwaysFail(false); // Currently fails
		}

		public void runTestAlwaysFail(boolean other) {
			WhileController.testMode = true;
			WhileController.testModeResult = false;
			LoopController controller = new LoopController();
			controller.setContinueForever(true);
			controller.setLoops(-1);
			WhileController while_cont = new WhileController();
			while_cont.setCondition("false");
			while_cont.addTestElement(new TestSampler("one"));
			while_cont.addTestElement(new TestSampler("two"));
			controller.addTestElement(while_cont);
			if (other)
				controller.addTestElement(new TestSampler("three"));
			controller.initialize();
			try {
				if (other) {
					assertEquals("three", nextName(controller));
				} else {
					assertNull(nextName(controller));
				}
			} catch (StackOverflowError e) {
				// e.printStackTrace();
				fail(e.toString());
			}
		}
}