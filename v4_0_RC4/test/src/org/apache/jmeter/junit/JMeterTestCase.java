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

package org.apache.jmeter.junit;

import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Common setup for JUnit4 test cases
 */
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
            logprop("java.class.path");
        } else {
            filePrefix = "";
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
        Collection<CompoundVariable> parms = new LinkedList<>();
        for (int c = 0; c < minimum; c++) {
            try {
                func.setParameters(parms);
                fail("Should have generated InvalidVariableException for " + parms.size()
                        + " parameters");
            } catch (InvalidVariableException ignored) {
            }
            parms.add(new CompoundVariable());
        }
        func.setParameters(parms);
    }
    
    protected void checkInvalidParameterCounts(AbstractFunction func, int min,
            int max) throws Exception {
        Collection<CompoundVariable> parms = new LinkedList<>();
        for (int count = 0; count < min; count++) {
            try {
                func.setParameters(parms);
                fail("Should have generated InvalidVariableException for " + parms.size()
                        + " parameters");
            } catch (InvalidVariableException ignored) {
            }
            parms.add(new CompoundVariable());
        }
        for (int count = min; count <= max; count++) {
            func.setParameters(parms);
            parms.add(new CompoundVariable());
        }
        parms.add(new CompoundVariable());
        try {
            func.setParameters(parms);
            fail("Should have generated InvalidVariableException for " + parms.size()
                    + " parameters");
        } catch (InvalidVariableException ignored) {
        }
    }

    public static void assertPrimitiveEquals(boolean expected, boolean actual) {
        org.junit.Assert.assertEquals(Boolean.valueOf(expected), Boolean.valueOf(actual));
    }
}
