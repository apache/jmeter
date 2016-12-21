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

import static org.junit.Assert.assertSame;

import org.junit.Test;

public class SimpleCacheTest {

    private SimpleCache cache = new SimpleCache();

    @Test
    public void put() {
        String value = "banana";
        String key = "key";
        cache.put(key, value);

        String actual = cache.get(key, k -> "none");

        assertSame(value, actual);
    }

    @Test
    public void getWithNoCacheValue() {
        String value = "banana";
        String key = "key";

        String actual = cache.get(key, k -> value);

        assertSame(value, actual);
    }
}
