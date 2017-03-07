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
 * Created on Sep 15, 2004
 */
package org.apache.jmeter.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import org.apache.jmeter.gui.action.KeyStrokes;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.Functor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates Configure pop-up dialogue for Listeners from all methods in SampleSaveConfiguration
 * with the signature "boolean saveXXX()".
 * There must be a corresponding "void setXXX(boolean)" method, and a property save_XXX which is
 * used to name the field on the dialogue.
 *
 */
public class SavePropertyDialog extends JDialog implements ActionListener {

    private static final Logger log = LoggerFactory.getLogger(SavePropertyDialog.class);

    private static final long serialVersionUID = 233L;

    private static final Map<String, Functor> functors = new HashMap<>();

    private static final String RESOURCE_PREFIX = "save_"; // $NON-NLS-1$ e.g. save_XXX property

    private SampleSaveConfiguration saveConfig;

    /**
     * @deprecated Constructor only intended for use in testing
     */
    @Deprecated // Constructor only intended for use in testing
    public SavePropertyDialog() {
        log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
    }
    /**
     * @param owner The {@link Frame} from which the dialog is displayed
     * @param title The string to be used as a title of this dialog
     * @param modal specifies whether the dialog should be modal
     * @param s The details, which sample attributes are to be saved
     * @throws java.awt.HeadlessException - when run headless
     */
    public SavePropertyDialog(Frame owner, String title, boolean modal, SampleSaveConfiguration s)
    {
        super(owner, title, modal);
        saveConfig = s;
        log.debug("SampleSaveConfiguration = {}", saveConfig);// $NON-NLS-1$
        initDialog();
    }

    private void initDialog() {
        this.getContentPane().setLayout(new BorderLayout());
        final int configCount = (SampleSaveConfiguration.SAVE_CONFIG_NAMES.size() / 3) + 1;
        log.debug("grid panel is {} by {}", 3, configCount);
        JPanel checkPanel = new JPanel(new GridLayout(configCount, 3));
        for (final String name : SampleSaveConfiguration.SAVE_CONFIG_NAMES) {
            try {
                JCheckBox check = new JCheckBox(
                        JMeterUtils.getResString(RESOURCE_PREFIX + name),
                        getSaveState(SampleSaveConfiguration.getterName(name)));
                check.addActionListener(this);
                final String actionCommand = SampleSaveConfiguration.setterName(name); // $NON-NLS-1$
                check.setActionCommand(actionCommand);
                if (!functors.containsKey(actionCommand)) {
                    functors.put(actionCommand, new Functor(actionCommand));
                }
                checkPanel.add(check, BorderLayout.NORTH);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.warn("Problem creating save config dialog", e);
            }
        }
        getContentPane().add(checkPanel, BorderLayout.NORTH);
        JButton exit = new JButton(JMeterUtils.getResString("done")); // $NON-NLS-1$
        this.getContentPane().add(exit, BorderLayout.SOUTH);
        exit.addActionListener(e -> dispose());
    }
    
    @Override
    protected JRootPane createRootPane() {
        JRootPane rootPane = new JRootPane();
        Action escapeAction = new AbstractAction("ESCAPE") {
            /**
             * 
             */
            private static final long serialVersionUID = 2208129319916921772L;

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStrokes.ESC, escapeAction.getValue(Action.NAME));
        rootPane.getActionMap().put(escapeAction.getValue(Action.NAME), escapeAction);
        return rootPane;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        Functor f = functors.get(action);
        f.invoke(saveConfig, new Object[] {Boolean.valueOf(((JCheckBox) e.getSource()).isSelected()) });
    }

    private boolean getSaveState(String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = SampleSaveConfiguration.class.getMethod(methodName);
        return ((Boolean) method.invoke(saveConfig)).booleanValue();
    }

    /**
     * @return Returns the saveConfig.
     */
    public SampleSaveConfiguration getSaveConfig() {
        return saveConfig;
    }

    /**
     * @param saveConfig
     *            The saveConfig to set.
     */
    public void setSaveConfig(SampleSaveConfiguration saveConfig) {
        this.saveConfig = saveConfig;
    }
}
