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

package org.apache.jmeter.threads;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.ServiceLoader;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.rmi.RmiUtils;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.LogAndIgnoreServiceLoadExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RMI Implementation, client side code (ie executed on Controller)
 * @since 2.10
 */
public class RemoteThreadsListenerImpl extends UnicastRemoteObject implements
        RemoteThreadsListener, ThreadListener {
    private static final Logger log = LoggerFactory.getLogger(RemoteThreadsListenerImpl.class);

    private final Collection<RemoteThreadsLifeCycleListener> listeners =
            JMeterUtils.loadServicesAndScanJars(
                    RemoteThreadsLifeCycleListener.class,
                    ServiceLoader.load(RemoteThreadsLifeCycleListener.class),
                    Thread.currentThread().getContextClassLoader(),
                    new LogAndIgnoreServiceLoadExceptionHandler(log)
            );

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    private static final int DEFAULT_LOCAL_PORT = addOffset(
            JMeterUtils.getPropDefault("client.rmi.localport", 0), 1); // $NON-NLS-1$

    /**
     * @throws RemoteException if failed to export object
     */
    public RemoteThreadsListenerImpl() throws RemoteException {
        super(DEFAULT_LOCAL_PORT, RmiUtils.createClientSocketFactory(), RmiUtils.createServerSocketFactory());
    }

    private static int addOffset(int port, int offset) {
        if (port == 0) {
            return 0;
        }
        return port + offset;
    }

    /**
     *
     * @see RemoteThreadsListener#threadStarted()
     */
    @Override
    public void threadStarted() {
        JMeterContextService.incrNumberOfThreads();
        GuiPackage gp =GuiPackage.getInstance();
        if (gp != null) {// check there is a GUI
            gp.getMainFrame().updateCounts();
        }
        for (RemoteThreadsLifeCycleListener listener : listeners) {
            listener.threadNumberIncreased(JMeterContextService.getNumberOfThreads());
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.samplers.RemoteThreadsListener#threadFinished()
     */
    @Override
    public void threadFinished() {
        JMeterContextService.decrNumberOfThreads();
        GuiPackage gp =GuiPackage.getInstance();
        if (gp != null) {// check there is a GUI
            gp.getMainFrame().updateCounts();
        }
        for (RemoteThreadsLifeCycleListener listener : listeners) {
            listener.threadNumberDecreased(JMeterContextService.getNumberOfThreads());
        }
    }
}
