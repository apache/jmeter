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
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFilter;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

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
        final String showLocale = "Locale="+Locale.getDefault().toString();
        log.info(showLocale);
        System.out.println(showLocale);
        logprop("os.name", true);
        logprop("os.version", true);
        logprop("os.arch");
        logprop("java.class.version");
        // logprop("java.class.path");
        String cp = System.getProperty("java.class.path");
        String cpe[] = JOrphanUtils.split(cp, java.io.File.pathSeparator);
        StringBuilder sb = new StringBuilder(3000);
        sb.append("java.class.path=");
        for (String path : cpe) {
            sb.append("\n");
            sb.append(path);
            if (new File(path).exists()) {
                sb.append(" - OK");
            } else {
                sb.append(" - ??");
            }
        }
        log.info(sb.toString());

        System.out.println("+++++++++++");
        logprop("java.awt.headless", true);
        logprop("java.awt.graphicsenv", true);
        
        System.out.println("------------");
        System.out.println("Creating test suite");
        TestSuite suite = suite(args[0]);
       
        int countTestCases = suite.countTestCases();
        System.out.println("Starting test run, test count = "+countTestCases);
        TestResult result = TestRunner.run(suite);
        
        System.exit(result.wasSuccessful() ? 0 : 1); // this is needed because the test may start the AWT EventQueue thread which is not a daemon.
    }

    /**
     * An overridable method that initializes the logging for the unit test run,
     * using the properties file passed in as the second argument.
     * 
     * @param args arguments to get the logging setup information from
     */
    protected static void initializeLogging(String[] args) {
        if (args.length >= 2) {
            Properties props = new Properties();
            InputStream inputStream = null;
            try {
                System.out.println("Setting up logging props using file: " + args[1]);
                inputStream = new FileInputStream(args[1]);
                props.load(inputStream);
                LoggingManager.initializeLogging(props);
            } catch (FileNotFoundException e) {
                System.out.println(e.getLocalizedMessage());
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
            } finally {
                JOrphanUtils.closeQuietly(inputStream);
            }
        }
    }

    /**
     * An overridable method that that instantiates a UnitTestManager (if one
     * was specified in the command-line arguments), and hands it the name of
     * the properties file to use to configure the system.
     * 
     * @param args arguments with the initialization parameter
     * arg[0] - not used
     * arg[1] - relative name of properties file
     * arg[2] - used as label
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
        String args[] = { "../lib/ext", "./testfiles/jmetertest.properties", "org.apache.jmeter.util.JMeterUtils" };

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
            List<String> classList = findJMeterJUnitTests(searchPaths);
            int sz=classList.size();
            log.info("ClassFinder(TestCase) found: "+sz+ " TestCase classes");
            System.out.println("ClassFinder found: "+sz+ " TestCase classes");
            for (String name : classList) {
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
                    System.out.println("ERROR: (see logfile) could not add test for class " + name + " " + ExceptionUtils.getStackTrace(ex));
                    log.error("error adding test :", ex);
                }
            }
        } catch (IOException e) {
            log.error("", e);
        }
        System.out.println("Created: "+tests+" tests including "+suites+" suites");
        return suite;
    }

    private static List<String> findJMeterJUnitTests(String searchPaths)  throws IOException {
        List<String> classList = ClassFinder.findClasses(JOrphanUtils.split(searchPaths, ","), new JunitTestFilter());
       
        return classList;
    }
    
    /**
     * find the junit tests in the test search path
     */
    private static class JunitTestFilter implements ClassFilter {
        
        private final transient ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        @Override
        public boolean accept(String className) {
            
            boolean isJunitTest = false;
            try {
                Class<?> c = Class.forName(className, false, contextClassLoader);

                if (!c.isInterface() && !Modifier.isAbstract(c.getModifiers())) {
                    if (TestCase.class.isAssignableFrom(c)) {
                        isJunitTest =  true;
                    }
                }
            } catch (UnsupportedClassVersionError ignored) {
                log.debug(ignored.getLocalizedMessage());
            } catch (NoClassDefFoundError ignored) {
                log.debug(ignored.getLocalizedMessage());
            } catch (ClassNotFoundException ignored) {
                log.debug(ignored.getLocalizedMessage());
            }
            
            return isJunitTest;
        }
        
    }
}
