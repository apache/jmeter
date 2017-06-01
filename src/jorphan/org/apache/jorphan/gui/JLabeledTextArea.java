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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * A Helper component that wraps a JTextField with a label into a JPanel (this).
 * This component also has an efficient event handling mechanism for handling
 * the text changing in the Text Field. The registered change listeners are only
 * called when the text has changed.
 *
 */
public class JLabeledTextArea extends JPanel implements JLabeledField, FocusListener {
    private static final long serialVersionUID = 240L;

    private final JLabel mLabel;

    private final JTextArea mTextArea;

    private final ArrayList<ChangeListener> mChangeListeners = new ArrayList<>(3);

    // A temporary cache for the focus listener
    private String oldValue = "";

    /**
     * Default constructor, The label and the Text field are left empty.
     */
    public JLabeledTextArea() {
        this("", null);
    }

    /**
     * Constructs a new component with the label displaying the passed text.
     *
     * @param label
     *            The text to display in the label.
     */
    public JLabeledTextArea(String label) {
        this(label, null);
    }

    /**
     * Constructs a new component with the label displaying the passed text.
     *
     * @param pLabel
     *            The text to display in the label.
     * @param docModel the document for the text area
     */
    public JLabeledTextArea(String pLabel, Document docModel) {
        super();
        mTextArea = new JTextArea();
        mLabel = new JLabel(pLabel);
        init();
        if (docModel != null) {
            mTextArea.setDocument(docModel);
        }
    }

    /**
     * Get the label {@link JLabel} followed by the text field @link {@link JTextArea}.
     */
    @Override
    public List<JComponent> getComponentList() {
        List<JComponent> comps = new LinkedList<>();
        comps.add(mLabel);
        comps.add(mTextArea);
        return comps;
    }

    public void setDocumentModel(Document docModel) {
        mTextArea.setDocument(docModel);
    }

    /**
     * Initialises all of the components on this panel.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());

        mTextArea.setRows(4);
        mTextArea.setLineWrap(true);
        mTextArea.setWrapStyleWord(true);
        // Register the handler for focus listening. This handler will
        // only notify the registered when the text changes from when
        // the focus is gained to when it is lost.
        mTextArea.addFocusListener(this);

        // Add the sub components
        this.add(mLabel, BorderLayout.NORTH);
        this.add(new JScrollPane(mTextArea), BorderLayout.CENTER);
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
        if (!oldValue.equals(mTextArea.getText())) {
            notifyChangeListeners();
        }
    }

    /**
     * Catch what the value was when focus was gained.
     */
    @Override
    public void focusGained(FocusEvent pFocusEvent) {
        oldValue = mTextArea.getText();
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
        mTextArea.setText(pText);
    }

    /**
     * Returns the text in the Text Field.
     *
     * @return The text in the Text Field.
     */
    @Override
    public String getText() {
        return mTextArea.getText();
    }

    /**
     * Returns the text of the label.
     *
     * @return The text of the label.
     */
    public String getLabel() {
        return mLabel.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        mTextArea.setEnabled(enable);
    }

    /**
     * Registers the text to display in a tool tip.
     * The text displays when the cursor lingers over the component.
     * @param text the string to display; if the text is null,
     *      the tool tip is turned off for this component
     */
    @Override
    public void setToolTipText(String text) {
        mTextArea.setToolTipText(text);
    }

    /**
      * Returns the tooltip string that has been set with setToolTipText
      * @return the text of the tool tip
      */
    @Override
    public String getToolTipText() {
        return mTextArea.getToolTipText();
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
    private void notifyChangeListeners() {
        ChangeEvent ce = new ChangeEvent(this);
        for (ChangeListener mChangeListener : mChangeListeners) {
            mChangeListener.stateChanged(ce);
        }
    }
    
    public String[] getTextLines() {
        int numLines = mTextArea.getLineCount();
        String[] lines = new String[numLines];
        for(int i = 0; i < numLines; i++) {
            try {
                int start = mTextArea.getLineStartOffset(i);
                int end = mTextArea.getLineEndOffset(i); // treats last line specially
                if (i == numLines-1) { // Last line
                    end++; // Allow for missing terminator
                }
                lines[i]=mTextArea.getText(start, end-start-1);
            } catch (BadLocationException e) { // should not happen
                throw new IllegalStateException("Could not read line "+i,e);
            }
        }
        return lines;
    }
}
