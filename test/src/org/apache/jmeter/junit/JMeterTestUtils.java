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

import org.apache.jmeter.util.JMeterUtils;

public class JMeterTestUtils {
    // Used by findTestFile
    private static String filePrefix;
    private JMeterTestUtils() {
        super();
    }
    /**
     * Set jmeter home and return file prefix
     * @return file prefix which is path from jmeter home to jmeter.properties
     */
    public static String setupJMeterHome() {
        if (JMeterUtils.getJMeterProperties() == null) {
            String file = "jmeter.properties";
            File f = new File(file);
            if (!f.canRead()) {
                System.out.println("Can't find " + file + " - trying bin/ and ../bin");
                if (!new File("bin/" + file).canRead()) {
                    // When running tests inside IntelliJ
                    filePrefix = "../bin/"; // JMeterUtils assumes Unix-style separators
                } else {
                    filePrefix = "bin/"; // JMeterUtils assumes Unix-style separators
                }
                file = filePrefix + file;
            } else {
                filePrefix = "";
            }
            // Used to be done in initializeProperties
            String home=new File(System.getProperty("user.dir"),filePrefix).getParent();
            System.out.println("Setting JMeterHome: "+home);
            JMeterUtils.setJMeterHome(home);
        }
        return filePrefix;
    }
}
