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

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 *
 * Lars-Erik Helander provided the idea (and original implementation) for the
 * caching functionality (sampleStore).
 *
 * @version $Revision$
 */
public class RemoteListenerWrapper extends AbstractTestElement implements SampleListener, TestListener, Serializable,
        NoThreadClone {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 240L;

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

    public void testStarted() {
        log.debug("Test Started()");
        try {
            listener.testStarted();
        } catch (Throwable ex) {
            log.warn("testStarted()", ex);
            if (ex instanceof Error){
                throw (Error) ex;
            }
            if (ex instanceof RuntimeException){
                throw (RuntimeException) ex;
            }
        }

    }

    public void testEnded() {
        sender.testEnded();
    }

    public void testStarted(String host) {
        log.debug("Test Started on " + host);
        try {
            listener.testStarted(host);
        } catch (Throwable ex) {
            log.error("testStarted(host)", ex);
            if (ex instanceof Error){
                throw (Error) ex;
            }
            if (ex instanceof RuntimeException){
                throw (RuntimeException) ex;
            }
}
    }

    public void testEnded(String host) {
        sender.testEnded(host);
    }

    public void sampleOccurred(SampleEvent e) {
        sender.sampleOccurred(e);
    }

    // Note that sampleStarted() and sampleStopped() is not made to appear
    // in synch with sampleOccured() when replaying held samples.
    // For now this is not critical since sampleStarted() and sampleStopped()
    // is not used, but it may become an issue in the future. Then these
    // events must also be stored so that replay of all events may occur and
    // in the right order. Each stored event must then be tagged with something
    // that lets you distinguish between occured, started and ended.

    public void sampleStarted(SampleEvent e) {
        log.debug("Sample started");
        try {
            listener.sampleStarted(e);
        } catch (RemoteException err) {
            log.error("sampleStarted", err);
        }
    }

    public void sampleStopped(SampleEvent e) {
        log.debug("Sample stopped");
        try {
            listener.sampleStopped(e);
        } catch (RemoteException err) {
            log.error("sampleStopped", err);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void testIterationStart(LoopIterationEvent event) {
    }

}