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

package org.apache.jorphan.gui;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.jorphan.reflect.Functor;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ObjectTableSorterTest {
    private ObjectTableSorter sorter;

    @BeforeEach
    public void createModelAndSorter() {
        String[] headers = {"key", "value", "object"};
        Functor[] readFunctors = {new Functor("getKey"), new Functor("getValue"), new Functor("getValue")};
        Functor[] writeFunctors = {null, null, null};
        Class<?>[] editorClasses = {String.class, Integer.class, Object.class};
        ObjectTableModel model = new ObjectTableModel(headers, readFunctors, writeFunctors, editorClasses);
        sorter = new ObjectTableSorter(model);
        List<Map.Entry<String, Integer>> data = asList(b2(), a3(), d4(), c1());
        data.forEach(model::addRow);
    }

    @Test
    public void noSorting() {
        assertRowOrderAndIndexes(asList(b2(), a3(), d4(), c1()));
    }

    @Test
    public void sortKeyAscending() {
        sorter.setSortKey(new SortKey(0, SortOrder.ASCENDING));
        assertRowOrderAndIndexes(asList(a3(), b2(), c1(), d4()));
    }

    @Test
    public void sortKeyDescending() {
        sorter.setSortKey(new SortKey(0, SortOrder.DESCENDING));
        assertRowOrderAndIndexes(asList(d4(), c1(), b2(), a3()));
    }

    @Test
    public void sortValueAscending() {
        sorter.setSortKey(new SortKey(1, SortOrder.ASCENDING));
        assertRowOrderAndIndexes(asList(c1(), b2(), a3(), d4()));
    }

    @Test
    public void sortValueDescending() {
        sorter.setSortKey(new SortKey(1, SortOrder.DESCENDING));
        assertRowOrderAndIndexes(asList(d4(), a3(), b2(), c1()));
    }


    @Test
    public void fixLastRowWithAscendingKey() {
        sorter.fixLastRow().setSortKey(new SortKey(0, SortOrder.ASCENDING));
        assertRowOrderAndIndexes(asList(a3(), b2(), d4(), c1()));
    }

    @Test
    public void fixLastRowWithDescendingKey() {
        sorter.fixLastRow().setSortKey(new SortKey(0, SortOrder.DESCENDING));
        assertRowOrderAndIndexes(asList(d4(), b2(), a3(), c1()));
    }

    @Test
    public void fixLastRowWithAscendingValue() {
        sorter.fixLastRow().setSortKey(new SortKey(1, SortOrder.ASCENDING));
        assertRowOrderAndIndexes(asList(b2(), a3(), d4(), c1()));
    }

    @Test
    public void fixLastRowWithDescendingValue() {
        sorter.fixLastRow().setSortKey(new SortKey(1, SortOrder.DESCENDING));
        assertRowOrderAndIndexes(asList(d4(), a3(), b2(), c1()));
    }

    @Test
    public void customKeyOrder() {
        HashMap<String, Integer> customKeyOrder = Stream.of("a", "c", "b", "d")
                .reduce(new HashMap<>(), (map, key) -> {
                    map.put(key, map.size());
                    return map;
                }, (a, b) -> a);
        Comparator<String> customKeyComparator = Comparator.comparing(customKeyOrder::get);
        sorter.setValueComparator(0, customKeyComparator)
                .setSortKey(new SortKey(0, SortOrder.ASCENDING));
        assertRowOrderAndIndexes(asList(a3(), c1(), b2(), d4()));
    }

    private ObjectTableModel createTableModel(
            final String name, final Class<?> klass) {
        return new ObjectTableModel(new String[]{name},
                new Functor[]{null}, new Functor[]{null},
                new Class<?>[]{klass});
    }

    @Test
    public void getDefaultComparatorForNullClass() {
        ObjectTableSorter sorter = new ObjectTableSorter(createTableModel("null", null));
        assertThat(sorter.getValueComparator(0), is(nullValue()));
    }

    @Test
    public void getDefaultComparatorForStringClass() {
        ObjectTableSorter sorter = new ObjectTableSorter(createTableModel("string", String.class));
        assertThat(sorter.getValueComparator(0), is(CoreMatchers.notNullValue()));
    }

    @Test
    public void getDefaultComparatorForIntegerClass() {
        ObjectTableSorter sorter = new ObjectTableSorter(createTableModel("integer", Integer.class));
        assertThat(sorter.getValueComparator(0), is(CoreMatchers.notNullValue()));
    }

    @Test
    public void getDefaultComparatorForObjectClass() {
        ObjectTableSorter sorter = new ObjectTableSorter(createTableModel("object", Object.class));
        assertThat(sorter.getValueComparator(0), is(nullValue()));
    }

    @Test
    public void toggleSortOrder_none() {
        assertSame(emptyList(), sorter.getSortKeys());
    }

    @Test
    public void toggleSortOrder_0() {
        sorter.toggleSortOrder(0);
        assertEquals(singletonList(new SortKey(0, SortOrder.ASCENDING)), sorter.getSortKeys());
    }

    @Test
    public void toggleSortOrder_0_1() {
        sorter.toggleSortOrder(0);
        sorter.toggleSortOrder(1);
        assertEquals(singletonList(new SortKey(1, SortOrder.ASCENDING)), sorter.getSortKeys());
    }

    @Test
    public void toggleSortOrder_0_0() {
        sorter.toggleSortOrder(0);
        sorter.toggleSortOrder(0);
        assertEquals(singletonList(new SortKey(0, SortOrder.DESCENDING)), sorter.getSortKeys());
    }

    @Test
    public void toggleSortOrder_0_0_0() {
        sorter.toggleSortOrder(0);
        sorter.toggleSortOrder(0);
        sorter.toggleSortOrder(0);
        assertEquals(singletonList(new SortKey(0, SortOrder.ASCENDING)), sorter.getSortKeys());
    }

    @Test
    public void toggleSortOrder_2() {
        sorter.toggleSortOrder(2);
        assertSame(emptyList(), sorter.getSortKeys());
    }

    @Test
    public void toggleSortOrder_0_2() {
        sorter.toggleSortOrder(0);
        sorter.toggleSortOrder(2);
        assertSame(emptyList(), sorter.getSortKeys());
    }

    @Test
    public void setSortKeys_none() {
        sorter.setSortKeys(new ArrayList<>());
        assertSame(Collections.emptyList(), sorter.getSortKeys());
    }

    @Test
    public void setSortKeys_withSortedThenUnsorted() {
        sorter.setSortKeys(singletonList(new SortKey(0, SortOrder.ASCENDING)));
        sorter.setSortKeys(new ArrayList<>());
        assertSame(Collections.emptyList(), sorter.getSortKeys());
    }

    @Test
    public void setSortKeys_single() {
        List<SortKey> keys = singletonList(new SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(keys);
        assertThat(sorter.getSortKeys(), allOf(is(not(sameInstance(keys))), is(equalTo(keys))));
    }

    @Test
    public void setSortKeys_many() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sorter.setSortKeys(asList(new SortKey(0, SortOrder.ASCENDING), new SortKey(1, SortOrder.ASCENDING))));
    }

    @Test
    public void setSortKeys_invalidColumn() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sorter.setSortKeys(Collections.singletonList(new SortKey(2, SortOrder.ASCENDING))));
    }

    @SuppressWarnings("unchecked")
    protected List<Map.Entry<String, Integer>> actual() {
        return IntStream
                .range(0, sorter.getViewRowCount())
                .map(sorter::convertRowIndexToModel)
                .mapToObj(modelIndex -> (Map.Entry<String, Integer>) sorter.getModel().getObjectListAsList().get(modelIndex))
                .collect(Collectors.toList())
                ;
    }

    protected SimpleImmutableEntry<String, Integer> d4() {
        return new AbstractMap.SimpleImmutableEntry<>("d", 4);
    }

    protected SimpleImmutableEntry<String, Integer> c1() {
        return new AbstractMap.SimpleImmutableEntry<>("c", 1);
    }

    protected SimpleImmutableEntry<String, Integer> b2() {
        return new AbstractMap.SimpleImmutableEntry<>("b", 2);
    }

    protected SimpleImmutableEntry<String, Integer> a3() {
        return new AbstractMap.SimpleImmutableEntry<>("a", 3);
    }

    protected void assertRowOrderAndIndexes(List<SimpleImmutableEntry<String, Integer>> expected) {
        assertEquals(expected, actual());
        assertRowIndexes();
    }

    protected void assertRowIndexes() {
        IntStream.range(0, sorter.getViewRowCount())
                .forEach(viewIndex -> {
                    int modelIndex = sorter.convertRowIndexToModel(viewIndex);
                    String errorMsg = format("view(%d) model(%d)", viewIndex, modelIndex);
                    Assertions.assertEquals(
                            sorter.convertRowIndexToView(modelIndex),
                            viewIndex,
                            errorMsg);
                });

    }
}
