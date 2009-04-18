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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.TransactionSampler;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.AbstractScopedAssertion;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterStopTestException;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.apache.log.Logger;

/**
 * The JMeter interface to the sampling process, allowing JMeter to see the
 * timing, add listeners for sampling events and to stop the sampling process.
 *
 */
public class JMeterThread implements Runnable, Interruptible {
    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String PACKAGE_OBJECT = "JMeterThread.pack"; // $NON-NLS-1$

    public static final String LAST_SAMPLE_OK = "JMeterThread.last_sample_ok"; // $NON-NLS-1$

    private final Controller controller;

    private final HashTree testTree;

    private final TestCompiler compiler;

    private final JMeterThreadMonitor monitor;

    private final JMeterVariables threadVars;

    private final Collection testListeners;

    private final ListenerNotifier notifier;

    /*
     * The following variables are set by StandardJMeterEngine.
     * This is done before start() is called, so the values will be published to the thread safely
     * TODO - consider passing them to the constructor, so that they can be made final
     * (to avoid adding lots of parameters, perhaps have a parameter wrapper object.
     */
    private String threadName;

    private int initialDelay = 0;

    private int threadNum = 0;

    private long startTime = 0;

    private long endTime = 0;

    private boolean scheduler = false;
    // based on this scheduler is enabled or disabled

    // Gives access to parent thread threadGroup
    private ThreadGroup threadGroup;

    private StandardJMeterEngine engine = null; // For access to stop methods.

    /*
     * The following variables may be set/read from multiple threads.
     */
    private volatile boolean running; // may be set from a different thread

    private volatile boolean onErrorStopTest;

    private volatile boolean onErrorStopThread;
    
    private volatile Sampler currentSampler;

    public JMeterThread(HashTree test, JMeterThreadMonitor monitor, ListenerNotifier note) {
        this.monitor = monitor;
        threadVars = new JMeterVariables();
        testTree = test;
        compiler = new TestCompiler(testTree, threadVars);
        controller = (Controller) testTree.getArray()[0];
        SearchByClass threadListenerSearcher = new SearchByClass(TestListener.class);
        test.traverse(threadListenerSearcher);
        testListeners = threadListenerSearcher.getSearchResults();
        notifier = note;
        running = true;
    }

    public void setInitialContext(JMeterContext context) {
        threadVars.putAll(context.getVariables());
    }

    /**
     * Enable the scheduler for this JMeterThread.
     */
    public void setScheduled(boolean sche) {
        this.scheduler = sche;
    }

    /**
     * Set the StartTime for this Thread.
     *
     * @param stime the StartTime value.
     */
    public void setStartTime(long stime) {
        startTime = stime;
    }

    /**
     * Get the start time value.
     *
     * @return the start time value.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Set the EndTime for this Thread.
     *
     * @param etime
     *            the EndTime value.
     */
    public void setEndTime(long etime) {
        endTime = etime;
    }

    /**
     * Get the end time value.
     *
     * @return the end time value.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Check the scheduled time is completed.
     *
     */
    private void stopScheduler() {
        long delay = System.currentTimeMillis() - endTime;
        if ((delay >= 0)) {
            running = false;
        }
    }

    /**
     * Wait until the scheduled start time if necessary
     *
     */
    private void startScheduler() {
        long delay = (startTime - System.currentTimeMillis());
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (Exception e) {
            }
        }
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    /*
     * See below for reason for this change. Just in case this causes problems,
     * allow the change to be backed out
     */
    private static final boolean startEarlier =
        JMeterUtils.getPropDefault("jmeterthread.startearlier", true); // $NON-NLS-1$

    private static final boolean reversePostProcessors =
        JMeterUtils.getPropDefault("jmeterthread.reversePostProcessors",false); // $NON-NLS-1$

