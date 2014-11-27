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
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.KeyStrokes;
import org.apache.jmeter.gui.action.LoadRecentProject;
import org.apache.jmeter.gui.plugin.MenuCreator;
import org.apache.jmeter.gui.plugin.MenuCreator.MENU_LOCATION;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.LocaleChangeEvent;
import org.apache.jmeter.util.LocaleChangeListener;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

public class JMeterMenuBar extends JMenuBar implements LocaleChangeListener {
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private JMenu fileMenu;

    private JMenuItem file_save_as;

    private JMenuItem file_selection_as;

    private JMenuItem file_selection_as_test_fragment;

    private JMenuItem file_revert;

    private JMenuItem file_load;

    private JMenuItem templates;

    private List<JComponent> file_load_recent_files;

    private JMenuItem file_merge;

    private JMenuItem file_exit;

    private JMenuItem file_close;

    private JMenu editMenu;

    private JMenu edit_add;

    private JMenu runMenu;

    private JMenuItem run_start;

    private JMenuItem run_start_no_timers;

    private JMenu remote_start;

    private JMenuItem remote_start_all;

    private Collection<JMenuItem> remote_engine_start;

    private JMenuItem run_stop;

    private JMenuItem run_shut;

    private JMenu remote_stop;

    private JMenu remote_shut;

    private JMenuItem remote_stop_all;

    private JMenuItem remote_shut_all;

    private Collection<JMenuItem> remote_engine_stop;

    private Collection<JMenuItem> remote_engine_shut;

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

    private Collection<JMenuItem> remote_engine_exit;

    private JMenu searchMenu;

    private ArrayList<MenuCreator> menuCreators;

    public static final String SYSTEM_LAF = "System"; // $NON-NLS-1$

    public static final String CROSS_PLATFORM_LAF = "CrossPlatform"; // $NON-NLS-1$

    public JMeterMenuBar() {
        // List for recent files menu items
        file_load_recent_files = new LinkedList<JComponent>();
        // Lists for remote engines menu items
        remote_engine_start = new LinkedList<JMenuItem>();
        remote_engine_stop = new LinkedList<JMenuItem>();
        remote_engine_shut = new LinkedList<JMenuItem>();
        remote_engine_exit = new LinkedList<JMenuItem>();
        remoteHosts = JOrphanUtils.split(JMeterUtils.getPropDefault("remote_hosts", ""), ","); //$NON-NLS-1$
        if (remoteHosts.length == 1 && remoteHosts[0].equals("")) {
            remoteHosts = new String[0];
        }
        this.getRemoteItems();
        createMenuBar();
        JMeterUtils.addLocaleChangeListener(this);
    }

    public void setFileSaveEnabled(boolean enabled) {
        if(file_save_as != null) {
            file_save_as.setEnabled(enabled);
        }
    }

    public void setFileLoadEnabled(boolean enabled) {
        if (file_load != null) {
            file_load.setEnabled(enabled);
        }
        if (file_merge != null) {
            file_merge.setEnabled(enabled);
        }
    }

    public void setFileRevertEnabled(boolean enabled) {
        if(file_revert != null) {
            file_revert.setEnabled(enabled);
        }
    }

    public void setProjectFileLoaded(String file) {
        if(file_load_recent_files != null && file != null) {
            LoadRecentProject.updateRecentFileMenuItems(file_load_recent_files, file);
        }
    }

    public void setEditEnabled(boolean enabled) {
        if (editMenu != null) {
            editMenu.setEnabled(enabled);
        }
    }

    // Does not appear to be used; called by MainFrame#setEditAddMenu() but that is not called
    public void setEditAddMenu(JMenu menu) {
        // If the Add menu already exists, remove it.
        if (edit_add != null) {
            editMenu.remove(edit_add);
        }
        // Insert the Add menu as the first menu item in the Edit menu.
        edit_add = menu;
        editMenu.insert(edit_add, 0);
    }

