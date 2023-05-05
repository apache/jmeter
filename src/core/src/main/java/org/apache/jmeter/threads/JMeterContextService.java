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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jmeter.util.JMeterUtils;

/**
 * Provides context service for JMeter threads.
 * Keeps track of active and total thread counts.
 */
public final class JMeterContextService {
    private static final ThreadLocal<JMeterContext> threadContext = ThreadLocal.withInitial(JMeterContext::new);

    private static final AtomicLong testStart = new AtomicLong();

    private static final AtomicInteger NUMBER_OF_ACTIVE_THREADS = new AtomicInteger();

    private static final AtomicInteger NUMBER_OF_THREADS_STARTED = new AtomicInteger();

    private static final AtomicInteger NUMBER_OF_THREADS_FINISHED = new AtomicInteger();

    private static final AtomicInteger TOTAL_THREADS = new AtomicInteger();

    private static UnmodifiableJMeterVariables variables;


    /**
     * Private constructor to prevent instantiation.
     */
    private JMeterContextService() {
    }

    /**
     * Gives access to the current thread context.
     *
     * @return the current thread Context
     */
    public static JMeterContext getContext() {
        return threadContext.get();
    }

    /**
     * Allows the thread Context to be completely cleared.
     * <br/>
     * Invokes {@link ThreadLocal#remove()}.
     */
    static void removeContext(){ // Currently only used by JMeterThread
        threadContext.remove();
    }

    /**
     * Replace Thread Context by the parameter.
     *
     * @param context
     *            {@link JMeterContext}
     */
    public static void replaceContext(JMeterContext context) {
        threadContext.remove();
        threadContext.set(context);
    }
    /**
     * Method is called by the JMeterEngine class when a test run is started.
     * Zeroes numberOfActiveThreads.
     * Saves current time in a field and in the JMeter property "TESTSTART.MS"
     */
    public static synchronized void startTest() {
        if (testStart.get() == 0) {
            NUMBER_OF_ACTIVE_THREADS.set(0);
            testStart.set(System.currentTimeMillis());
            JMeterUtils.setProperty("TESTSTART.MS", Long.toString(testStart.get()));// $NON-NLS-1$
        }
    }

    /**
     * Increment number of active threads.
     */
    static void incrNumberOfThreads() {
        NUMBER_OF_ACTIVE_THREADS.incrementAndGet();
        NUMBER_OF_THREADS_STARTED.incrementAndGet();
    }

    /**
     * Decrement number of active threads.
     */
    static void decrNumberOfThreads() {
        NUMBER_OF_ACTIVE_THREADS.decrementAndGet();
        NUMBER_OF_THREADS_FINISHED.incrementAndGet();
    }

    /**
     * Get the number of currently active threads
     * @return active thread count
     */
    public static int getNumberOfThreads() {
        return NUMBER_OF_ACTIVE_THREADS.get();
    }

    // return all the associated counts together
    public static ThreadCounts getThreadCounts() {
        return new ThreadCounts(NUMBER_OF_ACTIVE_THREADS.get(), NUMBER_OF_THREADS_STARTED.get(), NUMBER_OF_THREADS_FINISHED.get());
    }

    /**
     * Called by MainFrame#testEnded().
     * Clears start time field.
     */
    public static synchronized void endTest() {
        testStart.set(0);
        resetClientSideVariables();
    }

    public static long getTestStartTime() {
        return testStart.get();
    }

    /**
     * Get the total number of threads (&gt;= active)
     * @return total thread count
     */
    public static int getTotalThreads() {
        return TOTAL_THREADS.get();
    }

    /**
     * Update the total number of threads
     * @param thisGroup number of threads in this thread group
     */
    public static void addTotalThreads(int thisGroup) {
        TOTAL_THREADS.addAndGet(thisGroup);
    }

    /**
     * Set total threads to zero; also clears started and finished counts
     */
    public static void clearTotalThreads() {
        TOTAL_THREADS.set(0);
        NUMBER_OF_THREADS_STARTED.set(0);
        NUMBER_OF_THREADS_FINISHED.set(0);
    }

    /**
     * Get all variables accessible for JMeter client in a distributed test
     * (only test plan and user defined variables)
     * Note this is a read-only collection
     * @return {@link JMeterVariables} available for JMeter client
     */
    public static JMeterVariables getClientSideVariables() {
        return variables;
    }

    public static class ThreadCounts {

        public final int activeThreads;

        public final int startedThreads;

        public final int finishedThreads;

        ThreadCounts(int active, int started, int finished) {
            activeThreads = active;
            startedThreads = started;
            finishedThreads = finished;
        }
    }

    /**
     * Set variables for JMeter client in a distributed test (INTERNAL API)
     * @param clientSideVariables {@link JMeterVariables}
     */
    public static void initClientSideVariables(JMeterVariables clientSideVariables) {
        JMeterContextService.variables = new UnmodifiableJMeterVariables(clientSideVariables);
    }

    /**
     * Reset client side variables in a distributed mode
     */
    public static void resetClientSideVariables() {
        JMeterContextService.variables = null;
    }
}
