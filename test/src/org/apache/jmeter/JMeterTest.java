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
package org.apache.jmeter;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessControlException;
import java.security.Permission;

import org.apache.jmeter.junit.JMeterTestCase;
import org.junit.Before;
import org.junit.Test;

public class JMeterTest extends JMeterTestCase {
    @Before
    public void setUp() throws IOException {
        final SecurityManager securityManager = new SecurityManager() {
            public void checkPermission(Permission permission) {
                if (permission.getName().startsWith("exitVM.1")) {
                    throw new AccessControlException(permission.getName());
                }
            }
        };
        System.setSecurityManager(securityManager);
    }

    @Test
    public void testJmxDoesntExist() {
        JMeter jmeter = new JMeter();
        String command = "-n -t testPlan.jmx";
        String[] args = command.split(" ");
        try {
            jmeter.start(args);
        } catch (AccessControlException ex) {
            assertEquals("The jmx file does not exist, the system should exit with system.exit(1)", ex.getMessage(),
                    "exitVM.1");
        }
    }

    @Test
    public void testJmxExist() throws IOException {
        File temp = File.createTempFile("testPlan", ".jmx");
        String testPlan = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<jmeterTestPlan version=\"1.2\" properties=\"5.0\" jmeter=\"5.2-SNAPSHOT\">\n" + "  <hashTree>\n"
                + "    <TestPlan guiclass=\"TestPlanGui\" testclass=\"TestPlan\" testname=\"Test Plan\" enabled=\"true\">\n"
                + "      <stringProp name=\"TestPlan.comments\"></stringProp>\n"
                + "      <boolProp name=\"TestPlan.functional_mode\">false</boolProp>\n"
                + "      <boolProp name=\"TestPlan.tearDown_on_shutdown\">true</boolProp>\n"
                + "      <boolProp name=\"TestPlan.serialize_threadgroups\">false</boolProp>\n"
                + "      <elementProp name=\"TestPlan.user_defined_variables\" elementType=\"Arguments\" guiclass=\"ArgumentsPanel\" "
                + "testclass=\"Arguments\" testname=\"User Defined Variables\" enabled=\"true\">\n"
                + "        <collectionProp name=\"Arguments.arguments\"/>\n" + "      </elementProp>\n"
                + "      <stringProp name=\"TestPlan.user_define_classpath\"></stringProp>\n" + "    </TestPlan>\n"
                + "    <hashTree/>\n" + "  </hashTree>\n" + "</jmeterTestPlan>\n" + "";
        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write(testPlan);
        out.close();
        JMeter jmeter = new JMeter();
        String command = "-n -t " + temp.getAbsolutePath();
        String[] args = command.split(" ");
        jmeter.start(args);
    }

    @Test
    public void testPluginDoesntExist() throws IOException {
        File temp = File.createTempFile("testPlan", ".jmx");
        String testPlan = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<jmeterTestPlan version=\"1.2\" properties=\"5.0\" jmeter=\"5.2-SNAPSHOT.20190506\">\n"
                + "  <hashTree>\n"
                + "    <TestPlan guiclass=\"TestPlanGui\" testclass=\"TestPlan\" testname=\"Test Plan\" enabled=\"true\">\n"
                + "      <stringProp name=\"TestPlan.comments\"></stringProp>\n"
                + "      <boolProp name=\"TestPlan.functional_mode\">false</boolProp>\n"
                + "      <boolProp name=\"TestPlan.tearDown_on_shutdown\">true</boolProp>\n"
                + "      <boolProp name=\"TestPlan.serialize_threadgroups\">false</boolProp>\n"
                + "      <elementProp name=\"TestPlan.user_defined_variables\" elementType=\"Arguments\" "
                + "guiclass=\"ArgumentsPanel\" testclass=\"Arguments\" testname=\"User Defined Variables\" enabled=\"true\">\n"
                + "        <collectionProp name=\"Arguments.arguments\"/>\n" + "      </elementProp>\n"
                + "      <stringProp name=\"TestPlan.user_define_classpath\"></stringProp>\n" + "    </TestPlan>\n"
                + "    <hashTree>\n" + "      <hashTree>\n"
                + "        <kg.apc.jmeter.samplers.DummySampler guiclass=\"kg.apc.jmeter.samplers.DummySamplerGui\" "
                + "testclass=\"kg.apc.jmeter.samplers.DummySampler\" testname=\"jp@gc - Dummy Sampler\" enabled=\"true\">\n"
                + "          <boolProp name=\"WAITING\">true</boolProp>\n"
                + "          <boolProp name=\"SUCCESFULL\">true</boolProp>\n"
                + "          <stringProp name=\"RESPONSE_CODE\">200</stringProp>\n"
                + "          <stringProp name=\"RESPONSE_MESSAGE\">OK</stringProp>\n"
                + "          <stringProp name=\"REQUEST_DATA\">{&quot;email&quot;:&quot;user1&quot;, &quot;password&quot;:&quot;password1&quot;}；</stringProp>\n"
                + "          <stringProp name=\"RESPONSE_DATA\">{&quot;successful&quot;: true, &quot;account_id&quot;:&quot;0123456789&quot;}</stringProp>\n"
                + "          <stringProp name=\"RESPONSE_TIME\">${__Random(50,500)}</stringProp>\n"
                + "          <stringProp name=\"LATENCY\">${__Random(1,50)}</stringProp>\n"
                + "          <stringProp name=\"CONNECT\">${__Random(1,5)}</stringProp>\n"
                + "        </kg.apc.jmeter.samplers.DummySampler>\n" + "      </hashTree>\n" + "    </hashTree>\n"
                + "  </hashTree>\n" + "</jmeterTestPlan>\n" + "" + "    <hashTree/>\n" + "  </hashTree>\n"
                + "</jmeterTestPlan>\n" + "";
        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write(testPlan);
        out.close();
        JMeter jmeter = new JMeter();
        String command = "-n -t " + temp.getAbsolutePath();
        String[] args = command.split(" ");
        try {
            jmeter.start(args);
        } catch (AccessControlException ex) {
            assertEquals(
                    "The plugin is used for jmx file which doesn't exist, the system should exit with system.exit(1)",
                    ex.getMessage(), "exitVM.1");
        }
    }
}
