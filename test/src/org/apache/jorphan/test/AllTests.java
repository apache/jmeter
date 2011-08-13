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

package org.apache.jorphan.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * Provides a quick and easy way to run all <a
 * href="http://junit.sourceforge.net">junit</a> unit tests in your java
 * project. It will find all unit test classes and run all their test methods.
 * There is no need to configure it in any way to find these classes except to
 * give it a path to search.
 * <p>
 * Here is an example Ant target (See Ant at <a
 * href="http://jakarta.apache.org/ant">Apache</a>) that runs all your unit
 * tests:
 * 
 * <pre>
 * 
 *       &lt;target name=&quot;test&quot; depends=&quot;compile&quot;&gt;
 *           &lt;java classname=&quot;org.apache.jorphan.test.AllTests&quot; fork=&quot;yes&quot;&gt;
 *               &lt;classpath&gt;
 *                   &lt;path refid=&quot;YOUR_CLASSPATH&quot;/&gt;
 *                   &lt;pathelement location=&quot;ROOT_DIR_OF_YOUR_COMPILED_CLASSES&quot;/&gt;
 *               &lt;/classpath&gt;
 *               &lt;arg value=&quot;SEARCH_PATH/&quot;/&gt;
 *               &lt;arg value=&quot;PROPERTY_FILE&quot;/&gt;
 *               &lt;arg value=&quot;NAME_OF_UNITTESTMANAGER_CLASS&quot;/&gt;
 *           &lt;/java&gt;
 *       &lt;/target&gt;
 *  
 * </pre>
 * 
 * <dl>
 * <dt>YOUR_CLASSPATH</dt>
 * <dd>Refers to the classpath that includes all jars and libraries need to run
 * your unit tests</dd>
 * 
 * <dt>ROOT_DIR_OF_YOUR_COMPILED_CLASSES</dt>
 * <dd>The classpath should include the directory where all your project's
 * classes are compiled to, if it doesn't already.</dd>
 * 
 * <dt>SEARCH_PATH</dt>
 * <dd>The first argument tells AllTests where to look for unit test classes to
 * execute. In most cases, it is identical to ROOT_DIR_OF_YOUR_COMPILED_CLASSES.
 * You can specify multiple directories or jars to search by providing a
 * comma-delimited list.</dd>
 * 
 * <dt>PROPERTY_FILE</dt>
 * <dd>A simple property file that sets logging parameters. It is optional and
 * is only relevant if you use the same logging packages that JOrphan uses.</dd>
 * 
 * <dt>NAME_OF_UNITTESTMANAGER_CLASS</dt>
 * <dd>If your system requires some configuration to run correctly, you can
 * implement the {@link UnitTestManager} interface and be given an opportunity
 * to initialize your system from a configuration file.</dd>
 * </dl>
 * 
 * @see UnitTestManager
 */
public final class AllTests {
    private static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * Private constructor to prevent instantiation.
     */
    private AllTests() {
    }

    private static void logprop(String prop, boolean show) {
        String value = System.getProperty(prop);
        log.info(prop + "=" + value);
        if (show) {
            System.out.println(prop + "=" + value);
        }
    }

    private static void logprop(String prop) {
        logprop(prop, false);
    }

