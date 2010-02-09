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

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @version $Revision$
 */
public class RemoteTestListenerWrapper extends AbstractTestElement implements TestListener, Serializable, NoThreadClone {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 240L;

    private final RemoteSampleListener listener;

    public RemoteTestListenerWrapper() {
        log.warn("Only intended for use in testing");
        listener = null;
    }

    public RemoteTestListenerWrapper(RemoteSampleListener l) {
        listener = l;
    }

    public void testStarted() {
        try {
            listener.testStarted();
        } catch (Exception ex) {
            log.error("", ex); // $NON-NLS-1$
        }

    }

    public void testEnded() {
        try {
            listener.testEnded();
        } catch (Exception ex) {
            log.error("", ex); // $NON-NLS-1$
        }
    }

    public void testStarted(String host) {
        try {
            listener.testStarted(host);
        } catch (Exception ex) {
            log.error("", ex); // $NON-NLS-1$
        }
    }

    public void testEnded(String host) {
        try {
            listener.testEnded(host);
        } catch (Exception ex) {
            log.error("", ex); // $NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    public void testIterationStart(LoopIterationEvent event) {
        //listener.testIterationStart(event);
    }

}