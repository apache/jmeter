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

import java.rmi.RemoteException;

import org.apache.jmeter.junit.JMeterTestCase;
import org.junit.jupiter.api.Test;

/**
 * Class Name:     RemoteWorkerIPAddressTests
 *
 * Purpose:        Test JMeter's ability to parse IPv6 addresses when specifying the IP address of a remote worker machine.
 *
 * Description:    Previously, JMeter supported parsing of IPv4 addresses only when specifying the IP address of a remote
 *                 worker machine.  If an IPv6 address was parsed, a NumberFormatException would be thrown.
 *                 A pull request was created on 2021-08-18 to add support for IPv6 remote address.
 *                 This Java class was created to test the pull request's ability to parse IPv6 address.
 *
 * Author:         Peter Wong (Principal Developer at Connected Solution Experts)
 */
public class RemoteWorkerIPAddressTests extends JMeterTestCase {

    @Test
    public void testIPv4RemoteWorkerAddress() throws Exception {
        try {
            ClientJMeterEngine clientJMeterEngine = new ClientJMeterEngine("127.0.0.1"); // IP v4
        } catch (RemoteException remoteException) {
            // Ignore this exception, because this unit test focuses on the ability of JMeter to parse
            // IPv6 remote worker addresses. RemoteException is networking-related, and is beyond the scope of this test.
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void testIPv6RemoteWorkerAddress() throws Exception {
        try {
            ClientJMeterEngine clientJMeterEngine = new ClientJMeterEngine("::1"); // IP v6
        } catch (RemoteException remoteException) {
            // Ignore this exception, because this unit test focuses on the ability of JMeter to parse
            // IPv6 remote worker addresses. RemoteException is networking-related, and is beyond the scope of this test.
        } catch (Exception e) {
            throw e;
        }
    }
}
