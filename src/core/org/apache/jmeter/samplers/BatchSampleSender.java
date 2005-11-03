/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import org.apache.log.Logger;
import org.apache.jorphan.logging.LoggingManager;

import java.util.List;
import java.util.ArrayList;
import java.rmi.RemoteException;
import java.io.Serializable;

/**
 * Implements batch reporting for remote testing.
 * 
 * @author Michael Freeman
 */
public class BatchSampleSender implements SampleSender, Serializable {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private static final int DEFAULT_NUM_SAMPLE_THRESHOLD = 100;

	private static final long DEFAULT_TIME_THRESHOLD = 60000L;

	private RemoteSampleListener listener;

	private List sampleStore = new ArrayList();

	private int numSamplesThreshold;

	private long timeThreshold;

	private long batchSendTime = -1;

    public BatchSampleSender(){
        log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
    }
	/**
	 * Constructor
	 * 
	 * @param listener
	 *            that the List of sample events will be sent to.
	 */
	BatchSampleSender(RemoteSampleListener listener) {
		this.listener = listener;
		init();
		log.info("Using batching for this run." 
                + " Thresholds: num=" + numSamplesThreshold 
                + ", time="	+ timeThreshold);
	}

	/**
	 * Checks for the Jmeter properties num_sample_threshold and time_threshold,
	 * and assigns defaults if not found.
	 */
	private void init() {
		this.numSamplesThreshold = JMeterUtils.getPropDefault("num_sample_threshold", DEFAULT_NUM_SAMPLE_THRESHOLD);
		this.timeThreshold = JMeterUtils.getPropDefault("time_threshold", DEFAULT_TIME_THRESHOLD);
	}

	/**
	 * Checks if any sample events are still present in the sampleStore and
	 * sends them to the listener. Informs the listener of the testended.
	 */
	public void testEnded() {
		try {
			if (sampleStore.size() != 0) {
				listener.processBatch(sampleStore);
				sampleStore.clear();
			}
			listener.testEnded();
		} catch (RemoteException err) {
			log.error("testEnded()", err);
		}
	}

	/**
	 * Checks if any sample events are still present in the sampleStore and
	 * sends them to the listener. Informs the listener of the testended.
	 * 
	 * @param host
	 *            the host that the test has ended on.
	 */
	public void testEnded(String host) {
		try {
			if (sampleStore.size() != 0) {
				listener.processBatch(sampleStore);
				sampleStore.clear();
			}
			listener.testEnded(host);
		} catch (RemoteException err) {
			log.error("testEnded(host)", err);
		}
	}

	/**
	 * Stores sample events untill either a time or sample threshold is
	 * breached. Both thresholds are reset if one fires. If only one threshold
	 * is set it becomes the only value checked against. When a threhold is
	 * breached the list of sample events is sent to a listener where the event
	 * are fired locally.
	 * 
	 * @param e
	 *            a Sample Event
	 */
	public void SampleOccurred(SampleEvent e) {
		synchronized (sampleStore) {
			sampleStore.add(e);

			if (numSamplesThreshold != -1) {
				if (sampleStore.size() >= numSamplesThreshold) {
					try {
						log.debug("Firing sample");
						listener.processBatch(sampleStore);
						sampleStore.clear();
					} catch (RemoteException err) {
						log.error("sampleOccurred", err);
					}
				}
			}

			if (timeThreshold != -1) {
				SampleResult sr = e.getResult();
				long timestamp = sr.getTimeStamp();

				// Checking for and creating initial timestamp to cheak against
				if (batchSendTime == -1) {
					this.batchSendTime = timestamp + timeThreshold;
				}

				if (batchSendTime < timestamp) {
					try {
						log.debug("Firing time");
						if (sampleStore.size() > 0) {
							listener.processBatch(sampleStore);
							sampleStore.clear();
						}
						this.batchSendTime = timestamp + timeThreshold;
					} catch (RemoteException err) {
						log.error("sampleOccurred", err);
					}
				}
			}
		}
	}
}
