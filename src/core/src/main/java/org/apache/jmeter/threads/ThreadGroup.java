/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.threads;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.testelement.schema.PropertiesAccessor;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.util.JMeterStopTestException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThreadGroup holds the settings for a JMeter thread group.
 *
 * This class is intended to be ThreadSafe.
 */
@GUIMenuSortOrder(1)
@SuppressWarnings("JavaLangClash")
public class ThreadGroup extends AbstractThreadGroup {
    private static final long serialVersionUID = 282L;

    private static final Logger log = LoggerFactory.getLogger(ThreadGroup.class);

    private static final long WAIT_TO_DIE = DEFAULT_THREAD_STOP_TIMEOUT.toMillis();

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

    /** Scheduler duration, overrides end time */
    public static final String DURATION = "ThreadGroup.duration";

    /** Scheduler start delay, overrides start time */
    public static final String DELAY = "ThreadGroup.delay";
    //- JMX entries

    private transient Thread threadStarter;

    // List of active threads
    private final ConcurrentHashMap<JMeterThread, Thread> allThreads = new ConcurrentHashMap<>();

    private transient Object addThreadLock = new Object();

    /** Is test (still) running? */
    private volatile boolean running = false;

    /** Thread Group number */
    private int groupNumber;

    /** Are we using delayed startup? */
    private boolean delayedStartup;

    /** Thread safe class */
    private ListenerNotifier notifier;

    /** This property will be cloned */
    private ListedHashTree threadGroupTree;

    /**
     * No-arg constructor.
     */
    public ThreadGroup() {
        super();
    }

    @Override
    public ThreadGroupSchema getSchema() {
        return ThreadGroupSchema.INSTANCE;
    }

    @Override
    public @NotNull PropertiesAccessor<? extends ThreadGroup, ? extends ThreadGroupSchema> getProps() {
        return new PropertiesAccessor<>(this, getSchema());
    }

    /**
     * Set whether scheduler is being used
     *
     * @param scheduler true is scheduler is to be used
     */
    public void setScheduler(boolean scheduler) {
        setProperty(new BooleanProperty(SCHEDULER, scheduler));
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
        return get(getSchema().getDelayedStart());
    }

    /**
     * This will schedule the time for the JMeterThread.
     *
     * @param thread JMeterThread
     * @param now in milliseconds
     */
    private void scheduleThread(JMeterThread thread, long now) {

        if (!getScheduler()) { // if the Scheduler is not enabled
            return;
        }

        if (getDelay() >= 0) { // Duration is in seconds
            thread.setStartTime(getDelay() * 1000 + now);
        } else {
            throw new JMeterStopTestException("Invalid delay " + getDelay() + " set in Thread Group:" + getName());
        }

        // set the endtime for the Thread
        if (getDuration() > 0) {// Duration is in seconds
            thread.setEndTime(getDuration() * 1000 + thread.getStartTime());
        } else {
            throw new JMeterStopTestException("Invalid duration " + getDuration() + " set in Thread Group:" + getName());
        }
        // Enables the scheduler
        thread.setScheduled(true);
    }

    @Override
    public void start(int groupNum, ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine) {
        this.running = true;
        this.groupNumber = groupNum;
        this.notifier = notifier;
        this.threadGroupTree = threadGroupTree;
        int numThreads = getNumThreads();
        int rampUpPeriodInSeconds = getRampUp();
        delayedStartup = isDelayedStartup(); // Fetch once; needs to stay constant
        log.info("Starting thread group... number={} threads={} ramp-up={} delayedStart={}", groupNumber,
                numThreads, rampUpPeriodInSeconds, delayedStartup);
        if (delayedStartup) {
            threadStarter = new Thread(new ThreadStarter(notifier, threadGroupTree, engine), getName()+"-ThreadStarter");
            threadStarter.setDaemon(true);
            threadStarter.start();
            // N.B. we don't wait for the thread to complete, as that would prevent parallel TGs
        } else {
            final JMeterVariables variables = JMeterContextService.getContext().getVariables();
            long lastThreadStartInMillis = 0;
            int delayForNextThreadInMillis = 0;
            final int perThreadDelayInMillis = Math.round((float) rampUpPeriodInSeconds * 1000 / numThreads);
            for (int threadNum = 0; running && threadNum < numThreads; threadNum++) {
                long nowInMillis = System.currentTimeMillis();
                if(threadNum > 0) {
                    long timeElapsedToStartLastThread = nowInMillis - lastThreadStartInMillis;
                    // Note: `int += long` assignment hides lossy cast to int
                    delayForNextThreadInMillis = (int) (delayForNextThreadInMillis +
                            (perThreadDelayInMillis - timeElapsedToStartLastThread));
                }
                if (log.isDebugEnabled()) {
                    log.debug("Computed delayForNextThreadInMillis:{} for thread:{}", delayForNextThreadInMillis, Thread.currentThread().getId());
                }
                lastThreadStartInMillis = nowInMillis;
                startNewThread(notifier, threadGroupTree, engine, threadNum, variables, nowInMillis, Math.max(0, delayForNextThreadInMillis));
            }
        }
        log.info("Started thread group number {}", groupNumber);
    }

