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

import javax.swing.table.TableModel;

/**
 *
 * This is a basic interface for TreeTableModel that extends TableModel.
 * It's pretty minimal and isn't as full featured at other implementations.
 */
public interface TreeTableModel extends TableModel {

    /**
     * The method is similar to getValueAt(int,int). Instead of int, the row is
     * an object.
     *
     * @param node
     *            the node which value is to be fetched
     * @param col
     *            the column of the node
     * @return the value at the column
     */
    Object getValueAt(Object node, int col);

    /**
     * the method is similar to isCellEditable(int,int). Instead of int, the row
     * is an object.
     *
     * @param node
     *            the node which value is to be fetched
     * @param col
     *            the column of the node
     * @return <code>true</code> if cell is editable
     */
    boolean isCellEditable(Object node, int col);

    /**
     * the method is similar to isCellEditable(int,int). Instead of int, the row
     * is an object.
     *
     * @param val
     *            the value to be set
     * @param node
     *            the node which value is to be set
     * @param column
     *            the column of the node
     */
    void setValueAt(Object val, Object node, int column);
}
