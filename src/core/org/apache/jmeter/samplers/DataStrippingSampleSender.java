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

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The standard remote sample reporting should be more friendly to the main purpose of
 * remote testing - which is scalability.  To increase scalability, this class strips out the
 * response data before sending.
 *
 *
 */
public class DataStrippingSampleSender extends AbstractSampleSender implements Serializable {
    private static final long serialVersionUID = 1;
    private static final Logger log = LoggingManager.getLoggerForClass();

    private final RemoteSampleListener listener;
    private final SampleSender decoratedSender;

    /**
     * @deprecated only for use by test code
     */
    @Deprecated
    public DataStrippingSampleSender(){
        log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
        listener = null;
        decoratedSender = null;
    }

    DataStrippingSampleSender(RemoteSampleListener listener) {
        this.listener = listener;
        decoratedSender = null;
        log.info("Using DataStrippingSampleSender for this run");
    }

    DataStrippingSampleSender(SampleSender decorate)
    {
        this.decoratedSender = decorate;
        this.listener = null;
        log.info("Using DataStrippingSampleSender for this run");
    }

    public void testEnded(String host) {
        log.info("Test Ended on " + host);
        if(decoratedSender != null) decoratedSender.testEnded(host);
    }

    public void sampleOccurred(SampleEvent event) {
        //Strip the response data before writing, but only for a successful request.
        SampleResult result = event.getResult();
        if(result.isSuccessful()) {
            result.setResponseData(new byte[0]);
        }
        if(decoratedSender == null)
        {
            try {
                listener.sampleOccurred(event);
            } catch (RemoteException e) {
                log.error("Error sending sample result over network ",e);
            }
        }
        else
        {
            decoratedSender.sampleOccurred(event);
        }
    }

    /**
     * Processed by the RMI server code; acts as testStarted().
     * @throws ObjectStreamException  
     */
    private Object readResolve() throws ObjectStreamException{
        log.info("Using DataStrippingSampleSender for this run");
        return this;
    }
}
