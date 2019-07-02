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

package org.apache.jorphan.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.apache.jorphan.reflect.Functor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ObjectTableModel is a TableModel whose rows are objects;
 * columns are defined as Functors on the object.
 */
public class ObjectTableModel extends DefaultTableModel {
    private static final Logger log = LoggerFactory.getLogger(ObjectTableModel.class);

    private static final long serialVersionUID = 240L;

    private transient ArrayList<Object> objects = new ArrayList<>();

    private transient List<String> headers = new ArrayList<>();

    private transient ArrayList<Class<?>> classes = new ArrayList<>();

    private transient ArrayList<Functor> readFunctors = new ArrayList<>();

    private transient ArrayList<Functor> writeFunctors = new ArrayList<>();

    private transient Class<?> objectClass = null; // if provided

    private transient boolean cellEditable = true;

    /**
     * The ObjectTableModel is a TableModel whose rows are objects;
     * columns are defined as Functors on the object.
     *
     * @param headers - Column names
     * @param _objClass - Object class that will be used
     * @param readFunctors - used to get the values
     * @param writeFunctors - used to set the values
     * @param editorClasses - class for each column
     */
    public ObjectTableModel(String[] headers, Class<?> _objClass, Functor[] readFunctors, Functor[] writeFunctors, Class<?>[] editorClasses) {
        this(headers, readFunctors, writeFunctors, editorClasses);
        this.objectClass=_objClass;
    }

    /**
     * The ObjectTableModel is a TableModel whose rows are objects;
     * columns are defined as Functors on the object.
     *
     * @param headers - Column names
     * @param _objClass - Object class that will be used
     * @param readFunctors - used to get the values
     * @param writeFunctors - used to set the values
     * @param editorClasses - class for each column
     * @param cellEditable - if cell must editable (false to allow double click on cell)
     */
    public ObjectTableModel(String[] headers, Class<?> _objClass, Functor[] readFunctors,
            Functor[] writeFunctors, Class<?>[] editorClasses, boolean cellEditable) {
        this(headers, readFunctors, writeFunctors, editorClasses);
        this.objectClass=_objClass;
        this.cellEditable = cellEditable;
    }

    /**
     * The ObjectTableModel is a TableModel whose rows are objects;
     * columns are defined as Functors on the object.
     *
     * @param headers - Column names
     * @param readFunctors - used to get the values
     * @param writeFunctors - used to set the values
     * @param editorClasses - class for each column
     */
    public ObjectTableModel(String[] headers, Functor[] readFunctors, Functor[] writeFunctors, Class<?>[] editorClasses) {
        this.headers.addAll(Arrays.asList(headers));
        this.classes.addAll(Arrays.asList(editorClasses));
        this.readFunctors = new ArrayList<>(Arrays.asList(readFunctors));
        this.writeFunctors = new ArrayList<>(Arrays.asList(writeFunctors));

        int numHeaders = headers.length;

        int numClasses = classes.size();
        if (numClasses != numHeaders){
            log.warn("Header count={} but classes count={}", numHeaders, numClasses);
        }

        // Functor count = 0 is handled specially
        int numWrite = writeFunctors.length;
        if (numWrite > 0 && numWrite != numHeaders){
            log.warn("Header count={} but writeFunctor count={}", numHeaders, numWrite);
        }

        int numRead = readFunctors.length;
        if (numRead > 0 && numRead != numHeaders){
            log.warn("Header count={} but readFunctor count={}", numHeaders, numRead);
        }
    }

    private Object readResolve() {
        objects = new ArrayList<>();
        headers = new ArrayList<>();
        classes = new ArrayList<>();
        readFunctors = new ArrayList<>();
        writeFunctors = new ArrayList<>();
        return this;
    }

    public Iterator<?> iterator() {
        return objects.iterator();
    }

    public void clearData() {
        objects.clear();
        super.fireTableDataChanged();
    }

    public void addRow(Object value) {
        log.debug("Adding row value: {}", value);
        if (objectClass != null) {
            final Class<?> valueClass = value.getClass();
            if (!objectClass.isAssignableFrom(valueClass)){
                throw new IllegalArgumentException("Trying to add class: "+valueClass.getName()
                        +"; expecting class: "+objectClass.getName());
            }
        }
        objects.add(value);
        super.fireTableRowsInserted(objects.size() - 1, objects.size() - 1);
    }

    public void insertRow(Object value, int index) {
        objects.add(index, value);
        super.fireTableRowsInserted(index, index);
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return headers.size();
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(int col) {
        return headers.get(col);
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        if (objects == null) {
            return 0;
        }
        return objects.size();
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAt(int row, int col) {
        log.debug("Getting row value");
        Object value = objects.get(row);
        if(headers.size() == 1 && col >= readFunctors.size()) {
            return value;
        }
        Functor getMethod = readFunctors.get(col);
        if (getMethod != null && value != null) {
            return getMethod.invoke(value);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCellEditable(int arg0, int arg1) {
        return cellEditable;
    }

    /** {@inheritDoc} */
    @Override
    public void moveRow(int start, int end, int to) {
        List<Object> subList = objects.subList(start, end);
        List<Object> backup  = new ArrayList<>(subList);
        subList.clear();
        objects.addAll(to, backup);
        super.fireTableDataChanged();
    }

    /** {@inheritDoc} */
    @Override
    public void removeRow(int row) {
        objects.remove(row);
        super.fireTableRowsDeleted(row, row);
    }

    /** {@inheritDoc} */
    @Override
    public void setValueAt(Object cellValue, int row, int col) {
        if (row < objects.size()) {
            Object value = objects.get(row);
            if (col < writeFunctors.size()) {
                Functor setMethod = writeFunctors.get(col);
                if (setMethod != null) {
                    setMethod.invoke(value, new Object[] { cellValue });
                    super.fireTableDataChanged();
                }
            }
            else if(headers.size() == 1)
            {
                objects.set(row,cellValue);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(int arg0) {
        return classes.get(arg0);
    }

    /**
     * Check all registered functors.
     * <p>
     * <b>** only for use in unit test code **</b>
     * </p>
     *
     * @param _value - an instance of the table model row data item
     * (if null, use the class passed to the constructor).
     *
     * @param caller - class of caller.
     *
     * @return false if at least one Functor cannot be found.
     */
    @SuppressWarnings("deprecation")
    public boolean checkFunctors(Object _value, Class<?> caller){
        Object value;
        if (_value == null && objectClass != null) {
            try {
                value = objectClass.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                log.error("Cannot create instance of class {}", objectClass.getName(),e);
                return false;
            }
        } else {
            value = _value;
        }
        boolean status = true;
        for(int i=0;i<getColumnCount();i++){
            Functor setMethod = writeFunctors.get(i);
            if (setMethod != null
                 && !setMethod.checkMethod(value,getColumnClass(i))) {
                    status=false;
                    log.warn("{} is attempting to use nonexistent {}", caller.getName(), setMethod);
            }

            Functor getMethod = readFunctors.get(i);
            if (getMethod != null
                 && !getMethod.checkMethod(value)) {
                    status=false;
                    log.warn("{} is attempting to use nonexistent {}", caller.getName(), getMethod);
            }

        }
        return status;
    }

    /**
     * @return Object (List of Object)
     */
    public Object getObjectList() { // used by TableEditor
        return objects;
    }

    /**
     * @return List of Object
     */
    public List<Object> getObjectListAsList() {
        return objects;
    }

    public void setRows(Iterable<?> rows) { // used by TableEditor
        clearData();
        for(Object val : rows)
        {
            addRow(val);
        }
    }
}
