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

package org.apache.jmeter.timers;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.GenericTestBeanCustomizer;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.DoubleProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.IdentityKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a constant throughput timer. A Constant Throughput
 * Timer paces the samplers under its influence so that the total number of
 * samples per unit of time approaches a given constant as much as possible.
 *
 * There are two different ways of pacing the requests:
 * - delay each thread according to when it last ran
 * - delay each thread according to when any thread last ran
 */
@GUIMenuSortOrder(4)
@TestElementMetadata(labelResource = "displayName")
public class ConstantThroughputTimer extends AbstractTestElement implements Timer, TestBean {
    private static final long serialVersionUID = 4;

    private static class ThroughputInfo{
        final Object MUTEX = new Object();
        long lastScheduledTime = 0;
    }
    private static final Logger log = LoggerFactory.getLogger(ConstantThroughputTimer.class);
    private static final AtomicLong PREV_TEST_STARTED = new AtomicLong(0L);

    private static final double MILLISEC_PER_MIN = 60000.0;

    private static final Mode DEFAULT_CALC_MODE = Mode.ThisThreadOnly;

    // TODO: most props use class simpleName as prefix but that would break backward compatiblity here
    public static final String THROUGHPUT = "throughput";
    public static final String CALC_MODE = "calcMode";

    /**
     * This enum defines the calculation modes used by the ConstantThroughputTimer.
     */
    public enum Mode {
        ThisThreadOnly("calcMode.1"), // NOSONAR Keep naming for compatibility
        AllActiveThreads("calcMode.2"), // NOSONAR Keep naming for compatibility
        AllActiveThreadsInCurrentThreadGroup("calcMode.3"), // NOSONAR Keep naming for compatibility
        AllActiveThreads_Shared("calcMode.4"), // NOSONAR Keep naming for compatibility
        AllActiveThreadsInCurrentThreadGroup_Shared("calcMode.5"), // NOSONAR Keep naming for compatibility
        ;

        private final String propertyName; // The property name to be used to look up the display string
        // Enum#values() clones the array, and we don't want to pay that cost as we know we don't modify the array
        private static final Mode[] CACHED_VALUES = values();

        Mode(String name) {
            this.propertyName = name;
        }

        @Override
        public String toString() {
            return propertyName;
        }
    }

    /**
     * Target time for the start of the next request. The delay provided by the
     * timer will be calculated so that the next request happens at this time.
     */
    private long previousTime = 0;

    //For calculating throughput across all threads
    private static final ThroughputInfo allThreadsInfo = new ThroughputInfo();

