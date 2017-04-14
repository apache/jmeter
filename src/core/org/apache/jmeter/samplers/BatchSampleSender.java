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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements batch reporting for remote testing.
 *
 */
public class BatchSampleSender extends AbstractSampleSender implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(BatchSampleSender.class);

    private static final long serialVersionUID = 241L;

    private static final int DEFAULT_NUM_SAMPLE_THRESHOLD = 100;

    private static final long DEFAULT_TIME_THRESHOLD = 60000L;

    // Static fields are resolved on the server
    private static final int NUM_SAMPLES_THRESHOLD = 
        JMeterUtils.getPropDefault("num_sample_threshold", DEFAULT_NUM_SAMPLE_THRESHOLD); // $NON-NLS-1$

    private static final long TIME_THRESHOLD_MS =
        JMeterUtils.getPropDefault("time_threshold", DEFAULT_TIME_THRESHOLD); // $NON-NLS-1$

    // instance fields are copied from the client instance
    private final int clientConfiguredNumSamplesThreshold = 
            JMeterUtils.getPropDefault("num_sample_threshold", DEFAULT_NUM_SAMPLE_THRESHOLD); // $NON-NLS-1$

    private final long clientConfiguredTimeThresholdMs =
            JMeterUtils.getPropDefault("time_threshold", DEFAULT_TIME_THRESHOLD); // $NON-NLS-1$

    private final RemoteSampleListener listener;

    private final List<SampleEvent> sampleStore = new ArrayList<>();

    // Server-only work item
    private transient long batchSendTime = -1;

    // Configuration items, set up by readResolve
    private transient volatile int numSamplesThreshold;

    private transient volatile long timeThresholdMs;


    /**
     * @deprecated only for use by test code
     */
    @Deprecated
    public BatchSampleSender(){
        this(null);
        log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
    }
    /**
     * Constructor
     *
     * @param listener
     *            that the List of sample events will be sent to.
     */
    // protected added: Bug 50008 - allow BatchSampleSender to be subclassed
    protected BatchSampleSender(RemoteSampleListener listener) {
        this.listener = listener;
        if (isClientConfigured()) {
            log.info("Using batching (client settings) for this run. Thresholds: num={}, time={}",
                    clientConfiguredNumSamplesThreshold, clientConfiguredTimeThresholdMs);
        } else {
            log.info("Using batching (server settings) for this run.");
        }
    }

   /**
    * @return the listener
    */
    // added: Bug 50008 - allow BatchSampleSender to be subclassed
   protected RemoteSampleListener getListener() {
       return listener;
   }

   /**
    * @return the sampleStore
    */
   // added: Bug 50008 - allow BatchSampleSender to be subclassed
   protected List<SampleEvent> getSampleStore() {
       return sampleStore;
   }

    /**
     * Checks if any sample events are still present in the sampleStore and
     * sends them to the listener. Informs the listener of the testended.
     *
     * @param host
     *            the host that the test has ended on.
     */
    @Override
    public void testEnded(String host) {
        log.info("Test Ended on {}", host);
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
     * Stores sample events until either a time or sample threshold is
     * breached. Both thresholds are reset if one fires. If only one threshold
     * is set it becomes the only value checked against. When a threshold is
     * breached the list of sample events is sent to a listener where the event
     * are fired locally.
     *
     * @param e
     *            a Sample Event
     */
    @Override
    public void sampleOccurred(SampleEvent e) {
        List<SampleEvent> clonedStore = null;
        synchronized (sampleStore) {
            sampleStore.add(e);
            final int sampleCount = sampleStore.size();

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
                if (batchSendTime < now && sampleCount > 0) {
                    sendNow = true;
                }
            }

            if (sendNow){
                @SuppressWarnings("unchecked") // OK because sampleStore is of type ArrayList<SampleEvent>
                final ArrayList<SampleEvent> clone = (ArrayList<SampleEvent>)((ArrayList<SampleEvent>)sampleStore).clone();
                clonedStore = clone;
                sampleStore.clear();
                if (timeThresholdMs != -1) {
                    this.batchSendTime = now + timeThresholdMs;
                }
            }
        } // synchronized(sampleStore)
        
        if (clonedStore != null){
            try {
                log.debug("Firing sample");
                listener.processBatch(clonedStore);
                clonedStore.clear();
            } catch (RemoteException err) {
                log.error("sampleOccurred", err);
            }  
        }
    }
    
    /**
     * Processed by the RMI server code; acts as testStarted().
     *
     * @return this
     * @throws ObjectStreamException
     *             never
     */
    private Object readResolve() throws ObjectStreamException{
        if (isClientConfigured()) {
            numSamplesThreshold = clientConfiguredNumSamplesThreshold;
            timeThresholdMs = clientConfiguredTimeThresholdMs;
        } else {
            numSamplesThreshold =  NUM_SAMPLES_THRESHOLD;
            timeThresholdMs = TIME_THRESHOLD_MS;
        }
        log.info("Using batching for this run. Thresholds: num={}, time={}", numSamplesThreshold, timeThresholdMs);
        return this;
    }
}
