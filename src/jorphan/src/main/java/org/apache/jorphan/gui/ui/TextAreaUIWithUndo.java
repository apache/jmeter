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

package org.apache.jorphan.gui.ui;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.JTextComponent;

import org.apache.jorphan.gui.JMeterUIDefaults;
import org.apiguardian.api.API;

/**
 * Installs undo for all {@code TextArea} Swing components.
 */
@API(since = "5.3", status = API.Status.EXPERIMENTAL)
public class TextAreaUIWithUndo {
    public static final String UI_CLASS = "TextAreaUI"; // $NON-NLS-1$
    public static final String BACKUP_UI_CLASS = "[jmeter]" + UI_CLASS; // $NON-NLS-1$

    /**
     * Configures {@link UIDefaults} to use the patched class for {@code TextFieldUI}.
     *
     * @param defaults look and feel defaults
     */
    public static void install(UIDefaults defaults) {
        Object lafUI = defaults.get(UI_CLASS);
        String newUI = TextAreaUIWithUndo.class.getName();
        if (newUI.equals(lafUI)) {
            // Do not install the hook twice
            return;
        }
        defaults.put(BACKUP_UI_CLASS, lafUI);
        defaults.put(UI_CLASS, newUI);
    }

    /**
     * Creates a UI for a JTextField.
     * <p>Note: this method is called by Swing.</p>
     *
     * @param component the text field
     * @return the UI
     */
    @SuppressWarnings("unused")
    public static ComponentUI createUI(JComponent component) {
        KerningOptimizer.INSTANCE.installKerningListener((JTextComponent) component);
        TextComponentUI.INSTANCE.installUndo((JTextComponent) component);
        if (component.getClass() == JTextArea.class) {
            component.addPropertyChangeListener("UI",
                    evt -> component.setBorder(UIManager.getBorder(JMeterUIDefaults.TEXTAREA_BORDER)));
        }
        // Temporary restore the proper UI class
        UIManager.put(UI_CLASS, UIManager.get(BACKUP_UI_CLASS));
        try {
            return UIManager.getUI(component);
        } finally {
            // Add our class back so we handle the next created editor
            UIManager.put(UI_CLASS, TextAreaUIWithUndo.class.getName());
        }
    }
}
