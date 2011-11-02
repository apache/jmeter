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
import java.beans.PropertyEditorSupport;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextAreaEditor extends PropertyEditorSupport implements FocusListener {

    private JTextArea textUI;

    private JScrollPane scroller;

    /** {@inheritDoc} */
    public void focusGained(FocusEvent e) {
    }

    /** {@inheritDoc} */
    public void focusLost(FocusEvent e) {
        firePropertyChange();
    }

    private void init() {// called from ctor, so must not be overridable
        textUI = new JTextArea();
        textUI.addFocusListener(this);
        textUI.setWrapStyleWord(true);
        textUI.setLineWrap(true);
        scroller = new JScrollPane(textUI, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    /**
     *
     */
    public TextAreaEditor() {
        super();
        init();
    }

    /**
     * @param source
     */
    public TextAreaEditor(Object source) {
        super(source);
        init();
        setValue(source);
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
        textUI.setText(text);
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(Object value) {
        if (value != null) {
            textUI.setText(value.toString());
        } else {
            textUI.setText("");
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
}
