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
 */

package org.apache.jmeter.protocol.jms.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * ClientPool holds the client instances in an ArrayList. The main purpose of
 * this is to make it easier to clean up all the instances at the end of a test.
 * If we didn't do this, threads might become zombie.
 * 
 * N.B. This class needs to be fully synchronized as it is called from sample threads
 * and the thread that runs testEnded() methods.
 */
public class ClientPool {

    //GuardedBy("this")
    private static final ArrayList<Closeable> clients = new ArrayList<>();

    //GuardedBy("this")
    private static final Map<Object, Object> client_map = new ConcurrentHashMap<>();

    /**
     * Add a ReceiveClient to the ClientPool. This is so that we can make sure
     * to close all clients and make sure all threads are destroyed.
     *
     * @param client the ReceiveClient to add
     */
    public static synchronized void addClient(Closeable client) {
        clients.add(client);
    }

    /**
     * Clear all the clients created by either Publish or Subscribe sampler. We
     * need to do this to make sure all the threads created during the test are
     * destroyed and cleaned up. In some cases, the client provided by the
     * manufacturer of the JMS server may have bugs and some threads may become
     * zombie. In those cases, it is not the responsibility of JMeter for those
     * bugs.
     */
    public static synchronized void clearClient() {
        for (Closeable client : clients) {
            try {
                client.close();
            } catch (IOException e) {
                // Ignored
            }
            client = null;
        }
        clients.clear();
        client_map.clear();
    }

    // TODO Method with 0 reference, really useful ?
    public static void put(Object key, Object client) {
        client_map.put(key, client);
    }

    // TODO Method with 0 reference, really useful ?
    public static Object get(Object key) {
        return client_map.get(key);
    }

    /**
     * Remove publisher from clients
     * @param publisher {@link Publisher}
     */
    public static synchronized void removeClient(Publisher publisher) {
        clients.remove(publisher);
    }
}
