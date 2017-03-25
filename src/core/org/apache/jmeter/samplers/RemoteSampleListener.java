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
 * Allows notification on events occurring during the sampling process.
 * Specifically, when sampling is started, when a specific sample is obtained,
 * and when sampling is stopped.
 *
 */
public interface RemoteSampleListener extends java.rmi.Remote {
    void testStarted() throws RemoteException;

    void testStarted(String host) throws RemoteException;

    void testEnded() throws RemoteException;

    void testEnded(String host) throws RemoteException;

    // Not currently needed by any Remoteable classes
    // Anyway, would probably be too expensive in terms of network traffic
    // 
    // void testIterationStart(LoopIterationEvent event);
    
    /**
     * This method is called remotely and fires a list of samples events
     * received locally. The function is to reduce network load when using
     * remote testing.
     *
     * @param samples
     *            the list of sample events to be fired locally.
     * @throws RemoteException when calling the remote method fails
     */
    void processBatch(List<SampleEvent> samples) throws RemoteException;

    /**
     * A sample has started and stopped.
     * 
     * @param e
     *            the event with data about the completed sample
     * @throws RemoteException when calling the remote method fails
     */
    void sampleOccurred(SampleEvent e) throws RemoteException;

    /**
     * A sample has started.
     * 
     * @param e
     *            the event with data about the started sample
     * @throws RemoteException when calling the remote method fails
     */
    void sampleStarted(SampleEvent e) throws RemoteException;

    /**
     * A sample has stopped.
     * 
     * @param e
     *            the event with data about the stopped sample
     * @throws RemoteException when calling the remote method fails
     */
    void sampleStopped(SampleEvent e) throws RemoteException;
}
