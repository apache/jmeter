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
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

/**
 * Try to establish why some batchtestserver runs fail 
 */
public class LocalHostTest {

    @Test
    public void testInterfaces() throws Exception {
        String interfaces = Collections
                .list(NetworkInterface.getNetworkInterfaces()).stream()
                .map(this::ifaceWithAddresses)
                .collect(Collectors.joining(", "));
        perr("Interfaces: {" + interfaces + "}");
        InetAddress localHost = getLocalHost();
        boolean localHostIsBound = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                .flatMap(iface -> Collections.list(iface.getInetAddresses())
                        .stream())
                .filter(addr -> addr.equals(localHost))
                .findFirst()
                .isPresent();
        Assert.assertTrue("localHost: " + localHost + " is bound to an interface", localHostIsBound);
    }

    private String ifaceWithAddresses(NetworkInterface iface) {
        return iface + " => ["
                + Collections.list(iface.getInetAddresses()).stream()
                        .map(InetAddress::toString)
                        .collect(Collectors.joining(", "))
                + "]";
    }

    @Test
    public void testLocalHost() throws Exception {
        InetAddress localHost = getLocalHost();
        showAddress(localHost);
        showAddress(InetAddress.getByName("localhost"));
        for (InetAddress a : InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())) {
            perr("====================");
            showAddress(a);            
        }
    }

    private InetAddress getLocalHost() throws UnknownHostException {
        final String key = "java.rmi.server.hostname";
        String host = System.getProperties().getProperty(key); // $NON-NLS-1$
        perr(key + "=" + host);
        InetAddress localHost;
        if( host==null ) {
            localHost = InetAddress.getLocalHost();
        } else {
            localHost = InetAddress.getByName(host);
        }
        return localHost;
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
