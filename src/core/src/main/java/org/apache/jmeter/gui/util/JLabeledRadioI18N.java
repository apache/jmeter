/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.gui.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledField;

/**
 * JLabeledRadioI18N creates a set of Radio buttons with a label.
 * This is a version of the original JLabelledRadio class (now removed), but modified
 * to accept resource names rather than language strings.
 *
 */
public class JLabeledRadioI18N extends JPanel implements JLabeledField, ActionListener {

    private static final long serialVersionUID = 240L;

    private final JLabel mLabel = new JLabel();

    private final ButtonGroup bGroup = new ButtonGroup();

    private final ArrayList<ChangeListener> mChangeListeners = new ArrayList<>(3);

    /**
     *
     * @param labelResource text resource name for group label
     * @param itemResources list of resource names for individual buttons
     * @param selectedItem button to be selected (if not null)
     */
    public JLabeledRadioI18N(String labelResource, String[] itemResources, String selectedItem) {
        setLabel(labelResource);
        init(itemResources, selectedItem);
    }

    /**
     * @deprecated - only for use in testing
     */
    @Deprecated
    public JLabeledRadioI18N() {
        super();
    }

    /**
     * Method is responsible for creating the JRadioButtons and adding them to
     * the ButtonGroup.
     *
     * The resource name is used as the action command for the button model,
     * and the resource value is used to set the button label.
     *
     * @param resources list of resource names
     * @param selected initially selected resource (if not null)
     *
     */
    private void init(String[] resources, String selected) {
        this.add(mLabel);
        initButtonGroup(resources, selected);
    }

    /**
     * Method is responsible for creating the JRadioButtons and adding them to
     * the ButtonGroup.
     *
     * The resource name is used as the action command for the button model,
     * and the resource value is used to set the button label.
     *
     * @param resources list of resource names
     * @param selected initially selected resource (if not null)
     *
     */
    private void initButtonGroup(String[] resources, String selected) {
        for (String resource : resources) {
            JRadioButton btn = new JRadioButton(JMeterUtils.getResString(resource));
            btn.setActionCommand(resource);
            btn.addActionListener(this);
            // add the button to the button group
            this.bGroup.add(btn);
            // add the button
            this.add(btn);
            if (selected != null && selected.equals(resource)) {
                btn.setSelected(true);
            }
        }
    }

    /**
     * Method is responsible for removing current JRadioButtons of ButtonGroup and
     * add creating the JRadioButtons and adding them to
     * the ButtonGroup.
     *
     * The resource name is used as the action command for the button model,
     * and the resource value is used to set the button label.
     *
     * @param resources list of resource names
     * @param selected initially selected resource (if not null)
     *
     */
    @SuppressWarnings("JdkObsolete")
    public void resetButtons(String[] resources, String selected) {
        Enumeration<AbstractButton> buttons = bGroup.getElements();
        List<AbstractButton> buttonsToRemove = new ArrayList<>(this.bGroup.getButtonCount());
        while (buttons.hasMoreElements()) {
            AbstractButton abstractButton = buttons
                    .nextElement();
            buttonsToRemove.add(abstractButton);
        }
        for (AbstractButton abstractButton : buttonsToRemove) {
            abstractButton.removeActionListener(this);
            bGroup.remove(abstractButton);
        }
        for (AbstractButton abstractButton : buttonsToRemove) {
            this.remove(abstractButton);
        }
        initButtonGroup(resources, selected);
    }

    /**
     * The implementation will get the resource name from the selected radio button
     * in the JButtonGroup.
     */
    @Override
    public String getText() {
        return this.bGroup.getSelection().getActionCommand();
    }

    /**
     * The implementation will iterate through the radio buttons and find the
     * match. It then sets it to selected and sets all other radio buttons as
     * not selected.
     * @param resourceName name of resource whose button is to be selected
     */
    @Override
    @SuppressWarnings("JdkObsolete")
    public void setText(String resourceName) {
        Enumeration<AbstractButton> en = this.bGroup.getElements();
        while (en.hasMoreElements()) {
            ButtonModel model = en.nextElement().getModel();
            if (model.getActionCommand().equals(resourceName)) {
                this.bGroup.setSelected(model, true);
            } else {
                this.bGroup.setSelected(model, false);
            }
        }
    }

    /**
     * Set the group label from the resource name.
     *
     * @param labelResource The text to be looked up and set
     */
    @Override
    public final void setLabel(String labelResource) {
        this.mLabel.setText(JMeterUtils.getResString(labelResource));
    }

    /** {@inheritDoc} */
    @Override
    public void addChangeListener(ChangeListener pChangeListener) {
        this.mChangeListeners.add(pChangeListener);
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

    /**
     * Method will return all the label and JRadioButtons. ButtonGroup is
     * excluded from the list.
     */
    @Override
    @SuppressWarnings("JdkObsolete")
    public List<JComponent> getComponentList() {
        List<JComponent> comps = new ArrayList<>();
        comps.add(mLabel);
        Enumeration<AbstractButton> en = this.bGroup.getElements();
        while (en.hasMoreElements()) {
            comps.add(en.nextElement());
        }
        return comps;
    }

    /**
     * When a radio button is clicked, an ActionEvent is triggered.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        this.notifyChangeListeners();
    }

}
