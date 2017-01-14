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
import java.io.Serializable;
import java.text.MessageFormat;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.apache.jmeter.util.JMeterUtils;

/**
 * Renders items in a JTable by converting from resource names.
 */
public class HeaderAsPropertyRenderer implements TableCellRenderer, Serializable {

    private static final long serialVersionUID = 240L;
    private Object[][] columnsMsgParameters;

    private TableCellRenderer delegate;

    public static void install(JTable table) {
        install(table, null);
    }

    public static void install(JTable table, Object[][] columnsMsgParameters) {
        TableCellRenderer defaultRenderer = table.getTableHeader().getDefaultRenderer();
        if (!(defaultRenderer instanceof HeaderAsPropertyRenderer)) {
            HeaderAsPropertyRenderer newRenderer = new HeaderAsPropertyRenderer(defaultRenderer, columnsMsgParameters);
            table.getTableHeader().setDefaultRenderer(newRenderer);
        }
    }

    /**
     * @param columnsMsgParameters Optional parameters of i18n keys
     */
    public HeaderAsPropertyRenderer(TableCellRenderer renderer, Object[][] columnsMsgParameters) {
        this(renderer);
        this.columnsMsgParameters = columnsMsgParameters;
    }

    public HeaderAsPropertyRenderer(TableCellRenderer renderer) {
        this.delegate = renderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        return delegate.getTableCellRendererComponent(table, getText(value, row, column), isSelected, hasFocus, row, column);
    }

    /**
     * Get the text for the value as the translation of the resource name.
     *
     * @param value value for which to get the translation
     * @param column index which column message parameters should be used
     * @param row not used
     * @return the text
     */
    protected String getText(Object value, int row, int column) {
        if (value == null){
            return "";
        }
        if(columnsMsgParameters != null && columnsMsgParameters[column] != null) {
            return MessageFormat.format(JMeterUtils.getResString(value.toString()), columnsMsgParameters[column]);
        } else {
            return JMeterUtils.getResString(value.toString());
        }
    }
}
