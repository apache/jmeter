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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class TextAreaCellRenderer implements TableCellRenderer {

    private JSyntaxTextArea rend = createRenderer(""); //$NON-NLS-1$

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
        if(value != null) {
            rend = createRenderer((String)value);
        } else {
            rend = createRenderer(""); //$NON-NLS-1$
        }
        if (hasFocus || isSelected) {
            rend.setBackground(Color.blue);
            rend.setForeground(Color.white);
        }
        if (table.getRowHeight(row) < getPreferredHeight()) {
            table.setRowHeight(row, getPreferredHeight());
        }
        return JTextScrollPane.getInstance(rend);
    }

    /**
     * @param value initial value
     * @return {@link JSyntaxTextArea}
     */
    private JSyntaxTextArea createRenderer(String value) {
        JSyntaxTextArea textArea = JSyntaxTextArea.getInstance(2, 50);
        textArea.setLanguage("text"); //$NON-NLS-1$
        textArea.setInitialText(value);
        return textArea;
    }

    public int getPreferredHeight() {
        // Allow override for unit testing only
        // TODO Find a better way
        if ("true".equals(System.getProperty("java.awt.headless"))) { // $NON-NLS-1$ $NON-NLS-2$
            return 10;
        } else {
            return rend.getPreferredSize().height + 5;
        }
    }
}
