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

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The purpose of the SyncTimer is to block threads until X number of threads
 * have been blocked, and then they are all released at once. A SyncTimer can
 * thus create large instant loads at various points of the test plan.
 *
 */
public class SyncTimer extends AbstractTestElement implements Timer, Serializable, TestBean, TestListener {
    private static final long serialVersionUID = 2;

    private static final Logger log = LoggingManager.getLoggerForClass();

    // Must be an Object so it will be shared between threads
    private int[] timerCounter = new int[] { 0 };

    private transient Object sync = new Object();

    private int groupSize;

    // Ensure transient object is created by the server
    private Object readResolve(){
        sync = new Object();
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
        synchronized (sync) {
            timerCounter[0]++;
            final int groupSz = getGroupSize();
            final int count = timerCounter[0];
            if (
                (groupSz == 0 && count >= // count of threads in the thread group
                    JMeterContextService.getContext().getThreadGroup().getNumThreads())
                ||
                (groupSz > 0 && count >= groupSz)
                ) {
                sync.notifyAll();
                timerCounter[0]=0; // reset counter once we have reached the limit
            } else {
                try {
                    sync.wait();
                } catch (InterruptedException e) {
                    log.warn(e.getLocalizedMessage());
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
        newTimer.timerCounter = timerCounter;
        newTimer.sync = sync;
        return newTimer;
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
		this.timerCounter[0] = 0;
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
		 this.timerCounter[0] = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public void testIterationStart(LoopIterationEvent event) {
		// NOOP
	}
}