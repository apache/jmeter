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

package org.apache.jmeter.reporters;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterContextService.ThreadCounts;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.RunningSample;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * Generate a summary of the test run so far to the log file and/or standard
 * output. Both running and differential totals are shown. Output is generated
 * every n seconds (default 3 minutes) on the appropriate time boundary, so that
 * multiple test runs on the same time will be synchronised.
 *
 * This is mainly intended for batch (non-GUI) runs
 *
 * Note that the RunningSample start and end times relate to the samples,
 * not the reporting interval.
 *
 * Since the first sample in a delta is likely to have started in the previous reporting interval,
 * this means that the delta interval is likely to be longer than the reporting interval.
 *
 * Also, the sum of the delta intervals will be larger than the overall elapsed time.
 *
 * Data is accumulated according to the test element name.
 *
 */
public class Summariser extends AbstractTestElement
    implements Serializable, SampleListener, TestStateListener, NoThreadClone, Remoteable {

    /*
     * N.B. NoThreadClone is used to ensure that the testStarted() methods will share the same
     * instance as the sampleOccured() methods, so the testStarted() method can fetch the
     * Totals accumulator object for the samples to be stored in.
     */

    private static final long serialVersionUID = 233L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    /** interval between summaries (in seconds) default 30 seconds */
    private static final long INTERVAL = JMeterUtils.getPropDefault("summariser.interval", 30); //$NON-NLS-1$

    /** Write messages to log file ? */
    private static final boolean TOLOG = JMeterUtils.getPropDefault("summariser.log", true); //$NON-NLS-1$

    /** Write messages to System.out ? */
    private static final boolean TOOUT = JMeterUtils.getPropDefault("summariser.out", true); //$NON-NLS-1$

    /*
     * Ensure that a report is not skipped if we are slightly late in checking
     * the time.
     */
    private static final int INTERVAL_WINDOW = 5; // in seconds

    /**
     * Lock used to protect accumulators update + instanceCount update
     */
    private static final Object lock = new Object();

    /*
     * This map allows summarisers with the same name to contribute to the same totals.
     */
    //@GuardedBy("accumulators") - needed to ensure consistency between this and instanceCount
    private static final Map<String, Totals> accumulators = new ConcurrentHashMap<String, Totals>();

    //@GuardedBy("accumulators")
    private static int instanceCount; // number of active tests

    /*
     * Cached copy of Totals for this instance.
     * The variables do not need to be synchronised,
     * as they are not shared between threads
     * However the contents do need to be synchronized.
     */
    //@GuardedBy("myTotals")
    private transient Totals myTotals = null;

    // Name of the accumulator. Set up by testStarted().
    private transient String myName;

    /*
     * Constructor is initially called once for each occurrence in the test plan.
     * For GUI, several more instances are created.
     * Then clear is called at start of test.
     * Called several times during test startup.
     * The name will not necessarily have been set at this point.
     */
    public Summariser() {
        super();
        synchronized (lock) {
            accumulators.clear();
            instanceCount=0;
        }
    }

    /**
     * Constructor for use during startup (intended for non-GUI use)
     *
     * @param name of summariser
     */
    public Summariser(String name) {
        this();
        setName(name);
    }

    /*
     * Contains the items needed to collect stats for a summariser
     *
     */
    private static class Totals {

        /** Time of last summary (to prevent double reporting) */
        private long last = 0;

        private final RunningSample delta = new RunningSample("DELTA",0);

        private final RunningSample total = new RunningSample("TOTAL",0);

        /**
         * Add the delta values to the total values and clear the delta
         */
        private void moveDelta() {
            total.addSample(delta);
            delta.clear();
        }
    }

    /**
     * Accumulates the sample in two SampleResult objects - one for running
     * totals, and the other for deltas.
     *
     * @see org.apache.jmeter.samplers.SampleListener#sampleOccurred(org.apache.jmeter.samplers.SampleEvent)
     */
    @Override
    public void sampleOccurred(SampleEvent e) {
        SampleResult s = e.getResult();

        long now = System.currentTimeMillis() / 1000;// in seconds

        RunningSample myDelta = null;
        RunningSample myTotal = null;
        boolean reportNow = false;

        /*
         * Have we reached the reporting boundary?
         * Need to allow for a margin of error, otherwise can miss the slot.
         * Also need to check we've not hit the window already
         */
        synchronized (myTotals) {
            if (s != null) {
                myTotals.delta.addSample(s);
            }

            if ((now > myTotals.last + INTERVAL_WINDOW) && (now % INTERVAL <= INTERVAL_WINDOW)) {
                reportNow = true;

                // copy the data to minimise the synch time
                myDelta = new RunningSample(myTotals.delta);
                myTotals.moveDelta();
                myTotal = new RunningSample(myTotals.total);

                myTotals.last = now; // stop double-reporting
            }
        }
        if (reportNow) {
            String str;
            str = format(myName, myDelta, "+");
            if (TOLOG) {
                log.info(str);
            }
            if (TOOUT) {
                System.out.println(str);
            }

            // Only if we have updated them
            if (myTotal != null && myDelta != null &&myTotal.getNumSamples() != myDelta.getNumSamples()) {
                str = format(myName, myTotal, "=");
                if (TOLOG) {
                    log.info(str);
                }
                if (TOOUT) {
                    System.out.println(str);
                }
            }
        }
    }

    private static StringBuilder longToSb(StringBuilder sb, long l, int len) {
        sb.setLength(0);
        sb.append(l);
        return JOrphanUtils.rightAlign(sb, len);
    }

    private static StringBuilder doubleToSb(DecimalFormat dfDouble, StringBuilder sb, double d, int len, int frac) {
        sb.setLength(0);
        dfDouble.setMinimumFractionDigits(frac);
        dfDouble.setMaximumFractionDigits(frac);
        sb.append(dfDouble.format(d));
        return JOrphanUtils.rightAlign(sb, len);
    }

    /**
     * @param myTotal
     * @param string
     * @return the sunnary information
     */
    private static String format(String name, RunningSample s, String type) {
        DecimalFormat dfDouble = new DecimalFormat("#0.0"); // $NON-NLS-1$
        StringBuilder tmp = new StringBuilder(20); // for intermediate use
        StringBuilder sb = new StringBuilder(100); // output line buffer
        sb.append(name);
        sb.append(" ");
        sb.append(type);
        sb.append(" ");
        sb.append(longToSb(tmp, s.getNumSamples(), 6));
        sb.append(" in ");
        long elapsed = s.getElapsed();
        long elapsedSec = (elapsed + 500) / 1000; // rounded seconds
        if (elapsedSec > 100       // No point displaying decimals (less than 1% error)
         || (elapsed - elapsedSec * 1000) < 50 // decimal would be zero
         ) {
            sb.append(longToSb(tmp, elapsedSec, 5));
        } else {
            double elapsedSecf = elapsed / 1000.0d; // fractional seconds
            sb.append(doubleToSb(dfDouble, tmp, elapsedSecf, 5, 1)); // This will round
        }
        sb.append("s = ");
        if (elapsed > 0) {
            sb.append(doubleToSb(dfDouble, tmp, s.getRate(), 6, 1));
        } else {
            sb.append("******");// Rate is effectively infinite
        }
        sb.append("/s Avg: ");
        sb.append(longToSb(tmp, s.getAverage(), 5));
        sb.append(" Min: ");
        sb.append(longToSb(tmp, s.getMin(), 5));
        sb.append(" Max: ");
        sb.append(longToSb(tmp, s.getMax(), 5));
        sb.append(" Err: ");
        sb.append(longToSb(tmp, s.getErrorCount(), 5));
        sb.append(" (");
        sb.append(s.getErrorPercentageString());
        sb.append(")");
        if ("+".equals(type)) {
            ThreadCounts tc = JMeterContextService.getThreadCounts();
            sb.append(" Active: ");
            sb.append(tc.activeThreads);
            sb.append(" Started: ");
            sb.append(tc.startedThreads);
            sb.append(" Finished: ");
            sb.append(tc.finishedThreads);
        }
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public void sampleStarted(SampleEvent e) {
        // not used
    }

    /** {@inheritDoc} */
    @Override
    public void sampleStopped(SampleEvent e) {
        // not used
    }

    /*
     * The testStarted/testEnded methods are called at the start and end of a test.
     *
     * However, when a test is run on multiple nodes, there is no guarantee that all the
     * testStarted() methods will be called before all the threadStart() or sampleOccurred()
     * methods for other threads - nor that testEnded() will only be called after all
     * sampleOccurred() calls. The ordering is only guaranteed within a single test.
     *
     */


    /** {@inheritDoc} */
    @Override
    public void testStarted() {
        testStarted("local");
    }

    /** {@inheritDoc} */
    @Override
    public void testEnded() {
        testEnded("local");
    }

    /**
     * Called once for each Summariser in the test plan.
     * There may be more than one summariser with the same name,
     * however they will all be called before the test proper starts.
     * <p>
     * However, note that this applies to a single test only.
     * When running in client-server mode, testStarted() may be
     * invoked after sampleOccurred().
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void testStarted(String host) {
        synchronized (lock) {
            myName = getName();
            myTotals = accumulators.get(myName);
            if (myTotals == null){
                myTotals = new Totals();
                accumulators.put(myName, myTotals);
            }
            instanceCount++;
        }
    }

    /**
     * Called from a different thread as testStarted() but using the same instance.
     * So synch is needed to fetch the accumulator, and the myName field will already be set up.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void testEnded(String host) {
        Set<Entry<String, Totals>> totals = null;
        synchronized (lock) {
            instanceCount--;
            if (instanceCount <= 0){
                totals = accumulators.entrySet();
            }
        }
        if (totals == null) {// We're not done yet
            return;
        }
        for(Map.Entry<String, Totals> entry : totals){
            String str;
            String name = entry.getKey();
            Totals total = entry.getValue();
            // Only print final delta if there were some samples in the delta
            // and there has been at least one sample reported previously
            if (total.delta.getNumSamples() > 0 && total.total.getNumSamples() >  0) {
                str = format(name, total.delta, "+");
                if (TOLOG) {
                    log.info(str);
                }
                if (TOOUT) {
                    System.out.println(str);
                }
            }
            total.moveDelta();
            str = format(name, total.total, "=");
            if (TOLOG) {
                log.info(str);
            }
            if (TOOUT) {
                System.out.println(str);
            }
        }
    }

}