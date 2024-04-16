/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.Authenticator;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.cli.avalon.CLArgsParser;
import org.apache.commons.cli.avalon.CLOption;
import org.apache.commons.cli.avalon.CLOptionDescriptor;
import org.apache.commons.cli.avalon.CLUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.control.ReplaceableController;
import org.apache.jmeter.engine.ClientJMeterEngine;
import org.apache.jmeter.engine.DistributedRunner;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.RemoteJMeterEngineImpl;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.action.LoadRecentProject;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.plugin.JMeterPlugin;
import org.apache.jmeter.plugin.PluginManager;
import org.apache.jmeter.report.config.ConfigurationException;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.rmi.RmiUtils;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestElementSchema;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.RemoteThreadsListenerTestElement;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.BeanShellServer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.SecurityProviderLoader;
import org.apache.jmeter.util.ShutdownClient;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.reflect.ClassTools;
import org.apache.jorphan.util.HeapDumper;
import org.apache.jorphan.util.JMeterException;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.jorphan.util.ThreadDumper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main JMeter class; processes options and starts the GUI, non-GUI or server as appropriate.
 */
public class JMeter implements JMeterPlugin {
    private static final String JSR223_INIT_FILE = "jsr223.init.file";

    private static final Logger log = LoggerFactory.getLogger(JMeter.class);

    public static final int UDP_PORT_DEFAULT = ShutdownClient.UDP_PORT_DEFAULT;

    public static final String HTTP_PROXY_PASS = "http.proxyPass"; // $NON-NLS-1$

    public static final String HTTP_PROXY_USER = "http.proxyUser"; // $NON-NLS-1$

    public static final String JMETER_NON_GUI = "JMeter.NonGui"; // $NON-NLS-1$

    public static final String JMETER_REPORT_OUTPUT_DIR_PROPERTY =
            "jmeter.reportgenerator.outputdir"; //$NON-NLS-1$

    // Icons size in the JMeter tree
    public static final String TREE_ICON_SIZE = "jmeter.tree.icons.size"; //$NON-NLS-1$

    public static final String DEFAULT_TREE_ICON_SIZE = "19x19"; //$NON-NLS-1$

    protected static final String KEY_SIZE = "<SIZE>"; //$NON-NLS-1$

    // If the -t flag is to "LAST", then the last loaded file (if any) is used
    private static final String USE_LAST_JMX = "LAST";
    // If the -j  or -l flag is set to LAST or LAST.log|LAST.jtl, then the last loaded file name is used to
    // generate the log file name by removing .JMX and replacing it with .log|.jtl

    private static final int PROXY_PASSWORD     = 'a';// $NON-NLS-1$
    private static final int JMETER_HOME_OPT    = 'd';// $NON-NLS-1$
    private static final int HELP_OPT           = 'h';// $NON-NLS-1$
    private static final int OPTIONS_OPT        = '?';// $NON-NLS-1$
    // logging configuration file
    private static final int JMLOGCONF_OPT      = 'i';// $NON-NLS-1$
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
    private static final int REPORT_GENERATING_OPT  = 'g';// $NON-NLS-1$
    private static final int REPORT_AT_END_OPT      = 'e';// $NON-NLS-1$
    private static final int REPORT_OUTPUT_FOLDER_OPT      = 'o';// $NON-NLS-1$
    private static final int FORCE_DELETE_RESULT_FILE      = 'f';// $NON-NLS-1$

    private static final int SYSTEM_PROPERTY    = 'D';// $NON-NLS-1$
    private static final int JMETER_GLOBAL_PROP = 'G';// $NON-NLS-1$
    private static final int PROXY_SCHEME       = 'E';// $NON-NLS-1$
    private static final int PROXY_HOST         = 'H';// $NON-NLS-1$
    private static final int JMETER_PROPERTY    = 'J';// $NON-NLS-1$
    private static final int LOGLEVEL           = 'L';// $NON-NLS-1$
    private static final int NONPROXY_HOSTS     = 'N';// $NON-NLS-1$
    private static final int PROXY_PORT         = 'P';// $NON-NLS-1$
    private static final int REMOTE_OPT_PARAM   = 'R';// $NON-NLS-1$
    private static final int SYSTEM_PROPFILE    = 'S';// $NON-NLS-1$
    private static final int REMOTE_STOP        = 'X';// $NON-NLS-1$

    private static final String JMX_SUFFIX = ".JMX"; // $NON-NLS-1$

