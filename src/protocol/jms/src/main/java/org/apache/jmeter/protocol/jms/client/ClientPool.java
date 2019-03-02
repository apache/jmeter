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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jorphan.util.JOrphanUtils;

/**
 *
 * ClientPool holds the client instances in an ArrayList. The main purpose of
 * this is to make it easier to clean up all the instances at the end of a test.
 * If we didn't do this, threads might become zombie.
 *
 * N.B. This class is thread safe as it is called from sample threads
 * and the thread that runs testEnded() methods.
 */
public class ClientPool {

    private static final List<Closeable> CLIENTS = Collections.synchronizedList(new ArrayList<>());

    private ClientPool() {
        super();
    }

    /**
     * Add a ReceiveClient to the ClientPool. This is so that we can make sure
     * to close all clients and make sure all threads are destroyed.
     *
     * @param client the ReceiveClient to add
     */
    public static void addClient(Closeable client) {
        CLIENTS.add(client);
    }

    /**
     * Remove publisher from clients
     * @param publisher {@link Publisher}
     */
    public static void removeClient(Publisher publisher) {
        CLIENTS.remove(publisher);
    }

    /**
     * Clear all the clients created by either Publish or Subscribe sampler. We
     * need to do this to make sure all the threads created during the test are
     * destroyed and cleaned up. In some cases, the client provided by the
     * manufacturer of the JMS server may have bugs and some threads may become
     * zombie. In those cases, it is not JMeter's responsibility.
     */
    public static void clearClient() {
        synchronized (CLIENTS) {
            CLIENTS.forEach(JOrphanUtils::closeQuietly);
            // Clear should be inside synchronized block to avoid closing the same client
            // multiple times when clearClient is called from multiple threads concurrently
            CLIENTS.clear();
        }
    }
}
