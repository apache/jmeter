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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Renderer singleton.
 */
enum RendererFactory {

    INSTANCE;

    public static RendererFactory getInstance() {
        return INSTANCE;
    }

    private TextMessageRenderer   text   = new TextMessageRenderer();
    private BinaryMessageRenderer binary = new BinaryMessageRenderer(text);
    private ObjectMessageRenderer object = new ObjectMessageRenderer(text);
    private MapMessageRenderer    map    = new MapMessageRenderer(text);

    /** Registered renderers **/
    private Map<Class<?>, MessageRenderer<?>> renderers;
    {
        Map<Class<?>, MessageRenderer<?>> writable = new LinkedHashMap<>();
        writable.put(String.class, text);
        writable.put(byte[].class, binary);
        writable.put(Serializable.class, object);
        writable.put(Map.class, map);
        renderers = Collections.unmodifiableMap(writable);
    }

    public TextMessageRenderer getText() {
        return text;
    }

    public BinaryMessageRenderer getBinary() {
        return binary;
    }

    public ObjectMessageRenderer getObject() {
        return object;
    }

    public MapMessageRenderer getMap() {
        return map;
    }

    @SuppressWarnings("unchecked")
    public <T> MessageRenderer<T> getInstance(Class<T> type) {
        return (MessageRenderer<T>) renderers.get(type);
    }
}
