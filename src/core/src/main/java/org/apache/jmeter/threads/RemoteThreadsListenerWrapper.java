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

package org.apache.jmeter.threads;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * server side wrapper, used to notify RMI client
 * @since 2.10
 */
public class RemoteThreadsListenerWrapper extends AbstractTestElement implements ThreadListener, Serializable,
        NoThreadClone {
    private static final Logger log = LoggerFactory.getLogger(RemoteThreadsListenerWrapper.class);

    private static final long serialVersionUID = 241L;

    private RemoteThreadsListener listener;

    public RemoteThreadsListenerWrapper(RemoteThreadsListener l) {
        listener = l;
    }

    public RemoteThreadsListenerWrapper() {
    }

    @Override
    public void threadStarted() {
        try {
            listener.threadStarted();
        } catch (RemoteException err) {
            log.error("Exception invoking listener on threadStarted.", err); // $NON-NLS-1$
        }
    }

    @Override
    public void threadFinished() {
        try {
            listener.threadFinished();
        } catch (RemoteException err) {
            log.error("Exception invoking listener on threadFinished.", err); // $NON-NLS-1$
        }
    }
}
