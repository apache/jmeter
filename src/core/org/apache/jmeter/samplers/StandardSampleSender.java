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
import org.apache.jorphan.util.JMeterError;

import java.rmi.RemoteException;
import java.io.Serializable;

/**
 * Default behaviour for remote testing.
 */

public class StandardSampleSender implements SampleSender, Serializable {
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private RemoteSampleListener listener;

    static {
        log.info("Using Standard Remote Sampler for this test run");        
    }

    /**
     * @deprecated only for use by test code
     */
    @Deprecated
    public StandardSampleSender(){
        log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
    }

    StandardSampleSender(RemoteSampleListener listener) {
        this.listener = listener;
    }

    public void testEnded() {
        log.info("Test ended()");
        try {
            listener.testEnded();
        } catch (RemoteException ex) {
            log.warn("testEnded()"+ex);
        }

    }

    public void testEnded(String host) {
        log.info("Test Ended on " + host); // should this be debug?
        try {
            listener.testEnded(host);
        } catch (RemoteException ex) {
            log.warn("testEnded(host)"+ex);
        }
    }

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
}