    static {
        if (startEarlier) {
            log.info("jmeterthread.startearlier=true (see jmeter.properties)");
        } else {
            log.info("jmeterthread.startearlier=false (see jmeter.properties)");
        }
        if (reversePostProcessors) {
            log.info("Running PostProcessors in reverse order");
        } else {
            log.info("Running PostProcessors in forward order");
        }
    }

    public void run() {
        // threadContext is not thread-safe, so keep within thread
        JMeterContext threadContext = JMeterContextService.getContext();
        try {
            initRun(threadContext);
            while (running) {
                Sampler sam;
                while (running && (sam = controller.next()) != null) {
                    process_sampler(sam, null, threadContext);
                }
                if (controller.isDone()) {
                    running = false;
                }
            }
        }
        // Might be found by contoller.next()
        catch (JMeterStopTestException e) {
            log.info("Stopping Test: " + e.toString());
            stopTest();
        } catch (JMeterStopThreadException e) {
            log.info("Stop Thread seen: " + e.toString());
        } catch (Exception e) {
            log.error("Test failed!", e);
        } catch (ThreadDeath e) {
            throw e; // Must not ignore this one
        } catch (Error e) {// Make sure errors are output to the log file
            log.error("Test failed!", e);
        } finally {
            threadContext.clear();
            log.info("Thread finished: " + threadName);
            monitor.threadFinished(this);
            threadFinished();
        }
    }

    /**
     * Process the current sampler, handling transaction samplers.
     *
     * @param current sampler
     * @param parent sampler
     * @param threadContext 
     * @return SampleResult if a transaction was processed
     */
    private SampleResult process_sampler(Sampler current, Sampler parent, JMeterContext threadContext) {
        SampleResult transactionResult = null;
        try {
            threadContext.setCurrentSampler(current);
            currentSampler = current;

            // Check if we are running a transaction
            TransactionSampler transactionSampler = null;
            if(current instanceof TransactionSampler) {
                transactionSampler = (TransactionSampler) current;
            }
            // Find the package for the transaction
            SamplePackage transactionPack = null;
            if(transactionSampler != null) {
                transactionPack = compiler.configureTransactionSampler(transactionSampler);

                // Check if the transaction is done
                if(transactionSampler.isTransactionDone()) {
                    // Get the transaction sample result
                    transactionResult = transactionSampler.getTransactionResult();
                    transactionResult.setThreadName(threadName);
                    transactionResult.setGroupThreads(threadGroup.getNumberOfThreads());
                    transactionResult.setAllThreads(JMeterContextService.getNumberOfThreads());

                    // Check assertions for the transaction sample
                    checkAssertions(transactionPack.getAssertions(), transactionResult, threadContext);
                    // Notify listeners with the transaction sample result
                    if (!(parent instanceof TransactionSampler)){
                        notifyListeners(transactionPack.getSampleListeners(), transactionResult);
                    }
                    compiler.done(transactionPack);
                    // Transaction is done, we do not have a sampler to sample
                    current = null;
                }
                else {
                    Sampler prev = current;
                    // It is the sub sampler of the transaction that will be sampled
                    current = transactionSampler.getSubSampler();
                    if (current instanceof TransactionSampler){
                        SampleResult res = process_sampler(current, prev, threadContext);// recursive call
                        threadContext.setCurrentSampler(prev);
                        currentSampler = prev;
                        current=null;
                        if (res!=null){
                            transactionSampler.addSubSamplerResult(res);
                        }
                    }
                }
            }

            // Check if we have a sampler to sample
            if(current != null) {
                // Get the sampler ready to sample
                SamplePackage pack = compiler.configureSampler(current);
                runPreProcessors(pack.getPreProcessors());

                // Hack: save the package for any transaction controllers
                threadVars.putObject(PACKAGE_OBJECT, pack);

                delay(pack.getTimers());
                Sampler sampler = pack.getSampler();
                sampler.setThreadContext(threadContext);
                sampler.setThreadName(threadName);
                TestBeanHelper.prepare(sampler);

                // Perform the actual sample
                SampleResult
                result = sampler.sample(null);
                // TODO: remove this useless Entry parameter

                // If we got any results, then perform processing on the result
                if (result != null) {
                    result.setGroupThreads(threadGroup.getNumberOfThreads());
                    result.setAllThreads(JMeterContextService.getNumberOfThreads());
                    result.setThreadName(threadName);
                    threadContext.setPreviousResult(result);
                    runPostProcessors(pack.getPostProcessors());
                    checkAssertions(pack.getAssertions(), result, threadContext);
                    // Do not send subsamples to listeners which receive the transaction sample
                    List sampleListeners = getSampleListeners(pack, transactionPack, transactionSampler);
                    notifyListeners(sampleListeners, result);
                    compiler.done(pack);
                    // Add the result as subsample of transaction if we are in a transaction
                    if(transactionSampler != null) {
                        transactionSampler.addSubSamplerResult(result);
                    }

                    // Check if thread or test should be stopped
                    if (result.isStopThread() || (!result.isSuccessful() && onErrorStopThread)) {
                        stopThread();
                    }
                    if (result.isStopTest() || (!result.isSuccessful() && onErrorStopTest)) {
                        stopTest();
                    }
                } else {
                    compiler.done(pack); // Finish up
                }
            }
            if (scheduler) {
                // checks the scheduler to stop the iteration
                stopScheduler();
            }
        } catch (JMeterStopTestException e) {
            log.info("Stopping Test: " + e.toString());
            stopTest();
        } catch (JMeterStopThreadException e) {
            log.info("Stopping Thread: " + e.toString());
            stopThread();
        } catch (Exception e) {
            if (current != null) {
                log.error("Error while processing sampler '"+current.getName()+"' :", e);
            } else {
                log.error("", e);
            }
        }
        return transactionResult;
    }

