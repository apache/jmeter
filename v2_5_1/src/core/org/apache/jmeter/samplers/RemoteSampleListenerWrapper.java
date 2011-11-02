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
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @version $Revision$
 */

public class RemoteSampleListenerWrapper extends AbstractTestElement implements SampleListener, Serializable,
        NoThreadClone {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 240L;

    private RemoteSampleListener listener;

    public RemoteSampleListenerWrapper(RemoteSampleListener l) {
        listener = l;
    }

    public RemoteSampleListenerWrapper() {
    }

    public void sampleOccurred(SampleEvent e) {
        try {
            listener.sampleOccurred(e);
        } catch (RemoteException err) {
            log.error("", err); // $NON-NLS-1$
        }
    }

    public void sampleStarted(SampleEvent e) {
        try {
            listener.sampleStarted(e);
        } catch (RemoteException err) {
            log.error("", err); // $NON-NLS-1$
        }
    }

    public void sampleStopped(SampleEvent e) {
        try {
            listener.sampleStopped(e);
        } catch (RemoteException err) {
            log.error("", err); // $NON-NLS-1$
        }
    }
}