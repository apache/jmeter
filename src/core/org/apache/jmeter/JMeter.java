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

package org.apache.jmeter;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.avalon.CLArgsParser;
import org.apache.commons.cli.avalon.CLOption;
import org.apache.commons.cli.avalon.CLOptionDescriptor;
import org.apache.commons.cli.avalon.CLUtil;
import org.apache.jmeter.control.ReplaceableController;
import org.apache.jmeter.engine.ClientJMeterEngine;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.RemoteJMeterEngineImpl;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.MainFrame;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.Load;
import org.apache.jmeter.gui.action.LoadRecentProject;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.plugin.JMeterPlugin;
import org.apache.jmeter.plugin.PluginManager;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.BeanShellServer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassTools;
import org.apache.jorphan.util.JMeterException;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

import com.thoughtworks.xstream.converters.ConversionException;

/**
 * Main JMeter class; processes options and starts the GUI, non-GUI or server as appropriate.
 */
public class JMeter implements JMeterPlugin {

    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String HTTP_PROXY_PASS = "http.proxyPass"; // $NON-NLS-1$

    public static final String HTTP_PROXY_USER = "http.proxyUser"; // $NON-NLS-1$

    public static final String JMETER_NON_GUI = "JMeter.NonGui"; // $NON-NLS-1$

    // If the -t flag is to "LAST", then the last loaded file (if any) is used
    private static final String USE_LAST_JMX = "LAST";
    // If the -j  or -l flag is set to LAST or LAST.log|LAST.jtl, then the last loaded file name is used to
    // generate the log file name by removing .JMX and replacing it with .log|.jtl

    private static final int PROXY_PASSWORD     = 'a';// $NON-NLS-1$
    private static final int JMETER_HOME_OPT    = 'd';// $NON-NLS-1$
    private static final int HELP_OPT           = 'h';// $NON-NLS-1$
    // jmeter.log
    private static final int JMLOGFILE_OPT      = 'j';// $NON-NLS-1$
    // sample result log file
    private static final int LOGFILE_OPT        = 'l';// $NON-NLS-1$
    private static final int NONGUI_OPT         = 'n';// $NON-NLS-1$
    private static final int PROPFILE_OPT       = 'p';// $NON-NLS-1$
    private static final int PROPFILE2_OPT      = 'q';// $NON-NLS-1$
    private static final int REMOTE_OPT         = 'r';// $NON-NLS-1$
    private static final int SERVER_OPT         = 's';// $NON-NLS-1$
    private static final int TESTFILE_OPT       = 't';// $NON-NLS-1$
    private static final int PROXY_USERNAME     = 'u';// $NON-NLS-1$
    private static final int VERSION_OPT        = 'v';// $NON-NLS-1$

    private static final int SYSTEM_PROPERTY    = 'D';// $NON-NLS-1$
    private static final int JMETER_GLOBAL_PROP = 'G';// $NON-NLS-1$
    private static final int PROXY_HOST         = 'H';// $NON-NLS-1$
    private static final int JMETER_PROPERTY    = 'J';// $NON-NLS-1$
    private static final int LOGLEVEL           = 'L';// $NON-NLS-1$
    private static final int NONPROXY_HOSTS     = 'N';// $NON-NLS-1$
    private static final int PROXY_PORT         = 'P';// $NON-NLS-1$
    private static final int REMOTE_OPT_PARAM   = 'R';// $NON-NLS-1$
    private static final int SYSTEM_PROPFILE    = 'S';// $NON-NLS-1$
    private static final int REMOTE_STOP        = 'X';// $NON-NLS-1$







