// $Header$
/*
 * Copyright 2000-2004 The Apache Software Foundation.
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
import java.net.Authenticator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.avalon.excalibur.cli.CLArgsParser;
import org.apache.avalon.excalibur.cli.CLOption;
import org.apache.avalon.excalibur.cli.CLOptionDescriptor;
import org.apache.avalon.excalibur.cli.CLUtil;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.control.gui.WorkBenchGui;
import org.apache.jmeter.engine.ClientJMeterEngine;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.RemoteJMeterEngineImpl;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.CheckDirty;
import org.apache.jmeter.gui.action.Load;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.plugin.JMeterPlugin;
import org.apache.jmeter.plugin.PluginManager;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.processor.gui.AbstractPreProcessorGui;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.timers.gui.AbstractTimerGui;
import org.apache.jmeter.util.BeanShellServer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author mstover
 * @version $Revision$
 */
public class JMeter implements JMeterPlugin
{
    transient private static Logger log = LoggingManager.getLoggerForClass();

    private static final int PROPFILE_OPT = 'p';
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

    /**
     * Define the understood options. Each CLOptionDescriptor contains:
     * <ul>
     *   <li>The "long" version of the option. Eg, "help" means that "--help"
     *       will be recognised.</li> 
     *   <li>The option flags, governing the option's argument(s).</li>
     *   <li>The "short" version of the option. Eg, 'h' means that "-h" will
     *       be recognised.</li>
     *   <li>A description of the option.</li>
     * </ul>
     */
    protected static final CLOptionDescriptor[] options =
        new CLOptionDescriptor[] {
            new CLOptionDescriptor(
                "help",
                CLOptionDescriptor.ARGUMENT_DISALLOWED,
                HELP_OPT,
                "print usage information and exit"),
            new CLOptionDescriptor(
                "version",
                CLOptionDescriptor.ARGUMENT_DISALLOWED,
                VERSION_OPT,
                "print the version information and exit"),
            new CLOptionDescriptor(
                "propfile",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                PROPFILE_OPT,
                "the jmeter property file to use"),
            new CLOptionDescriptor(
                "testfile",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                TESTFILE_OPT,
                "the jmeter test(.jmx) file to run"),
            new CLOptionDescriptor(
                "logfile",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                LOGFILE_OPT,
                "the file to log samples to"),
            new CLOptionDescriptor(
                "nongui",
                CLOptionDescriptor.ARGUMENT_DISALLOWED,
                NONGUI_OPT,
                "run JMeter in nongui mode"),
            new CLOptionDescriptor(
                "server",
                CLOptionDescriptor.ARGUMENT_DISALLOWED,
                SERVER_OPT,
                "run the JMeter server"),
            new CLOptionDescriptor(
                "proxyHost",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                PROXY_HOST,
                "Set a proxy server for JMeter to use"),
            new CLOptionDescriptor(
                "proxyPort",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                PROXY_PORT,
                "Set proxy server port for JMeter to use"),
            new CLOptionDescriptor(
                "username",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                PROXY_USERNAME,
                "Set username for proxy server that JMeter is to use"),
            new CLOptionDescriptor(
                "password",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                PROXY_PASSWORD,
                "Set password for proxy server that JMeter is to use"),
            new CLOptionDescriptor(
                "jmeterproperty",
                CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENTS_REQUIRED_2,
                JMETER_PROPERTY,
                "Define additional JMeter properties"),
            new CLOptionDescriptor(
                "systemproperty",
                CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENTS_REQUIRED_2,
                SYSTEM_PROPERTY,
                "Define additional JMeter properties"),
            new CLOptionDescriptor(
                "loglevel",
                CLOptionDescriptor.DUPLICATES_ALLOWED
                    | CLOptionDescriptor.ARGUMENTS_REQUIRED_2,
                LOGLEVEL,
                "Define loglevel: [category=]level e.g. jorphan=INFO or " +
                        "jmeter.util=DEBUG"),
            new CLOptionDescriptor(
                "runremote",
                CLOptionDescriptor.ARGUMENT_DISALLOWED,
                REMOTE_OPT,
                "Start remote servers from non-gui mode")};

