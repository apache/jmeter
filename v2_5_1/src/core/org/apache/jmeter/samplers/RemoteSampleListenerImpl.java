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

import java.rmi.RemoteException;
import java.util.List;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.testelement.TestListener;

/**
 * Implementation of remote sampler listener, also supports TestListener
 */
public class RemoteSampleListenerImpl extends java.rmi.server.UnicastRemoteObject 
    implements RemoteSampleListener, SampleListener, TestListener {

    private static final long serialVersionUID = 240L;

    private final TestListener testListener;

    private final SampleListener sampleListener;

    public RemoteSampleListenerImpl(Object listener) throws RemoteException {
        super();
        if (listener instanceof TestListener) {
            testListener = (TestListener) listener;
        } else {
            testListener = null;
        }
        if (listener instanceof SampleListener) {
            sampleListener = (SampleListener) listener;
        } else {
            sampleListener = null;
        }
    }

    public void testStarted() {
        if (testListener != null) {
            testListener.testStarted();
        }
    }

    public void testStarted(String host) {
        if (testListener != null) {
            testListener.testStarted(host);
        }
    }

    public void testEnded() {
        if (testListener != null) {
            testListener.testEnded();
        }
    }

    public void testEnded(String host) {
        if (testListener != null) {
            testListener.testEnded(host);
        }
    }

    public void testIterationStart(LoopIterationEvent event) {
        if (testListener != null) {
            testListener.testIterationStart(event);
        }
    }

    /**
     * This method is called remotely and fires a list of samples events
     * received locally. The function is to reduce network load when using
     * remote testing.
     *
     * @param samples
     *            the list of sample events to be fired locally
     */
    public void processBatch(List<SampleEvent> samples) {
        if (samples != null && sampleListener != null) {
            for (SampleEvent e : samples) {
                sampleListener.sampleOccurred(e);                
            }
        }
    }

    public void sampleOccurred(SampleEvent e) {
        if (sampleListener != null) {
            sampleListener.sampleOccurred(e);
        }
    }

    /**
     * A sample has started.
     */
    public void sampleStarted(SampleEvent e) {
        if (sampleListener != null) {
            sampleListener.sampleStarted(e);
        }
    }

    /**
     * A sample has stopped.
     */
    public void sampleStopped(SampleEvent e) {
        if (sampleListener != null) {
            sampleListener.sampleStopped(e);
        }
    }
}
