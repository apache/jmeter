/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.samplers;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import org.apache.jmeter.rmi.RmiUtils;
import org.apache.jmeter.samplers.SampleListenerExecutionMode.Mode;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Implementation of remote sampler listener, also supports TestStateListener
 */
@SampleListenerExecutionMode(mode = Mode.ThreadUnbound)
public class RemoteSampleListenerImpl extends java.rmi.server.UnicastRemoteObject
    implements RemoteSampleListener, SampleListener, TestStateListener {

    private static final long serialVersionUID = 240L;

    private final TestStateListener testListener;

    private final List<SampleListener> sampleListeners;

    private static final int DEFAULT_LOCAL_PORT = addOffset(
        JMeterUtils.getPropDefault("client.rmi.localport", 0), 2); // $NON-NLS-1$

    public RemoteSampleListenerImpl(Object listener) throws RemoteException {
        super(DEFAULT_LOCAL_PORT, RmiUtils.createClientSocketFactory(),  RmiUtils.createServerSocketFactory());
        if (listener instanceof TestStateListener) {
            testListener = (TestStateListener) listener;
        } else {
            testListener = null;
        }
        if (listener instanceof SampleListener) {
            sampleListeners = Arrays.asList((SampleListener) listener);
        } else {
            sampleListeners = null;
        }
    }

    private static int addOffset(int port, int offset) {
        if (port == 0) {
            return 0;
        }
        return port + offset;
    }

    @Override
    public void testStarted() {
        if (testListener != null) {
            testListener.testStarted();
        }
    }

    @Override
    public void testStarted(String host) {
        if (testListener != null) {
            testListener.testStarted(host);
        }
    }

    @Override
    public void testEnded() {
        if (testListener != null) {
            testListener.testEnded();
        }
    }

    @Override
    public void testEnded(String host) {
        if (testListener != null) {
            testListener.testEnded(host);
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
    @Override
    public void processBatch(List<SampleEvent> samples) {
        if (samples != null && sampleListeners != null) {
        	ListenerNotifier listenerNotifier = ListenerNotifier.getInstance();
            for (SampleEvent e : samples) {
            	listenerNotifier.notifyListeners(e, sampleListeners);
            }
        }
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        if (sampleListeners != null) {
            ListenerNotifier.getInstance().notifyListeners(e, sampleListeners);
        }
    }

    /**
     * A sample has started.
     */
    @Override
    public void sampleStarted(SampleEvent e) {
        if (sampleListeners != null) {
            for (SampleListener sampleListener : sampleListeners) {
                sampleListener.sampleStarted(e);
            }
        }
    }

    /**
     * A sample has stopped.
     */
    @Override
    public void sampleStopped(SampleEvent e) {
        if (sampleListeners != null) {
            for (SampleListener sampleListener : sampleListeners) {
                sampleListener.sampleStopped(e);
            }
        }
    }
}
