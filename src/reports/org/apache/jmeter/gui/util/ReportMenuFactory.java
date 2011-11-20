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

package org.apache.jmeter.gui.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;

import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.KeyStrokes;
import org.apache.jmeter.report.gui.action.ReportActionRouter;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Printable;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

public final class ReportMenuFactory {
    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String TIMERS = "menu_timer";

    public static final String CONTROLLERS = "menu_logic_controller";

    public static final String CONFIG_ELEMENTS = "menu_config_element";

    public static final String POST_PROCESSORS = "menu_post_processors";

    public static final String PRE_PROCESSORS = "menu_pre_processors";

    public static final String NON_TEST_ELEMENTS = "menu_non_test_elements";

    public static final String LISTENERS = "menu_listener";

    public static final String REPORT_PAGE = "menu_report_page";

    public static final String TABLES = "menu_tables";

    private static final Map<String, List<MenuInfo>> menuMap = new HashMap<String, List<MenuInfo>>();

    private static final Set<String> elementsToSkip = new HashSet<String>();

    // MENU_ADD_xxx - controls which items are in the ADD menu
    // MENU_PARENT_xxx - controls which items are in the Insert Parent menu
    private static final String[] MENU_ADD_CONTROLLER = new String[] { ReportMenuFactory.CONTROLLERS,
            ReportMenuFactory.CONFIG_ELEMENTS, ReportMenuFactory.TIMERS, ReportMenuFactory.LISTENERS,
            ReportMenuFactory.PRE_PROCESSORS, ReportMenuFactory.POST_PROCESSORS };

    private static final String[] MENU_PARENT_CONTROLLER = new String[] { ReportMenuFactory.CONTROLLERS };

    private static List<MenuInfo> controllers, configElements, listeners, nonTestElements,
            postProcessors, preProcessors, reportPage, tables;

