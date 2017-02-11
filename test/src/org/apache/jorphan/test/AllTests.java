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

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import javax.crypto.Cipher;

import org.apache.jmeter.junit.categories.ExcludeCategoryFilter;
import org.apache.jmeter.junit.categories.NeedGuiTests;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassFilter;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.junit.internal.RealSystem;
import org.junit.internal.TextListener;
import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

/**
 * Provides a quick and easy way to run all <a href="http://http://junit.org">junit</a> 
 * unit tests in your java project. 
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
     * @param args
     *            the command line arguments
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
        } catch (NoSuchAlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        System.out.println("+++++++++++");
        logprop("java.awt.headless", true);
        logprop("java.awt.graphicsenv", true);
        
        System.out.println("------------");
        try {
            System.out.println("Searching junit tests in : "+args[0]);
            List<String> tests = findJMeterJUnitTests(args[0]);
            Class<?>[] classes = asClasses(tests);
            JUnitCore jUnitCore = new JUnitCore();
            
            // this listener is in the internal junit package
            // if it breaks, replace it with a custom text listener
            RunListener listener = new TextListener(new RealSystem());
            jUnitCore.addListener(listener);
            
            Request request = Request.classes(new Computer(), classes);
            if(GraphicsEnvironment.isHeadless()) {
                request = request.filterWith(new ExcludeCategoryFilter(NeedGuiTests.class));                
            }
            Result result = jUnitCore.run(request);
            
            System.exit(result.wasSuccessful() ? 0 : 1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Class<?>[] asClasses(List<String> tests) throws ClassNotFoundException {
        Class<?>[] classes = new Class<?>[tests.size()];
        for (int i = 0; i < classes.length; i++) {
            String test = tests.get(i);
            classes[i] = Class.forName(test, true, Thread.currentThread().getContextClassLoader());
        }
        
        return classes;
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
    protected static void initializeManager(String[] args) {
        if (args.length >= 3) {
            try {
                System.out.println("Using initializeProperties() from " + args[2]);
                UnitTestManager um = (UnitTestManager) Class.forName(args[2]).newInstance();
                System.out.println("Setting up initial properties using: " + args[1]);
                um.initializeProperties(args[1]);
            } catch (ClassNotFoundException | IllegalAccessException
                    | InstantiationException e) {
                System.out.println("Couldn't create: " + args[2]);
                e.printStackTrace();
            }
        }
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

                if (!c.isAnnotation() 
                        && !c.isEnum() 
                        && !c.isInterface() 
                        && !Modifier.isAbstract(c.getModifiers())) 
                {
                    if (TestCase.class.isAssignableFrom(c)) {
                        isJunitTest =  true;
                    }
                    else {
                        isJunitTest = checkForJUnitAnnotations(c);
                    }
                }
            } catch (UnsupportedClassVersionError | ClassNotFoundException
                    | NoClassDefFoundError e) {
                log.debug("Exception while filtering class {}. {}", className, e.toString());
            }

            return isJunitTest;
        }
        
        private boolean checkForJUnitAnnotations(Class<?> clazz)
        {
            Class<?> classToCheck = clazz;
            while(classToCheck != null) {
                if( checkforTestAnnotationOnMethods(classToCheck)) {
                    return true;
                }
                classToCheck = classToCheck.getSuperclass();
            }
            
            return false;
        }

        private boolean checkforTestAnnotationOnMethods(Class<?> clazz)
        {
            for(Method method : clazz.getDeclaredMethods()) {
                for(Annotation annotation : method.getAnnotations() ) {
                    if (org.junit.Test.class.isAssignableFrom(annotation.annotationType())) {
                        return true;
                    }
                }
            }
            
            return false;
        }
        
    }
}
