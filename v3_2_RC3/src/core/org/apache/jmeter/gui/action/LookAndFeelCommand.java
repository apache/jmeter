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

package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.UIManager;

import org.apache.jmeter.gui.util.JMeterMenuBar;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the Look and Feel menu item.
 */
public class LookAndFeelCommand extends AbstractAction {

    private static final Logger log = LoggerFactory.getLogger(LookAndFeelCommand.class);

    private static final String JMETER_LAF = "jmeter.laf"; // $NON-NLS-1$

    private static final Set<String> commands = new HashSet<>();

    private static final Preferences PREFS = Preferences.userNodeForPackage(LookAndFeelCommand.class);
    // Note: Windows user preferences are stored relative to: HKEY_CURRENT_USER\Software\JavaSoft\Prefs

    /** Prefix for the user preference key */
    private static final String USER_PREFS_KEY = "laf"; //$NON-NLS-1$

    static {
        UIManager.LookAndFeelInfo[] lfs = JMeterMenuBar.getAllLAFs();
        for (UIManager.LookAndFeelInfo lf : lfs) {
            commands.add(ActionNames.LAF_PREFIX + lf.getClassName());
        }
        if (log.isInfoEnabled()) {
            final String jMeterLaf = getJMeterLaf();
            List<String> names = new ArrayList<>();
            for(UIManager.LookAndFeelInfo laf : lfs) {
                if (laf.getClassName().equals(jMeterLaf)) {
                    names.add(laf.getName());
                }
            }
            if (!names.isEmpty()) {
                log.info("Using look and feel: {} {}", jMeterLaf, names);
            } else {
                log.info("Using look and feel: {}", jMeterLaf);
            }
        }
    }

    /**
     * Get LookAndFeel classname from the following properties:
     * <ul>
     * <li>User preferences key: "laf"</li>
     * <li>jmeter.laf.&lt;os.name&gt; - lowercased; spaces replaced by '_'</li>
     * <li>jmeter.laf.&lt;os.family&gt; - lowercased.</li>
     * <li>jmeter.laf</li>
     * <li>UIManager.getCrossPlatformLookAndFeelClassName()</li>
     * </ul>
     * @return LAF classname
     */
    public static String getJMeterLaf(){
        String laf = PREFS.get(USER_PREFS_KEY, null);
        if (laf != null) {
            return checkLafName(laf);            
        }

        String osName = System.getProperty("os.name") // $NON-NLS-1$
                        .toLowerCase(Locale.ENGLISH);
        // Spaces are not allowed in property names read from files
        laf = JMeterUtils.getProperty(JMETER_LAF+"."+osName.replace(' ', '_'));
        if (laf != null) {
            return checkLafName(laf);
        }
        String[] osFamily = osName.split("\\s"); // e.g. windows xp => windows
        laf = JMeterUtils.getProperty(JMETER_LAF+"."+osFamily[0]);
        if (laf != null) {
            return checkLafName(laf);
        }
        laf = JMeterUtils.getProperty(JMETER_LAF);
        if (laf != null) {
            return checkLafName(laf);
        }
        return UIManager.getCrossPlatformLookAndFeelClassName();
    }

    // Check if LAF is a built-in one
    private static String checkLafName(String laf){
        if (JMeterMenuBar.SYSTEM_LAF.equalsIgnoreCase(laf)){
            return UIManager.getSystemLookAndFeelClassName();
        }
        if (JMeterMenuBar.CROSS_PLATFORM_LAF.equalsIgnoreCase(laf)){
            return UIManager.getCrossPlatformLookAndFeelClassName();
        }
        return laf;
    }

    public LookAndFeelCommand() {
    }

    @Override
    public void doAction(ActionEvent ev) {
        try {
            String className = ev.getActionCommand().substring(ActionNames.LAF_PREFIX.length()).replace('/', '.');
            UIManager.setLookAndFeel(className);
            JMeterUtils.refreshUI();
            PREFS.put(USER_PREFS_KEY, className);
        } catch (javax.swing.UnsupportedLookAndFeelException | InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            JMeterUtils.reportErrorToUser("Look and Feel unavailable:" + e.toString());
        }
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
