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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.UIManager;

import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.KeyStrokes;
import org.apache.jmeter.report.gui.action.ReportActionRouter;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.LocaleChangeEvent;
import org.apache.jmeter.util.LocaleChangeListener;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * This is a version of the MenuBar for the reporting tool. I started
 * with the existing jmeter menubar.
 */
public class ReportMenuBar extends JMenuBar implements LocaleChangeListener {
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private JMenu fileMenu;

    private JMenuItem file_save_as;

    private JMenuItem file_load;

    private JMenuItem file_merge;

    private JMenuItem file_exit;

    private JMenuItem file_close;

    private JMenu editMenu;

    private JMenu edit_add;

    private JMenu runMenu;

    private JMenuItem run_start;

    private JMenu remote_start;

    private JMenuItem remote_start_all;

    private final Collection<JMenuItem> remote_engine_start;

    private JMenuItem run_stop;

    private JMenuItem run_shut; // all the others could be private too?

    private JMenu remote_stop;

    private JMenuItem remote_stop_all;

    private final Collection<JMenuItem> remote_engine_stop;

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

    private final Collection<JMenuItem> remote_engine_exit;

    public ReportMenuBar() {
        remote_engine_start = new LinkedList<JMenuItem>();
        remote_engine_stop = new LinkedList<JMenuItem>();
        remote_engine_exit = new LinkedList<JMenuItem>();
        remoteHosts = JOrphanUtils.split(JMeterUtils.getPropDefault("remote_hosts", ""), ",");
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
            // editMenu.setEnabled(false);
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
        setEditEnabled(enabled);
    }

    public void setEditRemoveEnabled(boolean enabled) {
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
        helpMenu = new JMenu(JMeterUtils.getResString("help")); // $NON-NLS-1$
        helpMenu.setMnemonic('H');
        JMenuItem contextHelp = new JMenuItem(JMeterUtils.getResString("help"), 'H'); // $NON-NLS-1$
        contextHelp.setActionCommand("help");
        contextHelp.setAccelerator(KeyStrokes.HELP);
        contextHelp.addActionListener(ReportActionRouter.getInstance());
        help_about = new JMenuItem(JMeterUtils.getResString("about"), 'A'); // $NON-NLS-1$
        help_about.setActionCommand("about");
        help_about.addActionListener(ReportActionRouter.getInstance());
        helpMenu.add(contextHelp);
        helpMenu.add(help_about);
    }

    private void makeOptionsMenu() {
        // OPTIONS MENU
        optionsMenu = new JMenu(JMeterUtils.getResString("option")); // $NON-NLS-1$
        JMenuItem functionHelper = new JMenuItem(JMeterUtils.getResString("function_dialog_menu_item"), 'F'); // $NON-NLS-1$
        functionHelper.addActionListener(ReportActionRouter.getInstance());
        functionHelper.setActionCommand("functions"); // $NON-NLS-1$
        functionHelper.setAccelerator(KeyStrokes.FUNCTIONS);
        lafMenu = new JMenu(JMeterUtils.getResString("appearance")); // $NON-NLS-1$
        UIManager.LookAndFeelInfo lafs[] = UIManager.getInstalledLookAndFeels();
        for (int i = 0; i < lafs.length; ++i) {
            JMenuItem laf = new JMenuItem(lafs[i].getName());
            laf.addActionListener(ReportActionRouter.getInstance());
            laf.setActionCommand("laf:" + lafs[i].getClassName());
            lafMenu.setMnemonic('L');
            lafMenu.add(laf);
        }
        optionsMenu.setMnemonic('O');
        optionsMenu.add(functionHelper);
        optionsMenu.add(lafMenu);
        if (SSLManager.isSSLSupported()) {
            sslManager = new JMenuItem(JMeterUtils.getResString("sslmanager")); // $NON-NLS-1$
            sslManager.addActionListener(ReportActionRouter.getInstance());
            sslManager.setActionCommand("sslManager"); // $NON-NLS-1$
            sslManager.setMnemonic('S');
            sslManager.setAccelerator(KeyStrokes.SSL_MANAGER);
            optionsMenu.add(sslManager);
        }
        optionsMenu.add(makeLanguageMenu());
    }

    // TODO fetch list of languages from a file?
    // N.B. Changes to language list need to be reflected in
    // resources/PackageTest.java
    private JMenu makeLanguageMenu() {
        return JMeterMenuBar.makeLanguageMenu();
    }

    /*
     * Strings used to set up and process actions in this menu The strings need
     * to agree with the those in the Action routines
     */
    public static final String ACTION_SHUTDOWN = "shutdown"; // $NON-NLS-1$

