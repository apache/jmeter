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

package org.apache.jmeter.engine;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

import org.apache.jorphan.collections.HashTree;

/**
 * This is the interface for the RMI server engine, i.e. {@link RemoteJMeterEngineImpl}
 */
public interface RemoteJMeterEngine extends Remote {
    void rconfigure(HashTree testTree, String host, File jmxBase, String scriptName) throws RemoteException;

    void rrunTest() throws RemoteException, JMeterEngineException;

    void rstopTest(boolean now) throws RemoteException;

    void rreset() throws RemoteException;

    @SuppressWarnings("NonApiType")
    void rsetProperties(HashMap<String,String> p) throws RemoteException;

    void rexit() throws RemoteException;
}
