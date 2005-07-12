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

package org.apache.jmeter.control;

import java.io.Serializable;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.testelement.property.StringProperty;

/**
 * @version $Revision$
 */
public class RunTime extends GenericController implements Serializable {

	private final static String SECONDS = "RunTime.seconds";

	private volatile long startTime = 0;

	private int loopCount = 0; // for getIterCount

	public RunTime() {
	}

	public void setRuntime(long seconds) {
		setProperty(new LongProperty(SECONDS, seconds));
	}

	public void setRuntime(String seconds) {
		setProperty(new StringProperty(SECONDS, seconds));
	}

	public long getRuntime() {
		try {
			return Long.parseLong(getPropertyAsString(SECONDS));
		} catch (NumberFormatException e) {
			return 0L;
		}
	}

	public String getRuntimeString() {
		return getPropertyAsString(SECONDS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.control.Controller#isDone()
	 */
	public boolean isDone() {
		if (getRuntime() > 0 && getSubControllers().size() > 0) {
			return super.isDone();
		} else {
			return true; // Runtime is zero - no point staying around
		}
	}

	private boolean endOfLoop() {
		return System.currentTimeMillis() - startTime >= 1000 * getRuntime();
	}

	public Sampler next() {
		if (startTime == 0)
			startTime = System.currentTimeMillis();
		if (endOfLoop()) {
			reInitialize();// ??
			resetLoopCount();
			return null;
		}
		return super.next();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.control.GenericController#nextIsNull()
	 */
	protected Sampler nextIsNull() throws NextIsNullException {
		reInitialize();
		if (endOfLoop()) {
			resetLoopCount();
			return null;
		} else {
			return next();
		}
	}

	protected void incrementLoopCount() {
		loopCount++;
	}

	protected void resetLoopCount() {
		loopCount = 0;
		startTime = 0;
	}

	/*
	 * This is needed for OnceOnly to work like other Loop Controllers
	 */
	protected int getIterCount() {
		return loopCount + 1;
	}

	protected void reInitialize() {
		setFirst(true);
		resetCurrent();
		incrementLoopCount();
		recoverRunningVersion();
	}

	// ////////////////////////////Start of Test Code
	// ///////////////////////////

	/**
	 * JUnit test
	 */
	public static class Test extends JMeterTestCase {
		public Test(String name) {
			super(name);
		}

		public void testProcessing() throws Exception {

			RunTime controller = new RunTime();
			controller.setRuntime(10);
			TestSampler samp1 = new TestSampler("Sample 1", 500);
			TestSampler samp2 = new TestSampler("Sample 2", 500);

			LoopController sub1 = new LoopController();
			sub1.setLoops(2);
			sub1.setContinueForever(false);
			sub1.addTestElement(samp1);

			LoopController sub2 = new LoopController();
			sub2.setLoops(40);
			sub2.setContinueForever(false);
			sub2.addTestElement(samp2);
			controller.addTestElement(sub1);
			controller.addTestElement(sub2);
			controller.setRunningVersion(true);
			sub1.setRunningVersion(true);
			sub2.setRunningVersion(true);
			controller.initialize();
			Sampler sampler = null;
			int loops = 0;
			long now = System.currentTimeMillis();
			while ((sampler = controller.next()) != null) {
				loops++;
				sampler.sample(null);
			}
			long elapsed = System.currentTimeMillis() - now;
			assertTrue("Should be at least 20 loops", loops >= 20);
			assertTrue("Should be fewer than 30 loops", loops < 30);
			assertTrue("Should take at least 10 seconds", elapsed >= 10000);
			assertTrue("Should take less than 12 seconds", elapsed <= 12000);
			assertEquals("Sampler 1 should run 2 times", 2, samp1.getSamples());
			assertTrue("Sampler 2 should run >= 18 times", samp2.getSamples() >= 18);
		}
	}

}