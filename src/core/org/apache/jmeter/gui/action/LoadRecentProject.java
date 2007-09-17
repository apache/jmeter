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
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

/**
 * Handles the loading of recent files, and also the content and
 * visibility of menu items for loading the recent files
 */
public class LoadRecentProject extends Load {
    /** Prefix for the user preference key */
    private static String USER_PREFS_KEY = "recent_file_"; //$NON-NLS-1$
    /** The number of menu items used for recent files */
    private static final int NUMBER_OF_MENU_ITEMS = 4;
    private static Set commands = new HashSet();
    static {
        commands.add(ActionNames.OPEN_RECENT);
    }

	public LoadRecentProject() {
		super();
	}

    public Set getActionNames() {
        return commands;
    }

	public void doAction(ActionEvent e) {
        // We must ask the user if it is ok to close current project
	    if (!Close.performAction(e)) return;
        // Load the file for this recent file command
	    loadProjectFile(e, getRecentFile(e), false);
	}
    
    /**
     * Get the recent file for the menu item
     */
    private File getRecentFile(ActionEvent e) {
        JMenuItem menuItem = (JMenuItem)e.getSource();
        // Get the preference for the recent files
        Preferences prefs = Preferences.userNodeForPackage(LoadRecentProject.class);
        return new File(getRecentFile(prefs, Integer.parseInt(menuItem.getName())));
    }

    /**
     * Get the menu items to add to the menu bar, to get recent file functionality
     * 
     * @return a List of JMenuItem and a JSeparator, representing recent files
     */
    public static List getRecentFileMenuItems() {
        LinkedList menuItems = new LinkedList();
        // Get the preference for the recent files
        Preferences prefs = Preferences.userNodeForPackage(LoadRecentProject.class);
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
                recentFile.setAccelerator(KeyStroke.getKeyStroke(shortKey, 0));
            }            
            // Add the menu item
            menuItems.add(recentFile);
        }
        // Add separator as the last item
        JSeparator separator = new JSeparator();
        separator.setVisible(false);
        menuItems.add(separator);
        
        // Update menu items to reflect recent files
        updateMenuItems(menuItems, prefs);
        
        return menuItems;
    }
    
    /**
     * Update the content and visibility of the menu items for recent files
     * 
     * @param menuItems the JMenuItem and JSeparator to update
     * @param loadedFileName the file name of the project file that has just
     * been loaded 
     */
    public static void updateRecentFileMenuItems(List menuItems, String loadedFileName) {
        // Get the preference for the recent files
        Preferences prefs = Preferences.userNodeForPackage(LoadRecentProject.class);
        LinkedList newRecentFiles = new LinkedList();
        // Check if the new file is already in the recent list
        boolean alreadyExists = false;
        for(int i = 0; i < NUMBER_OF_MENU_ITEMS; i++) {
            String recentFilePath = getRecentFile(prefs, i);
            if(!loadedFileName.equals(recentFilePath)) {
                newRecentFiles.add(recentFilePath);
            }
            else {
                alreadyExists = true;
            }
        }
        // Add the new file at the start of the list
        newRecentFiles.add(0, loadedFileName);
        // Remove the last item from the list if it was a brand new file
        if(!alreadyExists) {
            newRecentFiles.removeLast();
        }
        // Store the recent files
        for(int i = 0; i < NUMBER_OF_MENU_ITEMS; i++) {
            String fileName = (String)newRecentFiles.get(i);
            if(fileName != null) {
                setRecentFile(prefs, i, fileName);
            }
        }
        // Update menu items to reflect recent files
        updateMenuItems(menuItems, prefs);
    }

    /**
     * Set the content and visibility of menu items and menu separator,
     * based on the recent file stored user preferences.
     */
    private static void updateMenuItems(List menuItems, Preferences prefs) {
        // Assume no recent files
        boolean someRecentFiles = false;
        // Update the menu items
        for(int i = 0; i < NUMBER_OF_MENU_ITEMS; i++) {
            // Get the menu item
            JMenuItem recentFile = (JMenuItem)menuItems.get(i);
            
            // Find and set the file for this recent file command
            String recentFilePath = getRecentFile(prefs, i);
            if(recentFilePath != null) {
                File file = new File(recentFilePath);
                recentFile.setText(getMenuItemDisplayName(file));
                recentFile.setToolTipText(recentFilePath);
                recentFile.setEnabled(true);
                recentFile.setVisible(true);
                // At least one recent file menu item is visible
                someRecentFiles = true;
            }
            else {
                recentFile.setEnabled(false);
                recentFile.setVisible(false);
            }
        }
        // If there are some recent files, we must make the separator visisble
        // The separator is the last item in the list
        JSeparator separator = (JSeparator)menuItems.get(menuItems.size() - 1);
        separator.setVisible(someRecentFiles);
    }

    /**
     * Get the name to display in the menu item, it will chop the file name
     * if it is too long to display in the menu bar
     */
    private static String getMenuItemDisplayName(File file) {
        // Limit the length of the menu text if needed
        final int maxLength = 40;
        String menuText = file.getName();
        if(menuText.length() > maxLength) {
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
     */
    private static String getRecentFile(Preferences prefs, int index) {
        return prefs.get(USER_PREFS_KEY + index, null);        
    }

    /**
     * Set the full path to the recent file where index 0 is the most recent
     */
    private static void setRecentFile(Preferences prefs, int index, String fileName) {
        prefs.put(USER_PREFS_KEY + index, fileName);
    }
}
