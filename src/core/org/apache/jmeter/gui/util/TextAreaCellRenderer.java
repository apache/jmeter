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

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

public class TextAreaCellRenderer implements TableCellRenderer {

    private JTextArea rend = new JTextArea("");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
        if(value != null) {
            rend = new JTextArea(value.toString());
        } else {
            rend = new JTextArea();
        }
        // Use two rows, so that we have room for horisontal scrollbar, if the text is one long line. Fix for 40371
        // This is not an optimal solution, but makes it possible to see the line if it is long
        rend.setRows(2);
        rend.revalidate();
        if (!hasFocus && !isSelected) {
            rend.setBackground(JMeterColor.LAVENDER);
        }
        if (table.getRowHeight(row) < getPreferredHeight()) {
            table.setRowHeight(row, getPreferredHeight());
        }
        return rend;
    }

    public int getPreferredHeight() {
        return rend.getPreferredSize().height + 5;
    }
}
