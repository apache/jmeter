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

import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.report.gui.action.ReportActionRouter;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Printable;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * @author Peter Lin
 * @version $Revision$ updated on $Date$
 */
public final class ReportMenuFactory {
	transient private static Logger log = LoggingManager.getLoggerForClass();

	public final static String TIMERS = "menu_timer";

	public final static String CONTROLLERS = "menu_logic_controller";

	public final static String CONFIG_ELEMENTS = "menu_config_element";

	public final static String POST_PROCESSORS = "menu_post_processors";

	public final static String PRE_PROCESSORS = "menu_pre_processors";

	public final static String NON_TEST_ELEMENTS = "menu_non_test_elements";

	public final static String LISTENERS = "menu_listener";
	
	public final static String REPORT_PAGE = "menu_report_page";
	
	public final static String TABLES = "menu_tables";

	private static Map menuMap = new HashMap();

	private static Set elementsToSkip = new HashSet();

	// MENU_ADD_xxx - controls which items are in the ADD menu
	// MENU_PARENT_xxx - controls which items are in the Insert Parent menu
	private static final String[] MENU_ADD_CONTROLLER = new String[] { ReportMenuFactory.CONTROLLERS,
			ReportMenuFactory.CONFIG_ELEMENTS, ReportMenuFactory.TIMERS, ReportMenuFactory.LISTENERS,
			ReportMenuFactory.PRE_PROCESSORS, ReportMenuFactory.POST_PROCESSORS };

	private static final String[] MENU_PARENT_CONTROLLER = new String[] { ReportMenuFactory.CONTROLLERS };

//	private static final String[] MENU_ADD_REPORT_PAGE = new String[] { ReportMenuFactory.CONFIG_ELEMENTS,
//			ReportMenuFactory.PRE_PROCESSORS, ReportMenuFactory.POST_PROCESSORS,
//			ReportMenuFactory.TABLES };
//	
//	private static final String[] MENU_ADD_TABLES = new String[] { ReportMenuFactory.TABLES };
//
//	private static final String[] MENU_PARENT_SAMPLER = new String[] { ReportMenuFactory.CONTROLLERS };

	private static List controllers, configElements, listeners, nonTestElements,
			postProcessors, preProcessors, reportPage, tables;

	// private static JMenu timerMenu;
	// private static JMenu controllerMenu;
	// private static JMenu generativeControllerMenu;
	// private static JMenu listenerMenu;
	// private static JMenu assertionMenu;
	// private static JMenu configMenu;
	// private static JMenu insertControllerMenu;
	// private static JMenu postProcessorMenu;
	// private static JMenu preProcessorMenu;

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

	public static String doNothing() {
		return "doing nothing";
	}

	public static void addEditMenu(JPopupMenu menu, boolean removable) {
		addSeparator(menu);
		if (removable) {
			menu.add(makeMenuItem(JMeterUtils.getResString("remove"), "Remove", "remove", KeyStroke.getKeyStroke(
					KeyEvent.VK_DELETE, 0)));
		}
		menu.add(makeMenuItem(JMeterUtils.getResString("cut"), "Cut", "Cut", KeyStroke.getKeyStroke(KeyEvent.VK_X,
				KeyEvent.CTRL_MASK)));
		menu.add(makeMenuItem(JMeterUtils.getResString("copy"), "Copy", "Copy", KeyStroke.getKeyStroke(KeyEvent.VK_C,
				KeyEvent.CTRL_MASK)));
		menu.add(makeMenuItem(JMeterUtils.getResString("paste"), "Paste", "Paste", KeyStroke.getKeyStroke(
				KeyEvent.VK_V, KeyEvent.CTRL_MASK)));
		menu.add(makeMenuItem(JMeterUtils.getResString("paste_insert"), "Paste Insert", "Paste Insert"));
	}

	public static void addFileMenu(JPopupMenu menu) {
		addSeparator(menu);
		menu.add(makeMenuItem(JMeterUtils.getResString("open"), "Open", "open"));
		menu.add(makeMenuItem(JMeterUtils.getResString("save_as"), "Save As", "save_as"));
		JMenuItem savePicture = makeMenuItem(JMeterUtils.getResString("save_as_image"), "Save Image", "save_graphics",
				KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK));
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
		pop.add(MenuFactory.makeMenus(MENU_ADD_CONTROLLER, JMeterUtils.getResString("Add"),// $NON-NLS-1$
				"Add"));
		pop.add(makeMenus(MENU_PARENT_CONTROLLER, JMeterUtils.getResString("insert_parent"),// $NON-NLS-1$
				"Add Parent"));
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
		return makeMenu((Collection) menuMap.get(category), actionCommand, JMeterUtils.getResString(category));
	}

	public static JMenu makeMenu(Collection menuInfo, String actionCommand, String menuName) {
		Iterator iter = menuInfo.iterator();
		JMenu menu = new JMenu(menuName);
		while (iter.hasNext()) {
			MenuInfo info = (MenuInfo) iter.next();
			menu.add(makeMenuItem(info.label, info.className, actionCommand));
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
			List guiClasses = ClassFinder.findClassesThatExtend(JMeterUtils.getSearchPaths(), new Class[] {
					JMeterGUIComponent.class, TestBean.class });
			controllers = new LinkedList();
			configElements = new LinkedList();
			listeners = new LinkedList();
			postProcessors = new LinkedList();
			preProcessors = new LinkedList();
			tables = new LinkedList();
			reportPage = new LinkedList();
			nonTestElements = new LinkedList();
			menuMap.put(CONFIG_ELEMENTS, configElements);
			menuMap.put(CONTROLLERS, controllers);
			menuMap.put(LISTENERS, listeners);
			menuMap.put(NON_TEST_ELEMENTS, nonTestElements);
			menuMap.put(POST_PROCESSORS, postProcessors);
			menuMap.put(PRE_PROCESSORS, preProcessors);
			menuMap.put(REPORT_PAGE, reportPage);
			menuMap.put(TABLES, tables);
			Collections.sort(guiClasses);
			Iterator iter = guiClasses.iterator();
			while (iter.hasNext()) {
				String name = (String) iter.next();

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
					Class c = Class.forName(name);
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
				Collection categories = item.getMenuCategories();
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
		} catch (Exception e) {
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
