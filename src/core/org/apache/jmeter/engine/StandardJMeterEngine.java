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

package org.apache.jmeter.engine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.TestCompiler;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 */
public class StandardJMeterEngine implements JMeterEngine, JMeterThreadMonitor, Runnable {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 233L; // Remember to change this when the class changes ...

    private static final long WAIT_TO_DIE = 5 * 1000; // 5 seconds

    /** JMeterThread => its JVM thread */
    private final Map/*<JMeterThread, Thread>*/ allThreads;

    /** flag to show that groups are still being created, i.e test plan is not complete */
    private volatile boolean startingGroups;

    /** Flag to show whether test is running. Set to false to stop creating more threads. */
    private volatile boolean running = false;

    /** Thread Groups run sequentially */
    private volatile boolean serialized = false;

    private HashTree test;

    private volatile SearchByClass testListenersSave;

    private final String host;

    // Should we exit at end of the test? (only applies to server, because host is non-null)
    private static final boolean exitAfterTest =
        JMeterUtils.getPropDefault("server.exitaftertest", false);  // $NON-NLS-1$

    private static final boolean startListenersLater =
        JMeterUtils.getPropDefault("jmeterengine.startlistenerslater", true); // $NON-NLS-1$

    static {
        if (startListenersLater){
            log.info("Listeners will be started after enabling running version");
            log.info("To revert to the earlier behaviour, define jmeterengine.startlistenerslater=false");
        }
    }
    // Allow engine and threads to be stopped from outside a thread
    // e.g. from beanshell server
    // Assumes that there is only one instance of the engine
    // at any one time so it is not guaranteed to work ...
    private static Map/*<String, JMeterThread>*/ allThreadNames; //TODO does not appear to be populated yet

    private volatile static StandardJMeterEngine engine;

    /** Unmodifiable static version of {@link allThreads} JMeterThread => JVM Thread */
    private static Map/*<JMeterThread, Thread>*/  allThreadsSave;

    public static void stopEngineNow() {
        if (engine != null) {// May be null if called from Unit test
            engine.stopTest(true);
        }
    }

    public static void stopEngine() {
        if (engine != null) { // May be null if called from Unit test
            engine.stopTest(false);
        }
    }

    /*
     * Allow functions etc to register for testStopped notification.
     * Only used by the function parser so far.
     * The list is merged with the testListeners and then cleared.
     */
    private static final List testList = new ArrayList();

    public static synchronized void register(TestListener tl) {
        testList.add(tl);
    }

    public static boolean stopThread(String threadName) {
        return stopThread(threadName, false);
    }

    public static boolean stopThreadNow(String threadName) {
        return stopThread(threadName, true);
    }

    private static boolean stopThread(String threadName, boolean now) {
        if (allThreadNames == null) {
            return false;// e.g. not yet started
        }
        JMeterThread thrd;
        try {
            thrd = (JMeterThread) allThreadNames.get(threadName);
        } catch (Exception e) {
            log.warn("stopThread: " + e);
            return false;
        }
        if (thrd != null) {
            thrd.stop();
            if (now) {
                Thread t = (Thread) allThreadsSave.get(thrd);
                if (t != null) {
                    t.interrupt();
                }
            }
            return true;
        }
        return false;
    }

    // End of code to allow engine to be controlled remotely

    public StandardJMeterEngine() {
        this(null);
    }

    public StandardJMeterEngine(String host) {
        this.host = host;
        this.allThreads = new HashMap();
        // Hacks to allow external control
        engine = this;
        allThreadNames = new HashMap();
        allThreadsSave = Collections.unmodifiableMap(allThreads);
    }

    public void configure(HashTree testTree) {
        test = testTree;
    }

    // TODO: in Java1.5, perhaps we can use Thread.setUncaughtExceptionHandler() instead
    private static class MyThreadGroup extends java.lang.ThreadGroup{
        public MyThreadGroup(String s) {
            super(s);
          }

          public void uncaughtException(Thread t, Throwable e) {
            if (!(e instanceof ThreadDeath)) {
                log.error("Uncaught exception: ", e);
                System.err.println("Uncaught Exception " + e + ". See log file for details.");
            }
          }
    }

    public void runTest() throws JMeterEngineException {
        try {
            Thread runningThread = new Thread(new MyThreadGroup("JMeterThreadGroup"),this);
            runningThread.start();
        } catch (Exception err) {
            stopTest();
            StringWriter string = new StringWriter();
            PrintWriter writer = new PrintWriter(string);
            err.printStackTrace(writer);
            throw new JMeterEngineException(string.toString());
        }
    }

