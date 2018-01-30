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

import java.util.Vector;

import javax.swing.JTable;

public class JTreeTable extends JTable {

    private static final long serialVersionUID = 240L;

    /**
     * The default implementation will use DefaultTreeTableModel
     */
    public JTreeTable() {
        super(new DefaultTreeTableModel());
    }

    /**
     * @param numRows number of rows the table holds
     * @param numColumns number of columns the table holds
     */
    public JTreeTable(int numRows, int numColumns) {
        super(numRows, numColumns);
    }

    /**
     * @param dm the data model to use
     */
    public JTreeTable(TreeTableModel dm) {
        super(dm);
    }

    /**
     * @param rowData the data for the table
     * @param columnNames the names for the columns
     */
    public JTreeTable(Object[][] rowData, Object[] columnNames) {
        super(rowData, columnNames);
    }

    /**
     * @param rowData the data for the table. Has to be a {@link Vector} of {@link Vector}s.
     * @param columnNames the names for the columns
     */
    @SuppressWarnings("unchecked")
    public JTreeTable(Vector<?> rowData, Vector<?> columnNames) {
        super((Vector<? extends Vector<?>>) rowData, columnNames);
    }

}
