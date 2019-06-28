/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License") you may not use this file except in compliance with
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

package org.apache.jmeter.junit.spock

import org.apache.jmeter.util.JMeterUtils
import spock.lang.Specification

import java.nio.charset.Charset

/**
 * Common setup for Spock test cases.
 * <p>
 * Please only use this class if you <em>need</em> the things set up here.
 * <p>
 * Otherwise, extend {@link Specification}
 */
abstract class JMeterSpec extends Specification {

    // Used by findTestFile
    private static final String filePrefix

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
            String file = "jmeter.properties"
            File f = new File(file)
            if (!f.canRead()) {
                System.out.println("Can't find " + file + " - trying bin directory")
                if (!new File("bin/" + file).canRead()) {
                    // When running tests inside IntelliJ
                    System.out.println("Can't find " + file + " - trying ../bin directory")
                    filePrefix = "../bin/" // JMeterUtils assumes Unix-style separators
                    file = filePrefix + file
                } else {
                    filePrefix = "bin/" // JMeterUtils assumes Unix-style separators
                    file = filePrefix + file
                }
            } else {
                filePrefix = ""
            }
            // Used to be done in initializeProperties
            String home = new File(System.getProperty("user.dir"), filePrefix).getParent()
            System.out.println("Setting JMeterHome: " + home)
            JMeterUtils.setJMeterHome(home)
            System.setProperty("jmeter.home", home) // needed for scripts
            JMeterUtils jmu = new JMeterUtils()
            try {
                jmu.initializeProperties(file)
            } catch (MissingResourceException e) {
                System.out.println("** Can't find resources - continuing anyway **")
            }
            System.out.println("JMeterVersion=" + JMeterUtils.getJMeterVersion())
            logprop("java.version")
            logprop("java.vm.name")
            logprop("java.vendor")
            logprop("java.home")
            logprop("file.encoding")
            // Display actual encoding used (will differ if file.encoding is not recognised)
            System.out.println("default encoding=" + Charset.defaultCharset())
            logprop("user.home")
            logprop("user.dir")
            logprop("user.language")
            logprop("user.region")
            logprop("user.country")
            logprop("user.variant")
            System.out.println("Locale=" + Locale.getDefault().toString())
            logprop("java.class.version")
            logprop("java.awt.headless")
            logprop("os.name")
            logprop("os.version")
            logprop("os.arch")
            logprop("java.class.path")
        } else {
            filePrefix = ""
        }
    }

    private static void logprop(String prop) {
        System.out.println(prop + "=" + System.getProperty(prop))
    }

    // Helper method to find a test path
    protected static String findTestPath(String file) {
        File f = new File(file)
        if (filePrefix.length() > 0 && !f.isAbsolute()) {
            return filePrefix + file// Add the offset
        }
        return file
    }

    protected static boolean isHeadless() {
        System.properties['java.awt.headless'] == 'true'
    }

}
