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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.apache.jmeter.gui.util.FileDialoger;

/**
 * A property editor for File properties.
 * <p>
 * Note that it never gives out File objects, but always Strings. This is
 * because JMeter is now too dumb to handle File objects (there's no
 * FileProperty).
 *
 */
public class FileEditor implements PropertyEditor, ActionListener {

    /**
     * The editor's panel.
     */
    private JPanel panel;

    /**
     * The editor handling the text field inside:
     */
    private PropertyEditor editor;

    public FileEditor() {
        this(null);
    }

    public FileEditor(PropertyDescriptor descriptor) {
        // Create a button to trigger the file chooser:
        JButton button = new JButton("Browse...");
        button.addActionListener(this);

        // Get a WrapperEditor to provide the field or combo -- we'll delegate
        // most methods to it:
        boolean notNull = descriptor == null ? false : GenericTestBeanCustomizer.notNull(descriptor);
        boolean notExpression = descriptor == null ? false : GenericTestBeanCustomizer.notExpression(descriptor);
        boolean notOther = descriptor == null ? false : GenericTestBeanCustomizer.notOther(descriptor);
        Object defaultValue = descriptor == null ? null : descriptor.getValue(GenericTestBeanCustomizer.DEFAULT);
        ComboStringEditor cse = new ComboStringEditor();
        cse.setNoUndefined(notNull);
        cse.setNoEdit(notExpression && notOther);
        editor = new WrapperEditor(this, new SimpleFileEditor(), cse,
                !notNull, // acceptsNull
                !notExpression, // acceptsExpressions
                !notOther, // acceptsOther
                defaultValue); // default

        // Create a panel containing the combo and the button:
        panel = new JPanel(new BorderLayout(5, 0));
        panel.add(editor.getCustomEditor(), BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = FileDialoger.promptToOpenFile();

        if (chooser == null){
            return;
        }

        setValue(chooser.getSelectedFile().getPath());
    }

    /**
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        editor.addPropertyChangeListener(listener);
    }

    /**
     * @return the text
     */
    public String getAsText() {
        return editor.getAsText();
    }

    /**
     * @return custom editor panel
     */
    public Component getCustomEditor() {
        return panel;
    }

    /**
     * @return the Java initialisation string
     */
    public String getJavaInitializationString() {
        return editor.getJavaInitializationString();
    }

    /**
     * @return the editor tags
     */
    public String[] getTags() {
        return editor.getTags();
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return editor.getValue();
    }

    /**
     * @return true if the editor is paintable
     */
    public boolean isPaintable() {
        return editor.isPaintable();
    }

    /**
     * @param gfx
     * @param box
     */
    public void paintValue(Graphics gfx, Rectangle box) {
        editor.paintValue(gfx, box);
    }

    /**
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        editor.removePropertyChangeListener(listener);
    }

    /**
     * @param text
     * @throws java.lang.IllegalArgumentException
     */
    public void setAsText(String text) throws IllegalArgumentException {
        editor.setAsText(text);
    }

    /**
     * @param value
     */
    public void setValue(Object value) {
        editor.setValue(value);
    }

    /**
     * @return true if supports a custom editor
     */
    public boolean supportsCustomEditor() {
        return editor.supportsCustomEditor();
    }

    private static class SimpleFileEditor extends PropertyEditorSupport {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getAsText() {
            return ((File) super.getValue()).getPath();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            super.setValue(new File(text));
        }

        /**
         * JMeter doesn't support File properties yet. Need to
         * work on this as a String :-(
         * <p>
         * {@inheritDoc}
         */
        @Override
        public Object getValue() {
            return getAsText(); // should be super.getValue();
        }

        /**
         * I need to handle Strings when setting too.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public void setValue(Object file) {
            setAsText((String) file);
        }
    }
}