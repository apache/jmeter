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

import org.apache.jmeter.util.JMeterUtils;

/**
 * Provides context service for JMeter threads.
 * Keeps track of active and total thread counts.
 */
public final class JMeterContextService {
    private static final ThreadLocal<JMeterContext> threadContext = new ThreadLocal<JMeterContext>() {
        @Override
        public JMeterContext initialValue() {
            return new JMeterContext();
        }
    };

    //@GuardedBy(JMeterContextService.class)
    private static long testStart = 0;

    //@GuardedBy(JMeterContextService.class)
    private static int numberOfActiveThreads = 0;

    //@GuardedBy(JMeterContextService.class)
    private static int numberOfThreadsStarted = 0;

    //@GuardedBy(JMeterContextService.class)
    private static int numberOfThreadsFinished = 0;

    //@GuardedBy(JMeterContextService.class)
    private static int totalThreads = 0;
    
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
     * Replace Thread Context by the parameter. Currently only used by the
     * private class <code>ASyncSample</code> in
     * {@link org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase
     * HTTPSamplerBase}
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
        if (testStart == 0) {
            numberOfActiveThreads = 0;
            testStart = System.currentTimeMillis();
            JMeterUtils.setProperty("TESTSTART.MS",Long.toString(testStart));// $NON-NLS-1$
        }
    }

    /**
     * Increment number of active threads.
     */
    static synchronized void incrNumberOfThreads() {
        numberOfActiveThreads++;
        numberOfThreadsStarted++;
    }

    /**
     * Decrement number of active threads.
     */
    static synchronized void decrNumberOfThreads() {
        numberOfActiveThreads--;
        numberOfThreadsFinished++;
    }

    /**
     * Get the number of currently active threads
     * @return active thread count
     */
    public static synchronized int getNumberOfThreads() {
        return numberOfActiveThreads;
    }

    // return all the associated counts together
    public static synchronized ThreadCounts getThreadCounts() {
        return new ThreadCounts(numberOfActiveThreads, numberOfThreadsStarted, numberOfThreadsFinished);
    }

    /**
     * Called by MainFrame#testEnded().
     * Clears start time field.
     */
    public static synchronized void endTest() {
        testStart = 0;
    }

    public static synchronized long getTestStartTime() {
        return testStart;
    }

    /**
     * Get the total number of threads (&gt;= active)
     * @return total thread count
     */
    public static synchronized int getTotalThreads() {
        return totalThreads;
    }

    /**
     * Update the total number of threads
     * @param thisGroup number of threads in this thread group
     */
    public static synchronized void addTotalThreads(int thisGroup) {
        totalThreads += thisGroup;
    }

    /**
     * Set total threads to zero; also clears started and finished counts
     */
    public static synchronized void clearTotalThreads() {
        totalThreads = 0;
        numberOfThreadsStarted = 0;
        numberOfThreadsFinished = 0;
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
        
        ThreadCounts (int active, int started, int finished) {
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
}
