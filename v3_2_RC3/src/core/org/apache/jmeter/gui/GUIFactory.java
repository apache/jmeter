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

package org.apache.jmeter.gui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.jmeter.testbeans.gui.TestBeanGUI;

/**
 * Provides a way to register and retrieve GUI classes and icons.
 *
 */
public final class GUIFactory {
    /** A Map from String to JMeterGUIComponent of registered GUI classes. */
    private static final Map<String, JMeterGUIComponent> GUI_MAP = new HashMap<>();

    /** A Map from String to ImageIcon of registered icons. */
    private static final Map<String, ImageIcon> ICON_MAP = new HashMap<>();

    /** A Map from String to ImageIcon of registered icons. */
    private static final Map<String, ImageIcon> DISABLED_ICON_MAP = new HashMap<>();

    /**
     * Prevent instantiation since this is a static utility class.
     */
    private GUIFactory() {
    }

    /**
     * Get an icon which has previously been registered for this class object.
     *
     * @param elementClass
     *            the class object which we want to get an icon for
     *
     * @return the associated icon, or null if this class or its superclass has
     *         not been registered
     */
    public static ImageIcon getIcon(Class<?> elementClass) {
        return getIcon(elementClass, true);

    }

    /**
     * Get icon/disabledicon which has previously been registered for this class
     * object.
     *
     * @param elementClass
     *            the class object which we want to get an icon for
     * @param enabled -
     *            is icon enabled
     *
     * @return the associated icon, or null if this class or its superclass has
     *         not been registered
     */
    public static ImageIcon getIcon(Class<?> elementClass, boolean enabled) {
        String key = elementClass.getName();
        ImageIcon icon = enabled ? ICON_MAP.get(key) : DISABLED_ICON_MAP.get(key);

        if (icon != null) {
            return icon;
        }

        if (elementClass.getSuperclass() != null) {
            return getIcon(elementClass.getSuperclass(), enabled);
        }

        return null;
    }

    /**
     * Get a component instance which has previously been registered for this
     * class object.
     *
     * @param elementClass
     *            the class object which we want to get an instance of
     *
     * @return an instance of the class, or null if this class or its superclass
     *         has not been registered
     */
    public static JComponent getGUI(Class<?> elementClass) {
        // TODO: This method doesn't appear to be used.
        String key = elementClass.getName();
        JComponent gui = (JComponent) GUI_MAP.get(key);

        if (gui != null) {
            return gui;
        }

        if (elementClass.getSuperclass() != null) {
            return getGUI(elementClass.getSuperclass());
        }

        return null;
    }

    /**
     * Register an icon so that it can later be retrieved via
     * {@link #getIcon(Class)}. The key should match the fully-qualified class
     * name for the class used as the parameter when retrieving the icon.
     *
     * @param key
     *            the name which can be used to retrieve this icon later
     * @param icon
     *            the icon to store
     */
    public static void registerIcon(String key, ImageIcon icon) {
        ICON_MAP.put(key, icon);
    }

    /**
     * Register an icon so that it can later be retrieved via
     * {@link #getIcon(Class)}. The key should match the fully-qualified class
     * name for the class used as the parameter when retrieving the icon.
     *
     * @param key
     *            the name which can be used to retrieve this icon later
     * @param icon
     *            the icon to store
     */
    public static void registerDisabledIcon(String key, ImageIcon icon) {
        DISABLED_ICON_MAP.put(key, icon);
    }

    /**
     * Register a GUI class so that it can later be retrieved via
     * {@link #getGUI(Class)}. The key should match the fully-qualified class
     * name for the class used as the parameter when retrieving the GUI.
     *
     * @param key
     *            the name which can be used to retrieve this GUI later
     * @param guiClass
     *            the class object for the GUI component
     * @param testClass
     *            the class of the objects edited by this GUI
     *
     * @throws InstantiationException
     *             if an instance of the GUI class can not be instantiated
     * @throws IllegalAccessException
     *             if access rights do not permit an instance of the GUI class
     *             to be created
     */
    public static void registerGUI(String key, Class<?> guiClass, Class<?> testClass) throws InstantiationException,
            IllegalAccessException {
        // TODO: This method doesn't appear to be used.
        JMeterGUIComponent gui;

        if (guiClass == TestBeanGUI.class) {
            gui = new TestBeanGUI(testClass);
        } else {
            gui = (JMeterGUIComponent) guiClass.newInstance();
        }
        GUI_MAP.put(key, gui);
    }
}
