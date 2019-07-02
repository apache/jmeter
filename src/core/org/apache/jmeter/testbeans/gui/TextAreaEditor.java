/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

/*
 * Created on May 21, 2004
 */
package org.apache.jmeter.testbeans.gui;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;

import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;

public class TextAreaEditor extends PropertyEditorSupport implements FocusListener, PropertyChangeListener {

    private final JSyntaxTextArea textUI;

    private final JTextScrollPane scroller;

    /** {@inheritDoc} */
    @Override
    public void focusGained(FocusEvent e) {
    }

    /** {@inheritDoc} */
    @Override
    public void focusLost(FocusEvent e) {
        firePropertyChange();
    }

    private final void init() {// called from ctor, so must not be overridable
        textUI.discardAllEdits();
        textUI.addFocusListener(this);
    }

    /**
     *
     */
    public TextAreaEditor() {
        super();
        textUI = JSyntaxTextArea.getInstance(20, 20);
        scroller = JTextScrollPane.getInstance(textUI, true);
        init();
    }

    /**
     * @param source the source used for event firing
     */
    // TODO is this ever used?
    public TextAreaEditor(Object source) {
        super(source);
        textUI = JSyntaxTextArea.getInstance(20, 20);
        scroller = JTextScrollPane.getInstance(textUI, true);
        init();
        setValue(source);
    }

    /**
     * Construct a {@link TextAreaEditor} using the properties of a given
     * {@link PropertyDescriptor}
     *
     * @param descriptor
     *            to be used for the editor. Must not be <code>null</code>
     */
    public TextAreaEditor(PropertyDescriptor descriptor) {
        textUI = JSyntaxTextArea.getInstance(10, 60);
        textUI.setLanguage((String) descriptor.getValue(GenericTestBeanCustomizer.TEXT_LANGUAGE));
        scroller = JTextScrollPane.getInstance(textUI, true);
        init();
    }

    /** {@inheritDoc} */
    @Override
    public String getAsText() {
        return textUI.getText();
    }

    /** {@inheritDoc} */
    @Override
    public Component getCustomEditor() {
        return scroller;
    }

    /** {@inheritDoc} */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        textUI.setInitialText(text);
        textUI.setCaretPosition(0);
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(Object value) {
        if (value != null) {
            textUI.setInitialText(value.toString());
            textUI.setCaretPosition(0);
        } else {
            textUI.setInitialText("");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object getValue() {
        return textUI.getText();
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        if (source instanceof ComboStringEditor) {
            ComboStringEditor cse = (ComboStringEditor) source;
            String lang = cse.getAsText().toLowerCase();
            textUI.setLanguage(lang);
        }
    }
}
