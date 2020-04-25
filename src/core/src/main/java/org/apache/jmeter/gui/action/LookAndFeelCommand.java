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
import java.lang.reflect.InvocationTargetException;
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

import javax.swing.UIManager;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.util.JMeterMenuBar;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import com.github.weisj.darklaf.theme.HighContrastDarkTheme;
import com.github.weisj.darklaf.theme.HighContrastLightTheme;
import com.github.weisj.darklaf.theme.IntelliJTheme;
import com.github.weisj.darklaf.theme.OneDarkTheme;
import com.github.weisj.darklaf.theme.SolarizedDarkTheme;
import com.github.weisj.darklaf.theme.SolarizedLightTheme;
import com.github.weisj.darklaf.theme.Theme;

/**
 * Implements the Look and Feel menu item.
 */
public class LookAndFeelCommand extends AbstractAction {

    private static final Logger log = LoggerFactory.getLogger(LookAndFeelCommand.class);

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
        final Class<? extends Theme> lafTheme;

        private MenuItem(String title, String command, String lafClassName, Class<? extends Theme> lafTheme) {
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
            return new MenuItem(title,ActionNames.LAF_PREFIX + lafClass, lafClass, null);
        }

        private static MenuItem ofDarklafTheme(Class<? extends Theme> lafTheme) {
            return new MenuItem("Darklaf - " + lafTheme.getSimpleName().replace("Theme", ""),
                    JMeterMenuBar.DARKLAF_LAF_CLASS + ":" + lafTheme.getName(),
                    JMeterMenuBar.DARKLAF_LAF_CLASS,
                    lafTheme);
        }
    }

    static {
        if (System.getProperty("darklaf.decorations") == null) {
            System.setProperty("darklaf.decorations", "false");
        }
        if (System.getProperty("darklaf.allowNativeCode") == null) {
            System.setProperty("darklaf.allowNativeCode", "false");
        }
        UIManager.installLookAndFeel(JMeterMenuBar.DARKLAF_LAF, JMeterMenuBar.DARKLAF_LAF_CLASS);

        List<MenuItem> items = new ArrayList<>();
        for (UIManager.LookAndFeelInfo laf : JMeterMenuBar.getAllLAFs()) {
            if (!laf.getClassName().equals(JMeterMenuBar.DARKLAF_LAF_CLASS)) {
                items.add(MenuItem.of(laf.getName(), laf.getClassName()));
                continue;
            }
            items.add(MenuItem.ofDarklafTheme(DarculaTheme.class));
            items.add(MenuItem.ofDarklafTheme(IntelliJTheme.class));
            items.add(MenuItem.ofDarklafTheme(OneDarkTheme.class));
            items.add(MenuItem.ofDarklafTheme(SolarizedDarkTheme.class));
            items.add(MenuItem.ofDarklafTheme(SolarizedLightTheme.class));
            items.add(MenuItem.ofDarklafTheme(HighContrastDarkTheme.class));
            items.add(MenuItem.ofDarklafTheme(HighContrastLightTheme.class));
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
            return MenuItem.ofDarklafTheme(DarculaTheme.class).command;
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

    public static boolean isDark() {
        String lookAndFeelID = UIManager.getLookAndFeel().getID();
        if (lookAndFeelID.equals("Darklaf")) { // $NON-NLS-1$
            Theme lafTheme = LafManager.getTheme();
            if (lafTheme == null) {
                return false;
            }
            String name = lafTheme.getName();
            return name.equals("darcula") || name.equals("solarized_dark"); // $NON-NLS-1$
        }
        return false;
    }

    public static void activateLookAndFeel(String command) {
        MenuItem item = items.get(command);
        String className = item.lafClassName;
        try {
            if (item.lafTheme != null) {
                LafManager.setTheme(item.lafTheme.getConstructor().newInstance());
            }
            GuiPackage instance = GuiPackage.getInstance();
            if (instance != null) {
                instance.updateUIForHiddenComponents();
            }
            JFactory.refreshUI(className);
            PREFS.put(USER_PREFS_KEY, item.command);
        } catch ( InstantiationException
                | NoSuchMethodException
                | IllegalAccessException e) {
            throw new IllegalArgumentException("Look and Feel unavailable:" + e.toString(), e);
        } catch (InvocationTargetException e) {
            Throwable c = e.getCause();
            throw new IllegalArgumentException("Look and Feel unavailable:" + c.toString(), c);
        }
    }

    @Override
    public void doAction(ActionEvent ev) {
        try {
            activateLookAndFeel(ev.getActionCommand());
        } catch (IllegalArgumentException e) {
            JMeterUtils.reportErrorToUser(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> getActionNames() {
        return Collections.unmodifiableSet(items.keySet());
    }
}