    public static final String ACTION_STOP = "stop"; // $NON-NLS-1$

    public static final String ACTION_START = "start"; // $NON-NLS-1$

    private void makeRunMenu() {
        // RUN MENU
        runMenu = new JMenu(JMeterUtils.getResString("run")); // $NON-NLS-1$
        runMenu.setMnemonic('R');
        run_start = new JMenuItem(JMeterUtils.getResString("start"), 'S'); // $NON-NLS-1$
        run_start.setAccelerator(KeyStrokes.ACTION_START);
        run_start.addActionListener(ReportActionRouter.getInstance());
        run_start.setActionCommand(ACTION_START);
        run_stop = new JMenuItem(JMeterUtils.getResString("stop"), 'T'); // $NON-NLS-1$
        run_stop.setAccelerator(KeyStrokes.ACTION_STOP);
        run_stop.setEnabled(false);
        run_stop.addActionListener(ReportActionRouter.getInstance());
        run_stop.setActionCommand(ACTION_STOP);

        run_shut = new JMenuItem(JMeterUtils.getResString("shutdown"), 'Y'); // $NON-NLS-1$
        run_shut.setAccelerator(KeyStrokes.ACTION_SHUTDOWN);
        run_shut.setEnabled(false);
        run_shut.addActionListener(ReportActionRouter.getInstance());
        run_shut.setActionCommand(ACTION_SHUTDOWN);

        run_clear = new JMenuItem(JMeterUtils.getResString("clear"), 'C'); // $NON-NLS-1$
        run_clear.addActionListener(ReportActionRouter.getInstance());
        run_clear.setActionCommand(ActionNames.CLEAR);
        run_clearAll = new JMenuItem(JMeterUtils.getResString("clear_all"), 'a'); // $NON-NLS-1$
        run_clearAll.addActionListener(ReportActionRouter.getInstance());
        run_clearAll.setActionCommand(ActionNames.CLEAR_ALL);
        run_clearAll.setAccelerator(KeyStrokes.CLEAR_ALL);
        runMenu.add(run_start);
        if (remote_start != null) {
            runMenu.add(remote_start);
        }
        remote_start_all = new JMenuItem(JMeterUtils.getResString("remote_start_all"), 'Z'); // $NON-NLS-1$
        remote_start_all.setName("remote_start_all");
        remote_start_all.setAccelerator(KeyStrokes.REMOTE_START_ALL);
        remote_start_all.addActionListener(ReportActionRouter.getInstance());
        remote_start_all.setActionCommand("remote_start_all");
        runMenu.add(remote_start_all);
        runMenu.add(run_stop);
        runMenu.add(run_shut);
        if (remote_stop != null) {
            runMenu.add(remote_stop);
        }
        remote_stop_all = new JMenuItem(JMeterUtils.getResString("remote_stop_all"), 'X'); // $NON-NLS-1$
        remote_stop_all.setAccelerator(KeyStrokes.REMOTE_STOP_ALL);
        remote_stop_all.addActionListener(ReportActionRouter.getInstance());
        remote_stop_all.setActionCommand("remote_stop_all");
        runMenu.add(remote_stop_all);

        if (remote_exit != null) {
            runMenu.add(remote_exit);
        }
        remote_exit_all = new JMenuItem(JMeterUtils.getResString("remote_exit_all")); // $NON-NLS-1$
        remote_exit_all.addActionListener(ReportActionRouter.getInstance());
        remote_exit_all.setActionCommand("remote_exit_all");
        runMenu.add(remote_exit_all);

        runMenu.addSeparator();
        runMenu.add(run_clear);
        runMenu.add(run_clearAll);
    }

    private void makeEditMenu() {
        // EDIT MENU
        editMenu = new JMenu(JMeterUtils.getResString("edit")); // $NON-NLS-1$
        // From the Java Look and Feel Guidelines: If all items in a menu
        // are disabled, then disable the menu. Makes sense.
        editMenu.setEnabled(false);
    }