    //For holding the ThroughputInfo objects for all ThreadGroups. Keyed by AbstractThreadGroup objects
    //TestElements can't be used as keys in HashMap.
    private static final ConcurrentMap<IdentityKey<AbstractThreadGroup>, ThroughputInfo> threadGroupsInfoMap =
            new ConcurrentHashMap<>();

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
        setProperty(new DoubleProperty(THROUGHPUT, throughput));
    }

    /**
     * Gets the configured desired throughput.
     *
     * @return the rate at which samples should occur, in samples per minute.
     */
    public double getThroughput() {
        return getPropertyAsDouble(THROUGHPUT);
    }

    public int getCalcMode() {
        return getPropertyAsInt(CALC_MODE, DEFAULT_CALC_MODE.ordinal());
    }

    public void setCalcMode(int mode) {
        Mode resolved = Mode.CACHED_VALUES[mode];
        setProperty(new IntegerProperty(CALC_MODE, resolved.ordinal()));
    }

    /**
     * Retrieve the delay to use during test execution.
     *
     * @see org.apache.jmeter.timers.Timer#delay()
     */
    @Override
    public long delay() {
        long currentTime = System.currentTimeMillis();

        /*
         * If previous time is zero, then target will be in the past.
         * This is what we want, so first sample is run without a delay.
        */
        long currentTarget = previousTime  + calculateDelay();
        if (currentTime > currentTarget) {
            // We're behind schedule -- try to catch up:
            previousTime = currentTime; // assume the sample will run immediately
            return 0;
        }
        previousTime = currentTarget; // assume the sample will run as soon as the delay has expired
        return currentTarget - currentTime;
    }

    /**
     * Calculate the target time by adding the result of private method
     * <code>calculateDelay()</code> to the given <code>currentTime</code>
     *
     * @param currentTime
     *            time in ms
     * @return new Target time
     */
    // TODO - is this used? (apart from test code)
    protected long calculateCurrentTarget(long currentTime) {
        return currentTime + calculateDelay();
    }

    // Calculate the delay based on the mode
    private long calculateDelay() {
        long testStarted = JMeterContextService.getTestStartTime();
        long prevStarted = PREV_TEST_STARTED.get();
        if (prevStarted != testStarted && PREV_TEST_STARTED.compareAndSet(prevStarted, testStarted)) {
            // Reset counters if we are calculating throughput for a new test, see https://github.com/apache/jmeter/issues/6165
            reset();
        }

        long delay;
        // N.B. we fetch the throughput each time, as it may vary during a test
        double msPerRequest = MILLISEC_PER_MIN / getThroughput();
        switch (getMode()) {
        case AllActiveThreads: // Total number of threads
            delay = Math.round(JMeterContextService.getNumberOfThreads() * msPerRequest);
            break;

        case AllActiveThreadsInCurrentThreadGroup: // Active threads in this group
            delay = Math.round(JMeterContextService.getContext().getThreadGroup().getNumberOfThreads() * msPerRequest);
            break;

        case AllActiveThreads_Shared: // All threads - alternate calculation
            delay = calculateSharedDelay(allThreadsInfo,Math.round(msPerRequest));
            break;

        case AllActiveThreadsInCurrentThreadGroup_Shared: //All threads in this group - alternate calculation
            final org.apache.jmeter.threads.AbstractThreadGroup group =
                JMeterContextService.getContext().getThreadGroup();
            IdentityKey<AbstractThreadGroup> key = new IdentityKey<>(group);
            ThroughputInfo groupInfo = threadGroupsInfoMap.get(key);
            if (groupInfo == null) {
                groupInfo = threadGroupsInfoMap.computeIfAbsent(key, (k) -> new ThroughputInfo());
            }
            delay = calculateSharedDelay(groupInfo,Math.round(msPerRequest));
            break;

        case ThisThreadOnly:
        default: // e.g. 0
            delay = Math.round(msPerRequest); // i.e. * 1
            break;
        }
        return delay;
    }

    private static long calculateSharedDelay(ThroughputInfo info, long milliSecPerRequest) {
        final long now = System.currentTimeMillis();
        final long calculatedDelay;

        //Synchronize on the info object's MUTEX to ensure
        //Multiple threads don't update the scheduled time simultaneously
        synchronized (info.MUTEX) {
            final long nextRequestTime = info.lastScheduledTime + milliSecPerRequest;
            info.lastScheduledTime = Math.max(now, nextRequestTime);
            calculatedDelay = info.lastScheduledTime - now;
        }

        return Math.max(calculatedDelay, 0);
    }

    private void reset() {
        synchronized (allThreadsInfo.MUTEX) {
            allThreadsInfo.lastScheduledTime = 0;
        }
        threadGroupsInfoMap.clear();
        // no need to sync as one per instance
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
     * Override the setProperty method in order to convert
     * the original String calcMode property.
     * This used the locale-dependent display value, so caused
     * problems when the language was changed.
     * Note that the calcMode StringProperty is replaced with an IntegerProperty
     * so the conversion only needs to happen once.
     */
    @Override
    public void setProperty(JMeterProperty property) {
        if (property instanceof StringProperty) {
            final String pn = property.getName();
            if (pn.equals("calcMode")) {
                final Object objectValue = property.getObjectValue();
                try {
                    final BeanInfo beanInfo = Introspector.getBeanInfo(this.getClass());
                    final ResourceBundle rb = (ResourceBundle) beanInfo.getBeanDescriptor().getValue(GenericTestBeanCustomizer.RESOURCE_BUNDLE);
                    for(Enum<Mode> e : Mode.CACHED_VALUES) {
                        final String propName = e.toString();
                        if (objectValue.equals(rb.getObject(propName))) {
                            final int tmpMode = e.ordinal();
                            log.debug("Converted {}={} to mode={} using Locale: {}", pn, objectValue, tmpMode,
                                    rb.getLocale());
                            super.setProperty(pn, tmpMode);
                            return;
                        }
                    }
                    log.warn("Could not convert {}={} using Locale: {}", pn, objectValue, rb.getLocale());
                } catch (IntrospectionException e) {
                    log.error("Could not find BeanInfo", e);
                }
            }
        }
        super.setProperty(property);
    }

    // For access from test code
    Mode getMode() {
        int mode = getCalcMode();
        return Mode.CACHED_VALUES[mode];
    }

    // For access from test code
    void setMode(Mode newMode) {
        setCalcMode(newMode.ordinal());
    }
}
