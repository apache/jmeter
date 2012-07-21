/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Thread Group implementation that creates Threads progressively
 */
public class OnDemandThreadGroup extends ThreadGroup {
    /** How often to check for shutdown during ramp-up, default 1000ms */
    private static final int RAMPUP_GRANULARITY =
            JMeterUtils.getPropDefault("jmeterthread.rampup.granularity", 1000); // $NON-NLS-1$

    private static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * 
     */
    private static final long serialVersionUID = 1326448504092168570L;

    private Thread threadStarter;

    /**
     * Was test stopped
     */
    private AtomicBoolean stopped = new AtomicBoolean(false);

    /**
     * 
     */
    public OnDemandThreadGroup() {
        super();
    }

    /**
     * @see org.apache.jmeter.threads.AbstractThreadGroup#start()
     */
    @Override
    public void start() {
        stopped.set(false);
        this.threadStarter = new Thread(new ThreadStarter(), getName()+"-ThreadStarter");
        threadStarter.start();  
        try {
            threadStarter.join();
        } catch (InterruptedException e) {
            return;
        }
    }
    
    /**
     * Starts Threads using ramp up
     */
    private class ThreadStarter implements Runnable {

        public ThreadStarter() {
            super();
        }
        
        public void run() {
            final JMeterThread[] jMeterThreads = getJMeterThreads();
            
            int rampUp = getRampUp();
            float perThreadDelay = ((float) (rampUp * 1000) / (float) getNumThreads());
            if (getScheduler()) {
                long now = System.currentTimeMillis();
                // set the start time for the Thread
                if (getDelay() > 0) {// Duration is in seconds
                    delayBy(getDelay() * 1000, "start");
                } else {
                    long start = getStartTime();
                    if (start >= now) {
                        delayBy(start-now, "start");
                    } 
                    // else start immediately
                }
            }
            for (int i = 0; i < jMeterThreads.length; i++) {
                try {
                    if(!stopped.get()) {
                        Thread.sleep(Math.round(perThreadDelay));
                        Thread newThread = new Thread(jMeterThreads[i]);
                        newThread.setName(jMeterThreads[i].getThreadName());
                        registerStartedThread(jMeterThreads[i], newThread);
                        newThread.start();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    /**
     * Wait for delay with RAMPUP_GRANULARITY
     * @param delay delay in ms
     * @param type Delay type
     */
    protected final void delayBy(long delay, String type) {
        if (delay > 0) {
            long start = System.currentTimeMillis();
            long end = start + delay;
            long now=0;
            long pause = RAMPUP_GRANULARITY;
            while(!stopped.get() && (now = System.currentTimeMillis()) < end) {
                long togo = end - now;
                if (togo < pause) {
                    pause = togo;
                }
                try {
                    Thread.sleep(pause); // delay between checks
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
    /**
     * @see org.apache.jmeter.threads.AbstractThreadGroup#stop()
     */
    @Override
    public void stop() {
        stopped.set(true);
        try {
            threadStarter.interrupt();
        } catch (Exception e) {
            log.warn("Exception occured interrupting ThreadStarter");
        }
        super.stop();
    }
    
    /**
     * Schedule thread
     */
    @Override
    public void scheduleThread(JMeterThread thread)
    {
        // No delay as OnDemandThreadGroup starts thread during rampup
        thread.setInitialDelay(0);
        super.scheduleThread(thread, this);
    }
    
    /**
     * Stop thread stopper and JMeterThread Threads
     */
    @Override
    public void tellThreadsToStop() {
        stopped.set(true);
        try {
            threadStarter.interrupt();
        } catch (Exception e) {
            log.warn("Exception occured interrupting ThreadStarter");
        }
        super.tellThreadsToStop();
    }


    /* (non-Javadoc)
     * @see org.apache.jmeter.threads.AbstractThreadGroup#verifyThreadsStopped()
     */
    @Override
    public boolean verifyThreadsStopped() {
        return verifyThreadStopped(threadStarter) && super.verifyThreadsStopped();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.threads.AbstractThreadGroup#waitThreadsStopped()
     */
    @Override
    public void waitThreadsStopped() {
        waitThreadStopped(threadStarter);
        super.waitThreadsStopped();
    }
}