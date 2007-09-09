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

package org.apache.jmeter.samplers;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements batch reporting for remote testing.
 *
 * @author Lars Krog-Jensen
 *         Created: 2005-okt-04
 */
public class StatisticalSampleSender implements SampleSender, Serializable {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private static final int DEFAULT_NUM_SAMPLE_THRESHOLD = 100;

	private static final long DEFAULT_TIME_THRESHOLD = 60000L;

	private RemoteSampleListener listener;

	private List sampleStore = new ArrayList();

	private Map sampleTable = new HashMap();

	private int numSamplesThreshold;

	private int sampleCount;

	private long timeThreshold;

	private long batchSendTime = -1;

    public StatisticalSampleSender(){
        log.warn("Constructor only intended for use in testing");
    }
        

	/**
	 * Constructor
	 *
	 * @param listener that the List of sample events will be sent to.
	 */
	StatisticalSampleSender(RemoteSampleListener listener) {
		this.listener = listener;
		init();
		log.info("Using batching for this run." + " Thresholds: num="
				+ numSamplesThreshold + ", time=" + timeThreshold);
	}

	/**
	 * Checks for the Jmeter properties num_sample_threshold and time_threshold,
	 * and assigns defaults if not found.
	 */
	private void init() {
		this.numSamplesThreshold = JMeterUtils.getPropDefault(
				"num_sample_threshold", DEFAULT_NUM_SAMPLE_THRESHOLD);
		this.timeThreshold = JMeterUtils.getPropDefault("time_threshold",
				DEFAULT_TIME_THRESHOLD);
	}

	/**
	 * Checks if any sample events are still present in the sampleStore and
	 * sends them to the listener. Informs the listener of the testended.
	 */
	public void testEnded() {
		try {
			if (sampleStore.size() != 0) {
				sendBatch();
			}
			listener.testEnded();
		} catch (RemoteException err) {
			log.warn("testEnded()", err);
		}
	}

	/**
	 * Checks if any sample events are still present in the sampleStore and
	 * sends them to the listener. Informs the listener of the testended.
	 *
	 * @param host the hostname that the test has ended on.
	 */
	public void testEnded(String host) {
		try {
			if (sampleStore.size() != 0) {
				sendBatch();
			}
			listener.testEnded(host);
		} catch (RemoteException err) {
			log.warn("testEnded(hostname)", err);
		}
	}

	/**
	 * Stores sample events untill either a time or sample threshold is
	 * breached. Both thresholds are reset if one fires. If only one threshold
	 * is set it becomes the only value checked against. When a threhold is
	 * breached the list of sample events is sent to a listener where the event
	 * are fired locally.
	 *
	 * @param e a Sample Event
	 */
	public void SampleOccurred(SampleEvent e) {
		synchronized (sampleStore) {
			// Locate the statistical sample colector
			String key = StatisticalSampleResult.getKey(e);
			StatisticalSampleResult statResult = (StatisticalSampleResult) sampleTable
					.get(key);
			if (statResult == null) {
				statResult = new StatisticalSampleResult(e.getResult());
				// store the new statistical result collector
				sampleTable.put(key, statResult);
				// add a new wrapper samplevent
				sampleStore
						.add(new SampleEvent(statResult, e.getThreadGroup()));
			}
			statResult.add(e.getResult());
			sampleCount++;
			if (numSamplesThreshold != -1) {
				if (sampleCount >= numSamplesThreshold) {
					try {
						if (log.isDebugEnabled()) {
							log.debug("Firing sample");
						}
						sendBatch();
					} catch (RemoteException err) {
						log.warn("sampleOccurred", err);
					}
				}
			}

			if (timeThreshold != -1) {
				long now = System.currentTimeMillis();
				// Checking for and creating initial timestamp to cheak against
				if (batchSendTime == -1) {
					this.batchSendTime = now + timeThreshold;
				}

				if (batchSendTime < now) {
					try {
						if (log.isDebugEnabled()) {
							log.debug("Firing time");
						}
						sendBatch();
						this.batchSendTime = now + timeThreshold;
					} catch (RemoteException err) {
						log.warn("sampleOccurred", err);
					}
				}
			}
		}
	}

	private void sendBatch() throws RemoteException {
		if (sampleStore.size() > 0) {
			listener.processBatch(sampleStore);
			sampleStore.clear();
			sampleTable.clear();
			sampleCount = 0;
		}
	}
}
