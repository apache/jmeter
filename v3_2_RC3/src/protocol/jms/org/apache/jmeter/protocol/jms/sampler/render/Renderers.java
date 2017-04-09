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
import java.util.Map;

/**
 * Renderer API entry point.
 */
public interface Renderers {

    public static MessageRenderer<String> getText() {
        return RendererFactory.getInstance().getText();
    }

    public static MessageRenderer<byte[]> getBinary() {
        return RendererFactory.getInstance().getBinary();
    }

    public static MessageRenderer<Serializable> getObject() {
        return RendererFactory.getInstance().getObject();
    }

    public static MessageRenderer<Map<String,Object>> getMap() {
        return RendererFactory.getInstance().getMap();
    }

    public static <T> MessageRenderer<T> getInstance(Class<T> type) {
        return RendererFactory.getInstance().getInstance(type);
    }
}