    /**
     * Get the SampleListeners for the sampler. Listeners who receive transaction sample
     * will not be in this list.
     *
     * @param samplePack
     * @param transactionPack
     * @param transactionSampler
     * @return the listeners who should receive the sample result
     */
    private List getSampleListeners(SamplePackage samplePack, SamplePackage transactionPack, TransactionSampler transactionSampler) {
        List sampleListeners = samplePack.getSampleListeners();
        // Do not send subsamples to listeners which receive the transaction sample
        if(transactionSampler != null) {
            ArrayList onlySubSamplerListeners = new ArrayList();
            List transListeners = transactionPack.getSampleListeners();
            for(Iterator i = sampleListeners.iterator(); i.hasNext();) {
                SampleListener listener = (SampleListener)i.next();
                // Check if this instance is present in transaction listener list
                boolean found = false;
                for(Iterator j = transListeners.iterator(); j.hasNext();) {
                    // Check for the same instance
                    if(j.next() == listener) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    onlySubSamplerListeners.add(listener);
                }
            }
            sampleListeners = onlySubSamplerListeners;
        }
        return sampleListeners;
    }

    /**
     * @param threadContext 
     *
     */
    private void initRun(JMeterContext threadContext) {
        threadContext.setVariables(threadVars);
        threadContext.setThreadNum(getThreadNum());
        threadContext.getVariables().put(LAST_SAMPLE_OK, "true");
        threadContext.setThread(this);
        threadContext.setThreadGroup(threadGroup);
        threadContext.setEngine(engine);
        testTree.traverse(compiler);
        // listeners = controller.getListeners();
        if (scheduler) {
            // set the scheduler to start
            startScheduler();
        }
        rampUpDelay();
        log.info("Thread started: " + Thread.currentThread().getName());
        JMeterContextService.incrNumberOfThreads();
        threadGroup.incrNumberOfThreads();
        GuiPackage gp =GuiPackage.getInstance();
        if (gp != null) {// check there is a GUI
            gp.getMainFrame().updateCounts();
        }
        /*
         * Setting SamplingStarted before the contollers are initialised allows
         * them to access the running values of functions and variables (however
         * it does not seem to help with the listeners)
         */
        if (startEarlier) {
            threadContext.setSamplingStarted(true);
        }
        controller.initialize();
        controller.addIterationListener(new IterationListener());
        if (!startEarlier) {
            threadContext.setSamplingStarted(true);
        }
        threadStarted();
    }

