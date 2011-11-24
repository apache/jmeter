/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.timers;

import java.io.Serializable;
import java.util.concurrent.BrokenBarrierException;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.JMeterContextService;

/**
 * The purpose of the SyncTimer is to block threads until X number of threads
 * have been blocked, and then they are all released at once. A SyncTimer can
 * thus create large instant loads at various points of the test plan.
 *
 */
public class SyncTimer extends AbstractTestElement implements Timer, Serializable, TestBean, TestListener, ThreadListener {
    private static final long serialVersionUID = 2;
    
    private BarrierWrapper barrier;

    private int groupSize;

    // Ensure transient object is created by the server
    private Object readResolve(){
    	createBarrier();
        return this;
    }

    /**
     * @return Returns the numThreads.
     */
    public int getGroupSize() {
        return groupSize;
    }

    /**
     * @param numThreads
     *            The numThreads to set.
     */
    public void setGroupSize(int numThreads) {
        this.groupSize = numThreads;
    }

    /**
     * {@inheritDoc}
     */
    public long delay() {
    	if(getGroupSize()>=0) {
    		int arrival = 0;
    		try {
				arrival = this.barrier.await();
			} catch (InterruptedException e) {
				return 0;
			} catch (BrokenBarrierException e) {
				return 0;
			} finally {
				if(arrival == 0) {
					barrier.reset();
				}
			}
    	}
        return 0;
    }

    /**
     * We have to control the cloning process because we need some cross-thread
     * communication if our synctimers are to be able to determine when to block
     * and when to release.
     */
    @Override
    public Object clone() {
        SyncTimer newTimer = (SyncTimer) super.clone();
        newTimer.barrier = barrier;
        return newTimer;
    }

    /**
     * {@inheritDoc}
     */
    public void testEnded() {
        this.testEnded(null);        
    }

    /**
     * Reset timerCounter
     */
    public void testEnded(String host) {
    	createBarrier();
    }

    /**
     * {@inheritDoc}
     */
    public void testStarted() {
        testStarted(null);
    }

    /**
     * Reset timerCounter
     */
    public void testStarted(String host) {
        createBarrier();
    }

	@Override
	public void testIterationStart(LoopIterationEvent event) {
		// NOOP
	}
	
	/**
	 * 
	 */
	private void createBarrier() {
		if(getGroupSize() == 0) {
			// Lazy init 
			this.barrier = new BarrierWrapper();
        } else {
        	this.barrier = new BarrierWrapper(getGroupSize());
        }
	}

	@Override
	public void threadStarted() {
		int numThreadsInGroup = JMeterContextService.getContext().getThreadGroup().getNumThreads();
		if(getGroupSize() == 0) {
			// Unique Barrier creation ensured by synchronized setup
			this.barrier.setup(numThreadsInGroup);
        }
	}

	@Override
	public void threadFinished() {
		// NOOP
	}
}