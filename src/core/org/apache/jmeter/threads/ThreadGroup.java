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

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * ThreadGroup holds the settings for a JMeter thread group.
 * 
 * This class is intended to be ThreadSafe.
 */
public class ThreadGroup extends AbstractThreadGroup {
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long WAIT_TO_DIE = JMeterUtils.getPropDefault("jmeterengine.threadstop.wait", 5 * 1000); // 5 seconds

    /** How often to check for shutdown during ramp-up, default 1000ms */
    private static final int RAMPUP_GRANULARITY =
            JMeterUtils.getPropDefault("jmeterthread.rampup.granularity", 1000); // $NON-NLS-1$

    //+ JMX entries - do not change the string values

    /** Ramp-up time */
    public final static String RAMP_TIME = "ThreadGroup.ramp_time";

    /** Whether thread startup is delayed until required */
    public static final String DELAYED_START = "ThreadGroup.delayedStart";

    /** Whether scheduler is being used */
    public final static String SCHEDULER = "ThreadGroup.scheduler";

    /** Scheduler absolute start time */
    public final static String START_TIME = "ThreadGroup.start_time";

    /** Scheduler absolute end time */
    public final static String END_TIME = "ThreadGroup.end_time";

    /** Scheduler duration, overrides end time */
    public final static String DURATION = "ThreadGroup.duration";

    /** Scheduler start delay, overrides start time */
    public final static String DELAY = "ThreadGroup.delay";

    //- JMX entries

    private Thread threadStarter;

    private JMeterThread[] jmThreads;

    // List of active threads
    private Map<JMeterThread, Thread> allThreads = new ConcurrentHashMap<JMeterThread, Thread>();

    /**
     * Is test (still) running?
     */
    private volatile boolean running = false;

    /**
     * Are we using delayed startup?
     */
    private boolean delayedStartup;

    /**
     * No-arg constructor.
     */
    public ThreadGroup() {
    }

    /**
     * Set whether scheduler is being used
     *
     * @param Scheduler true is scheduler is to be used
     */
    public void setScheduler(boolean Scheduler) {
        setProperty(new BooleanProperty(SCHEDULER, Scheduler));
    }

    /**
     * Get whether scheduler is being used
     *
     * @return true if scheduler is being used
     */
    public boolean getScheduler() {
        return getPropertyAsBoolean(SCHEDULER);
    }

    /**
     * Set the absolute StartTime value.
     *
     * @param stime -
     *            the StartTime value.
     */
    public void setStartTime(long stime) {
        setProperty(new LongProperty(START_TIME, stime));
    }

    /**
     * Get the absolute start time value.
     *
     * @return the start time value.
     */
    public long getStartTime() {
        return getPropertyAsLong(START_TIME);
    }

    /**
     * Get the desired duration of the thread group test run
     *
     * @return the duration (in secs)
     */
    public long getDuration() {
        return getPropertyAsLong(DURATION);
    }

    /**
     * Set the desired duration of the thread group test run
     *
     * @param duration
     *            in seconds
     */
    public void setDuration(long duration) {
        setProperty(new LongProperty(DURATION, duration));
    }

    /**
     * Get the startup delay
     *
     * @return the delay (in secs)
     */
    public long getDelay() {
        return getPropertyAsLong(DELAY);
    }

    /**
     * Set the startup delay
     *
     * @param delay
     *            in seconds
     */
    public void setDelay(long delay) {
        setProperty(new LongProperty(DELAY, delay));
    }

    /**
     * Set the EndTime value.
     *
     * @param etime -
     *            the EndTime value.
     */
    public void setEndTime(long etime) {
        setProperty(new LongProperty(END_TIME, etime));
    }

    /**
     * Get the end time value.
     *
     * @return the end time value.
     */
    public long getEndTime() {
        return getPropertyAsLong(END_TIME);
    }

    /**
     * Set the ramp-up value.
     *
     * @param rampUp
     *            the ramp-up value.
     */
    public void setRampUp(int rampUp) {
        setProperty(new IntegerProperty(RAMP_TIME, rampUp));
    }

