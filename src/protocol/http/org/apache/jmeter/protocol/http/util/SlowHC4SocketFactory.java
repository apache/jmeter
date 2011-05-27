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

import java.net.Socket;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.params.HttpParams;
import org.apache.jmeter.util.SlowSocket;

/**
 * Apache HttpClient protocol factory to generate "slow" sockets for emulating dial-up modems
 */

public class SlowHC4SocketFactory extends PlainSocketFactory {

    private final int CPS; // Characters per second to emulate

    /**
     * Create a factory 
     * @param cps - characters per second
     */
    public SlowHC4SocketFactory(final int cps) {
        super();
        CPS = cps;
    }

    // Override all the super-class Socket methods.
    
    @Override
    public Socket createSocket(final HttpParams params) {
        return new SlowSocket(CPS);
    }

    @Override
    public Socket createSocket() {
        return new SlowSocket(CPS);
    }
    
}
