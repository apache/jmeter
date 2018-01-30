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

package org.apache.jmeter.rmi;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

import javax.net.ServerSocketFactory;

/**
 * Custom {@link RMIServerSocketFactory} that binds RMI to particular host/ip
 * @since 4.0
 */
public class RMIServerSocketFactoryImpl implements RMIServerSocketFactory, Serializable {

    private static final long serialVersionUID = 6106381149147208254L;
    private final InetAddress localAddress;


    public RMIServerSocketFactoryImpl(final InetAddress pAddress) {
        this.localAddress = pAddress;
    }

    /**
     * Creates a server socket that listens on localAddress:port
     * @param port to use for the RMI server socket 
     * @see java.rmi.server.RMIServerSocketFactory#createServerSocket(int)
     */
    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return ServerSocketFactory.getDefault().createServerSocket(port, 0, localAddress);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RMIServerSocketFactoryImpl other = (RMIServerSocketFactoryImpl) obj;
        if (localAddress == null) {
            if (other.localAddress != null) {
                return false;
            }
        } else if (!localAddress.equals(other.localAddress)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return (localAddress == null) ? 0 : localAddress.hashCode();
    }

    @Override
    public String toString() {
        return "RMIServerSocketFactoryImpl(host=" + localAddress + ")";
    }
}
