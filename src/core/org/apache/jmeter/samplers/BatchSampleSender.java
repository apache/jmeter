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
import org.apache.log.Logger;
import org.apache.jorphan.logging.LoggingManager;

import java.util.List;
import java.util.ArrayList;
import java.rmi.RemoteException;
import java.io.Serializable;

/**
 * Implements batch reporting for remote testing.
 *
 */
public class BatchSampleSender implements SampleSender, Serializable {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 240L;

    private static final int DEFAULT_NUM_SAMPLE_THRESHOLD = 100;

    private static final long DEFAULT_TIME_THRESHOLD = 60000L;

    private static final int NUM_SAMPLES_THRESHOLD = 
        JMeterUtils.getPropDefault("num_sample_threshold", DEFAULT_NUM_SAMPLE_THRESHOLD); // $NON-NLS-1$

    private static final long TIME_THRESHOLD_MS =
        JMeterUtils.getPropDefault("time_threshold", DEFAULT_TIME_THRESHOLD); // $NON-NLS-1$

    private final RemoteSampleListener listener;

    private final List<SampleEvent> sampleStore = new ArrayList<SampleEvent>();

    private long batchSendTime = -1;

    static {
        log.info("Using batching for this run."
                + " Thresholds: num=" + NUM_SAMPLES_THRESHOLD
                + ", time=" + TIME_THRESHOLD_MS);        
    }

    /**
     * @deprecated only for use by test code
     */
    @Deprecated
    public BatchSampleSender(){
        log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
        listener = null;
    }
    /**
     * Constructor
     *
     * @param listener
     *            that the List of sample events will be sent to.
     */
    protected BatchSampleSender(RemoteSampleListener listener) {
        this.listener = listener;
    }

   /**
    * @return the listener
    */
   protected RemoteSampleListener getListener() {
       return listener;
   }

   /**
    * @return the sampleStore
    */
   protected List<SampleEvent> getSampleStore() {
       return sampleStore;
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
    public void sampleOccurred(SampleEvent e) {
        synchronized (sampleStore) {
            sampleStore.add(e);
            final int sampleCount = sampleStore.size();

            boolean sendNow = false;            
            if (NUM_SAMPLES_THRESHOLD != -1) {
                if (sampleCount >= NUM_SAMPLES_THRESHOLD) {
                    sendNow = true;
                }
            }

            long now = 0;
            if (TIME_THRESHOLD_MS != -1) {
                now = System.currentTimeMillis();
                // Checking for and creating initial timestamp to check against
                if (batchSendTime == -1) {
                    this.batchSendTime = now + TIME_THRESHOLD_MS;
                }
                if (batchSendTime < now && sampleCount > 0) {
                    sendNow = true;
                }
            }

            if (sendNow){
                try {
                    log.debug("Firing sample");
                    listener.processBatch(sampleStore);
                    sampleStore.clear();
                    if (TIME_THRESHOLD_MS != -1) {
                        this.batchSendTime = now + TIME_THRESHOLD_MS;
                    }
                } catch (RemoteException err) {
                    log.error("sampleOccurred", err);
                }                
            }
        } // synchronized(sampleStore)
    }
}
