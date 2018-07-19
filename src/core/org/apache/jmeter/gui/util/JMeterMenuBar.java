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
import java.util.Arrays;
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
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class JMeterMenuBar extends JMenuBar implements LocaleChangeListener {
    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(JMeterMenuBar.class);

    private JMenu fileMenu;
    private JMenuItem fileSaveAs;
    private JMenuItem fileRevert;
    private JMenuItem fileLoad;
    private JMenu recentFilesOpen;
    private List<JComponent> fileLoadRecentFiles;
    private JMenuItem fileMerge;
    private JMenu editMenu;
    private JMenu editAdd;
    private JMenu runMenu;
    private JMenuItem runStart;
    private JMenuItem runStartNoTimers;
    private JMenu remoteStart;
    private Collection<JMenuItem> remoteEngineStart;
    private JMenuItem runStop;
    private JMenuItem runShut;
    private JMenu remoteStop;
    private JMenu remoteShut;
    private Collection<JMenuItem> remoteEngineStop;
    private Collection<JMenuItem> remoteEngineShut;
    private JMenu optionsMenu;
    private JMenu helpMenu;
    private String[] remoteHosts;
    private JMenu remoteExit;
    private Collection<JMenuItem> remoteEngineExit;
    private JMenu searchMenu;
    private List<MenuCreator> menuCreators;

    public static final String SYSTEM_LAF = "System"; // $NON-NLS-1$
    public static final String CROSS_PLATFORM_LAF = "CrossPlatform"; // $NON-NLS-1$
    public static final String DARCULA_LAF = "Darcula"; // $NON-NLS-1$
    public static final String DARCULA_LAF_CLASS = "com.bulenkov.darcula.DarculaLaf"; // $NON-NLS-1$
    
    public JMeterMenuBar() {
        // List for recent files menu items
        fileLoadRecentFiles = new LinkedList<>();
        // Lists for remote engines menu items
        remoteEngineStart = new LinkedList<>();
        remoteEngineStop = new LinkedList<>();
        remoteEngineShut = new LinkedList<>();
        remoteEngineExit = new LinkedList<>();
        remoteHosts = JOrphanUtils.split(JMeterUtils.getPropDefault("remote_hosts", ""), ","); //$NON-NLS-1$
        if (remoteHosts.length == 1 && remoteHosts[0].isEmpty()) {
            remoteHosts = new String[0];
        }
        this.getRemoteItems();
        createMenuBar();
        JMeterUtils.addLocaleChangeListener(this);
    }

    public void setFileSaveEnabled(boolean enabled) {
        if (fileSaveAs != null) {
            fileSaveAs.setEnabled(enabled);
        }
    }

    public void setFileLoadEnabled(boolean enabled) {
        if (fileLoad != null) {
            fileLoad.setEnabled(enabled);
        }
        if (fileMerge != null) {
            fileMerge.setEnabled(enabled);
        }
    }

    public void setFileRevertEnabled(boolean enabled) {
        if (fileRevert != null) {
            fileRevert.setEnabled(enabled);
        }
    }

    public void setProjectFileLoaded(String file) {
        if (fileLoadRecentFiles != null && file != null) {
            LoadRecentProject.updateRecentFileMenuItems(fileLoadRecentFiles, file);
            recentFilesOpen.setEnabled(true);
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
        if (editAdd != null) {
            editMenu.remove(editAdd);
        }
        // Insert the Add menu as the first menu item in the Edit menu.
        editAdd = menu;
        editMenu.insert(editAdd, 0);
    }

    // Called by MainFrame#setEditMenu() which is called by EditCommand#doAction and GuiPackage#localeChanged
    public void setEditMenu(JPopupMenu menu) {
        if (menu != null) {
            editMenu.removeAll();
            for (Component comp : menu.getComponents()) {
                editMenu.add(comp);
            }
            editMenu.setEnabled(true);
        } else {
            editMenu.setEnabled(false);
        }
    }

    public void setEditAddEnabled(boolean enabled) {
        // There was a NPE being thrown without the null check here.. JKB
        if (editAdd != null) {
            editAdd.setEnabled(enabled);
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

        this.menuCreators = findMenuCreators();

        makeFileMenu();
        makeEditMenu();
        makeRunMenu();
        makeSearchMenu();
        makeOptionsMenu();
        makeHelpMenu();
        this.add(fileMenu);
        this.add(editMenu);
        this.add(searchMenu);
        this.add(runMenu);
        this.add(optionsMenu);

        menuCreators.stream()
                .map(MenuCreator::getTopLevelMenus)
                .flatMap(Arrays::stream)
                .forEachOrdered(this::add);

        this.add(helpMenu);
    }

    private List<MenuCreator> findMenuCreators() {
        List<MenuCreator> creators = new ArrayList<>();
        try {
            List<String> listClasses = ClassFinder.findClassesThatExtend(
                    JMeterUtils.getSearchPaths(),
                    new Class[] {MenuCreator.class });
            for (String strClassName : listClasses) {
                try {
                    log.debug("Loading menu creator class: {}", strClassName);
                    Class<?> commandClass = Class.forName(strClassName);
                    if (!Modifier.isAbstract(commandClass.getModifiers())) {
                        log.debug("Instantiating: {}", commandClass);
                        MenuCreator creator = (MenuCreator) commandClass.newInstance();
                        creators.add(creator);
                    }
                } catch (Exception e) {
                    log.error("Exception registering {} with implementation: {}", MenuCreator.class, strClassName, e);
                }
            }
        } catch (IOException e) {
            log.error("Exception finding implementations of {}", MenuCreator.class, e);
        }
        return creators;
    }

    private void makeHelpMenu() {
        helpMenu = makeMenuRes("help",'H'); //$NON-NLS-1$

        JMenuItem contextHelp = makeMenuItemRes("help", 'H', ActionNames.HELP, KeyStrokes.HELP); //$NON-NLS-1$
        JMenuItem whatClass = makeMenuItemRes("help_node", 'W', ActionNames.WHAT_CLASS, KeyStrokes.WHAT_CLASS);//$NON-NLS-1$
        JMenuItem setDebug = makeMenuItemRes("debug_on", ActionNames.DEBUG_ON, KeyStrokes.DEBUG_ON);//$NON-NLS-1$
        JMenuItem resetDebug = makeMenuItemRes("debug_off", ActionNames.DEBUG_OFF, KeyStrokes.DEBUG_OFF);//$NON-NLS-1$
        JMenuItem heapDump = makeMenuItemRes("heap_dump", ActionNames.HEAP_DUMP);//$NON-NLS-1$
        JMenuItem threadDump = makeMenuItemRes("thread_dump", ActionNames.THREAD_DUMP);//$NON-NLS-1$
        
        JMenu usefulLinks = makeMenuRes("useful_links");//$NON-NLS-1$
        usefulLinks.add(makeMenuItemRes("link_release_notes", ActionNames.LINK_RELEASE_NOTES));
        usefulLinks.add(makeMenuItemRes("link_bug_tracker", ActionNames.LINK_BUG_TRACKER));
        usefulLinks.add(makeMenuItemRes("link_comp_ref", ActionNames.LINK_COMP_REF));
        usefulLinks.add(makeMenuItemRes("link_func_ref", ActionNames.LINK_FUNC_REF));
        usefulLinks.add(makeMenuItemRes("link_nightly_build", ActionNames.LINK_NIGHTLY_BUILD));

        JMenuItem helpAbout = makeMenuItemRes("about", 'A', ActionNames.ABOUT);

        helpMenu.add(contextHelp);
        helpMenu.addSeparator();
        helpMenu.add(whatClass);
        helpMenu.add(setDebug);
        helpMenu.add(resetDebug);
        helpMenu.add(heapDump);
        helpMenu.add(threadDump);

        addPluginsMenuItems(helpMenu, menuCreators, MENU_LOCATION.HELP);
        
        helpMenu.addSeparator();
        helpMenu.add(usefulLinks);
        helpMenu.addSeparator();
        helpMenu.add(helpAbout);
    }

    private void makeOptionsMenu() {
        optionsMenu = makeMenuRes("option",'O'); //$NON-NLS-1$
        optionsMenu.add(makeMenuItemRes("function_dialog_menu_item", 'F', ActionNames.FUNCTIONS, KeyStrokes.FUNCTIONS));
        optionsMenu.add(createLaFMenu());

        JCheckBoxMenuItem menuLoggerPanel = makeCheckBoxMenuItemRes("menu_logger_panel", ActionNames.LOGGER_PANEL_ENABLE_DISABLE); //$NON-NLS-1$
        GuiPackage guiInstance = GuiPackage.getInstance();
        if (guiInstance != null) { //avoid error in ant task tests (good way?)
            guiInstance.setMenuItemLoggerPanel(menuLoggerPanel);
        }
        optionsMenu.add(menuLoggerPanel);

        JMenu menuLoggerLevel = makeMenuRes("menu_logger_level"); //$NON-NLS-1$
        JMenuItem menuItem;
        String levelString;
        for (Level level : Level.values()) {
            levelString = level.toString();
            menuItem = new JMenuItem(levelString);
            menuItem.addActionListener(ActionRouter.getInstance());
            menuItem.setActionCommand(ActionNames.LOG_LEVEL_PREFIX + levelString);
            menuItem.setToolTipText(levelString); // show the classname to the user
            menuLoggerLevel.add(menuItem);
        }
        optionsMenu.add(menuLoggerLevel);

        if (SSLManager.isSSLSupported()) {
            optionsMenu.add(makeMenuItemRes("sslmanager", 'S', ActionNames.SSL_MANAGER, KeyStrokes.SSL_MANAGER));
        }
        optionsMenu.add(makeLanguageMenu());

        optionsMenu.add(makeMenuItemRes("menu_collapse_all", 'A', ActionNames.COLLAPSE_ALL, KeyStrokes.COLLAPSE_ALL));
        optionsMenu.add(makeMenuItemRes("menu_expand_all", 'X', ActionNames.EXPAND_ALL, KeyStrokes.EXPAND_ALL));
        optionsMenu.add(makeMenuItemRes("menu_zoom_in", 'I', ActionNames.ZOOM_IN));
        optionsMenu.add(makeMenuItemRes("menu_zoom_out", 'U', ActionNames.ZOOM_OUT));
        JCheckBoxMenuItem saveBeforeRun = makeCheckBoxMenuItemRes("menu_save_before_run", ActionNames.SAVE_BEFORE_RUN); //$NON-NLS-1$
        if (guiInstance != null) {
            saveBeforeRun.setSelected(guiInstance.shouldSaveBeforeRunByPreference());
            guiInstance.setMenuItemSaveBeforeRunPanel(saveBeforeRun);
        }
        optionsMenu.add(saveBeforeRun);

        addPluginsMenuItems(optionsMenu, menuCreators, MENU_LOCATION.OPTIONS);
    }

    private JMenu createLaFMenu() {
        JMenu lafMenu = makeMenuRes("appearance", 'L');
        for (LookAndFeelInfo laf : getAllLAFs()) {
            JMenuItem menuItem = new JMenuItem(laf.getName());
            menuItem.addActionListener(ActionRouter.getInstance());
            menuItem.setActionCommand(ActionNames.LAF_PREFIX + laf.getClassName());
            menuItem.setToolTipText(laf.getClassName()); // show the classname to the user
            lafMenu.add(menuItem);
        }
        return lafMenu;
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
     * @return array of language Strings
     */
    // Also used by org.apache.jmeter.resources.PackageTest
    public static String[] getLanguages(){
        List<String> lang = new ArrayList<>(20);
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
        if (addedLocales != null) {
            Arrays.stream(addedLocales.split(","))
                    .peek(newLang -> log.info("Adding locale {}", newLang))
                    .forEachOrdered(lang::add);
        }
        return lang.toArray(new String[lang.size()]);
    }

    private static JMenu makeLanguageMenu() {
        final JMenu languageMenu = makeMenuRes("choose_language",'C'); //$NON-NLS-1$

        LangMenuHelper langMenu = new LangMenuHelper(languageMenu);

        /*
         * Note: the item name is used by ChangeLanguage to create a Locale for
         * that language, so need to ensure that the language strings are valid
         * If they exist, use the Locale language constants.
         * Also, need to ensure that the names are valid resource entries too.
         */

        for (String lang : getLanguages()) {
            langMenu.addLang(lang);
        }
        return languageMenu;
    }

    private void makeRunMenu() {
        runMenu = makeMenuRes("run",'R'); //$NON-NLS-1$
        
        runStart = makeMenuItemRes("start", 'S', ActionNames.ACTION_START, KeyStrokes.ACTION_START); //$NON-NLS-1$

        runStartNoTimers = makeMenuItemRes("start_no_timers", 'N', ActionNames.ACTION_START_NO_TIMERS); //$NON-NLS-1$
        
        runStop = makeMenuItemRes("stop", 'T', ActionNames.ACTION_STOP, KeyStrokes.ACTION_STOP); //$NON-NLS-1$
        runStop.setEnabled(false);

        runShut = makeMenuItemRes("shutdown", 'Y', ActionNames.ACTION_SHUTDOWN, KeyStrokes.ACTION_SHUTDOWN); //$NON-NLS-1$
        runShut.setEnabled(false);

        JMenuItem runClear = makeMenuItemRes("clear", 'C', ActionNames.CLEAR, KeyStrokes.CLEAR);

        JMenuItem runClearAll = makeMenuItemRes("clear_all", 'a', ActionNames.CLEAR_ALL, KeyStrokes.CLEAR_ALL);

        runMenu.add(runStart);
        runMenu.add(runStartNoTimers);
        runMenu.add(runStop);
        runMenu.add(runShut);
        runMenu.addSeparator();
        
        if (remoteStart != null) {
            runMenu.add(remoteStart);
        }
        JMenuItem remoteStartAll = makeMenuItemRes("remote_start_all", ActionNames.REMOTE_START_ALL, KeyStrokes.REMOTE_START_ALL);
        runMenu.add(remoteStartAll);
        if (remoteStop != null) {
            runMenu.add(remoteStop);
        }
        JMenuItem remoteStopAll = makeMenuItemRes("remote_stop_all", 'X', ActionNames.REMOTE_STOP_ALL, KeyStrokes.REMOTE_STOP_ALL);
        runMenu.add(remoteStopAll);

        if (remoteShut != null) {
            runMenu.add(remoteShut);
        }
        JMenuItem remoteShutAll = makeMenuItemRes("remote_shut_all", 'X', ActionNames.REMOTE_SHUT_ALL, KeyStrokes.REMOTE_SHUT_ALL);
        runMenu.add(remoteShutAll);

        if (remoteExit != null) {
            runMenu.add(remoteExit);
        }
        JMenuItem remoteExitAll = makeMenuItemRes("remote_exit_all", ActionNames.REMOTE_EXIT_ALL);
        runMenu.add(remoteExitAll);

        runMenu.addSeparator();
        runMenu.add(runClear);
        runMenu.add(runClearAll);

        addPluginsMenuItems(runMenu, menuCreators, MENU_LOCATION.RUN);
    }

    private void makeEditMenu() {
        editMenu = makeMenuRes("edit",'E'); //$NON-NLS-1$

        // From the Java Look and Feel Guidelines: If all items in a menu
        // are disabled, then disable the menu. Makes sense.
        editMenu.setEnabled(false);

        addPluginsMenuItems(editMenu, menuCreators, MENU_LOCATION.EDIT);
    }

    private void makeFileMenu() {
        fileMenu = makeMenuRes("file",'F'); //$NON-NLS-1$

        JMenuItem fileSave = makeMenuItemRes("save", 'S', ActionNames.SAVE, KeyStrokes.SAVE); //$NON-NLS-1$

        fileSaveAs = makeMenuItemRes("save_all_as", 'A', ActionNames.SAVE_ALL_AS, KeyStrokes.SAVE_ALL_AS); //$NON-NLS-1$
        fileSaveAs.setEnabled(true);

        JMenuItem fileSaveSelectionAs = makeMenuItemRes("save_as", 'L', ActionNames.SAVE_AS);
        fileSaveSelectionAs.setEnabled(true);

        JMenuItem fileSelectionAsTestFragment = makeMenuItemRes("save_as_test_fragment", 'F', ActionNames.SAVE_AS_TEST_FRAGMENT);
        fileSelectionAsTestFragment.setEnabled(true);

        fileRevert = makeMenuItemRes("revert_project", 'R', ActionNames.REVERT_PROJECT); //$NON-NLS-1$
        fileRevert.setEnabled(false);

        fileLoad = makeMenuItemRes("menu_open", 'O', ActionNames.OPEN, KeyStrokes.OPEN); //$NON-NLS-1$
        // Set default SAVE menu item to disabled since the default node that
        // is selected is ROOT, which does not allow items to be inserted.
        fileLoad.setEnabled(false);

        recentFilesOpen = makeMenuRes("menu_recent", 'E'); //$NON-NLS-1$
        recentFilesOpen.setEnabled(false);

        JMenuItem templates = makeMenuItemRes("template_menu", 'T', ActionNames.TEMPLATES);
        templates.setEnabled(true);

        JMenuItem fileNew = makeMenuItemRes("new", 'N', ActionNames.CLOSE, KeyStrokes.CLOSE);

        JMenuItem fileExit = makeMenuItemRes("exit", 'X', ActionNames.EXIT, KeyStrokes.EXIT);

        fileMerge = makeMenuItemRes("menu_merge", 'M', ActionNames.MERGE); //$NON-NLS-1$
        fileMerge.setEnabled(false);

        fileMenu.add(fileNew);
        fileMenu.add(templates);
        fileMenu.add(fileLoad);
        fileMenu.add(recentFilesOpen);
        fileMenu.add(fileMerge);
        fileMenu.addSeparator();
        fileMenu.add(fileSave);
        fileMenu.add(fileSaveAs);
        fileMenu.add(fileSaveSelectionAs);
        fileMenu.add(fileSelectionAsTestFragment);
        fileMenu.add(fileRevert);
        fileMenu.addSeparator();
        // Add the recent files, which will also add a separator that is
        // visible when needed
        fileLoadRecentFiles = LoadRecentProject.getRecentFileMenuItems();
        fileLoadRecentFiles.forEach(jc -> recentFilesOpen.add(jc));
        recentFilesOpen.setEnabled(LoadRecentProject.hasVisibleMenuItem(fileLoadRecentFiles));

        addPluginsMenuItems(fileMenu, menuCreators, MENU_LOCATION.FILE);

        fileMenu.add(fileExit);
    }

    private void makeSearchMenu() {
        searchMenu = makeMenuRes("menu_search", 'S'); //$NON-NLS-1$

        JMenuItem search = makeMenuItemRes("menu_search", 'F', ActionNames.SEARCH_TREE, KeyStrokes.SEARCH_TREE); //$NON-NLS-1$
        searchMenu.add(search);
        search.setEnabled(true);

        JMenuItem searchReset = makeMenuItemRes("menu_search_reset", 'R', ActionNames.SEARCH_RESET); //$NON-NLS-1$
        searchMenu.add(searchReset);
        searchReset.setEnabled(true);

        addPluginsMenuItems(searchMenu, menuCreators, MENU_LOCATION.SEARCH);
    }

    /**
     * Mutate the given menu by, for each menuCreator adding a separator
     * followed by each of its menus based upon the menu location.
     *
     * @param menu the menu to mutate
     * @param menuCreators
     * @param location
     */
    private void addPluginsMenuItems(JMenu menu, List<MenuCreator> menuCreators, MENU_LOCATION location) {
        for (MenuCreator menuCreator : menuCreators) {
            JMenuItem[] menuItems = menuCreator.getMenuItemsAtLocation(location);
            if (menuItems.length != 0) {
                menu.addSeparator();
            }
            Arrays.stream(menuItems).forEachOrdered(menu::add);
        }
    }
    
    public void setRunning(boolean running, String host) {
        log.info("setRunning({}, {})", running, host);
        if (org.apache.jmeter.gui.MainFrame.LOCAL.equals(host)) {
            return;
        }
        Iterator<JMenuItem> iter = remoteEngineStart.iterator();
        Iterator<JMenuItem> iter2 = remoteEngineStop.iterator();
        Iterator<JMenuItem> iter3 = remoteEngineExit.iterator();
        Iterator<JMenuItem> iter4 = remoteEngineShut.iterator();
        while (iter.hasNext() && iter2.hasNext() && iter3.hasNext() &&iter4.hasNext()) {
            JMenuItem start = iter.next();
            JMenuItem stop = iter2.next();
            JMenuItem exit = iter3.next();
            JMenuItem shut = iter4.next();
            if (start.getText().equals(host)) {
                if (log.isDebugEnabled()) {
                    log.debug("Found start host: {}", start.getText());
                }
                start.setEnabled(!running);
            }
            if (stop.getText().equals(host)) {
                if (log.isDebugEnabled()) {
                    log.debug("Found stop  host: {}", stop.getText());
                }
                stop.setEnabled(running);
            }
            if (exit.getText().equals(host)) {
                if (log.isDebugEnabled()) {
                    log.debug("Found exit  host: {}", exit.getText());
                }
                exit.setEnabled(true);
            }
            if (shut.getText().equals(host)) {
                if (log.isDebugEnabled()) {
                    log.debug("Found shut  host: {}", exit.getText());
                }
                shut.setEnabled(running);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(boolean enable) {
        runStart.setEnabled(!enable);
        runStartNoTimers.setEnabled(!enable);
        runStop.setEnabled(enable);
        runShut.setEnabled(enable);
    }

    private void getRemoteItems() {
        if (remoteHosts.length > 0) {
            remoteStart = makeMenuRes("remote_start"); //$NON-NLS-1$
            remoteStop = makeMenuRes("remote_stop"); //$NON-NLS-1$
            remoteShut = makeMenuRes("remote_shut"); //$NON-NLS-1$
            remoteExit = makeMenuRes("remote_exit"); //$NON-NLS-1$

            for (int i = 0; i < remoteHosts.length; i++) {
                remoteHosts[i] = remoteHosts[i].trim();

                JMenuItem item = makeMenuItemNoRes(remoteHosts[i], ActionNames.REMOTE_START);
                remoteEngineStart.add(item);
                remoteStart.add(item);

                item = makeMenuItemNoRes(remoteHosts[i], ActionNames.REMOTE_STOP);
                item.setEnabled(false);
                remoteEngineStop.add(item);
                remoteStop.add(item);

                item = makeMenuItemNoRes(remoteHosts[i], ActionNames.REMOTE_SHUT);
                item.setEnabled(false);
                remoteEngineShut.add(item);
                remoteShut.add(item);

                item = makeMenuItemNoRes(remoteHosts[i],ActionNames.REMOTE_EXIT);
                item.setEnabled(false);
                remoteEngineExit.add(item);
                remoteExit.add(item);
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
        UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        int i = lafs.length;
        UIManager.LookAndFeelInfo[] lafsAll = new UIManager.LookAndFeelInfo[i+2];
        System.arraycopy(lafs, 0, lafsAll, 0, i);
        lafsAll[i++]=new UIManager.LookAndFeelInfo(CROSS_PLATFORM_LAF,UIManager.getCrossPlatformLookAndFeelClassName());
        lafsAll[i]=new UIManager.LookAndFeelInfo(SYSTEM_LAF,UIManager.getSystemLookAndFeelClassName());
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

        for (MenuElement subElement : menu.getSubElements()) {
            updateMenuElement(subElement);
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
