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

package org.apache.jorphan.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A Helper component that wraps a JTextField with a label into a JPanel (this).
 * This component also has an efficient event handling mechanism for handling
 * the text changing in the Text Field. The registered change listeners are only
 * called when the text has changed.
 *
 */
public class JLabeledTextField extends JPanel implements JLabeledField, FocusListener {
    private static final long serialVersionUID = 240L;

    private JLabel mLabel;

    private JTextField mTextField;

    private final ArrayList<ChangeListener> mChangeListeners = new ArrayList<>(3);

    // A temporary cache for the focus listener
    private String oldValue = "";

    /**
     * Default constructor, The label and the Text field are left empty.
     */
    public JLabeledTextField() {
        this("", 20);
    }

    /**
     * Constructs a new component with the label displaying the passed text.
     *
     * @param pLabel
     *            The text to in the label.
     */
    public JLabeledTextField(String pLabel) {
        this(pLabel, 20);
    }

    public JLabeledTextField(String pLabel, int size) {
        super();
        mTextField = createTextField(size);
        mLabel = new JLabel(pLabel);
        mLabel.setLabelFor(mTextField);
        init();
    }

    public JLabeledTextField(String pLabel, Color bk) {
        super();
        mTextField = createTextField(20);
        mLabel = new JLabel(pLabel);
        mLabel.setBackground(bk);
        mLabel.setLabelFor(mTextField);
        this.setBackground(bk);
        init();
    }

    /**
     * Get the label {@link JLabel} followed by the text field @link {@link JTextField}.
     */
    @Override
    public List<JComponent> getComponentList() {
        List<JComponent> comps = new LinkedList<>();
        comps.add(mLabel);
        comps.add(mTextField);
        return comps;
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        mTextField.setEnabled(enable);
    }

    protected JTextField createTextField(int size) {
        return new JTextField(size);
    }

    /**
     * Initialises all of the components on this panel.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(5, 0));
        // Register the handler for focus listening. This handler will
        // only notify the registered when the text changes from when
        // the focus is gained to when it is lost.
        mTextField.addFocusListener(this);

        // Add the sub components
        add(mLabel, BorderLayout.WEST);
        add(mTextField, BorderLayout.CENTER);
    }

    /**
     * Callback method when the focus to the Text Field component is lost.
     *
     * @param pFocusEvent
     *            The focus event that occurred.
     */
    @Override
    public void focusLost(FocusEvent pFocusEvent) {
        // Compare if the value has changed, since we received focus.
        if (!oldValue.equals(mTextField.getText())) {
            notifyChangeListeners();
        }
    }

    /**
     * Catch what the value was when focus was gained.
     */
    @Override
    public void focusGained(FocusEvent pFocusEvent) {
        oldValue = mTextField.getText();
    }

    /**
     * Set the text displayed in the label.
     *
     * @param pLabel
     *            The new label text.
     */
    @Override
    public void setLabel(String pLabel) {
        mLabel.setText(pLabel);
    }

    /**
     * Set the text displayed in the Text Field.
     *
     * @param pText
     *            The new text to display in the text field.
     */
    @Override
    public void setText(String pText) {
        mTextField.setText(pText);
    }

    /**
     * Returns the text in the Text Field.
     *
     * @return The text in the Text Field.
     */
    @Override
    public String getText() {
        return mTextField.getText();
    }

    /**
     * Returns the text of the label.
     *
     * @return The text of the label.
     */
    public String getLabel() {
        return mLabel.getText();
    }

    /**
     * Registers the text to display in a tool tip.
     * The text displays when the cursor lingers over the component.
     * @param text the string to display; if the text is null,
     *      the tool tip is turned off for this component
     */
    @Override
    public void setToolTipText(String text) {
        mLabel.setToolTipText(text);
        mTextField.setToolTipText(text);
    }

    /**
      * Returns the tooltip string that has been set with setToolTipText
      * @return the text of the tool tip
      */
    @Override
    public String getToolTipText() {
        if (mTextField == null){ // Necessary to avoid NPE when testing serialisation
            return null;
        }
       return mTextField.getToolTipText();
    }

    /**
     * Adds a change listener, that will be notified when the text in the text
     * field is changed. The ChangeEvent that will be passed to registered
     * listeners will contain this object as the source, allowing the new text
     * to be extracted using the {@link #getText() getText} method.
     *
     * @param pChangeListener
     *            The listener to add
     */
    @Override
    public void addChangeListener(ChangeListener pChangeListener) {
        mChangeListeners.add(pChangeListener);
    }

    /**
     * Removes a change listener.
     *
     * @param pChangeListener
     *            The change listener to remove.
     */
    public void removeChangeListener(ChangeListener pChangeListener) {
        mChangeListeners.remove(pChangeListener);
    }

    /**
     * Notify all registered change listeners that the text in the text field
     * has changed.
     */
    protected void notifyChangeListeners() {
        ChangeEvent ce = new ChangeEvent(this);
        for (ChangeListener mChangeListener : mChangeListeners) {
            mChangeListener.stateChanged(ce);
        }
    }
}
