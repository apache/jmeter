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
 */
package org.apache.jmeter.testbeans.gui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.util.JMeterUtils;

/**
 * This class implements a property editor for possibly null String properties
 * that supports custom editing (i.e.: provides a GUI component) based on a
 * combo box.
 * <p>
 * The provided GUI is a combo box with:
 * <ul>
 * <li>An option for "undefined" (corresponding to the null value), unless the
 * <b>noUndefined</b> property is set.
 * <li>An option for each value in the <b>tags</b> property.
 * <li>The possibility to write your own value, unless the <b>noEdit</b>
 * property is set.
 * </ul>
 *
 */
class ComboStringEditor extends PropertyEditorSupport implements ItemListener, ClearGui {

    /**
     * The list of options to be offered by this editor.
     */
    private final String[] tags;

    /**
     * The edited property's default value.
     */
    private String initialEditValue;

    // Cannot use <String> here because combo can contain EDIT and UNDEFINED
    private final JComboBox<Object> combo;

    private final DefaultComboBoxModel<Object> model;

    /*
     * Map of translations for tags; only created if there is at least
     * one tag and a ResourceBundle has been provided.
     */
    private final Map<String, String> validTranslations;

    private boolean startingEdit = false;

    /*
     * True iff we're currently processing an event triggered by the user
     * selecting the "Edit" option. Used to prevent reverting the combo to
     * non-editable during processing of secondary events.
     */

    // Needs to be visible to test cases
    final Object UNDEFINED = new UniqueObject("property_undefined"); //$NON-NLS-1$

    private final Object EDIT = new UniqueObject("property_edit"); //$NON-NLS-1$

    // The minimum index of the tags in the combo box
    private final int minTagIndex;

    // The maximum index of the tags in the combo box
    private final int maxTagIndex;

    @Deprecated // only for use from test code
    ComboStringEditor() {
        this(null, false, false);
    }

    ComboStringEditor(PropertyDescriptor descriptor) {
        this((String[])descriptor.getValue(GenericTestBeanCustomizer.TAGS),
              GenericTestBeanCustomizer.notExpression(descriptor),
              GenericTestBeanCustomizer.notNull(descriptor),
              (ResourceBundle) descriptor.getValue(GenericTestBeanCustomizer.RESOURCE_BUNDLE));
    }

    ComboStringEditor(String []tags, boolean noEdit, boolean noUndefined) {
        this(tags, noEdit, noUndefined, null);
    }

    ComboStringEditor(String []pTags, boolean noEdit, boolean noUndefined, ResourceBundle rb) {

        tags = pTags == null ? ArrayUtils.EMPTY_STRING_ARRAY : pTags.clone();

        model = new DefaultComboBoxModel<>();

        if (rb != null && tags.length > 0) {
            validTranslations = new HashMap<>();
            for (String tag : this.tags) {
                validTranslations.put(tag, rb.getString(tag));
            }
        } else {
            validTranslations=null;
        }

        if (!noUndefined) {
            model.addElement(UNDEFINED);
        }
        if (tags.length == 0) {
            this.minTagIndex = Integer.MAX_VALUE;
            this.maxTagIndex = Integer.MIN_VALUE;
        } else {
            this.minTagIndex=model.getSize(); // track where tags start ...
            for (String tag : this.tags) {
                model.addElement(translate(tag));
            }
            this.maxTagIndex=model.getSize(); // ... and where they end
        }
        if (!noEdit) {
            model.addElement(EDIT);
        }

        combo = new JComboBox<>(model);
        combo.addItemListener(this);
        combo.setEditable(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getCustomEditor() {
        return combo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue() {
        return getAsText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsText() {
        final Object value = combo.getSelectedItem();
        if (UNDEFINED.equals(value)) {
            return null;
        }
        final int item = combo.getSelectedIndex();
        // Check if the entry index corresponds to a tag, if so return the tag
        // This also works if the tags were not translated
        if (item >= minTagIndex && item <= maxTagIndex) {
            return tags[item-minTagIndex];
        }
        // Not a tag entry, return the original value
        return (String) value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(Object value) {
        setAsText((String) value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAsText(String value) {
        combo.setEditable(true);

        if (value == null) {
            combo.setSelectedItem(UNDEFINED);
        } else {
            combo.setSelectedItem(translate(value));
        }

        if (!startingEdit && combo.getSelectedIndex() >= 0) {
            combo.setEditable(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (EDIT.equals(e.getItem())) {
                startingEdit = true;
                startEditing();
                startingEdit = false;
            } else {
                if (!startingEdit && combo.getSelectedIndex() >= 0) {
                    combo.setEditable(false);
                }

                firePropertyChange();
            }
        }
    }

    private void startEditing() {
        JTextComponent textField = (JTextComponent) combo.getEditor().getEditorComponent();

        combo.setEditable(true);

        textField.requestFocusInWindow();
        String text = translate(initialEditValue);
        if (text == null) {
            text = ""; // will revert to last valid value if invalid
        }

        combo.setSelectedItem(text);

        int i = text.indexOf("${}");
        if (i != -1) {
            textField.setCaretPosition(i + 2);
        } else {
            textField.selectAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getTags() {
        return tags.clone();
    }

    /**
     * @param object the initial edit value
     */
    public void setInitialEditValue(String object) {
        initialEditValue = object;
    }

    /**
     * This is a funny hack: if you use a plain String, entering the text of the
     * string in the editor will make the combo revert to that option -- which
     * actually amounts to making that string 'reserved'. I preferred to avoid
     * this by using a different type having a controlled .toString().
     */
    private static class UniqueObject {
        private final String propKey;
        private final String propValue;

        UniqueObject(String propKey) {
            this.propKey = propKey;
            this.propValue = JMeterUtils.getResString(propKey);
        }

        @Override
        public String toString() {
            return propValue;
        }
        
        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other instanceof UniqueObject) {
                return propKey.equals(((UniqueObject) other).propKey);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return propKey.hashCode();
        }
    }

    @Override
    public void clearGui() {
        setAsText(initialEditValue);
    }
    
    // Replace a string with its translation, if one exists
    private String translate(String input) {
        if (validTranslations != null) {
            final String entry = validTranslations.get(input);
            return entry != null ? entry : input;
        }
        return input;
    }
}