    /**
     * Starts a run through all unit tests found in the specified classpaths.
     * The first argument should be a list of paths to search. The second
     * argument is optional and specifies a properties file used to initialize
     * logging. The third argument is also optional, and specifies a class that
     * implements the UnitTestManager interface. This provides a means of
     * initializing your application with a configuration file prior to the
     * start of any unit tests.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("You must specify a comma-delimited list of paths to search " + "for unit tests");
            return;
        }
        String home=new File(System.getProperty("user.dir")).getParent();
        System.out.println("Setting JMeterHome: "+home);
        JMeterUtils.setJMeterHome(home);
        initializeLogging(args);
        initializeManager(args);

        String version = "JMeterVersion="+JMeterUtils.getJMeterVersion();
        log.info(version);
        System.out.println(version);
        logprop("java.version", true);
        logprop("java.vm.name");
        logprop("java.vendor");
        logprop("java.home", true);
        logprop("file.encoding", true);
        // Display actual encoding used (will differ if file.encoding is not recognised)
        String msg = "default encoding="+Charset.defaultCharset();
        System.out.println(msg);
        log.info(msg);
        logprop("user.home");
        logprop("user.dir", true);
        logprop("user.language");
        logprop("user.region");
        logprop("user.country");
        logprop("user.variant");
        log.info("Locale="+Locale.getDefault().toString());
        logprop("os.name", true);
        logprop("os.version", true);
        logprop("os.arch");
        logprop("java.class.version");
        // logprop("java.class.path");
        String cp = System.getProperty("java.class.path");
        String cpe[] = JOrphanUtils.split(cp, java.io.File.pathSeparator);
        StringBuilder sb = new StringBuilder(3000);
        sb.append("java.class.path=");
        for (int i = 0; i < cpe.length; i++) {
            sb.append("\n");
            sb.append(cpe[i]);
            if (new java.io.File(cpe[i]).exists()) {
                sb.append(" - OK");
            } else {
                sb.append(" - ??");
            }
        }
        log.info(sb.toString());

        // ++
        // GUI tests throw the error
        // testArgumentCreation(org.apache.jmeter.config.gui.ArgumentsPanel$Test)java.lang.NoClassDefFoundError
        // at java.lang.Class.forName0(Native Method)
        // at java.lang.Class.forName(Class.java:141)
        // at
        // java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment(GraphicsEnvironment.java:62)
        //
        // Try to find out why this is ...

        System.out.println("+++++++++++");
        logprop("java.awt.headless", true);
        logprop("java.awt.graphicsenv", true);
        //
        // try {//
        // Class c = Class.forName(n);
        // System.out.println("Found class: "+n);
        // // c.newInstance();
        // // System.out.println("Instantiated: "+n);
        // } catch (Exception e1) {
        // System.out.println("Error finding class "+n+" "+e1);
        // } catch (java.lang.InternalError e1){
        // System.out.println("Error finding class "+n+" "+e1);
        // }
        //
        System.out.println("------------");
        // don't call isHeadless() here, as it has a side effect.
        // --
        System.out.println("Creating test suite");
        TestSuite suite = suite(args[0]);
        int countTestCases = suite.countTestCases();
        System.out.println("Starting test run, test count = "+countTestCases);
//        for (int i=0;i<suite.testCount();i++){
//           Test testAt = suite.testAt(i);
//           int testCases = testAt.countTestCases();
//           if (testAt instanceof junit.framework.TestCase){
//                System.out.print(((junit.framework.TestCase) testAt).getName());
//            }
//            if (testAt instanceof TestSuite){
//                TestSuite testSuite = ((TestSuite) testAt);
//                String name = testSuite.getName();
//                System.out.print(name);
//                System.out.println(" "+testCases);
//            }                
//        }
        
        // Jeremy Arnold: This method used to attempt to write results to
        // a file, but it had a bug and instead just wrote to System.out.
        // Since nobody has complained about this behavior, I'm changing
        // the code to not attempt to write to a file, so it will continue
        // behaving as it did before. It would be simple to make it write
        // to a file instead if that is the desired behavior.
        TestResult result = TestRunner.run(suite);
        // ++
        // Recheck settings:
        //System.out.println("+++++++++++");
        // System.out.println(e+"="+System.getProperty(e));
        // System.out.println(g+"="+System.getProperty(g));
        // System.out.println("Headless?
        // "+java.awt.GraphicsEnvironment.isHeadless());
        // try {
        // Class c = Class.forName(n);
        // System.out.println("Found class: "+n);
        // c.newInstance();
        // System.out.println("Instantiated: "+n);
        // } catch (Exception e1) {
        // System.out.println("Error with class "+n+" "+e1);
        // } catch (java.lang.InternalError e1){
        // System.out.println("Error with class "+n+" "+e1);
        // }
        //System.out.println("------------");
        // --
        System.exit(result.wasSuccessful() ? 0 : 1); // this is needed because the test may start the AWT EventQueue thread which is not a daemon.
    }

    /**
     * An overridable method that initializes the logging for the unit test run,
     * using the properties file passed in as the second argument.
     * 
     * @param args
     */
    protected static void initializeLogging(String[] args) {
        if (args.length >= 2) {
            Properties props = new Properties();
            try {
                System.out.println("Setting up logging props using file: " + args[1]);
                props.load(new FileInputStream(args[1]));
                LoggingManager.initializeLogging(props);
            } catch (FileNotFoundException e) {
                System.out.println(e.getLocalizedMessage());
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
            }
        }
    }

