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

import org.apache.jorphan.util.JMeterError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default behaviour for remote testing.
 */

public class StandardSampleSender extends AbstractSampleSender implements Serializable {
    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(StandardSampleSender.class);

    private final RemoteSampleListener listener;

    /**
     * @deprecated only for use by test code
     */
    @Deprecated
    public StandardSampleSender(){
        this.listener = null;
        log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
    }

    StandardSampleSender(RemoteSampleListener listener) {
        this.listener = listener;
        log.info("Using StandardSampleSender for this test run");
    }

    @Override
    public void testEnded(String host) {
        log.info("Test Ended on {}", host);
        try {
            listener.testEnded(host);
        } catch (RemoteException ex) {
            log.warn("testEnded(host)", ex);
        }
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        try {
            listener.sampleOccurred(e);
        } catch (RemoteException err) {
            if (err.getCause() instanceof java.net.ConnectException){
                throw new JMeterError("Could not return sample",err);
            }
            log.error("sampleOccurred", err);
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
        log.info("Using StandardSampleSender for this test run");
        return this;
    }
}
