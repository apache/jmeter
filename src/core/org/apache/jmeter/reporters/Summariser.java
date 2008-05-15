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
import java.util.Hashtable;
import java.util.Map;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.ThreadListener;
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
 */
public class Summariser extends AbstractTestElement implements Serializable, SampleListener, TestListener, ThreadListener {
	private static final Logger log = LoggingManager.getLoggerForClass();

	/** interval between summaries (in seconds) default 3 minutes */
	private static final long INTERVAL = JMeterUtils.getPropDefault("summariser.interval", 3 * 60); //$NON-NLS-1$

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
	 * Summariser elements are cloned for each thread in each group; this Map is
	 * used to allow them to share the same statistics. The key is the
	 * Summariser name, so all Summarisers with the same name will use the same
	 * accumulators.
	 */
	//@GuardedBy("accumulators")
	private static final Hashtable accumulators = new Hashtable();

    /*
     * Cached copy of Totals for this instance.
     * The variables do not need to be synchronised,
     * as they are not shared between threads
     * However the contents do need to be synchronized.
     */
	//@GuardedBy("myTotals")
    private transient Totals myTotals = null;

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

	private static StringBuffer longToSb(StringBuffer sb, long l, int len) {
		sb.setLength(0);
		sb.append(l);
		return JOrphanUtils.rightAlign(sb, len);
	}

	private static final DecimalFormat dfDouble = new DecimalFormat("#0.0"); // $NON-NLS-1$

	private static StringBuffer doubleToSb(StringBuffer sb, double d, int len, int frac) {
		sb.setLength(0);
		dfDouble.setMinimumFractionDigits(frac);
		dfDouble.setMaximumFractionDigits(frac);
		sb.append(dfDouble.format(d));
		return JOrphanUtils.rightAlign(sb, len);
	}

	/**
	 * @param myTotal
	 * @param string
	 * @return
	 */
	private String format(String name, RunningSample s, String type) {
		StringBuffer tmp = new StringBuffer(20); // for intermediate use
		StringBuffer sb = new StringBuffer(100); // output line buffer
		sb.append(name);
		sb.append(" ");
		sb.append(type);
		sb.append(" ");
		sb.append(longToSb(tmp, s.getNumSamples(), 5));
		sb.append(" in ");
		long elapsed = s.getElapsed();
		sb.append(doubleToSb(tmp, elapsed / 1000.0, 5, 1));
		sb.append("s = ");
		if (elapsed > 0) {
			sb.append(doubleToSb(tmp, s.getRate(), 6, 1));
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
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.samplers.SampleListener#sampleStarted(org.apache.jmeter.samplers.SampleEvent)
	 */
	public void sampleStarted(SampleEvent e) {
		// not used
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.samplers.SampleListener#sampleStopped(org.apache.jmeter.samplers.SampleEvent)
	 */
	public void sampleStopped(SampleEvent e) {
		// not used
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testStarted()
	 */
	public void testStarted() {
		testStarted("local");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testEnded()
	 */
	public void testEnded() {
		testEnded("local");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Called once for each Summariser in the test plan.
	 * There may be more than one summariser with the same name,
	 * however they will all be called before the test proper starts,
	 * so it does not matter if the totals are reset again.
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testStarted(java.lang.String)
	 */
	public void testStarted(String host) {
	    // testStarted and testFinished are called from different threads,
	    // so need to synch for visibility.
		synchronized (accumulators) {
            accumulators.clear(); // Should not be needed, but just in case previous run does not clear up.
		}
	}

	/*
	 * (non-Javadoc)
	 * Called from a different thread as testStarted() but using the same instance.
	 * So synch is needed to fetch the accumulator, and the myName field will already be set up.
	 * @see org.apache.jmeter.testelement.TestListener#testEnded(java.lang.String)
	 */
	public void testEnded(String host) {
	    Object[] totals;
		synchronized (accumulators) {
		    totals = accumulators.entrySet().toArray();
			accumulators.clear(); // Instance is not needed anymore
		}
		for (int i=0; i<totals.length; i++) {
		    Map.Entry me = (Map.Entry)totals[i];
			String str;
			String name = (String) me.getKey();
			Totals total = (Totals) me.getValue();
			if (total.total.getNumSamples() != 0) {// Only print delta if different from total
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testIterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
	 */
	public void testIterationStart(LoopIterationEvent event) {
		// not used
	}

    public void threadFinished() {
        // not used
    }

    public void threadStarted() {
        myName = getName();
        synchronized (accumulators) {
            myTotals = (Totals) accumulators.get(myName);
            if (myTotals == null){
                myTotals = new Totals();
                accumulators.put(myName, myTotals);
            }
        }
    }

}