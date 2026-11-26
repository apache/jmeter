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

package org.apache.jmeter.testelement.property;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class MapPropertyTest {

    @Test
    public void testBug62281MapProperty() {
        MapProperty props = new MapProperty();
        props.addProperty("Foo", new BooleanProperty());
    }

    @Test
    public void testGetPropertyTypeOfEmptyMap() {
        MapProperty props = new MapProperty("foo", new HashMap<>());
        assertEquals(NullProperty.class, props.getPropertyType());
    }

    @Test
    public void testGetPropertyTypeOfStringElements() {
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("One", "1");
        MapProperty props = new MapProperty("foo", stringMap);
        assertEquals(StringProperty.class, props.getPropertyType());
    }
}
