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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements batch reporting for remote testing.
 *
 */
public class StatisticalSampleSender extends AbstractSampleSender implements Serializable {
    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(StatisticalSampleSender.class);

    private static final int DEFAULT_NUM_SAMPLE_THRESHOLD = 100;

    private static final long DEFAULT_TIME_THRESHOLD = 60000L;

    // Static fields are set by the server when the class is constructed

    private static final int NUM_SAMPLES_THRESHOLD = JMeterUtils.getPropDefault(
            "num_sample_threshold", DEFAULT_NUM_SAMPLE_THRESHOLD);

    private static final long TIME_THRESHOLD_MS = JMeterUtils.getPropDefault("time_threshold",
            DEFAULT_TIME_THRESHOLD);

    // should the samples be aggregated on thread name or thread group (default) ?
    private static boolean KEY_ON_THREADNAME = JMeterUtils.getPropDefault("key_on_threadname", false);

    // Instance fields are constructed by the client when the instance is create in the test plan
    // and the field values are then transferred to the server copy by RMI serialisation/deserialisation

    private final int clientConfiguredNumSamplesThreshold = JMeterUtils.getPropDefault(
            "num_sample_threshold", DEFAULT_NUM_SAMPLE_THRESHOLD);

    private final long clientConfiguredTimeThresholdMs = JMeterUtils.getPropDefault("time_threshold",
            DEFAULT_TIME_THRESHOLD);

    // should the samples be aggregated on thread name or thread group (default) ?
    private final boolean clientConfiguredKeyOnThreadName = JMeterUtils.getPropDefault("key_on_threadname", false);

    private final RemoteSampleListener listener;

    private final List<SampleEvent> sampleStore = new ArrayList<>();

    //@GuardedBy("sampleStore") TODO perhaps use ConcurrentHashMap ?
    private final Map<String, StatisticalSampleResult> sampleTable = new HashMap<>();

    // Settings; readResolve sets these from the server/client values as appropriate
    // TODO would be nice to make these final; not 100% sure volatile is needed as not changed after creation
    private transient volatile int numSamplesThreshold;

    private transient volatile long timeThresholdMs;

    private transient volatile boolean keyOnThreadName;


    // variables maintained by server code
    // @GuardedBy("sampleStore")
    private transient int sampleCount; // maintain separate count of samples for speed

    private transient long batchSendTime = -1; // @GuardedBy("sampleStore")

    /**
     * @deprecated only for use by test code
     */
    @Deprecated
    public StatisticalSampleSender(){
        this(null);
        log.warn("Constructor only intended for use in testing");
    }

    /**
     * Constructor, only called by client code.
     *
     * @param listener that the List of sample events will be sent to.
     */
    StatisticalSampleSender(RemoteSampleListener listener) {
        this.listener = listener;
        if (isClientConfigured()) {
            log.info(
                    "Using StatisticalSampleSender (client settings) for this run."
                            + " Thresholds: num={}, time={}. Key uses ThreadName: {}",
                    clientConfiguredNumSamplesThreshold, clientConfiguredTimeThresholdMs,
                    clientConfiguredKeyOnThreadName);
        } else {
            log.info("Using StatisticalSampleSender (server settings) for this run.");
        }
    }

    /**
     * Checks if any sample events are still present in the sampleStore and
     * sends them to the listener. Informs the listener that the test ended.
     *
     * @param host the hostname that the test has ended on.
     */
    @Override
    public void testEnded(String host) {
        log.info("Test Ended on {}", host);
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
     * Stores sample events until either a time or sample threshold is
     * breached. Both thresholds are reset if one fires. If only one threshold
     * is set it becomes the only value checked against. When a threshold is
     * breached the list of sample events is sent to a listener where the event
     * are fired locally.
     *
     * @param e a Sample Event
     */
    @Override
    public void sampleOccurred(SampleEvent e) {
        synchronized (sampleStore) {
            // Locate the statistical sample collector
            String key = StatisticalSampleResult.getKey(e, keyOnThreadName);
            StatisticalSampleResult statResult = sampleTable.get(key);
            if (statResult == null) {
                statResult = new StatisticalSampleResult(e.getResult());
                // store the new statistical result collector
                sampleTable.put(key, statResult);
                // add a new wrapper sampleevent
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
                    log.debug("Firing sample");
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
     * Processed by the RMI server code; acts as testStarted().
     * @return this
     * @throws ObjectStreamException never
     */
    private Object readResolve() throws ObjectStreamException{
        if (isClientConfigured()) {
            numSamplesThreshold = clientConfiguredNumSamplesThreshold;
            timeThresholdMs = clientConfiguredTimeThresholdMs;
            keyOnThreadName = clientConfiguredKeyOnThreadName;
        } else {
            numSamplesThreshold = NUM_SAMPLES_THRESHOLD;
            timeThresholdMs = TIME_THRESHOLD_MS;
            keyOnThreadName = KEY_ON_THREADNAME;
        }
        if (log.isInfoEnabled()) {
            log.info(
                    "Using StatisticalSampleSender for this run. {} config: Thresholds: num={}, time={}. Key uses ThreadName: {}",
                    (isClientConfigured() ? "Client" : "Server"), numSamplesThreshold, timeThresholdMs,
                    keyOnThreadName);
        }
        return this;
    }
}
