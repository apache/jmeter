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

/**
 * Dummy Sampler used to pause or stop a thread or the test;
 * intended for use in Conditional Controllers.
 * 
 */
public class TestAction extends AbstractSampler {
	// Actions
	public final static int STOP = 0;
	public final static int PAUSE = 1;

	// Action targets
	public final static int THREAD = 0;
	// public final static int THREAD_GROUP = 1;
	public final static int TEST = 2;

	// Identifiers
	private final static String TARGET = "ActionProcessor.target"; //$NON-NLS-1$
	private final static String ACTION = "ActionProcessor.action"; //$NON-NLS-1$
	private final static String DURATION = "ActionProcessor.duration"; //$NON-NLS-1$

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
		JMeterContext context = JMeterContextService.getContext();

		int target = getTarget();
		int action = getAction();
        if (action == PAUSE) {
            pause(getDuration());
        } else if (action == STOP) {
    		if (target == THREAD) {
                context.getThread().stop();
    		}
    		// Not yet implemented
    		// else if (target==THREAD_GROUP)
    		// {
    		// }
    		else if (target == TEST) {
   				context.getEngine().askThreadsToStop();
    		}
        }

		return null; // This means no sample is saved
	}

    private void pause(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
        }
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