    private static final String PACKAGE_PREFIX = "org.apache."; //$NON_NLS-1$

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
    private static final CLOptionDescriptor D_OPTIONS_OPT =
            new CLOptionDescriptor("?", CLOptionDescriptor.ARGUMENT_DISALLOWED, OPTIONS_OPT,
                "print command line options and exit");
    private static final CLOptionDescriptor D_HELP_OPT =
            new CLOptionDescriptor("help", CLOptionDescriptor.ARGUMENT_DISALLOWED, HELP_OPT,
                    "print usage information and exit");
    private static final CLOptionDescriptor D_VERSION_OPT =
            new CLOptionDescriptor("version", CLOptionDescriptor.ARGUMENT_DISALLOWED, VERSION_OPT,
                    "print the version information and exit");
    private static final CLOptionDescriptor D_PROPFILE_OPT =
            new CLOptionDescriptor("propfile", CLOptionDescriptor.ARGUMENT_REQUIRED, PROPFILE_OPT,
                    "the jmeter property file to use");
    private static final CLOptionDescriptor D_PROPFILE2_OPT =
            new CLOptionDescriptor("addprop", CLOptionDescriptor.ARGUMENT_REQUIRED
                    | CLOptionDescriptor.DUPLICATES_ALLOWED, PROPFILE2_OPT,
                    "additional JMeter property file(s)");
    private static final CLOptionDescriptor D_TESTFILE_OPT =
            new CLOptionDescriptor("testfile", CLOptionDescriptor.ARGUMENT_REQUIRED, TESTFILE_OPT,
                    "the jmeter test(.jmx) file to run. \"-t LAST\" will load last used file");
    private static final CLOptionDescriptor D_LOGFILE_OPT =
            new CLOptionDescriptor("logfile", CLOptionDescriptor.ARGUMENT_REQUIRED, LOGFILE_OPT,
                    "the file to log samples to");
    private static final CLOptionDescriptor D_JMLOGCONF_OPT =
            new CLOptionDescriptor("jmeterlogconf", CLOptionDescriptor.ARGUMENT_REQUIRED, JMLOGCONF_OPT,
                    "jmeter logging configuration file (log4j2.xml)");
    private static final CLOptionDescriptor D_JMLOGFILE_OPT =
            new CLOptionDescriptor("jmeterlogfile", CLOptionDescriptor.ARGUMENT_REQUIRED, JMLOGFILE_OPT,
                    "jmeter run log file (jmeter.log)");
    private static final CLOptionDescriptor D_NONGUI_OPT =
            new CLOptionDescriptor("nongui", CLOptionDescriptor.ARGUMENT_DISALLOWED, NONGUI_OPT,
                    "run JMeter in nongui mode");
    private static final CLOptionDescriptor D_SERVER_OPT =
            new CLOptionDescriptor("server", CLOptionDescriptor.ARGUMENT_DISALLOWED, SERVER_OPT,
                    "run the JMeter server");
    private static final CLOptionDescriptor D_PROXY_SCHEME =
            new CLOptionDescriptor("proxyScheme", CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_SCHEME,
                    "Set a proxy scheme to use for the proxy server");
    private static final CLOptionDescriptor D_PROXY_HOST =
            new CLOptionDescriptor("proxyHost", CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_HOST,
                    "Set a proxy server for JMeter to use");
    private static final CLOptionDescriptor D_PROXY_PORT =
            new CLOptionDescriptor("proxyPort", CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_PORT,
                    "Set proxy server port for JMeter to use");
    private static final CLOptionDescriptor D_NONPROXY_HOSTS =
            new CLOptionDescriptor("nonProxyHosts", CLOptionDescriptor.ARGUMENT_REQUIRED, NONPROXY_HOSTS,
                    "Set nonproxy host list (e.g. *.apache.org|localhost)");
    private static final CLOptionDescriptor D_PROXY_USERNAME =
            new CLOptionDescriptor("username", CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_USERNAME,
                    "Set username for proxy server that JMeter is to use");
    private static final CLOptionDescriptor D_PROXY_PASSWORD =
            new CLOptionDescriptor("password", CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_PASSWORD,
                    "Set password for proxy server that JMeter is to use");
    private static final CLOptionDescriptor D_JMETER_PROPERTY =
            new CLOptionDescriptor("jmeterproperty", CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENTS_REQUIRED_2, JMETER_PROPERTY,
                    "Define additional JMeter properties");
    private static final CLOptionDescriptor D_JMETER_GLOBAL_PROP =
            new CLOptionDescriptor("globalproperty", CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENTS_REQUIRED_2, JMETER_GLOBAL_PROP,
                    "Define Global properties (sent to servers)\n\t\te.g. -Gport=123 or -Gglobal.properties");
    private static final CLOptionDescriptor D_SYSTEM_PROPERTY =
            new CLOptionDescriptor("systemproperty", CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENTS_REQUIRED_2, SYSTEM_PROPERTY,
                    "Define additional system properties");
    private static final CLOptionDescriptor D_SYSTEM_PROPFILE =
            new CLOptionDescriptor("systemPropertyFile", CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENT_REQUIRED, SYSTEM_PROPFILE,
                    "additional system property file(s)");
    private static final CLOptionDescriptor D_LOGLEVEL =
            new CLOptionDescriptor("loglevel", CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENTS_REQUIRED_2, LOGLEVEL,
                    "[category=]level e.g. jorphan=INFO, jmeter.util=DEBUG or com.example.foo=WARN");
    private static final CLOptionDescriptor D_REMOTE_OPT =
            new CLOptionDescriptor("runremote", CLOptionDescriptor.ARGUMENT_DISALLOWED, REMOTE_OPT,
                    "Start remote servers (as defined in remote_hosts)");
    private static final CLOptionDescriptor D_REMOTE_OPT_PARAM =
            new CLOptionDescriptor("remotestart", CLOptionDescriptor.ARGUMENT_REQUIRED, REMOTE_OPT_PARAM,
                    "Start these remote servers (overrides remote_hosts)");
    private static final CLOptionDescriptor D_JMETER_HOME_OPT =
            new CLOptionDescriptor("homedir", CLOptionDescriptor.ARGUMENT_REQUIRED, JMETER_HOME_OPT,
                    "the jmeter home directory to use");
    private static final CLOptionDescriptor D_REMOTE_STOP =
            new CLOptionDescriptor("remoteexit", CLOptionDescriptor.ARGUMENT_DISALLOWED, REMOTE_STOP,
                    "Exit the remote servers at end of test (non-GUI)");
    private static final CLOptionDescriptor D_REPORT_GENERATING_OPT =
            new CLOptionDescriptor("reportonly",
                    CLOptionDescriptor.ARGUMENT_REQUIRED, REPORT_GENERATING_OPT,
                    "generate report dashboard only, from a test results file",
                    new CLOptionDescriptor[]{ D_NONGUI_OPT, D_REMOTE_OPT, D_REMOTE_OPT_PARAM, D_LOGFILE_OPT }); // disallowed
    private static final CLOptionDescriptor D_REPORT_AT_END_OPT =
            new CLOptionDescriptor("reportatendofloadtests",
                    CLOptionDescriptor.ARGUMENT_DISALLOWED, REPORT_AT_END_OPT,
                    "generate report dashboard after load test");
    private static final CLOptionDescriptor D_REPORT_OUTPUT_FOLDER_OPT =
            new CLOptionDescriptor("reportoutputfolder",
                    CLOptionDescriptor.ARGUMENT_REQUIRED, REPORT_OUTPUT_FOLDER_OPT,
                    "output folder for report dashboard");
     private static final CLOptionDescriptor D_FORCE_DELETE_RESULT_FILE =
            new CLOptionDescriptor("forceDeleteResultFile",
                    CLOptionDescriptor.ARGUMENT_DISALLOWED, FORCE_DELETE_RESULT_FILE,
                    "force delete existing results files and web report folder if present before starting the test");

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

    private static final CLOptionDescriptor[] options = new CLOptionDescriptor[] {
            D_OPTIONS_OPT,
            D_HELP_OPT,
            D_VERSION_OPT,
            D_PROPFILE_OPT,
            D_PROPFILE2_OPT,
            D_TESTFILE_OPT,
            D_LOGFILE_OPT,
            D_JMLOGCONF_OPT,
            D_JMLOGFILE_OPT,
            D_NONGUI_OPT,
            D_SERVER_OPT,
            D_PROXY_SCHEME,
            D_PROXY_HOST,
            D_PROXY_PORT,
            D_NONPROXY_HOSTS,
            D_PROXY_USERNAME,
            D_PROXY_PASSWORD,
            D_JMETER_PROPERTY,
            D_JMETER_GLOBAL_PROP,
            D_SYSTEM_PROPERTY,
            D_SYSTEM_PROPFILE,
            D_FORCE_DELETE_RESULT_FILE,
            D_LOGLEVEL,
            D_REMOTE_OPT,
            D_REMOTE_OPT_PARAM,
            D_JMETER_HOME_OPT,
            D_REMOTE_STOP,
            D_REPORT_GENERATING_OPT,
            D_REPORT_AT_END_OPT,
            D_REPORT_OUTPUT_FOLDER_OPT,
    };

    /** Properties to be sent to remote servers */
    private Properties remoteProps;

    /** should remote engines be stopped at end of non-GUI test? */
    private boolean remoteStop;

    /** should delete result file / report folder before start ? */
    private boolean deleteResultFile = false;

    public JMeter() {
        super();
    }