    public JMeter()
    {
    }

    /**
     * Starts up JMeter in GUI mode
     */
    public void startGui(CLOption testFile)
        throws
            IllegalUserActionException,
            IllegalAccessException,
            ClassNotFoundException,
            InstantiationException
    {

        PluginManager.install(this, true);
        JMeterTreeModel treeModel = new JMeterTreeModel();
        JMeterTreeListener treeLis = new JMeterTreeListener(treeModel);
        treeLis.setActionHandler(ActionRouter.getInstance());
        //NOTUSED: GuiPackage guiPack = 
        GuiPackage.getInstance(treeLis, treeModel);
        org.apache.jmeter.gui.MainFrame main =
            new org.apache.jmeter.gui.MainFrame(
                ActionRouter.getInstance(),
                treeModel,
                treeLis);
        main.setTitle("Apache JMeter");
        main.setIconImage(JMeterUtils.getImage("jmeter.jpg").getImage());
        ComponentUtil.centerComponentInWindow(main, 80);
        main.show();
        ActionRouter.getInstance().actionPerformed(
            new ActionEvent(main, 1, CheckDirty.ADD_ALL));
        if (testFile != null)
        {
            try
            {
                File f = new File(testFile.getArgument());
                log.info("Loading file: " + f);
                FileInputStream reader = new FileInputStream(f);
                HashTree tree = SaveService.loadSubTree(reader);
                new Load().insertLoadedTree(1, tree);
            }
            catch (Exception e)
            {
                log.error("Failure loading test file", e);
            }
        }
    }

