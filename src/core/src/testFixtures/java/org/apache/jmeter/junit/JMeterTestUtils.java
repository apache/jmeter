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

import java.io.File;

import org.apache.jmeter.testkit.ResourceLocator;
import org.apache.jmeter.util.JMeterUtils;

public class JMeterTestUtils {
    // Used by findTestFile
    private static volatile String filePrefix;
    private JMeterTestUtils() {
        super();
    }
    /**
     * Set jmeter home and return file prefix
     * @return file prefix which is path from jmeter home to jmeter.properties
     */
    public static String setupJMeterHome() {
        if (filePrefix == null) {
            String prefix = ".";
            for (int i = 0; i < 5 && !new File(prefix, "bin/jmeter.properties").canRead(); i++) {
                prefix = "../" + prefix;
            }
            // Used to be done in initializeProperties
            String home = new File(prefix).getAbsolutePath();
            filePrefix = prefix + "/bin/";
            System.out.println("Setting JMeterHome: "+home);
            JMeterUtils.setJMeterHome(home);
        }
        return filePrefix;
    }

    /**
     * Returns absolute path of a resource file.
     * It allows to have test resources in {@code test/resources/org/apache...} folder
     * and reference it as {@code getResourceFilePath(MyTest.class, "test1.txt")}.
     * @param klass class to resolve the resource
     * @param resource argument for klass.getResource. Relative or absolute file path
     * @return "" when input is "", input resource when resource is not found, or absolute file path of a resource
     */
    public static String getResourceFilePath(Class<?> klass, String resource) {
        return ResourceLocator.getResource(klass, resource);
    }
}
