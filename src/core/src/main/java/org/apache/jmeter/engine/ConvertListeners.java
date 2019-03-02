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

package org.apache.jmeter.engine;

import java.rmi.RemoteException;

import org.apache.jmeter.samplers.RemoteListenerWrapper;
import org.apache.jmeter.samplers.RemoteSampleListener;
import org.apache.jmeter.samplers.RemoteSampleListenerImpl;
import org.apache.jmeter.samplers.RemoteSampleListenerWrapper;
import org.apache.jmeter.samplers.RemoteTestListenerWrapper;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.RemoteThreadsListenerImpl;
import org.apache.jmeter.threads.RemoteThreadsListenerTestElement;
import org.apache.jmeter.threads.RemoteThreadsListenerWrapper;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts the Remoteable Test and Sample Listeners in the test tree by wrapping
 * them with RemoteSampleListeners so that the samples are returned to the client.
 * 
 * N.B. Does not handle ThreadListeners.
 * 
 */
public class ConvertListeners implements HashTreeTraverser {
    private static final Logger log = LoggerFactory.getLogger(ConvertListeners.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNode(Object node, HashTree subTree) {
        for (Object item : subTree.list()) {
            if (item instanceof AbstractThreadGroup && log.isDebugEnabled()) {
                log.debug("num threads = {}", ((AbstractThreadGroup) item).getNumThreads());
            }
            if (item instanceof Remoteable) {
                if (item instanceof RemoteThreadsListenerTestElement){
                    // Used for remote notification of threads start/stop,see BUG 54152
                    try {
                        RemoteThreadsListenerWrapper wrapper = new RemoteThreadsListenerWrapper(new RemoteThreadsListenerImpl());
                        subTree.replaceKey(item, wrapper);
                    } catch (RemoteException e) {
                        log.error("Error replacing {} by wrapper: {}", RemoteThreadsListenerTestElement.class,
                                RemoteThreadsListenerWrapper.class, e);
                    }
                    continue;
                }
                if (item instanceof ThreadListener){
                    // TODO Document the reason for this
                    log.error("Cannot handle ThreadListener Remotable item: {}", item.getClass());
                    continue;
                }
                try {
                    RemoteSampleListener rtl = new RemoteSampleListenerImpl(item);
                    if (item instanceof TestStateListener && item instanceof SampleListener) { // TL - all
                        RemoteListenerWrapper wrap = new RemoteListenerWrapper(rtl);
                        subTree.replaceKey(item, wrap);
                    } else if (item instanceof TestStateListener) {
                        RemoteTestListenerWrapper wrap = new RemoteTestListenerWrapper(rtl);
                        subTree.replaceKey(item, wrap);
                    } else if (item instanceof SampleListener) {
                        RemoteSampleListenerWrapper wrap = new RemoteSampleListenerWrapper(rtl);
                        subTree.replaceKey(item, wrap);
                    } else {
                        if (log.isWarnEnabled()) {
                            log.warn("Could not replace Remotable item: {}", item.getClass());
                        }
                    }
                } catch (RemoteException e) {
                    log.error("RemoteException occurred while replacing Remotable item.", e); // $NON-NLS-1$
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subtractNode() {
        // NOOP
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processPath() {
        // NOOP
    }

}