    /**
     * An overridable method that that instantiates a UnitTestManager (if one
     * was specified in the command-line arguments), and hands it the name of
     * the properties file to use to configure the system.
     * 
     * @param args
     */
    protected static void initializeManager(String[] args) {
        if (args.length >= 3) {
            try {
                System.out.println("Using initializeProperties() from " + args[2]);
                UnitTestManager um = (UnitTestManager) Class.forName(args[2]).newInstance();
                System.out.println("Setting up initial properties using: " + args[1]);
                um.initializeProperties(args[1]);
            } catch (ClassNotFoundException e) {
                System.out.println("Couldn't create: " + args[2]);
                e.printStackTrace();
            } catch (InstantiationException e) {
                System.out.println("Couldn't create: " + args[2]);
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                System.out.println("Couldn't create: " + args[2]);
                e.printStackTrace();
            }
        }
    }

    /*
     * Externally callable suite() method for use by JUnit Allows tests to be
     * run directly under JUnit, rather than using the startup code in the rest
     * of the module. No parameters can be passed in, so it is less flexible.
     */
    public static TestSuite suite() {
        String args[] = { "../lib/ext", "./jmetertest.properties", "org.apache.jmeter.util.JMeterUtils" };

        initializeManager(args);
        return suite(args[0]);
    }

    /**
     * A unit test suite for JUnit.
     * 
     * @return The test suite
     */
    private static TestSuite suite(String searchPaths) {
        TestSuite suite = new TestSuite("All Tests");
        System.out.println("Scanning "+searchPaths+ " for test cases");
        int tests=0;
        int suites=0;
        try {
            log.info("ClassFinder(TestCase)");
            List<String> classList = ClassFinder.findClassesThatExtend(JOrphanUtils.split(searchPaths, ","),
                    new Class[] { TestCase.class }, true);
            int sz=classList.size();
            log.info("ClassFinder(TestCase) found: "+sz+ " TestCase classes");
            System.out.println("ClassFinder found: "+sz+ " TestCase classes");
            Iterator<String> classes = classList.iterator();
            while (classes.hasNext()) {
                String name = classes.next();
                try {
                    /*
                     * TestSuite only finds testXXX() methods, and does not look
                     * for suite() methods.
                     * 
                     * To provide more compatibilty with stand-alone tests,
                     * where JUnit does look for a suite() method, check for it
                     * first here.
                     * 
                     */

                    Class<?> clazz = Class.forName(name);
                    Test t = null;
                    try {
                        Method m = clazz.getMethod("suite", new Class[0]);
                        t = (Test) m.invoke(clazz, (Object[])null);
                        suites++;
                    } catch (NoSuchMethodException e) {
                    } // this is not an error, the others are
                    // catch (SecurityException e) {}
                    // catch (IllegalAccessException e) {}
                    // catch (IllegalArgumentException e) {}
                    // catch (InvocationTargetException e) {}

                    if (t == null) {
                        t = new TestSuite(clazz);
                    }

                    tests++;
                    suite.addTest(t);
                } catch (Exception ex) {
                    System.out.println("Error adding test for class " + name + " " + ex.toString());
                    log.error("error adding test :", ex);
                }
            }
        } catch (IOException e) {
            log.error("", e);
        }
        System.out.println("Created: "+tests+" tests including "+suites+" suites");
        return suite;
    }
}
