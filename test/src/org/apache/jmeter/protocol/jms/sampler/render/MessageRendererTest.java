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

import java.util.function.Consumer;

import org.apache.jmeter.test.ResourceLocator;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextServiceHelper;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Rule;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 *
 */
public abstract class MessageRendererTest<T> {

    protected Cache<Object,Object> cache = Caffeine.newBuilder().build();

    protected Object getFirstCachedValue() {
        return cache.asMap().values().stream().findFirst().get();
    }

    @Rule
    public JMeterContextServiceHelper jmeterCtxService = new JMeterContextServiceHelper() {
        @Override
        protected void initContext(JMeterContext jMeterContext) {
            jMeterContext.setVariables(new JMeterVariables());
        }
    };

    protected abstract MessageRenderer<T> getRenderer();

    protected String getResourceFile(String resource) {
        return ResourceLocator.getResource(this, resource);
    }

    protected void assertValueFromFile(Consumer<T> assertion, String resource, boolean hasVariable) {
        String filename = getResourceFile(resource);
        T actual = getRenderer().getValueFromFile(filename, "UTF-8", hasVariable, cache);
        assertion.accept(actual);
    }

}
