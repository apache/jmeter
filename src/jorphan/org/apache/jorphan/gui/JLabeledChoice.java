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

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JLabeledChoice extends JPanel implements JLabeledField {
    private static final long serialVersionUID = 240L;

    private final JLabel mLabel = new JLabel();

    private final JComboBox<String> choiceList;

    private final boolean withButtons;

    private final ArrayList<ChangeListener> mChangeListeners = new ArrayList<>(3);

    private JButton delete;
    private JButton add;

    /**
     * Default constructor, The label and the Text field are left empty.
     */
    public JLabeledChoice() {
        super();
        choiceList = new JComboBox<>();
        withButtons = false;
        init();
    }

    public JLabeledChoice(String pLabel, boolean editable) {
        super();
        choiceList = new JComboBox<>();
        mLabel.setText(pLabel);
        choiceList.setEditable(editable);
        withButtons = false;
        init();
    }

    /**
     * Constructs a non-editable combo-box with the label displaying the passed text.
     *
     * @param pLabel - the text to display in the label.
     * @param items - the items to display in the Combo box
     */
    public JLabeledChoice(String pLabel, String[] items) {
        this(pLabel, items, false);
    }

    /**
     * Constructs a combo-box with the label displaying the passed text.
     *
     * @param pLabel - the text to display in the label.
     * @param items - the items to display in the Combo box
     * @param editable - if true, then Add and Delete buttons are created.
     *
     */
    public JLabeledChoice(String pLabel, String[] items, boolean editable) {
        this(pLabel, items, editable, editable);
    }

    /**
     * Constructs a combo-box with the label displaying the passed text.
     *
     * @param pLabel - the text to display in the label.
     * @param items - the items to display in the Combo box
     * @param editable - the box is made editable
     * @param withButtons - if true, then Add and Delete buttons are created.
     *
     */
    public JLabeledChoice(String pLabel, String[] items, boolean editable, boolean withButtons) {
        super();
        mLabel.setText(pLabel);
        choiceList = new JComboBox<>(items);
        choiceList.setEditable(editable);
        this.withButtons = withButtons;
        init();
    }

    /**
     * Get the label {@link JLabel} followed by the combo-box @link {@link JComboBox}.
     */
    @Override
    public List<JComponent> getComponentList() {
        List<JComponent> comps = new LinkedList<>();
        comps.add(mLabel);
        comps.add(choiceList);
        return comps;
    }

    public void setEditable(boolean editable) {
        choiceList.setEditable(editable);
    }

    public void addValue(String item) {
        choiceList.addItem(item);
    }

    public void setValues(String[] items) {
        choiceList.removeAllItems();
        for (String item : items) {
            choiceList.addItem(item);
        }
    }

    /**
     * Initialises all of the components on this panel.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        // Register the handler for focus listening. This handler will
        // only notify the registered when the text changes from when
        // the focus is gained to when it is lost.
        choiceList.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                notifyChangeListeners();
            }
        });

        // Add the sub components
        this.add(mLabel);
        this.add(choiceList);
        if (withButtons) {
            add = new JButton("Add");
            add.setMargin(new Insets(1, 1, 1, 1));
            add.addActionListener(new AddListener());
            this.add(add);
            delete = new JButton("Del");
            delete.setMargin(new Insets(1, 1, 1, 1));
            delete.addActionListener(new DeleteListener());
            this.add(delete);
        }

    }

    public void setChoiceListEnabled(boolean enabled) {
        choiceList.setEnabled(enabled);
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
        choiceList.setSelectedItem(pText);
    }

    public void setSelectedIndex(int index){
        choiceList.setSelectedIndex(index);
    }
    /**
     * Returns the text in the Text Field.
     *
     * @return The text in the Text Field. Never returns null.
     */
    @Override
    public String getText() {
        Object item = choiceList.getSelectedItem();
        if (item == null) {
            return "";
        } else {
            return (String) item;
        }
    }

    public int getSelectedIndex(){
        return choiceList.getSelectedIndex();
    }

    public Object[] getSelectedItems() {
        return choiceList.getSelectedObjects();
    }

    public String[] getItems() {
        String[] items = new String[choiceList.getItemCount()];
        for (int i = 0; i < items.length; i++) {
            items[i] = choiceList.getItemAt(i);
        }
        return items;
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
        choiceList.setToolTipText(text);
    }

    /**
     * Returns the tooltip string that has been set with setToolTipText
     * @return the text of the tool tip
     */
    @Override
    public String getToolTipText() {
        if (choiceList == null){ // Necessary to avoid NPE when testing serialisation
            return null;
        }
        return choiceList.getToolTipText();
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

    private class AddListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String item = (String) choiceList.getSelectedItem();
            int index = choiceList.getSelectedIndex();
            if (!item.equals(choiceList.getItemAt(index))) {
                choiceList.addItem(item);
            }
            choiceList.setSelectedItem(item);
            notifyChangeListeners();
        }
    }

    private class DeleteListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (choiceList.getItemCount() > 1) {
                choiceList.removeItemAt(choiceList.getSelectedIndex());
                notifyChangeListeners();
            }
        }
    }
}
