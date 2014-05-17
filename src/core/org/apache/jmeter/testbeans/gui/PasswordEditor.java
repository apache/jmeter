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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyEditorSupport;

import javax.swing.JPasswordField;

/**
 * This class implements a property editor for non-null String properties that
 * supports custom editing (i.e.: provides a GUI component) based on a text
 * field.
 * <p>
 * The provided GUI is a simple password field.
 *
 */
public class PasswordEditor extends PropertyEditorSupport implements ActionListener, FocusListener {

    private JPasswordField textField;

    /**
     * Value on which we started the editing. Used to avoid firing
     * PropertyChanged events when there's not been such change.
     */
    private String initialValue = "";

    protected PasswordEditor() {
        super();

        textField = new JPasswordField();
        textField.addActionListener(this);
        textField.addFocusListener(this);
    }

    @Override
    public String getAsText() {
        return new String(textField.getPassword());
    }

    @Override
    public void setAsText(String value) {
        initialValue = value;
        textField.setText(value);
    }

    @Override
    public Object getValue() {
        return getAsText();
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof String) {
            setAsText((String) value);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getCustomEditor() {
        return textField;
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * Avoid needlessly firing PropertyChanged events.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void firePropertyChange() {
        String newValue = getAsText();

        if (initialValue.equals(newValue)) {
            return;
        }
        initialValue = newValue;

        super.firePropertyChange();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        firePropertyChange();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void focusGained(FocusEvent e) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void focusLost(FocusEvent e) {
        firePropertyChange();
    }
}
