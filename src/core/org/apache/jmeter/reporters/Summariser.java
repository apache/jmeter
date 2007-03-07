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

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
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
 */
public class Summariser extends AbstractTestElement implements Serializable, SampleListener, TestListener, Clearable {
	private static final Logger log = LoggingManager.getLoggerForClass();

	/** interval between summaries (in seconds) default 3 minutes */
	private static final long INTERVAL = JMeterUtils.getPropDefault("summariser.interval", 3 * 60); //$NON-NLS-1$

	/** Write messages to log file ? */
	private static final boolean TOLOG = JMeterUtils.getPropDefault("summariser.log", true); //$NON-NLS-1$

	/** Write messages to System.out ? */
	private static final boolean TOOUT = JMeterUtils.getPropDefault("summariser.out", true); //$NON-NLS-1$

	/**
	 * Summariser elements are cloned for each thread in each group; this Map is
	 * used to allow them to share the same statistics. The key is the
	 * Summariser name, so all Summarisers with the same name will use the same
	 * accumulators.
	 */
	private static Hashtable accumulators = new Hashtable();

	/*
	 * Constructor is initially called once for each occurrence in the test plan.
	 * For GUI, several more instances are created.
	 * Then clear is called at start of test.
	 * Called several times during test startup.
	 * The name will not necessarily have been set at this point.
	 */
	public Summariser() {
		super();
		// log.debug(Thread.currentThread().getName());
		// System.out.println(">> "+me+" "+this.getName()+"
		// "+Thread.currentThread().getName());
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
	 * This is called once for each occurrence in the test plan, before the
	 * start of the test. The super.clear() method clears the name (and all
	 * other properties), so it is called last.
	 */
	public void clear() {
		// System.out.println("-- "+me+this.getName()+"
		// "+Thread.currentThread().getName());

		myName = this.getName();

		// Hashtable is synchronised, but there could be more than one Summariser
		// with the same name, so we need to synch.
		synchronized (accumulators) {
			Totals tots = (Totals) accumulators.get(myName);
			if (tots != null) {// This can be null (before first sample)
				tots.clear();
			} else {
				// System.out.println("Creating totals for "+myName);
				tots = new Totals();
				accumulators.put(myName, tots);
			}
		}

		super.clear();
	}

	/*
	 * Contains the items needed to collect stats for a summariser
	 * 
	 */
	private static class Totals {

		/** Time of last summary (to prevent double reporting) */
		private long last = 0;// set to -1 by TestEnded to prevent double
								// reporting

		private RunningSample delta = new RunningSample("DELTA",0);

		private RunningSample total = new RunningSample("TOTAL",0);

		private void clear() {
			delta.clear();
			total.clear();
			last = 0;
		}

		/**
		 * Add the delta values to the total values and clear the delta
		 */
		private synchronized void moveDelta() {
			total.addSample(delta);
			delta.clear();
		}
	}

	/**
	 * Cached copy of Totals for this instance.
     * These do not need to be synchronised,
     * as they are not shared between threads
	 */
	transient private Totals myTotals = null;

	transient private String myName;

	/**
	 * Ensure that a report is not skipped if we are slightly late in checking
	 * the time.
	 */
	private static final int INTERVAL_WINDOW = 5; // in seconds

	/**
	 * Accumulates the sample in two SampleResult objects - one for running
	 * totals, and the other for deltas.
	 * 
	 * @see org.apache.jmeter.samplers.SampleListener#sampleOccurred(org.apache.jmeter.samplers.SampleEvent)
	 */
	public void sampleOccurred(SampleEvent e) {
		SampleResult s = e.getResult();

		// System.out.println("SO "+me+this.getName()+"
		// "+Thread.currentThread().getName()
		// +" "+s.getSampleLabel());

		if (myName == null)
			myName = getName();

		if (myTotals == null)
			myTotals = (Totals) accumulators.get(myName);

		if (s != null) {
			myTotals.delta.addSample(s);
		}

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
			if ((now > myTotals.last + INTERVAL_WINDOW) && (now % INTERVAL <= INTERVAL_WINDOW)) {
				reportNow = true;
				
				// copy the data to minimise the synch time
				myDelta = new RunningSample(myTotals.delta);
				myTotals.moveDelta();
				myTotal = new RunningSample(myTotals.total);
				
				myTotals.last = now;
			}
		}
		if (reportNow) {
			String str;
			str = format(myDelta, "+");
			if (TOLOG)
				log.info(str);
			if (TOOUT)
				System.out.println(str);

			// Only if we have updated them
			if (myTotal.getNumSamples() != myDelta.getNumSamples()) {
				str = format(myTotal, "=");
				if (TOLOG)
					log.info(str);
				if (TOOUT)
					System.out.println(str);
			}
		}
	}

	private static StringBuffer longToSb(StringBuffer sb, long l, int len) {
		sb.setLength(0);
		sb.append(l);
		return JOrphanUtils.rightAlign(sb, len);
	}

	private static DecimalFormat dfDouble = new DecimalFormat("#0.0");

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
	private String format(RunningSample s, String type) {
		StringBuffer tmp = new StringBuffer(20); // for intermediate use
		StringBuffer sb = new StringBuffer(100); // output line buffer
		sb.append(myName);
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
		// not used
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
	 * @see org.apache.jmeter.testelement.TestListener#testStarted(java.lang.String)
	 */
	public void testStarted(String host) {
		// not used
	}

	/*
	 * (non-Javadoc) Can be called more than once with the same name, so need to
	 * synch. However, there is no need to create copies, to shorten the synch
	 * zone, as timing is not critical at the end of the test.
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testEnded(java.lang.String)
	 */
	public void testEnded(String host) {
		// System.out.println("TE "+me+this.getName()+"
		// "+Thread.currentThread().getName());
		synchronized (accumulators) {
			Totals t = (Totals) accumulators.get(myName);
			if (t.last != -1) {
				String str;
				if (t.total.getNumSamples() != 0) {// Only print delta if different
												// from total
					str = format(t.delta, "+");
					if (TOLOG)
						log.info(str);
					if (TOOUT)
						System.out.println(str);
				}
				t.moveDelta();
				str = format(t.total, "=");
				if (TOLOG)
					log.info(str);
				if (TOOUT)
					System.out.println(str);
				t.last = -1;
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

}