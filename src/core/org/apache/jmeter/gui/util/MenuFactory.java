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

import java.awt.Component;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.KeyStrokes;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Printable;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

public final class MenuFactory {
    private static final Logger log = LoggingManager.getLoggerForClass();

    /*
     *  Predefined strings for makeMenu().
     *  These are used as menu categories in the menuMap Hashmap,
     *  and also for resource lookup in messages.properties
    */
    public static final String THREADS = "menu_threads"; //$NON-NLS-1$
    
    public static final String FRAGMENTS = "menu_fragments"; //$NON-NLS-1$

    public static final String TIMERS = "menu_timer"; //$NON-NLS-1$

    public static final String CONTROLLERS = "menu_logic_controller"; //$NON-NLS-1$

    public static final String SAMPLERS = "menu_generative_controller"; //$NON-NLS-1$

    public static final String CONFIG_ELEMENTS = "menu_config_element"; //$NON-NLS-1$

    public static final String POST_PROCESSORS = "menu_post_processors"; //$NON-NLS-1$

    public static final String PRE_PROCESSORS = "menu_pre_processors"; //$NON-NLS-1$

    public static final String ASSERTIONS = "menu_assertions"; //$NON-NLS-1$

    public static final String NON_TEST_ELEMENTS = "menu_non_test_elements"; //$NON-NLS-1$

    public static final String LISTENERS = "menu_listener"; //$NON-NLS-1$

    private static final Map<String, List<MenuInfo>> menuMap =
        new HashMap<String, List<MenuInfo>>();

    private static final Set<String> elementsToSkip = new HashSet<String>();

    // MENU_ADD_xxx - controls which items are in the ADD menu
    // MENU_PARENT_xxx - controls which items are in the Insert Parent menu
    private static final String[] MENU_ADD_CONTROLLER = new String[] {
        MenuFactory.CONTROLLERS,
        MenuFactory.CONFIG_ELEMENTS,
        MenuFactory.TIMERS,
        MenuFactory.PRE_PROCESSORS,
        MenuFactory.SAMPLERS,
        MenuFactory.POST_PROCESSORS,
        MenuFactory.ASSERTIONS,
        MenuFactory.LISTENERS,
        };

    private static final String[] MENU_PARENT_CONTROLLER = new String[] {
        MenuFactory.CONTROLLERS };

    private static final String[] MENU_ADD_SAMPLER = new String[] {
        MenuFactory.CONFIG_ELEMENTS,
        MenuFactory.TIMERS,
        MenuFactory.PRE_PROCESSORS,
        MenuFactory.POST_PROCESSORS,
        MenuFactory.ASSERTIONS,
        MenuFactory.LISTENERS,
        };

    private static final String[] MENU_PARENT_SAMPLER = new String[] {
        MenuFactory.CONTROLLERS };

    private static final List<MenuInfo> timers, controllers, samplers, threads, 
        fragments,configElements, assertions, listeners, nonTestElements,
        postProcessors, preProcessors;

