/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * @author    Michael Stover
 * @author	<a href="mailto:klancast@swbell.net">Keith Lancaster</a>
 * @version $Id$
 */
package org.apache.jmeter.gui.util;

import java.awt.event.KeyEvent;
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

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public final class MenuFactory
{
    transient private static Logger log = LoggingManager.getLoggerForClass();

    public final static String TIMERS = "menu_timer";
    public final static String CONTROLLERS = "menu_logic_controller";
    public final static String SAMPLERS = "menu_generative_controller";
    public final static String CONFIG_ELEMENTS = "menu_config_element";
    public final static String POST_PROCESSORS = "menu_post_processors";
    public final static String PRE_PROCESSORS = "menu_pre_processors";
    public final static String ASSERTIONS = "menu_assertions";
    public final static String NON_TEST_ELEMENTS = "menu_non_test_elements";
    public final static String LISTENERS = "menu_listener";
    private static Map menuMap = new HashMap();
    private static Set elementsToSkip = new HashSet();

    private static List timers,
        controllers,
        samplers,
        configElements,
        assertions,
        listeners,
        nonTestElements,
        postProcessors,
        preProcessors;

    //private static JMenu timerMenu;
    //private static JMenu controllerMenu;
    //private static JMenu generativeControllerMenu;
    //private static JMenu listenerMenu;
    //private static JMenu assertionMenu;
    //private static JMenu configMenu;
    //private static JMenu insertControllerMenu;
    //private static JMenu postProcessorMenu;
    //private static JMenu preProcessorMenu;

    static {
        try
        {
            String[] classesToSkip =
                JOrphanUtils.split(
                    JMeterUtils.getPropDefault("not_in_menu", ""),
                    ",");
            for (int i = 0; i < classesToSkip.length; i++)
            {
                elementsToSkip.add(classesToSkip[i].trim());
            }

            initializeMenus();
        }
        catch (Throwable e)
        {
            log.error("", e);
        }
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private MenuFactory()
    {
    }

    public static String doNothing()
    {
        return "doing nothing";
    }

    public static void addEditMenu(JPopupMenu menu, boolean removable)
    {
        addSeparator(menu);
        if (removable)
        {
            menu.add(
                makeMenuItem(
                    JMeterUtils.getResString("remove"),
                    "Remove",
                    "remove",
                    KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)));
        }
        menu.add(
            makeMenuItem(
                JMeterUtils.getResString("cut"),
                "Cut",
                "Cut",
                KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK)));
        menu.add(
            makeMenuItem(
                JMeterUtils.getResString("copy"),
                "Copy",
                "Copy",
                KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK)));
        menu.add(
            makeMenuItem(
                JMeterUtils.getResString("paste"),
                "Paste",
                "Paste",
                KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK)));
        menu.add(
            makeMenuItem(
                JMeterUtils.getResString("paste_insert"),
                "Paste Insert",
                "Paste Insert"));
    }

    public static void addFileMenu(JPopupMenu menu)
    {
        addSeparator(menu);
        menu.add(
            makeMenuItem(JMeterUtils.getResString("open"), "Open", "open"));
        menu.add(
            makeMenuItem(JMeterUtils.getResString("save_as"), "Save As", "save_as"));
        JMenuItem disabled =
            makeMenuItem(
                JMeterUtils.getResString("disable"),
                "Disable",
                "disable");
        JMenuItem enabled =
            makeMenuItem(
                JMeterUtils.getResString("enable"),
                "Enable",
                "enable");
        boolean isEnabled =
            GuiPackage
                .getInstance()
                .getTreeListener()
                .getCurrentNode()
                .isEnabled();
        if (isEnabled)
        {
            disabled.setEnabled(true);
            enabled.setEnabled(false);
        }
        else
        {
            disabled.setEnabled(false);
            enabled.setEnabled(true);
        }
        menu.add(enabled);
        menu.add(disabled);
        addSeparator(menu);
        menu.add(
            makeMenuItem(JMeterUtils.getResString("help"), "Help", "help"));
    }

    public static JMenu makeMenus(
        String[] categories,
        String label,
        String actionCommand)
    {
        JMenu addMenu = new JMenu(label);
        for (int i = 0; i < categories.length; i++)
        {
            addMenu.add(makeMenu(categories[i], actionCommand));
        }
        return addMenu;
    }

    public static JPopupMenu getDefaultControllerMenu()
    {
        JPopupMenu pop = new JPopupMenu();
        pop.add(
            MenuFactory.makeMenus(
                new String[] {
                    MenuFactory.CONTROLLERS,
                    MenuFactory.SAMPLERS,
                    MenuFactory.CONFIG_ELEMENTS,
                    MenuFactory.TIMERS,
                    MenuFactory.LISTENERS,
                    MenuFactory.PRE_PROCESSORS,
                    MenuFactory.POST_PROCESSORS },
                JMeterUtils.getResString("Add"),
                "Add"));
        pop.add(
            makeMenus(
                new String[] { MenuFactory.CONTROLLERS },
                JMeterUtils.getResString("insert_parent"),
                "Add Parent"));
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    public static JPopupMenu getDefaultSamplerMenu()
    {
        JPopupMenu pop = new JPopupMenu();
        pop.add(
            MenuFactory.makeMenus(
                new String[] {
                    MenuFactory.CONFIG_ELEMENTS,
                    MenuFactory.ASSERTIONS,
                    MenuFactory.TIMERS,
                    MenuFactory.LISTENERS,
                    MenuFactory.PRE_PROCESSORS,
                    MenuFactory.POST_PROCESSORS },
                JMeterUtils.getResString("Add"),
                "Add"));
        pop.add(
            makeMenus(
                new String[] { MenuFactory.CONTROLLERS },
                JMeterUtils.getResString("insert_parent"),
                "Add Parent"));
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    public static JPopupMenu getDefaultConfigElementMenu()
    {
        JPopupMenu pop = new JPopupMenu();
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    public static JPopupMenu getDefaultVisualizerMenu()
    {
        JPopupMenu pop = new JPopupMenu();
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    public static JPopupMenu getDefaultTimerMenu()
    {
        JPopupMenu pop = new JPopupMenu();
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    public static JPopupMenu getDefaultAssertionMenu()
    {
        JPopupMenu pop = new JPopupMenu();
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    public static JPopupMenu getDefaultExtractorMenu()
    {
        JPopupMenu pop = new JPopupMenu();
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    public static JMenu makeMenu(String category, String actionCommand)
    {
        return makeMenu(
            (Collection) menuMap.get(category),
            actionCommand,
            JMeterUtils.getResString(category));
    }

    public static JMenu makeMenu(
        Collection menuInfo,
        String actionCommand,
        String menuName)
    {
        Iterator iter = menuInfo.iterator();
        JMenu menu = new JMenu(menuName);
        while (iter.hasNext())
        {
            MenuInfo info = (MenuInfo) iter.next();
            menu.add(makeMenuItem(info.label, info.className, actionCommand));
        }
        return menu;
    }

    public static void setEnabled(JMenu menu)
    {
        if (menu.getSubElements().length == 0)
        {
            menu.setEnabled(false);
        }
    }

    public static JMenuItem makeMenuItem(
        String label,
        String name,
        String actionCommand)
    {
        JMenuItem newMenuChoice = new JMenuItem(label);
        newMenuChoice.setName(name);
        newMenuChoice.addActionListener(ActionRouter.getInstance());
        if (actionCommand != null)
        {
            newMenuChoice.setActionCommand(actionCommand);
        }

        return newMenuChoice;
    }

    public static JMenuItem makeMenuItem(
        String label,
        String name,
        String actionCommand,
        KeyStroke accel)
    {
        JMenuItem item = makeMenuItem(label, name, actionCommand);
        item.setAccelerator(accel);
        return item;
    }

    private static void initializeMenus()
    {
        try
        {
            List guiClasses =
                ClassFinder.findClassesThatExtend(
                    JMeterUtils.getSearchPaths(),
                    new Class[] { JMeterGUIComponent.class, TestBean.class });
            timers = new LinkedList();
            controllers = new LinkedList();
            samplers = new LinkedList();
            configElements = new LinkedList();
            assertions = new LinkedList();
            listeners = new LinkedList();
            postProcessors = new LinkedList();
            preProcessors = new LinkedList();
            nonTestElements = new LinkedList();
            menuMap.put(TIMERS, timers);
            menuMap.put(ASSERTIONS, assertions);
            menuMap.put(CONFIG_ELEMENTS, configElements);
            menuMap.put(CONTROLLERS, controllers);
            menuMap.put(LISTENERS, listeners);
            menuMap.put(NON_TEST_ELEMENTS, nonTestElements);
            menuMap.put(SAMPLERS, samplers);
            menuMap.put(POST_PROCESSORS, postProcessors);
            menuMap.put(PRE_PROCESSORS, preProcessors);
            Collections.sort(guiClasses);
            Iterator iter = guiClasses.iterator();
            while (iter.hasNext())
            {
                String name= (String)iter.next();
                
                JMeterGUIComponent item;
                try
                {
                    Class c = Class.forName(name);
                    if (TestBean.class.isAssignableFrom(c))
                    {
                        item = new TestBeanGUI(c);
                    }
                    else
                    {
                        item = (JMeterGUIComponent) c.newInstance();
                    }
                }
                catch (Throwable e)
                {
                    log.debug("Could not instantiate "+name, e);
                    continue;
                }
                if (elementsToSkip.contains(name)
                    || elementsToSkip.contains(item.getStaticLabel()))
                {
                    log.debug("Skipping "+name);
                    continue;
                }
                else
                {
                    elementsToSkip.add(name);
                }
                Collection categories = item.getMenuCategories();
                if (categories == null)
                {
                    log.debug(name+" participates in no menus.");
                    continue;
                }
                if (categories.contains(TIMERS))
                {
                    timers.add(
                        new MenuInfo(
                            item.getStaticLabel(),
                            name));
                }

                if (categories.contains(POST_PROCESSORS))
                {
                    postProcessors.add(
                        new MenuInfo(
                            item.getStaticLabel(),
                            name));
                }

                if (categories.contains(PRE_PROCESSORS))
                {
                    preProcessors.add(
                        new MenuInfo(
                            item.getStaticLabel(),
                            name));
                }

                if (categories.contains(CONTROLLERS))
                {
                    controllers.add(
                        new MenuInfo(
                            item.getStaticLabel(),
                            name));
                }

                if (categories.contains(SAMPLERS))
                {
                    samplers.add(
                        new MenuInfo(
                            item.getStaticLabel(),
                            name));
                }

                if (categories.contains(NON_TEST_ELEMENTS))
                {
                    nonTestElements.add(
                        new MenuInfo(
                            item.getStaticLabel(),
                            name));
                }

                if (categories.contains(LISTENERS))
                {
                    listeners.add(
                        new MenuInfo(
                            item.getStaticLabel(),
                            name));
                }

                if (categories.contains(CONFIG_ELEMENTS))
                {
                    configElements.add(
                        new MenuInfo(
                            item.getStaticLabel(),
                            name));
                }
                if (categories.contains(ASSERTIONS))
                {
                    assertions.add(
                        new MenuInfo(
                            item.getStaticLabel(),
                            name));
                }

            }
        }
        catch (Exception e)
        {
            log.error("", e);
        }
    }

    private static void addSeparator(JPopupMenu menu)
    {
        MenuElement[] elements = menu.getSubElements();
        if ((elements.length > 0)
            && !(elements[elements.length - 1] instanceof JPopupMenu.Separator))
        {
            menu.addSeparator();
        }
    }
}
