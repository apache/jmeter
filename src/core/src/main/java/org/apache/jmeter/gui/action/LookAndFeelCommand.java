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

package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.util.JMeterMenuBar;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JFactory;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import com.github.weisj.darklaf.theme.Theme;

/**
 * Implements the Look and Feel menu item.
 */
public class LookAndFeelCommand extends AbstractAction {
    private static final String JMETER_LAF = "jmeter.laf"; // $NON-NLS-1$

    private static final Map<String, MenuItem> items = new LinkedHashMap<>();

    private static final Preferences PREFS = Preferences.userNodeForPackage(LookAndFeelCommand.class);
    // Note: Windows user preferences are stored relative to: HKEY_CURRENT_USER\Software\JavaSoft\Prefs

    /** Prefix for the user preference key */
    private static final String USER_PREFS_KEY = "laf.command"; //$NON-NLS-1$

    public static class MenuItem {
        final String title;
        final String command;
        final String lafClassName;
        final Theme lafTheme;

        private MenuItem(String title, String command, String lafClassName, Theme lafTheme) {
            this.title = title;
            this.command = command;
            this.lafClassName = lafClassName;
            this.lafTheme = lafTheme;
        }

        public String getTitle() {
            return title;
        }

        public String getCommand() {
            return command;
        }

        private static MenuItem of(String title, String lafClass) {
            return new MenuItem(title, ActionNames.LAF_PREFIX + lafClass, lafClass, null);
        }

        private static MenuItem ofDarklafTheme(Theme theme) {
            return new MenuItem("Darklaf - " + theme.getName(),
                    JMeterMenuBar.DARKLAF_LAF_CLASS + ":" + theme.getThemeClass().getName(),
                    JMeterMenuBar.DARKLAF_LAF_CLASS,
                    theme);
        }
    }

    static {
        if (System.getProperty("darklaf.decorations") == null) {
            System.setProperty("darklaf.decorations", "false");
        } else if (Boolean.getBoolean("darklaf.allowNativeCode")) {
            // darklaf.allowNativeCode=true is required for darklaf.decorations=true to work.
            System.setProperty("darklaf.decorations", "true");
        }
        if (System.getProperty("darklaf.allowNativeCode") == null) {
            System.setProperty("darklaf.allowNativeCode", "false");
        }
        if (System.getProperty("darklaf.unifiedMenuBar") == null) {
            System.setProperty("darklaf.unifiedMenuBar", "true");
        }
        if (System.getProperty("darklaf.treeRowPopup") == null) {
            System.setProperty("darklaf.treeRowPopup", "false");
        }
        UIManager.installLookAndFeel(JMeterMenuBar.DARCULA_LAF, JMeterMenuBar.DARCULA_LAF_CLASS);

        List<MenuItem> items = new ArrayList<>();
        for (UIManager.LookAndFeelInfo laf : JMeterMenuBar.getAllLAFs()) {
            if (!laf.getClassName().equals(JMeterMenuBar.DARCULA_LAF_CLASS)) {
                items.add(MenuItem.of(laf.getName(), laf.getClassName()));
            } else {
                for (Theme theme : LafManager.getRegisteredThemes()) {
                    items.add(MenuItem.ofDarklafTheme(theme));
                }
            }
        }
        items.sort(Comparator.comparing(MenuItem::getTitle));
        for (MenuItem item : items) {
            LookAndFeelCommand.items.put(item.command, item);
        }
    }

    public static Collection<MenuItem> getMenuItems() {
        return Collections.unmodifiableCollection(items.values());
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
     * @deprecated see #getPreferredLafCommand
     * @see #getPreferredLafCommand
     */
    @Deprecated
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

    /**
     * Returns a command that would activate the preferred LaF.
     * @return command that would activate the preferred LaF
     */
    public static String getPreferredLafCommand() {
        String laf = PREFS.get(USER_PREFS_KEY, null);
        if (laf != null) {
            return laf;
        }

        String jMeterLaf = getJMeterLaf();
        if (jMeterLaf.equals(JMeterMenuBar.DARCULA_LAF_CLASS)) {
            // Convert old Darcula to new Darklaf-Darcula LaF
            return MenuItem.ofDarklafTheme(new DarculaTheme()).command;
        }

        return MenuItem.of("default", jMeterLaf).command; // $NON-NLS-1$
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

    public static boolean isDarklafTheme() {
        return "Darklaf".equalsIgnoreCase(UIManager.getLookAndFeel().getID()); // $NON-NLS-1$
    }

    public static boolean isDark() {
        return isDarklafTheme() && Theme.isDark(LafManager.getTheme());
    }

    public static void activateLookAndFeel(String command) {
        MenuItem item = items.get(command);
        String className = item.lafClassName;
        if (item.lafTheme != null) {
            LafManager.setTheme(item.lafTheme);
        }
        GuiPackage instance = GuiPackage.getInstance();
        if (instance != null) {
            instance.updateUIForHiddenComponents();
        }
        JFactory.refreshUI(className);
        PREFS.put(USER_PREFS_KEY, item.command);
    }

    @Override
    public void doAction(ActionEvent ev) {
        try {
            activateLookAndFeel(ev.getActionCommand());
            int chosenOption = JOptionPane.showConfirmDialog(GuiPackage.getInstance().getMainFrame(), JMeterUtils
                    .getResString("laf_quit_after_change"), // $NON-NLS-1$
                    JMeterUtils.getResString("exit"), // $NON-NLS-1$
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (chosenOption == JOptionPane.YES_OPTION) {
                ActionRouter.getInstance().doActionNow(new ActionEvent(ev.getSource(), ev.getID(), ActionNames.RESTART));
            }
        } catch (IllegalArgumentException e) {
            JMeterUtils.reportErrorToUser(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> getActionNames() {
        return Collections.unmodifiableSet(items.keySet());
    }
}
