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

package org.apache.jmeter.protocol.http.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.jmeter.util.SlowSocket;

/**
 * Commons HttpClient protocol factory to generate "slow" sockets for emulating dial-up modems
 */

public class SlowHttpClientSocketFactory implements ProtocolSocketFactory {

    private final int CPS; // Characters per second to emulate

    /**
     *
     * @param cps - characters per second
     */
    public SlowHttpClientSocketFactory(final int cps) {
        super();
        CPS = cps;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress clientHost,
            int clientPort) throws IOException, UnknownHostException {
        return new SlowSocket(CPS,host,port,clientHost,clientPort);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException,
            UnknownHostException {
        return new SlowSocket(CPS,host,port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort,
            HttpConnectionParams params)
    throws IOException, UnknownHostException, ConnectTimeoutException {
        int timeout = params.getConnectionTimeout();
        if (timeout == 0) {
            return new SlowSocket(CPS,host,port,localAddress,localPort);
        } else {
            return new SlowSocket(CPS,host,port,localAddress,localPort, timeout);
        }
    }
}
