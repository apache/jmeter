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

package org.apache.jmeter.protocol.jms.sampler.render;

import static java.lang.String.format;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.jmeter.protocol.jms.sampler.cache.Cache;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextServiceHelper;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Rule;

/**
 *
 */
public abstract class MessageRendererTest<T> {

    @SuppressWarnings("rawtypes")
    protected Entry cacheContent;
    protected Cache cache = new Cache() {
        @Override
        public void put(Object key, Object value) {
            cacheContent = new AbstractMap.SimpleImmutableEntry<>(key, value);
        }

        @Override
        public <K, V> V get(K key, Function<K, V> get) {
            V value = get.apply(key);
            cacheContent = new AbstractMap.SimpleImmutableEntry<>(key, value);
            return value;
        }
    };

    @Rule
    public JMeterContextServiceHelper jmeterCtxService = new JMeterContextServiceHelper() {
        @Override
        protected void initContext(JMeterContext jMeterContext) {
            jMeterContext.setVariables(new JMeterVariables());
        }
    };

    protected abstract MessageRenderer<T> getRenderer();

    protected String getResourceFile(String resource) {
        return format("test/resources/%s/%s", getClass().getPackage().getName().replace('.', '/'), resource);
    }

    protected void assertValueFromFile(Consumer<T> assertion, String resource, boolean hasVariable) {
        String filename = getResourceFile(resource);
        T actual = getRenderer().getValueFromFile(filename, "UTF-8", hasVariable, cache);
        assertion.accept(actual);
    }

}
