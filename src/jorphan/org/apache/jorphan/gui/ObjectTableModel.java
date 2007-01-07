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

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.Functor;
import org.apache.log.Logger;

/**
 * @version $Revision$
 */
public class ObjectTableModel extends DefaultTableModel {
	private static Logger log = LoggingManager.getLoggerForClass();

	private transient ArrayList objects = new ArrayList();

	private transient List headers = new ArrayList();

	private transient ArrayList classes = new ArrayList();

	private transient ArrayList readFunctors = new ArrayList();

	private transient ArrayList writeFunctors = new ArrayList();

	public ObjectTableModel(String[] headers, Functor[] readFunctors, Functor[] writeFunctors, Class[] editorClasses) {
		this.headers.addAll(Arrays.asList(headers));
		this.classes.addAll(Arrays.asList(editorClasses));
		this.readFunctors = new ArrayList(Arrays.asList(readFunctors));
		this.writeFunctors = new ArrayList(Arrays.asList(writeFunctors));

        int numHeaders = headers.length;

        int numClasses = classes.size();
        if (numClasses != numHeaders){
            log.warn("Header count="+numHeaders+" but classes count="+numClasses);
        }
        
        // Functor count = 0 is handled specially 
        int numWrite = writeFunctors.length;
        if (numWrite > 0 && numWrite != numHeaders){
            log.warn("Header count="+numHeaders+" but writeFunctor count="+numWrite);
        }
        
        int numRead = readFunctors.length;
        if (numRead > 0 && numRead != numHeaders){
            log.warn("Header count="+numHeaders+" but readFunctor count="+numRead);
        }
	}

	public Iterator iterator() {
		return objects.iterator();
	}

	public void clearData() {
		int size = getRowCount();
		objects.clear();
		super.fireTableRowsDeleted(0, size);
	}

	public void addRow(Object value) {
		log.debug("Adding row value: " + value);
		objects.add(value);
		super.fireTableRowsInserted(objects.size() - 1, objects.size());
	}

	public void insertRow(Object value, int index) {
		objects.add(index, value);
		super.fireTableRowsInserted(index, index + 1);
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return headers.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int col) {
		return (String) headers.get(col);
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		if (objects == null) {
			return 0;
		}
		return objects.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		log.debug("Getting row value");
		Object value = objects.get(row);
		if(headers.size() == 1 && col >= readFunctors.size())
			return value;
		Functor getMethod = (Functor) readFunctors.get(col);
		if (getMethod != null && value != null) {
			return getMethod.invoke(value);
		}
		return null;
	}

	/**
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int arg0, int arg1) {
		return true;
	}

	/**
	 * @see javax.swing.table.DefaultTableModel#moveRow(int, int, int)
	 */
	public void moveRow(int start, int end, int to) {
		List subList = objects.subList(start, end);
		for (int x = end - 1; x >= start; x--) {
			objects.remove(x);
		}
		objects.addAll(to, subList);
		super.fireTableChanged(new TableModelEvent(this));
	}

	/**
	 * @see javax.swing.table.DefaultTableModel#removeRow(int)
	 */
	public void removeRow(int row) {
		objects.remove(row);
		super.fireTableRowsDeleted(row, row);
	}

	/**
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object cellValue, int row, int col) {
		if (row < objects.size()) {
			Object value = objects.get(row);
			if (col < writeFunctors.size()) {
				Functor setMethod = (Functor) writeFunctors.get(col);
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

	/**
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int arg0) {
		return (Class) classes.get(arg0);
	}

}
