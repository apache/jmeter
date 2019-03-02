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

package org.apache.jmeter.protocol.mail.sampler;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.apache.jmeter.util.JMeterUtils;
import org.junit.Test;

public class TestMailReaderSampler {

    public static void createJMeterEnv() {
        File propsFile;
        try {
            propsFile = File.createTempFile("jmeter", ".properties");
            propsFile.deleteOnExit();
            JMeterUtils.loadJMeterProperties(propsFile.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        JMeterUtils.setLocale(new Locale("ignoreResources"));
    }

    @Test
    public void testPassCustomProperties() {
        createJMeterEnv();
        Properties jMeterProperties = JMeterUtils.getJMeterProperties();
        jMeterProperties.put("prop1.name", "prop1.value");
        jMeterProperties.put("mail.prop2.name", "mail.prop2.value");

        MailReaderSampler sampler = new MailReaderSampler();
        Properties properties = new Properties();
        sampler.addCustomProperties(properties);
        assertEquals(1, properties.size());
        assertEquals("mail.prop2.value", properties.getProperty("mail.prop2.name"));
    }
}
