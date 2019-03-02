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

package org.apache.jmeter.resources;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.Test;

public class TestPropertiesFiles extends JMeterTestCase {


    @Test
    public void testUserProperties() throws Exception {
        Properties props = loadProps(new File(JMeterUtils.getJMeterBinDir(), "user.properties"));
        assertTrue("user.properties should not contain any enabled properties", props.isEmpty());
    }

    // The keys in jmeter.properties and reportgenerator.properties should be distinct
    @Test
    public void testDefaultProperties() throws Exception {
        Properties jmeter = loadProps(new File(JMeterUtils.getJMeterBinDir(), "jmeter.properties"));
        Properties report = loadProps(new File(JMeterUtils.getJMeterBinDir(), "reportgenerator.properties"));
        Enumeration<?> jmeterNames = jmeter.propertyNames();
        while (jmeterNames.hasMoreElements()) {
            final Object key = jmeterNames.nextElement();
            assertFalse("reportgenerator should not contain the jmeter key " + key, report.containsKey(key));
        }
        Enumeration<?> reportNames = report.propertyNames();
        while (reportNames.hasMoreElements()) {
            final Object key = reportNames.nextElement();
            assertFalse("jmeter should not contain the reportgenerator key " + key, jmeter.containsKey(key));
        }
    }

    private static Properties loadProps(File file) throws Exception {
        Properties props = new Properties();
        try (FileInputStream inStream = new FileInputStream(file)) {
            props.load(inStream);
        }
        return props;
    }
}
