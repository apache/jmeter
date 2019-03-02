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
import java.beans.IntrospectionException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.apache.commons.io.FilenameUtils;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.util.JMeterUtils;

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
    private final JPanel panel;

    /**
     * The editor handling the text field inside:
     */
    private final PropertyEditor editor;

    /**
     * @throws IntrospectionException
     *             when introspection fails while creating a dummy
     *             PropertyDescriptor
     * @deprecated Only for use by test cases
     */
    @Deprecated
    public FileEditor() throws IntrospectionException {
        this(new PropertyDescriptor("dummy", null, null));
    }

    /**
     * Construct a {@link FileEditor} using the properties of the given
     * {@link PropertyDescriptor}
     * 
     * @param descriptor
     *            the {@link PropertyDescriptor} to be used. Must not be <code>null</code>
     * @throws IllegalArgumentException
     *             when <code>descriptor</code> is <code>null</code>
     */
    public FileEditor(PropertyDescriptor descriptor) {
        if (descriptor == null) {
            throw new IllegalArgumentException("Descriptor must not be null");
        }

        // Create a button to trigger the file chooser:
        JButton button = new JButton(JMeterUtils.getResString("browse"));
        button.addActionListener(this);

        // Get a WrapperEditor to provide the field or combo -- we'll delegate
        // most methods to it:
        boolean notNull = GenericTestBeanCustomizer.notNull(descriptor);
        boolean notExpression = GenericTestBeanCustomizer.notExpression(descriptor);
        boolean notOther = GenericTestBeanCustomizer.notOther(descriptor);
        Object defaultValue = descriptor.getValue(GenericTestBeanCustomizer.DEFAULT);
        FieldStringEditor cse = new FieldStringEditor();
        editor = new WrapperEditor(this, new PropertyEditorSupport(), cse,
                !notNull, // acceptsNull
                !notExpression, // acceptsExpressions
                !notOther, // acceptsOther
                defaultValue == null ? "":defaultValue); // default // //$NON-NLS-1$

        // Create a panel containing the combo and the button:
        panel = new JPanel(new BorderLayout(5, 0));
        panel.add(editor.getCustomEditor(), BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = FileDialoger.promptToOpenFile();

        if (chooser == null){
            return;
        }

        setValue(toUnix(chooser.getSelectedFile()));
    }

    private String toUnix(final File selectedFile) {
        if (File.separatorChar == '\\') {
            return FilenameUtils.separatorsToUnix(selectedFile.getPath());
        }
        return selectedFile.getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        editor.addPropertyChangeListener(listener);
    }

    /**
     * @return the text
     */
    @Override
    public String getAsText() {
        return editor.getAsText();
    }

    /**
     * @return custom editor panel
     */
    @Override
    public Component getCustomEditor() {
        return panel;
    }

    /**
     * @return the Java initialisation string
     */
    @Override
    public String getJavaInitializationString() {
        return editor.getJavaInitializationString();
    }

    /**
     * @return the editor tags
     */
    @Override
    public String[] getTags() {
        return editor.getTags();
    }

    /**
     * @return the value
     */
    @Override
    public Object getValue() {
        return editor.getValue();
    }

    /**
     * @return true if the editor is paintable
     */
    @Override
    public boolean isPaintable() {
        return editor.isPaintable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        editor.paintValue(gfx, box);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        editor.removePropertyChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAsText(String text) {
        editor.setAsText(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(Object value) {
        editor.setValue(value);
    }

    /**
     * @return true if supports a custom editor
     */
    @Override
    public boolean supportsCustomEditor() {
        return editor.supportsCustomEditor();
    }

}
