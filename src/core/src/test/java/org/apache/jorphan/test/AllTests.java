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
 */

package org.apache.jorphan.test;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.crypto.Cipher;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.jmeter.junit.categories.ExcludeCategoryFilter;
import org.apache.jmeter.junit.categories.NeedGuiTests;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassFilter;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.junit.experimental.ParallelComputer;
import org.junit.internal.TextListener;
import org.junit.runner.Computer;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;
import spock.lang.Specification;

/**
 * Provides a quick and easy way to run all <a href="http://http://junit.org">junit</a>
 * unit tests (including Spock tests) in your Java project.
 * It will find all unit test classes and run all their test methods.
 * There is no need to configure it in any way to find these classes except to
 * give it a path to search.
 * <p>
 * Here is an example Ant target (See Ant at <a
 * href="http://ant.apache.org">Apache Ant</a>) that runs all your unit
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
    private static final Logger log = LoggerFactory.getLogger(AllTests.class);

    /**
     * Private constructor to prevent instantiation.
     */
    private AllTests() {
    }

    private static void logprop(String prop, boolean show) {
        String value = System.getProperty(prop);
        log.info("{}={}", prop, value);
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
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("You must specify a comma-delimited list of paths to search " + "for unit tests");
            return;
        }

        String home = new File(System.getProperty("user.dir")).getParent();
        System.out.println("Setting JMeterHome: "+home);
        JMeterUtils.setJMeterHome(home);
        initializeManager(args);

        log.info("JMeterVersion={}", JMeterUtils.getJMeterVersion());
        System.out.println("JMeterVersion=" + JMeterUtils.getJMeterVersion());
        logprop("java.version", true);
        logprop("java.vm.name");
        logprop("java.vendor");
        logprop("java.home", true);
        logprop("file.encoding", true);
        // Display actual encoding used (will differ if file.encoding is not recognised)
        System.out.println("default encoding="+Charset.defaultCharset());
        log.info("default encoding={}", Charset.defaultCharset());
        logprop("user.home");
        logprop("user.dir", true);
        logprop("user.language");
        logprop("user.region");
        logprop("user.country");
        logprop("user.variant");
        log.info("Locale={}", Locale.getDefault());
        System.out.println("Locale=" + Locale.getDefault());
        logprop("os.name", true);
        logprop("os.version", true);
        logprop("os.arch");
        logprop("java.class.version");

        String cp = System.getProperty("java.class.path");
        String[] cpe = JOrphanUtils.split(cp, java.io.File.pathSeparator);
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

        try {
            int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
            System.out.println("JCE max key length = " + maxKeyLen);
        } catch (NoSuchAlgorithmException e) {
            log.warn(e.getLocalizedMessage());
        }
        System.out.println("+++++++++++");
        logprop("java.awt.headless", true);
        logprop("java.awt.graphicsenv", true);

        System.out.println("------------");
        JUnitCore jUnitCore = new JUnitCore();
        // this listener is in the internal junit package
        // if it breaks, replace it with a custom text listener
        jUnitCore.addListener(new TextListener(System.out));
        // this will time each unit test and then print to file
        // TODO: put behind a flag
        jUnitCore.addListener(new TimePrinter());

        System.out.println("Searching junit tests in : "+args[0]);

        try {
            List<String> tests = findJMeterJUnitTests(args[0]);
            List<Class<?>> classes = asClasses(tests);

            Result parallelResults = jUnitCore.run(getParallelTests(classes));
            Result serialResults = jUnitCore.run(getSerialTests(classes));

            boolean allTestsSuccessful =
                    parallelResults.wasSuccessful() && serialResults.wasSuccessful();
            System.exit(allTestsSuccessful ? 0 : 1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Request getParallelTests(List<Class<?>> classes) {
        Request parallelRequest = Request.classes(
                ParallelComputer.methods(), // ParallelComputer.classes() causes failures
                classes.stream()
                        .filter(c -> !JMeterSerialTest.class.isAssignableFrom(c))
                        .toArray(Class<?>[]::new));
        return filterGUITests(parallelRequest);
    }

    private static Request getSerialTests(List<Class<?>> classes) {
        Request serialRequest = Request.classes(Computer.serial(),
                classes.stream()
                        .filter(JMeterSerialTest.class::isAssignableFrom)
                        .toArray(Class<?>[]::new));
        return filterGUITests(serialRequest);
    }

    private static Request filterGUITests(Request request) {
        if (GraphicsEnvironment.isHeadless()) {
            return request.filterWith(new ExcludeCategoryFilter(NeedGuiTests.class));
        } else {
            return request;
        }
    }

    private static List<Class<?>> asClasses(List<String> tests) {
        return tests.stream()
                .map(AllTests::asClass)
                .collect(Collectors.toList());
    }

    private static Class<?> asClass(String test) {
        try {
            return Class.forName(test, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Used to time each unit test and then write the results to file
     */
    private static class TimePrinter extends RunListener {
        private ConcurrentHashMap<Description, StopWatch> testTimers = new ConcurrentHashMap<>();
        private List<String> logLines = new ArrayList<>();

        @Override
        public void testStarted(Description description) {
            StopWatch sw = new StopWatch();
            sw.start();
            testTimers.put(description, sw);
        }

        @Override
        public void testFinished(Description desc) {
            StopWatch sw = testTimers.get(desc);
            sw.stop();
            logLines.add(desc.getClassName() + "." + desc.getMethodName() + "\t" + sw.getNanoTime());
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
            Files.write(Paths.get("unit-test-perf.log"), logLines);
        }
    }

    /**
     * An overridable method that instantiates a UnitTestManager (if one
     * was specified in the command-line arguments), and hands it the name of
     * the properties file to use to configure the system.
     *
     * @param args arguments with the initialization parameter
     * arg[0] - not used
     * arg[1] - relative name of properties file
     * arg[2] - used as label
     */
    private static void initializeManager(String[] args) {
        if (args.length >= 3) {
            try {
                System.out.println("Using initializeProperties() from " + args[2]);
                UnitTestManager um = (UnitTestManager) Class.forName(args[2]).getDeclaredConstructor().newInstance();
                System.out.println("Setting up initial properties using: " + args[1]);
                um.initializeProperties(args[1]);
            } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
                System.out.println("Couldn't create: " + args[2]);
                e.printStackTrace();
            }
        }
    }

    private static List<String> findJMeterJUnitTests(String searchPathString) throws IOException {
        final String[] searchPaths = JOrphanUtils.split(searchPathString, ",");
        return ClassFinder.findClasses(searchPaths, new JunitTestFilter());
    }

    /**
     * Match JUnit (including Spock) tests
     */
    private static class JunitTestFilter implements ClassFilter {

        private final transient ClassLoader contextClassLoader =
                Thread.currentThread().getContextClassLoader();

        @Override
        public boolean accept(String className) {
            boolean isJunitTest = false;
            try {
                Class<?> clazz = Class.forName(className, false, contextClassLoader);

                if (!clazz.isAnnotation()
                        && !clazz.isEnum()
                        && !clazz.isInterface()
                        && !Modifier.isAbstract(clazz.getModifiers())) {
                    isJunitTest = TestCase.class.isAssignableFrom(clazz)
                            || Specification.class.isAssignableFrom(clazz) // Spock
                            || checkForJUnitAnnotations(clazz);
                }
            } catch (UnsupportedClassVersionError
                    | ClassNotFoundException
                    | NoClassDefFoundError e) {
                log.debug("Exception while filtering class {}. {}", className, e.toString());
            }

            return isJunitTest;
        }

        private boolean checkForJUnitAnnotations(Class<?> clazz) {
            Class<?> classToCheck = clazz;
            while (classToCheck != null) {
                if (checkForTestAnnotationOnMethods(classToCheck)) {
                    return true;
                }
                classToCheck = classToCheck.getSuperclass();
            }

            return false;
        }

        private boolean checkForTestAnnotationOnMethods(Class<?> clazz) {
            return Arrays.stream(clazz.getDeclaredMethods())
                    .flatMap(method -> Arrays.stream(method.getAnnotations()))
                    .map(Annotation::annotationType)
                    .anyMatch(org.junit.Test.class::isAssignableFrom);
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "JunitTestFilter []";
        }
    }
}
