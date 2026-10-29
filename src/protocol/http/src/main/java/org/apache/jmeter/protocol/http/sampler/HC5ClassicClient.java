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

package org.apache.jmeter.protocol.http.sampler;

import java.io.Closeable;
import java.io.IOException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;

/**
 * Client of {@link HTTPHC5Impl} for HTTP/1.1, with the pool of its connections, which a JMeter
 * thread shares with the threads that download its embedded resources in parallel.
 *
 * @param client the client
 * @param pool   the pool of the connections of the client
 */
record HC5ClassicClient(CloseableHttpClient client, PoolingHttpClientConnectionManager pool) implements Closeable {

    /**
     * Lets the pool open as many connections to a host as requests are made to it at the same
     * time, rather than have those beyond the default of HttpClient, five per host, wait for one,
     * which would add to their elapsed time. The limits are only ever raised, so a sampler that
     * downloads less in parallel than another one using the same client does not lower them.
     *
     * @param connections number of requests made at the same time
     */
    synchronized void allowConnectionsPerRoute(int connections) {
        if (pool.getDefaultMaxPerRoute() < connections) {
            pool.setDefaultMaxPerRoute(connections);
        }
        if (pool.getMaxTotal() < connections) {
            pool.setMaxTotal(connections);
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
