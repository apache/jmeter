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

package org.apache.jmeter.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;

import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.FileEditor;
import org.apache.jmeter.testbeans.gui.TextAreaEditor;

/**
 * Parent class to define common GUI parameters for BSF and JSR223 test elements
 */
public abstract class ScriptingBeanInfoSupport extends BeanInfoSupport {

    static final String FALSE_AS_STRING = Boolean.FALSE.toString();
    static final String TRUE_AS_STRING = Boolean.TRUE.toString();

    public ScriptingBeanInfoSupport(Class<? extends TestBean> beanClass, String[] languageTags) {
        this(beanClass, languageTags, null);
    }

    protected ScriptingBeanInfoSupport(Class<? extends TestBean> beanClass, String[] languageTags, ResourceBundle rb) {
        super(beanClass);
        PropertyDescriptor p;

        p = property("scriptLanguage"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        if (JSR223TestElement.class.isAssignableFrom(beanClass) ) {
            p.setValue(DEFAULT, "groovy"); // $NON-NLS-1$
        } else {
            p.setValue(DEFAULT, ""); // $NON-NLS-1$
        }
        if (rb != null) {
            p.setValue(RESOURCE_BUNDLE, rb);
        }

        p.setValue(TAGS, languageTags);

        createPropertyGroup("scriptingLanguage", // $NON-NLS-1$
                new String[] { "scriptLanguage" }); // $NON-NLS-1$

        p = property("parameters"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$

        createPropertyGroup("parameterGroup", // $NON-NLS-1$
                new String[] { "parameters" }); // $NON-NLS-1$

        p = property("filename"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$
        p.setPropertyEditorClass(FileEditor.class);

        createPropertyGroup("filenameGroup",  // $NON-NLS-1$
                new String[] { "filename" }); // $NON-NLS-1$

        /*
         * If we are creating a JSR223 element, add the cache key property.
         *
         * Note that this cannot be done in the JSR223BeanInfoSupport class
         * because that causes problems with the group; its properties are
         * not always set up before they are needed. This cause various
         * issues with the GUI:
         * - wrong field attributes (should not allow null)
         * - sometimes GUI is completely mangled
         * - field appears at start rather than at end.
         * - the following warning is logged:
         * jmeter.testbeans.gui.GenericTestBeanCustomizer:
         * org.apache.jmeter.util.JSR223TestElement#cacheKey does not appear to have been configured
         *
         * Adding the group here solves these issues, and it's also
         * possible to add the key just before the script panel
         * to which it relates.
         *
         * It's not yet clear why this should be, but it looks as though
         * createPropertyGroup does not work properly if it is called from
         * any subclasses of this class.
         *
         */
        if (JSR223TestElement.class.isAssignableFrom(beanClass) ) {
            p = property("cacheKey"); // $NON-NLS-1$
            p.setValue(NOT_UNDEFINED, Boolean.TRUE);
            p.setValue(NOT_OTHER, Boolean.TRUE);
            p.setValue(DEFAULT, TRUE_AS_STRING); // $NON-NLS-1$
            p.setPropertyEditorClass(JSR223ScriptCacheCheckboxEditor.class);
            p.setValue(TAGS, new String[]{TRUE_AS_STRING,FALSE_AS_STRING});
            createPropertyGroup("cacheKey_group", // $NON-NLS-1$
                new String[] { "cacheKey" }); // $NON-NLS-1$
        }

        p = property("script"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$
        p.setPropertyEditorClass(TextAreaEditor.class);

        createPropertyGroup("scripting", // $NON-NLS-1$
                new String[] { "script" }); // $NON-NLS-1$

    }

    public static class JSR223ScriptCacheCheckboxEditor extends PropertyEditorSupport implements ActionListener, ClearGui {

        private final JCheckBox checkbox;

        /**
         * Value on which we started the editing.
         */
        private String initialValue = null;

        public JSR223ScriptCacheCheckboxEditor() {
            super();
            checkbox = new JCheckBox();
            checkbox.addActionListener(this);
        }

        @Override
        public String getAsText() {
            if(checkbox.isSelected()) {
                return TRUE_AS_STRING;
            } else {
                return FALSE_AS_STRING;
            }
        }

        @Override
        public void setAsText(String value) {
            initialValue = getBooleanValueAsString(value);
            checkbox.setSelected(Boolean.parseBoolean(initialValue));
        }

        @Override
        public Object getValue() {
            return getAsText();
        }

        @Override
        public void setValue(Object value) {
            if (value == null || value instanceof String) {
                setAsText(getBooleanValueAsString((String)value));
            } else {
                throw new IllegalArgumentException();
            }
        }

        /**
         * "false" leads to false
         * "true" or any other non "false" value leads to true
         * @param value boolean or UUID
         * @return String true/false
         */
        private static String getBooleanValueAsString(String value) {
            return value == null ? FALSE_AS_STRING :
                // We must use this form as:
                // - "false" leads to false
                // - "true" or any other non "false" value leads to true
                Boolean.toString(!FALSE_AS_STRING.equals(value));
        }

        @Override
        public Component getCustomEditor() {
            return checkbox;
        }

        @Override
        public void firePropertyChange() {
            String newValue = getAsText();

            if (initialValue != null && initialValue.equals(newValue)) {
                return;
            }
            initialValue = newValue;

            super.firePropertyChange();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            firePropertyChange();
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public void clearGui() {
            initialValue = TRUE_AS_STRING;
            checkbox.setSelected(true);
        }
    }
}
