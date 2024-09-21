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
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import com.google.auto.service.AutoService;

/**
 * Handles the loading of recent files, and also the content and
 * visibility of menu items for loading the recent files
 */
@AutoService(Command.class)
public class LoadRecentProject extends Load {
    /** Prefix for the user preference key */
    private static final String USER_PREFS_KEY = "recent_file_"; //$NON-NLS-1$
    /** The number of menu items used for recent files */
    private static final int NUMBER_OF_MENU_ITEMS = 9;
    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.OPEN_RECENT);
    }

    private static final Preferences prefs = Preferences.userNodeForPackage(LoadRecentProject.class);
    // Note: Windows user preferences are stored relative to: HKEY_CURRENT_USER\Software\JavaSoft\Prefs

    public LoadRecentProject() {
        super();
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public void doActionAfterCheck(ActionEvent e) {
        // We must ask the user if it is ok to close current project
        if (!Close.performAction(e)) {
            return;
        }
        // Load the file for this recent file command
        loadProjectFile(e, getRecentFile(e), false);
    }

    /**
     * Get the recent file for the menu item
     */
    private static File getRecentFile(ActionEvent e) {
        JMenuItem menuItem = (JMenuItem)e.getSource();
        // Get the preference for the recent files
        return new File(getRecentFile(Integer.parseInt(menuItem.getName())));
    }

    /**
     * Get the menu items to add to the menu bar, to get recent file functionality
     *
     * @return a List of JMenuItem, representing recent files. JMenuItem may not be visible
     */
    public static List<JComponent> getRecentFileMenuItems() {
        List<JComponent> menuItems = new ArrayList<>();
        // Get the preference for the recent files
        for(int i = 0; i < NUMBER_OF_MENU_ITEMS; i++) {
            // Create the menu item
            JMenuItem recentFile = new JMenuItem();
            // Use the index as the name, used when processing the action
            recentFile.setName(Integer.toString(i));
            recentFile.addActionListener(ActionRouter.getInstance());
            recentFile.setActionCommand(ActionNames.OPEN_RECENT);
            // Set the KeyStroke to use
            int shortKey = getShortcutKey(i);
            if(shortKey >= 0) {
                recentFile.setMnemonic(shortKey);
            }
            // Add the menu item
            menuItems.add(recentFile);
        }
        // Update menu items to reflect recent files
        updateMenuItems(menuItems);

        return menuItems;
    }

    /**
     * Update the content and visibility of the menu items for recent files
     *
     * @param menuItems the JMenuItem to update
     * @param loadedFileName the file name of the project file that has just
     * been loaded
     */
    public static void updateRecentFileMenuItems(List<JComponent> menuItems, String loadedFileName) {
        // Do nothing, when no real file was given
        if (loadedFileName == null) {
            return;
        }
        // Get the preference for the recent files
        Deque<String> newRecentFiles = IntStream.range(0, NUMBER_OF_MENU_ITEMS)
                .mapToObj(LoadRecentProject::getRecentFile)
                .filter(Objects::nonNull)
                .filter(s -> !s.equals(loadedFileName))
                .collect(Collectors.toCollection(ArrayDeque::new));
        newRecentFiles.addFirst(loadedFileName);

        // Store the recent files
        int index = 0;
        for (String fileName : newRecentFiles) {
            setRecentFile(index, fileName);
            index++;
            if (index >= NUMBER_OF_MENU_ITEMS) {
                break;
            }
        }
        while (index < NUMBER_OF_MENU_ITEMS) {
            removeRecentFile(index);
            index++;
        }
        // Update menu items to reflect recent files
        updateMenuItems(menuItems);
    }

    /**
     * Set the content and visibility of menu items and menu separator,
     * based on the recent file stored user preferences.
     */
    private static void updateMenuItems(List<JComponent> menuItems) {
        // Update the menu items
        for (int i = 0; i < NUMBER_OF_MENU_ITEMS; i++) {
            // Get the menu item
            JMenuItem recentFile = (JMenuItem) menuItems.get(i);

            // Find and set the file for this recent file command
            String recentFilePath = getRecentFile(i);
            if (recentFilePath != null) {
                File file = new File(recentFilePath);
                String sb = String.valueOf(i + 1) + " " + //$NON-NLS-1$
                        getMenuItemDisplayName(file);
                // Index before file name
                recentFile.setText(sb);
                recentFile.setToolTipText(recentFilePath);
                recentFile.setEnabled(true);
                recentFile.setVisible(true);
            } else {
                recentFile.setEnabled(false);
                recentFile.setVisible(false);
            }
        }
    }

    /**
     * Get the name to display in the menu item, it will chop the file name
     * if it is too long to display in the menu bar
     */
    private static String getMenuItemDisplayName(File file) {
        // Limit the length of the menu text if needed
        final int maxLength = 40;
        String menuText = file.getName();
        if (menuText.length() > maxLength) {
            menuText = "..." + menuText.substring(menuText.length() - maxLength, menuText.length()); //$NON-NLS-1$
        }
        return menuText;
    }

    /**
     * Get the KeyEvent to use as shortcut key for menu item
     */
    private static int getShortcutKey(int index) {
        int shortKey = -1;
        switch(index+1) {
            case 1:
                shortKey = KeyEvent.VK_1;
                break;
            case 2:
                shortKey = KeyEvent.VK_2;
                break;
            case 3:
                shortKey = KeyEvent.VK_3;
                break;
            case 4:
                shortKey = KeyEvent.VK_4;
                break;
            case 5:
                shortKey = KeyEvent.VK_5;
                break;
            case 6:
                shortKey = KeyEvent.VK_6;
                break;
            case 7:
                shortKey = KeyEvent.VK_7;
                break;
            case 8:
                shortKey = KeyEvent.VK_8;
                break;
            case 9:
                shortKey = KeyEvent.VK_9;
                break;
            default:
                break;
        }
        return shortKey;
    }

    /**
     * Get the full path to the recent file where index 0 is the most recent
     * @param index the index of the recent file
     * @return full path to the recent file at <code>index</code>
     */
    public static String getRecentFile(int index) {
        return prefs.get(USER_PREFS_KEY + index, null);
    }

    /**
     * Set the full path to the recent file where index 0 is the most recent
     */
    private static void setRecentFile(int index, String fileName) {
        prefs.put(USER_PREFS_KEY + index, fileName);
    }

    private static void removeRecentFile(int index) {
        prefs.remove(USER_PREFS_KEY + index);
    }

    /**
     * @param fileLoadRecentFiles List of JMenuItem
     * @return true if at least on JMenuItem is visible
     */
    public static boolean hasVisibleMenuItem(List<? extends JComponent> fileLoadRecentFiles) {
        return fileLoadRecentFiles.stream()
                .anyMatch(JComponent::isVisible);
    }
}
