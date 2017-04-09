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

import java.net.InetAddress;

import org.junit.Test;

/**
 * Try to establish why some batchtestserver runs fail 
 */
public class LocalHostTest {

    @Test
    public void testLocalHost() throws Exception {
        final String key = "java.rmi.server.hostname";
        String host = System.getProperties().getProperty(key); // $NON-NLS-1$
        perr(key + "=" + host);
        InetAddress localHost;
        if( host==null ) {
            localHost = InetAddress.getLocalHost();
        } else {
            localHost = InetAddress.getByName(host);
        }
        showAddress(localHost);
        showAddress(InetAddress.getByName("localhost"));
        for (InetAddress a : InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())) {
            perr("====================");
            showAddress(a);            
        }
    }

    private static void showAddress(InetAddress localHost) {
        perr(localHost);
        perr("isSiteLocalAddress:"+localHost.isSiteLocalAddress());
        perr("isAnyLocalAddress:"+localHost.isAnyLocalAddress());
        perr("isLinkLocalAddress:"+localHost.isLinkLocalAddress());
        perr("isLoopbackAddress:"+localHost.isLoopbackAddress());
        perr("isMulticastAddress:"+localHost.isMulticastAddress());
    }

    private static void perr(Object s) {
        System.err.println(s);
    }

}
