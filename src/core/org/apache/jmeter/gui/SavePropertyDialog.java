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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.Functor;
import org.apache.log.Logger;

/**
 * Generates Configure pop-up dialogue for Listeners from all methods in SampleSaveConfiguration
 * with the signature "boolean saveXXX()".
 * There must be a corresponding "void setXXX(boolean)" method, and a property save_XXX which is
 * used to name the field on the dialogue.
 *
 */
public class SavePropertyDialog extends JDialog implements ActionListener {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 232L;

    private static final Map<String, Functor> functors = new HashMap<String, Functor>();

    private static final String NAME_SAVE_PFX   = "save";  // $NON-NLS-1$ i.e. boolean saveXXX()
    private static final String NAME_SET_PREFIX = "set";   // $NON-NLS-1$ i.e. void setXXX(boolean)
    private static final String RESOURCE_PREFIX = "save_"; // $NON-NLS-1$ e.g. save_XXX property
    private static final int    NAME_SAVE_PFX_LEN = NAME_SAVE_PFX.length();

    private SampleSaveConfiguration saveConfig;

    public SavePropertyDialog(){
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
    // throws HeadlessException
    {
        super(owner, title, modal);
        saveConfig = s;
        log.debug("SampleSaveConfiguration = " + saveConfig);// $NON-NLS-1$
        initDialog();
    }

    private int countMethods(Method[] m) {
        int count = 0;
        for (int i = 0; i < m.length; i++) {
            if (m[i].getName().startsWith(NAME_SAVE_PFX)) {
                count++;
            }
        }
        return count;
    }

    private void initDialog() {
        this.getContentPane().setLayout(new BorderLayout());
        Method[] methods = SampleSaveConfiguration.class.getMethods();
        int x = (countMethods(methods) / 3) + 1;
        log.debug("grid panel is " + 3 + " by " + x);
        JPanel checkPanel = new JPanel(new GridLayout(x, 3));
        for (int i = 0; i < methods.length; i++) {
            String name = methods[i].getName();
            if (name.startsWith(NAME_SAVE_PFX) && methods[i].getParameterTypes().length == 0) {
                try {
                    name = name.substring(NAME_SAVE_PFX_LEN);
                    JCheckBox check = new JCheckBox(
                            JMeterUtils.getResString(RESOURCE_PREFIX + name)
                            ,((Boolean) methods[i].invoke(saveConfig, new Object[0])).booleanValue());
                    checkPanel.add(check, BorderLayout.NORTH);
                    check.addActionListener(this);
                    String actionCommand = NAME_SET_PREFIX + name; // $NON-NLS-1$
                    check.setActionCommand(actionCommand);
                    if (!functors.containsKey(actionCommand)) {
                        functors.put(actionCommand, new Functor(actionCommand));
                    }
                } catch (IllegalAccessException e) {
                    log.warn("Problem creating save config dialog", e);
                } catch (InvocationTargetException e) {
                    log.warn("Problem creating save config dialog", e);
                }
            }
        }
        getContentPane().add(checkPanel, BorderLayout.NORTH);
        JButton exit = new JButton(JMeterUtils.getResString("done")); // $NON-NLS-1$
        this.getContentPane().add(exit, BorderLayout.SOUTH);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        Functor f = functors.get(action);
        f.invoke(saveConfig, new Object[] {
                Boolean.valueOf(((JCheckBox) e.getSource()).isSelected()) });
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