    /**
     * Get the ramp-up value.
     *
     * @return the ramp-up value.
     */
    public int getRampUp() {
        return getPropertyAsInt(ThreadGroup.RAMP_TIME);
    }

    private boolean isDelayedStartup() {
        return getPropertyAsBoolean(DELAYED_START);
    }

   @Override
   public void scheduleThread(JMeterThread thread)
   {
       if (isDelayedStartup()) { // Fetch once; needs to stay constant
           delayedStartup = true; 
           // No delay as OnDemandThreadGroup starts thread during rampup
           thread.setInitialDelay(0);
       } else {
           int rampUp = getRampUp();
           float perThreadDelay = ((float) (rampUp * 1000) / (float) getNumThreads());
           thread.setInitialDelay((int) (perThreadDelay * thread.getThreadNum()));
       }
       // if true the Scheduler is enabled
       if (getScheduler()) {
           long now = System.currentTimeMillis();
           // set the start time for the Thread
           if (getDelay() > 0) {// Duration is in seconds
               thread.setStartTime(getDelay() * 1000 + now);
           } else {
               long start = getStartTime();
               if (start < now) {
                   start = now; // Force a sensible start time
               }
               thread.setStartTime(start);
           }

           // set the endtime for the Thread
           if (getDuration() > 0) {// Duration is in seconds
               thread.setEndTime(getDuration() * 1000 + (thread.getStartTime()));
           } else {
               thread.setEndTime(getEndTime());
           }

           // Enables the scheduler
           thread.setScheduled(true);
       }
   }

    /**
     * Wait for delay with RAMPUP_GRANULARITY
     * @param delay delay in ms
     */
    private void delayBy(long delay) {
        if (delay > 0) {
            long start = System.currentTimeMillis();
            long end = start + delay;
            long now=0;
            long pause = RAMPUP_GRANULARITY; // maximum pause to use
            while(running && (now = System.currentTimeMillis()) < end) {
                long togo = end - now;
                if (togo < pause) {
                    pause = togo;
                }
                pause(pause); // delay between checks
            }
        }
    }

    /**
     * Default implementation starts threads immediately
     */
    @Override
    public void start() {
        running = true;
        if (delayedStartup) {
            this.threadStarter = new Thread(new ThreadStarter(), getName()+"-ThreadStarter");
            threadStarter.start();  
            try {
                threadStarter.join();
            } catch (InterruptedException e) {
            }            
        } else {
            for (int i = 0; i < jmThreads.length; i++) {
                Thread newThread = new Thread(jmThreads[i]);
                newThread.setName(jmThreads[i].getThreadName());
                registerStartedThread(jmThreads[i], newThread);
                newThread.start();            
            }
        }
    }

    /**
     * Register Thread when it starts
     * @param jMeterThread {@link JMeterThread}
     * @param newThread Thread
     */
    private void registerStartedThread(JMeterThread jMeterThread, Thread newThread) {
        allThreads.put(jMeterThread, newThread);
    }

    /**
     * 
     * @param jmThreads JMeterThread[]
     */
    @Override
    public final void setJMeterThreads(JMeterThread[] jmThreads) {
        this.jmThreads = jmThreads;
    }

