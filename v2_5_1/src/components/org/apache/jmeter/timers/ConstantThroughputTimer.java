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

package org.apache.jmeter.timers;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This class implements a constant throughput timer. A Constant Throughtput
 * Timer paces the samplers under its influence so that the total number of
 * samples per unit of time approaches a given constant as much as possible.
 *
 * There are two different ways of pacing the requests:
 * - delay each thread according to when it last ran
 * - delay each thread according to when any thread last ran
 */
public class ConstantThroughputTimer extends AbstractTestElement implements Timer, TestListener, TestBean {
    private static final long serialVersionUID = 3;

    private static class ThroughputInfo{
        final Object MUTEX = new Object();
        long lastScheduledTime = 0;
    }
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final double MILLISEC_PER_MIN = 60000.0;

    /**
     * Target time for the start of the next request. The delay provided by the
     * timer will be calculated so that the next request happens at this time.
     */
    private long previousTime = 0;

    private String calcMode; // String representing the mode
                                // (Locale-specific)

    private int modeInt; // mode as an integer

    /**
     * Desired throughput, in samples per minute.
     */
    private double throughput;

    //For calculating throughput across all threads
    private final static ThroughputInfo allThreadsInfo = new ThroughputInfo();

    //For holding the ThrougputInfo objects for all ThreadGroups. Keyed by AbstractThreadGroup objects
    private final static ConcurrentMap<AbstractThreadGroup, ThroughputInfo> threadGroupsInfoMap =
        new ConcurrentHashMap<AbstractThreadGroup, ThroughputInfo>();


    /**
     * Constructor for a non-configured ConstantThroughputTimer.
     */
    public ConstantThroughputTimer() {
    }

    /**
     * Sets the desired throughput.
     *
     * @param throughput
     *            Desired sampling rate, in samples per minute.
     */
    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }

    /**
     * Gets the configured desired throughput.
     *
     * @return the rate at which samples should occur, in samples per minute.
     */
    public double getThroughput() {
        return throughput;
    }

    public String getCalcMode() {
        return calcMode;
    }

    // Needed by test code
    int getCalcModeInt() {
        return modeInt;
    }

    public void setCalcMode(String mode) {
        this.calcMode = mode;
        // TODO find better way to get modeInt
        this.modeInt = ConstantThroughputTimerBeanInfo.getCalcModeAsInt(calcMode);
    }

    /**
     * Retrieve the delay to use during test execution.
     *
     * @see org.apache.jmeter.timers.Timer#delay()
     */
    public long delay() {
        long currentTime = System.currentTimeMillis();

        /*
         * If previous time is zero, then target will be in the past.
         * This is what we want, so first sample is run without a delay.
        */
        long currentTarget = previousTime  + calculateDelay();
        if (currentTime > currentTarget) {
            // We're behind schedule -- try to catch up:
            previousTime = currentTime;
            return 0;
        }
        previousTime = currentTarget;
        return currentTarget - currentTime;
    }

    /**
     * @param currentTime
     * @return new Target time
     */
    // TODO - is this used? (apart from test code)
    protected long calculateCurrentTarget(long currentTime) {
        return currentTime + calculateDelay();
    }

    // Calculate the delay based on the mode
    private long calculateDelay() {
        long delay = 0;
        // N.B. we fetch the throughput each time, as it may vary during a test
        double msPerRequest = (MILLISEC_PER_MIN / getThroughput());
        switch (modeInt) {
        case 1: // Total number of threads
            delay = (long) (JMeterContextService.getNumberOfThreads() * msPerRequest);
            break;

        case 2: // Active threads in this group
            delay = (long) (JMeterContextService.getContext().getThreadGroup().getNumberOfThreads() * msPerRequest);
            break;

        case 3: // All threads - alternate calculation
            delay = calculateSharedDelay(allThreadsInfo,(long) msPerRequest);
            break;

        case 4: //All threads in this group - alternate calculation
            final org.apache.jmeter.threads.AbstractThreadGroup group =
                JMeterContextService.getContext().getThreadGroup();
            ThroughputInfo groupInfo = threadGroupsInfoMap.get(group);
            if (groupInfo == null) {
                groupInfo = new ThroughputInfo();
                ThroughputInfo previous = threadGroupsInfoMap.putIfAbsent(group, groupInfo);
                if (previous != null) { // We did not replace the entry
                    groupInfo = previous; // so use the existing one
                }
            }
            delay = calculateSharedDelay(groupInfo,(long) msPerRequest);
            break;

        default: // e.g. 0
            delay = (long) msPerRequest; // i.e. * 1
            break;
        }
        return delay;
    }

    private long calculateSharedDelay(ThroughputInfo info, long milliSecPerRequest) {
        final long now = System.currentTimeMillis();
        final long calculatedDelay;

        //Synchronize on the info object's MUTEX to ensure
        //Multiple threads don't update the scheduled time simultaneously
        synchronized (info.MUTEX) {
            final long nextRequstTime = info.lastScheduledTime + milliSecPerRequest;
            info.lastScheduledTime = Math.max(now, nextRequstTime);
            calculatedDelay = info.lastScheduledTime - now;
        }

        return Math.max(calculatedDelay, 0);
    }

    private synchronized void reset() {
        allThreadsInfo.lastScheduledTime = 0;
        threadGroupsInfoMap.clear();
        previousTime = 0;
    }

    /**
     * Provide a description of this timer class.
     *
     * TODO: Is this ever used? I can't remember where. Remove if it isn't --
     * TODO: or obtain text from bean's displayName or shortDescription.
     *
     * @return the description of this timer class.
     */
    @Override
    public String toString() {
        return JMeterUtils.getResString("constant_throughput_timer_memo"); //$NON-NLS-1$
    }

    /**
     * Get the timer ready to compute delays for a new test.
     * <p>
     * {@inheritDoc}
     */
    public void testStarted()
    {
        log.debug("Test started - reset throughput calculation.");
        reset();
    }

    /**
     * {@inheritDoc}
     */
    public void testEnded() {
    }

    /**
     * {@inheritDoc}
     */
    public void testStarted(String host) {
        testStarted();
    }

    /**
     * {@inheritDoc}
     */
    public void testEnded(String host) {
    }

    /**
     * {@inheritDoc}
     */
    public void testIterationStart(LoopIterationEvent event) {
    }
}