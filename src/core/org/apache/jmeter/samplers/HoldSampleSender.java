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
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Lars-Erik Helander provided the idea (and original implementation) for the
 * caching functionality (sampleStore).
 */
public class HoldSampleSender extends AbstractSampleSender implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(HoldSampleSender.class);

    private static final long serialVersionUID = 241L;

    private final ConcurrentLinkedQueue<SampleEvent> sampleStore = new ConcurrentLinkedQueue<>();

    private final RemoteSampleListener listener;

    /**
     * @deprecated only for use by test code
     */
    @Deprecated
    public HoldSampleSender(){
        log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
        listener = null;
    }

    HoldSampleSender(RemoteSampleListener listener) {
        this.listener = listener;
        log.warn("Using HoldSampleSender for this test run, ensure you have configured enough memory (-Xmx) for your test"); // client
    }

    @Override
    public void testEnded(String host) {
        log.info("Test Ended on {}", host);
        try {
            for (SampleEvent se : sampleStore) {
                listener.sampleOccurred(se);
            }
            listener.testEnded(host);
            sampleStore.clear();
        } catch (Error | RuntimeException ex) { // NOSONAR We want to have errors logged in log file
            log.error("testEnded(host)", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("testEnded(host)", ex);
        }

    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        sampleStore.add(e);
    }

    /**
     * Processed by the RMI server code; acts as testStarted().
     *
     * @return this
     * @throws ObjectStreamException
     *             never
     */
    protected Object readResolve() throws ObjectStreamException{
        log.warn("Using HoldSampleSender for this test run, ensure you have configured enough memory (-Xmx) for your test"); // server
        return this;
    }
}
