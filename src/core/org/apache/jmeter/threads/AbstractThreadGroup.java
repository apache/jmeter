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
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * ThreadGroup holds the settings for a JMeter thread group.
 * 
 * This class is intended to be ThreadSafe.
 */
public abstract class AbstractThreadGroup extends AbstractTestElement implements Serializable, Controller {

    private static final long serialVersionUID = 240L;

    private static final long WAIT_TO_DIE = JMeterUtils.getPropDefault("jmeterengine.threadstop.wait", 5 * 1000); // 5 seconds

    private static final Logger log = LoggingManager.getLoggerForClass();

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

    private JMeterThread[] jmThreads;

    private Map<JMeterThread, Thread> allThreads = new ConcurrentHashMap<JMeterThread, Thread>();

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

    public abstract void scheduleThread(JMeterThread thread);

    /**
     * Default implementation starts threads immediately
     */
    public void start() {
        for (int i = 0; i < jmThreads.length; i++) {
            Thread newThread = new Thread(jmThreads[i]);
            newThread.setName(jmThreads[i].getThreadName());
            registerStartedThread(jmThreads[i], newThread);
            newThread.start();            
        }
    }

    /**
     * Register Thread when it starts
     * @param jMeterThread {@link JMeterThread}
     * @param newThread Thread
     */
    protected final void registerStartedThread(JMeterThread jMeterThread, Thread newThread) {
        allThreads.put(jMeterThread, newThread);
    }

    /**
     * 
     * @param jmThreads JMeterThread[]
     */
    public final void setJMeterThreads(JMeterThread[] jmThreads) {
        this.jmThreads = jmThreads;
    }

    /**
     * @return JMeterThread[]
     */
    protected final JMeterThread[] getJMeterThreads() {
        return this.jmThreads;
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
     * Called by JMeter thread when it finishes
     */
    public void threadFinished(JMeterThread thread) {
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
    public void tellThreadsToStop() {
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
    public void stop() {
        for (JMeterThread item : allThreads.keySet()) {
            item.stop();
        }
    }

    /**
     * @return number of active threads
     */
    public int numberOfActiveThreads() {
        return allThreads.size();
    }

    /**
     * @return boolean true if all threads stopped
     */
    public boolean verifyThreadsStopped() {
        boolean stoppedAll = true;
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
    protected final boolean verifyThreadStopped(Thread thread) {
        boolean stoppedAll = true;
        if (thread != null) {
            if (thread.isAlive()) {
                try {
                    thread.join(WAIT_TO_DIE);
                } catch (InterruptedException e) {
                }
                if (thread.isAlive()) {
                    stoppedAll = false;
                    log.warn("Thread won't exit: " + thread.getName());
                }
            }
        }
        return stoppedAll;
    }

    /**
     * Wait for all Group Threads to stop
     */
    public void waitThreadsStopped() {
        for (Thread t : allThreads.values()) {
            waitThreadStopped(t);
        }
    }

    /**
     * Wait for thread to stop
     * @param thread Thread
     */
    protected final void waitThreadStopped(Thread thread) {
        if (thread != null) {
            while (thread.isAlive()) {
                try {
                    thread.join(WAIT_TO_DIE);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
