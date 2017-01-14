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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.jorphan.reflect.Functor;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

public class ObjectTableSorterTest {
    ObjectTableModel  model;
    ObjectTableSorter sorter;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Before
    public void createModelAndSorter() {
        String[] headers         = { "key", "value", "object" };
        Functor[] readFunctors   = { new Functor("getKey"), new Functor("getValue"), new Functor("getValue") };
        Functor[] writeFunctors  = { null, null, null };
        Class<?>[] editorClasses = { String.class, Integer.class, Object.class };
        model                    = new ObjectTableModel(headers, readFunctors, writeFunctors, editorClasses);
        sorter                   = new ObjectTableSorter(model);
        List<Entry<String,Integer>> data = asList(b2(), a3(), d4(), c1());
        data.forEach(model::addRow);
    }

    @Test
    public void noSorting() {
        List<SimpleImmutableEntry<String, Integer>> expected = asList(b2(), a3(), d4(), c1());
        assertRowOrderAndIndexes(expected);
    }

    @Test
    public void sortKeyAscending() {
        sorter.setSortKey(new SortKey(0, SortOrder.ASCENDING));
        List<SimpleImmutableEntry<String, Integer>> expected = asList(a3(), b2(), c1(), d4());
        assertRowOrderAndIndexes(expected);
    }

    @Test
    public void sortKeyDescending() {
        sorter.setSortKey(new SortKey(0, SortOrder.DESCENDING));
        List<SimpleImmutableEntry<String, Integer>> expected = asList(d4(), c1(), b2(), a3());
        assertRowOrderAndIndexes(expected);
    }

    @Test
    public void sortValueAscending() {
        sorter.setSortKey(new SortKey(1, SortOrder.ASCENDING));
        List<SimpleImmutableEntry<String, Integer>> expected = asList(c1(), b2(), a3(), d4());
        assertRowOrderAndIndexes(expected);
    }

    @Test
    public void sortValueDescending() {
        sorter.setSortKey(new SortKey(1, SortOrder.DESCENDING));
        List<SimpleImmutableEntry<String, Integer>> expected = asList(d4(), a3(), b2(), c1());
        assertRowOrderAndIndexes(expected);
    }


    @Test
    public void fixLastRowWithAscendingKey() {
        sorter.fixLastRow().setSortKey(new SortKey(0, SortOrder.ASCENDING));
        List<SimpleImmutableEntry<String, Integer>> expected = asList(a3(), b2(), d4(), c1());
        assertRowOrderAndIndexes(expected);
    }

    @Test
    public void fixLastRowWithDescendingKey() {
        sorter.fixLastRow().setSortKey(new SortKey(0, SortOrder.DESCENDING));
        List<SimpleImmutableEntry<String, Integer>> expected = asList(d4(), b2(), a3(), c1());
        assertRowOrderAndIndexes(expected);
    }

    @Test
    public void fixLastRowWithAscendingValue() {
        sorter.fixLastRow().setSortKey(new SortKey(1, SortOrder.ASCENDING));
        List<SimpleImmutableEntry<String, Integer>> expected = asList(b2(), a3(), d4(), c1());
        assertRowOrderAndIndexes(expected);
    }

    @Test
    public void fixLastRowWithDescendingValue() {
        sorter.fixLastRow().setSortKey(new SortKey(1, SortOrder.DESCENDING));
        List<SimpleImmutableEntry<String, Integer>> expected = asList(d4(), a3(), b2(), c1());
        assertRowOrderAndIndexes(expected);
    }

    @Test
    public void customKeyOrder() {
        HashMap<String, Integer> customKeyOrder = asList("a", "c", "b", "d").stream().reduce(new HashMap<String,Integer>(), (map,key) -> { map.put(key, map.size()); return map; }, (a,b) -> a);
        Comparator<String> customKeyComparator = (a,b) -> customKeyOrder.get(a).compareTo(customKeyOrder.get(b));
        sorter.setValueComparator(0, customKeyComparator).setSortKey(new SortKey(0, SortOrder.ASCENDING));
        List<SimpleImmutableEntry<String, Integer>> expected = asList(a3(), c1(), b2(), d4());
        assertRowOrderAndIndexes(expected);
    }

