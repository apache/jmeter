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

package org.apache.jorphan.gui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusStyle;

import org.apiguardian.api.API;

/**
 * Enables to apply styles that persist across look and feel updates.
 * <p>The class provides APIs to configure Swing components so the look is consistent,
 * and it updates the components when Look and Feel changes</p>
 * <p>Swing API provides no standard components and colors for: small, big components;
 * warning, error styles for labels; and so on</p>
 * <p>Note: by default {@link JTable} comes with fixed {@code rowHeight} which does not work
 * when the fonts are scaled. So you need to call {@link #singleLineRowHeight(JTable)}
 * or configure {@code rowHeight} manually</p>
 */
@API(since = "5.3", status = API.Status.EXPERIMENTAL)
public class JFactory {
    private static final String SIZE_VARIANT = "JComponent.sizeVariant"; // $NON-NLS-1$

    private static final DynamicStyle STYLE = new DynamicStyle();

    /**
     * Re-initializes the current LaF and updates the UI for all the open windows.
     */
    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public static void refreshUI() {
        STYLE.updateLaf();
    }

    /**
     * Set new look and feel for all the open windows.
     * @param className look and feel class name
     */
    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public static void refreshUI(String className) {
        STYLE.updateLaf(className);
    }

    /**
     * By default {@link JTextArea} uses {@code tab} to add tab character,
     * however, sometimes it is desired to use {@code tab} to move focus.
     * @param textArea input textarea to configure moving focus on tab
     * @return input textarea (for fluent APIs)
     */
    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public static JTextArea tabMovesFocus(JTextArea textArea) {
        return STYLE.withDynamic(textArea, c -> {
            FocusActions.bind(c,
                    KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
                    KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK)
            );
        });
    }

    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public static JButton small(JButton component) {
        component.putClientProperty(SIZE_VARIANT, NimbusStyle.SMALL_KEY);
        return STYLE.withFont(component, JMeterUIDefaults.BUTTON_SMALL_FONT);
    }

    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public static JLabel small(JLabel component) {
        return STYLE.withFont(component, JMeterUIDefaults.LABEL_SMALL_FONT);
    }

    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public static JTextField small(JTextField component) {
        component.putClientProperty(SIZE_VARIANT, NimbusStyle.SMALL_KEY);
        return STYLE.withFont(component, JMeterUIDefaults.TEXTFIELD_SMALL_FONT);
    }

    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public static JCheckBox small(JCheckBox component) {
        component.putClientProperty(SIZE_VARIANT, NimbusStyle.SMALL_KEY);
        return STYLE.withFont(component, JMeterUIDefaults.CHECKBOX_SMALL_FONT);
    }

    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public static JLabel big(JLabel component) {
        return STYLE.withFont(component, JMeterUIDefaults.LABEL_BIG_FONT);
    }

    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public static JToolBar small(JToolBar component) {
        return STYLE.withFont(component, JMeterUIDefaults.TOOLBAR_SMALL_FONT);
    }

    /**
     * Updates {@link JTable#setRowHeight(int)} with the height of a single line.
     * There's no Swing property for {@code rowHeight}, so each table should be configured
     * individually
     * @param component input table to configure
     * @return input component (for fluent APIs)
     */
    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public static JTable singleLineRowHeight(JTable component) {
        return STYLE.withDynamic(component,
                c -> {
                    int rowHeight = UIManager.getInt(JMeterUIDefaults.TABLE_ROW_HEIGHT);
                    // rowHeight is 0 when JMeterUIDefaults was not installed
                    if (rowHeight != 0) {
                        c.setRowHeight(rowHeight);
                    }
                });
    }

    /**
     * Configures the label to look like {@code warning}.
     * @param component input label
     * @return input label (for fluent APIs)
     */
    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public static JLabel error(JLabel component) {
        return STYLE.withDynamic(component, c -> {
            c.setFont(UIManager.getFont(JMeterUIDefaults.LABEL_ERROR_FONT));
            c.setForeground(UIManager.getColor(JMeterUIDefaults.LABEL_ERROR_FOREGROUND));
        });
    }

    /**
     * Configures the label to look like {@code error}.
     * @param component input label
     * @return input label (for fluent APIs)
     */
    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public static JLabel warning(JLabel component) {
        return STYLE.withDynamic(component, c -> {
            c.setFont(UIManager.getFont(JMeterUIDefaults.LABEL_WARNING_FONT));
            c.setForeground(UIManager.getColor(JMeterUIDefaults.LABEL_WARNING_FOREGROUND));
        });
    }

    /**
     * Attaches a configuration action that is executed when Look and Feel changes.
     * <p>Note: the action is executed when {@code withDynamic} is called, and the action is
     * executed even if the new and the old LaFs are the same.</p>
     * @param component component to update
     * @param onUpdateUi action to run (immediately and when look and feel changes)
     * @param <T> type of the component
     * @return input component (e.g. for fluent APIs)
     */
    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public static <T extends JComponent> T withDynamic(T component, Consumer<T> onUpdateUi) {
        return STYLE.withDynamic(component, onUpdateUi);
    }

    public static void updateUi(JComponent c) {
        STYLE.updateComponentTreeUI(c);
    }
}
