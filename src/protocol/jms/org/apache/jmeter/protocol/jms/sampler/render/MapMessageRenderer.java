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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.github.benmanes.caffeine.cache.Cache;

public class MapMessageRenderer implements MessageRenderer<Map<String,Object>> {

    private TextMessageRenderer delegate;

    public MapMessageRenderer(TextMessageRenderer delegate) {
        this.delegate = delegate;
    }

    @Override
    public Map<String, Object> getValueFromText(String text) {
        Map<String,Object> m = new HashMap<>();
        String[] lines = text.split("\n");
        for (String line : lines){
            String[] parts = line.split(",",3);
            if (parts.length != 3) {
                throw new IllegalArgumentException("line must have 3 parts: "+line);
            }
            String name = parts[0];
            String type = parts[1];
            if (!type.contains(".")){// Allow shorthand names
                type = "java.lang."+type;
            }
            String value = parts[2];
            Object obj;
            if (type.equals("java.lang.String")){
                obj = value;
            } else {
                try {
                    Class<?> clazz = Class.forName(type);
                    Method method = clazz.getMethod("valueOf", String.class);
                    obj = method.invoke(clazz, value);
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(String.format("Can't convert %s to object", line), e);
                }
            }
            m.put(name, obj);
        }
        return m;
    }

    @Override
    public Map<String, Object> getValueFromFile(String filename, String encoding, boolean hasVariable, Cache<Object,Object> cache) {
        String text = delegate.getValueFromFile(filename, encoding, hasVariable, cache);
        return getValueFromText(text);
    }
}
