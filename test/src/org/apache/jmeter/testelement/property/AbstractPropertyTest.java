/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.jmeter.testelement.property;

import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.testelement.TestElement;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

public class AbstractPropertyTest {

    private AbstractProperty dummyProperty;

    @SuppressWarnings("serial")
    @Before
    public void setUp() {

        this.dummyProperty = new AbstractProperty() {

            @Override
            public void setObjectValue(Object value) {
                // not needed for our tests
            }

            @Override
            public void recoverRunningVersion(TestElement owner) {
                // not needed for our tests
            }

            @Override
            public String getStringValue() {
                // not needed for our tests
                return null;
            }

            @Override
            public Object getObjectValue() {
                // not needed for our tests
                return null;
            }
        };
    }

    @Test
    public void testNormalizeListWithEmptyList() {
        Collection<JMeterProperty> emptyCollection = Collections.emptyList();
        Collection<JMeterProperty> newCollection = dummyProperty.normalizeList(emptyCollection);
        assertThat(newCollection, CoreMatchers.nullValue());
    }

    @Test
    public void testNormalizeListWithEmptyArrayList() {
        Collection<JMeterProperty> emptyCollection = new ArrayList<JMeterProperty>();
        Collection<JMeterProperty> newCollection = dummyProperty.normalizeList(emptyCollection);
        assertThat(newCollection, CoreMatchers.not(CoreMatchers.sameInstance(emptyCollection)));
        assertThat(newCollection, CoreMatchers.equalTo(emptyCollection));
    }

    @Test
    public void testNormalizeListWithFilledArrayList() {
        List<JMeterProperty> filledCollection = new ArrayList<JMeterProperty>();
        filledCollection.add(new StringProperty("key", "value"));
        Collection<JMeterProperty> newCollection = dummyProperty.normalizeList(filledCollection);
        assertThat(newCollection, CoreMatchers.not(CoreMatchers.sameInstance(filledCollection)));
        assertThat(newCollection, CoreMatchers.equalTo(filledCollection));
    }

    @Test
    public void testNormalizeListWithEmptyMap() {
        Map<String, JMeterProperty> emptyCollection = Collections.emptyMap();
        Map<String, JMeterProperty> newCollection = dummyProperty.normalizeMap(emptyCollection);
        assertThat(newCollection, CoreMatchers.nullValue());
    }

    @Test
    public void testNormalizeMapWithEmptyHashMap() {
        Map<String, JMeterProperty> emptyCollection = new HashMap<>();
        Map<String, JMeterProperty> newCollection = dummyProperty.normalizeMap(emptyCollection);
        assertThat(newCollection, CoreMatchers.not(CoreMatchers.sameInstance(emptyCollection)));
        assertThat(newCollection, CoreMatchers.equalTo(emptyCollection));
    }

    @Test
    public void testNormalizeMapWithFilledHashMap() {
        Map<String, JMeterProperty> filledCollection = new HashMap<>();
        filledCollection.put("someKey", new StringProperty("key", "value"));
        Map<String, JMeterProperty> newCollection = dummyProperty.normalizeMap(filledCollection);
        assertThat(newCollection, CoreMatchers.not(CoreMatchers.sameInstance(filledCollection)));
        assertThat(newCollection, CoreMatchers.equalTo(filledCollection));
    }
}
