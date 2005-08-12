/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package org.apache.jmeter;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.avalon.CLArgsParser;
import org.apache.commons.cli.avalon.CLOption;
import org.apache.commons.cli.avalon.CLOptionDescriptor;
import org.apache.commons.cli.avalon.CLUtil;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.control.gui.ReportGui;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.control.gui.WorkBenchGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.CheckDirty;
import org.apache.jmeter.gui.action.Load;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.plugin.JMeterPlugin;
import org.apache.jmeter.plugin.PluginManager;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.timers.gui.AbstractTimerGui;
import org.apache.jmeter.report.writers.gui.AbstractReportWriterGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * @author pete
 *
 * JMeterReport is the main class for the reporting component. For now,
 * the plan is to make the reporting component a separate GUI, which 
 * can run in GUI or console mode. The purpose of the GUI is to design
 * reports, which can then be run. One of the primary goals of the
 * reporting component is to make it so the reports can be run in an
 * automated process.
 * The report GUI is different than the main JMeter GUI in several ways.
 * <ul>
 *   <li> the gui is not multi-threaded</li>
 *   <li> the gui uses different components</li>
 *   <li> the gui is focused on designing reports from the jtl logs
 * generated during a test run</li>
 * </ul>
 * The class follows the same design as JMeter.java. This should keep
 * things consistent and make it easier to maintain.
 */
public class JMeterReport implements JMeterPlugin {

    transient private static Logger log = LoggingManager.getLoggerForClass();

    private static final int PROPFILE_OPT = 'p';

    private static final int PROPFILE2_OPT = 'q'; // Bug 33920 - additional
                                                    // prop files

    private static final int TESTFILE_OPT = 't';

    private static final int LOGFILE_OPT = 'l';

    private static final int NONGUI_OPT = 'n';

    private static final int HELP_OPT = 'h';

    private static final int VERSION_OPT = 'v';

    private static final int SERVER_OPT = 's';

    private static final int PROXY_HOST = 'H';

    private static final int PROXY_PORT = 'P';

    private static final int PROXY_USERNAME = 'u';

    private static final int PROXY_PASSWORD = 'a';

    private static final int JMETER_PROPERTY = 'J';

    private static final int SYSTEM_PROPERTY = 'D';

    private static final int LOGLEVEL = 'L';

    private static final int REMOTE_OPT = 'r';

    private static final int JMETER_HOME_OPT = 'd';

    private JMeterReport parent;

    private static final CLOptionDescriptor[] options = new CLOptionDescriptor[] {
            new CLOptionDescriptor("help", CLOptionDescriptor.ARGUMENT_DISALLOWED, HELP_OPT,
                    "print usage information and exit"),
            new CLOptionDescriptor("version", CLOptionDescriptor.ARGUMENT_DISALLOWED, VERSION_OPT,
                    "print the version information and exit"),
            new CLOptionDescriptor("propfile", CLOptionDescriptor.ARGUMENT_REQUIRED, PROPFILE_OPT,
                    "the jmeter property file to use"),
            new CLOptionDescriptor("addprop", CLOptionDescriptor.ARGUMENT_REQUIRED
                    | CLOptionDescriptor.DUPLICATES_ALLOWED, // Bug 33920 -
                                                                // allow
                                                                // multiple
                                                                // props
                    PROPFILE2_OPT, "additional property file(s)"),
            new CLOptionDescriptor("testfile", CLOptionDescriptor.ARGUMENT_REQUIRED, TESTFILE_OPT,
                    "the jmeter test(.jmx) file to run"),
            new CLOptionDescriptor("logfile", CLOptionDescriptor.ARGUMENT_REQUIRED, LOGFILE_OPT,
                    "the file to log samples to"),
            new CLOptionDescriptor("nongui", CLOptionDescriptor.ARGUMENT_DISALLOWED, NONGUI_OPT,
                    "run JMeter in nongui mode"),
            new CLOptionDescriptor("server", CLOptionDescriptor.ARGUMENT_DISALLOWED, SERVER_OPT,
                    "run the JMeter server"),
            new CLOptionDescriptor("proxyHost", CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_HOST,
                    "Set a proxy server for JMeter to use"),
            new CLOptionDescriptor("proxyPort", CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_PORT,
                    "Set proxy server port for JMeter to use"),
            new CLOptionDescriptor("username", CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_USERNAME,
                    "Set username for proxy server that JMeter is to use"),
            new CLOptionDescriptor("password", CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_PASSWORD,
                    "Set password for proxy server that JMeter is to use"),
            new CLOptionDescriptor("jmeterproperty", CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENTS_REQUIRED_2, JMETER_PROPERTY, "Define additional JMeter properties"),
            new CLOptionDescriptor("systemproperty", CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENTS_REQUIRED_2, SYSTEM_PROPERTY, "Define additional JMeter properties"),
            new CLOptionDescriptor("loglevel", CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENTS_REQUIRED_2, LOGLEVEL,
                    "Define loglevel: [category=]level e.g. jorphan=INFO or " + "jmeter.util=DEBUG"),
            new CLOptionDescriptor("runremote", CLOptionDescriptor.ARGUMENT_DISALLOWED, REMOTE_OPT,
                    "Start remote servers from non-gui mode"),
            new CLOptionDescriptor("homedir", CLOptionDescriptor.ARGUMENT_REQUIRED, JMETER_HOME_OPT,
                    "the jmeter home directory to use"), };
    
