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
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.UIManager;

import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.LocaleChangeEvent;
import org.apache.jmeter.util.LocaleChangeListener;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class JMeterMenuBar extends JMenuBar implements LocaleChangeListener {
	private static final Logger log = LoggingManager.getLoggerForClass();

    private JMenu fileMenu;

    private JMenuItem file_save_as;

    private JMenuItem file_load;

    private JMenuItem file_merge;

    private JMenuItem file_exit;

    private JMenuItem file_close;

    private JMenu editMenu;

    private JMenu edit_add;

	// JMenu edit_add_submenu;
    private JMenuItem edit_remove; // TODO - should this be created?

    private JMenu runMenu;

    private JMenuItem run_start;

    private JMenu remote_start;

    private JMenuItem remote_start_all;

    private Collection remote_engine_start;

    private JMenuItem run_stop;

	private JMenuItem run_shut;
    
    private JMenu remote_stop;

    private JMenuItem remote_stop_all;

    private Collection remote_engine_stop;

    private JMenuItem run_clear;

    private JMenuItem run_clearAll;

	// JMenu reportMenu;
	// JMenuItem analyze;
    private JMenu optionsMenu;

    private JMenu lafMenu;

    private JMenuItem sslManager;

    private JMenu helpMenu;

    private JMenuItem help_about;

    private String[] remoteHosts;

	private JMenu remote_exit;

	private JMenuItem remote_exit_all;

	private Collection remote_engine_exit;

	public JMeterMenuBar() {
		remote_engine_start = new LinkedList();
		remote_engine_stop = new LinkedList();
		remote_engine_exit = new LinkedList();
		remoteHosts = JOrphanUtils.split(JMeterUtils.getPropDefault("remote_hosts", ""), ","); //$NON-NLS-1$
		if (remoteHosts.length == 1 && remoteHosts[0].equals("")) {
			remoteHosts = new String[0];
		}
		this.getRemoteItems();
		createMenuBar();
	}

	public void setFileSaveEnabled(boolean enabled) {
		file_save_as.setEnabled(enabled);
	}

	public void setFileLoadEnabled(boolean enabled) {
		if (file_load != null) {
			file_load.setEnabled(enabled);
		}
		if (file_merge != null) {
			file_merge.setEnabled(enabled);
		}
	}

	public void setEditEnabled(boolean enabled) {
		if (editMenu != null) {
			editMenu.setEnabled(enabled);
		}
	}

	public void setEditAddMenu(JMenu menu) {
		// If the Add menu already exists, remove it.
		if (edit_add != null) {
			editMenu.remove(edit_add);
		}
		// Insert the Add menu as the first menu item in the Edit menu.
		edit_add = menu;
		editMenu.insert(edit_add, 0);
	}

	public void setEditMenu(JPopupMenu menu) {
		if (menu != null) {
			editMenu.removeAll();
			Component[] comps = menu.getComponents();
			for (int i = 0; i < comps.length; i++) {
				editMenu.add(comps[i]);
			}
			editMenu.setEnabled(true);
		} else {
			editMenu.setEnabled(false);
		}
	}

	public void setEditAddEnabled(boolean enabled) {
		// There was a NPE being thrown without the null check here.. JKB
		if (edit_add != null) {
			edit_add.setEnabled(enabled);
		}
		// If we are enabling the Edit-->Add menu item, then we also need to
		// enable the Edit menu. The Edit menu may already be enabled, but
		// there's no harm it trying to enable it again.
		if (enabled) {
			setEditEnabled(true);
		} else {
			// If we are disabling the Edit-->Add menu item and the
			// Edit-->Remove menu item is disabled, then we also need to
			// disable the Edit menu.
			// The Java Look and Feel Guidelines say to disable a menu if all
			// menu items are disabled.
			if (!edit_remove.isEnabled()) {
				editMenu.setEnabled(false);
			}
		}
	}

	public void setEditRemoveEnabled(boolean enabled) {
		edit_remove.setEnabled(enabled);
		// If we are enabling the Edit-->Remove menu item, then we also need to
		// enable the Edit menu. The Edit menu may already be enabled, but
		// there's no harm it trying to enable it again.
		if (enabled) {
			setEditEnabled(true);
		} else {
			// If we are disabling the Edit-->Remove menu item and the
			// Edit-->Add menu item is disabled, then we also need to disable
			// the Edit menu.
			// The Java Look and Feel Guidelines say to disable a menu if all
			// menu items are disabled.
			if (!edit_add.isEnabled()) {
				editMenu.setEnabled(false);
			}
		}
	}

	/**
	 * Creates the MenuBar for this application. I believe in my heart that this
	 * should be defined in a file somewhere, but that is for later.
	 */
	public void createMenuBar() {
		makeFileMenu();
		makeEditMenu();
		makeRunMenu();
		makeOptionsMenu();
		makeHelpMenu();
		this.add(fileMenu);
		this.add(editMenu);
		this.add(runMenu);
		this.add(optionsMenu);
		this.add(helpMenu);
	}

	private void makeHelpMenu() {
		// HELP MENU
		helpMenu = new JMenu(JMeterUtils.getResString("help")); //$NON-NLS-1$
		helpMenu.setMnemonic('H');
		JMenuItem contextHelp = new JMenuItem(JMeterUtils.getResString("help"), 'H'); //$NON-NLS-1$
		contextHelp.setActionCommand(ActionNames.HELP);
		contextHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK));
		contextHelp.addActionListener(ActionRouter.getInstance());

        JMenuItem whatClass = new JMenuItem(JMeterUtils.getResString("help_node"), 'W');//$NON-NLS-1$
        whatClass.setActionCommand(ActionNames.WHAT_CLASS);
        whatClass.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
        whatClass.addActionListener(ActionRouter.getInstance());

        JMenuItem setDebug = new JMenuItem(JMeterUtils.getResString("debug_on"));//$NON-NLS-1$
        setDebug.setActionCommand(ActionNames.DEBUG_ON);
        setDebug.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
        setDebug.addActionListener(ActionRouter.getInstance());

        JMenuItem resetDebug = new JMenuItem(JMeterUtils.getResString("debug_off"));//$NON-NLS-1$
        resetDebug.setActionCommand(ActionNames.DEBUG_OFF);
        resetDebug.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK));
        resetDebug.addActionListener(ActionRouter.getInstance());

        help_about = new JMenuItem(JMeterUtils.getResString("about"), 'A'); //$NON-NLS-1$
		help_about.setActionCommand(ActionNames.ABOUT);
		help_about.addActionListener(ActionRouter.getInstance());
		helpMenu.add(contextHelp);
        helpMenu.addSeparator();
        helpMenu.add(whatClass);
        helpMenu.add(setDebug);
        helpMenu.add(resetDebug);
        helpMenu.addSeparator();
		helpMenu.add(help_about);
	}

	private void makeOptionsMenu() {
		// OPTIONS MENU
		optionsMenu = new JMenu(JMeterUtils.getResString("option")); //$NON-NLS-1$
		JMenuItem functionHelper = new JMenuItem(JMeterUtils.getResString("function_dialog_menu_item"), 'F'); //$NON-NLS-1$
		functionHelper.addActionListener(ActionRouter.getInstance());
		functionHelper.setActionCommand(ActionNames.FUNCTIONS);
		functionHelper.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));
		lafMenu = new JMenu(JMeterUtils.getResString("appearance")); //$NON-NLS-1$
		UIManager.LookAndFeelInfo lafs[] = UIManager.getInstalledLookAndFeels();
		for (int i = 0; i < lafs.length; ++i) {
			JMenuItem laf = new JMenuItem(lafs[i].getName());
			laf.addActionListener(ActionRouter.getInstance());
			laf.setActionCommand(ActionNames.LAF_PREFIX + lafs[i].getClassName());
			lafMenu.setMnemonic('L');
			lafMenu.add(laf);
		}
		optionsMenu.setMnemonic('O');
		optionsMenu.add(functionHelper);
		optionsMenu.add(lafMenu);
		if (SSLManager.isSSLSupported()) {
			sslManager = new JMenuItem(JMeterUtils.getResString("sslmanager")); //$NON-NLS-1$
			sslManager.addActionListener(ActionRouter.getInstance());
			sslManager.setActionCommand(ActionNames.SSL_MANAGER);
			sslManager.setMnemonic('S');
			sslManager.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_MASK));
			optionsMenu.add(sslManager);
		}
		optionsMenu.add(makeLanguageMenu());
	}

	// TODO fetch list of languages from a file?
	// N.B. Changes to language list need to be reflected in
	// resources/PackageTest.java
	private JMenu makeLanguageMenu() {
		/*
		 * Note: the item name is used by ChangeLanguage to create a Locale for
		 * that language, so need to ensure that the language strings are valid
		 * If they exist, use the Locale language constants
		 */
		// TODO: do accelerator keys make sense? The key may not be present in
		// translations
		JMenu languageMenu = new JMenu(JMeterUtils.getResString("choose_language")); //$NON-NLS-1$
		languageMenu.setMnemonic('C');
		// add english
		JMenuItem english = new JMenuItem(JMeterUtils.getResString("en"), 'E'); //$NON-NLS-1$
		english.addActionListener(ActionRouter.getInstance());
		english.setActionCommand(ActionNames.CHANGE_LANGUAGE);
		english.setName(Locale.ENGLISH.getLanguage());
		languageMenu.add(english);
		// add Japanese
		JMenuItem japanese = new JMenuItem(JMeterUtils.getResString("jp"), 'J'); //$NON-NLS-1$
		japanese.addActionListener(ActionRouter.getInstance());
		japanese.setActionCommand(ActionNames.CHANGE_LANGUAGE);
		japanese.setName(Locale.JAPANESE.getLanguage());
		languageMenu.add(japanese);
		// add Norwegian
		JMenuItem norway = new JMenuItem(JMeterUtils.getResString("no"), 'N'); //$NON-NLS-1$
		norway.addActionListener(ActionRouter.getInstance());
		norway.setActionCommand(ActionNames.CHANGE_LANGUAGE);
		norway.setName("no"); // No default for Norwegian
		languageMenu.add(norway);
		// add German
		JMenuItem german = new JMenuItem(JMeterUtils.getResString("de"), 'G'); //$NON-NLS-1$
		german.addActionListener(ActionRouter.getInstance());
		german.setActionCommand(ActionNames.CHANGE_LANGUAGE);
		german.setName(Locale.GERMAN.getLanguage());
		languageMenu.add(german);
		// add French
		JMenuItem french = new JMenuItem(JMeterUtils.getResString("fr"), 'F'); //$NON-NLS-1$
		french.addActionListener(ActionRouter.getInstance());
		french.setActionCommand(ActionNames.CHANGE_LANGUAGE);
		french.setName(Locale.FRENCH.getLanguage());
		languageMenu.add(french);
		// add chinese (simple)
		JMenuItem chineseSimple = new JMenuItem(JMeterUtils.getResString("zh_cn")); //$NON-NLS-1$
		chineseSimple.addActionListener(ActionRouter.getInstance());
		chineseSimple.setActionCommand(ActionNames.CHANGE_LANGUAGE);
		chineseSimple.setName(Locale.SIMPLIFIED_CHINESE.toString());
		languageMenu.add(chineseSimple);
		// add chinese (traditional)
		JMenuItem chineseTrad = new JMenuItem(JMeterUtils.getResString("zh_tw")); //$NON-NLS-1$
		chineseTrad.addActionListener(ActionRouter.getInstance());
		chineseTrad.setActionCommand(ActionNames.CHANGE_LANGUAGE);
		chineseTrad.setName(Locale.TRADITIONAL_CHINESE.toString());
		languageMenu.add(chineseTrad);
		// add spanish
		JMenuItem spanish = new JMenuItem(JMeterUtils.getResString("es")); //$NON-NLS-1$
		spanish.addActionListener(ActionRouter.getInstance());
		spanish.setActionCommand(ActionNames.CHANGE_LANGUAGE);
		spanish.setName("es"); //$NON-NLS-1$
		languageMenu.add(spanish);
		return languageMenu;
	}

	private void makeRunMenu() {
		// RUN MENU
		runMenu = new JMenu(JMeterUtils.getResString("run")); //$NON-NLS-1$
		runMenu.setMnemonic('R');
		run_start = new JMenuItem(JMeterUtils.getResString("start"), 'S'); //$NON-NLS-1$
		run_start.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
		run_start.addActionListener(ActionRouter.getInstance());
		run_start.setActionCommand(ActionNames.ACTION_START);
		run_stop = new JMenuItem(JMeterUtils.getResString("stop"), 'T'); //$NON-NLS-1$
		run_stop.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, KeyEvent.CTRL_MASK));
		run_stop.setEnabled(false);
		run_stop.addActionListener(ActionRouter.getInstance());
		run_stop.setActionCommand(ActionNames.ACTION_STOP);

		run_shut = new JMenuItem(JMeterUtils.getResString("shutdown"), 'Y'); //$NON-NLS-1$
		run_shut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, KeyEvent.CTRL_MASK));
		run_shut.setEnabled(false);
		run_shut.addActionListener(ActionRouter.getInstance());
		run_shut.setActionCommand(ActionNames.ACTION_SHUTDOWN);

		run_clear = new JMenuItem(JMeterUtils.getResString("clear"), 'C'); //$NON-NLS-1$
		run_clear.addActionListener(ActionRouter.getInstance());
		run_clear.setActionCommand(ActionNames.CLEAR);
		run_clearAll = new JMenuItem(JMeterUtils.getResString("clear_all"), 'a'); //$NON-NLS-1$
		run_clearAll.addActionListener(ActionRouter.getInstance());
		run_clearAll.setActionCommand(ActionNames.CLEAR_ALL);
		run_clearAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK));
		runMenu.add(run_start);
		if (remote_start != null) {
			runMenu.add(remote_start);
		}
		remote_start_all = new JMenuItem(JMeterUtils.getResString("remote_start_all"), 'Z'); //$NON-NLS-1$
		remote_start_all.setName("remote_start_all"); //$NON-NLS-1$
		remote_start_all.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
		remote_start_all.addActionListener(ActionRouter.getInstance());
		remote_start_all.setActionCommand(ActionNames.REMOTE_START_ALL);
		runMenu.add(remote_start_all);
		runMenu.add(run_stop);
		runMenu.add(run_shut);
		if (remote_stop != null) {
			runMenu.add(remote_stop);
		}
		remote_stop_all = new JMenuItem(JMeterUtils.getResString("remote_stop_all"), 'X'); //$NON-NLS-1$
		remote_stop_all.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.ALT_MASK));
		remote_stop_all.addActionListener(ActionRouter.getInstance());
		remote_stop_all.setActionCommand(ActionNames.REMOTE_STOP_ALL);
		runMenu.add(remote_stop_all);

		if (remote_exit != null) {
			runMenu.add(remote_exit);
		}
		remote_exit_all = new JMenuItem(JMeterUtils.getResString("remote_exit_all")); //$NON-NLS-1$
		remote_exit_all.addActionListener(ActionRouter.getInstance());
		remote_exit_all.setActionCommand(ActionNames.REMOTE_EXIT_ALL);
		runMenu.add(remote_exit_all);

		runMenu.addSeparator();
		runMenu.add(run_clear);
		runMenu.add(run_clearAll);
	}

	private void makeEditMenu() {
		// EDIT MENU
		editMenu = new JMenu(JMeterUtils.getResString("edit")); //$NON-NLS-1$
        editMenu.setMnemonic('E');
		// From the Java Look and Feel Guidelines: If all items in a menu
		// are disabled, then disable the menu. Makes sense.
		editMenu.setEnabled(false);
	}

	private void makeFileMenu() {
		// FILE MENU
		fileMenu = new JMenu(JMeterUtils.getResString("file")); //$NON-NLS-1$
		fileMenu.setMnemonic('F');
		JMenuItem file_save = new JMenuItem(JMeterUtils.getResString("save"), 'S'); //$NON-NLS-1$
		file_save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
		file_save.setActionCommand(ActionNames.SAVE);
		file_save.addActionListener(ActionRouter.getInstance());
		file_save.setEnabled(true);

		file_save_as = new JMenuItem(JMeterUtils.getResString("save_all_as"), 'A'); //$NON-NLS-1$
		file_save_as.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));
		file_save_as.setActionCommand(ActionNames.SAVE_ALL_AS);
		file_save_as.addActionListener(ActionRouter.getInstance());
		file_save_as.setEnabled(true);

		file_load = new JMenuItem(JMeterUtils.getResString("menu_open"), 'O'); //$NON-NLS-1$
		file_load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
		file_load.addActionListener(ActionRouter.getInstance());
		// Set default SAVE menu item to disabled since the default node that
		// is selected is ROOT, which does not allow items to be inserted.
		file_load.setEnabled(false);
		file_load.setActionCommand(ActionNames.OPEN);

		file_close = new JMenuItem(JMeterUtils.getResString("menu_close"), 'C'); //$NON-NLS-1$
		file_close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK));
		file_close.setActionCommand(ActionNames.CLOSE);
		file_close.addActionListener(ActionRouter.getInstance());

		file_exit = new JMenuItem(JMeterUtils.getResString("exit"), 'X'); //$NON-NLS-1$
		file_exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
		file_exit.setActionCommand(ActionNames.EXIT);
		file_exit.addActionListener(ActionRouter.getInstance());

		file_merge = new JMenuItem(JMeterUtils.getResString("menu_merge"), 'M'); //$NON-NLS-1$
		// file_merge.setAccelerator(
		// KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
		file_merge.addActionListener(ActionRouter.getInstance());
		// Set default SAVE menu item to disabled since the default node that
		// is selected is ROOT, which does not allow items to be inserted.
		file_merge.setEnabled(false);
		file_merge.setActionCommand(ActionNames.MERGE);

		fileMenu.add(file_close);
		fileMenu.add(file_load);
		fileMenu.add(file_merge);
		fileMenu.add(file_save);
		fileMenu.add(file_save_as);
        fileMenu.addSeparator();
        fileMenu.add(file_exit);
	}

	public void setRunning(boolean running, String host) {
		log.info("setRunning(" + running + "," + host + ")");

		Iterator iter = remote_engine_start.iterator();
		Iterator iter2 = remote_engine_stop.iterator();
		Iterator iter3 = remote_engine_exit.iterator();
		while (iter.hasNext() && iter2.hasNext() && iter3.hasNext()) {
			JMenuItem start = (JMenuItem) iter.next();
			JMenuItem stop = (JMenuItem) iter2.next();
			JMenuItem exit = (JMenuItem) iter3.next();
			if (start.getText().equals(host)) {
				log.debug("Found start host: " + start.getText());
				start.setEnabled(!running);
			}
			if (stop.getText().equals(host)) {
				log.debug("Found stop  host: " + stop.getText());
				stop.setEnabled(running);
			}
			if (exit.getText().equals(host)) {
				log.debug("Found exit  host: " + exit.getText());
				exit.setEnabled(true);
			}
		}
	}

	public void setEnabled(boolean enable) {
		run_start.setEnabled(!enable);
		run_stop.setEnabled(enable);
		run_shut.setEnabled(enable);
	}

	private void getRemoteItems() {
		if (remoteHosts.length > 0) {
			remote_start = new JMenu(JMeterUtils.getResString("remote_start")); //$NON-NLS-1$
			remote_stop = new JMenu(JMeterUtils.getResString("remote_stop")); //$NON-NLS-1$
			remote_exit = new JMenu(JMeterUtils.getResString("remote_exit")); //$NON-NLS-1$

			for (int i = 0; i < remoteHosts.length; i++) {
				remoteHosts[i] = remoteHosts[i].trim();
				JMenuItem item = new JMenuItem(remoteHosts[i]);
				item.setActionCommand(ActionNames.REMOTE_START);
				item.setName(remoteHosts[i]);
				item.addActionListener(ActionRouter.getInstance());
				remote_engine_start.add(item);
				remote_start.add(item);
				item = new JMenuItem(remoteHosts[i]);
				item.setActionCommand(ActionNames.REMOTE_STOP);
				item.setName(remoteHosts[i]);
				item.addActionListener(ActionRouter.getInstance());
				item.setEnabled(false);
				remote_engine_stop.add(item);
				remote_stop.add(item);
				item = new JMenuItem(remoteHosts[i]);
				item.setActionCommand(ActionNames.REMOTE_EXIT);
				item.setName(remoteHosts[i]);
				item.addActionListener(ActionRouter.getInstance());
				item.setEnabled(false);
				remote_engine_exit.add(item);
				remote_exit.add(item);
			}
		}
	}

	/**
	 * Processes a locale change notification. Changes the texts in all menus to
	 * the new language.
	 */
	public void localeChanged(LocaleChangeEvent event) {
		updateMenuElement(fileMenu);
		updateMenuElement(editMenu);
		updateMenuElement(runMenu);
		updateMenuElement(optionsMenu);
		updateMenuElement(helpMenu);
	}

	/**
	 * Refreshes all texts in the menu and all submenus to a new locale.
	 */
	private void updateMenuElement(MenuElement menu) {
		Component component = menu.getComponent();

		if (component.getName() != null) {
			if (component instanceof JMenu) {
				((JMenu) component).setText(JMeterUtils.getResString(component.getName()));
			} else {
				((JMenuItem) component).setText(JMeterUtils.getResString(component.getName()));
			}
		}

		MenuElement[] subelements = menu.getSubElements();

		for (int i = 0; i < subelements.length; i++) {
			updateMenuElement(subelements[i]);
		}
	}
}
