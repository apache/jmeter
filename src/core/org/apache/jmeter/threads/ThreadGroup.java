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

import java.io.Serializable;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;

/**
 * ThreadGroup holds the settings for a JMeter thread group.
 * 
 * This class is intended to be ThreadSafe.
 */
public class ThreadGroup extends AbstractTestElement implements Serializable, Controller {

    private static final long serialVersionUID = 233L;

    /** Number of threads in the thread group */
    public final static String NUM_THREADS = "ThreadGroup.num_threads";

    /** Ramp-up time */
    public final static String RAMP_TIME = "ThreadGroup.ramp_time";

    public final static String MAIN_CONTROLLER = "ThreadGroup.main_controller";

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

    /** Action to be taken when a Sampler error occurs */
    public final static String ON_SAMPLE_ERROR = "ThreadGroup.on_sample_error"; // int

    /** Continue, i.e. ignore sampler errors */
    public final static String ON_SAMPLE_ERROR_CONTINUE = "continue";

    /** Stop current thread if sampler error occurs */
    public final static String ON_SAMPLE_ERROR_STOPTHREAD = "stopthread";

    /** Stop test (all threads) if sampler error occurs */
    public final static String ON_SAMPLE_ERROR_STOPTEST = "stoptest";

    /** Stop test NOW (all threads) if sampler error occurs */
    public final static String ON_SAMPLE_ERROR_STOPTEST_NOW = "stoptestnow";

    // @GuardedBy("this")
    private int numberOfThreads = 0; // Number of active threads in this group

    /**
     * No-arg constructor.
     */
    public ThreadGroup() {
    }

    /**
     * Set the total number of threads to start
     *
     * @param numThreads
     *            the number of threads.
     */
    public void setNumThreads(int numThreads) {
        setProperty(new IntegerProperty(NUM_THREADS, numThreads));
    }

    /**
     * Increment the number of active threads
     */
    synchronized void incrNumberOfThreads() {
        numberOfThreads++;
    }

    /**
     * Decrement the number of active threads
     */
    synchronized void decrNumberOfThreads() {
        numberOfThreads--;
    }

    /**
     * Get the number of active threads
     */
    public synchronized int getNumberOfThreads() {
        return numberOfThreads;
    }

    /** {@inheritDoc} */
    public boolean isDone() {
        return getSamplerController().isDone();
    }

    /** {@inheritDoc} */
    public Sampler next() {
        return getSamplerController().next();
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

    /**
     * Get the sampler controller.
     *
     * @return the sampler controller.
     */
    public Controller getSamplerController() {
        Controller c = (Controller) getProperty(MAIN_CONTROLLER).getObjectValue();
        return c;
    }

    /**
     * Set the sampler controller.
     *
     * @param c
     *            the sampler controller.
     */
    public void setSamplerController(LoopController c) {
        c.setContinueForever(false);
        setProperty(new TestElementProperty(MAIN_CONTROLLER, c));
    }

    /**
     * Get the number of threads.
     *
     * @return the number of threads.
     */
    public int getNumThreads() {
        return this.getPropertyAsInt(ThreadGroup.NUM_THREADS);
    }

    /**
     * Add a test element.
     *
     * @param child
     *            the test element to add.
     */
    public void addTestElement(TestElement child) {
        getSamplerController().addTestElement(child);
    }

    /** {@inheritDoc} */
    public void addIterationListener(LoopIterationListener lis) {
        getSamplerController().addIterationListener(lis);
    }

    /** {@inheritDoc} */
    public void initialize() {
        Controller c = getSamplerController();
        JMeterProperty property = c.getProperty(TestElement.NAME);
        property.setObjectValue(getName()); // Copy our name into that of the controller
        property.setRunningVersion(property.isRunningVersion());// otherwise name reverts
        c.initialize();
    }

    /**
     * Check if a sampler error should cause thread to stop.
     *
     * @return true if thread should stop
     */
    public boolean getOnErrorStopThread() {
        return getPropertyAsString(ThreadGroup.ON_SAMPLE_ERROR).equalsIgnoreCase(ON_SAMPLE_ERROR_STOPTHREAD);
    }

    /**
     * Check if a sampler error should cause test to stop.
     *
     * @return true if test (all threads) should stop
     */
    public boolean getOnErrorStopTest() {
        return getPropertyAsString(ThreadGroup.ON_SAMPLE_ERROR).equalsIgnoreCase(ON_SAMPLE_ERROR_STOPTEST);
    }

    /**
     * Check if a sampler error should cause test to stop now.
     *
     * @return true if test (all threads) should stop immediately
     */
    public boolean getOnErrorStopTestNow() {
        return getPropertyAsString(ThreadGroup.ON_SAMPLE_ERROR).equalsIgnoreCase(ON_SAMPLE_ERROR_STOPTEST_NOW);
    }

}