    /**
     *
     */
    private void threadStarted() {
        ThreadListenerTraverser startup = new ThreadListenerTraverser(true);
        testTree.traverse(startup);
    }

    /**
     *
     */
    private void threadFinished() {
        ThreadListenerTraverser shut = new ThreadListenerTraverser(false);
        testTree.traverse(shut);
        JMeterContextService.decrNumberOfThreads();
        threadGroup.decrNumberOfThreads();
        GuiPackage gp = GuiPackage.getInstance();
        if (gp != null){
            gp.getMainFrame().updateCounts();
        }
    }

    private static class ThreadListenerTraverser implements HashTreeTraverser {
        private boolean isStart = false;

        private ThreadListenerTraverser(boolean start) {
            isStart = start;
        }

        public void addNode(Object node, HashTree subTree) {
            if (node instanceof ThreadListener) {
                ThreadListener tl = (ThreadListener) node;
                if (isStart) {
                    tl.threadStarted();
                } else {
                    tl.threadFinished();
                }
            }
        }

        public void subtractNode() {
        }

        public void processPath() {
        }
    }

    public String getThreadName() {
        return threadName;
    }

    public void stop() { // Called by StandardJMeterEngine
        running = false;
        log.info("Stopping: " + threadName);
    }

    /** {@inheritDoc} */
    public boolean interrupt(){
        log.warn("Interrupting: " + threadName);
        Sampler samp = currentSampler; // fetch once
        if (samp instanceof Interruptible){
            try {
                ((Interruptible)samp).interrupt();
            } catch (Exception e) {
                log.warn("Caught Exception interrupting sampler: "+e.toString());
            }
            return true;
        }
        return false;
    }

    private void stopTest() {
        running = false;
        log.info("Stop Test detected by thread: " + threadName);
        // engine.stopTest();
        if (engine != null) {
            engine.askThreadsToStop();
        }
    }

    private void stopThread() {
        running = false;
        log.info("Stop Thread detected by thread: " + threadName);
    }

    private void checkAssertions(List assertions, SampleResult parent, JMeterContext threadContext) {
        Iterator iter = assertions.iterator();
        while (iter.hasNext()) {
            Assertion assertion = (Assertion) iter.next();
            TestBeanHelper.prepare((TestElement) assertion);
            if (assertion instanceof AbstractScopedAssertion){
                AbstractScopedAssertion scopedAssertion = (AbstractScopedAssertion) assertion;
                String scope = scopedAssertion.fetchScope();
                if (scopedAssertion.isScopeParent(scope) || scopedAssertion.isScopeAll(scope)){
                    processAssertion(parent, assertion);
                }
                if (scopedAssertion.isScopeChildren(scope) || scopedAssertion.isScopeAll(scope)){
                    SampleResult children[] = parent.getSubResults();
                    boolean childError = false;
                    for (int i=0;i <children.length; i++){
                        processAssertion(children[i], assertion);
                        if (!children[i].isSuccessful()){
                            childError = true;
                        }
                    }
                    // If parent is OK, but child failed, add a message and flag the parent as failed
                    if (childError && parent.isSuccessful()) {
                        AssertionResult assertionResult = new AssertionResult(((AbstractTestElement)assertion).getName());
                        assertionResult.setResultForFailure("One or more sub-samples failed");
                        parent.addAssertionResult(assertionResult);
                        parent.setSuccessful(false);
                    }
                }
            } else {
                processAssertion(parent, assertion);
            }
        }
        threadContext.getVariables().put(LAST_SAMPLE_OK, Boolean.toString(parent.isSuccessful()));
    }

