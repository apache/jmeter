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

/**
 * Allows notification on events occuring during the sampling process.
 * Specifically, when sampling is started, when a specific sample is obtained,
 * and when sampling is stopped.
 *
 * @version $Revision$
 */
public interface RemoteSampleListener extends java.rmi.Remote {
    public void testStarted() throws RemoteException;

    public void testStarted(String host) throws RemoteException;

    public void testEnded() throws RemoteException;

    public void testEnded(String host) throws RemoteException;

    /**
     * This method is called remotely and fires a list of samples events
     * recieved locally. The function is to reduce network load when using
     * remote testing.
     *
     * @param samples
     *            the list of sample events to be fired locally.
     * @throws RemoteException
     */
    public void processBatch(List<SampleEvent> samples) throws RemoteException;

    /**
     * A sample has started and stopped.
     */
    public void sampleOccurred(SampleEvent e) throws RemoteException;

    /**
     * A sample has started.
     */
    public void sampleStarted(SampleEvent e) throws RemoteException;

    /**
     * A sample has stopped.
     */
    public void sampleStopped(SampleEvent e) throws RemoteException;
}