    /**
     * Start a new {@link JMeterThread} and registers it
     * @param notifier {@link ListenerNotifier}
     * @param threadGroupTree {@link ListedHashTree}
     * @param engine {@link StandardJMeterEngine}
     * @param threadNum Thread number
     * @param variables initial values for the variables in the thread
     * @param now Nom in milliseconds
     * @param delay int delay in milliseconds
     * @return {@link JMeterThread} newly created
     */
    private JMeterThread startNewThread(ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine,
            int threadNum, JMeterVariables variables, long now, int delay) {
        JMeterThread jmThread = makeThread(engine, this, notifier, groupNumber, threadNum, cloneTree(threadGroupTree), variables);
        scheduleThread(jmThread, now); // set start and end time
        jmThread.setInitialDelay(delay);
        Thread newThread = new Thread(jmThread, jmThread.getThreadName());
        registerStartedThread(jmThread, newThread);
        newThread.start();
        return jmThread;
    }

    /*
     * Fix NPE for addThreadLock transient object in remote mode (BZ60829)
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        addThreadLock = new Object();
    }

    /**
     * Register Thread when it starts
     * @param jMeterThread {@link JMeterThread}
     * @param newThread Thread
     */
    private void registerStartedThread(JMeterThread jMeterThread, Thread newThread) {
        allThreads.put(jMeterThread, newThread);
    }

    @Override
    @SuppressWarnings("SynchronizeOnNonFinalField")
    public JMeterThread addNewThread(int delay, StandardJMeterEngine engine) {
        long now = System.currentTimeMillis();
        JMeterContext context = JMeterContextService.getContext();
        JMeterThread newJmThread;
        int numThreads;
        synchronized (addThreadLock) {
            numThreads = getNumThreads();
            setNumThreads(numThreads + 1);
        }
        newJmThread = startNewThread(notifier, threadGroupTree, engine, numThreads, context.getVariables(), now, delay);
        JMeterContextService.addTotalThreads( 1 );
        log.info("Started new thread in group {}", groupNumber);
        return newJmThread;
    }

    /**
     * Stop thread called threadName:
     * <ol>
     *  <li>stop JMeter thread</li>
     *  <li>interrupt JMeter thread</li>
     *  <li>interrupt underlying thread</li>
     * </ol>
     * @param threadName String thread name
     * @param now boolean for stop
     * @return true if thread stopped
     */
    @Override
    public boolean stopThread(String threadName, boolean now) {
        for (Map.Entry<JMeterThread, Thread> threadEntry : allThreads.entrySet()) {
            JMeterThread jMeterThread = threadEntry.getKey();
            if (jMeterThread.getThreadName().equals(threadName)) {
                stopThread(jMeterThread, threadEntry.getValue(), now);
                return true;
            }
        }
        return false;
    }

    /**
     * Hard Stop JMeterThread thread and interrupt JVM Thread if interrupt is {@code true}
     * @param jmeterThread {@link JMeterThread}
     * @param jvmThread {@link Thread}
     * @param interrupt Interrupt thread or not
     */
    private static void stopThread(JMeterThread jmeterThread, Thread jvmThread, boolean interrupt) {
        jmeterThread.stop();
        jmeterThread.interrupt(); // interrupt sampler if possible
        if (interrupt && jvmThread != null) { // Bug 49734
            jvmThread.interrupt(); // also interrupt JVM thread
        }
    }

    /**
     * Called by JMeterThread when it finishes
     */
    @Override
    public void threadFinished(JMeterThread thread) {
        if (log.isDebugEnabled()) {
            log.debug("Ending thread {}", thread.getThreadName());
        }
        allThreads.remove(thread);
    }

    public void tellThreadsToStop(boolean now) {
        running = false;
        if (delayedStartup) {
            try {
                threadStarter.interrupt();
            } catch (Exception e) {
                log.warn("Exception occurred interrupting ThreadStarter", e);
            }
        }

        allThreads.forEach((key, value) -> stopThread(key, value, now));
    }