    private void processAssertion(SampleResult result, Assertion assertion) {
        AssertionResult assertionResult = assertion.getResult(result);
        result.setSuccessful(result.isSuccessful() && !(assertionResult.isError() || assertionResult.isFailure()));
        result.addAssertionResult(assertionResult);
    }

    private void runPostProcessors(List extractors) {
        ListIterator iter;
        if (reversePostProcessors) {// Original (rather odd) behaviour
            iter = extractors.listIterator(extractors.size());// start at the end
            while (iter.hasPrevious()) {
                PostProcessor ex = (PostProcessor) iter.previous();
                TestBeanHelper.prepare((TestElement) ex);
                ex.process();
            }
        } else {
            iter = extractors.listIterator(); // start at the beginning
            while (iter.hasNext()) {
                PostProcessor ex = (PostProcessor) iter.next();
                TestBeanHelper.prepare((TestElement) ex);
                ex.process();
            }
        }
    }

    private void runPreProcessors(List preProcessors) {
        Iterator iter = preProcessors.iterator();
        while (iter.hasNext()) {
            PreProcessor ex = (PreProcessor) iter.next();
            if (log.isDebugEnabled()) {
                log.debug("Running preprocessor: " + ((AbstractTestElement) ex).getName());
            }
            TestBeanHelper.prepare((TestElement) ex);
            ex.process();
        }
    }

    private void delay(List timers) {
        long sum = 0;
        Iterator iter = timers.iterator();
        while (iter.hasNext()) {
            Timer timer = (Timer) iter.next();
            TestBeanHelper.prepare((TestElement) timer);
            sum += timer.delay();
        }
        if (sum > 0) {
            try {
                Thread.sleep(sum);
            } catch (InterruptedException e) {
                log.warn("The delay timer was interrupted - probably did not wait as long as intended.");
            }
        }
    }

    private void notifyTestListeners() {
        threadVars.incIteration();
        Iterator iter = testListeners.iterator();
        while (iter.hasNext()) {
            TestListener listener = (TestListener) iter.next();
            if (listener instanceof TestElement) {
                listener.testIterationStart(new LoopIterationEvent(controller, threadVars.getIteration()));
                ((TestElement) listener).recoverRunningVersion();
            } else {
                listener.testIterationStart(new LoopIterationEvent(controller, threadVars.getIteration()));
            }
        }
    }

    private void notifyListeners(List listeners, SampleResult result) {
        SampleEvent event = new SampleEvent(result, threadGroup.getName(), threadVars);
        notifier.notifyListeners(event, listeners);

    }

    public void setInitialDelay(int delay) {
        initialDelay = delay;
    }

    /**
     * Initial delay if ramp-up period is active for this threadGroup.
     */
    private void rampUpDelay() {
        if (initialDelay > 0) {
            try {
                Thread.sleep(initialDelay);
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Returns the threadNum.
     */
    public int getThreadNum() {
        return threadNum;
    }

    /**
     * Sets the threadNum.
     *
     * @param threadNum
     *            the threadNum to set
     */
    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    private class IterationListener implements LoopIterationListener {
        /*
         * (non-Javadoc)
         *
         * @see LoopIterationListener#iterationStart(LoopIterationEvent)
         */
        public void iterationStart(LoopIterationEvent iterEvent) {
            notifyTestListeners();
        }
    }

    /**
     * Save the engine instance for access to the stop methods
     *
     * @param engine
     */
    public void setEngine(StandardJMeterEngine engine) {
        this.engine = engine;
    }

    /**
     * Should Test stop on sampler error?
     *
     * @param b -
     *            true or false
     */
    public void setOnErrorStopTest(boolean b) {
        onErrorStopTest = b;
    }

    /**
     * Should Thread stop on Sampler error?
     *
     * @param b -
     *            true or false
     */
    public void setOnErrorStopThread(boolean b) {
        onErrorStopThread = b;
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    public void setThreadGroup(ThreadGroup group) {
        this.threadGroup = group;
    }

}