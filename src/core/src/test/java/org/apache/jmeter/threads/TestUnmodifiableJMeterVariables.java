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

package org.apache.jmeter.threads;

import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

public class TestUnmodifiableJMeterVariables {

    private static final String MY_OBJECT_KEY = "my.objectKey";
    private static final String MY_KEY = "my.key";
    private JMeterVariables vars;
    private UnmodifiableJMeterVariables unmodifiables;

    @Before
    public void setUp() {
        vars = new JMeterVariables();
        vars.put(MY_KEY, "something to test for");
        vars.putObject(MY_OBJECT_KEY, new Object());
        unmodifiables = new UnmodifiableJMeterVariables(vars);
    }

    @Test
    public void testGetThreadName() {
        assertThat(unmodifiables.getThreadName(), CoreMatchers.is(vars.getThreadName()));
    }

    @Test
    public void testGetIteration() {
        assertThat(unmodifiables.getIteration(), CoreMatchers.is(vars.getIteration()));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIncIteration() {
        unmodifiables.incIteration();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        unmodifiables.remove("some.key");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPut() {
        unmodifiables.put("some.key", "anything");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPutObject() {
        unmodifiables.putObject("some.key", new Object());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPutAllMapOfStringQ() {
        unmodifiables.putAll(Collections.emptyMap());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPutAllJMeterVariables() {
        unmodifiables.putAll(vars);
    }

    @Test
    public void testGet() {
        assertThat(unmodifiables.get(MY_KEY), CoreMatchers.is(vars.get(MY_KEY)));
    }

    @Test
    public void testGetObject() {
        assertThat(unmodifiables.getObject(MY_OBJECT_KEY), CoreMatchers.is(vars.getObject(MY_OBJECT_KEY)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetIteratorIsUnmodifable() {
        Iterator<Entry<String, Object>> iterator = unmodifiables.getIterator();
        assertThat(iterator.hasNext(), CoreMatchers.is(true));
        iterator.next();
        iterator.remove();
    }

    @Test
    public void testGetIterator() {
        assertThat(iteratorToMap(unmodifiables.getIterator()), CoreMatchers.is(iteratorToMap(vars.getIterator())));
    }

    private <K, V> Map<K, V> iteratorToMap(Iterator<Entry<K, V>> it) {
        Map<K, V> result = new HashMap<>();
        while (it.hasNext()) {
            Entry<K, V> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Test
    public void testEntrySet() {
        assertThat(unmodifiables.entrySet(), CoreMatchers.is(vars.entrySet()));
    }

    @Test
    public void testEqualsObjectSymmetry() {
        UnmodifiableJMeterVariables otherUnmodifiables = new UnmodifiableJMeterVariables(vars);
        assertThat(unmodifiables, CoreMatchers.is(otherUnmodifiables));
        assertThat(otherUnmodifiables, CoreMatchers.is(unmodifiables));
    }

    @Test
    public void testEqualsObjectReflexivity() {
        assertThat(unmodifiables, CoreMatchers.is(unmodifiables));
    }

    @Test
    public void testEqualsObjectWithJMeterVariables() {
        assertThat(unmodifiables.equals(vars), CoreMatchers.is(vars.equals(unmodifiables)));
    }

    @Test
    public void testHashCode() {
        UnmodifiableJMeterVariables otherUnmodifiables = new UnmodifiableJMeterVariables(vars);
        assertThat(unmodifiables.hashCode(), CoreMatchers.is(otherUnmodifiables.hashCode()));
    }

}