    /**
     * Takes the command line arguments and uses them to determine how to
     * startup JMeter.
     *
     * Called reflectively by {@link NewDriver#main(String[])}
     * @param args The arguments for JMeter
     */
    public void start(String[] args) {
        CLArgsParser parser = new CLArgsParser(args, options);
        String error = parser.getErrorString();
        if (error == null){// Check option combinations
            boolean gui = parser.getArgumentById(NONGUI_OPT)==null;
            boolean nonGuiOnly = parser.getArgumentById(REMOTE_OPT)!=null
                               || parser.getArgumentById(REMOTE_OPT_PARAM)!=null
                               || parser.getArgumentById(REMOTE_STOP)!=null;
            if (gui && nonGuiOnly) {
                error = "-r and -R and -X are only valid in non-GUI mode";
            }
        }
        if (null != error) {
            System.err.println("Error: " + error);//NOSONAR
            System.out.println("Usage");//NOSONAR
            System.out.println(CLUtil.describeOptions(options).toString());//NOSONAR
            // repeat the error so no need to scroll back past the usage to see it
            System.out.println("Error: " + error);//NOSONAR
            return;
        }
        try {
            initializeProperties(parser); // Also initialises JMeter logging

            SecurityProviderLoader.addSecurityProvider(JMeterUtils.getJMeterProperties());

            Thread.setDefaultUncaughtExceptionHandler(
                    (Thread t, Throwable e) -> {
                    if (!(e instanceof ThreadDeath)) {
                        log.error("Uncaught exception in thread {}", t, e);
                        System.err.println("Uncaught Exception " + e + " in thread " + t + ". See log file for details.");//NOSONAR
                    }
            });

            if (log.isInfoEnabled()) {
                log.info(JMeterUtils.getJMeterCopyright());
                log.info("Version {}", JMeterUtils.getJMeterVersion());
                log.info("java.version={}", System.getProperty("java.version"));//$NON-NLS-1$ //$NON-NLS-2$
                log.info("java.vm.name={}", System.getProperty("java.vm.name"));//$NON-NLS-1$ //$NON-NLS-2$
                log.info("os.name={}", System.getProperty("os.name"));//$NON-NLS-1$ //$NON-NLS-2$
                log.info("os.arch={}", System.getProperty("os.arch"));//$NON-NLS-1$ //$NON-NLS-2$
                log.info("os.version={}", System.getProperty("os.version"));//$NON-NLS-1$ //$NON-NLS-2$
                log.info("file.encoding={}", System.getProperty("file.encoding"));//$NON-NLS-1$ //$NON-NLS-2$
                log.info("java.awt.headless={}", System.getProperty("java.awt.headless"));//$NON-NLS-1$ //$NON-NLS-2$
                log.info("Max memory     ={}", Runtime.getRuntime().maxMemory());
                log.info("Available Processors ={}", Runtime.getRuntime().availableProcessors());
                log.info("Default Locale={}", Locale.getDefault().getDisplayName());
                log.info("JMeter  Locale={}", JMeterUtils.getLocale().getDisplayName());
                log.info("JMeterHome={}", JMeterUtils.getJMeterHome());
                log.info("user.dir  ={}", System.getProperty("user.dir"));//$NON-NLS-1$ //$NON-NLS-2$
                log.info("PWD       ={}", new File(".").getCanonicalPath());//$NON-NLS-1$
                log.info("IP: {} Name: {} FullName: {}", JMeterUtils.getLocalHostIP(), JMeterUtils.getLocalHostName(),
                        JMeterUtils.getLocalHostFullName());
            }
            setProxy(parser);

            updateClassLoader();
            if (log.isDebugEnabled())
            {
                String jcp=System.getProperty("java.class.path");// $NON-NLS-1$
                String[] bits = jcp.split(File.pathSeparator);
                log.debug("ClassPath");
                for(String bit : bits){
                    log.debug(bit);
                }
            }

            // Set some (hopefully!) useful properties
            Instant now = Instant.now();
            JMeterUtils.setProperty("START.MS",Long.toString(now.toEpochMilli()));// $NON-NLS-1$
            JMeterUtils.setProperty("START.YMD", getFormatter("yyyyMMdd").format(now));// $NON-NLS-1$ $NON-NLS-2$
            JMeterUtils.setProperty("START.HMS", getFormatter("HHmmss").format(now));// $NON-NLS-1$ $NON-NLS-2$

            // For unknown reason, TestElementSchema might fail to initialize in remote execution mode
            // It reproduces with Java 11.0.13, and the error is StackOverflowError with the following stacktrace
            // The workaround is to initialize Kotlin reflection before deserializing the test plan.
            //  at java.security.SecureClassLoader.defineClass(SecureClassLoader.java:174) ~[?:?]
            //  at java.net.URLClassLoader.defineClass(URLClassLoader.java:555) ~[?:?]
            //  at java.net.URLClassLoader$1.run(URLClassLoader.java:458) ~[?:?]
            //  at java.net.URLClassLoader$1.run(URLClassLoader.java:452) ~[?:?]
            //  at java.security.AccessController.doPrivileged(Native Method) ~[?:?]
            //  at java.net.URLClassLoader.findClass(URLClassLoader.java:451) ~[?:?]
            //  at java.lang.ClassLoader.loadClass(ClassLoader.java:589) ~[?:?]
            //  at org.apache.jmeter.DynamicClassLoader.loadClass(DynamicClassLoader.java:81) ~[ApacheJMeter.jar:5.5.1-SNAPSHOT]
            //  at java.lang.ClassLoader.loadClass(ClassLoader.java:522) ~[?:?]
            //  at kotlin.jvm.internal.ClassReference.<clinit>(ClassReference.kt:156) ~[kotlin-stdlib-1.8.21.jar:1.8.21-release-380(1.8.21)]
            //  at kotlin.jvm.internal.ReflectionFactory.getOrCreateKotlinClass(ReflectionFactory.java:30) ~[kotlin-stdlib-1.8.21.jar)]
            //  at kotlin.jvm.internal.Reflection.getOrCreateKotlinClass(Reflection.java:60) ~[kotlin-stdlib-1.8.21.jar:1.8.21-release-380(1.8.21)]
            //  at org.apache.jmeter.testelement.TestElementSchema.<init>(TestElementSchema.kt:33) ~[ApacheJMeter_core.jar:5.5.1-SNAPSHOT]
            //  at org.apache.jmeter.testelement.TestElementSchema$INSTANCE.<init>(TestElementSchema.kt:26) ~[ApacheJMeter_core.jar:5.5.1-SNAPSHOT]
            //  at org.apache.jmeter.testelement.TestElementSchema$INSTANCE.<init>(TestElementSchema.kt) ~[ApacheJMeter_core.jar:5.5.1-SNAPSHOT]
            //  at org.apache.jmeter.testelement.TestElementSchema.<clinit>(TestElementSchema.kt) ~[ApacheJMeter_core.jar:5.5.1-SNAPSHOT]
            //  at org.apache.jmeter.protocol.java.sampler.BeanShellSampler.<clinit>(BeanShellSampler.java:53) ~[ApacheJMeter_java.jar:5.5.1-SNAPSHOT]
            //  at jdk.internal.misc.Unsafe.ensureClassInitialized0(Native Method) ~[?:?]
            TestElementSchema.INSTANCE.getGuiClass();

            if (parser.getArgumentById(VERSION_OPT) != null) {
                displayAsciiArt();
            } else if (parser.getArgumentById(HELP_OPT) != null) {
                displayAsciiArt();
                System.out.println(JMeterUtils.getResourceFileAsText("org/apache/jmeter/help.txt"));//NOSONAR $NON-NLS-1$
            } else if (parser.getArgumentById(OPTIONS_OPT) != null) {
                displayAsciiArt();
                System.out.println(CLUtil.describeOptions(options).toString());//NOSONAR
            } else if (parser.getArgumentById(SERVER_OPT) != null) {
                // Start the server
                try {
                    RemoteJMeterEngineImpl.startServer(RmiUtils.getRmiRegistryPort()); // $NON-NLS-1$
                    startOptionalServers();
                } catch (Exception ex) {
                    System.err.println("Server failed to start: "+ex);//NOSONAR
                    log.error("Giving up, as server failed with:", ex);
                    throw ex;
                }
            } else {
                String testFile=null;
                CLOption testFileOpt = parser.getArgumentById(TESTFILE_OPT);
                if (testFileOpt != null){
                    testFile = testFileOpt.getArgument();
                    if (USE_LAST_JMX.equals(testFile)) {
                        testFile = LoadRecentProject.getRecentFile(0);// most recent
                    }
                }
                CLOption testReportOpt = parser.getArgumentById(REPORT_GENERATING_OPT);
                if (testReportOpt != null) { // generate report from existing file
                    String reportFile = testReportOpt.getArgument();
                    extractAndSetReportOutputFolder(parser, deleteResultFile);
                    ReportGenerator generator = new ReportGenerator(reportFile, null);
                    generator.generate();
                } else if (parser.getArgumentById(NONGUI_OPT) == null) { // not non-GUI => GUI
                    PluginManager.install(this, true);
                    String initialTestFile = testFile;
                    JMeterGuiLauncher.startGui(initialTestFile);
                    startOptionalServers();
                } else { // NON-GUI must be true
                    extractAndSetReportOutputFolder(parser, deleteResultFile);

                    CLOption remoteTest = parser.getArgumentById(REMOTE_OPT_PARAM);
                    if (remoteTest == null) {
                        remoteTest = parser.getArgumentById(REMOTE_OPT);
                    }
                    CLOption jtl = parser.getArgumentById(LOGFILE_OPT);
                    String jtlFile = null;
                    if (jtl != null) {
                        jtlFile = processLAST(jtl.getArgument(), ".jtl"); // $NON-NLS-1$
                    }
                    CLOption reportAtEndOpt = parser.getArgumentById(REPORT_AT_END_OPT);
                    if(reportAtEndOpt != null && jtlFile == null) {
                        throw new IllegalUserActionException(
                                "Option -"+ ((char)REPORT_AT_END_OPT)+" requires -"+((char)LOGFILE_OPT )+ " option");
                    }
                    startNonGui(testFile, jtlFile, remoteTest, reportAtEndOpt != null);
                    startOptionalServers();
                }
            }
        } catch (IllegalUserActionException e) {// NOSONAR
            System.out.println("Incorrect Usage:"+e.getMessage());//NOSONAR
            System.out.println(CLUtil.describeOptions(options).toString());//NOSONAR
        } catch (Throwable e) { // NOSONAR
            log.error("An error occurred: ", e);
            System.out.println("An error occurred: " + e.getMessage());//NOSONAR
            // FIXME Should we exit here ? If we are called by Maven or Jenkins
            System.exit(1);
        }
    }

