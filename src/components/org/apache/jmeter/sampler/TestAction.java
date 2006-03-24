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
package org.apache.jmeter.sampler;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;

/**
 * Dummy Sampler used to pause or stop a thread or the test;
 * intended for use in Conditional Controllers.
 * 
 */
public class TestAction extends AbstractSampler {
	// Actions
	public final static int STOP = 0;

	public final static int PAUSE = 1;

	// Action target
	public final static int THREAD = 0;

	// public final static int THREAD_GROUP = 1;
	public final static int TEST = 2;

	// Identifiers
	private final static String TARGET = "ActionProcessor.target";

	private final static String ACTION = "ActionProcessor.action";

	private final static String DURATION = "ActionProcessor.duration";

	public TestAction() {
		super();
		setTarget(THREAD);
		setAction(PAUSE);
		setDuration(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.samplers.Sampler#sample(org.apache.jmeter.samplers.Entry)
	 */
	public SampleResult sample(Entry e) {
		// SampleResult res = new SampleResult();
		JMeterContext context = JMeterContextService.getContext();
		// StringBuffer response = new StringBuffer();
		//				
		// res.setSampleLabel(getTitle());
		// res.setSuccessful(true);
		// res.setResponseMessage("OK");
		// res.sampleStart();

		int target = getTarget();
		int action = getAction();
		if (target == THREAD) {
			JMeterThread thread = context.getThread();
			if (action == PAUSE) {
				// res.setSamplerData("Pause Thread "+thread.getThreadNum());
				thread.pauseThread(getDuration());
				// response.append("Thread ");
				// response.append(thread.getThreadNum());
				// response.append(" paused for ");
				// response.append(getDuration());
				// response.append(" miliseconds");
			} else if (action == STOP) {
				// res.setSamplerData("Stop Thread"+thread.getThreadNum());
				context.getThread().stop();
				// response.append("Thread ");
				// response.append(thread.getThreadNum());
				// response.append(" stopped");
			}
		}
		// Not yet implemented
		// else if (target==THREAD_GROUP)
		// {
		// if (action==PAUSE)
		// {
		// }
		// else if (action==STOP)
		// {
		// }
		// }
		else if (target == TEST) {
			if (action == PAUSE) {
				context.getEngine().pauseTest(getDuration());
			} else if (action == STOP) {
				context.getEngine().askThreadsToStop();
			}
		}

		// res.setResponseData(response.toString().getBytes());
		// res.sampleEnd();
		// return res;
		return null; // This means no sample is saved
	}

	public void setTarget(int target) {
		setProperty(new IntegerProperty(TARGET, target));
	}

	public int getTarget() {
		return getPropertyAsInt(TARGET);
	}

	public void setAction(int action) {
		setProperty(new IntegerProperty(ACTION, action));
	}

	public int getAction() {
		return getPropertyAsInt(ACTION);
	}

	public void setDuration(int duration) {
		setProperty(new IntegerProperty(DURATION, duration));
	}

	public int getDuration() {
		return getPropertyAsInt(DURATION);
	}
}
