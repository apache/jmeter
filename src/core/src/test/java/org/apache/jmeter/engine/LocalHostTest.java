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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.junit.jupiter.api.Test;

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
        String externInterface = guessExternalIPv4Interface();
        perr("Choose " + externInterface + " to talk to external services");
        String localHost = getLocalHost().getHostAddress();
        boolean localHostIsBound = Collections
                .list(NetworkInterface.getNetworkInterfaces()).stream()
                .flatMap(iface -> iface.getInterfaceAddresses().stream())
                .filter(iface -> iface.getAddress().getAddress().length == 4) // hack to prevent checking IPv6
                .map(this::toSubnetInfo)
                .anyMatch(subnetInfo -> subnetInfo.isInRange(localHost));
        assertTrue(localHostIsBound, () -> "localHost: " + localHost + " is bound to an interface");
    }

    private String guessExternalIPv4Interface() throws SocketException {
        return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                .map(n -> Collections.list(n.getInetAddresses()))
                .flatMap(ArrayList<InetAddress>::stream)
                .filter(i -> !i.isLoopbackAddress())
                .filter(i -> i.getAddress().length == 4)
                .map(InetAddress::getHostAddress)
                .findFirst()
                .orElse("NO DEVICE");
    }

    private SubnetInfo toSubnetInfo(InterfaceAddress addr) {
        return new SubnetUtils(
                addr.getAddress().getHostAddress() + "/" + addr.getNetworkPrefixLength())
                        .getInfo();
    }

    private String ifaceWithAddresses(NetworkInterface iface) {
        return iface + " => ["
                + iface.getInterfaceAddresses().stream()
                        .map(InterfaceAddress::toString)
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
        if (host == null) {
            localHost = InetAddress.getLocalHost();
        } else {
            localHost = InetAddress.getByName(host);
        }
        return localHost;
    }

    private static void showAddress(InetAddress localHost) {
        perr(localHost);
        perr("isSiteLocalAddress:" + localHost.isSiteLocalAddress());
        perr("isAnyLocalAddress:" + localHost.isAnyLocalAddress());
        perr("isLinkLocalAddress:" + localHost.isLinkLocalAddress());
        perr("isLoopbackAddress:" + localHost.isLoopbackAddress());
        perr("isMulticastAddress:" + localHost.isMulticastAddress());
    }

    private static void perr(Object s) {
        System.err.println(s);
    }

}