    static {
        threads = new LinkedList<MenuInfo>();
        fragments = new LinkedList<MenuInfo>();
        timers = new LinkedList<MenuInfo>();
        controllers = new LinkedList<MenuInfo>();
        samplers = new LinkedList<MenuInfo>();
        configElements = new LinkedList<MenuInfo>();
        assertions = new LinkedList<MenuInfo>();
        listeners = new LinkedList<MenuInfo>();
        postProcessors = new LinkedList<MenuInfo>();
        preProcessors = new LinkedList<MenuInfo>();
        nonTestElements = new LinkedList<MenuInfo>();
        menuMap.put(THREADS, threads);
        menuMap.put(FRAGMENTS, fragments);
        menuMap.put(TIMERS, timers);
        menuMap.put(ASSERTIONS, assertions);
        menuMap.put(CONFIG_ELEMENTS, configElements);
        menuMap.put(CONTROLLERS, controllers);
        menuMap.put(LISTENERS, listeners);
        menuMap.put(NON_TEST_ELEMENTS, nonTestElements);
        menuMap.put(SAMPLERS, samplers);
        menuMap.put(POST_PROCESSORS, postProcessors);
        menuMap.put(PRE_PROCESSORS, preProcessors);
        try {
            String[] classesToSkip =
                JOrphanUtils.split(JMeterUtils.getPropDefault("not_in_menu", ""), ","); //$NON-NLS-1$
            for (int i = 0; i < classesToSkip.length; i++) {
                elementsToSkip.add(classesToSkip[i].trim());
            }

            initializeMenus();
            sortPluginMenus();
        } catch (Throwable e) {
            log.error("", e);
            if (e instanceof Error){
                throw (Error) e;
            }
            if (e instanceof RuntimeException){
                throw (RuntimeException) e;
            }
        }
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private MenuFactory() {
    }

    public static void addEditMenu(JPopupMenu menu, boolean removable) {
        addSeparator(menu);
        if (removable) {
            menu.add(makeMenuItemRes("cut", ActionNames.CUT, KeyStrokes.CUT)); //$NON-NLS-1$
        }
        menu.add(makeMenuItemRes("copy", ActionNames.COPY, KeyStrokes.COPY));  //$NON-NLS-1$
        menu.add(makeMenuItemRes("paste", ActionNames.PASTE, KeyStrokes.PASTE)); //$NON-NLS-1$
        menu.add(makeMenuItemRes("reset_gui", ActionNames.RESET_GUI )); //$NON-NLS-1$
        if (removable) {
            menu.add(makeMenuItemRes("remove", ActionNames.REMOVE, KeyStrokes.REMOVE)); //$NON-NLS-1$
        }
    }

    public static void addPasteResetMenu(JPopupMenu menu) {
        addSeparator(menu);
        menu.add(makeMenuItemRes("paste", ActionNames.PASTE, KeyStrokes.PASTE)); //$NON-NLS-1$
        menu.add(makeMenuItemRes("reset_gui", ActionNames.RESET_GUI )); //$NON-NLS-1$
    }

    public static void addFileMenu(JPopupMenu menu) {
        addSeparator(menu);
        menu.add(makeMenuItemRes("open", ActionNames.OPEN));// $NON-NLS-1$
        menu.add(makeMenuItemRes("menu_merge", ActionNames.MERGE));// $NON-NLS-1$
        menu.add(makeMenuItemRes("save_as", ActionNames.SAVE_AS));// $NON-NLS-1$

        addSeparator(menu);
        JMenuItem savePicture = makeMenuItemRes("save_as_image",// $NON-NLS-1$
                ActionNames.SAVE_GRAPHICS,
                KeyStrokes.SAVE_GRAPHICS);
        menu.add(savePicture);
        if (!(GuiPackage.getInstance().getCurrentGui() instanceof Printable)) {
            savePicture.setEnabled(false);
        }

        JMenuItem savePictureAll = makeMenuItemRes("save_as_image_all",// $NON-NLS-1$
                ActionNames.SAVE_GRAPHICS_ALL,
                KeyStrokes.SAVE_GRAPHICS_ALL);
        menu.add(savePictureAll);

        addSeparator(menu);

        JMenuItem disabled = makeMenuItemRes("disable", ActionNames.DISABLE);// $NON-NLS-1$
        JMenuItem enabled = makeMenuItemRes("enable", ActionNames.ENABLE);// $NON-NLS-1$
        boolean isEnabled = GuiPackage.getInstance().getTreeListener().getCurrentNode().isEnabled();
        if (isEnabled) {
            disabled.setEnabled(true);
            enabled.setEnabled(false);
        } else {
            disabled.setEnabled(false);
            enabled.setEnabled(true);
        }
        menu.add(enabled);
        menu.add(disabled);
        JMenuItem toggle = makeMenuItemRes("toggle", ActionNames.TOGGLE, KeyStrokes.TOGGLE);// $NON-NLS-1$
        menu.add(toggle);
        addSeparator(menu);
        menu.add(makeMenuItemRes("help", ActionNames.HELP));// $NON-NLS-1$
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

    public static JPopupMenu getDefaultSamplerMenu() {
        JPopupMenu pop = new JPopupMenu();
        pop.add(MenuFactory.makeMenus(MENU_ADD_SAMPLER,
                JMeterUtils.getResString("add"),// $NON-NLS-1$
                ActionNames.ADD));
        pop.add(makeMenus(MENU_PARENT_SAMPLER,
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

    public static JPopupMenu getDefaultMenu() { // if type is unknown
        JPopupMenu pop = new JPopupMenu();
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    /**
     * Create a menu from a menu category.
     *
     * @param category - predefined string (used as key for menuMap HashMap and messages.properties lookup)
     * @param actionCommand - predefined string, e.g. ActionNames.ADD
     *     @see org.apache.jmeter.gui.action.ActionNames
     * @return the menu
     */
    public static JMenu makeMenu(String category, String actionCommand) {
        return makeMenu(menuMap.get(category), actionCommand, JMeterUtils.getResString(category));
    }

    /**
     * Create a menu from a collection of items.
     *
     * @param menuInfo - collection of MenuInfo items
     * @param actionCommand - predefined string, e.g. ActionNames.ADD
     *     @see org.apache.jmeter.gui.action.ActionNames
     * @param menuName
     * @return the menu
     */
    public static JMenu makeMenu(Collection<MenuInfo> menuInfo, String actionCommand, String menuName) {
        JMenu menu = new JMenu(menuName);
        for (MenuInfo info : menuInfo) {
            menu.add(makeMenuItem(info, actionCommand));
        }
        return menu;
    }

    public static void setEnabled(JMenu menu) {
        if (menu.getSubElements().length == 0) {
            menu.setEnabled(false);
        }
    }

    /**
     * Create a single menu item
     *
     * @param label for the MenuItem
     * @param name for the MenuItem
     * @param actionCommand - predefined string, e.g. ActionNames.ADD
     *     @see org.apache.jmeter.gui.action.ActionNames
     * @return the menu item
     */
    public static JMenuItem makeMenuItem(String label, String name, String actionCommand) {
        JMenuItem newMenuChoice = new JMenuItem(label);
        newMenuChoice.setName(name);
        newMenuChoice.addActionListener(ActionRouter.getInstance());
        if (actionCommand != null) {
            newMenuChoice.setActionCommand(actionCommand);
        }

        return newMenuChoice;
    }

    /**
     * Create a single menu item from the resource name.
     *
     * @param resource for the MenuItem
     * @param actionCommand - predefined string, e.g. ActionNames.ADD
     *     @see org.apache.jmeter.gui.action.ActionNames
     * @return the menu item
     */
    public static JMenuItem makeMenuItemRes(String resource, String actionCommand) {
        JMenuItem newMenuChoice = new JMenuItem(JMeterUtils.getResString(resource));
        newMenuChoice.setName(resource);
        newMenuChoice.addActionListener(ActionRouter.getInstance());
        if (actionCommand != null) {
            newMenuChoice.setActionCommand(actionCommand);
        }

        return newMenuChoice;
    }

    /**
     * Create a single menu item from a MenuInfo object
     *
     * @param info the MenuInfo object
     * @param actionCommand - predefined string, e.g. ActionNames.ADD
     *     @see org.apache.jmeter.gui.action.ActionNames
     * @return the menu item
     */
    public static Component makeMenuItem(MenuInfo info, String actionCommand) {
        JMenuItem newMenuChoice = new JMenuItem(info.getLabel());
        newMenuChoice.setName(info.getClassName());
        newMenuChoice.addActionListener(ActionRouter.getInstance());
        if (actionCommand != null) {
            newMenuChoice.setActionCommand(actionCommand);
        }

        return newMenuChoice;
    }

    public static JMenuItem makeMenuItemRes(String resource, String actionCommand, KeyStroke accel) {
        JMenuItem item = makeMenuItemRes(resource, actionCommand);
        item.setAccelerator(accel);
        return item;
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
            Collections.sort(guiClasses);
            for (String name : guiClasses) {

                /*
                 * JMeterTreeNode and TestBeanGUI are special GUI classes, and
                 * aren't intended to be added to menus
                 *
                 * TODO: find a better way of checking this
                 */
                if (name.endsWith("JMeterTreeNode") // $NON-NLS-1$
                        || name.endsWith("TestBeanGUI")) {// $NON-NLS-1$
                    continue;// Don't try to instantiate these
                }

                if (elementsToSkip.contains(name)) { // No point instantiating class
                    log.info("Skipping " + name);
                    continue;
                }

                boolean hideBean = false; // Should the TestBean be hidden?

                JMeterGUIComponent item;
                try {
                    Class<?> c = Class.forName(name);
                    if (TestBean.class.isAssignableFrom(c)) {
                        TestBeanGUI tbgui = new TestBeanGUI(c);
                        hideBean = tbgui.isHidden() || (tbgui.isExpert() && !JMeterUtils.isExpertMode());
                        item = tbgui;
                    } else {
                        item = (JMeterGUIComponent) c.newInstance();
                    }
                } catch (NoClassDefFoundError e) {
                    log.warn("Missing jar? Could not create " + name + ". " + e);
                    continue;
                } catch (Throwable e) {
                    log.warn("Could not instantiate " + name, e);
                    if (e instanceof Error){
                        throw (Error) e;
                    }
                    if (e instanceof RuntimeException){
                        throw (RuntimeException) e;
                    }
                    continue;
                }
                if (hideBean || elementsToSkip.contains(item.getStaticLabel())) {
                    log.info("Skipping " + name);
                    continue;
                } else {
                    elementsToSkip.add(name); // Don't add it again
                }
                Collection<String> categories = item.getMenuCategories();
                if (categories == null) {
                    log.debug(name + " participates in no menus.");
                    continue;
                }
                if (categories.contains(THREADS)) {
                    threads.add(new MenuInfo(item, name));
                }
                if (categories.contains(FRAGMENTS)) {
                    fragments.add(new MenuInfo(item, name));
                }
                if (categories.contains(TIMERS)) {
                    timers.add(new MenuInfo(item, name));
                }

                if (categories.contains(POST_PROCESSORS)) {
                    postProcessors.add(new MenuInfo(item, name));
                }

                if (categories.contains(PRE_PROCESSORS)) {
                    preProcessors.add(new MenuInfo(item, name));
                }

                if (categories.contains(CONTROLLERS)) {
                    controllers.add(new MenuInfo(item, name));
                }

                if (categories.contains(SAMPLERS)) {
                    samplers.add(new MenuInfo(item, name));
                }

                if (categories.contains(NON_TEST_ELEMENTS)) {
                    nonTestElements.add(new MenuInfo(item, name));
                }

                if (categories.contains(LISTENERS)) {
                    listeners.add(new MenuInfo(item, name));
                }

                if (categories.contains(CONFIG_ELEMENTS)) {
                    configElements.add(new MenuInfo(item, name));
                }
                if (categories.contains(ASSERTIONS)) {
                    assertions.add(new MenuInfo(item, name));
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

    /**
     * Determine whether or not nodes can be added to this parent.
     *
     * Used by Merge
     *
     * @param parentNode
     * @param element - top-level test element to be added
     *
     * @return whether it is OK to add the element to this parent
     */
    public static boolean canAddTo(JMeterTreeNode parentNode, TestElement element) {
        JMeterTreeNode node = new JMeterTreeNode(element, null);
        return canAddTo(parentNode, new JMeterTreeNode[]{node});
    }

    /**
     * Determine whether or not nodes can be added to this parent.
     *
     * Used by DragNDrop and Paste.
     *
     * @param parentNode
     * @param nodes - array of nodes that are to be added
     *
     * @return whether it is OK to add the dragged nodes to this parent
     */
    public static boolean canAddTo(JMeterTreeNode parentNode, JMeterTreeNode nodes[]) {
        if (null == parentNode) {
            return false;
        }
        if (foundClass(nodes, new Class[]{WorkBench.class})){// Can't add a Workbench anywhere
            return false;
        }
        if (foundClass(nodes, new Class[]{TestPlan.class})){// Can't add a TestPlan anywhere
            return false;
        }
        TestElement parent = parentNode.getTestElement();

        // Force TestFragment to only be pastable under a Test Plan
        if (foundClass(nodes, new Class[]{org.apache.jmeter.control.TestFragmentController.class})){
            if (parent instanceof TestPlan)
                return true;
            return false;
        }

        if (parent instanceof WorkBench) {// allow everything else
            return true;
        }
        if (parent instanceof TestPlan) {
            if (foundClass(nodes,
                     new Class[]{Sampler.class, Controller.class}, // Samplers and Controllers need not apply ...
                     org.apache.jmeter.threads.AbstractThreadGroup.class)  // but AbstractThreadGroup (Controller) is OK
                ){
                return false;
            }
            return true;
        }
        // AbstractThreadGroup is only allowed under a TestPlan
        if (foundClass(nodes, new Class[]{org.apache.jmeter.threads.AbstractThreadGroup.class})){
            return false;
        }
        if (parent instanceof Controller) {// Includes thread group; anything goes
            return true;
        }
        if (parent instanceof Sampler) {// Samplers and Controllers need not apply ...
            if (foundClass(nodes, new Class[]{Sampler.class, Controller.class})){
                return false;
            }
            return true;
        }
        // All other
        return false;
    }

    // Is any node an instance of one of the classes?
    private static boolean foundClass(JMeterTreeNode nodes[],Class<?> classes[]){
        for (int i = 0; i < nodes.length; i++) {
            JMeterTreeNode node = nodes[i];
            for (int j=0; j < classes.length; j++) {
                if (classes[j].isInstance(node.getUserObject())){
                    return true;
                }
            }
        }
        return false;
    }

    // Is any node an instance of one of the classes, but not an exception?
    private static boolean foundClass(JMeterTreeNode nodes[],Class<?> classes[], Class<?> except){
        for (int i = 0; i < nodes.length; i++) {
            JMeterTreeNode node = nodes[i];
            Object userObject = node.getUserObject();
            if (!except.isInstance(userObject)) {
                for (int j=0; j < classes.length; j++) {
                    if (classes[j].isInstance(userObject)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Methods used for Test cases
    static int menuMap_size() {
        return menuMap.size();
    }
    static int assertions_size() {
        return assertions.size();
    }
    static int configElements_size() {
        return configElements.size();
    }
    static int controllers_size() {
        return controllers.size();
    }
    static int listeners_size() {
        return listeners.size();
    }
    static int nonTestElements_size() {
        return nonTestElements.size();
    }
    static int postProcessors_size() {
        return postProcessors.size();
    }
    static int preProcessors_size() {
        return preProcessors.size();
    }
    static int samplers_size() {
        return samplers.size();
    }
    static int timers_size() {
        return timers.size();
    }
    static int elementsToSkip_size() {
        return elementsToSkip.size();
    }

    /**
     * Menu sort helper class
     */
    private static class MenuInfoComparator implements Comparator<MenuInfo>, Serializable {
        private static final long serialVersionUID = 1L;
        public int compare(MenuInfo o1, MenuInfo o2) {
              return o1.getLabel().compareTo(o2.getLabel());
        }
    }

    /**
     * Sort loaded menus
     */
    private static void sortPluginMenus() {
       for (List<MenuInfo> menuToSort : menuMap.values()) {
          Collections.sort(menuToSort, new MenuInfoComparator());
       }
    }
}
