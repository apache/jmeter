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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.gui.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterException;
import org.apache.log.Logger;

/**
 * The JMeter main toolbar class
 *
 */
public class JMeterToolBar {
    
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String TOOLBAR_ENTRY_SEP = ",";  //$NON-NLS-1$

    private static final String TOOLBAR_PROP_NAME = "toolbar"; //$NON-NLS-1$

    // protected fields: JMeterToolBar class can be use to create another toolbar (plugin, etc.)    
    protected static final String defaultIconProp = "org/apache/jmeter/images/toolbar/icons-toolbar.properties"; //$NON-NLS-1$
    
    protected static final String keyIconProp = "jmeter.toolbar.icons"; //$NON-NLS-1$
    
    /**
     * Create the default JMeter toolbar
     * @return the JMeter toolbar
     */
    public static JToolBar createToolbar(boolean visible) {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setVisible(visible);

        List<IconToolbarBean> icons = getIconMappings();
        if (icons != null) {
            for (IconToolbarBean iconToolbarBean : icons) {
                if (iconToolbarBean == null) {
                    toolBar.addSeparator();
                } else {
                    toolBar.add(makeButtonItemRes(iconToolbarBean));
                }
            }
        }
        // implicit return empty toolbar if icons == null
        return toolBar;
    }
    
    /**
     * Generate a button component from icon bean
     * @param iconBean contains I18N key, ActionNames, icon path, optional icon path pressed
     * @return a button for toolbar
     */
    private static JButton makeButtonItemRes(IconToolbarBean iconBean) {
        final URL imageURL = JMeterUtils.class.getClassLoader().getResource(iconBean.getIconPath());
        JButton button = new JButton(new ImageIcon(imageURL));
        button.setToolTipText(JMeterUtils.getResString(iconBean.getI18nKey()));
        final URL imageURLPressed = JMeterUtils.class.getClassLoader().getResource(iconBean.getIconPathPressed());
        button.setPressedIcon(new ImageIcon(imageURLPressed));
        button.addActionListener(ActionRouter.getInstance());
        button.setActionCommand(iconBean.getActionNameResolve());
        return button;
    }
    
    /**
     * Parse icon set file.
     * @return List of icons/action definition
     */
    private static List<IconToolbarBean> getIconMappings() {
        String iconProp = JMeterUtils.getPropDefault(keyIconProp, defaultIconProp); //$NON-NLS-1$
        Properties p = JMeterUtils.loadProperties(iconProp);
        if (p == null && !iconProp.equals(defaultIconProp)) {
            log.info(iconProp + " not found - using " + defaultIconProp);
            iconProp = defaultIconProp;
            p = JMeterUtils.loadProperties(defaultIconProp);
        }
        if (p == null) {
            JOptionPane.showMessageDialog(null, 
                    JMeterUtils.getResString("toolbar_icon_set_not_found"), // $NON-NLS-1$
                    JMeterUtils.getResString("toolbar_icon_set_not_found"), // $NON-NLS-1$
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        log.info("Loading toolbar icons properties from " + iconProp); //$NON-NLS-1$
        
        String order = p.getProperty(TOOLBAR_PROP_NAME);
        p.remove(TOOLBAR_PROP_NAME);
        String[] oList = order.split(TOOLBAR_ENTRY_SEP);
        
        List<IconToolbarBean> listIcons = new ArrayList<IconToolbarBean>();
        for (String key : oList) {
            log.debug("Toolbar icon key: " + key); //$NON-NLS-1$
            if (key.trim().equals("|")) { //$NON-NLS-1$
                listIcons.add(null);
            } else {
                try {
                    IconToolbarBean itb = new IconToolbarBean(p.getProperty(key));
                    listIcons.add(itb);
                } catch (JMeterException je) {
                    log.error("Toolbar icon loading error - key: " + key); //$NON-NLS-1$
                }
            }
        }
        return listIcons;
    }

}
