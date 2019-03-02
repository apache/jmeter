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

import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The standard remote sample reporting should be more friendly to the main purpose of
 * remote testing - which is scalability.  To increase scalability, this class strips out the
 * response data before sending.
 */
public class DataStrippingSampleSender extends AbstractSampleSender implements Serializable {

    private static final long serialVersionUID = 1L;

    /** empty array which can be returned instead of null */
    private static final byte[] EMPTY_BA = new byte[0];

    private static final Logger log = LoggerFactory.getLogger(DataStrippingSampleSender.class);

    private static final boolean DEFAULT_STRIP_ALSO_ON_ERROR = true;

    private static final boolean SERVER_CONFIGURED_STRIP_ALSO_ON_ERROR =
            JMeterUtils.getPropDefault("sample_sender_strip_also_on_error", DEFAULT_STRIP_ALSO_ON_ERROR); // $NON-NLS-1$

    // instance fields are copied from the client instance
    private final boolean clientConfiguredStripAlsoOnError =
            JMeterUtils.getPropDefault("sample_sender_strip_also_on_error", DEFAULT_STRIP_ALSO_ON_ERROR); // $NON-NLS-1$


    private final RemoteSampleListener listener;
    private final SampleSender decoratedSender;
    // Configuration items, set up by readResolve
    private transient volatile boolean stripAlsoOnError;


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

    @Override
    public void testEnded(String host) {
        log.info("Test Ended on {}", host);
        if(decoratedSender != null) {
            decoratedSender.testEnded(host);
        }
    }

    @Override
    public void sampleOccurred(SampleEvent event) {
        //Strip the response data before writing, but only for a successful request.
        SampleResult result = event.getResult();
        if(stripAlsoOnError || result.isSuccessful()) {
            // Compute bytes before stripping
            stripResponse(result);
            // see Bug 57449
            for (SampleResult subResult : result.getSubResults()) {
                stripResponse(subResult);
            }
        }
        if(decoratedSender == null)
        {
            try {
                listener.sampleOccurred(event);
            } catch (RemoteException e) {
                log.error("Error sending sample result over network", e);
            }
        }
        else
        {
            decoratedSender.sampleOccurred(event);
        }
    }

    /**
     * Strip response but fill in bytes field.
     * @param result {@link SampleResult}
     */
    private void stripResponse(SampleResult result) {
        result.setBytes(result.getBytesAsLong());
        result.setResponseData(EMPTY_BA);
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
            stripAlsoOnError = clientConfiguredStripAlsoOnError;
        } else {
            stripAlsoOnError = SERVER_CONFIGURED_STRIP_ALSO_ON_ERROR;
        }
        log.info("Using DataStrippingSampleSender for this run with stripAlsoOnError: {}", stripAlsoOnError);
        return this;
    }
}
