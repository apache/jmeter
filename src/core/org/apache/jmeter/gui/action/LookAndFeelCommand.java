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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.jmeter.gui.GuiPackage;
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

    private static final Set<String> commands;

    private static final Preferences PREFS = Preferences.userNodeForPackage(LookAndFeelCommand.class);
    // Note: Windows user preferences are stored relative to: HKEY_CURRENT_USER\Software\JavaSoft\Prefs

    /** Prefix for the user preference key */
    private static final String USER_PREFS_KEY = "laf.class"; //$NON-NLS-1$

    static {
        log.info("Installing Darcula LAF");
        UIManager.installLookAndFeel(JMeterMenuBar.DARCULA_LAF, JMeterMenuBar.DARCULA_LAF_CLASS);
        UIManager.LookAndFeelInfo[] allLAFs = JMeterMenuBar.getAllLAFs();
        commands = Arrays.stream(allLAFs)
                .map(lf -> ActionNames.LAF_PREFIX + lf.getClassName())
                .collect(Collectors.toSet());
        if (log.isInfoEnabled()) {
            final String jMeterLaf = getJMeterLaf();
            List<String> names = Arrays.stream(allLAFs)
                    .filter(laf -> laf.getClassName().equals(JMeterMenuBar.DARCULA_LAF_CLASS))
                    .map(UIManager.LookAndFeelInfo::getName)
                    .collect(Collectors.toList());
            log.info("Using look and feel: {} {}", jMeterLaf, names);
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
        laf = JMeterUtils.getPropDefault(JMETER_LAF, JMeterMenuBar.DARCULA_LAF_CLASS);
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
        // NOOP
    }

    @Override
    public void doAction(ActionEvent ev) {
        try {
            String className = ev.getActionCommand().substring(ActionNames.LAF_PREFIX.length()).replace('/', '.');
            UIManager.setLookAndFeel(className);
            JMeterUtils.refreshUI();
            PREFS.put(USER_PREFS_KEY, className);
            int chosenOption = JOptionPane.showConfirmDialog(GuiPackage.getInstance().getMainFrame(), JMeterUtils
                    .getResString("laf_quit_after_change"), // $NON-NLS-1$
                    JMeterUtils.getResString("exit"), // $NON-NLS-1$
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (chosenOption == JOptionPane.YES_OPTION) {
                ActionRouter.getInstance().doActionNow(new ActionEvent(ev.getSource(), ev.getID(), ActionNames.RESTART));
            }
        } catch (UnsupportedLookAndFeelException
                | InstantiationException
                | ClassNotFoundException
                | IllegalAccessException e) {
            JMeterUtils.reportErrorToUser("Look and Feel unavailable:" + e.toString());
        }
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