    private void makeFileMenu() {
        // FILE MENU
        fileMenu = new JMenu(JMeterUtils.getResString("file")); // $NON-NLS-1$
        fileMenu.setMnemonic('F');
        JMenuItem file_save = new JMenuItem(JMeterUtils.getResString("save"), 'S'); // $NON-NLS-1$
        file_save.setAccelerator(KeyStrokes.SAVE);
        file_save.setActionCommand("save"); // $NON-NLS-1$
        file_save.addActionListener(ReportActionRouter.getInstance());
        file_save.setEnabled(true);

        file_save_as = new JMenuItem(JMeterUtils.getResString("save_all_as"), 'A'); // $NON-NLS-1$
        file_save_as.setAccelerator(KeyStrokes.SAVE_ALL_AS);
        file_save_as.setActionCommand("save_all_as"); // $NON-NLS-1$
        file_save_as.addActionListener(ReportActionRouter.getInstance());
        file_save_as.setEnabled(true);

        file_load = new JMenuItem(JMeterUtils.getResString("menu_open"), 'O'); // $NON-NLS-1$
        file_load.setAccelerator(KeyStrokes.OPEN);
        file_load.addActionListener(ReportActionRouter.getInstance());
        // Set default SAVE menu item to disabled since the default node that
        // is selected is ROOT, which does not allow items to be inserted.
        file_load.setEnabled(false);
        file_load.setActionCommand("open"); // $NON-NLS-1$

        file_close = new JMenuItem(JMeterUtils.getResString("menu_close"), 'C'); // $NON-NLS-1$
        file_close.setAccelerator(KeyStrokes.CLOSE);
        file_close.setActionCommand("close"); // $NON-NLS-1$
        file_close.addActionListener(ReportActionRouter.getInstance());

        file_exit = new JMenuItem(JMeterUtils.getResString("exit"), 'X'); // $NON-NLS-1$
        file_exit.setAccelerator(KeyStrokes.EXIT);
        file_exit.setActionCommand("exit"); // $NON-NLS-1$
        file_exit.addActionListener(ReportActionRouter.getInstance());

        file_merge = new JMenuItem(JMeterUtils.getResString("menu_merge"), 'M'); // $NON-NLS-1$
        // file_merge.setAccelerator(
        // KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        file_merge.addActionListener(ReportActionRouter.getInstance());
        // Set default SAVE menu item to disabled since the default node that
        // is selected is ROOT, which does not allow items to be inserted.
        file_merge.setEnabled(false);
        file_merge.setActionCommand("merge"); // $NON-NLS-1$

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

        Iterator<JMenuItem> iter = remote_engine_start.iterator();
        Iterator<JMenuItem> iter2 = remote_engine_stop.iterator();
        Iterator<JMenuItem> iter3 = remote_engine_exit.iterator();
        while (iter.hasNext() && iter2.hasNext() && iter3.hasNext()) {
            JMenuItem start = iter.next();
            JMenuItem stop = iter2.next();
            JMenuItem exit = iter3.next();
            if (start.getText().equals(host)) {
                log.info("Found start host: " + start.getText());
                start.setEnabled(!running);
            }
            if (stop.getText().equals(host)) {
                log.info("Found stop  host: " + stop.getText());
                stop.setEnabled(running);
            }
            if (exit.getText().equals(host)) {
                log.info("Found exit  host: " + exit.getText());
                exit.setEnabled(true);
            }
        }
    }

    @Override
    public void setEnabled(boolean enable) {
        run_start.setEnabled(!enable);
        run_stop.setEnabled(enable);
        run_shut.setEnabled(enable);
    }

    private void getRemoteItems() {
        if (remoteHosts.length > 0) {
            remote_start = new JMenu(JMeterUtils.getResString("remote_start")); // $NON-NLS-1$
            remote_stop = new JMenu(JMeterUtils.getResString("remote_stop")); // $NON-NLS-1$
            remote_exit = new JMenu(JMeterUtils.getResString("remote_exit")); // $NON-NLS-1$

            for (int i = 0; i < remoteHosts.length; i++) {
                remoteHosts[i] = remoteHosts[i].trim();
                JMenuItem item = new JMenuItem(remoteHosts[i]);
                item.setActionCommand("remote_start"); // $NON-NLS-1$
                item.setName(remoteHosts[i]);
                item.addActionListener(ReportActionRouter.getInstance());
                remote_engine_start.add(item);
                remote_start.add(item);
                item = new JMenuItem(remoteHosts[i]);
                item.setActionCommand("remote_stop"); // $NON-NLS-1$
                item.setName(remoteHosts[i]);
                item.addActionListener(ReportActionRouter.getInstance());
                item.setEnabled(false);
                remote_engine_stop.add(item);
                remote_stop.add(item);
                item = new JMenuItem(remoteHosts[i]);
                item.setActionCommand("remote_exit"); // $NON-NLS-1$
                item.setName(remoteHosts[i]);
                item.addActionListener(ReportActionRouter.getInstance());
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
    @Override
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
            ((JMenuItem) component).setText(JMeterUtils.getResString(component.getName()));
        }

        MenuElement[] subelements = menu.getSubElements();

        for (int i = 0; i < subelements.length; i++) {
            updateMenuElement(subelements[i]);
        }
    }
}
