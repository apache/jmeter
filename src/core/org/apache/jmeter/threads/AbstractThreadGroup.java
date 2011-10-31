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
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;

/**
 * ThreadGroup holds the settings for a JMeter thread group.
 * 
 * This class is intended to be ThreadSafe.
 */
public abstract class AbstractThreadGroup extends AbstractTestElement implements Serializable, Controller {

    private static final long serialVersionUID = 240L;

    /** Action to be taken when a Sampler error occurs */
    public final static String ON_SAMPLE_ERROR = "ThreadGroup.on_sample_error"; // int

    /** Continue, i.e. ignore sampler errors */
    public final static String ON_SAMPLE_ERROR_CONTINUE = "continue";

    /** Start next loop for current thread if sampler error occurs */
    public final static String ON_SAMPLE_ERROR_START_NEXT_LOOP = "startnextloop";

    /** Stop current thread if sampler error occurs */
    public final static String ON_SAMPLE_ERROR_STOPTHREAD = "stopthread";

    /** Stop test (all threads) if sampler error occurs */
    public final static String ON_SAMPLE_ERROR_STOPTEST = "stoptest";

    /** Stop test NOW (all threads) if sampler error occurs */
    public final static String ON_SAMPLE_ERROR_STOPTEST_NOW = "stoptestnow";

    /** Number of threads in the thread group */
    public final static String NUM_THREADS = "ThreadGroup.num_threads";

    public final static String MAIN_CONTROLLER = "ThreadGroup.main_controller";

    // @GuardedBy("this")
    private int numberOfThreads = 0; // Number of active threads in this group

    /** {@inheritDoc} */
    public boolean isDone() {
        return getSamplerController().isDone();
    }

    /** {@inheritDoc} */
    public Sampler next() {
        return getSamplerController().next();
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
     * Add a test element.
     *
     * @param child
     *            the test element to add.
     */
    @Override
    public void addTestElement(TestElement child) {
        getSamplerController().addTestElement(child);
    }

    /** {@inheritDoc} */
    public void addIterationListener(LoopIterationListener lis) {
        getSamplerController().addIterationListener(lis);
    }
    
    /** {@inheritDoc} */
    public void removeIterationListener(LoopIterationListener iterationListener) {
        getSamplerController().removeIterationListener(iterationListener);
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
     * Start next iteration after an error
     */
    public void startNextLoop() {
       ((LoopController) getSamplerController()).startNextLoop();
    }
    
    /**
     * NOOP
     */
    public void triggerEndOfLoop() {
        // NOOP
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
    
    /**
     * Get the number of threads.
     *
     * @return the number of threads.
     */
    public int getNumThreads() {
        return this.getPropertyAsInt(AbstractThreadGroup.NUM_THREADS);
    }

    /**
     * Check if a sampler error should cause thread to start next loop.
     *
     * @return true if thread should start next loop
     */
    public boolean getOnErrorStartNextLoop() {
        return getPropertyAsString(ThreadGroup.ON_SAMPLE_ERROR).equalsIgnoreCase(ON_SAMPLE_ERROR_START_NEXT_LOOP);
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

    public abstract void scheduleThread(JMeterThread thread);
}
