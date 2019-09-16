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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.gui;

import javax.swing.table.AbstractTableModel;

public class CorrelationTableModel extends AbstractTableModel {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5071306820857374637L;

    public static Object[][] rowData = null;

    String[] columnNames = { "Select field for correlation", "variable name", "value 1", "value 2" };

    public int getColumnCount() {
        return columnNames.length;
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    public int getRowCount() {
        return rowData.length;
    }

    public Object getValueAt(int row, int column) {
        return rowData[row][column];
    }

    public Class getColumnClass(int column) {
        return column == 0 ? Boolean.class : String.class;
    }

    public void setValueAt(Object value, int row, int column) {
        rowData[row][column] = value;
    }

    public boolean isCellEditable(int row, int column) {
        // Only make the first column editable
        return column == 0;
    }

}