    /**
     * Define the understood options. Each CLOptionDescriptor contains:
     * <ul>
     * <li>The "long" version of the option. Eg, "help" means that "--help"
     * will be recognised.</li>
     * <li>The option flags, governing the option's argument(s).</li>
     * <li>The "short" version of the option. Eg, 'h' means that "-h" will be
     * recognised.</li>
     * <li>A description of the option.</li>
     * </ul>
     */
    private static final CLOptionDescriptor[] options = new CLOptionDescriptor[] {
            new CLOptionDescriptor("help", CLOptionDescriptor.ARGUMENT_DISALLOWED, HELP_OPT,
                    "print usage information and exit"),
            new CLOptionDescriptor("version", CLOptionDescriptor.ARGUMENT_DISALLOWED, VERSION_OPT,
                    "print the version information and exit"),
            new CLOptionDescriptor("propfile", CLOptionDescriptor.ARGUMENT_REQUIRED, PROPFILE_OPT,
                    "the jmeter property file to use"),
            new CLOptionDescriptor("addprop", CLOptionDescriptor.ARGUMENT_REQUIRED
                    | CLOptionDescriptor.DUPLICATES_ALLOWED, PROPFILE2_OPT,
                    "additional JMeter property file(s)"),
            new CLOptionDescriptor("testfile", CLOptionDescriptor.ARGUMENT_REQUIRED, TESTFILE_OPT,
                    "the jmeter test(.jmx) file to run"),
            new CLOptionDescriptor("logfile", CLOptionDescriptor.ARGUMENT_REQUIRED, LOGFILE_OPT,
                    "the file to log samples to"),
            new CLOptionDescriptor("jmeterlogfile", CLOptionDescriptor.ARGUMENT_REQUIRED, JMLOGFILE_OPT,
                    "jmeter run log file (jmeter.log)"),
            new CLOptionDescriptor("nongui", CLOptionDescriptor.ARGUMENT_DISALLOWED, NONGUI_OPT,
                    "run JMeter in nongui mode"),
            new CLOptionDescriptor("server", CLOptionDescriptor.ARGUMENT_DISALLOWED, SERVER_OPT,
                    "run the JMeter server"),
            new CLOptionDescriptor("proxyHost", CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_HOST,
                    "Set a proxy server for JMeter to use"),
            new CLOptionDescriptor("proxyPort", CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_PORT,
                    "Set proxy server port for JMeter to use"),
            new CLOptionDescriptor("nonProxyHosts", CLOptionDescriptor.ARGUMENT_REQUIRED, NONPROXY_HOSTS,
                    "Set nonproxy host list (e.g. *.apache.org|localhost)"),
            new CLOptionDescriptor("username", CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_USERNAME,
                    "Set username for proxy server that JMeter is to use"),
            new CLOptionDescriptor("password", CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_PASSWORD,
                    "Set password for proxy server that JMeter is to use"),
            new CLOptionDescriptor("jmeterproperty", CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENTS_REQUIRED_2, JMETER_PROPERTY,
                    "Define additional JMeter properties"),
            new CLOptionDescriptor("globalproperty", CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENTS_REQUIRED_2, JMETER_GLOBAL_PROP,
                    "Define Global properties (sent to servers)\n\t\te.g. -Gport=123 or -Gglobal.properties"),
            new CLOptionDescriptor("systemproperty", CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENTS_REQUIRED_2, SYSTEM_PROPERTY,
                    "Define additional system properties"),
            new CLOptionDescriptor("systemPropertyFile", CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENT_REQUIRED, SYSTEM_PROPFILE,
                    "additional system property file(s)"),
            new CLOptionDescriptor("loglevel", CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENTS_REQUIRED_2, LOGLEVEL,
                    "[category=]level e.g. jorphan=INFO or jmeter.util=DEBUG"),
            new CLOptionDescriptor("runremote", CLOptionDescriptor.ARGUMENT_DISALLOWED, REMOTE_OPT,
                    "Start remote servers (as defined in remote_hosts)"),
            new CLOptionDescriptor("remotestart", CLOptionDescriptor.ARGUMENT_REQUIRED, REMOTE_OPT_PARAM,
                    "Start these remote servers (overrides remote_hosts)"),
            new CLOptionDescriptor("homedir", CLOptionDescriptor.ARGUMENT_REQUIRED, JMETER_HOME_OPT,
                    "the jmeter home directory to use"),
            new CLOptionDescriptor("remoteexit", CLOptionDescriptor.ARGUMENT_DISALLOWED, REMOTE_STOP,
            "Exit the remote servers at end of test (non-GUI)"),
                    };

    public JMeter() {
    }

    // Hack to allow automated tests to find when test has ended
    //transient boolean testEnded = false;

    private JMeter parent;

    private Properties remoteProps; // Properties to be sent to remote servers

    private boolean remoteStop; // should remote engines be stopped at end of GUI test?

    /**
     * Starts up JMeter in GUI mode
     */
    private void startGui(String testFile) {

        PluginManager.install(this, true);
        JMeterTreeModel treeModel = new JMeterTreeModel();
        JMeterTreeListener treeLis = new JMeterTreeListener(treeModel);
        treeLis.setActionHandler(ActionRouter.getInstance());
        // NOTUSED: GuiPackage guiPack =
        GuiPackage.getInstance(treeLis, treeModel);
        MainFrame main = new MainFrame(ActionRouter.getInstance(), treeModel, treeLis);
        ComponentUtil.centerComponentInWindow(main, 80);
        main.setVisible(true);
        ActionRouter.getInstance().actionPerformed(new ActionEvent(main, 1, ActionNames.ADD_ALL));
        if (testFile != null) {
            FileInputStream reader = null;
            try {
                File f = new File(testFile);
                log.info("Loading file: " + f);
                reader = new FileInputStream(f);
                HashTree tree = SaveService.loadTree(reader);

                GuiPackage.getInstance().setTestPlanFile(f.getAbsolutePath());

                Load.insertLoadedTree(1, tree);
            } catch (ConversionException e) {
                log.error("Failure loading test file", e);
                JMeterUtils.reportErrorToUser(SaveService.CEtoString(e));
            } catch (Exception e) {
                log.error("Failure loading test file", e);
                JMeterUtils.reportErrorToUser(e.toString());
            } finally {
                JOrphanUtils.closeQuietly(reader);
            }
        }
    }

