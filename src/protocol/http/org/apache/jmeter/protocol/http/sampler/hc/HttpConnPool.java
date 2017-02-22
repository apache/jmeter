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

package org.apache.jmeter.protocol.http.sampler.hc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.pool.AbstractConnPool;
import org.apache.http.pool.ConnFactory;
import org.slf4j.Logger;

/**
 * Implementation of AbstractConnPool to allow better connection validity 
 * management.
 * @since 3.2
 */
public class HttpConnPool extends AbstractConnPool<HttpRoute, OperatedClientConnection, HttpPoolEntry> {

    private static final LongAdder COUNTER = new LongAdder();

    private final Logger log;
    private final long timeToLive;
    private final TimeUnit tunit;

    public HttpConnPool(final Logger log,
            final ClientConnectionOperator connOperator,
            final int defaultMaxPerRoute, final int maxTotal,
            final long timeToLive, final TimeUnit tunit) {
        super(new InternalConnFactory(connOperator), defaultMaxPerRoute, maxTotal);
        this.log = log;
        this.timeToLive = timeToLive;
        this.tunit = tunit;
    }

    @Override
    protected HttpPoolEntry createEntry(final HttpRoute route, final OperatedClientConnection conn) {
        final String id = Long.toString(COUNTER.sum());
        return new HttpPoolEntry(this.log, id, route, conn, this.timeToLive, this.tunit);
    }

    static class InternalConnFactory implements ConnFactory<HttpRoute, OperatedClientConnection> {

        private final ClientConnectionOperator connOperator;

        InternalConnFactory(final ClientConnectionOperator connOperator) {
            this.connOperator = connOperator;
        }

        @Override
        public OperatedClientConnection create(final HttpRoute route) throws IOException {
            return connOperator.createConnection();
        }
    }
}