    /**
     * Takes the command line arguments and uses them to determine how to
     * startup JMeter.
     */
    public void start(String[] args)
    {

        CLArgsParser parser = new CLArgsParser(args, options);
        if (null != parser.getErrorString())
        {
            System.err.println("Error: " + parser.getErrorString());
			System.out.println("Usage");
			System.out.println(CLUtil.describeOptions(options).toString());
            return;
        }
        try
        {
            initializeProperties(parser);
            setProxy(parser);
            log.info("Version " + JMeterUtils.getJMeterVersion());
			log.info("java.version="+System.getProperty("java.version"));
			log.info(JMeterUtils.getJMeterCopyright());
            if (parser.getArgumentById(VERSION_OPT) != null)
            {
                System.out.println(JMeterUtils.getJMeterCopyright());
                System.out.println("Version " + JMeterUtils.getJMeterVersion());
            }
            else if (parser.getArgumentById(HELP_OPT) != null)
            {
                System.out.println(
                    JMeterUtils.getResourceFileAsText(
                        "org/apache/jmeter/help.txt"));
            }
            else if (parser.getArgumentById(SERVER_OPT) != null)
            {
                startServer();
                startBSH();
            }
            else if (parser.getArgumentById(NONGUI_OPT) == null)
            {
                startGui(parser.getArgumentById(TESTFILE_OPT));
				startBSH();
            }
            else
            {
                startNonGui(
                    parser.getArgumentById(TESTFILE_OPT),
                    parser.getArgumentById(LOGFILE_OPT),
                    parser.getArgumentById(REMOTE_OPT));
				startBSH();
            }
        }
        catch (IllegalUserActionException e)
        {
            System.out.println(e.getMessage());
            System.out.println("Incorrect Usage");
            System.out.println(CLUtil.describeOptions(options).toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("An error occurred: " + e.getMessage());
            System.exit(-1);
        }
    }

    /**
	 * 
	 */
	private void startBSH() {
		int    bshport = JMeterUtils.getPropDefault("beanshell.server.port",0);
		String bshfile = JMeterUtils.getPropDefault("beanshell.server.file","");
		if (bshport > 0 ){
			log.info("Starting Beanshell server ("+bshport+","+bshfile+")");
		    Runnable t = new BeanShellServer(bshport,bshfile);
		    t.run();
		}
	}

	/**
     * Sets a proxy server for the JVM if the command line arguments are
     * specified.
     */
    private void setProxy(CLArgsParser parser)
        throws IllegalUserActionException
    {
        if (parser.getArgumentById(PROXY_USERNAME) != null)
        {
            if (parser.getArgumentById(PROXY_PASSWORD) != null)
            {
                Authenticator.setDefault(
                    new ProxyAuthenticator(
                        parser.getArgumentById(PROXY_USERNAME).getArgument(),
                        parser.getArgumentById(PROXY_PASSWORD).getArgument()));
            }
            else
            {
                Authenticator.setDefault(
                    new ProxyAuthenticator(
                        parser.getArgumentById(PROXY_USERNAME).getArgument(),
                        ""));
            }
        }
        if (parser.getArgumentById(PROXY_HOST) != null
            && parser.getArgumentById(PROXY_PORT) != null)
        {
            System.setProperty(
                "http.proxyHost",
                parser.getArgumentById(PROXY_HOST).getArgument());
            System.setProperty(
                "https.proxyHost",
                parser.getArgumentById(PROXY_HOST).getArgument());
            System.setProperty(
                "http.proxyPort",
                parser.getArgumentById(PROXY_PORT).getArgument());
            System.setProperty(
                "https.proxyPort",
                parser.getArgumentById(PROXY_PORT).getArgument());
        }
        else if (
            parser.getArgumentById(PROXY_HOST) != null
                || parser.getArgumentById(PROXY_PORT) != null)
        {
            throw new IllegalUserActionException(
                JMeterUtils.getResString("proxy_cl_error"));
        }
    }

    private void initializeProperties(CLArgsParser parser)
    {
        if (parser.getArgumentById(PROPFILE_OPT) != null)
        {
            JMeterUtils.getProperties(
                parser.getArgumentById(PROPFILE_OPT).getArgument());
        }
        else
        {
            JMeterUtils.getProperties(
                NewDriver.getJMeterDir()
                    + File.separator
                    + "bin"
                    + File.separator
                    + "jmeter.properties");
        }
        JMeterUtils.setJMeterHome(NewDriver.getJMeterDir());

        // Process command line property definitions (can occur multiple times)

        Properties jmeterProps = JMeterUtils.getJMeterProperties();
        List clOptions = parser.getArguments();
        int size = clOptions.size();

        for (int i = 0; i < size; i++)
        {
            CLOption option = (CLOption) clOptions.get(i);
            String name = option.getArgument(0);
            String value = option.getArgument(1);

            switch (option.getId())
            {
                case SYSTEM_PROPERTY :
                    if (value.length() > 0)
                    { // Set it
                        log.info(
                            "Setting System property: " + name + "=" + value);
                        System.getProperties().setProperty(name, value);
                    }
                    else
                    { // Reset it
                        log.warn("Removing System property: " + name);
                        System.getProperties().remove(name);
                    }
                    break;
                case JMETER_PROPERTY :
                    if (value.length() > 0)
                    { // Set it
                        log.info(
                            "Setting JMeter property: " + name + "=" + value);
                        jmeterProps.setProperty(name, value);
                    }
                    else
                    { // Reset it
                        log.warn("Removing JMeter property: " + name);
                        jmeterProps.remove(name);
                    }
                    break;
                case LOGLEVEL :
                    if (value.length() > 0)
                    { // Set category
                        log.info("LogLevel: " + name + "=" + value);
                        LoggingManager.setPriority(value, name);
                    }
                    else
                    { // Set root level
                        log.warn("LogLevel: " + name);
                        LoggingManager.setPriority(name);
                    }
                    break;
            }
        }

    }

    public void startServer()
    {
        try
        {
            new RemoteJMeterEngineImpl();
            while (true)
            {
                Thread.sleep(Long.MAX_VALUE);
            }
        }
        catch (Exception ex)
        {
            log.error("Giving up, as server failed with:", ex);
            System.exit(0);// Give up
        }
    }

    public void startNonGui(
        CLOption testFile,
        CLOption logFile,
        CLOption remoteStart)
        throws
            IllegalUserActionException,
            IllegalAccessException,
            ClassNotFoundException,
            InstantiationException
    {
    	// add a system property so samplers can check to see if JMeter
    	// is running in NonGui mode
		System.setProperty("JMeter.NonGui","true");    	
        JMeter driver = new JMeter();
        PluginManager.install(this, false);

        if (testFile == null)
        {
            throw new IllegalUserActionException();
        }
        if (logFile == null)
        {
            driver.run(testFile.getArgument(), null, remoteStart != null);
        }
        else
        {
            driver.run(
                testFile.getArgument(),
                logFile.getArgument(),
                remoteStart != null);
        }
    }

    private void run(String testFile, String logFile, boolean remoteStart)
    { // run test in batch mode
        FileInputStream reader = null;
        try
        {
            File f = new File(testFile);
            if (!f.exists() || !f.isFile())
            {
                println("Could not open " + testFile);
                return;
            }

            reader = new FileInputStream(f);
            log.info("Loading file: " + f);

            HashTree tree = SaveService.loadSubTree(reader);

            // Remove the disabled items
            // For GUI runs this is done in Start.java
			convertSubTree(tree);

            if (logFile != null)
            {
                ResultCollector logger = new ResultCollector();
                logger.setFilename(logFile);
                tree.add(tree.getArray()[0], logger);
            }
            String summariserName=
                JMeterUtils.getPropDefault("summariser.name","");//$NON-NLS-1$
            if (summariserName.length() > 0){
            	log.info("Creating summariser <"+summariserName+">");
				println( "Creating summariser <"+summariserName+">");
            	Summariser summer=new Summariser(summariserName);
				tree.add(tree.getArray()[0], summer);
            }
            tree.add(tree.getArray()[0], new ListenToTest());
            println("Created the tree successfully");
            JMeterEngine engine = null;
            if (!remoteStart)
            {
                engine = new StandardJMeterEngine();
                engine.configure(tree);
                println("Starting the test");
                engine.runTest();
            }
            else
            {
                String remote_hosts_string =
                    JMeterUtils.getPropDefault("remote_hosts", "127.0.0.1");
                java.util.StringTokenizer st =
                    new java.util.StringTokenizer(remote_hosts_string, ",");
                List engines = new LinkedList();
                while (st.hasMoreElements())
                {
                    String el = (String) st.nextElement();
					println("Configuring remote engine for "+el);
                    engines.add(doRemoteInit(el.trim(), tree));
                }
                println("Starting remote engines");
                Iterator iter = engines.iterator();
                while (iter.hasNext())
                {
                    engine = (JMeterEngine) iter.next();
                    engine.runTest();
                }
				println("Remote engines have been started");
            }
        }
        catch (Exception e)
        {
            System.out.println("Error in NonGUIDriver " + e.toString());
            log.error("", e);
        }
    }

    private boolean isEnabled(TestElement te){//TODO - belongs in TestElement ...
    	return 
    	te.getProperty(TestElement.ENABLED) instanceof NullProperty
    	||
    	te.getPropertyAsBoolean(TestElement.ENABLED);
    }
    
    /**
     * Code copied from AbstractAction.java and modified to suit TestElements
	 * @param tree
	 */
	private void convertSubTree(HashTree tree) {//TODO check build dependencies
			Iterator iter = new LinkedList(tree.list()).iterator();
			while (iter.hasNext())
			{
				TestElement item = (TestElement) iter.next();
				if (isEnabled(item))
				{//TODO handle ReplaceableControllers
//					if (item instanceof ReplaceableController)
//					{
//						System.out.println("Replaceable "+item.getClass().getName());
//						HashTree subTree = tree.getTree(item);
//
//						if (subTree != null)
//						{
//							ReplaceableController rc =
//								(ReplaceableController) item;//.createTestElement();
//							rc.replace(subTree);
//							convertSubTree(subTree);
//							tree.replace(item, rc.getReplacement());
//						}
//					}
//					else
					{
						//System.out.println("NonReplaceable "+item.getClass().getName());
						convertSubTree(tree.getTree(item));
//						TestElement testElement = item.createTestElement();
//						tree.replace(item, testElement);
					}
				}
				else
				{
					//System.out.println("Disabled "+item.getClass().getName());
					tree.remove(item);
				}
			}
	}

	private JMeterEngine doRemoteInit(String hostName, HashTree testTree)
    {
        JMeterEngine engine = null;
        try
        {
            engine = new ClientJMeterEngine(hostName);
        }
        catch (Exception e)
        {
            log.fatalError("Failure connecting to remote host", e);
            System.exit(0);
        }
        engine.configure(testTree);
        return engine;
    }

    /**
     * Listen to test and exit program after test completes, after a 5 second
     * delay to give listeners a chance to close out their files.
     */
    private class ListenToTest implements TestListener, Runnable, Remoteable
    {
        int started = 0;
        public synchronized void testEnded(String host)
        {
            started--;
            log.info("Remote host " + host + " finished");
            if (started == 0)
            {
                testEnded();
            }
        }

        public void testEnded()
        {
            Thread stopSoon = new Thread(this);
            stopSoon.start();
        }

        public synchronized void testStarted(String host)
        {
            started++;
            log.info("Started remote host: " + host);
        }

        public void testStarted()
        {
            log.info(JMeterUtils.getResString("running_test"));
        }

        /**
         * This is a hack to allow listeners a chance to close their files.
         * Must implement a queue for sample responses tied to the engine, and
         * the engine won't deliver testEnded signal till all sample responses
         * have been delivered.  Should also improve performance of remote
         * JMeter testing.
         */
        public void run()
        {
			println("Tidying up ...");
            try
            {
                Thread.sleep(5000);
            }
            catch (InterruptedException e)
            {
            }
            println("... end of run");
            System.exit(0);
        }
        /**
         * @see TestListener#iterationStart(IterationEvent)
         */
        public void testIterationStart(LoopIterationEvent event)
        {
        }
    }

    private static void println(String str)
    {
        System.out.println(str);
    }

    public String[][] getIconMappings()
    {
        return new String[][] {
            {
                TestPlanGui.class.getName(),
                "org/apache/jmeter/images/beaker.gif" },
            {
                AbstractTimerGui.class.getName(),
                    "org/apache/jmeter/images/timer.gif" },
                    {
                ThreadGroupGui.class.getName(),
                    "org/apache/jmeter/images/thread.gif" },
                    {
                AbstractVisualizer.class.getName(),
                    "org/apache/jmeter/images/meter.png" },
                    {
                AbstractConfigGui.class.getName(),
                    "org/apache/jmeter/images/testtubes.png" },
                    {
                AbstractPreProcessorGui.class.getName(),
                    "org/apache/jmeter/images/testtubes.gif" },
                    {
                AbstractPostProcessorGui.class.getName(),
                    "org/apache/jmeter/images/testtubes.gif" },
                    {
                AbstractControllerGui.class.getName(),
                    "org/apache/jmeter/images/knob.gif" },
                    {
                WorkBenchGui.class.getName(),
                    "org/apache/jmeter/images/clipboard.gif" },
                    {
                AbstractSamplerGui.class.getName(),
                    "org/apache/jmeter/images/pipet.png" }
        };
    }

    public String[][] getResourceBundles()
    {
        return new String[0][];
    }
}