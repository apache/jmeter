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

import java.io.Serializable;
import java.rmi.RemoteException;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Lars-Erik Helander provided the idea (and original implementation) for the
 * caching functionality (sampleStore).
 */
public class RemoteListenerWrapper extends AbstractTestElement implements SampleListener, TestStateListener, Serializable,
        NoThreadClone {
    private static final Logger log = LoggerFactory.getLogger(RemoteListenerWrapper.class);

    private static final long serialVersionUID = 241L;

    private final RemoteSampleListener listener;

    private final SampleSender sender;

    public RemoteListenerWrapper(RemoteSampleListener l) {
        listener = l;
        // Get appropriate sender class governed by the behaviour set in the JMeter property
        sender = SampleSenderFactory.getInstance(listener);
    }

    public RemoteListenerWrapper() // TODO: not used - make private?
    {
        listener = null;
        sender = null;
    }

    @Override
    public void testStarted() {
        log.debug("Test Started()");
        try {
            listener.testStarted();
        } catch (Error | RuntimeException ex) { // NOSONAR We want to have errors logged in log file
            log.error("testStarted()", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("testStarted()", ex);
        }
    }

    @Override
    public void testEnded() {
        sender.testEnded();
    }

    @Override
    public void testStarted(String host) {
        log.debug("Test Started on {}", host);
        try {
            listener.testStarted(host);
        } catch (Error | RuntimeException ex) { // NOSONAR We want to have errors logged in log file
            log.error("testStarted(host) on {}", host, ex);
            throw ex;
        } catch(Exception ex) {
            log.error("testStarted(host) on {}", host, ex);
        }
    }

    @Override
    public void testEnded(String host) {
        sender.testEnded(host);
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        sender.sampleOccurred(e);
    }

    // Note that sampleStarted() and sampleStopped() is not made to appear
    // in synch with sampleOccured() when replaying held samples.
    // For now this is not critical since sampleStarted() and sampleStopped()
    // is not used, but it may become an issue in the future. Then these
    // events must also be stored so that replay of all events may occur and
    // in the right order. Each stored event must then be tagged with something
    // that lets you distinguish between occurred, started and ended.

    @Override
    public void sampleStarted(SampleEvent e) {
        log.debug("Sample started");
        try {
            listener.sampleStarted(e);
        } catch (RemoteException err) {
            log.error("sampleStarted", err);
        }
    }

    @Override
    public void sampleStopped(SampleEvent e) {
        log.debug("Sample stopped");
        try {
            listener.sampleStopped(e);
        } catch (RemoteException err) {
            log.error("sampleStopped", err);
        }
    }
}
