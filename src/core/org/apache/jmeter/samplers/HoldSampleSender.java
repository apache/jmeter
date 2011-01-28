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

import org.apache.log.Logger;
import org.apache.jorphan.logging.LoggingManager;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Lars-Erik Helander provided the idea (and original implementation) for the
 * caching functionality (sampleStore).
 */

public class HoldSampleSender implements SampleSender, Serializable {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 240L;

    private final List<SampleEvent> sampleStore = new ArrayList<SampleEvent>();

    private final RemoteSampleListener listener;

    static {
        log.info("Using Sample store for this test run");        
    }

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
    }

    /** {@inheritDoc} */
    public void testEnded() {
        log.debug("Test ended()");
        try {
            synchronized (sampleStore) {
                Iterator<SampleEvent> i = sampleStore.iterator();
                while (i.hasNext()) {
                    SampleEvent se = i.next();
                    listener.sampleOccurred(se);
                }
            }
            listener.testEnded();
            sampleStore.clear();
        } catch (Throwable ex) {
            log.warn("testEnded()", ex);
            if (ex instanceof Error){
                throw (Error) ex;
            }
            if (ex instanceof RuntimeException){
                throw (RuntimeException) ex;
            }
        }

    }

    /** {@inheritDoc} */
    public void testEnded(String host) {
        log.debug("Test Ended on " + host);
        try {
            Iterator<SampleEvent> i = sampleStore.iterator();
            while (i.hasNext()) {
                SampleEvent se = i.next();
                listener.sampleOccurred(se);
            }
            listener.testEnded(host);
            sampleStore.clear();
        } catch (Throwable ex) {
            log.error("testEnded(host)", ex);
            if (ex instanceof Error){
                throw (Error) ex;
            }
            if (ex instanceof RuntimeException){
                throw (RuntimeException) ex;
            }
        }

    }

    /** {@inheritDoc} */
    public void sampleOccurred(SampleEvent e) {
        synchronized (sampleStore) {
            sampleStore.add(e);
        }
    }
}
