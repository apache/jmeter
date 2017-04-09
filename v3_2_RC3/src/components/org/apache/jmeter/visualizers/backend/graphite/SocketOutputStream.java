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

package org.apache.jmeter.visualizers.backend.graphite;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Convenience class for writing bytes to a {@linkplain java.net.Socket}.
 * @since 2.13
 */
public class SocketOutputStream extends FilterOutputStream {

    private final Socket socket;

    public SocketOutputStream(InetSocketAddress inetSocketAddress) throws IOException {
        this(new Socket(inetSocketAddress.getAddress(), inetSocketAddress.getPort()));
    }

    public SocketOutputStream(Socket socket) throws IOException {
        super(socket.getOutputStream());
        this.socket = socket;
    }

    /**
     * Return the underlying Socket
     *
     * @return the underlying {@link Socket}
     */
    public Socket getSocket() {
        return socket;
    }

    @Override
    public String toString() {
        return "SocketOutputStream{" +
                "socket=" + socket +
                '}';
    }

}