    /**
     * This is an immediate stop interrupting:
     * <ul>
     *  <li>current running threads</li>
     *  <li>current running samplers</li>
     * </ul>
     * For each thread, invoke:
     * <ul>
     * <li>{@link JMeterThread#stop()} - set stop flag</li>
     * <li>{@link JMeterThread#interrupt()} - interrupt sampler</li>
     * <li>{@link Thread#interrupt()} - interrupt JVM thread</li>
     * </ul>
     */
    @Override
    public void tellThreadsToStop() {
        tellThreadsToStop(true);
    }

    /**
     * This is a clean shutdown.
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
                log.warn("Exception occurred interrupting ThreadStarter", e);
            }
        }
        allThreads.keySet().forEach(JMeterThread::stop);
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
        if (delayedStartup) {
            stoppedAll = verifyThreadStopped(threadStarter);
        }
        if(stoppedAll) {
            for (Thread t : allThreads.values()) {
                if(!verifyThreadStopped(t)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Verify thread stopped and return true if stopped successfully
     * @param thread Thread
     * @return boolean
     */
    private static boolean verifyThreadStopped(Thread thread) {
        boolean stopped = true;
        if (thread != null && thread.isAlive()) {
            try {
                thread.join(WAIT_TO_DIE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (thread.isAlive()) {
                stopped = false;
                if (log.isWarnEnabled()) {
                    log.warn("Thread won't exit: {}", thread.getName());
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
        /* @Bugzilla 60933
         * Threads can be added on the fly during a test into allThreads
         * we have to check if allThreads is really empty before stopping
         */
        while (!allThreads.isEmpty()) {
            allThreads.values().forEach(ThreadGroup::waitThreadStopped);
        }

    }

    /**
     * Wait for thread to stop
     * @param thread Thread
     */
    private static void waitThreadStopped(Thread thread) {
        if (thread == null) {
            return;
        }
        while (thread.isAlive()) {
            try {
                thread.join(WAIT_TO_DIE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Starts Threads using ramp up
     */
    class ThreadStarter implements Runnable {

        private final ListenerNotifier notifier;
        private final ListedHashTree threadGroupTree;
        private final StandardJMeterEngine engine;
        private final JMeterVariables variables;

        public ThreadStarter(ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine) {
            super();
            this.notifier = notifier;
            this.threadGroupTree = threadGroupTree;
            this.engine = engine;
            // Store context from Root Thread to pass it to created threads
            this.variables = JMeterContextService.getContext().getVariables();
        }

        /**
         * Pause ms milliseconds
         * @param ms long milliseconds
         */
        private void pause(long ms){
            try {
                TimeUnit.MILLISECONDS.sleep(ms);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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
                long now;
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
        public void run() {
            try {
                // Copy in ThreadStarter thread context from calling Thread
                JMeterContextService.getContext().setVariables(variables);
                long endtime = 0;
                final boolean usingScheduler = getScheduler();
                if (usingScheduler) {
                    // set the start time for the Thread
                    if (getDelay() > 0) {// Duration is in seconds
                        delayBy(getDelay() * 1000);
                    }
                    // set the endtime for the Thread
                    endtime = getDuration();
                    if (endtime > 0) {// Duration is in seconds, starting from when the threads start
                        endtime = endtime *1000 + System.currentTimeMillis();
                    }
                }
                final int numThreads = getNumThreads();
                final float rampUpOriginInMillis = (float) getRampUp() * 1000;
                final long startTimeInMillis = System.currentTimeMillis();
                for (int threadNumber = 0; running && threadNumber < numThreads; threadNumber++) {
                    if (threadNumber > 0) {
                        long elapsedInMillis = System.currentTimeMillis() - startTimeInMillis;
                        final int perThreadDelayInMillis =
                                Math.round((rampUpOriginInMillis - elapsedInMillis) / (float) (numThreads - threadNumber));
                        pause(Math.max(0, perThreadDelayInMillis)); // ramp-up delay (except first)
                    }
                    if (usingScheduler && System.currentTimeMillis() > endtime) {
                        break; // no point continuing beyond the end time
                    }
                    JMeterThread jmThread = makeThread(engine, ThreadGroup.this, notifier, groupNumber, threadNumber, cloneTree(threadGroupTree), variables);
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
            } catch (Exception ex) {
                log.error("An error occurred scheduling delay start of threads for Thread Group: {}", getName(), ex);
            }
        }
    }
}
