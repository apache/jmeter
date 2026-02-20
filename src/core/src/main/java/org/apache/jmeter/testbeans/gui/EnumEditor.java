/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.testbeans.gui;

import java.awt.Component;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import org.apache.jmeter.gui.ClearGui;
import org.apache.jorphan.locale.ResourceKeyed;
import org.apache.jorphan.util.EnumUtils;

/**
 * This class implements a property editor for String properties based on an enum
 * that supports custom editing (i.e.: provides a GUI component) based on a
 * combo box.
 * <p>
 * The provided GUI is a combo box with an option for each value in the enum.
 * <p>
 */
class EnumEditor<T extends Enum<?> & ResourceKeyed> extends PropertyEditorSupport implements ClearGui {
    private final JComboBox<T> combo;

    private final T defaultValue;

    public EnumEditor(final PropertyDescriptor descriptor, final Class<T> enumClass, final ResourceBundle rb) {
        DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
        combo = new JComboBox<>(model);
        combo.setEditable(false);
        combo.setRenderer(
                new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        label.setText(rb.getString(enumClass.cast(value).getResourceKey()));
                        return label;
                    }
                }
        );
        List<T> values = EnumUtils.getEnumValues(enumClass);
        for (T e : values) {
            model.addElement(e);
        }
        Object def = descriptor.getValue(GenericTestBeanCustomizer.DEFAULT);
        if (def instanceof Enum<?> enumValue) {
            defaultValue = enumClass.cast(enumValue);
        } else if (def instanceof Integer index) {
            defaultValue = values.get(index);
        } else {
            defaultValue = values.get(0);
        }
        combo.setSelectedItem(defaultValue);
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        return combo;
    }

    @Override
    public Object getValue() {
        return combo.getSelectedItem();
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Enum<?> anEnum){
            combo.setSelectedItem(anEnum);
        } else if (value instanceof Integer integer) {
            combo.setSelectedIndex(integer);
        } else if (value instanceof String string) {
            setAsText(string);
        }
    }

    @Override
    public void setAsText(String value) {
        ComboBoxModel<T> model = combo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            T element = model.getElementAt(i);
            if (value.equals(element.getResourceKey())) {
                combo.setSelectedItem(element);
                return;
            }
        }
    }

    @Override
    public void clearGui() {
        combo.setSelectedItem(defaultValue);
    }
}