    /**
     * Stop thread called threadName:
     * <ol>
     *  <li>stop JMeter thread</li>
     *  <li>interrupt JMeter thread</li>
     *  <li>interrupt underlying thread</li>
     * <ol>
     * @param threadName String thread name
     * @param now boolean for stop
     * @return true if thread stopped
     */
    @Override
    public boolean stopThread(String threadName, boolean now) {
        for(Entry<JMeterThread, Thread> entry : allThreads.entrySet()){
            JMeterThread thrd = entry.getKey();
            if (thrd.getThreadName().equals(threadName)){
                thrd.stop();
                thrd.interrupt();
                if (now) {
                    Thread t = entry.getValue();
                    if (t != null) {
                        t.interrupt();
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Called by JMeterThread when it finishes
     */
    public void threadFinished(JMeterThread thread) {
        log.info("Ending thread " + thread.getThreadName());
        allThreads.remove(thread);
    }

    /**
     * For each thread, invoke:
     * <ul> 
     * <li>{@link JMeterThread#stop()} - set stop flag</li>
     * <li>{@link JMeterThread#interrupt()} - interrupt sampler</li>
     * <li>{@link Thread#interrupt()} - interrupt JVM thread</li>
     * </ul> 
     */
    @Override
    public void tellThreadsToStop() {
        running = false;
        if (delayedStartup) {
            try {
                threadStarter.interrupt();
            } catch (Exception e) {
                log.warn("Exception occured interrupting ThreadStarter");
            }            
        }
        for (Entry<JMeterThread, Thread> entry : allThreads.entrySet()) {
            JMeterThread item = entry.getKey();
            item.stop(); // set stop flag
            item.interrupt(); // interrupt sampler if possible
            Thread t = entry.getValue();
            if (t != null ) { // Bug 49734
                t.interrupt(); // also interrupt JVM thread
            }
        }
    }


    /**
     * For each thread, invoke:
     * <ul> 
     * <li>{@link JMeterThread#stop()} - set stop flag</li>
     * </ul> 
     */
    @Override
    public void stop() {
        running = false;
        if (delayedStartup) {
            try {
                threadStarter.interrupt();
            } catch (Exception e) {
                log.warn("Exception occured interrupting ThreadStarter");
            }            
        }
        for (JMeterThread item : allThreads.keySet()) {
            item.stop();
        }
    }

    /**
     * @return number of active threads
     */
    @Override
    public int numberOfActiveThreads() {
        return allThreads.size();
    }

    /**
     * @return boolean true if all threads stopped
     */
    @Override
    public boolean verifyThreadsStopped() {
        boolean stoppedAll = true;
        if (delayedStartup){
            stoppedAll &= verifyThreadStopped(threadStarter);
        }
        for (Thread t : allThreads.values()) {
            stoppedAll = stoppedAll && verifyThreadStopped(t);
        }
        return stoppedAll;
    }

    /**
     * Verify thread stopped and return true if stopped successfully
     * @param thread Thread
     * @return boolean
     */
    private boolean verifyThreadStopped(Thread thread) {
        boolean stopped = true;
        if (thread != null) {
            if (thread.isAlive()) {
                try {
                    thread.join(WAIT_TO_DIE);
                } catch (InterruptedException e) {
                }
                if (thread.isAlive()) {
                    stopped = false;
                    log.warn("Thread won't exit: " + thread.getName());
                }
            }
        }
        return stopped;
    }

    /**
     * Wait for all Group Threads to stop
     */
    @Override
    public void waitThreadsStopped() {
        if (delayedStartup) {
            waitThreadStopped(threadStarter);            
        }
        for (Thread t : allThreads.values()) {
            waitThreadStopped(t);
        }
    }

    /**
     * Wait for thread to stop
     * @param thread Thread
     */
    private void waitThreadStopped(Thread thread) {
        if (thread != null) {
            while (thread.isAlive()) {
                try {
                    thread.join(WAIT_TO_DIE);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private void pause(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Starts Threads using ramp up
     */
    class ThreadStarter implements Runnable {

        public ThreadStarter() {
            super();
        }
        
        public void run() {
            final JMeterThread[] jMeterThreads = jmThreads;
            
            int rampUp = getRampUp();
            float perThreadDelay = ((float) (rampUp * 1000) / (float) getNumThreads());
            if (getScheduler()) {
                long now = System.currentTimeMillis();
                // set the start time for the Thread
                if (getDelay() > 0) {// Duration is in seconds
                    delayBy(getDelay() * 1000);
                } else {
                    long start = getStartTime();
                    if (start >= now) {
                        delayBy(start-now);
                    } 
                    // else start immediately
                }
            }
            for (int i = 0; i < jMeterThreads.length; i++) {
                try {
                    if(running) {
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
}
