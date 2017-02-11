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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.swing.RowSorter;
import javax.swing.SortOrder;

import org.apache.commons.lang3.ObjectUtils;

/**
 * Implementation of a {@link RowSorter} for {@link ObjectTableModel}
 * @since 3.2
 *
 */
public class ObjectTableSorter extends RowSorter<ObjectTableModel> {

    /**
     * View row with model mapping. All data relates to model.
     */
    public class Row {
        private int index;

        protected Row(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public Object getValue() {
            return getModel().getObjectListAsList().get(getIndex());
        }

        public Object getValueAt(int column) {
            return getModel().getValueAt(getIndex(), column);
        }
    }

    protected class PreserveLastRowComparator implements Comparator<Row> {
        @Override
        public int compare(Row o1, Row o2) {
            int lastIndex = model.getRowCount() - 1;
            if (o1.getIndex() >= lastIndex || o2.getIndex() >= lastIndex) {
                return o1.getIndex() - o2.getIndex();
            }
            return 0;
        }
    }

    private ObjectTableModel model;
    private SortKey sortkey;

    private Comparator<Row> comparator  = null;
    private ArrayList<Row>  viewToModel = new ArrayList<>();
    private int[]           modelToView = new int[0];

    private Comparator<Row>  primaryComparator = null;
    private Comparator<?>[]  valueComparators;
    private Comparator<Row>  fallbackComparator;

    public ObjectTableSorter(ObjectTableModel model) {
        this.model = model;

        this.valueComparators = new Comparator<?>[this.model.getColumnCount()];
        for (int i = 0; i < valueComparators.length; i++) {
            this.setValueComparator(i, null);
        }

        setFallbackComparator(null);
    }

    /**
     * @return Comparator used prior to sorted columns.
     */
    public Comparator<Row> getPrimaryComparator() {
        return primaryComparator;
    }

    /**
     * @param column to be compared
     * @return Comparator used on column.
     */
    public Comparator<?> getValueComparator(int column) {
        return valueComparators[column];
    }

    /**
     * @return Comparator if all sorted columns matches. Defaults to model index comparison.
     */
    public Comparator<Row> getFallbackComparator() {
        return fallbackComparator;
    }

    /**
     * Comparator used prior to sorted columns.
     * @param primaryComparator {@link Comparator} to be used first
     * @return <code>this</code>
     */
    public ObjectTableSorter setPrimaryComparator(Comparator<Row> primaryComparator) {
      invalidate();
      this.primaryComparator = primaryComparator;
      return this;
    }

    /**
     * Sets {@link #getPrimaryComparator() primary comparator} to one that don't sort last row.
     * @return <code>this</code>
     */
    public ObjectTableSorter fixLastRow() {
        return setPrimaryComparator(new PreserveLastRowComparator());
    }

    /**
     * Assign comparator to given column, if <code>null</code> a getDefaultComparator(int) default one is used instead.
     * @param column Model column index.
     * @param comparator Column value comparator.
     * @return <code>this</code>
     */
    public ObjectTableSorter setValueComparator(int column, Comparator<?> comparator) {
        invalidate();
        valueComparators[column] = ObjectUtils.defaultIfNull(comparator, getDefaultComparator(column));
        return this;
    }

    /**
     * Builds a default comparator based on model column class. {@link Collator#getInstance()} for {@link String},
     * {@link Comparator#naturalOrder() natural order} for {@link Comparable}, no sort support for others.
     * @param column Model column index.
     * @return default {@link Comparator}
     */
    protected Comparator<?> getDefaultComparator(int column) {
        Class<?> columnClass = model.getColumnClass(column);
        if (columnClass == null) {
            return null;
        }
        if (columnClass == String.class) {
            return Comparator.nullsFirst(Collator.getInstance());
        }
        if (Comparable.class.isAssignableFrom(columnClass)) {
            return Comparator.nullsFirst(Comparator.naturalOrder());
        }
        return null;
    }

    /**
     * Sets a fallback comparator (defaults to model index comparison) if none
     * {@link #getPrimaryComparator() primary}, neither
     * {@link #getValueComparator(int) column value comparators} can make
     * differences between two rows.
     *
     * @param comparator
     *            to be used, when all other {@link Comparator}s can't see a
     *            difference
     * @return <code>this</code>
     */
    public ObjectTableSorter setFallbackComparator(Comparator<Row> comparator) {
        invalidate();
        fallbackComparator = ObjectUtils.defaultIfNull(comparator, Comparator.comparingInt(Row::getIndex));
        return this;
    }

    @Override
    public ObjectTableModel getModel() {
        return model;
    }