    /**
     * Takes the command line arguments and uses them to determine how to
     * startup JMeter.
     * 
     * Called reflectively by {@link NewDriver#main(String[])}
     */
    public void start(String[] args) {

        CLArgsParser parser = new CLArgsParser(args, options);
        String error = parser.getErrorString();
        if (error == null){// Check option combinations
            boolean gui = parser.getArgumentById(NONGUI_OPT)==null;
            boolean remoteStart = parser.getArgumentById(REMOTE_OPT)!=null
                               || parser.getArgumentById(REMOTE_OPT_PARAM)!=null;
            if (gui && remoteStart) {
                error = "-r and -R are only valid in non-GUI mode";
            }
        }
        if (null != error) {
            System.err.println("Error: " + error);
            System.out.println("Usage");
            System.out.println(CLUtil.describeOptions(options).toString());
            return;
        }
        try {
            initializeProperties(parser); // Also initialises JMeter logging

            /*
             * The following is needed for HTTPClient.
             * (originally tried doing this in HTTPSampler2,
             * but it appears that it was done too late when running in GUI mode)
             * Set the commons logging default to Avalon Logkit, if not already defined
             */
            if (System.getProperty("org.apache.commons.logging.Log") == null) { // $NON-NLS-1$
                System.setProperty("org.apache.commons.logging.Log" // $NON-NLS-1$
                        , "org.apache.commons.logging.impl.LogKitLogger"); // $NON-NLS-1$
            }

            log.info(JMeterUtils.getJMeterCopyright());
            log.info("Version " + JMeterUtils.getJMeterVersion());
            logProperty("java.version"); //$NON-NLS-1$
            logProperty("java.vm.name"); //$NON-NLS-1$
            logProperty("os.name"); //$NON-NLS-1$
            logProperty("os.arch"); //$NON-NLS-1$
            logProperty("os.version"); //$NON-NLS-1$
            logProperty("file.encoding"); // $NON-NLS-1$
            log.info("Default Locale=" + Locale.getDefault().getDisplayName());
            log.info("JMeter  Locale=" + JMeterUtils.getLocale().getDisplayName());
            log.info("JMeterHome="     + JMeterUtils.getJMeterHome());
            logProperty("user.dir","  ="); //$NON-NLS-1$
            log.info("PWD       ="+new File(".").getCanonicalPath());//$NON-NLS-1$
            log.info("IP: "+JMeterUtils.getLocalHostIP()
                    +" Name: "+JMeterUtils.getLocalHostName()
                    +" FullName: "+JMeterUtils.getLocalHostFullName());
            setProxy(parser);

            updateClassLoader();
            if (log.isDebugEnabled())
            {
                String jcp=System.getProperty("java.class.path");// $NON-NLS-1$
                String bits[] =jcp.split(File.pathSeparator);
                log.debug("ClassPath");
                for(int i = 0; i<bits.length ;i++){
                    log.debug(bits[i]);
                }
                log.debug(jcp);
            }

            // Set some (hopefully!) useful properties
            long now=System.currentTimeMillis();
            JMeterUtils.setProperty("START.MS",Long.toString(now));// $NON-NLS-1$
            Date today=new Date(now); // so it agrees with above
            // TODO perhaps should share code with __time() function for this...
            JMeterUtils.setProperty("START.YMD",new SimpleDateFormat("yyyyMMdd").format(today));// $NON-NLS-1$ $NON-NLS-2$
            JMeterUtils.setProperty("START.HMS",new SimpleDateFormat("HHmmss").format(today));// $NON-NLS-1$ $NON-NLS-2$

            if (parser.getArgumentById(VERSION_OPT) != null) {
                System.out.println(JMeterUtils.getJMeterCopyright());
                System.out.println("Version " + JMeterUtils.getJMeterVersion());
            } else if (parser.getArgumentById(HELP_OPT) != null) {
                System.out.println(JMeterUtils.getResourceFileAsText("org/apache/jmeter/help.txt"));// $NON-NLS-1$
            } else if (parser.getArgumentById(SERVER_OPT) != null) {
                // Start the server
                startServer(JMeterUtils.getPropDefault("server_port", 0));// $NON-NLS-1$
                startOptionalServers();
            } else {
                String testFile=null;
                CLOption testFileOpt = parser.getArgumentById(TESTFILE_OPT);
                if (testFileOpt != null){
                    testFile = testFileOpt.getArgument();
                    if (USE_LAST_JMX.equals(testFile)) {
                        testFile = LoadRecentProject.getRecentFile(0);// most recent
                    }
                }
                if (parser.getArgumentById(NONGUI_OPT) == null) {
                    startGui(testFile);
                    startOptionalServers();
                } else {
                    CLOption rem=parser.getArgumentById(REMOTE_OPT_PARAM);
                    if (rem==null) { rem=parser.getArgumentById(REMOTE_OPT); }
                    CLOption jtl = parser.getArgumentById(LOGFILE_OPT);
                    String jtlFile = null;
                    if (jtl != null){
                        jtlFile=processLAST(jtl.getArgument(), ".jtl"); // $NON-NLS-1$
                    }
                    startNonGui(testFile, jtlFile, rem);
                    startOptionalServers();
                }
            }
        } catch (IllegalUserActionException e) {
            System.out.println(e.getMessage());
            System.out.println("Incorrect Usage");
            System.out.println(CLUtil.describeOptions(options).toString());
        } catch (Throwable e) {
            if (log != null){
                log.fatalError("An error occurred: ",e);
            } else {
                e.printStackTrace();
            }
            System.out.println("An error occurred: " + e.getMessage());
            System.exit(1); // TODO - could this be return?
        }
    }

