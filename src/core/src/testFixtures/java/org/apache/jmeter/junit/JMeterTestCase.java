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

package org.apache.jmeter.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.parallel.Isolated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Common setup for JUnit4 test cases.
 * It initializes JMeterProperties, so it is unsafe to execute several such tests concurrently, so it is marked with
 * {@code @Isolated}.
 */
@Isolated
public abstract class JMeterTestCase {
    // Used by findTestFile
    private static final String filePrefix;

    /*
     * If not running under AllTests.java, make sure that the properties (and
     * log file) are set up correctly.
     *
     * N.B. This assumes the JUnit test are executed in the
     * project root, bin directory or one level down, and all the JMeter jars
     * (plus any others needed at run-time) need to be on the classpath.
     */
    static {
        if (JMeterUtils.getJMeterProperties() == null) {
            filePrefix = JMeterTestUtils.setupJMeterHome();
            String home = JMeterUtils.getJMeterHome();
            System.setProperty("jmeter.home", home); // needed for scripts
            JMeterUtils jmu = new JMeterUtils();
            try {
                jmu.initializeProperties(filePrefix+"jmeter.properties");
            } catch (MissingResourceException e) {
                System.out.println("** Can't find resources - continuing anyway **");
            }
            System.out.println("JMeterVersion="+JMeterUtils.getJMeterVersion());
            logprop("java.version");
            logprop("java.vm.name");
            logprop("java.vendor");
            logprop("java.home");
            logprop("file.encoding");
            // Display actual encoding used (will differ if file.encoding is not recognised)
            System.out.println("default encoding="+Charset.defaultCharset());
            logprop("user.home");
            logprop("user.dir");
            logprop("user.language");
            logprop("user.region");
            logprop("user.country");
            logprop("user.variant");
            System.out.println("Locale="+Locale.getDefault().toString());
            logprop("java.class.version");
            logprop("java.awt.headless");
            logprop("os.name");
            logprop("os.version");
            logprop("os.arch");
            if (Boolean.getBoolean("jmeter.test.log.classpath")) {
                logprop("java.class.path");
            }
        } else {
            filePrefix = JMeterTestUtils.setupJMeterHome();
        }
    }

    private static void logprop(String prop) {
        System.out.println(prop + "=" + System.getProperty(prop));
    }

    // Helper method to find a file
    protected static File findTestFile(String file) {
        File f = new File(file);
        if (filePrefix.length() > 0 && !f.isAbsolute()) {
            f = new File(filePrefix, file);// Add the offset
        }
        return f;
    }

    // Helper method to find a test path
    protected static String findTestPath(String file) {
        File f = new File(file);
        if (filePrefix.length() > 0 && !f.isAbsolute()) {
            return filePrefix + file;// Add the offset
        }
        return file;
    }

    protected static final Logger testLog = LoggerFactory.getLogger(JMeterTestCase.class);

    protected void checkInvalidParameterCounts(AbstractFunction func, int minimum)
            throws Exception {
        Collection<CompoundVariable> parms = new ArrayDeque<>();
        for (int c = 0; c < minimum; c++) {
            assertThrows(InvalidVariableException.class, () -> func.setParameters(parms),
                    "parms.size() = " + parms.size() + " is too small");
            parms.add(new CompoundVariable());
        }
        func.setParameters(parms);
    }

    protected void checkInvalidParameterCounts(AbstractFunction func, int min,
            int max) throws Exception {
        Collection<CompoundVariable> parms = new ArrayDeque<>();
        for (int count = 0; count < min; count++) {
            assertThrows(InvalidVariableException.class, () -> func.setParameters(parms),
                    "parms.size() = " + parms.size() + " is too small");
            parms.add(new CompoundVariable());
        }
        for (int count = min; count <= max; count++) {
            func.setParameters(parms);
            parms.add(new CompoundVariable());
        }
        parms.add(new CompoundVariable());
        assertThrows(InvalidVariableException.class, () -> func.setParameters(parms),
                "parms.size() = " + parms.size() + " is too big");
    }

    public static void assertPrimitiveEquals(boolean expected, boolean actual) {
        assertEquals(expected, actual);
    }

    /**
     * Returns absolute path of a resource file.
     * It allows to have test resources in {@code test/resources/org/apache...} folder
     * and reference it as {@code getResourceFilePath("test1.txt")}.
     * @param resource argument for klass.getResource. Relative (to current class) or absolute resource path
     * @return absolute file path of a resource
     */
    protected String getResourceFilePath(String resource) {
        return JMeterTestUtils.getResourceFilePath(getClass(), resource);
    }
}
