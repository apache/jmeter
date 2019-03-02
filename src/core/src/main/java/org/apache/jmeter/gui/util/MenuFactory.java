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
import java.awt.HeadlessException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.TestFragmentController;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.UndoHistory;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.KeyStrokes;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.NonTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Printable;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.reflect.ClassFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MenuFactory {
    private static final Logger log = LoggerFactory.getLogger(MenuFactory.class);

    /*
     *  Predefined strings for makeMenu().
     *  These are used as menu categories in the menuMap HashMap,
     *  and also for resource lookup in messages.properties
     *  TODO: why isn't this an enum?
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
    public static final String SEPARATOR = "menu_separator"; //$NON-NLS-1$

    private static final Map<String, List<MenuInfo>> menuMap;

    static {
        menuMap = new HashMap<>();
        menuMap.put(THREADS, new LinkedList<>());
        menuMap.put(TIMERS, new LinkedList<>());
        menuMap.put(ASSERTIONS, new LinkedList<>());
        menuMap.put(CONFIG_ELEMENTS, new LinkedList<>());
        menuMap.put(CONTROLLERS, new LinkedList<>());
        menuMap.put(LISTENERS, new LinkedList<>());
        menuMap.put(NON_TEST_ELEMENTS, new LinkedList<>());
        menuMap.put(SAMPLERS, new LinkedList<>());
        menuMap.put(POST_PROCESSORS, new LinkedList<>());
        menuMap.put(PRE_PROCESSORS, new LinkedList<>());
        menuMap.put(FRAGMENTS, new LinkedList<>());
        menuMap.put(SEPARATOR, Collections.singletonList(new MenuSeparatorInfo()));

        try {
            initializeMenus(menuMap, classesToSkip());
            sortMenus(menuMap.values());
            separateItemsWithExplicitOrder(menuMap.values());
        } catch (Error | RuntimeException ex) { // NOSONAR We want to log Errors in jmeter.log
            log.error("Error initializing menus, check configuration if using 3rd party libraries", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Error initializing menus, check configuration if using 3rd party libraries", ex);
        }
    }

    private static Set<String> classesToSkip() {
        return Arrays.stream(JMeterUtils.getPropDefault("not_in_menu", "").split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    private static void initializeMenus(
            Map<String, List<MenuInfo>> menus, Set<String> elementsToSkip) {
        try {
            List<String> guiClasses = ClassFinder
                    .findClassesThatExtend(
                            JMeterUtils.getSearchPaths(),
                            new Class[] {JMeterGUIComponent.class, TestBean.class})
                    .stream()
                    // JMeterTreeNode and TestBeanGUI are special GUI classes,
                    // and aren't intended to be added to menus
                    .filter(name -> !name.endsWith("JMeterTreeNode"))
                    .filter(name -> !name.endsWith("TestBeanGUI"))
                    .filter(name -> !elementsToSkip.contains(name))
                    .distinct()
                    .map(String::trim)
                    .collect(Collectors.toList());

            for (String className : guiClasses) {
                JMeterGUIComponent item = getGUIComponent(className, elementsToSkip);
                if (item == null) {
                    continue;
                }

                Collection<String> categories = item.getMenuCategories();
                if (categories == null) {
                    log.debug("{} participates in no menus.", className);
                    continue;
                }
                for (Map.Entry<String, List<MenuInfo>> entry: menus.entrySet()) {
                    if (categories.contains(entry.getKey())) {
                        entry.getValue().add(new MenuInfo(item, className));
                    }
                }
            }
        } catch (IOException e) {
            log.error("IO Exception while initializing menus.", e);
        }
    }

    private static JMeterGUIComponent getGUIComponent(
            String name, Set<String> elementsToSkip) {
        JMeterGUIComponent item = null;
        boolean hideBean = false; // Should the TestBean be hidden?
        try {
            Class<?> c = Class.forName(name);
            if (TestBean.class.isAssignableFrom(c)) {
                TestBeanGUI testBeanGUI = new TestBeanGUI(c);
                hideBean = testBeanGUI.isHidden()
                        || (testBeanGUI.isExpert() && !JMeterUtils.isExpertMode());
                item = testBeanGUI;
            } else {
                item = (JMeterGUIComponent) c.getDeclaredConstructor().newInstance();
            }
        } catch (NoClassDefFoundError e) {
            log.warn("Configuration error, probably corrupt or missing third party library(jar)? Could not create class: {}.",
                    name, e);
        } catch (HeadlessException e) {
            log.warn("Could not instantiate class: {}", name, e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not instantiate class: {}", name, e);
        }
        if (hideBean || (item != null && elementsToSkip.contains(item.getStaticLabel()))) {
            log.info("Skipping {}", name);
            item = null;
        }
        return item;
    }

    private static void sortMenus(Collection<List<MenuInfo>> menus) {
        for (List<MenuInfo> menu : menus) {
            menu.sort(Comparator.comparing(MenuInfo::getLabel));
            menu.sort(Comparator.comparingInt(MenuInfo::getSortOrder));
        }
    }

    private static void separateItemsWithExplicitOrder(Collection<List<MenuInfo>> menus) {
        for (List<MenuInfo> menu : menus) {
            Optional<MenuInfo> firstDefaultSortItem = menu.stream()
                    .filter(info -> info.getSortOrder() == MenuInfo.SORT_ORDER_DEFAULT)
                    .findFirst();
            int index = menu.indexOf(firstDefaultSortItem.orElseThrow(IllegalStateException::new));
            if (index > 0) {
                menu.add(index, new MenuSeparatorInfo());
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
        menu.add(makeMenuItemRes("duplicate", ActionNames.DUPLICATE, KeyStrokes.DUPLICATE));  //$NON-NLS-1$
        if (removable) {
            menu.add(makeMenuItemRes("remove", ActionNames.REMOVE, KeyStrokes.REMOVE)); //$NON-NLS-1$
        }
    }

    public static void addPasteResetMenu(JPopupMenu menu) {
        addSeparator(menu);
        menu.add(makeMenuItemRes("paste", ActionNames.PASTE, KeyStrokes.PASTE)); //$NON-NLS-1$
    }

    public static void addFileMenu(JPopupMenu pop) {
        addFileMenu(pop, true);
    }

    /**
     * @param menu JPopupMenu
     * @param addSaveTestFragmentMenu Add Save as Test Fragment menu if true
     */
    public static void addFileMenu(JPopupMenu menu, boolean addSaveTestFragmentMenu) {
        // the undo/redo as a standard goes first in Edit menus
        if(UndoHistory.isEnabled()) {
            addUndoItems(menu);
        }

        addSeparator(menu);
        menu.add(makeMenuItemRes("open", ActionNames.OPEN));// $NON-NLS-1$
        menu.add(makeMenuItemRes("menu_merge", ActionNames.MERGE));// $NON-NLS-1$
        menu.add(makeMenuItemRes("save_as", ActionNames.SAVE_AS));// $NON-NLS-1$
        if(addSaveTestFragmentMenu) {
            menu.add(makeMenuItemRes("save_as_test_fragment", // $NON-NLS-1$
                    ActionNames.SAVE_AS_TEST_FRAGMENT));
        }
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
        disabled.setEnabled(isEnabled);
        enabled.setEnabled(!isEnabled);
        menu.add(enabled);
        menu.add(disabled);
        JMenuItem toggle = makeMenuItemRes("toggle", ActionNames.TOGGLE, KeyStrokes.TOGGLE);// $NON-NLS-1$
        menu.add(toggle);
        addSeparator(menu);
        menu.add(makeMenuItemRes("help", ActionNames.HELP));// $NON-NLS-1$
    }

    /**
     * Add undo / redo to the provided menu
     *
     * @param menu JPopupMenu
     */
    private static void addUndoItems(JPopupMenu menu) {
        addSeparator(menu);

        JMenuItem undo = makeMenuItemRes("undo", ActionNames.UNDO); //$NON-NLS-1$
        undo.setEnabled(GuiPackage.getInstance().canUndo());
        menu.add(undo);

        JMenuItem redo = makeMenuItemRes("redo", ActionNames.REDO); //$NON-NLS-1$
        // TODO: we could even show some hints on action being undone here
        // if required (by passing those hints into history records)
        redo.setEnabled(GuiPackage.getInstance().canRedo());
        menu.add(redo);
    }


    public static JMenu makeMenus(String[] categories, String label, String actionCommand) {
        JMenu addMenu = new JMenu(label);
        Arrays.stream(categories)
                .map(category -> makeMenu(category, actionCommand))
                .forEach(addMenu::add);
        GuiUtils.makeScrollableMenu(addMenu);
        return addMenu;
    }

    public static JPopupMenu getDefaultControllerMenu() {
        JPopupMenu pop = new JPopupMenu();
        String addAction = ActionNames.ADD;
        JMenu addMenu = new JMenu(JMeterUtils.getResString("add")); // $NON-NLS-1$
        addMenu.add(MenuFactory.makeMenu(MenuFactory.SAMPLERS, addAction));
        addMenu.addSeparator();
        addMenu.add(MenuFactory.makeMenu(MenuFactory.CONTROLLERS, addAction));
        addMenu.addSeparator();
        pop.add(addDefaultAddMenuToMenu(addMenu, addAction));
        pop.add(MenuFactory.makeMenuItemRes("add_think_times",// $NON-NLS-1$
                ActionNames.ADD_THINK_TIME_BETWEEN_EACH_STEP));

        pop.add(MenuFactory.makeMenuItemRes("apply_naming",// $NON-NLS-1$
                ActionNames.APPLY_NAMING_CONVENTION));

        pop.add(makeMenus(new String[]{CONTROLLERS},
                JMeterUtils.getResString("change_parent"),// $NON-NLS-1$
                ActionNames.CHANGE_PARENT));

        pop.add(makeMenus(new String[]{CONTROLLERS},
                JMeterUtils.getResString("insert_parent"),// $NON-NLS-1$
                ActionNames.ADD_PARENT));
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    private static JMenu createDefaultAddMenu() {
        String addAction = ActionNames.ADD;
        JMenu addMenu = new JMenu(JMeterUtils.getResString("add")); // $NON-NLS-1$
        addDefaultAddMenuToMenu(addMenu, addAction);
        return addMenu;
    }

    private static JMenu addDefaultAddMenuToMenu(JMenu addMenu, String addAction) {
        addMenu.add(MenuFactory.makeMenu(MenuFactory.ASSERTIONS, addAction));
        addMenu.addSeparator();
        addMenu.add(MenuFactory.makeMenu(MenuFactory.TIMERS, addAction));
        addMenu.addSeparator();
        addMenu.add(MenuFactory.makeMenu(MenuFactory.PRE_PROCESSORS, addAction));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.POST_PROCESSORS, addAction));
        addMenu.addSeparator();
        addMenu.add(MenuFactory.makeMenu(MenuFactory.CONFIG_ELEMENTS, addAction));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.LISTENERS, addAction));
        return addMenu;
    }

    public static JPopupMenu getDefaultSamplerMenu() {
        JPopupMenu pop = new JPopupMenu();
        pop.add(createDefaultAddMenu());
        pop.add(makeMenus(new String[]{CONTROLLERS},
                JMeterUtils.getResString("insert_parent"),// $NON-NLS-1$
                ActionNames.ADD_PARENT));
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    public static JPopupMenu getDefaultConfigElementMenu() {
        return createDefaultPopupMenu();
    }

    public static JPopupMenu getDefaultVisualizerMenu() {
        JPopupMenu pop = new JPopupMenu();
        pop.add(MenuFactory.makeMenuItemRes(
                "clear", ActionNames.CLEAR)); //$NON-NLS-1$
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    public static JPopupMenu getDefaultTimerMenu() {
        return createDefaultPopupMenu();
    }

    public static JPopupMenu getDefaultAssertionMenu() {
        return createDefaultPopupMenu();
    }

    public static JPopupMenu getDefaultExtractorMenu() {
        return createDefaultPopupMenu();
    }

    public static JPopupMenu getDefaultMenu() { // if type is unknown
        return createDefaultPopupMenu();
    }

    private static JPopupMenu createDefaultPopupMenu() {
        JPopupMenu pop = new JPopupMenu();
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    /**
     * Create a menu from a menu category.
     *
     * @param category      predefined string (used as key for menuMap HashMap
     *                      and messages.properties lookup)
     * @param actionCommand predefined string, e.g. {@code }ActionNames.ADD}
     *                      {@link ActionNames}
     * @return the menu
     */
    public static JMenu makeMenu(String category, String actionCommand) {
        return makeMenu(
                menuMap.get(category),
                actionCommand,
                JMeterUtils.getResString(category));
    }

    /**
     * Create a menu from a collection of items.
     *
     * @param menuInfo      collection of MenuInfo items
     * @param actionCommand predefined string, e.g. ActionNames.ADD
     *                      {@link ActionNames}
     * @param menuName The name of the newly created menu
     * @return the menu
     */
    private static JMenu makeMenu(
            Collection<MenuInfo> menuInfo, String actionCommand, String menuName) {

        JMenu menu = new JMenu(menuName);
        menuInfo.stream()
                .map(info -> makeMenuItem(info, actionCommand))
                .forEach(menu::add);
        GuiUtils.makeScrollableMenu(menu);
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
     * @param actionCommand predefined string, e.g. ActionNames.ADD
     *                      {@link ActionNames}
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
     * @param actionCommand predefined string, e.g. ActionNames.ADD
     *                      {@link ActionNames}
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
     * @param actionCommand predefined string, e.g. ActionNames.ADD
     *                      {@link ActionNames}
     * @return the menu item
     */
    private static Component makeMenuItem(MenuInfo info, String actionCommand) {
        if (info instanceof MenuSeparatorInfo) {
            return new JPopupMenu.Separator();
        }

        JMenuItem newMenuChoice = new JMenuItem(info.getLabel());
        newMenuChoice.setName(info.getClassName());
        newMenuChoice.setEnabled(info.getEnabled(actionCommand));
        newMenuChoice.addActionListener(ActionRouter.getInstance());
        if (actionCommand != null) {
            newMenuChoice.setActionCommand(actionCommand);
        }

        return newMenuChoice;
    }

    private static JMenuItem makeMenuItemRes(String resource, String actionCommand, KeyStroke accel) {
        JMenuItem item = makeMenuItemRes(resource, actionCommand);
        item.setAccelerator(accel);
        return item;
    }

    private static void addSeparator(JPopupMenu menu) {
        MenuElement[] elements = menu.getSubElements();
        if ((elements.length > 0)
                && !(elements[elements.length - 1] instanceof JPopupMenu.Separator)) {
            menu.addSeparator();
        }
    }

    /**
     * Determine whether or not nodes can be added to this parent.
     * <p>
     * Used by Merge
     *
     * @param parentNode The {@link JMeterTreeNode} to test, if a new element
     *                   can be added to it
     * @param element    top-level test element to be added
     * @return whether it is OK to add the element to this parent
     */
    public static boolean canAddTo(JMeterTreeNode parentNode, TestElement element) {
        JMeterTreeNode node = new JMeterTreeNode(element, null);
        return canAddTo(parentNode, new JMeterTreeNode[]{node});
    }

    /**
     * Determine whether or not nodes can be added to this parent.
     * <p>
     * Used by DragNDrop and Paste.
     *
     * @param parentNode The {@link JMeterTreeNode} to test, if <code>nodes[]</code>
     *            can be added to it
     * @param nodes      array of nodes that are to be added
     * @return whether it is OK to add the dragged nodes to this parent
     */
    public static boolean canAddTo(JMeterTreeNode parentNode, JMeterTreeNode[] nodes) {
        if (parentNode == null
                || foundClass(nodes, new Class[]{TestPlan.class})) {
            return false;
        }
        TestElement parent = parentNode.getTestElement();

        // Force TestFragment to only be pastable under a Test Plan
        if (foundClass(nodes, new Class[]{TestFragmentController.class})) {
            return parent instanceof TestPlan;
        }

        // Cannot move Non-Test Elements from root of Test Plan or Test Fragment
        if (foundMenuCategories(nodes, NON_TEST_ELEMENTS)
                && !(parent instanceof TestPlan || parent instanceof TestFragmentController)) {
            return false;
        }

        if (parent instanceof TestPlan) {
            List<Class<?>> samplerAndController = Arrays.asList(Sampler.class, Controller.class);
            List<Class<?>> exceptions = Arrays.asList(AbstractThreadGroup.class, NonTestElement.class);
            return !foundClass(nodes, samplerAndController, exceptions);
        }
        // AbstractThreadGroup is only allowed under a TestPlan
        if (foundClass(nodes, new Class[]{AbstractThreadGroup.class})) {
            return false;
        }

        // Includes thread group; anything goes
        if (parent instanceof Controller) {
            return true;
        }

        // No Samplers and Controllers
        if (parent instanceof Sampler) {
            return !foundClass(nodes, new Class[]{Sampler.class, Controller.class});
        }

        // All other
        return false;
    }

    /**
     * Is any of nodes an instance of one of the classes?
     *
     * @param nodes Array of {@link JMeterTreeNode}
     * @param classes Array of {@link Class}
     * @return true if nodes is one of classes
     */
    private static boolean foundClass(JMeterTreeNode[] nodes, Class<?>[] classes) {
        for (JMeterTreeNode node : nodes) {
            for (Class<?> aClass : classes) {
                if (aClass.isInstance(node.getUserObject())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Is any node an instance of one of the menu category?
     * @param nodes Array of {@link JMeterTreeNode}
     * @param category Category
     * @return true if nodes is in category
     */
    private static boolean foundMenuCategories(JMeterTreeNode[] nodes, String category) {
        return Arrays.stream(nodes)
                .flatMap(node -> node.getMenuCategories().stream())
                .anyMatch(category::equals);
    }

    /**
     * Is any node an instance of one of the classes, but not an exceptions?
     *
     * @param nodes array of {@link JMeterTreeNode}
     * @param classes Array of {@link Class}
     * @param exceptions Array of {@link Class}
     * @return boolean
     */
    private static boolean foundClass(
            JMeterTreeNode[] nodes, List<Class<?>> classes, List<Class<?>> exceptions) {
        return Arrays.stream(nodes)
                .map(DefaultMutableTreeNode::getUserObject)
                .filter(userObj -> exceptions.stream().noneMatch(c -> c.isInstance(userObj)))
                .anyMatch(userObj -> classes.stream().anyMatch(c -> c.isInstance(userObj)));
    }
}
