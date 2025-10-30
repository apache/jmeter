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

package org.apache.jmeter.threads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class TestUnmodifiableJMeterVariables {

    private static final String MY_OBJECT_KEY = "my.objectKey";
    private static final String MY_KEY = "my.key";
    private JMeterVariables vars;
    private UnmodifiableJMeterVariables unmodifiables;

    @BeforeEach
    public void setUp() {
        vars = new JMeterVariables();
        vars.put(MY_KEY, "something to test for");
        vars.putObject(MY_OBJECT_KEY, new Object());
        unmodifiables = new UnmodifiableJMeterVariables(vars);
    }

    @Test
    public void testGetThreadName() {
        assertEquals(vars.getThreadName(), unmodifiables.getThreadName());
    }

    @Test
    public void testGetIteration() {
        assertEquals(vars.getIteration(), unmodifiables.getIteration());
    }

    @Test
    public void testIncIteration() {
        assertThrowsUnsupportedOperation(unmodifiables::incIteration);
    }

    @Test
    public void testRemove() {
        assertThrowsUnsupportedOperation(
                () -> unmodifiables.remove("some.key"));
    }

    @Test
    public void testPut() {
        assertThrowsUnsupportedOperation(
                () -> unmodifiables.put("some.key", "anything"));
    }

    @Test
    public void testPutObject() {
        assertThrowsUnsupportedOperation(
                () -> unmodifiables.putObject("some.key", new Object()));
    }

    @Test
    public void testPutAllMapOfStringQ() {
        assertThrowsUnsupportedOperation(
                () -> unmodifiables.putAll(Collections.emptyMap()));
    }

    @Test
    public void testPutAllJMeterVariables() {
        assertThrowsUnsupportedOperation(
                () -> unmodifiables.putAll(vars));
    }

    @Test
    public void testGet() {
        assertEquals(vars.get(MY_KEY), unmodifiables.get(MY_KEY));
    }

    @Test
    public void testGetObject() {
        assertEquals(vars.getObject(MY_OBJECT_KEY), unmodifiables.getObject(MY_OBJECT_KEY));
    }

    @Test
    public void testGetIteratorIsUnmodifable() {
        Iterator<Map.Entry<String, Object>> iterator = unmodifiables.getIterator();
        assertTrue(iterator.hasNext());
        iterator.next();
        assertThrowsUnsupportedOperation(iterator::remove);
    }

    private void assertThrowsUnsupportedOperation(Executable executable) {
        assertThrows(
                UnsupportedOperationException.class,
                executable
        );
    }

    @Test
    public void testGetIterator() {
        assertEquals(iteratorToMap(vars.getIterator()), iteratorToMap(unmodifiables.getIterator()));
    }

    private <K, V> Map<K, V> iteratorToMap(Iterator<Map.Entry<K, V>> it) {
        Map<K, V> result = new HashMap<>();
        while (it.hasNext()) {
            Map.Entry<K, V> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Test
    public void testEntrySet() {
        assertEquals(vars.entrySet(), unmodifiables.entrySet());
    }

    @Test
    public void testEqualsObjectSymmetry() {
        UnmodifiableJMeterVariables otherUnmodifiables = new UnmodifiableJMeterVariables(vars);
        assertEquals(otherUnmodifiables, unmodifiables);
        assertEquals(unmodifiables, otherUnmodifiables);
    }

    @Test
    public void testEqualsObjectReflexivity() {
        assertEquals(unmodifiables, unmodifiables);
    }

    @Test
    public void testEqualsObjectWithJMeterVariables() {
        assertEquals(vars.equals(unmodifiables), unmodifiables.equals(vars));
    }

    @Test
    public void testHashCode() {
        UnmodifiableJMeterVariables otherUnmodifiables = new UnmodifiableJMeterVariables(vars);
        assertEquals(otherUnmodifiables.hashCode(), unmodifiables.hashCode());
    }
}
