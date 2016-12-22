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

package org.apache.jmeter.protocol.jms.sampler.cache;

import java.util.function.Function;

/**
 * Basic cache interface.
 */
public interface Cache {

    /** Force a cache value **/
    void put(Object key, Object value);

    /** Check if key is already cached, or execute function and cache value **/
    <T, R> R get(T key, Function<T, R> get);

    /** Non-caching implementation **/
    static Cache getNoopCache() {
        return NoopCache.getInstance();
    }

    /** Caching a single value, if key change, cached value is thrown away. <code>null</code> key is supported. **/
    static Cache newSingleInstance() {
        return new SimpleCache();
    }
}
