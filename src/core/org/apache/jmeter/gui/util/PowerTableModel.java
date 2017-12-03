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

package org.apache.jmeter.gui.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import org.apache.jorphan.collections.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerTableModel extends DefaultTableModel {
    private static final Logger log = LoggerFactory.getLogger(PowerTableModel.class);

    private static final long serialVersionUID = 234L;

    private Data model = new Data();

    private Class<?>[] columnClasses;

    public PowerTableModel(String[] headers, Class<?>[] classes) {
        if (headers.length != classes.length){
            throw new IllegalArgumentException("Header and column array sizes differ");
        }
        model.setHeaders(headers);
        columnClasses = classes;
    }

    public PowerTableModel() {
    }

    public void setRowValues(int row, Object[] values) {
        if (values.length != model.getHeaderCount()){
            throw new IllegalArgumentException("Incorrect number of data items");
        }
        model.setCurrentPos(row);
        for (int i = 0; i < values.length; i++) {
            model.addColumnValue(model.getHeaders()[i], values[i]);
        }
    }

    public Data getData() {
        return model;
    }

    public void addNewColumn(String colName, Class<?> colClass) {
        model.addHeader(colName);
        Class<?>[] newClasses = new Class[columnClasses.length + 1];
        System.arraycopy(columnClasses, 0, newClasses, 0, columnClasses.length);
        newClasses[newClasses.length - 1] = colClass;
        columnClasses = newClasses;
        Object defaultValue = createDefaultValue(columnClasses.length - 1);
        model.setColumnData(colName, defaultValue);
        this.fireTableStructureChanged();
    }

    @Override
    public void removeRow(int row) {
        log.debug("remove row: {}", row);
        if (model.size() > row) {
            log.debug("Calling remove row on Data");
            model.removeRow(row);
        }
    }

    public void removeColumn(int col) {
        model.removeColumn(col);
        this.fireTableStructureChanged();
    }

    public void setColumnData(int col, List<?> data) {
        model.setColumnData(col, data);
    }

    public List<?> getColumnData(String colName) {
        return model.getColumnAsObjectArray(colName);
    }

    public void clearData() {
        String[] headers = model.getHeaders();
        model = new Data();
        model.setHeaders(headers);
        this.fireTableDataChanged();
    }

    @Override
    public void addRow(Object[] data) {
        if (data.length != model.getHeaderCount()){
            throw new IllegalArgumentException("Incorrect number of data items");
        }
        model.setCurrentPos(model.size());
        for (int i = 0; i < data.length; i++) {
            model.addColumnValue(model.getHeaders()[i], data[i]);
        }
    }

    @Override
    public void moveRow(int start, int end, int to) {
        ArrayList<Object[]> rows = new ArrayList<>();
        for(int i=0; i < getRowCount(); i++){
            rows.add(getRowData(i));
        }

        List<Object[]> subList = new ArrayList<>(rows.subList(start, end));
        for (int x = end - 1; x >= start; x--) {
            rows.remove(x);
        }

        rows.addAll(to, subList);
        for(int i = 0; i < rows.size(); i++){
            setRowValues(i, rows.get(i));
        }

        super.fireTableChanged(new TableModelEvent(this));
    }

    public void addNewRow() {
        addRow(createDefaultRow());
    }

    private Object[] createDefaultRow() {
        Object[] rowData = new Object[getColumnCount()];
        for (int i = 0; i < rowData.length; i++) {
            rowData[i] = createDefaultValue(i);
        }
        return rowData;
    }

    public Object[] getRowData(int row) {
        Object[] rowData = new Object[getColumnCount()];
        Arrays.setAll(rowData, i -> model.getColumnValue(i, row));
        return rowData;
    }

    private Object createDefaultValue(int i) { // CHECKSTYLE IGNORE ReturnCount
        Class<?> colClass = getColumnClass(i);
        try {
            return colClass.newInstance();
        } catch (Exception e) {
            try {
                Constructor<?> constr = colClass.getConstructor(new Class[] { String.class });
                return constr.newInstance(new Object[] { "" });
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
            }
            try {
                Constructor<?> constr = colClass.getConstructor(new Class[] { Integer.TYPE });
                return constr.newInstance(new Object[] { Integer.valueOf(0) });
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
            }
            try {
                Constructor<?> constr = colClass.getConstructor(new Class[] { Long.TYPE });
                return constr.newInstance(new Object[] { Long.valueOf(0L) });
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
            }
            try {
                Constructor<?> constr = colClass.getConstructor(new Class[] { Boolean.TYPE });
                return constr.newInstance(new Object[] { Boolean.FALSE });
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
            }
            try {
                Constructor<?> constr = colClass.getConstructor(new Class[] { Float.TYPE });
                return constr.newInstance(new Object[] { Float.valueOf(0F) });
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
            }
            try {
                Constructor<?> constr = colClass.getConstructor(new Class[] { Double.TYPE });
                return constr.newInstance(new Object[] { Double.valueOf(0D) });
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
            }
            try {
                Constructor<?> constr = colClass.getConstructor(new Class[] { Character.TYPE });
                return constr.newInstance(new Object[] { Character.valueOf(' ') });
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
            }
            try {
                Constructor<?> constr = colClass.getConstructor(new Class[] { Byte.TYPE });
                return constr.newInstance(new Object[] { Byte.valueOf(Byte.MIN_VALUE) });
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
            }
            try {
                Constructor<?> constr = colClass.getConstructor(new Class[] { Short.TYPE });
                return constr.newInstance(new Object[] { Short.valueOf(Short.MIN_VALUE) });
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
            }
        }
        return "";
    }

    /**
     * Required by table model interface.
     *
     * @return the RowCount value
     */
    @Override
    public int getRowCount() {
        if (model == null) {
            return 0;
        }
        return model.size();
    }

    /**
     * Required by table model interface.
     *
     * @return the ColumnCount value
     */
    @Override
    public int getColumnCount() {
        return model.getHeaders().length;
    }

    /**
     * Required by table model interface.
     *
     * @return the ColumnName value
     */
    @Override
    public String getColumnName(int column) {
        return model.getHeaders()[column];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        // all table cells are editable
        return true;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columnClasses[column];
    }

    /**
     * Required by table model interface. return the ValueAt value
     */
    @Override
    public Object getValueAt(int row, int column) {
        return model.getColumnValue(column, row);
    }

    /**
     * Sets the ValueAt attribute of the Arguments object.
     *
     * @param value
     *            the new ValueAt value
     */
    @Override
    public void setValueAt(Object value, int row, int column) {
        if (row < model.size()) {
            model.setCurrentPos(row);
            model.addColumnValue(model.getHeaders()[column], value);
        }
    }
}
