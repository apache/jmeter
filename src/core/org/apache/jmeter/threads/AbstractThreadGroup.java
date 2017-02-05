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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * ThreadGroup holds the settings for a JMeter thread group.
 * 
 * This class is intended to be ThreadSafe.
 */
public abstract class AbstractThreadGroup extends AbstractTestElement 
    implements Serializable, Controller, JMeterThreadMonitor, TestCompilerHelper {

    private static final long serialVersionUID = 240L;

    // Only create the map if it is required
    private final transient ConcurrentMap<TestElement, Object> children = new ConcurrentHashMap<>();

    private static final Object DUMMY = new Object();

    /** Action to be taken when a Sampler error occurs */
    public static final String ON_SAMPLE_ERROR = "ThreadGroup.on_sample_error"; // int

    /** Continue, i.e. ignore sampler errors */
    public static final String ON_SAMPLE_ERROR_CONTINUE = "continue";

    /** Start next loop for current thread if sampler error occurs */
    public static final String ON_SAMPLE_ERROR_START_NEXT_LOOP = "startnextloop";

    /** Stop current thread if sampler error occurs */
    public static final String ON_SAMPLE_ERROR_STOPTHREAD = "stopthread";

    /** Stop test (all threads) if sampler error occurs, the entire test is stopped at the end of any current samples */
    public static final String ON_SAMPLE_ERROR_STOPTEST = "stoptest";

    /** Stop test NOW (all threads) if sampler error occurs, the entire test is stopped abruptly. Any current samplers are interrupted if possible. */
    public static final String ON_SAMPLE_ERROR_STOPTEST_NOW = "stoptestnow";

    /** Number of threads in the thread group */
    public static final String NUM_THREADS = "ThreadGroup.num_threads";

    public static final String MAIN_CONTROLLER = "ThreadGroup.main_controller";

    private final AtomicInteger numberOfThreads = new AtomicInteger(0); // Number of active threads in this group

    /** {@inheritDoc} */
    @Override
    public boolean isDone() {
        return getSamplerController().isDone();
    }

    /** {@inheritDoc} */
    @Override
    public Sampler next() {
        return getSamplerController().next();
    }

    /**
     * Get the sampler controller.
     *
     * @return the sampler controller.
     */
    public Controller getSamplerController() {
        return (Controller) getProperty(MAIN_CONTROLLER).getObjectValue();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean addTestElementOnce(TestElement child){
        if (children.putIfAbsent(child, DUMMY) == null) {
            addTestElement(child);
            return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void addIterationListener(LoopIterationListener lis) {
        getSamplerController().addIterationListener(lis);
    }
    
    /** {@inheritDoc} */
    @Override
    public void removeIterationListener(LoopIterationListener iterationListener) {
        getSamplerController().removeIterationListener(iterationListener);
    }

    /** {@inheritDoc} */
    @Override
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
    @Override
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
    void incrNumberOfThreads() {
        numberOfThreads.incrementAndGet();
    }

    /**
     * Decrement the number of active threads
     */
    void decrNumberOfThreads() {
        numberOfThreads.decrementAndGet();
    }

    /**
     * Get the number of active threads
     *
     * @return the number of active threads
     */
    public int getNumberOfThreads() {
        return numberOfThreads.get();
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
        return getPropertyAsString(AbstractThreadGroup.ON_SAMPLE_ERROR).equalsIgnoreCase(ON_SAMPLE_ERROR_START_NEXT_LOOP);
    }

    /**
     * Check if a sampler error should cause thread to stop.
     *
     * @return true if thread should stop
     */
    public boolean getOnErrorStopThread() {
        return getPropertyAsString(AbstractThreadGroup.ON_SAMPLE_ERROR).equalsIgnoreCase(ON_SAMPLE_ERROR_STOPTHREAD);
    }

    /**
     * Check if a sampler error should cause test to stop.
     *
     * @return true if test (all threads) should stop
     */
    public boolean getOnErrorStopTest() {
        return getPropertyAsString(AbstractThreadGroup.ON_SAMPLE_ERROR).equalsIgnoreCase(ON_SAMPLE_ERROR_STOPTEST);
    }

    /**
     * Check if a sampler error should cause test to stop now.
     *
     * @return true if test (all threads) should stop immediately
     */
    public boolean getOnErrorStopTestNow() {
        return getPropertyAsString(AbstractThreadGroup.ON_SAMPLE_ERROR).equalsIgnoreCase(ON_SAMPLE_ERROR_STOPTEST_NOW);
    }

    /**
     * Hard or graceful stop depending on now flag
     * @param threadName String thread name
     * @param now if true interrupt {@link Thread} 
     * @return boolean true if stop succeeded
     */
    public abstract boolean stopThread(String threadName, boolean now);

    /**
     * @return int number of active threads 
     */
    public abstract int numberOfActiveThreads();

    /**
     * Start the {@link ThreadGroup}
     * @param groupCount group number
     * @param notifier {@link ListenerNotifier}
     * @param threadGroupTree {@link ListedHashTree}
     * @param engine {@link StandardJMeterEngine}
     */
    public abstract void start(int groupCount, ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine);

    /**
     * Add a new {@link JMeterThread} to this {@link ThreadGroup} for engine
     * @param delay Delay in milliseconds
     * @param engine {@link StandardJMeterEngine}
     * @return {@link JMeterThread}
     */
    public abstract JMeterThread addNewThread(int delay, StandardJMeterEngine engine);

    /**
     * @return true if threads were correctly stopped
     */
    public abstract boolean verifyThreadsStopped();

    /**
     * Wait for all Group Threads to stop after a graceful stop
     */
    public abstract void waitThreadsStopped();

    /**
     * Ask threads to stop gracefully
     */
    public abstract void tellThreadsToStop();

    /**
     * This immediately stop threads of Group by interrupting them
     * It differs from {@link AbstractThreadGroup#tellThreadsToStop()} by being a hard stop
     */
    public abstract void stop();
}
