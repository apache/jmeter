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

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.MissingResourceException;

import junit.framework.TestCase;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/*
 * Extend JUnit TestCase to provide common setup
 */
public abstract class JMeterTestCase extends TestCase {
    // Used by findTestFile
    private static final String filePrefix;

    public JMeterTestCase() {
        super();
    }

    public JMeterTestCase(String name) {
        super(name);
    }

    /*
     * If not running under AllTests.java, make sure that the properties (and
     * log file) are set up correctly.
     * 
     * N.B. In order for this to work correctly, the JUnit test must be started
     * in the bin directory, and all the JMeter jars (plus any others needed at
     * run-time) need to be on the classpath.
     * 
     */
    static {
        if (JMeterUtils.getJMeterProperties() == null) {
            String file = "testfiles/jmetertest.properties";
            File f = new File(file);
            if (!f.canRead()) {
                System.out.println("Can't find " + file + " - trying bin directory");
                file = "bin/" + file;// JMeterUtils assumes Unix-style separators
                filePrefix = "bin/";
            } else {
                filePrefix = "";
            }
            // Used to be done in initializeProperties
            String home=new File(System.getProperty("user.dir"),filePrefix).getParent();
            System.out.println("Setting JMeterHome: "+home);
            JMeterUtils.setJMeterHome(home);
            JMeterUtils jmu = new JMeterUtils();
            try {
                jmu.initializeProperties(file);
            } catch (MissingResourceException e) {
                System.out.println("** Can't find resources - continuing anyway **");
            }
            logprop("java.version");
            logprop("java.vm.name");
            logprop("java.vendor");
            logprop("java.home");
            logprop("file.encoding");
            logprop("user.home");
            logprop("user.dir");
            logprop("user.language");
            logprop("user.region");
            logprop("user.country");
            logprop("user.variant");
            System.out.println("Locale="+Locale.getDefault().toString());
            logprop("java.class.version");
            logprop("os.name");
            logprop("os.version");
            logprop("os.arch");
            logprop("java.class.path");
            // String cp = System.getProperty("java.class.path");
            // String cpe[]= JOrphanUtils.split(cp,File.pathSeparator);
            // System.out.println("java.class.path=");
            // for (int i=0;i<cpe.length;i++){
            // System.out.println(cpe[i]);
            // }
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

    protected static final Logger testLog = LoggingManager.getLoggerForClass();

    protected void checkInvalidParameterCounts(AbstractFunction func, int minimum)
            throws Exception {
        Collection<CompoundVariable> parms = new LinkedList<CompoundVariable>();
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
        Collection<CompoundVariable> parms = new LinkedList<CompoundVariable>();
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
}