    // Update classloader if necessary
    private void updateClassLoader() {
            updatePath("search_paths",";"); //$NON-NLS-1$//$NON-NLS-2$
            updatePath("user.classpath",File.pathSeparator);//$NON-NLS-1$
    }

    private void updatePath(String property, String sep) {
        String userpath= JMeterUtils.getPropDefault(property,"");// $NON-NLS-1$
        if (userpath.length() <= 0) { return; }
        log.info(property+"="+userpath); //$NON-NLS-1$
        StringTokenizer tok = new StringTokenizer(userpath, sep);
        while(tok.hasMoreTokens()) {
            String path=tok.nextToken();
            File f=new File(path);
            if (!f.canRead() && !f.isDirectory()) {
                log.warn("Can't read "+path);
            } else {
                log.info("Adding to classpath: "+path);
                try {
                    NewDriver.addPath(path);
                } catch (MalformedURLException e) {
                    log.warn("Error adding: "+path+" "+e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     *
     */
    private void startOptionalServers() {
        int bshport = JMeterUtils.getPropDefault("beanshell.server.port", 0);// $NON-NLS-1$
        String bshfile = JMeterUtils.getPropDefault("beanshell.server.file", "");// $NON-NLS-1$ $NON-NLS-2$
        if (bshport > 0) {
            log.info("Starting Beanshell server (" + bshport + "," + bshfile + ")");
            Runnable t = new BeanShellServer(bshport, bshfile);
            t.run();
        }

        // Should we run a beanshell script on startup?
        String bshinit = JMeterUtils.getProperty("beanshell.init.file");// $NON-NLS-1$
        if (bshinit != null){
            log.info("Run Beanshell on file: "+bshinit);
            try {
                BeanShellInterpreter bsi = new BeanShellInterpreter();//bshinit,log);
                bsi.source(bshinit);
            } catch (ClassNotFoundException e) {
                log.warn("Could not start Beanshell: "+e.getLocalizedMessage());
            } catch (JMeterException e) {
                log.warn("Could not process Beanshell file: "+e.getLocalizedMessage());
            }
        }

        int mirrorPort=JMeterUtils.getPropDefault("mirror.server.port", 0);// $NON-NLS-1$
        if (mirrorPort > 0){
            log.info("Starting Mirror server (" + mirrorPort + ")");
            try {
                Object instance = ClassTools.construct(
                        "org.apache.jmeter.protocol.http.control.HttpMirrorControl",// $NON-NLS-1$
                        mirrorPort);
                ClassTools.invoke(instance,"startHttpMirror");
            } catch (JMeterException e) {
                log.warn("Could not start Mirror server",e);
            }
        }
    }

    /**
     * Sets a proxy server for the JVM if the command line arguments are
     * specified.
     */
    private void setProxy(CLArgsParser parser) throws IllegalUserActionException {
        if (parser.getArgumentById(PROXY_USERNAME) != null) {
            Properties jmeterProps = JMeterUtils.getJMeterProperties();
            if (parser.getArgumentById(PROXY_PASSWORD) != null) {
                String u, p;
                Authenticator.setDefault(new ProxyAuthenticator(u = parser.getArgumentById(PROXY_USERNAME)
                        .getArgument(), p = parser.getArgumentById(PROXY_PASSWORD).getArgument()));
                log.info("Set Proxy login: " + u + "/" + p);
                jmeterProps.setProperty(HTTP_PROXY_USER, u);//for Httpclient
                jmeterProps.setProperty(HTTP_PROXY_PASS, p);//for Httpclient
            } else {
                String u;
                Authenticator.setDefault(new ProxyAuthenticator(u = parser.getArgumentById(PROXY_USERNAME)
                        .getArgument(), ""));
                log.info("Set Proxy login: " + u);
                jmeterProps.setProperty(HTTP_PROXY_USER, u);
            }
        }
        if (parser.getArgumentById(PROXY_HOST) != null && parser.getArgumentById(PROXY_PORT) != null) {
            String h = parser.getArgumentById(PROXY_HOST).getArgument();
            String p = parser.getArgumentById(PROXY_PORT).getArgument();
            System.setProperty("http.proxyHost",  h );// $NON-NLS-1$
            System.setProperty("https.proxyHost", h);// $NON-NLS-1$
            System.setProperty("http.proxyPort",  p);// $NON-NLS-1$
            System.setProperty("https.proxyPort", p);// $NON-NLS-1$
            log.info("Set http[s].proxyHost: " + h + " Port: " + p);
        } else if (parser.getArgumentById(PROXY_HOST) != null || parser.getArgumentById(PROXY_PORT) != null) {
            throw new IllegalUserActionException(JMeterUtils.getResString("proxy_cl_error"));// $NON-NLS-1$
        }

        if (parser.getArgumentById(NONPROXY_HOSTS) != null) {
            String n = parser.getArgumentById(NONPROXY_HOSTS).getArgument();
            System.setProperty("http.nonProxyHosts",  n );// $NON-NLS-1$
            System.setProperty("https.nonProxyHosts", n );// $NON-NLS-1$
            log.info("Set http[s].nonProxyHosts: "+n);
        }
    }

    private void initializeProperties(CLArgsParser parser) {
        if (parser.getArgumentById(PROPFILE_OPT) != null) {
            JMeterUtils.loadJMeterProperties(parser.getArgumentById(PROPFILE_OPT).getArgument());
        } else {
            JMeterUtils.loadJMeterProperties(NewDriver.getJMeterDir() + File.separator
                    + "bin" + File.separator // $NON-NLS-1$
                    + "jmeter.properties");// $NON-NLS-1$
        }

        if (parser.getArgumentById(JMLOGFILE_OPT) != null){
            String jmlogfile=parser.getArgumentById(JMLOGFILE_OPT).getArgument();
            jmlogfile = processLAST(jmlogfile, ".log");// $NON-NLS-1$
            JMeterUtils.setProperty(LoggingManager.LOG_FILE,jmlogfile);
        }

        JMeterUtils.initLogging();
        JMeterUtils.initLocale();
        // Bug 33845 - allow direct override of Home dir
        if (parser.getArgumentById(JMETER_HOME_OPT) == null) {
            JMeterUtils.setJMeterHome(NewDriver.getJMeterDir());
        } else {
            JMeterUtils.setJMeterHome(parser.getArgumentById(JMETER_HOME_OPT).getArgument());
        }

        Properties jmeterProps = JMeterUtils.getJMeterProperties();
        remoteProps = new Properties();

        // Add local JMeter properties, if the file is found
        String userProp = JMeterUtils.getPropDefault("user.properties",""); //$NON-NLS-1$
        if (userProp.length() > 0){ //$NON-NLS-1$
            FileInputStream fis=null;
            try {
                File file = JMeterUtils.findFile(userProp);
                if (file.canRead()){
                    log.info("Loading user properties from: "+file.getCanonicalPath());
                    fis = new FileInputStream(file);
                    Properties tmp = new Properties();
                    tmp.load(fis);
                    jmeterProps.putAll(tmp);
                    LoggingManager.setLoggingLevels(tmp);//Do what would be done earlier
                }
            } catch (IOException e) {
                log.warn("Error loading user property file: " + userProp, e);
            } finally {
                JOrphanUtils.closeQuietly(fis);
            }
        }

        // Add local system properties, if the file is found
        String sysProp = JMeterUtils.getPropDefault("system.properties",""); //$NON-NLS-1$
        if (sysProp.length() > 0){
            FileInputStream fis=null;
            try {
                File file = JMeterUtils.findFile(sysProp);
                if (file.canRead()){
                    log.info("Loading system properties from: "+file.getCanonicalPath());
                    fis = new FileInputStream(file);
                    System.getProperties().load(fis);
                }
            } catch (IOException e) {
                log.warn("Error loading system property file: " + sysProp, e);
            } finally {
                JOrphanUtils.closeQuietly(fis);
            }
        }

        // Process command line property definitions
        // These can potentially occur multiple times

        List<CLOption> clOptions = parser.getArguments();
        int size = clOptions.size();

        for (int i = 0; i < size; i++) {
            CLOption option = clOptions.get(i);
            String name = option.getArgument(0);
            String value = option.getArgument(1);
            FileInputStream fis = null;

            switch (option.getDescriptor().getId()) {

            // Should not have any text arguments
            case CLOption.TEXT_ARGUMENT:
                throw new IllegalArgumentException("Unknown arg: "+option.getArgument());

            case PROPFILE2_OPT: // Bug 33920 - allow multiple props
                try {
                    fis = new FileInputStream(new File(name));
                    Properties tmp = new Properties();
                    tmp.load(fis);
                    jmeterProps.putAll(tmp);
                    LoggingManager.setLoggingLevels(tmp);//Do what would be done earlier
                } catch (FileNotFoundException e) {
                    log.warn("Can't find additional property file: " + name, e);
                } catch (IOException e) {
                    log.warn("Error loading additional property file: " + name, e);
                } finally {
                    JOrphanUtils.closeQuietly(fis);
                }
                break;
            case SYSTEM_PROPFILE:
                log.info("Setting System properties from file: " + name);
                try {
                    fis = new FileInputStream(new File(name));
                    System.getProperties().load(fis);
                } catch (IOException e) {
                    log.warn("Cannot find system property file "+e.getLocalizedMessage());
                } finally {
                    JOrphanUtils.closeQuietly(fis);
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
            case JMETER_GLOBAL_PROP:
                if (value.length() > 0) { // Set it
                    log.info("Setting Global property: " + name + "=" + value);
                    remoteProps.setProperty(name, value);
                } else {
                    File propFile = new File(name);
                    if (propFile.canRead()) {
                        log.info("Setting Global properties from the file "+name);
                        try {
                            fis = new FileInputStream(propFile);
                            remoteProps.load(fis);
                        } catch (FileNotFoundException e) {
                            log.warn("Could not find properties file: "+e.getLocalizedMessage());
                        } catch (IOException e) {
                            log.warn("Could not load properties file: "+e.getLocalizedMessage());
                        } finally {
                            JOrphanUtils.closeQuietly(fis);
                        }
                    }
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
            case REMOTE_STOP:
                remoteStop = true;
                break;
            }
        }

        String sample_variables = (String) jmeterProps.get(SampleEvent.SAMPLE_VARIABLES);
        if (sample_variables != null){
            remoteProps.put(SampleEvent.SAMPLE_VARIABLES, sample_variables);
        }
    }

    /*
     * Checks for LAST or LASTsuffix.
     * Returns the LAST name with .JMX replaced by suffix.
     */
    private String processLAST(String jmlogfile, String suffix) {
        if (USE_LAST_JMX.equals(jmlogfile) || USE_LAST_JMX.concat(suffix).equals(jmlogfile)){
            String last = LoadRecentProject.getRecentFile(0);// most recent
            final String JMX_SUFFIX = ".JMX"; // $NON-NLS-1$
            if (last.toUpperCase(Locale.ENGLISH).endsWith(JMX_SUFFIX)){
                jmlogfile=last.substring(0, last.length() - JMX_SUFFIX.length()).concat(suffix);
            }
        }
        return jmlogfile;
    }

    private void startServer(int port) {
        try {
            RemoteJMeterEngineImpl.startServer(port);
        } catch (Exception ex) {
            log.error("Giving up, as server failed with:", ex);
            System.err.println("Server failed to start: "+ex);
            System.exit(1);// Give up
        }
    }

    private void startNonGui(String testFile, String logFile, CLOption remoteStart)
            throws IllegalUserActionException {
        // add a system property so samplers can check to see if JMeter
        // is running in NonGui mode
        System.setProperty(JMETER_NON_GUI, "true");// $NON-NLS-1$
        JMeter driver = new JMeter();// TODO - why does it create a new instance?
        driver.remoteProps = this.remoteProps;
        driver.remoteStop = this.remoteStop;
        driver.parent = this;
        PluginManager.install(this, false);

        String remote_hosts_string = null;
        if (remoteStart != null) {
            remote_hosts_string = remoteStart.getArgument();
            if (remote_hosts_string == null) {
                remote_hosts_string = JMeterUtils.getPropDefault(
                        "remote_hosts", //$NON-NLS-1$
                        "127.0.0.1");//$NON-NLS-1$
            }
        }
        if (testFile == null) {
            throw new IllegalUserActionException("Non-GUI runs require a test plan");
        }
        driver.runNonGui(testFile, logFile, remoteStart != null, remote_hosts_string);
    }

    // run test in batch mode
    private void runNonGui(String testFile, String logFile, boolean remoteStart, String remote_hosts_string) {
        FileInputStream reader = null;
        try {
            File f = new File(testFile);
            if (!f.exists() || !f.isFile()) {
                println("Could not open " + testFile);
                return;
            }
            FileServer.getFileServer().setBaseForScript(f);

            reader = new FileInputStream(f);
            log.info("Loading file: " + f);

            HashTree tree = SaveService.loadTree(reader);

            @SuppressWarnings("deprecation") // Deliberate use of deprecated ctor
            JMeterTreeModel treeModel = new JMeterTreeModel(new Object());// Create non-GUI version to avoid headless problems
            JMeterTreeNode root = (JMeterTreeNode) treeModel.getRoot();
            treeModel.addSubTree(tree, root);

            // Hack to resolve ModuleControllers in non GUI mode
            SearchByClass<ReplaceableController> replaceableControllers = new SearchByClass<ReplaceableController>(ReplaceableController.class);
            tree.traverse(replaceableControllers);
            Collection<ReplaceableController> replaceableControllersRes = replaceableControllers.getSearchResults();
            for (Iterator<ReplaceableController> iter = replaceableControllersRes.iterator(); iter.hasNext();) {
                ReplaceableController replaceableController = iter.next();
                replaceableController.resolveReplacementSubTree(root);
            }

            // Remove the disabled items
            // For GUI runs this is done in Start.java
            convertSubTree(tree);

            Summariser summer = null;
            String summariserName = JMeterUtils.getPropDefault("summariser.name", "");//$NON-NLS-1$
            if (summariserName.length() > 0) {
                log.info("Creating summariser <" + summariserName + ">");
                println("Creating summariser <" + summariserName + ">");
                summer = new Summariser(summariserName);
            }

            if (logFile != null) {
                ResultCollector logger = new ResultCollector(summer);
                logger.setFilename(logFile);
                tree.add(tree.getArray()[0], logger);
            }
            else {
                // only add Summariser if it can not be shared with the ResultCollector
                if (summer != null) {
                    tree.add(tree.getArray()[0], summer);
                }
            }

            List<JMeterEngine> engines = new LinkedList<JMeterEngine>();
            tree.add(tree.getArray()[0], new ListenToTest(parent, (remoteStart && remoteStop) ? engines : null));
            println("Created the tree successfully using "+testFile);
            JMeterEngine engine = null;
            if (!remoteStart) {
                engine = new StandardJMeterEngine();
                engine.configure(tree);
                long now=System.currentTimeMillis();
                println("Starting the test @ "+new Date(now)+" ("+now+")");
                engine.runTest();
            } else {
                java.util.StringTokenizer st = new java.util.StringTokenizer(remote_hosts_string, ",");//$NON-NLS-1$
                while (st.hasMoreElements()) {
                    String el = (String) st.nextElement();
                    println("Configuring remote engine for " + el);
                    log.info("Configuring remote engine for " + el);
                    engines.add(doRemoteInit(el.trim(), tree));
                }
                println("Starting remote engines");
                log.info("Starting remote engines");
                long now=System.currentTimeMillis();
                println("Starting the test @ "+new Date(now)+" ("+now+")");
                Iterator<JMeterEngine> iter = engines.iterator();
                while (iter.hasNext()) {
                    engine = iter.next();
                    if (engine != null){
                        engine.runTest();
                    }
                }
                println("Remote engines have been started");
                log.info("Remote engines have been started");
            }
        } catch (Exception e) {
            System.out.println("Error in NonGUIDriver " + e.toString());
            log.error("Error in NonGUIDriver", e);
        } finally {
            JOrphanUtils.closeQuietly(reader);
        }
    }

    /**
     * Refactored from AbstractAction.java
     *
     * @param tree
     */
    public static void convertSubTree(HashTree tree) {
        Iterator<Object> iter = new LinkedList<Object>(tree.list()).iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof TestElement) {
                TestElement item = (TestElement) o;
                if (item.isEnabled()) {
                    if (item instanceof ReplaceableController) {
                        ReplaceableController rc;

                        // TODO this bit of code needs to be tidied up
                        // Unfortunately ModuleController is in components, not core
                        if (item.getClass().getName().equals("org.apache.jmeter.control.ModuleController")){ // Bug 47165
                            rc = (ReplaceableController) item;
                        } else {
                            // HACK: force the controller to load its tree
                            rc = (ReplaceableController) item.clone();
                        }

                        HashTree subTree = tree.getTree(item);
                        if (subTree != null) {
                            HashTree replacementTree = rc.getReplacementSubTree();
                            if (replacementTree != null) {
                                convertSubTree(replacementTree);
                                tree.replace(item, rc);
                                tree.set(rc, replacementTree);
                            }
                        } else { // null subTree
                            convertSubTree(tree.getTree(item));
                        }
                    } else { // not Replaceable Controller
                        convertSubTree(tree.getTree(item));
                    }
                } else { // Not enabled
                    tree.remove(item);
                }
            } else { // Not a TestElement
                JMeterTreeNode item = (JMeterTreeNode) o;
                if (item.isEnabled()) {
                    // Replacement only needs to occur when starting the engine
                    // @see StandardJMeterEngine.run()
                    if (item.getUserObject() instanceof ReplaceableController) {
                        ReplaceableController rc =
                            (ReplaceableController) item.getTestElement();
                        HashTree subTree = tree.getTree(item);

                        if (subTree != null) {
                            HashTree replacementTree = rc.getReplacementSubTree();
                            if (replacementTree != null) {
                                convertSubTree(replacementTree);
                                tree.replace(item, rc);
                                tree.set(rc, replacementTree);
                            }
                        }
                    } else { // Not a ReplaceableController
                        convertSubTree(tree.getTree(item));
                        TestElement testElement = item.getTestElement();
                        tree.replace(item, testElement);
                    }
                 } else { // Not enabled
                    tree.remove(item);
                }
            }
        }
    }

    private JMeterEngine doRemoteInit(String hostName, HashTree testTree) {
        JMeterEngine engine = null;
        try {
            engine = new ClientJMeterEngine(hostName);
        } catch (Exception e) {
            log.fatalError("Failure connecting to remote host", e);
            System.err.println("Failure connecting to remote host"+e);
            return null;
        }
        engine.configure(testTree);
        if (!remoteProps.isEmpty()) {
            engine.setProperties(remoteProps);
        }
        return engine;
    }

    /*
     * Listen to test and handle tidyup after non-GUI test completes.
     * If running a remote test, then after waiting a few seconds for listeners to finish files,
     * it calls System.exit to deal with the Naming Timer Thread.
     */
    private static class ListenToTest implements TestListener, Runnable, Remoteable {
        private AtomicInteger started = new AtomicInteger(0); // keep track of remote tests

        //NOT YET USED private JMeter _parent;

        private final List<JMeterEngine> engines;

        public ListenToTest(JMeter parent, List<JMeterEngine> engines) {
            //_parent = parent;
            this.engines=engines;
        }

        public void testEnded(String host) {
            long now=System.currentTimeMillis();
            log.info("Finished remote host: " + host + " ("+now+")");
            if (started.decrementAndGet() <= 0) {
                Thread stopSoon = new Thread(this);
                stopSoon.start();
            }
        }

        public void testEnded() {
            long now = System.currentTimeMillis();
            println("Tidying up ...    @ "+new Date(now)+" ("+now+")");
            println("... end of run");
        }

        public void testStarted(String host) {
            started.incrementAndGet();
            long now=System.currentTimeMillis();
            log.info("Started remote host:  " + host + " ("+now+")");
        }

        public void testStarted() {
            long now=System.currentTimeMillis();
            log.info(JMeterUtils.getResString("running_test")+" ("+now+")");//$NON-NLS-1$
        }

        /**
         * This is a hack to allow listeners a chance to close their files. Must
         * implement a queue for sample responses tied to the engine, and the
         * engine won't deliver testEnded signal till all sample responses have
         * been delivered. Should also improve performance of remote JMeter
         * testing.
         */
        public void run() {
            long now = System.currentTimeMillis();
            println("Tidying up ...    @ "+new Date(now)+" ("+now+")");
            /*
             * Note: although it should not be necessary to call System.exit here, in the case
             * of a remote test, a Timer thread seems to be generated by the Naming.lookup()
             * method, and it does not die.
             */
            if (engines!=null){ // it will be null unless remoteStop = true
                println("Exitting remote servers");
                Iterator<JMeterEngine> it = engines.iterator();
                while(it.hasNext()){
                    JMeterEngine e = it.next();
                    e.exit();
                }
            }
            try {
                Thread.sleep(5000); // Allow listeners to close files
            } catch (InterruptedException ignored) {
            }
            println("... end of run");
        }

        /**
         * @see TestListener#testIterationStart(LoopIterationEvent)
         */
        public void testIterationStart(LoopIterationEvent event) {
            // ignored
        }
    }

    private static void println(String str) {
        System.out.println(str);
    }

    private static final String[][] DEFAULT_ICONS = {
        { "org.apache.jmeter.control.gui.TestPlanGui",               "org/apache/jmeter/images/beaker.gif" },     //$NON-NLS-1$ $NON-NLS-2$
        { "org.apache.jmeter.timers.gui.AbstractTimerGui",           "org/apache/jmeter/images/timer.gif" },      //$NON-NLS-1$ $NON-NLS-2$
        { "org.apache.jmeter.threads.gui.ThreadGroupGui",            "org/apache/jmeter/images/thread.gif" },     //$NON-NLS-1$ $NON-NLS-2$
        { "org.apache.jmeter.visualizers.gui.AbstractListenerGui",   "org/apache/jmeter/images/meter.png" },      //$NON-NLS-1$ $NON-NLS-2$
        { "org.apache.jmeter.config.gui.AbstractConfigGui",          "org/apache/jmeter/images/testtubes.png" },  //$NON-NLS-1$ $NON-NLS-2$
        { "org.apache.jmeter.processor.gui.AbstractPreProcessorGui", "org/apache/jmeter/images/leafnode.gif"},    //$NON-NLS-1$ $NON-NLS-2$
        { "org.apache.jmeter.processor.gui.AbstractPostProcessorGui","org/apache/jmeter/images/leafnodeflip.gif"},//$NON-NLS-1$ $NON-NLS-2$
        { "org.apache.jmeter.control.gui.AbstractControllerGui",     "org/apache/jmeter/images/knob.gif" },       //$NON-NLS-1$ $NON-NLS-2$
        { "org.apache.jmeter.control.gui.WorkBenchGui",              "org/apache/jmeter/images/clipboard.gif" },  //$NON-NLS-1$ $NON-NLS-2$
        { "org.apache.jmeter.samplers.gui.AbstractSamplerGui",       "org/apache/jmeter/images/pipet.png" },      //$NON-NLS-1$ $NON-NLS-2$
        { "org.apache.jmeter.assertions.gui.AbstractAssertionGui",   "org/apache/jmeter/images/question.gif"}     //$NON-NLS-1$ $NON-NLS-2$
    };

    public String[][] getIconMappings() {
        final String defaultIconProp = "org/apache/jmeter/images/icon.properties"; //$NON-NLS-1$
        String iconProp = JMeterUtils.getPropDefault("jmeter.icons", defaultIconProp);//$NON-NLS-1$
        Properties p = JMeterUtils.loadProperties(iconProp);
        if (p == null && !iconProp.equals(defaultIconProp)) {
            log.info(iconProp + " not found - using " + defaultIconProp);
            iconProp = defaultIconProp;
            p = JMeterUtils.loadProperties(iconProp);
        }
        if (p == null) {
            log.info(iconProp + " not found - using inbuilt icon set");
            return DEFAULT_ICONS;
        }
        log.info("Loaded icon properties from " + iconProp);
        String[][] iconlist = new String[p.size()][3];
        Enumeration<?> pe = p.keys();
        int i = 0;
        while (pe.hasMoreElements()) {
            String key = (String) pe.nextElement();
            String icons[] = JOrphanUtils.split(p.getProperty(key), " ");//$NON-NLS-1$
            iconlist[i][0] = key;
            iconlist[i][1] = icons[0];
            if (icons.length > 1) {
                iconlist[i][2] = icons[1];
            }
            i++;
        }
        return iconlist;
    }

    public String[][] getResourceBundles() {
        return new String[0][];
    }

    /**
     * Check if JMeter is running in non-GUI mode.
     *
     * @return true if JMeter is running in non-GUI mode.
     */
    public static boolean isNonGUI(){
        return "true".equals(System.getProperty(JMeter.JMETER_NON_GUI)); //$NON-NLS-1$
    }

    private void logProperty(String prop){
        log.info(prop+"="+System.getProperty(prop));//$NON-NLS-1$
    }
    private void logProperty(String prop,String separator){
        log.info(prop+separator+System.getProperty(prop));//$NON-NLS-1$
    }
}