    // Called by MainFrame#setEditMenu() which is called by EditCommand#doAction and GuiPackage#localeChanged
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
        setEditEnabled(enabled);
    }

    /**
     * Creates the MenuBar for this application. I believe in my heart that this
     * should be defined in a file somewhere, but that is for later.
     */
    public void createMenuBar() {
        this.menuCreators = new ArrayList<MenuCreator>();
        try {
            List<String> listClasses = ClassFinder.findClassesThatExtend(
                    JMeterUtils.getSearchPaths(), 
                    new Class[] {MenuCreator.class }); 
            for (String strClassName : listClasses) {
                try {
                    if(log.isDebugEnabled()) {
                        log.debug("Loading menu creator class: "+ strClassName);
                    }
                    Class<?> commandClass = Class.forName(strClassName);
                    if (!Modifier.isAbstract(commandClass.getModifiers())) {
                        if(log.isDebugEnabled()) {
                            log.debug("Instantiating: "+ commandClass.getName());
                        }
                        MenuCreator creator = (MenuCreator) commandClass.newInstance();
                        menuCreators.add(creator);                  
                    }
                } catch (Exception e) {
                    log.error("Exception registering "+MenuCreator.class.getName() + " with implementation:"+strClassName, e);
                }
            }
        } catch (IOException e) {
            log.error("Exception finding implementations of "+MenuCreator.class, e);
        }

        makeFileMenu();
        makeEditMenu();
        makeRunMenu();
        makeOptionsMenu();
        makeHelpMenu();
        makeSearchMenu();
        this.add(fileMenu);
        this.add(editMenu);
        this.add(searchMenu);
        this.add(runMenu);
        this.add(optionsMenu);
        for (Iterator<MenuCreator> iterator = menuCreators.iterator(); iterator.hasNext();) {
            MenuCreator menuCreator = iterator.next();
            JMenu[] topLevelMenus = menuCreator.getTopLevelMenus();
            for (JMenu topLevelMenu : topLevelMenus) {
                this.add(topLevelMenu);                
            }
        }
        this.add(helpMenu);
    }

    private void makeHelpMenu() {
        // HELP MENU
        helpMenu = makeMenuRes("help",'H'); //$NON-NLS-1$

        JMenuItem contextHelp = makeMenuItemRes("help", 'H', ActionNames.HELP, KeyStrokes.HELP); //$NON-NLS-1$

        JMenuItem whatClass = makeMenuItemRes("help_node", 'W', ActionNames.WHAT_CLASS, KeyStrokes.WHAT_CLASS);//$NON-NLS-1$

        JMenuItem setDebug = makeMenuItemRes("debug_on", ActionNames.DEBUG_ON, KeyStrokes.DEBUG_ON);//$NON-NLS-1$

        JMenuItem resetDebug = makeMenuItemRes("debug_off", ActionNames.DEBUG_OFF, KeyStrokes.DEBUG_OFF);//$NON-NLS-1$

        JMenuItem heapDump = makeMenuItemRes("heap_dump", ActionNames.HEAP_DUMP);//$NON-NLS-1$

        help_about = makeMenuItemRes("about", 'A', ActionNames.ABOUT); //$NON-NLS-1$

        helpMenu.add(contextHelp);
        helpMenu.addSeparator();
        helpMenu.add(whatClass);
        helpMenu.add(setDebug);
        helpMenu.add(resetDebug);
        helpMenu.add(heapDump);

        addPluginsMenuItems(helpMenu, menuCreators, MENU_LOCATION.HELP);

        helpMenu.addSeparator();
        helpMenu.add(help_about);
    }

    private void makeOptionsMenu() {
        // OPTIONS MENU
        optionsMenu = makeMenuRes("option",'O'); //$NON-NLS-1$
        JMenuItem functionHelper = makeMenuItemRes("function_dialog_menu_item", 'F', ActionNames.FUNCTIONS, KeyStrokes.FUNCTIONS); //$NON-NLS-1$

        lafMenu = makeMenuRes("appearance",'L'); //$NON-NLS-1$
        UIManager.LookAndFeelInfo lafs[] = getAllLAFs();
        for (int i = 0; i < lafs.length; ++i) {
            JMenuItem laf = new JMenuItem(lafs[i].getName());
            laf.addActionListener(ActionRouter.getInstance());
            laf.setActionCommand(ActionNames.LAF_PREFIX + lafs[i].getClassName());
            laf.setToolTipText(lafs[i].getClassName()); // show the classname to the user
            lafMenu.add(laf);
        }
        optionsMenu.add(functionHelper);
        optionsMenu.add(lafMenu);

        JCheckBoxMenuItem menuToolBar = makeCheckBoxMenuItemRes("menu_toolbar", ActionNames.TOOLBAR); //$NON-NLS-1$
        JCheckBoxMenuItem menuLoggerPanel = makeCheckBoxMenuItemRes("menu_logger_panel", ActionNames.LOGGER_PANEL_ENABLE_DISABLE); //$NON-NLS-1$
        GuiPackage guiInstance = GuiPackage.getInstance();
        if (guiInstance != null) { //avoid error in ant task tests (good way?)
            guiInstance.setMenuItemToolbar(menuToolBar);
            guiInstance.setMenuItemLoggerPanel(menuLoggerPanel);
        }
        optionsMenu.add(menuToolBar);
        optionsMenu.add(menuLoggerPanel);
        
        if (SSLManager.isSSLSupported()) {
            sslManager = makeMenuItemRes("sslmanager", 'S', ActionNames.SSL_MANAGER, KeyStrokes.SSL_MANAGER); //$NON-NLS-1$
            optionsMenu.add(sslManager);
        }
        optionsMenu.add(makeLanguageMenu());

        JMenuItem collapse = makeMenuItemRes("menu_collapse_all", ActionNames.COLLAPSE_ALL, KeyStrokes.COLLAPSE_ALL); //$NON-NLS-1$
        optionsMenu.add(collapse);

        JMenuItem expand = makeMenuItemRes("menu_expand_all", ActionNames.EXPAND_ALL, KeyStrokes.EXPAND_ALL); //$NON-NLS-1$
        optionsMenu.add(expand);

        addPluginsMenuItems(optionsMenu, menuCreators, MENU_LOCATION.OPTIONS);
    }

    private static class LangMenuHelper{
        final ActionRouter actionRouter = ActionRouter.getInstance();
        final JMenu languageMenu;

        LangMenuHelper(JMenu _languageMenu){
            languageMenu = _languageMenu;
        }

        /**
         * Create a language entry from the locale name.
         *
         * @param locale - must also be a valid resource name
         */
        void addLang(String locale){
            String localeString = JMeterUtils.getLocaleString(locale);
            JMenuItem language = new JMenuItem(localeString);
            language.addActionListener(actionRouter);
            language.setActionCommand(ActionNames.CHANGE_LANGUAGE);
            language.setName(locale); // This is used by the ChangeLanguage class to define the Locale
            languageMenu.add(language);
        }

   }

    /**
     * Generate the list of supported languages.
     *
     * @return list of languages
     */
    // Also used by org.apache.jmeter.resources.PackageTest
    public static String[] getLanguages(){
        List<String> lang = new ArrayList<String>(20);
        lang.add(Locale.ENGLISH.toString()); // en
        lang.add(Locale.FRENCH.toString()); // fr
        lang.add(Locale.GERMAN.toString()); // de
        lang.add("no"); // $NON-NLS-1$
        lang.add("pl"); // $NON-NLS-1$
        lang.add("pt_BR"); // $NON-NLS-1$
        lang.add("es"); // $NON-NLS-1$
        lang.add("tr"); // $NON-NLS-1$
        lang.add(Locale.JAPANESE.toString()); // ja
        lang.add(Locale.SIMPLIFIED_CHINESE.toString()); // zh_CN
        lang.add(Locale.TRADITIONAL_CHINESE.toString()); // zh_TW
        final String addedLocales = JMeterUtils.getProperty("locales.add");
        if (addedLocales != null){
            String [] addLanguages =addedLocales.split(","); // $NON-NLS-1$
            for(String newLang : addLanguages){
                log.info("Adding locale "+newLang);
                lang.add(newLang);
            }
        }
        return lang.toArray(new String[lang.size()]);
    }

    static JMenu makeLanguageMenu() {
        final JMenu languageMenu = makeMenuRes("choose_language",'C'); //$NON-NLS-1$

        LangMenuHelper langMenu = new LangMenuHelper(languageMenu);

        /*
         * Note: the item name is used by ChangeLanguage to create a Locale for
         * that language, so need to ensure that the language strings are valid
         * If they exist, use the Locale language constants.
         * Also, need to ensure that the names are valid resource entries too.
         */

        for(String lang : getLanguages()){
            langMenu.addLang(lang);
        }
        return languageMenu;
    }

    private void makeRunMenu() {
        // RUN MENU
        runMenu = makeMenuRes("run",'R'); //$NON-NLS-1$

        run_start = makeMenuItemRes("start", 'S', ActionNames.ACTION_START, KeyStrokes.ACTION_START); //$NON-NLS-1$

        run_start_no_timers = makeMenuItemRes("start_no_timers", ActionNames.ACTION_START_NO_TIMERS); //$NON-NLS-1$
        
        run_stop = makeMenuItemRes("stop", 'T', ActionNames.ACTION_STOP, KeyStrokes.ACTION_STOP); //$NON-NLS-1$
        run_stop.setEnabled(false);

        run_shut = makeMenuItemRes("shutdown", 'Y', ActionNames.ACTION_SHUTDOWN, KeyStrokes.ACTION_SHUTDOWN); //$NON-NLS-1$
        run_shut.setEnabled(false);

        run_clear = makeMenuItemRes("clear", 'C', ActionNames.CLEAR, KeyStrokes.CLEAR); //$NON-NLS-1$

        run_clearAll = makeMenuItemRes("clear_all", 'a', ActionNames.CLEAR_ALL, KeyStrokes.CLEAR_ALL); //$NON-NLS-1$

        runMenu.add(run_start);
        runMenu.add(run_start_no_timers);
        if (remote_start != null) {
            runMenu.add(remote_start);
        }
        remote_start_all = makeMenuItemRes("remote_start_all", ActionNames.REMOTE_START_ALL, KeyStrokes.REMOTE_START_ALL); //$NON-NLS-1$

        runMenu.add(remote_start_all);
        runMenu.add(run_stop);
        runMenu.add(run_shut);
        if (remote_stop != null) {
            runMenu.add(remote_stop);
        }
        remote_stop_all = makeMenuItemRes("remote_stop_all", 'X', ActionNames.REMOTE_STOP_ALL, KeyStrokes.REMOTE_STOP_ALL); //$NON-NLS-1$
        runMenu.add(remote_stop_all);

        if (remote_shut != null) {
            runMenu.add(remote_shut);
        }
        remote_shut_all = makeMenuItemRes("remote_shut_all", 'X', ActionNames.REMOTE_SHUT_ALL, KeyStrokes.REMOTE_SHUT_ALL); //$NON-NLS-1$
        runMenu.add(remote_shut_all);

        if (remote_exit != null) {
            runMenu.add(remote_exit);
        }
        remote_exit_all = makeMenuItemRes("remote_exit_all", ActionNames.REMOTE_EXIT_ALL); //$NON-NLS-1$
        runMenu.add(remote_exit_all);

        runMenu.addSeparator();
        runMenu.add(run_clear);
        runMenu.add(run_clearAll);

        addPluginsMenuItems(runMenu, menuCreators, MENU_LOCATION.RUN);
    }

    private void makeEditMenu() {
        // EDIT MENU
        editMenu = makeMenuRes("edit",'E'); //$NON-NLS-1$

        // From the Java Look and Feel Guidelines: If all items in a menu
        // are disabled, then disable the menu. Makes sense.
        editMenu.setEnabled(false);

        addPluginsMenuItems(editMenu, menuCreators, MENU_LOCATION.EDIT);
    }

    private void makeFileMenu() {
        // FILE MENU
        fileMenu = makeMenuRes("file",'F'); //$NON-NLS-1$

        JMenuItem file_save = makeMenuItemRes("save", 'S', ActionNames.SAVE, KeyStrokes.SAVE); //$NON-NLS-1$
        file_save.setEnabled(true);

        file_save_as = makeMenuItemRes("save_all_as", 'A', ActionNames.SAVE_ALL_AS, KeyStrokes.SAVE_ALL_AS); //$NON-NLS-1$
        file_save_as.setEnabled(true);

        file_selection_as = makeMenuItemRes("save_as", ActionNames.SAVE_AS); //$NON-NLS-1$
        file_selection_as.setEnabled(true);

        file_selection_as_test_fragment = makeMenuItemRes("save_as_test_fragment", ActionNames.SAVE_AS_TEST_FRAGMENT); //$NON-NLS-1$
        file_selection_as_test_fragment.setEnabled(true);

        file_revert = makeMenuItemRes("revert_project", 'R', ActionNames.REVERT_PROJECT); //$NON-NLS-1$
        file_revert.setEnabled(false);

        file_load = makeMenuItemRes("menu_open", 'O', ActionNames.OPEN, KeyStrokes.OPEN); //$NON-NLS-1$
        // Set default SAVE menu item to disabled since the default node that
        // is selected is ROOT, which does not allow items to be inserted.
        file_load.setEnabled(false);

        templates = makeMenuItemRes("template_menu", 'T', ActionNames.TEMPLATES); //$NON-NLS-1$
        templates.setEnabled(true);

        file_close = makeMenuItemRes("menu_close", 'C', ActionNames.CLOSE, KeyStrokes.CLOSE); //$NON-NLS-1$

        file_exit = makeMenuItemRes("exit", 'X', ActionNames.EXIT, KeyStrokes.EXIT); //$NON-NLS-1$

        file_merge = makeMenuItemRes("menu_merge", 'M', ActionNames.MERGE); //$NON-NLS-1$
        // file_merge.setAccelerator(
        // KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        // Set default SAVE menu item to disabled since the default node that
        // is selected is ROOT, which does not allow items to be inserted.
        file_merge.setEnabled(false);

        fileMenu.add(file_close);
        fileMenu.add(file_load);
        fileMenu.add(templates);
        fileMenu.add(file_merge);
        fileMenu.addSeparator();
        fileMenu.add(file_save);
        fileMenu.add(file_save_as);
        fileMenu.add(file_selection_as);
        fileMenu.add(file_selection_as_test_fragment);
        fileMenu.add(file_revert);
        fileMenu.addSeparator();
        // Add the recent files, which will also add a separator that is
        // visible when needed
        file_load_recent_files = LoadRecentProject.getRecentFileMenuItems();
        for(JComponent jc : file_load_recent_files){
            fileMenu.add(jc);
        }

        addPluginsMenuItems(fileMenu, menuCreators, MENU_LOCATION.FILE);

        fileMenu.add(file_exit);
    }

    private void makeSearchMenu() {
        // Search MENU
        searchMenu = makeMenuRes("menu_search"); //$NON-NLS-1$

        JMenuItem search = makeMenuItemRes("menu_search", 'F', ActionNames.SEARCH_TREE, KeyStrokes.SEARCH_TREE); //$NON-NLS-1$
        searchMenu.add(search);
        search.setEnabled(true);

        JMenuItem searchReset = makeMenuItemRes("menu_search_reset", ActionNames.SEARCH_RESET); //$NON-NLS-1$
        searchMenu.add(searchReset);
        searchReset.setEnabled(true);

        addPluginsMenuItems(searchMenu, menuCreators, MENU_LOCATION.SEARCH);
    }

    /**
     * @param menu 
     * @param menuCreators
     * @param location
     */
    private void addPluginsMenuItems(JMenu menu, List<MenuCreator> menuCreators, MENU_LOCATION location) {
        boolean addedSeparator = false;
        for (MenuCreator menuCreator : menuCreators) {
            JMenuItem[] menuItems = menuCreator.getMenuItemsAtLocation(location);
            for (JMenuItem jMenuItem : menuItems) {
                if(!addedSeparator) {
                    menu.addSeparator();
                    addedSeparator = true;
                }
                menu.add(jMenuItem);
            }
        }
    }
    
    public void setRunning(boolean running, String host) {
        log.info("setRunning(" + running + "," + host + ")");
        if(org.apache.jmeter.gui.MainFrame.LOCAL.equals(host)) {
            return;
        }
        Iterator<JMenuItem> iter = remote_engine_start.iterator();
        Iterator<JMenuItem> iter2 = remote_engine_stop.iterator();
        Iterator<JMenuItem> iter3 = remote_engine_exit.iterator();
        Iterator<JMenuItem> iter4 = remote_engine_shut.iterator();
        while (iter.hasNext() && iter2.hasNext() && iter3.hasNext() &&iter4.hasNext()) {
            JMenuItem start = iter.next();
            JMenuItem stop = iter2.next();
            JMenuItem exit = iter3.next();
            JMenuItem shut = iter4.next();
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
            if (shut.getText().equals(host)) {
                log.debug("Found exit  host: " + exit.getText());
                shut.setEnabled(running);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(boolean enable) {
        run_start.setEnabled(!enable);
        run_start_no_timers.setEnabled(!enable);
        run_stop.setEnabled(enable);
        run_shut.setEnabled(enable);
    }

    private void getRemoteItems() {
        if (remoteHosts.length > 0) {
            remote_start = makeMenuRes("remote_start"); //$NON-NLS-1$
            remote_stop = makeMenuRes("remote_stop"); //$NON-NLS-1$
            remote_shut = makeMenuRes("remote_shut"); //$NON-NLS-1$
            remote_exit = makeMenuRes("remote_exit"); //$NON-NLS-1$

            for (int i = 0; i < remoteHosts.length; i++) {
                remoteHosts[i] = remoteHosts[i].trim();

                JMenuItem item = makeMenuItemNoRes(remoteHosts[i], ActionNames.REMOTE_START);
                remote_engine_start.add(item);
                remote_start.add(item);

                item = makeMenuItemNoRes(remoteHosts[i], ActionNames.REMOTE_STOP);
                item.setEnabled(false);
                remote_engine_stop.add(item);
                remote_stop.add(item);

                item = makeMenuItemNoRes(remoteHosts[i], ActionNames.REMOTE_SHUT);
                item.setEnabled(false);
                remote_engine_shut.add(item);
                remote_shut.add(item);

                item = makeMenuItemNoRes(remoteHosts[i],ActionNames.REMOTE_EXIT);
                item.setEnabled(false);
                remote_engine_exit.add(item);
                remote_exit.add(item);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void localeChanged(LocaleChangeEvent event) {
        updateMenuElement(fileMenu);
        updateMenuElement(editMenu);
        updateMenuElement(searchMenu);
        updateMenuElement(runMenu);
        updateMenuElement(optionsMenu);
        updateMenuElement(helpMenu);
        for (MenuCreator creator : menuCreators) {
            creator.localeChanged();
        }
    }

    /**
     * Get a list of all installed LAFs plus CrossPlatform and System.
     * 
     * @return The list of available {@link LookAndFeelInfo}s
     */
    // This is also used by LookAndFeelCommand
    public static LookAndFeelInfo[] getAllLAFs() {
        UIManager.LookAndFeelInfo lafs[] = UIManager.getInstalledLookAndFeels();
        int i = lafs.length;
        UIManager.LookAndFeelInfo lafsAll[] = new UIManager.LookAndFeelInfo[i+2];
        System.arraycopy(lafs, 0, lafsAll, 0, i);
        lafsAll[i++]=new UIManager.LookAndFeelInfo(CROSS_PLATFORM_LAF,UIManager.getCrossPlatformLookAndFeelClassName());
        lafsAll[i++]=new UIManager.LookAndFeelInfo(SYSTEM_LAF,UIManager.getSystemLookAndFeelClassName());
        return lafsAll;
    }
    /**
     * <p>Refreshes all texts in the menu and all submenus to a new locale.</p>
     *
     * <p>Assumes that the item name is set to the resource key, so the resource can be retrieved.
     * Certain action types do not follow this rule, @see JMeterMenuBar#isNotResource(String)</p>
     *
     * The Language Change event assumes that the name is the same as the locale name,
     * so this additionally means that all supported locales must be defined as resources.
     *
     */
    private void updateMenuElement(MenuElement menu) {
        Component component = menu.getComponent();
        final String compName = component.getName();
        if (compName != null) {
            for (MenuCreator menuCreator : menuCreators) {
                if(menuCreator.localeChanged(menu)) {
                    return;
                }
            }
            if (component instanceof JMenu) {
                final JMenu jMenu = (JMenu) component;
                if (isResource(jMenu.getActionCommand())){
                    jMenu.setText(JMeterUtils.getResString(compName));
                }
            } else {
                final JMenuItem jMenuItem = (JMenuItem) component;
                if (isResource(jMenuItem.getActionCommand())){
                    jMenuItem.setText(JMeterUtils.getResString(compName));
                } else if  (ActionNames.CHANGE_LANGUAGE.equals(jMenuItem.getActionCommand())){
                    jMenuItem.setText(JMeterUtils.getLocaleString(compName));
                }
            }
        }

        MenuElement[] subelements = menu.getSubElements();

        for (int i = 0; i < subelements.length; i++) {
            updateMenuElement(subelements[i]);
        }
    }

    /**
     * Return true if component name is a resource.<br/>
     * i.e it is not a hostname:<br/>
     *
     * <tt>ActionNames.REMOTE_START</tt><br/>
     * <tt>ActionNames.REMOTE_STOP</tt><br/>
     * <tt>ActionNames.REMOTE_EXIT</tt><br/>
     *
     * nor a filename:<br/>
     * <tt>ActionNames.OPEN_RECENT</tt>
     *
     * nor a look and feel prefix:<br/>
     * <tt>ActionNames.LAF_PREFIX</tt>
     */
    private static boolean isResource(String actionCommand) {
        if (ActionNames.CHANGE_LANGUAGE.equals(actionCommand)){//
            return false;
        }
        if (ActionNames.ADD.equals(actionCommand)){//
            return false;
        }
        if (ActionNames.REMOTE_START.equals(actionCommand)){//
            return false;
        }
        if (ActionNames.REMOTE_STOP.equals(actionCommand)){//
            return false;
        }
        if (ActionNames.REMOTE_SHUT.equals(actionCommand)){//
            return false;
        }
        if (ActionNames.REMOTE_EXIT.equals(actionCommand)){//
            return false;
        }
        if (ActionNames.OPEN_RECENT.equals(actionCommand)){//
            return false;
        }
        if (actionCommand != null && actionCommand.startsWith(ActionNames.LAF_PREFIX)){
            return false;
        }
        return true;
    }

    /**
     * Make a menu from a resource string.
     * @param resource used to name menu and set text.
     * @return the menu
     */
    private static JMenu makeMenuRes(String resource) {
        JMenu menu = new JMenu(JMeterUtils.getResString(resource));
        menu.setName(resource);
        return menu;
    }

    /**
     * Make a menu from a resource string and set its mnemonic.
     *
     * @param resource
     * @param mnemonic
     * @return the menu
     */
    private static JMenu makeMenuRes(String resource, int mnemonic){
        JMenu menu = makeMenuRes(resource);
        menu.setMnemonic(mnemonic);
        return menu;
    }

    /**
     * Make a menuItem using a fixed label which is also used as the item name.
     * This is used for items such as recent files and hostnames which are not resources
     * @param label (this is not used as a resource key)
     * @param actionCommand
     * @return the menu item
     */
    private static JMenuItem makeMenuItemNoRes(String label, String actionCommand) {
        JMenuItem menuItem = new JMenuItem(label);
        menuItem.setName(label);
        menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(ActionRouter.getInstance());
        return menuItem;
    }

    private static JMenuItem makeMenuItemRes(String resource, String actionCommand) {
        return makeMenuItemRes(resource, KeyEvent.VK_UNDEFINED, actionCommand, null);
    }

    private static JMenuItem makeMenuItemRes(String resource, String actionCommand, KeyStroke keyStroke) {
        return makeMenuItemRes(resource, KeyEvent.VK_UNDEFINED, actionCommand, keyStroke);
    }

    private static JMenuItem makeMenuItemRes(String resource, int mnemonic, String actionCommand) {
        return makeMenuItemRes(resource, mnemonic, actionCommand, null);
    }

    private static JMenuItem makeMenuItemRes(String resource, int mnemonic, String actionCommand, KeyStroke keyStroke){
        JMenuItem menuItem = new JMenuItem(JMeterUtils.getResString(resource), mnemonic);
        menuItem.setName(resource);
        menuItem.setActionCommand(actionCommand);
        menuItem.setAccelerator(keyStroke);
        menuItem.addActionListener(ActionRouter.getInstance());
        return menuItem;
    }
    
    private static JCheckBoxMenuItem makeCheckBoxMenuItemRes(String resource, String actionCommand) {
        return makeCheckBoxMenuItemRes(resource, actionCommand, null);
    }

    private static JCheckBoxMenuItem makeCheckBoxMenuItemRes(String resource, 
            String actionCommand, KeyStroke keyStroke){
        JCheckBoxMenuItem cbkMenuItem = new JCheckBoxMenuItem(JMeterUtils.getResString(resource));
        cbkMenuItem.setName(resource);
        cbkMenuItem.setActionCommand(actionCommand);
        cbkMenuItem.setAccelerator(keyStroke);
        cbkMenuItem.addActionListener(ActionRouter.getInstance());
        return cbkMenuItem;
    }
}
