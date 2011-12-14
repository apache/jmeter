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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements batch reporting for remote testing.
 *
 */
public class StatisticalSampleSender extends AbstractSampleSender implements Serializable {
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final int DEFAULT_NUM_SAMPLE_THRESHOLD = 100;

    private static final long DEFAULT_TIME_THRESHOLD = 60000L;

    private final int clientConfiguredNumSamplesThreshold = JMeterUtils.getPropDefault(
            "num_sample_threshold", DEFAULT_NUM_SAMPLE_THRESHOLD);

    private final long clientConfiguredTimeThresholdMs = JMeterUtils.getPropDefault("time_threshold",
            DEFAULT_TIME_THRESHOLD);

    // should the samples be aggregated on thread name or thread group (default) ?
    private boolean clientConfiguredKeyOnThreadName = JMeterUtils.getPropDefault("key_on_threadname", false);

    private static final int serverConfiguredNumSamplesThreshold = JMeterUtils.getPropDefault(
            "num_sample_threshold", DEFAULT_NUM_SAMPLE_THRESHOLD);

    private static final long serverConfiguredTimeThresholdMs = JMeterUtils.getPropDefault("time_threshold",
            DEFAULT_TIME_THRESHOLD);
    
    // should the samples be aggregated on thread name or thread group (default) ?
    private static boolean serverConfiguredKeyOnThreadName = JMeterUtils.getPropDefault("key_on_threadname", false);

    private final RemoteSampleListener listener;

    private final List<SampleEvent> sampleStore = new ArrayList<SampleEvent>();

    //@GuardedBy("sampleStore") TODO perhaps use ConcurrentHashMap ?
    private final Map<String, StatisticalSampleResult> sampleTable = new HashMap<String, StatisticalSampleResult>();

    private int sampleCount; // maintain separate count of samples for speed

    private long batchSendTime = -1;

    /**
     * @deprecated only for use by test code
     */
    @Deprecated
    public StatisticalSampleSender(){
    	this(null);
        log.warn("Constructor only intended for use in testing");
    }

    /**
     * Constructor
     *
     * @param listener that the List of sample events will be sent to.
     */
    StatisticalSampleSender(RemoteSampleListener listener) {
        this.listener = listener;
        log.info("Using StatisticalSampleSender for this run." + " Thresholds: num="
                + getNumSamplesThreshold() + ", time=" + getTimeThresholdMs()
                + ". Key uses ThreadName: " + getKeyOnThreadName());        
    }

    /**
     * Checks if any sample events are still present in the sampleStore and
     * sends them to the listener. Informs the listener of the testended.
     *
     * @param host the hostname that the test has ended on.
     */
    public void testEnded(String host) {
        log.info("Test Ended on " + host);
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
    public void sampleOccurred(SampleEvent e) {
    	int numSamplesThreshold = getNumSamplesThreshold();
    	long timeThresholdMs = getTimeThresholdMs();
    	boolean keyOnThreadName = getKeyOnThreadName();
    	synchronized (sampleStore) {
            // Locate the statistical sample colector
            String key = StatisticalSampleResult.getKey(e, keyOnThreadName);
            StatisticalSampleResult statResult = sampleTable.get(key);
            if (statResult == null) {
                statResult = new StatisticalSampleResult(e.getResult(), keyOnThreadName);
                // store the new statistical result collector
                sampleTable.put(key, statResult);
                // add a new wrapper samplevent
                sampleStore
                        .add(new SampleEvent(statResult, e.getThreadGroup()));
            }
            statResult.add(e.getResult());
            sampleCount++;
            boolean sendNow = false;
            if (numSamplesThreshold != -1) {
                if (sampleCount >= numSamplesThreshold) {
                    sendNow = true;
                }
            }

            long now = 0;
            if (timeThresholdMs != -1) {
                now = System.currentTimeMillis();
                // Checking for and creating initial timestamp to check against
                if (batchSendTime == -1) {
                    this.batchSendTime = now + timeThresholdMs;
                }
                if (batchSendTime < now) {
                    sendNow = true;
                }
            }
            if (sendNow) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Firing sample");
                    }
                    sendBatch();
                    if (timeThresholdMs != -1) {
                        this.batchSendTime = now + timeThresholdMs;
                    }
                } catch (RemoteException err) {
                    log.warn("sampleOccurred", err);
                }
            }
        } // synchronized(sampleStore)
    }

    private void sendBatch() throws RemoteException {
        if (sampleStore.size() > 0) {
            listener.processBatch(sampleStore);
            sampleStore.clear();
            sampleTable.clear();
            sampleCount = 0;
        }
    }
    
    /**
     * @return time in ms when a send will occur if limit is breached
     */
    private long getTimeThresholdMs() {
    	return isClientConfigured() ?
    			clientConfiguredTimeThresholdMs : serverConfiguredTimeThresholdMs;
    }
    
    /**
     * @return number of samples threshold over which results will be sent
     */
    private int getNumSamplesThreshold() {
    	return isClientConfigured() ?
    			clientConfiguredNumSamplesThreshold: serverConfiguredNumSamplesThreshold;
    }
    
    /**
     * @return boolean indicating wether samples should be aggregated on thread name or thread group (default) ?
     */
    private boolean getKeyOnThreadName() {
    	return isClientConfigured() ?
    			clientConfiguredKeyOnThreadName: serverConfiguredKeyOnThreadName;
    }

    /**
     * Processed by the RMI server code; acts as testStarted().
     * @throws ObjectStreamException  
     */
    private Object readResolve() throws ObjectStreamException{
        log.info("Using StatisticalSampleSender for this run." + " Thresholds: num="
                + getNumSamplesThreshold() + ", time=" + getTimeThresholdMs()
                + ". Key uses ThreadName: " + getKeyOnThreadName());        
        return this;
    }
}