    private void removeThreadGroups(List elements) {
        Iterator iter = elements.iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
            if (item instanceof ThreadGroup) {
                iter.remove();
            } else if (!(item instanceof TestElement)) {
                iter.remove();
            }
        }
    }

    private void notifyTestListenersOfStart(SearchByClass testListeners) {
        Iterator iter = testListeners.getSearchResults().iterator();
        while (iter.hasNext()) {
            TestListener tl = (TestListener) iter.next();
            if (tl instanceof TestBean) {
                TestBeanHelper.prepare((TestElement) tl);
            }
            if (host == null) {
                tl.testStarted();
            } else {
                tl.testStarted(host);
            }
        }
    }

    private void notifyTestListenersOfEnd(SearchByClass testListeners) {
        log.info("Notifying test listeners of end of test");
        Iterator iter = testListeners.getSearchResults().iterator();
        while (iter.hasNext()) {
            TestListener tl = (TestListener) iter.next();
            try {
                if (host == null) {
                    tl.testEnded();
                } else {
                    tl.testEnded(host);
                }
            } catch (Exception e) {
                log.warn("Error encountered during shutdown of "+tl.toString(),e);
            }
        }
        log.info("Test has ended");
        if (host != null) {
            long now=System.currentTimeMillis();
            System.out.println("Finished the test on host " + host + " @ "+new Date(now)+" ("+now+")");
            if (exitAfterTest){
                exit();
            }
        }
    }

    private ListedHashTree cloneTree(ListedHashTree tree) {
        TreeCloner cloner = new TreeCloner(true);
        tree.traverse(cloner);
        return cloner.getClonedTree();
    }

    public void reset() {
        if (running) {
            stopTest();
        }
    }

    // Called by JMeter thread when it finishes
    public synchronized void threadFinished(JMeterThread thread) {
        try {
            allThreads.remove(thread);
            log.info("Ending thread " + thread.getThreadName());
            if (!startingGroups && allThreads.size() == 0 ) {
                log.info("Stopping test");
                stopTest();
            }
        } catch (Throwable e) {
            log.fatalError("Call to threadFinished should never throw an exception - this can deadlock JMeter",e);
        }
    }

    public synchronized void stopTest() {
        stopTest(true);
    }

    public synchronized void stopTest(boolean b) {
        Thread stopThread = new Thread(new StopTest(b));
        stopThread.start();
    }

    private class StopTest implements Runnable {
        private final boolean now;

        private StopTest(boolean b) {
            now = b;
        }

        public void run() {
            if (running) {
                running = false;
                if (now) {
                    tellThreadsToStop();
                } else {
                    stopAllThreads();
                }
                try {
                    Thread.sleep(10 * allThreads.size());
                } catch (InterruptedException e) {
                }
                boolean stopped = verifyThreadsStopped();
                if (stopped || now) {
                    notifyTestListenersOfEnd(testListenersSave);
                }
            }
        }
    }

    public void run() {
        log.info("Running the test!");
        running = true;

        SearchByClass testPlan = new SearchByClass(TestPlan.class);
        test.traverse(testPlan);
        Object[] plan = testPlan.getSearchResults().toArray();
        if (plan.length == 0) {
            System.err.println("Could not find the TestPlan!");
            log.error("Could not find the TestPlan!");
            System.exit(1);
        }
        if (((TestPlan) plan[0]).isSerialized()) {
            serialized = true;
        }
        JMeterContextService.startTest();
        try {
            PreCompiler compiler = new PreCompiler();
            test.traverse(compiler);
        } catch (RuntimeException e) {
            log.error("Error occurred compiling the tree:",e);
            JMeterUtils.reportErrorToUser("Error occurred compiling the tree: - see log file");
            return; // no point continuing
        }
        /**
         * Notification of test listeners needs to happen after function
         * replacement, but before setting RunningVersion to true.
         */
        SearchByClass testListeners = new SearchByClass(TestListener.class);
        test.traverse(testListeners);

        // Merge in any additional test listeners
        // currently only used by the function parser
        testListeners.getSearchResults().addAll(testList);
        testList.clear(); // no longer needed

        testListenersSave = testListeners;

        if (!startListenersLater ) { notifyTestListenersOfStart(testListeners); }
        test.traverse(new TurnElementsOn());
        if (startListenersLater) { notifyTestListenersOfStart(testListeners); }

        List testLevelElements = new LinkedList(test.list(test.getArray()[0]));
        removeThreadGroups(testLevelElements);
        SearchByClass searcher = new SearchByClass(ThreadGroup.class);
        test.traverse(searcher);
        TestCompiler.initialize();
        // for each thread group, generate threads
        // hand each thread the sampler controller
        // and the listeners, and the timer
        Iterator iter = searcher.getSearchResults().iterator();

        /*
         * Here's where the test really starts. Run a Full GC now: it's no harm
         * at all (just delays test start by a tiny amount) and hitting one too
         * early in the test can impair results for short tests.
         */
        System.gc();

        ListenerNotifier notifier = new ListenerNotifier();

        JMeterContextService.getContext().setSamplingStarted(true);
        int groupCount = 0;
        JMeterContextService.clearTotalThreads();
        startingGroups = true;
        while (running && iter.hasNext()) {// for each thread group
            groupCount++;
            ThreadGroup group = (ThreadGroup) iter.next();
            int numThreads = group.getNumThreads();
            JMeterContextService.addTotalThreads(numThreads);
            boolean onErrorStopTest = group.getOnErrorStopTest();
            boolean onErrorStopThread = group.getOnErrorStopThread();
            String groupName = group.getName();
            int rampUp = group.getRampUp();
            float perThreadDelay = ((float) (rampUp * 1000) / (float) numThreads);
            log.info("Starting " + numThreads + " threads for group " + groupName + ". Ramp up = " + rampUp + ".");

            if (onErrorStopTest) {
                log.info("Test will stop on error");
            } else if (onErrorStopThread) {
                log.info("Thread will stop on error");
            } else {
                log.info("Thread will continue on error");
            }

            ListedHashTree threadGroupTree = (ListedHashTree) searcher.getSubTree(group);
            threadGroupTree.add(group, testLevelElements);
            for (int i = 0; running && i < numThreads; i++) {
                final JMeterThread jmeterThread = new JMeterThread(cloneTree(threadGroupTree), this, notifier);
                jmeterThread.setThreadNum(i);
                jmeterThread.setThreadGroup(group);
                jmeterThread.setInitialContext(JMeterContextService.getContext());
                jmeterThread.setInitialDelay((int) (perThreadDelay * i));
                final String threadName = groupName + " " + (groupCount) + "-" + (i + 1);
                jmeterThread.setThreadName(threadName);

                scheduleThread(jmeterThread, group);

                // Set up variables for stop handling
                jmeterThread.setEngine(this);
                jmeterThread.setOnErrorStopTest(onErrorStopTest);
                jmeterThread.setOnErrorStopThread(onErrorStopThread);

                Thread newThread = new Thread(jmeterThread);
                newThread.setName(threadName);
                allThreads.put(jmeterThread, newThread);
                newThread.start();
            } // end of thread startup for this thread group
            if (serialized && !iter.hasNext()) {
                log.info("Waiting for thread group: "+groupName+" to finish before starting next group");
                while (running && allThreads.size() > 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        } // end of thread groups
        if (running) {
            log.info("All threads have been started");
        } else {
            log.info("Test stopped - no more threads will be started");
        }
        startingGroups = false;
    }

    /**
     * This will schedule the time for the JMeterThread.
     *
     * @param thread
     * @param group
     */
    private void scheduleThread(JMeterThread thread, ThreadGroup group) {
        // if true the Scheduler is enabled
        if (group.getScheduler()) {
            long now = System.currentTimeMillis();
            // set the start time for the Thread
            if (group.getDelay() > 0) {// Duration is in seconds
                thread.setStartTime(group.getDelay() * 1000 + now);
            } else {
                long start = group.getStartTime();
                if (start < now) {
                    start = now; // Force a sensible start time
                }
                thread.setStartTime(start);
            }

            // set the endtime for the Thread
            if (group.getDuration() > 0) {// Duration is in seconds
                thread.setEndTime(group.getDuration() * 1000 + (thread.getStartTime()));
            } else {
                thread.setEndTime(group.getEndTime());
            }

            // Enables the scheduler
            thread.setScheduled(true);
        }
    }

    private boolean verifyThreadsStopped() {
        boolean stoppedAll = true;
        Iterator iter = new HashSet(allThreads.keySet()).iterator();
        while (iter.hasNext()) {
            Thread t = (Thread) allThreads.get(iter.next());
            if (t != null && t.isAlive()) {
                try {
                    t.join(WAIT_TO_DIE);
                } catch (InterruptedException e) {
                }
                if (t.isAlive()) {
                    stoppedAll = false;
                    log.warn("Thread won't die: " + t.getName());
                }
            }
        }
        return stoppedAll;
    }

    private void tellThreadsToStop() {
        Iterator iter = new HashSet(allThreads.keySet()).iterator();
        while (iter.hasNext()) {
            JMeterThread item = (JMeterThread) iter.next();
            item.stop(); // set stop flag
            item.interrupt(); // interrupt sampler if possible
            Thread t = (Thread) allThreads.get(item);
            if (t != null) {
                t.interrupt(); // also interrupt JVM thread
            } else {
                log.warn("Lost thread: " + item.getThreadName());
                allThreads.remove(item);
            }
        }
    }

    public void askThreadsToStop() {
        engine.stopTest(false);
    }

    public void askThreadsToStopNow() {
        engine.stopTest(true);
    }

    private void stopAllThreads() {
        Iterator iter = new HashSet(allThreads.keySet()).iterator();
        while (iter.hasNext()) {
            JMeterThread item = (JMeterThread) iter.next();
            item.stop();
        }
    }

    // Remote exit
    public void exit() {
        // Needs to be run in a separate thread to allow RMI call to return OK
        Thread t = new Thread() {
            public void run() {
                // log.info("Pausing");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                log.info("Bye");
                System.exit(0);
            }
        };
        log.info("Starting Closedown");
        t.start();
    }

    public void setProperties(Properties p) {
        log.info("Applying properties "+p);
        JMeterUtils.getJMeterProperties().putAll(p);
    }
}
