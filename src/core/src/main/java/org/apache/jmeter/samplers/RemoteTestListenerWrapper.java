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

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class RemoteTestListenerWrapper extends AbstractTestElement implements TestStateListener, Serializable, NoThreadClone {
    private static final Logger log = LoggerFactory.getLogger(RemoteTestListenerWrapper.class);

    private static final long serialVersionUID = 241L;

    private final RemoteSampleListener listener;

    public RemoteTestListenerWrapper() {
        log.warn("Only intended for use in testing");
        listener = null;
    }

    public RemoteTestListenerWrapper(RemoteSampleListener l) {
        listener = l;
    }

    @Override
    public void testStarted() {
        try {
            listener.testStarted();
        } catch (Exception ex) {
            log.error("Exception on testStarted.", ex); // $NON-NLS-1$
        }

    }

    @Override
    public void testEnded() {
        try {
            listener.testEnded();
        } catch (Exception ex) {
            log.error("Exception on testEnded.", ex); // $NON-NLS-1$
        }
    }

    @Override
    public void testStarted(String host) {
        try {
            listener.testStarted(host);
        } catch (Exception ex) {
            log.error("Exception on testStarted on host {}", host, ex); // $NON-NLS-1$
        }
    }

    @Override
    public void testEnded(String host) {
        try {
            listener.testEnded(host);
        } catch (Exception ex) {
            log.error("Exception on testEnded on host {}", host, ex); // $NON-NLS-1$
        }
    }

}