    static {
        try {
            String[] classesToSkip = JOrphanUtils.split(JMeterUtils.getPropDefault("not_in_menu", ""), ",");
            for (int i = 0; i < classesToSkip.length; i++) {
                elementsToSkip.add(classesToSkip[i].trim());
            }

            initializeMenus();
        } catch (Throwable e) {
            log.error("", e);
        }
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private ReportMenuFactory() {
    }

    public static void addEditMenu(JPopupMenu menu, boolean removable) {
        addSeparator(menu);
        if (removable) {
            menu.add(makeMenuItem(JMeterUtils.getResString("remove"), "Remove", "remove", KeyStrokes.REMOVE));
        }
        menu.add(makeMenuItem(JMeterUtils.getResString("cut"), "Cut", "Cut", KeyStrokes.CUT));
        menu.add(makeMenuItem(JMeterUtils.getResString("copy"), "Copy", "Copy", KeyStrokes.COPY));
        menu.add(makeMenuItem(JMeterUtils.getResString("paste"), "Paste", "Paste", KeyStrokes.PASTE));
        menu.add(makeMenuItem(JMeterUtils.getResString("paste_insert"), "Paste Insert", "Paste Insert"));
    }

    public static void addFileMenu(JPopupMenu menu) {
        addSeparator(menu);
        menu.add(makeMenuItem(JMeterUtils.getResString("open"), "Open", "open"));
        menu.add(makeMenuItem(JMeterUtils.getResString("save_as"), "Save As", "save_as"));
        JMenuItem savePicture = makeMenuItem(JMeterUtils.getResString("save_as_image"), "Save Image", "save_graphics",
                KeyStrokes.SAVE_GRAPHICS);
        menu.add(savePicture);
        if (!(ReportGuiPackage.getInstance().getCurrentGui() instanceof Printable)) {
            savePicture.setEnabled(false);
        }
        JMenuItem disabled = makeMenuItem(JMeterUtils.getResString("disable"), "Disable", "disable");
        JMenuItem enabled = makeMenuItem(JMeterUtils.getResString("enable"), "Enable", "enable");
        boolean isEnabled = ReportGuiPackage.getInstance().getTreeListener().getCurrentNode().isEnabled();
        if (isEnabled) {
            disabled.setEnabled(true);
            enabled.setEnabled(false);
        } else {
            disabled.setEnabled(false);
            enabled.setEnabled(true);
        }
        menu.add(enabled);
        menu.add(disabled);
        addSeparator(menu);
        menu.add(makeMenuItem(JMeterUtils.getResString("help"), "Help", "help"));
    }

    public static JMenu makeMenus(String[] categories, String label, String actionCommand) {
        JMenu addMenu = new JMenu(label);
        for (int i = 0; i < categories.length; i++) {
            addMenu.add(makeMenu(categories[i], actionCommand));
        }
        return addMenu;
    }

    public static JPopupMenu getDefaultControllerMenu() {
        JPopupMenu pop = new JPopupMenu();
        pop.add(MenuFactory.makeMenus(MENU_ADD_CONTROLLER,
                JMeterUtils.getResString("add"),// $NON-NLS-1$
                ActionNames.ADD));
        pop.add(makeMenus(MENU_PARENT_CONTROLLER,
                JMeterUtils.getResString("insert_parent"),// $NON-NLS-1$
                ActionNames.ADD_PARENT));
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    public static JPopupMenu getDefaultConfigElementMenu() {
        JPopupMenu pop = new JPopupMenu();
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    public static JPopupMenu getDefaultVisualizerMenu() {
        JPopupMenu pop = new JPopupMenu();
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    public static JPopupMenu getDefaultTimerMenu() {
        JPopupMenu pop = new JPopupMenu();
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    public static JPopupMenu getDefaultAssertionMenu() {
        JPopupMenu pop = new JPopupMenu();
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    public static JPopupMenu getDefaultExtractorMenu() {
        JPopupMenu pop = new JPopupMenu();
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    public static JMenu makeMenu(String category, String actionCommand) {
        return makeMenu(menuMap.get(category), actionCommand, JMeterUtils.getResString(category));
    }

    public static JMenu makeMenu(Collection<MenuInfo> menuInfo, String actionCommand, String menuName) {
        Iterator<MenuInfo> iter = menuInfo.iterator();
        JMenu menu = new JMenu(menuName);
        while (iter.hasNext()) {
            MenuInfo info = iter.next();
            menu.add(makeMenuItem(info.getLabel(), info.getClassName(), actionCommand));
        }
        return menu;
    }

    public static void setEnabled(JMenu menu) {
        if (menu.getSubElements().length == 0) {
            menu.setEnabled(false);
        }
    }

    public static JMenuItem makeMenuItem(String label, String name, String actionCommand) {
        JMenuItem newMenuChoice = new JMenuItem(label);
        newMenuChoice.setName(name);
        newMenuChoice.addActionListener(ReportActionRouter.getInstance());
        if (actionCommand != null) {
            newMenuChoice.setActionCommand(actionCommand);
        }

        return newMenuChoice;
    }

    public static JMenuItem makeMenuItem(String label, String name, String actionCommand, KeyStroke accel) {
        JMenuItem item = makeMenuItem(label, name, actionCommand);
        item.setAccelerator(accel);
        return item;
    }

    private static void initializeMenus() {
        try {
            List<String> guiClasses = ClassFinder.findClassesThatExtend(JMeterUtils.getSearchPaths(), new Class[] {
                    JMeterGUIComponent.class, TestBean.class });
            controllers = new LinkedList<MenuInfo>();
            configElements = new LinkedList<MenuInfo>();
            listeners = new LinkedList<MenuInfo>();
            postProcessors = new LinkedList<MenuInfo>();
            preProcessors = new LinkedList<MenuInfo>();
            tables = new LinkedList<MenuInfo>();
            reportPage = new LinkedList<MenuInfo>();
            nonTestElements = new LinkedList<MenuInfo>();
            menuMap.put(CONFIG_ELEMENTS, configElements);
            menuMap.put(CONTROLLERS, controllers);
            menuMap.put(LISTENERS, listeners);
            menuMap.put(NON_TEST_ELEMENTS, nonTestElements);
            menuMap.put(POST_PROCESSORS, postProcessors);
            menuMap.put(PRE_PROCESSORS, preProcessors);
            menuMap.put(REPORT_PAGE, reportPage);
            menuMap.put(TABLES, tables);
            Collections.sort(guiClasses);
            Iterator<String> iter = guiClasses.iterator();
            while (iter.hasNext()) {
                String name = iter.next();

                /*
                 * JMeterTreeNode and TestBeanGUI are special GUI classes, and
                 * aren't intended to be added to menus
                 *
                 * TODO: find a better way of checking this
                 */
                if (name.endsWith("JMeterTreeNode") || name.endsWith("TestBeanGUI")) {
                    continue;// Don't try to instantiate these
                }

                JMeterGUIComponent item;
                try {
                    Class<?> c = Class.forName(name);
                    if (TestBean.class.isAssignableFrom(c)) {
                        item = new TestBeanGUI(c);
                    } else {
                        item = (JMeterGUIComponent) c.newInstance();
                    }
                } catch (NoClassDefFoundError e) {
                    log.warn("Missing jar? Could not create " + name + ". " + e);
                    continue;
                } catch (Throwable e) {
                    log.warn("Could not instantiate " + name, e);
                    continue;
                }
                if (elementsToSkip.contains(name) || elementsToSkip.contains(item.getStaticLabel())) {
                    log.info("Skipping " + name);
                    continue;
                } else {
                    elementsToSkip.add(name);
                }
                Collection<String> categories = item.getMenuCategories();
                if (categories == null) {
                    log.debug(name + " participates in no menus.");
                    continue;
                }

                if (categories.contains(POST_PROCESSORS)) {
                    postProcessors.add(new MenuInfo(item.getStaticLabel(), name));
                }

                if (categories.contains(PRE_PROCESSORS)) {
                    preProcessors.add(new MenuInfo(item.getStaticLabel(), name));
                }

                if (categories.contains(CONTROLLERS)) {
                    controllers.add(new MenuInfo(item.getStaticLabel(), name));
                }

                if (categories.contains(NON_TEST_ELEMENTS)) {
                    nonTestElements.add(new MenuInfo(item.getStaticLabel(), name));
                }

                if (categories.contains(LISTENERS)) {
                    listeners.add(new MenuInfo(item.getStaticLabel(), name));
                }

                if (categories.contains(CONFIG_ELEMENTS)) {
                    configElements.add(new MenuInfo(item.getStaticLabel(), name));
                }

                if (categories.contains(TABLES)) {
                    tables.add(new MenuInfo(item.getStaticLabel(), name));
                }

                if (categories.contains(REPORT_PAGE)) {
                    reportPage.add(new MenuInfo(item.getStaticLabel(), name));
                }
            }
        } catch (IOException e) {
            log.error("", e);
        }
    }

    private static void addSeparator(JPopupMenu menu) {
        MenuElement[] elements = menu.getSubElements();
        if ((elements.length > 0) && !(elements[elements.length - 1] instanceof JPopupMenu.Separator)) {
            menu.addSeparator();
        }
    }
}
