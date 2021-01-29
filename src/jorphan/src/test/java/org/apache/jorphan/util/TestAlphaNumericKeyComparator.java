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

package org.apache.jorphan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Comparator;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TestAlphaNumericKeyComparator {

    @ParameterizedTest
    @ValueSource(strings = { "abc", "", "var_123", "434", "_" })
    void testComparatorWithSameKeys(String candidate) {
        Comparator<Map.Entry<Object, Object>> comparator = AlphaNumericKeyComparator.INSTANCE;
        assertEquals(0, comparator.compare(entry(candidate), entry(candidate)));
    }

    @ParameterizedTest
    @CsvSource({ "abc-001, abc-1", "007, 7", "0000, 0", "abc|000, abc|0" })
    void testComparatorWithEquivalentKeys(String left, String right) {
        Comparator<Map.Entry<Object, Object>> comparator = AlphaNumericKeyComparator.INSTANCE;
        assertEquals(0, comparator.compare(entry(left), entry(right)));
        assertEquals(0, comparator.compare(entry(right), entry(left)));
    }

    @ParameterizedTest
    @CsvSource({
        "a,                    1",
        "something-0001,       999999999999999999999999999999999999999999999999999999999999999999999999999999",
        "abc[23],              abc[2]",
        "a10,                  a1",
        "a2,                   a1",
        "2,                    01",
        "0010,                 000005",
        "a20,                  a10",
        "a10,                  a2",
        "z,                    10000",
        "def,                  abc",
        "123_z,                123_a",
        "9-9-z,                9-9-a",
        "abc,                  ''",
        "'abc.,${something}1', 'abc.,${something}'",
        "number1,              number",
        "789b,                 789",
        "0xcafebabe,           0x8664",
        "abc_0000,             abc_"
        })
    void testComparatorDifferentKeys(String higher, String lower) {
        Comparator<Map.Entry<Object, Object>> comparator = AlphaNumericKeyComparator.INSTANCE;
        int compareLowerFirst = comparator.compare(entry(lower), entry(higher)) > 0 ? 1 : -1;
        assertEquals(-1, compareLowerFirst);
        int compareHigherFirst = comparator.compare(entry(higher), entry(lower)) > 0 ? 1 : -1;
        assertEquals(1, compareHigherFirst);
    }

    private Map.Entry<Object, Object> entry(final String key) {
        return new Map.Entry<Object, Object>() {

            @Override
            public Object getKey() {
                return key;
            }

            @Override
            public Object getValue() {
                return null;
            }

            @Override
            public Object setValue(Object value) {
                return null;
            }
        };
    }
}
