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

import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.jorphan.util.JOrphanUtils;

/**
 * Pool Factory of {@link SocketOutputStream}
 * @since 2.13
 */
public class SocketOutputStreamPoolFactory
    extends BaseKeyedPooledObjectFactory<SocketConnectionInfos, SocketOutputStream>
    implements KeyedPooledObjectFactory<SocketConnectionInfos, SocketOutputStream> {

    private final int socketTimeoutInMillis;
    private final int socketConnectTimeoutInMillis;

    public SocketOutputStreamPoolFactory(int socketConnectTimeoutInMillis, int socketTimeoutInMillis) {
        this.socketConnectTimeoutInMillis = socketConnectTimeoutInMillis;
        this.socketTimeoutInMillis = socketTimeoutInMillis;
    }

    @Override
    public PooledObject<SocketOutputStream> makeObject(SocketConnectionInfos connectionInfos) throws Exception {
        return wrap(create(connectionInfos));
    }

    @Override
    public void destroyObject(SocketConnectionInfos socketConnectionInfos, PooledObject<SocketOutputStream> socketOutputStream) throws Exception {
        super.destroyObject(socketConnectionInfos, socketOutputStream);
        SocketOutputStream outputStream = socketOutputStream.getObject();
        JOrphanUtils.closeQuietly(outputStream);
        JOrphanUtils.closeQuietly(outputStream.getSocket());
    }

    /**
     */
    @Override
    public boolean validateObject(SocketConnectionInfos hostAndPort, PooledObject<SocketOutputStream> socketOutputStream) {
        Socket socket = socketOutputStream.getObject().getSocket();
        return socket.isConnected()
                && socket.isBound()
                && !socket.isClosed()
                && !socket.isInputShutdown()
                && !socket.isOutputShutdown();
    }

    @Override
    public SocketOutputStream create(SocketConnectionInfos connectionInfos)
            throws Exception {
        Socket socket = new Socket(); // NOSONAR closed by destroyObject
        socket.setKeepAlive(true);
        socket.setSoTimeout(socketTimeoutInMillis);
        socket.connect(new InetSocketAddress(connectionInfos.getHost(), connectionInfos.getPort()), socketConnectTimeoutInMillis);

        return new SocketOutputStream(socket);
    }

    @Override
    public PooledObject<SocketOutputStream> wrap(SocketOutputStream outputStream) {
        return new DefaultPooledObject<>(outputStream);
    }
}
