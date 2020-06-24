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

import java.awt.Component;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.UndoHistory;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.LocaleChangeEvent;
import org.apache.jmeter.util.LocaleChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.weisj.darklaf.icons.ThemedSVGIcon;
import com.github.weisj.darklaf.ui.button.DarkButtonUI;

/**
 * The JMeter main toolbar class
 *
 */
public class JMeterToolBar extends JToolBar implements LocaleChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(JMeterToolBar.class);

    private static final String TOOLBAR_ENTRY_SEP = ",";  //$NON-NLS-1$

    private static final String TOOLBAR_PROP_NAME = "toolbar"; //$NON-NLS-1$

    // protected fields: JMeterToolBar class can be use to create another toolbar (plugin, etc.)
    protected static final String DEFAULT_TOOLBAR_PROPERTY_FILE = "org/apache/jmeter/images/toolbar/icons-toolbar.properties"; //$NON-NLS-1$

    protected static final String USER_DEFINED_TOOLBAR_PROPERTY_FILE = "jmeter.toolbar.icons"; //$NON-NLS-1$

    public static final String TOOLBAR_ICON_SIZE = "jmeter.toolbar.icons.size"; //$NON-NLS-1$

    public static final String DEFAULT_TOOLBAR_ICON_SIZE = "22x22"; //$NON-NLS-1$

    protected static final String TOOLBAR_LIST = "jmeter.toolbar";

    /**
     * Create the default JMeter toolbar
     *
     * @param visible
     *            Flag whether toolbar should be visible
     * @return the newly created {@link JMeterToolBar}
     */
    public static JMeterToolBar createToolbar(boolean visible) {
        JMeterToolBar toolBar = new JMeterToolBar();
        toolBar.setFloatable(false);
        toolBar.setVisible(visible);

        setupToolbarContent(toolBar);
        JMeterUtils.addLocaleChangeListener(toolBar);
        // implicit return empty toolbar if icons == null
        return toolBar;
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        super.addImpl(comp, constraints, index);
        if (comp instanceof JButton) {
            // Ensure buttons added to the toolbar have the same style.
            JButton b = (JButton) comp;
            b.setFocusable(false);
            if (b.isBorderPainted() && (b.getText() == null || b.getText().isEmpty())) {
                b.setRolloverEnabled(true);
                b.putClientProperty(DarkButtonUI.KEY_VARIANT, DarkButtonUI.VARIANT_BORDERLESS);
                b.putClientProperty(DarkButtonUI.KEY_THIN, true);
                b.putClientProperty(DarkButtonUI.KEY_SQUARE, true);
            }
        }
    }

    /**
     * Setup toolbar content
     * @param toolBar {@link JMeterToolBar}
     */
    private static void setupToolbarContent(JMeterToolBar toolBar) {
        List<IconToolbarBean> icons = getIconMappings();
        if (icons != null) {
            for (IconToolbarBean iconToolbarBean : icons) {
                if (iconToolbarBean == null) {
                    toolBar.addSeparator();
                } else {
                    try {
                        if(ActionNames.UNDO.equalsIgnoreCase(iconToolbarBean.getActionName())
                                        || ActionNames.REDO.equalsIgnoreCase(iconToolbarBean.getActionName())) {
                            if(UndoHistory.isEnabled()) {
                                toolBar.add(makeButtonItemRes(iconToolbarBean));
                            }
                        } else {
                            toolBar.add(makeButtonItemRes(iconToolbarBean));
                        }
                    } catch (Exception e) {
                        if (log.isWarnEnabled()) {
                            log.warn("Exception while adding button item to toolbar. {}", e.getMessage());
                        }
                    }
                }
            }
            toolBar.initButtonsState();
        }
    }

    /**
     * Generate a button component from icon bean
     * @param iconBean contains I18N key, ActionNames, icon path, optional icon path pressed
     * @return a button for toolbar
     */
    private static JButton makeButtonItemRes(IconToolbarBean iconBean) throws Exception {
        JButton button = new JButton(loadIcon(iconBean, iconBean.getIconPath()));
        button.setToolTipText(JMeterUtils.getResString(iconBean.getI18nKey()));
        if (!iconBean.getIconPathPressed().equals(iconBean.getIconPath())) {
            button.setPressedIcon(loadIcon(iconBean, iconBean.getIconPathPressed()));
        }
        button.addActionListener(ActionRouter.getInstance());
        button.setActionCommand(iconBean.getActionNameResolve());
        return button;
    }

    private static Icon loadIcon(IconToolbarBean iconBean, String iconPath) throws URISyntaxException {
        final URL imageURL = JMeterUtils.class.getClassLoader().getResource(iconPath);
        if (imageURL == null) {
            throw new IllegalArgumentException("No icon for: " + iconBean.getActionName());
        }
        if (StringUtils.endsWithIgnoreCase(iconBean.getIconPath(), ".svg")) {
            return new ThemedSVGIcon(imageURL.toURI(), iconBean.getWidth(), iconBean.getHeight());
        }
        return new ImageIcon(imageURL);
    }

    /**
     * Parse icon set file.
     * @return List of icons/action definition
     */
    private static List<IconToolbarBean> getIconMappings() {
        // Get the standard toolbar properties
        Properties defaultProps = JMeterUtils.loadProperties(DEFAULT_TOOLBAR_PROPERTY_FILE);
        if (defaultProps == null) {
            JOptionPane.showMessageDialog(null,
                    JMeterUtils.getResString("toolbar_icon_set_not_found"), // $NON-NLS-1$
                    JMeterUtils.getResString("toolbar_icon_set_not_found"), // $NON-NLS-1$
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Properties p;
        String userProp = JMeterUtils.getProperty(USER_DEFINED_TOOLBAR_PROPERTY_FILE);
        if (userProp != null){
            p = JMeterUtils.loadProperties(userProp, defaultProps);
        } else {
            p = defaultProps;
        }

        String order = JMeterUtils.getPropDefault(TOOLBAR_LIST, p.getProperty(TOOLBAR_PROP_NAME));

        if (order == null) {
            log.warn("Could not find toolbar definition list");
            JOptionPane.showMessageDialog(null,
                    JMeterUtils.getResString("toolbar_icon_set_not_found"), // $NON-NLS-1$
                    JMeterUtils.getResString("toolbar_icon_set_not_found"), // $NON-NLS-1$
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }

        String[] oList = order.split(TOOLBAR_ENTRY_SEP);

        String iconSize = JMeterUtils.getPropDefault(TOOLBAR_ICON_SIZE, DEFAULT_TOOLBAR_ICON_SIZE);

        List<IconToolbarBean> listIcons = new ArrayList<>();
        for (String key : oList) {
            log.debug("Toolbar icon key: {}", key); //$NON-NLS-1$
            String trimmed = key.trim();
            if (trimmed.equals("|")) { //$NON-NLS-1$
                listIcons.add(null);
            } else {
                String property = p.getProperty(trimmed);
                if (property == null) {
                    log.warn("No definition for toolbar entry: {}", key);
                } else {
                    try {
                        IconToolbarBean itb = new IconToolbarBean(property, iconSize);
                        listIcons.add(itb);
                    } catch (IllegalArgumentException e) {
                        // already reported by IconToolbarBean
                    }
                }
            }
        }
        return listIcons;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void localeChanged(LocaleChangeEvent event) {
        Map<String, Boolean> currentButtonStates = getCurrentButtonsStates();
        this.removeAll();
        setupToolbarContent(this);
        updateButtons(currentButtonStates);
    }

    /**
     *
     * @return Current state (enabled/disabled) of Toolbar button
     */
    private Map<String, Boolean> getCurrentButtonsStates() {
        Component[] components = getComponents();
        Map<String, Boolean> buttonStates = new HashMap<>(components.length);
        for (Component component : components) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                buttonStates.put(button.getActionCommand(), button.isEnabled());
            }
        }
        return buttonStates;
    }

    /**
     * Init the state of buttons
     */
    public void initButtonsState() {
        Map<String, Boolean> buttonStates = new HashMap<>();
        buttonStates.put(ActionNames.ACTION_START, Boolean.TRUE);
        buttonStates.put(ActionNames.ACTION_START_NO_TIMERS, Boolean.TRUE);
        buttonStates.put(ActionNames.ACTION_STOP, Boolean.FALSE);
        buttonStates.put(ActionNames.ACTION_SHUTDOWN, Boolean.FALSE);
        buttonStates.put(ActionNames.UNDO, Boolean.FALSE);
        buttonStates.put(ActionNames.REDO, Boolean.FALSE);
        buttonStates.put(ActionNames.REMOTE_START_ALL, Boolean.TRUE);
        buttonStates.put(ActionNames.REMOTE_STOP_ALL, Boolean.FALSE);
        buttonStates.put(ActionNames.REMOTE_SHUT_ALL, Boolean.FALSE);
        updateButtons(buttonStates);
    }

    /**
     * Change state of buttons on local test
     *
     * @param started
     *            Flag whether local test is started
     */
    public void setLocalTestStarted(boolean started) {
        Map<String, Boolean> buttonStates = new HashMap<>(3);
        buttonStates.put(ActionNames.ACTION_START, !started);
        buttonStates.put(ActionNames.ACTION_START_NO_TIMERS, !started);
        buttonStates.put(ActionNames.ACTION_STOP, started);
        buttonStates.put(ActionNames.ACTION_SHUTDOWN, started);
        updateButtons(buttonStates);
    }

    /**
     * Change state of buttons on remote test
     *
     * @param started
     *            Flag whether the test is started
     */
    public void setRemoteTestStarted(boolean started) {
        Map<String, Boolean> buttonStates = new HashMap<>(3);
        buttonStates.put(ActionNames.REMOTE_START_ALL, !started);
        buttonStates.put(ActionNames.REMOTE_STOP_ALL, started);
        buttonStates.put(ActionNames.REMOTE_SHUT_ALL, started);
        updateButtons(buttonStates);
    }

    /**
     * Change state of buttons after undo or redo
     *
     * @param canUndo
     *            Flag whether the button corresponding to
     *            {@link ActionNames#UNDO} should be enabled
     * @param canRedo
     *            Flag whether the button corresponding to
     *            {@link ActionNames#REDO} should be enabled
     */
    public void updateUndoRedoIcons(boolean canUndo, boolean canRedo) {
        Map<String, Boolean> buttonStates = new HashMap<>(2);
        buttonStates.put(ActionNames.UNDO, canUndo);
        buttonStates.put(ActionNames.REDO, canRedo);
        updateButtons(buttonStates);
    }

    /**
     * Set buttons to a given state
     *
     * @param buttonStates
     *            {@link Map} of button names and their states
     */
    private void updateButtons(Map<String, Boolean> buttonStates) {
        // Swing APIs (e.g. Button.setEnabled) must be called on a Swing dispatch thread
        boolean synchronous = false;
        JMeterUtils.runSafe(synchronous, () -> {
            for (Component component : getComponents()) {
                if (component instanceof JButton) {
                    JButton button = (JButton) component;
                    Boolean enabled = buttonStates.get(button.getActionCommand());
                    if (enabled != null) {
                        button.setEnabled(enabled);
                    }
                }
            }
        });
    }
}