    /**
	 * 
	 */
	public JMeterReport() {
		super();
	}

    /**
     * The default icons for the report GUI.
     */
    private static final String[][] DEFAULT_ICONS = {
            { TestPlanGui.class.getName(), "org/apache/jmeter/images/beaker.gif" },
            { AbstractTimerGui.class.getName(), "org/apache/jmeter/images/timer.gif" },
            { ThreadGroupGui.class.getName(), "org/apache/jmeter/images/thread.gif" },
            { AbstractVisualizer.class.getName(), "org/apache/jmeter/images/meter.png" },
            { AbstractConfigGui.class.getName(), "org/apache/jmeter/images/testtubes.png" },
            { AbstractControllerGui.class.getName(), "org/apache/jmeter/images/knob.gif" },
            { WorkBenchGui.class.getName(), "org/apache/jmeter/images/clipboard.gif" },
            { AbstractSamplerGui.class.getName(), "org/apache/jmeter/images/pipet.png" },
            { AbstractReportWriterGui.class.getName(), "org/apache/jmeter/images/new/pencil.png" },
            { ReportGui.class.getName(), "org/apache/jmeter/images/new/book.png" }
    };
    
	/* (non-Javadoc)
	 * @see org.apache.jmeter.plugin.JMeterPlugin#getIconMappings()
	 */
	public String[][] getIconMappings() {
        String iconProp = JMeterUtils.getPropDefault("jmeter.icons", "org/apache/jmeter/images/icon.properties");
        Properties p = JMeterUtils.loadProperties(iconProp);
        if (p == null) {
            log.info(iconProp + " not found - using default icon set");
            return DEFAULT_ICONS;
        }
        log.info("Loaded icon properties from " + iconProp);
        String[][] iconlist = new String[p.size()][3];
        Enumeration pe = p.keys();
        int i = 0;
        while (pe.hasMoreElements()) {
            String key = (String) pe.nextElement();
            String icons[] = JOrphanUtils.split(p.getProperty(key), " ");
            iconlist[i][0] = key;
            iconlist[i][1] = icons[0];
            if (icons.length > 1)
                iconlist[i][2] = icons[1];
            i++;
        }
        return iconlist;
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.plugin.JMeterPlugin#getResourceBundles()
	 */
	public String[][] getResourceBundles() {
        return new String[0][];
	}

    public void startNonGui(CLOption testFile, CLOption logFile){
        System.setProperty("JMeter.NonGui", "true");
        JMeterReport driver = new JMeterReport();
        driver.parent = this;
        PluginManager.install(this, false);
    }
    
    public void startGui(CLOption testFile) {
        PluginManager.install(this, true);
        JMeterTreeModel treeModel = new JMeterTreeModel();
        JMeterTreeListener treeLis = new JMeterTreeListener(treeModel);
        treeLis.setActionHandler(ActionRouter.getInstance());
        // NOTUSED: GuiPackage guiPack =
        GuiPackage.getInstance(treeLis, treeModel);
        org.apache.jmeter.gui.ReportMainFrame main = new org.apache.jmeter.gui.ReportMainFrame(ActionRouter.getInstance(),
                treeModel, treeLis);
        main.setTitle("Apache JMeter Report");
        main.setIconImage(JMeterUtils.getImage("jmeter.jpg").getImage());
        ComponentUtil.centerComponentInWindow(main, 80);
        main.show();
        ActionRouter.getInstance().actionPerformed(new ActionEvent(main, 1, CheckDirty.ADD_ALL));
        if (testFile != null) {
            try {
                File f = new File(testFile.getArgument());
                log.info("Loading file: " + f);
                FileInputStream reader = new FileInputStream(f);
                HashTree tree = SaveService.loadTree(reader);

                GuiPackage.getInstance().setTestPlanFile(f.getAbsolutePath());

                new Load().insertLoadedTree(1, tree);
            } catch (Exception e) {
                log.error("Failure loading test file", e);
                JMeterUtils.reportErrorToUser(e.toString());
            }
        }
        
    }

    /**
     * 
     * @param args
     */
    public void start(String[] args) {
        CLArgsParser parser = new CLArgsParser(args, options);
        if (null != parser.getErrorString()) {
            System.err.println("Error: " + parser.getErrorString());
            System.out.println("Usage");
            System.out.println(CLUtil.describeOptions(options).toString());
            return;
        }
        try {
            initializeProperties(parser);
            log.info("Version " + JMeterUtils.getJMeterVersion());
            log.info("java.version=" + System.getProperty("java.version"));
            log.info(JMeterUtils.getJMeterCopyright());
            if (parser.getArgumentById(VERSION_OPT) != null) {
                System.out.println(JMeterUtils.getJMeterCopyright());
                System.out.println("Version " + JMeterUtils.getJMeterVersion());
            } else if (parser.getArgumentById(HELP_OPT) != null) {
                System.out.println(JMeterUtils.getResourceFileAsText("org/apache/jmeter/help.txt"));
            } else if (parser.getArgumentById(NONGUI_OPT) == null) {
                startGui(parser.getArgumentById(TESTFILE_OPT));
            } else {
                startNonGui(parser.getArgumentById(TESTFILE_OPT), parser.getArgumentById(LOGFILE_OPT));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred: " + e.getMessage());
            System.exit(-1);
        }
    }
    
    private void initializeProperties(CLArgsParser parser) {
        if (parser.getArgumentById(PROPFILE_OPT) != null) {
            JMeterUtils.getProperties(parser.getArgumentById(PROPFILE_OPT).getArgument());
        } else {
            JMeterUtils.getProperties(NewDriver.getJMeterDir() + File.separator + "bin" + File.separator
                    + "jmeter.properties");
        }

        // Bug 33845 - allow direct override of Home dir
        if (parser.getArgumentById(JMETER_HOME_OPT) == null) {
            JMeterUtils.setJMeterHome(NewDriver.getJMeterDir());
        } else {
            JMeterUtils.setJMeterHome(parser.getArgumentById(JMETER_HOME_OPT).getArgument());
        }

        // Process command line property definitions (can occur multiple times)

        Properties jmeterProps = JMeterUtils.getJMeterProperties();
        List clOptions = parser.getArguments();
        int size = clOptions.size();

        for (int i = 0; i < size; i++) {
            CLOption option = (CLOption) clOptions.get(i);
            String name = option.getArgument(0);
            String value = option.getArgument(1);

            switch (option.getDescriptor().getId()) {
            case PROPFILE2_OPT: // Bug 33920 - allow multiple props
                File f = new File(name);
                try {
                    jmeterProps.load(new FileInputStream(f));
                } catch (FileNotFoundException e) {
                    log.warn("Can't find additional property file: " + name, e);
                } catch (IOException e) {
                    log.warn("Error loading additional property file: " + name, e);
                }
                break;
            case SYSTEM_PROPERTY:
                if (value.length() > 0) { // Set it
                    log.info("Setting System property: " + name + "=" + value);
                    System.getProperties().setProperty(name, value);
                } else { // Reset it
                    log.warn("Removing System property: " + name);
                    System.getProperties().remove(name);
                }
                break;
            case JMETER_PROPERTY:
                if (value.length() > 0) { // Set it
                    log.info("Setting JMeter property: " + name + "=" + value);
                    jmeterProps.setProperty(name, value);
                } else { // Reset it
                    log.warn("Removing JMeter property: " + name);
                    jmeterProps.remove(name);
                }
                break;
            case LOGLEVEL:
                if (value.length() > 0) { // Set category
                    log.info("LogLevel: " + name + "=" + value);
                    LoggingManager.setPriority(value, name);
                } else { // Set root level
                    log.warn("LogLevel: " + name);
                    LoggingManager.setPriority(name);
                }
                break;
            }
        }

    }
    
}