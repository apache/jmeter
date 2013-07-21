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
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * ThreadGroup holds the settings for a JMeter thread group.
 * 
 * This class is intended to be ThreadSafe.
 */
public class ThreadGroup extends AbstractThreadGroup {
    private static final long serialVersionUID = 280L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long WAIT_TO_DIE = JMeterUtils.getPropDefault("jmeterengine.threadstop.wait", 5 * 1000); // 5 seconds

    /** How often to check for shutdown during ramp-up, default 1000ms */
    private static final int RAMPUP_GRANULARITY =
            JMeterUtils.getPropDefault("jmeterthread.rampup.granularity", 1000); // $NON-NLS-1$

    //+ JMX entries - do not change the string values

    /** Ramp-up time */
    public static final String RAMP_TIME = "ThreadGroup.ramp_time";

    /** Whether thread startup is delayed until required */
    public static final String DELAYED_START = "ThreadGroup.delayedStart";

    /** Whether scheduler is being used */
    public static final String SCHEDULER = "ThreadGroup.scheduler";

    /** Scheduler absolute start time */
    public static final String START_TIME = "ThreadGroup.start_time";

    /** Scheduler absolute end time */
    public static final String END_TIME = "ThreadGroup.end_time";

    /** Scheduler duration, overrides end time */
    public static final String DURATION = "ThreadGroup.duration";

    /** Scheduler start delay, overrides start time */
    public static final String DELAY = "ThreadGroup.delay";

    //- JMX entries

    private transient Thread threadStarter;

    // List of active threads
    private final Map<JMeterThread, Thread> allThreads = new ConcurrentHashMap<JMeterThread, Thread>();

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

    /**
     * This will schedule the time for the JMeterThread.
     *
     * @param thread JMeterThread
     */
    private void scheduleThread(JMeterThread thread, long now) {
        // if true the Scheduler is enabled
        if (getScheduler()) {
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

    @Override
    public void start(int groupCount, ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine) {
        running = true;
        int numThreads = getNumThreads();       
        int rampUp = getRampUp();
        float perThreadDelay = ((float) (rampUp * 1000) / (float) getNumThreads());

        delayedStartup = isDelayedStartup(); // Fetch once; needs to stay constant
        log.info("Starting thread group number " + groupCount
                + " threads " + numThreads
                + " ramp-up " + rampUp
                + " perThread " + perThreadDelay
                + " delayedStart=" + delayedStartup);
        if (delayedStartup) {
            threadStarter = new Thread(new ThreadStarter(groupCount, notifier, threadGroupTree, engine), getName()+"-ThreadStarter");
            threadStarter.setDaemon(true);
            threadStarter.start();
            // N.B. we don't wait for the thread to complete, as that would prevent parallel TGs
        } else {
            long now = System.currentTimeMillis(); // needs to be same time for all threads in the group
            final JMeterContext context = JMeterContextService.getContext();
            for (int i = 0; running && i < numThreads; i++) {
                JMeterThread jmThread = makeThread(groupCount, notifier, threadGroupTree, engine, i, context);
                scheduleThread(jmThread, now); // set start and end time
                jmThread.setInitialDelay((int)(i * perThreadDelay));
                Thread newThread = new Thread(jmThread, jmThread.getThreadName());
                registerStartedThread(jmThread, newThread);
                newThread.start();
            }
        }
        log.info("Started thread group number "+groupCount);
    }

    /**
     * Register Thread when it starts
     * @param jMeterThread {@link JMeterThread}
     * @param newThread Thread
     */
    private void registerStartedThread(JMeterThread jMeterThread, Thread newThread) {
        allThreads.put(jMeterThread, newThread);
    }

    private JMeterThread makeThread(int groupCount,
            ListenerNotifier notifier, ListedHashTree threadGroupTree,
            StandardJMeterEngine engine, int i, 
            JMeterContext context) { // N.B. Context needs to be fetched in the correct thread
        boolean onErrorStopTest = getOnErrorStopTest();
        boolean onErrorStopTestNow = getOnErrorStopTestNow();
        boolean onErrorStopThread = getOnErrorStopThread();
        boolean onErrorStartNextLoop = getOnErrorStartNextLoop();
        String groupName = getName();
        final JMeterThread jmeterThread = new JMeterThread(cloneTree(threadGroupTree), this, notifier);
        jmeterThread.setThreadNum(i);
        jmeterThread.setThreadGroup(this);
        jmeterThread.setInitialContext(context);
        final String threadName = groupName + " " + (groupCount) + "-" + (i + 1);
        jmeterThread.setThreadName(threadName);
        jmeterThread.setEngine(engine);
        jmeterThread.setOnErrorStopTest(onErrorStopTest);
        jmeterThread.setOnErrorStopTestNow(onErrorStopTestNow);
        jmeterThread.setOnErrorStopThread(onErrorStopThread);
        jmeterThread.setOnErrorStartNextLoop(onErrorStartNextLoop);
        return jmeterThread;
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
    @Override
    public void threadFinished(JMeterThread thread) {
        log.debug("Ending thread " + thread.getThreadName());
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
            stoppedAll = verifyThreadStopped(threadStarter);
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

    private ListedHashTree cloneTree(ListedHashTree tree) {
        TreeCloner cloner = new TreeCloner(true);
        tree.traverse(cloner);
        return cloner.getClonedTree();
    }

    private void pause(long ms){
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            // TODO Is this silent exception intended
        }
    }

    /**
     * Starts Threads using ramp up
     */
    class ThreadStarter implements Runnable {

        private final int groupCount;
        private final ListenerNotifier notifier;
        private final ListedHashTree threadGroupTree;
        private final StandardJMeterEngine engine;
        private final JMeterContext context;

        public ThreadStarter(int groupCount, ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine) {
            super();
            this.groupCount = groupCount;
            this.notifier = notifier;
            this.threadGroupTree = threadGroupTree;
            this.engine = engine;
            // Store context from Root Thread to pass it to created threads
            this.context = JMeterContextService.getContext();
            
        }
        
        @Override
        public void run() {
            // Copy in ThreadStarter thread context from calling Thread
            JMeterContextService.getContext().setVariables(this.context.getVariables());
            long now = System.currentTimeMillis(); // needs to be constant for all threads
            long endtime = 0;
            final boolean usingScheduler = getScheduler();
            if (usingScheduler) {
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
                // set the endtime for the Thread
                endtime = getDuration();
                if (endtime > 0) {// Duration is in seconds, starting from when the threads start
                    endtime = endtime *1000 + System.currentTimeMillis();
                } else {
                    endtime = getEndTime();
                }
            }
            final int numThreads = getNumThreads();
            final int perTthreadDelay = Math.round(((float) (getRampUp() * 1000) / (float) numThreads));
            for (int i = 0; running && i < numThreads; i++) {
                if (i > 0) {
                    pause(perTthreadDelay); // ramp-up delay (except first)
                }
                if (usingScheduler && System.currentTimeMillis() > endtime) {
                    break; // no point continuing beyond the end time
                }
                JMeterThread jmThread = makeThread(groupCount, notifier, threadGroupTree, engine, i, context);
                jmThread.setInitialDelay(0);   // Already waited
                if (usingScheduler) {
                    jmThread.setScheduled(true);
                    jmThread.setEndTime(endtime);
                }
                Thread newThread = new Thread(jmThread, jmThread.getThreadName());
                newThread.setDaemon(false); // ThreadStarter is daemon, but we don't want sampler threads to be so too
                registerStartedThread(jmThread, newThread);
                newThread.start();
            }
        }
    }
}