    @Test
    public void getDefaultComparatorForNullClass() {
        ObjectTableModel model = new ObjectTableModel(new String[] { "null" }, new Functor[] { null }, new Functor[] { null }, new Class<?>[] { null });
        ObjectTableSorter sorter = new ObjectTableSorter(model);

        assertThat(sorter.getValueComparator(0), is(nullValue()));
    }

    @Test
    public void getDefaultComparatorForStringClass() {
        ObjectTableModel model = new ObjectTableModel(new String[] { "string" }, new Functor[] { null }, new Functor[] { null }, new Class<?>[] { String.class });
        ObjectTableSorter sorter = new ObjectTableSorter(model);

        assertThat(sorter.getValueComparator(0), is(CoreMatchers.notNullValue()));
    }

    @Test
    public void getDefaultComparatorForIntegerClass() {
        ObjectTableModel model = new ObjectTableModel(new String[] { "integer" }, new Functor[] { null }, new Functor[] { null }, new Class<?>[] { Integer.class });
        ObjectTableSorter sorter = new ObjectTableSorter(model);

        assertThat(sorter.getValueComparator(0), is(CoreMatchers.notNullValue()));
    }

    @Test
    public void getDefaultComparatorForObjectClass() {
        ObjectTableModel model = new ObjectTableModel(new String[] { "integer" }, new Functor[] { null }, new Functor[] { null }, new Class<?>[] { Object.class });
        ObjectTableSorter sorter = new ObjectTableSorter(model);

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
        assertThat(sorter.getSortKeys(), allOf(  is(not(sameInstance(keys))),  is(equalTo(keys)) ));
    }

    @Test
    public void setSortKeys_many() {
        expectedException.expect(IllegalArgumentException.class);

        sorter.setSortKeys(asList(new SortKey(0, SortOrder.ASCENDING), new SortKey(1, SortOrder.ASCENDING)));
    }

    @Test
    public void setSortKeys_invalidColumn() {
        expectedException.expect(IllegalArgumentException.class);

        sorter.setSortKeys(Collections.singletonList(new SortKey(2, SortOrder.ASCENDING)));
    }


    @SuppressWarnings("unchecked")
    protected List<Entry<String,Integer>> actual() {
        return IntStream
                .range(0, sorter.getViewRowCount())
                .map(sorter::convertRowIndexToModel)
                .mapToObj(modelIndex -> (Entry<String,Integer>) sorter.getModel().getObjectListAsList().get(modelIndex))
                .collect(Collectors.toList())
                ;
    }

    protected SimpleImmutableEntry<String, Integer> d4() {
        return new AbstractMap.SimpleImmutableEntry<>("d",  4);
    }

    protected SimpleImmutableEntry<String, Integer> c1() {
        return new AbstractMap.SimpleImmutableEntry<>("c",  1);
    }

    protected SimpleImmutableEntry<String, Integer> b2() {
        return new AbstractMap.SimpleImmutableEntry<>("b",  2);
    }

    protected SimpleImmutableEntry<String, Integer> a3() {
        return new AbstractMap.SimpleImmutableEntry<>("a",  3);
    }

    protected void assertRowOrderAndIndexes(List<SimpleImmutableEntry<String, Integer>> expected) {
        assertEquals(expected, actual());
        assertRowIndexes();
    }

    protected void assertRowIndexes() {
        IntStream
            .range(0, sorter.getViewRowCount())
            .forEach(viewIndex -> {
                int modelIndex = sorter.convertRowIndexToModel(viewIndex);
                errorCollector.checkThat(format("view(%d) model(%d)", viewIndex, modelIndex),
                        sorter.convertRowIndexToView(modelIndex),
                        CoreMatchers.equalTo(viewIndex));
            });

    }
}
