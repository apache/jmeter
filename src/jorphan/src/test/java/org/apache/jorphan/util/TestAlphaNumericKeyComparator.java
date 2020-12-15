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
    void testComparatorWithEqualKeys(String candidate) {
        Comparator<Map.Entry<Object, Object>> comparator = AlphaNumericKeyComparator.INSTANCE;
        assertEquals(0, comparator.compare(entry(candidate), entry(candidate)));
    }

    @ParameterizedTest
    @CsvSource({
        "a,                    1",
        "a10,                  a1",
        "a2,                   a1",
        "a20,                  a10",
        "a10,                  a2",
        "z,                    10000",
        "def,                  abc",
        "123_z,                123_a",
        "9-9-z,                9-9-a",
        "abc,                  ''",
        "'abc.,${something}1', 'abc.,${something}'",
        "number1,              number",
        "789b,                 789"
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
