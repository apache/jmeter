/*
 * Copyright 2000-2004,2006 The Apache Software Foundation.
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

package org.apache.jmeter.threads;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.engine.StandardJMeterEngine;

/**
 * Holds context for a thread
 */
public class JMeterContext {
	JMeterVariables variables;

	SampleResult previousResult;

	Sampler currentSampler;

	Sampler previousSampler;

	boolean samplingStarted;

	private StandardJMeterEngine engine;

	private JMeterThread thread;

	private ThreadGroup threadGroup;
	
    private int threadNum;

	private byte[] readBuffer = null;

	JMeterContext() {
		variables = null;
		previousResult = null;
		currentSampler = null;
		samplingStarted = false;
	}

	public void clear() {
		variables = null;
		previousResult = null;
		currentSampler = null;
		previousSampler = null;
		samplingStarted = false;
		threadNum = 0;
		readBuffer = null;
	}

	public JMeterVariables getVariables() {
		return variables;
	}

	public byte[] getReadBuffer() {
		if (readBuffer == null) {
			readBuffer = new byte[8192];
		}
		return readBuffer;
	}

	public void setVariables(JMeterVariables vars) {
		this.variables = vars;
	}

	public SampleResult getPreviousResult() {
		return previousResult;
	}

	public void setPreviousResult(SampleResult result) {
		this.previousResult = result;
	}

	public Sampler getCurrentSampler() {
		return currentSampler;
	}

	public void setCurrentSampler(Sampler sampler) {
		setPreviousSampler(currentSampler);
		this.currentSampler = sampler;
	}

	/**
	 * Returns the previousSampler.
	 * 
	 * @return Sampler
	 */
	public Sampler getPreviousSampler() {
		return previousSampler;
	}

	/**
	 * Sets the previousSampler.
	 * 
	 * @param previousSampler
	 *            the previousSampler to set
	 */
	public void setPreviousSampler(Sampler previousSampler) {
		this.previousSampler = previousSampler;
	}

	/**
	 * Returns the threadNum.
	 * 
	 * @return int
	 */
	public int getThreadNum() {
		return threadNum;
	}

	/**
	 * Sets the threadNum.
	 * 
	 * @param threadNum
	 *            the threadNum to set
	 */
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	public JMeterThread getThread() {
		return this.thread;
	}

	public void setThread(JMeterThread thread) {
		this.thread = thread;
	}

    public ThreadGroup getThreadGroup() {
        return this.threadGroup;
    }

    public void setThreadGroup(ThreadGroup threadgrp) {
        this.threadGroup = threadgrp;
    }

	public StandardJMeterEngine getEngine() {
		return engine;
	}

	public void setEngine(StandardJMeterEngine engine) {
		this.engine = engine;
	}

	public boolean isSamplingStarted() {
		return samplingStarted;
	}

	public void setSamplingStarted(boolean b) {
		samplingStarted = b;
	}
}