    @Override
    public void toggleSortOrder(int column) {
        SortKey newSortKey;
        if (isSortable(column)) {
            SortOrder newOrder = sortkey == null || sortkey.getColumn() != column
                    || sortkey.getSortOrder() != SortOrder.ASCENDING ? SortOrder.ASCENDING : SortOrder.DESCENDING;
            newSortKey = new SortKey(column, newOrder);
        } else {
            newSortKey = null;
        }
        setSortKey(newSortKey);
    }

    @Override
    public int convertRowIndexToModel(int index) {
        if (!isSorted()) {
            return index;
        }
        validate();
        return viewToModel.get(index).getIndex();
    }

    @Override
    public int convertRowIndexToView(int index) {
        if (!isSorted()) {
            return index;
        }
        validate();
        return modelToView[index];
    }

    @Override
    public void setSortKeys(List<? extends SortKey> keys) {
        switch (keys.size()) {
            case 0:
                setSortKey(null);
                break;
            case 1:
                setSortKey(keys.get(0));
                break;
            default:
                throw new IllegalArgumentException("Only one column can be sorted");
        }
    }

    public void setSortKey(SortKey sortkey) {
        if (Objects.equals(this.sortkey, sortkey)) {
            return;
        }

        invalidate();
        if (sortkey != null) {
            int column = sortkey.getColumn();
            if (valueComparators[column] == null) {
                throw new IllegalArgumentException(
                        format("Can't sort column %s, it is mapped to type %s and this one have no natural order. So an explicit one must be specified",
                                column, model.getColumnClass(column)));
            }
        }
        this.sortkey    = sortkey;
        this.comparator = null;
    }

    @Override
    public List<? extends SortKey> getSortKeys() {
        return isSorted() ? Collections.singletonList(sortkey) : Collections.emptyList();
    }

    @Override
    public int getViewRowCount() {
        return getModelRowCount();
    }

    @Override
    public int getModelRowCount() {
        return model.getRowCount();
    }

    @Override
    public void modelStructureChanged() {
        setSortKey(null);
    }

    @Override
    public void allRowsChanged() {
        invalidate();
    }

    @Override
    public void rowsInserted(int firstRow, int endRow) {
        rowsChanged(firstRow, endRow, false, true);
    }

    @Override
    public void rowsDeleted(int firstRow, int endRow) {
        rowsChanged(firstRow, endRow, true, false);
    }

    @Override
    public void rowsUpdated(int firstRow, int endRow) {
        rowsChanged(firstRow, endRow, true, true);
    }

    protected void rowsChanged(int firstRow, int endRow, boolean deleted, boolean inserted) {
        invalidate();
    }

    @Override
    public void rowsUpdated(int firstRow, int endRow, int column) {
        if (isSorted(column)) {
            rowsUpdated(firstRow, endRow);
        }
    }

    protected boolean isSortable(int column) {
        return getValueComparator(column) != null;
    }

    protected boolean isSorted(int column) {
        return isSorted() && sortkey.getColumn() == column && sortkey.getSortOrder() != SortOrder.UNSORTED;
    }

    protected boolean isSorted() {
        return sortkey != null;
    }

    protected void invalidate() {
      viewToModel.clear();
      modelToView = new int[0];
    }

    protected void validate() {
      if (isSorted() && viewToModel.isEmpty()) {
          sort();
      }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Comparator<Row> getComparatorFromSortKey(SortKey sortkey) {
        Comparator comp = getValueComparator(sortkey.getColumn());
        if (sortkey.getSortOrder() == SortOrder.DESCENDING) {
            comp = comp.reversed();
        }
        Function<Row,Object> getValueAt = (Row row) -> row.getValueAt(sortkey.getColumn());
        return Comparator.comparing(getValueAt, comp);
    }

    /**
     * Sort table
     */
    protected void sort() {
        if (comparator == null) {
            comparator = Stream.concat(
                    Stream.concat(
                            getPrimaryComparator() != null ? Stream.of(getPrimaryComparator()) : Stream.<Comparator<Row>>empty(),
                            getSortKeys().stream().filter(sk -> sk != null && sk.getSortOrder() != SortOrder.UNSORTED).map(this::getComparatorFromSortKey)
                    ),
                    Stream.of(getFallbackComparator())
            ).reduce(comparator, (result, current) -> result != null ? result.thenComparing(current) : current);
        }

        viewToModel.clear();
        int rowCount = model.getRowCount();
        viewToModel.ensureCapacity(rowCount);
        for (int i = 0; i < rowCount; i++) {
            viewToModel.add(new Row(i));
        }
        Collections.sort(viewToModel, comparator);

        updateModelToView();
    }

    /**
     * fill in modelToView list with index of view
     */
    protected void updateModelToView() {
        modelToView = new int[viewToModel.size()];
        for(int i=0; i<viewToModel.size();i++) {
            modelToView[viewToModel.get(i).getIndex()] = i;
        }
    }
}