    private static DateTimeFormatter getFormatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault());
    }

    /**
     * Extract option JMeter#REPORT_OUTPUT_FOLDER_OPT and if defined sets property
     * {@link JMeter#JMETER_REPORT_OUTPUT_DIR_PROPERTY} after checking folder can
     * be safely written to
     * @param parser {@link CLArgsParser}
     * @param deleteReportFolder true means delete report folder
     * @throws IllegalArgumentException
     */
    private static void extractAndSetReportOutputFolder(CLArgsParser parser, boolean deleteReportFolder) {
        CLOption reportOutputFolderOpt = parser
                .getArgumentById(REPORT_OUTPUT_FOLDER_OPT);
        if(reportOutputFolderOpt != null) {
            String reportOutputFolder = parser.getArgumentById(REPORT_OUTPUT_FOLDER_OPT).getArgument();
            File reportOutputFolderAsFile = new File(reportOutputFolder);

            JOrphanUtils.canSafelyWriteToFolder(reportOutputFolderAsFile, deleteReportFolder);
            final String reportOutputFolderAbsPath = reportOutputFolderAsFile.getAbsolutePath();
            log.info("Setting property '{}' to:'{}'", JMETER_REPORT_OUTPUT_DIR_PROPERTY, reportOutputFolderAbsPath);
            JMeterUtils.setProperty(JMETER_REPORT_OUTPUT_DIR_PROPERTY, reportOutputFolderAbsPath);
        }
    }

    /**
     * Displays as ASCII Art Apache JMeter version + Copyright notice
     */
    private static void displayAsciiArt() {
        try (InputStream inputStream = JMeter.class.getResourceAsStream("jmeter_as_ascii_art.txt")) {
            if(inputStream != null) {
                String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                System.out.println(text);//NOSONAR
            }
        } catch (Exception e1) { //NOSONAR No logging here
            System.out.println(JMeterUtils.getJMeterCopyright());//NOSONAR
            System.out.println("Version " + JMeterUtils.getJMeterVersion());//NOSONAR
        }
    }

    // Update classloader if necessary
    private static void updateClassLoader() throws MalformedURLException {
        updatePath("search_paths",";", true); //$NON-NLS-1$//$NON-NLS-2$
        updatePath("user.classpath",File.pathSeparator, true);//$NON-NLS-1$
        updatePath("plugin_dependency_paths",";", false);//$NON-NLS-1$
    }

    private static void updatePath(String property, String sep, boolean cp) throws MalformedURLException {
        String userpath= JMeterUtils.getPropDefault(property,"");// $NON-NLS-1$
        if (userpath.length() <= 0) {
            return;
        }
        log.info("{}={}", property, userpath); //$NON-NLS-1$
        StringTokenizer tok = new StringTokenizer(userpath, sep);
        while(tok.hasMoreTokens()) {
            String path=tok.nextToken();
            File f=new File(path);
            if (!f.canRead() && !f.isDirectory()) {
                log.warn("Can't read {}", path);
            } else {
                if (cp) {
                    log.info("Adding to classpath and loader: {}", path);
                    NewDriver.addPath(path);
                } else {
                    log.info("Adding to loader: {}", path);
                    NewDriver.addURL(path);
                }
            }
        }
    }

    /**
     *
     */
    private static void startOptionalServers() {
        int bshport = JMeterUtils.getPropDefault("beanshell.server.port", 0);// $NON-NLS-1$
        String bshfile = JMeterUtils.getPropDefault("beanshell.server.file", "");// $NON-NLS-1$ $NON-NLS-2$
        if (bshport > 0) {
            log.info("Starting Beanshell server ({},{})", bshport, bshfile);
            Runnable t = new BeanShellServer(bshport, bshfile);
            t.run(); // NOSONAR we just evaluate some code here
        }

        runInitScripts();

        int mirrorPort=JMeterUtils.getPropDefault("mirror.server.port", 0);// $NON-NLS-1$
        if (mirrorPort > 0){
            log.info("Starting Mirror server ({})", mirrorPort);
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
     * Runs user configured init scripts
     */
    static void runInitScripts() {
        // Should we run a beanshell script on startup?
        String bshinit = JMeterUtils.getProperty("beanshell.init.file");// $NON-NLS-1$
        if (bshinit != null){
            log.info("Running Beanshell on file: {}", bshinit);
            try {
                BeanShellInterpreter bsi = new BeanShellInterpreter();
                bsi.source(bshinit);
            } catch (ClassNotFoundException|JMeterException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Could not process Beanshell file: {}", e.getMessage());
                }
            }
        }

        // Should we run a JSR223 script on startup?
        String jsr223Init = JMeterUtils.getProperty(JSR223_INIT_FILE);// $NON-NLS-1$
        if (jsr223Init != null){
            log.info("Running JSR-223 init script in file: {}", jsr223Init);
            File file = new File(jsr223Init);
            if(file.exists() && file.canRead()) {
                String extension = StringUtils.defaultIfBlank(FilenameUtils.getExtension(jsr223Init), "Groovy");
                try (Reader reader = Files.newBufferedReader(file.toPath())) {
                    ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
                    ScriptEngine engine = scriptEngineManager.getEngineByExtension(extension);
                    if (engine == null) {
                        log.warn(
                                "No script engine found for [{}]. Will try to use Groovy. Possible engines and their extensions are: {}",
                                extension, getEnginesAndExtensions(scriptEngineManager));
                        extension = "Groovy";
                        engine = scriptEngineManager.getEngineByName(extension);
                    }
                    Bindings bindings = engine.createBindings();
                    final Logger logger = LoggerFactory.getLogger(JSR223_INIT_FILE);
                    bindings.put("log", logger); // $NON-NLS-1$ (this name is fixed)
                    Properties props = JMeterUtils.getJMeterProperties();
                    bindings.put("props", props); // $NON-NLS-1$ (this name is fixed)
                    // For use in debugging:
                    bindings.put("OUT", System.out); // NOSONAR $NON-NLS-1$ (this name is fixed)
                    engine.eval(reader, bindings);
                } catch (IOException | ScriptException ex) {
                    log.error("Error running init script {} with engine for {}: {}", jsr223Init, extension, ex);
                }
            } else {
                log.error("Script {} referenced by property {} is not readable or does not exist", file.getAbsolutePath(), JSR223_INIT_FILE);
            }
        }
    }


    private static Map<String, List<String>> getEnginesAndExtensions(ScriptEngineManager scriptEngineManager) {
        return scriptEngineManager.getEngineFactories().stream()
                .collect(Collectors.toMap(
                        f -> f.getLanguageName() + " (" + f.getLanguageVersion() + ")",
                        ScriptEngineFactory::getExtensions));
    }

    /**
     * Sets a proxy server for the JVM if the command line arguments are
     * specified.
     */
    private static void setProxy(CLArgsParser parser) throws IllegalUserActionException {
        if (parser.getArgumentById(PROXY_USERNAME) != null) {
            Properties jmeterProps = JMeterUtils.getJMeterProperties();
            if (parser.getArgumentById(PROXY_PASSWORD) != null) {
                String u = parser.getArgumentById(PROXY_USERNAME).getArgument();
                String p = parser.getArgumentById(PROXY_PASSWORD).getArgument();
                Authenticator.setDefault(new ProxyAuthenticator(u, p));
                log.info("Set Proxy login: {}/{}", u, p);
                jmeterProps.setProperty(HTTP_PROXY_USER, u);//for Httpclient
                jmeterProps.setProperty(HTTP_PROXY_PASS, p);//for Httpclient
            } else {
                String u = parser.getArgumentById(PROXY_USERNAME).getArgument();
                Authenticator.setDefault(new ProxyAuthenticator(u, ""));
                log.info("Set Proxy login: {}", u);
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
            String proxyScheme = null;
            if (parser.getArgumentById(PROXY_SCHEME) != null) {
                proxyScheme = parser.getArgumentById(PROXY_SCHEME).getArgument();
                if(!StringUtils.isBlank(proxyScheme)){
                    System.setProperty("http.proxyScheme",  proxyScheme );// $NON-NLS-1$
                }
            }
            if(log.isInfoEnabled()) {
                log.info("Set proxy Host: {}, Port: {}, Scheme: {}", h, p, proxyScheme != null ? proxyScheme : "Not set");
            }
        } else if (parser.getArgumentById(PROXY_HOST) != null || parser.getArgumentById(PROXY_PORT) != null) {
            throw new IllegalUserActionException(JMeterUtils.getResString("proxy_cl_error"));// $NON-NLS-1$
        }

        if (parser.getArgumentById(NONPROXY_HOSTS) != null) {
            String n = parser.getArgumentById(NONPROXY_HOSTS).getArgument();
            System.setProperty("http.nonProxyHosts",  n );// $NON-NLS-1$
            System.setProperty("https.nonProxyHosts", n );// $NON-NLS-1$
            log.info("Set http[s].nonProxyHosts: {}", n);
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
            File file = JMeterUtils.findFile(userProp);
            if (file.canRead()){
                try (FileInputStream fis = new FileInputStream(file)){
                    log.info("Loading user properties from: {}", file);
                    Properties tmp = new Properties();
                    tmp.load(fis);
                    jmeterProps.putAll(tmp);
                } catch (IOException e) {
                    log.warn("Error loading user property file: {}", userProp, e);
                }
            }
        }

        // Add local system properties, if the file is found
        String sysProp = JMeterUtils.getPropDefault("system.properties",""); //$NON-NLS-1$
        if (sysProp.length() > 0){
            File file = JMeterUtils.findFile(sysProp);
            if (file.canRead()) {
                try (FileInputStream fis = new FileInputStream(file)){
                    log.info("Loading system properties from: {}", file);
                    System.getProperties().load(fis);
                } catch (IOException e) {
                    log.warn("Error loading system property file: {}", sysProp, e);
                }
            }
        }

        // Process command line property definitions
        // These can potentially occur multiple times

        List<CLOption> clOptions = parser.getArguments();
        for (CLOption option : clOptions) {
            String name = option.getArgument(0);
            String value = option.getArgument(1);

            switch (option.getDescriptor().getId()) {

            // Should not have any text arguments
            case CLOption.TEXT_ARGUMENT:
                throw new IllegalArgumentException("Unknown arg: " + option.getArgument());

            case PROPFILE2_OPT: // Bug 33920 - allow multiple props
                log.info("Loading additional properties from: {}", name);
                try (FileInputStream fis = new FileInputStream(new File(name))){
                    Properties tmp = new Properties();
                    tmp.load(fis);
                    jmeterProps.putAll(tmp);
                } catch (FileNotFoundException e) { // NOSONAR
                    log.warn("Can't find additional property file: {}", name, e);
                } catch (IOException e) { // NOSONAR
                    log.warn("Error loading additional property file: {}", name, e);
                }
                break;
            case SYSTEM_PROPFILE:
                log.info("Setting System properties from file: {}", name);
                try (FileInputStream fis = new FileInputStream(new File(name))){
                    System.getProperties().load(fis);
                } catch (IOException e) { // NOSONAR
                    if (log.isWarnEnabled()) {
                        log.warn("Cannot find system property file. {}", e.getLocalizedMessage());
                    }
                }
                break;
            case SYSTEM_PROPERTY:
                if (value.length() > 0) { // Set it
                    log.info("Setting System property: {}={}", name, value);
                    System.getProperties().setProperty(name, value);
                } else { // Reset it
                    log.warn("Removing System property: {}", name);
                    System.getProperties().remove(name);
                }
                break;
            case JMETER_PROPERTY:
                if (value.length() > 0) { // Set it
                    log.info("Setting JMeter property: {}={}", name, value);
                    jmeterProps.setProperty(name, value);
                } else { // Reset it
                    log.warn("Removing JMeter property: {}", name);
                    jmeterProps.remove(name);
                }
                break;
            case JMETER_GLOBAL_PROP:
                if (value.length() > 0) { // Set it
                    log.info("Setting Global property: {}={}", name, value);
                    remoteProps.setProperty(name, value);
                } else {
                    File propFile = new File(name);
                    if (propFile.canRead()) {
                        log.info("Setting Global properties from the file {}", name);
                        try (FileInputStream fis = new FileInputStream(propFile)){
                            remoteProps.load(fis);
                        } catch (FileNotFoundException e) { // NOSONAR
                            if (log.isWarnEnabled()) {
                                log.warn("Could not find properties file: {}", e.getLocalizedMessage());
                            }
                        } catch (IOException e) { // NOSONAR
                            if (log.isWarnEnabled()) {
                                log.warn("Could not load properties file: {}", e.getLocalizedMessage());
                            }
                        }
                    }
                }
                break;
            case LOGLEVEL:
                if (value.length() > 0) { // Set category
                    log.info("LogLevel: {}={}", name, value);
                    final Level logLevel = Level.getLevel(value);
                    if (logLevel != null) {
                        String loggerName = name;
                        if (name.startsWith("jmeter") || name.startsWith("jorphan")) {
                            loggerName = PACKAGE_PREFIX + name;
                        }
                        Configurator.setAllLevels(loggerName, logLevel);
                    } else {
                        log.warn("Invalid log level, '{}' for '{}'.", value, name);
                    }
                } else { // Set root level
                    log.warn("LogLevel: {}", name);
                    final Level logLevel = Level.getLevel(name);
                    if (logLevel != null) {
                        Configurator.setRootLevel(logLevel);
                    } else {
                        log.warn("Invalid log level, '{}', for the root logger.", name);
                    }
                }
                break;
            case REMOTE_STOP:
                remoteStop = true;
                break;
            case FORCE_DELETE_RESULT_FILE:
                deleteResultFile = true;
                break;
            default:
                // ignored
                break;
            }
        }

        String sampleVariables = (String) jmeterProps.get(SampleEvent.SAMPLE_VARIABLES);
        if (sampleVariables != null){
            remoteProps.put(SampleEvent.SAMPLE_VARIABLES, sampleVariables);
        }
        jmeterProps.put("jmeter.version", JMeterUtils.getJMeterVersion());
    }

    /*
     * Checks for LAST or LASTsuffix.
     * Returns the LAST name with .JMX replaced by suffix.
     */
    private static String processLAST(final String jmlogfile, final String suffix) {
        if (USE_LAST_JMX.equals(jmlogfile) || USE_LAST_JMX.concat(suffix).equals(jmlogfile)){
            String last = LoadRecentProject.getRecentFile(0);// most recent
            if (last.toUpperCase(Locale.ENGLISH).endsWith(JMX_SUFFIX)){
                return last.substring(0, last.length() - JMX_SUFFIX.length()).concat(suffix);
            }
        }
        return jmlogfile;
    }

    private void startNonGui(String testFile, String logFile, CLOption remoteStart, boolean generateReportDashboard)
            throws IllegalUserActionException, ConfigurationException {
        // add a system property so samplers can check to see if JMeter
        // is running in NonGui mode
        System.setProperty(JMETER_NON_GUI, "true");// $NON-NLS-1$
        JMeter driver = new JMeter();// TODO - why does it create a new instance?
        driver.remoteProps = this.remoteProps;
        driver.remoteStop = this.remoteStop;
        driver.deleteResultFile = this.deleteResultFile;

        PluginManager.install(this, false);

        String remoteHostsString = null;
        if (remoteStart != null) {
            remoteHostsString = remoteStart.getArgument();
            if (remoteHostsString == null) {
                remoteHostsString = JMeterUtils.getPropDefault(
                        "remote_hosts", //$NON-NLS-1$
                        "127.0.0.1");//NOSONAR $NON-NLS-1$
            }
        }
        if (testFile == null) {
            throw new IllegalUserActionException("Non-GUI runs require a test plan");
        }
        driver.runNonGui(testFile, logFile, remoteStart != null, remoteHostsString, generateReportDashboard);
    }

    // run test in batch mode
    @SuppressWarnings("JdkObsolete")
    void runNonGui(String testFile, String logFile, boolean remoteStart, String remoteHostsString, boolean generateReportDashboard)
            throws ConfigurationException {
        try {
            File f = new File(testFile);
            if (!f.exists() || !f.isFile()) {
                throw new ConfigurationException("The file " + f.getAbsolutePath() + " doesn't exist or can't be opened");
            }
            FileServer.getFileServer().setBaseForScript(f);

            HashTree tree = SaveService.loadTree(f);

            @SuppressWarnings("deprecation") // Deliberate use of deprecated ctor
            JMeterTreeModel treeModel = new JMeterTreeModel(new Object());// NOSONAR Create non-GUI version to avoid headless problems
            JMeterTreeNode root = (JMeterTreeNode) treeModel.getRoot();
            treeModel.addSubTree(tree, root);

            // Hack to resolve ModuleControllers in non GUI mode
            SearchByClass<ReplaceableController> replaceableControllers =
                    new SearchByClass<>(ReplaceableController.class);
            tree.traverse(replaceableControllers);
            Collection<ReplaceableController> replaceableControllersRes = replaceableControllers.getSearchResults();
            for (ReplaceableController replaceableController : replaceableControllersRes) {
                replaceableController.resolveReplacementSubTree(root);
            }

            // Ensure tree is interpreted (ReplaceableControllers are replaced)
            // For GUI runs this is done in Start.java
            HashTree clonedTree = convertSubTree(tree, true);

            Summariser summariser = null;
            String summariserName = JMeterUtils.getPropDefault("summariser.name", "");//$NON-NLS-1$
            if (summariserName.length() > 0) {
                log.info("Creating summariser <{}>", summariserName);
                println("Creating summariser <" + summariserName + ">");
                summariser = new Summariser(summariserName);
            }
            ResultCollector resultCollector = null;
            if (logFile != null) {
                resultCollector = new ResultCollector(summariser);
                resultCollector.setFilename(logFile);
                clonedTree.add(clonedTree.getArray()[0], resultCollector);
            }
            else {
                // only add Summariser if it can not be shared with the ResultCollector
                if (summariser != null) {
                    clonedTree.add(clonedTree.getArray()[0], summariser);
                }
            }

            if (deleteResultFile) {
                SearchByClass<ResultCollector> resultListeners = new SearchByClass<>(ResultCollector.class);
                clonedTree.traverse(resultListeners);
                for (ResultCollector rc : resultListeners.getSearchResults()) {
                    File resultFile = new File(rc.getFilename());
                    if (resultFile.exists() && !resultFile.delete()) {
                        throw new IllegalStateException("Could not delete results file " + resultFile.getAbsolutePath()
                                + "(canRead:" + resultFile.canRead() + ", canWrite:" + resultFile.canWrite() + ")");
                    }
                }
            }
            ReportGenerator reportGenerator = null;
            if (logFile != null && generateReportDashboard) {
                reportGenerator = new ReportGenerator(logFile, resultCollector);
            }

            // Used for remote notification of threads start/stop,see BUG 54152
            // Summariser uses this feature to compute correctly number of threads
            // when NON GUI mode is used
            clonedTree.add(clonedTree.getArray()[0], new RemoteThreadsListenerTestElement());

            List<JMeterEngine> engines = new ArrayList<>();
            println("Created the tree successfully using "+testFile);
            if (!remoteStart) {
                JMeterEngine engine = new StandardJMeterEngine();
                clonedTree.add(clonedTree.getArray()[0], new ListenToTest(
                        org.apache.jmeter.JMeter.ListenToTest.RunMode.LOCAL, false, reportGenerator));
                engine.configure(clonedTree);
                Instant now = Instant.now();
                println("Starting standalone test @ "+ formatLikeDate(now) + " (" + now.toEpochMilli() + ')');
                engines.add(engine);
                engine.runTest();
            } else {
                java.util.StringTokenizer st = new java.util.StringTokenizer(remoteHostsString.trim(), ",");//$NON-NLS-1$
                List<String> hosts = new ArrayList<>();
                while (st.hasMoreElements()) {
                    hosts.add(((String) st.nextElement()).trim());
                }
                ListenToTest testListener = new ListenToTest(
                        org.apache.jmeter.JMeter.ListenToTest.RunMode.REMOTE, remoteStop, reportGenerator);
                clonedTree.add(clonedTree.getArray()[0], testListener);
                DistributedRunner distributedRunner=new DistributedRunner(this.remoteProps);
                distributedRunner.setStdout(System.out); // NOSONAR
                distributedRunner.setStdErr(System.err); // NOSONAR
                distributedRunner.init(hosts, clonedTree);
                engines.addAll(distributedRunner.getEngines());
                testListener.setStartedRemoteEngines(engines);
                distributedRunner.start();
            }
            startUdpDdaemon(engines);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("Error in NonGUIDriver " + e.toString());//NOSONAR
            log.error("Error in NonGUIDriver", e);
            throw new ConfigurationException("Error in NonGUIDriver " + e.getMessage(), e);
        }
    }

    private static String formatLikeDate(Instant instant) {
        return DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.LONG)
                .withLocale(Locale.ROOT)
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }

    /**
     * This function does the following:
     * <ul>
     * <li>Remove disabled elements</li>
     * <li>Replace the ReplaceableController with the target subtree</li>
     * <li>Clone the tree to ensure Commonly referenced NoThreadClone elements are cloned</li>
     * </ul>
     * @param tree The {@link HashTree} to convert
     * @deprecated This method does not correctly handle a tree with Replaceable controllers
     *     that contain NoThreadClone element. Use {@link JMeter#convertSubTree(HashTree, boolean)}
     */
    @Deprecated
    public static void convertSubTree(HashTree tree) {
        convertSubTree(tree, false);
    }

    /**
     * This function does the following:
     * <ul>
     * <li>Remove disabled elements</li>
     * <li>Replace the ReplaceableController with the target subtree</li>
     * <li>If cloneAtEnd is true : Clone the tree to ensure Commonly referenced NoThreadClone elements are cloned</li>
     * </ul>
     * THIS IS INTERNAL JMETER API and should be used with care
     * @param tree The {@link HashTree} to convert
     * @param cloneAtEnd  boolean whether we clone the tree at end
     * @return HashTree the output {@link HashTree} to use
     */
    public static HashTree convertSubTree(HashTree tree, boolean cloneAtEnd) {
        pConvertSubTree(tree);
        if(cloneAtEnd) {
            TreeCloner cloner = new TreeCloner(false);
            tree.traverse(cloner);
            return cloner.getClonedTree();
        }
        return tree;
    }

    /**
     * This function does the following:
     * <ul>
     * <li>Remove disabled elements</li>
     * <li>Replace the ReplaceableController with the target subtree</li>
     * <li>Clones the tree to ensure Commonly referenced NoThreadClone elements are cloned</li>
     * </ul>
     * @param tree The {@link HashTree} to convert
     */
    private static void pConvertSubTree(HashTree tree) {
        for (Object o : new ArrayList<>(tree.list())) {
            if (o instanceof TestElement) {
                TestElement item = (TestElement) o;
                if (item.isEnabled()) {
                    if (item instanceof ReplaceableController) {
                        ReplaceableController rc = ensureReplaceableControllerIsLoaded(item);

                        HashTree subTree = tree.getTree(item);
                        if (subTree != null) {
                            HashTree replacementTree = rc.getReplacementSubTree();
                            if (replacementTree != null) {
                                pConvertSubTree(replacementTree);
                                tree.replaceKey(item, rc);
                                tree.set(rc, replacementTree);
                            }
                        }
                    } else { // not Replaceable Controller
                        pConvertSubTree(tree.getTree(item));
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
                        TestElement controllerAsItem = item.getTestElement();
                        ReplaceableController rc = ensureReplaceableControllerIsLoaded(controllerAsItem);

                        HashTree subTree = tree.getTree(item);

                        if (subTree != null) {
                            HashTree replacementTree = rc.getReplacementSubTree();
                            if (replacementTree != null) {
                                pConvertSubTree(replacementTree);
                                tree.replaceKey(item, rc);
                                tree.set(rc, replacementTree);
                            }
                        }
                    } else { // Not a ReplaceableController
                        pConvertSubTree(tree.getTree(item));
                        TestElement testElement = item.getTestElement();
                        tree.replaceKey(item, testElement);
                    }
                 } else { // Not enabled
                    tree.remove(item);
                }
            }
        }
    }

    /**
     * Ensures the {@link ReplaceableController} is loaded
     * @param item {@link TestElement}
     * @return {@link ReplaceableController} loaded
     */
    private static ReplaceableController ensureReplaceableControllerIsLoaded(
            TestElement item) {
        ReplaceableController rc;
        // TODO this bit of code needs to be tidied up
        // Unfortunately ModuleController is in components, not core
        if ("org.apache.jmeter.control.ModuleController".equals(item.getClass().getName())){ // NOSONAR (comparison is intentional) Bug 47165
            rc = (ReplaceableController) item;
        } else {
            // HACK: force the controller to load its tree
            rc = (ReplaceableController) item.clone();
        }
        return rc;
    }

    /*
     * Listen to test and handle tidyup after non-GUI test completes.
     * If running a remote test, then after waiting a few seconds for listeners to finish files,
     * it calls ClientJMeterEngine.tidyRMI() to deal with the Naming Timer Thread.
     */
    private static class ListenToTest implements TestStateListener, Remoteable {
        enum RunMode {
            LOCAL,
            REMOTE
        }

        private AtomicInteger startedRemoteEngines = new AtomicInteger(0);

        private final ConcurrentLinkedQueue<JMeterEngine> remoteEngines = new ConcurrentLinkedQueue<>();

        private final ReportGenerator reportGenerator;

        private final RunMode runMode;

        private final boolean remoteStop;

        /**
         * Listener for remote test
         * @param runMode RunMode
         * @param remoteStop
         * @param reportGenerator {@link ReportGenerator}
         */
        public ListenToTest(RunMode runMode, boolean remoteStop, ReportGenerator reportGenerator) {
            this.runMode = runMode;
            this.remoteStop = remoteStop;
            this.reportGenerator = reportGenerator;
        }

        public void setStartedRemoteEngines(List<? extends JMeterEngine> engines) {
            if (runMode != RunMode.REMOTE) {
                throw new IllegalArgumentException("This method should only be called in RunMode.REMOTE");
            }
            this.remoteEngines.clear();
            this.remoteEngines.addAll(engines);
            this.startedRemoteEngines = new AtomicInteger(remoteEngines.size());
        }

        @Override
        // N.B. this is called by a daemon RMI thread from the remote host
        public void testEnded(String host) {
            final long now=System.currentTimeMillis();
            log.info("Finished remote host: {} ({})", host, now);
            if (startedRemoteEngines.decrementAndGet() <= 0) {
                log.info("All remote engines have ended test, starting RemoteTestStopper thread");
                Thread stopSoon = new Thread(() -> endTest(true), "RemoteTestStopper");
                // the calling thread is a daemon; this thread must not be
                // see Bug 59391
                stopSoon.setDaemon(false);
                stopSoon.start();
            }
        }

        @Override
        public void testEnded() {
            endTest(false);
        }

        @Override
        public void testStarted(String host) {
            final long now=System.currentTimeMillis();
            log.info("Started remote host:  {} ({})", host, now);
        }

        @Override
        public void testStarted() {
            if (log.isInfoEnabled()) {
                final long now = System.currentTimeMillis();
                log.info("{} ({})", JMeterUtils.getResString("running_test"), now);//$NON-NLS-1$
            }
        }

        @SuppressWarnings("JdkObsolete")
        private void endTest(boolean isDistributed) {
            Instant now = Instant.now();
            if (isDistributed) {
                println("Tidying up remote @ " + formatLikeDate(now) + " (" + now.toEpochMilli() + ')');
            } else {
                println("Tidying up ...    @ " + formatLikeDate(now) + " (" + now.toEpochMilli() + ')');
            }

            if (isDistributed) {
                if (remoteStop) {
                    println("Exiting remote servers:"+remoteEngines);
                    for (JMeterEngine engine : remoteEngines){
                        println("Exiting remote server:"+engine);
                        engine.exit();
                    }
                }
                try {
                    TimeUnit.SECONDS.sleep(5); // Allow listeners to close files
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                ClientJMeterEngine.tidyRMI(log);
            }

            if(reportGenerator != null) {
                try {
                    log.info("Generating Dashboard");
                    reportGenerator.generate();
                    log.info("Dashboard generated");
                } catch (Exception ex) {
                    System.err.println("Error generating the report: "+ex);//NOSONAR
                    log.error("Error generating the report: {}", ex.getMessage(), ex);
                }
            }
            checkForRemainingThreads();
            println("... end of run");
        }

        /**
         * Runs daemon thread which waits a short while;
         * if JVM does not exit, lists remaining non-daemon threads on stdout.
         */
        private static void checkForRemainingThreads() {
            // This cannot be a JMeter class variable, because properties
            // are not initialised until later.
            final int pauseToCheckForRemainingThreads =
                    JMeterUtils.getPropDefault("jmeter.exit.check.pause", 0); // $NON-NLS-1$

            if (pauseToCheckForRemainingThreads > 0) {
                Thread daemon = new Thread(){
                    @Override
                    public void run(){
                        try {
                            TimeUnit.MILLISECONDS.sleep(pauseToCheckForRemainingThreads); // Allow enough time for JVM to exit
                        } catch (InterruptedException ignored) {
                            Thread.currentThread().interrupt();
                        }
                        // This is a daemon thread, which should only reach here if there are other
                        // non-daemon threads still active
                        System.out.println("The JVM should have exited but did not.");//NOSONAR
                        System.out.println("The following non-daemon threads are still running (DestroyJavaVM is OK):");//NOSONAR
                        JOrphanUtils.displayThreads(false);
                    }

                };
                daemon.setDaemon(true);
                daemon.start();
            } else if (pauseToCheckForRemainingThreads<=0) {
                log.debug("jmeter.exit.check.pause is <= 0, JMeter won't check for unterminated non-daemon threads");
            }
        }
    }

    private static void println(String str) {
        System.out.println(str);//NOSONAR
    }

    @Override
    @SuppressWarnings("JdkObsolete")
    public String[][] getIconMappings() {
        final String defaultIconProp = "org/apache/jmeter/images/icon.properties"; //$NON-NLS-1$
        final String iconSize = JMeterUtils.getPropDefault(TREE_ICON_SIZE, DEFAULT_TREE_ICON_SIZE);
        String iconProp = JMeterUtils.getPropDefault("jmeter.icons", defaultIconProp);//$NON-NLS-1$
        Properties p = JMeterUtils.loadProperties(iconProp);
        if (p == null && !iconProp.equals(defaultIconProp)) {
            log.info("{} not found - using {}", iconProp, defaultIconProp);
            iconProp = defaultIconProp;
            p = JMeterUtils.loadProperties(iconProp);
        }
        if (p == null) {
            log.info("{} not found - using inbuilt icon set", iconProp);
            return DEFAULT_ICONS;
        }
        log.info("Loaded icon properties from {}", iconProp);
        String[][] iconlist = new String[p.size()][3];
        Enumeration<?> pe = p.keys();
        int i = 0;
        while (pe.hasMoreElements()) {
            String key = (String) pe.nextElement();
            String[] icons = JOrphanUtils.split(p.getProperty(key), " ");//$NON-NLS-1$
            iconlist[i][0] = key;
            iconlist[i][1] = icons[0].replace(KEY_SIZE, iconSize);
            if (icons.length > 1) {
                iconlist[i][2] = icons[1].replace(KEY_SIZE, iconSize);
            }
            i++;
        }
        return iconlist;
    }

    @Override
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

    private static void startUdpDdaemon(final List<? extends JMeterEngine> engines) {
        int port = JMeterUtils.getPropDefault("jmeterengine.nongui.port", UDP_PORT_DEFAULT); // $NON-NLS-1$
        int maxPort = JMeterUtils.getPropDefault("jmeterengine.nongui.maxport", 4455); // $NON-NLS-1$
        if (port > 1000){
            final DatagramSocket socket = getSocket(port, maxPort);
            if (socket != null) {
                Thread waiter = new Thread("UDP Listener"){
                    @Override
                    public void run() {
                        waitForSignals(engines, socket);
                    }
                };
                waiter.setDaemon(true);
                waiter.start();
            } else {
                System.out.println("Failed to create UDP port");//NOSONAR
            }
        }
    }

    private static void waitForSignals(final List<? extends JMeterEngine> engines, DatagramSocket socket) {
        byte[] buf = new byte[80];
        System.out.println("Waiting for possible Shutdown/StopTestNow/HeapDump/ThreadDump message on port "+socket.getLocalPort());//NOSONAR
        DatagramPacket request = new DatagramPacket(buf, buf.length);
        try {
            while(true) {
                socket.receive(request);
                InetAddress address = request.getAddress();
                // Only accept commands from the local host
                if (address.isLoopbackAddress()){
                    String command = new String(request.getData(), request.getOffset(), request.getLength(), StandardCharsets.US_ASCII);
                    System.out.println("Command: "+command+" received from "+address);//NOSONAR
                    log.info("Command: {} received from {}", command, address);
                    switch(command) {
                        case "StopTestNow" :
                            for(JMeterEngine engine : engines) {
                                engine.stopTest(true);
                            }
                            break;
                        case "Shutdown" :
                            for(JMeterEngine engine : engines) {
                                engine.stopTest(false);
                            }
                            break;
                        case "HeapDump" :
                            HeapDumper.dumpHeap();
                            break;
                        case "ThreadDump" :
                            ThreadDumper.threadDump();
                            break;
                        default:
                            System.out.println("Command: "+command+" not recognised ");//NOSONAR
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);//NOSONAR
        } finally {
            socket.close();
        }
    }

    private static DatagramSocket getSocket(int udpPort, int udpPortMax) {
        DatagramSocket socket = null;
        int i = udpPort;
        while (i<= udpPortMax) {
            try {
                socket = new DatagramSocket(i);
                break;
            } catch (SocketException e) { // NOSONAR
                i++;
            }
        }

        return socket;
    }
}
