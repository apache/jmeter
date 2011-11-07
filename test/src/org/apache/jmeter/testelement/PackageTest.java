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

/*
 * Created on Jul 16, 2003
 *
 */
package org.apache.jmeter.testelement;

import junit.framework.TestCase;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.LoginConfig;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;

public class PackageTest extends TestCase {
    public PackageTest(String arg0) {
        super(arg0);
    }

    // Test needs to run in this package in order to give access to AbstractTestElement.addProperty() 
    public void DISABLEDtestBug50799() throws Exception {
        HeaderManager headerManager = new HeaderManager();
        headerManager.add(new Header("1stLevelTestHeader", "testValue1"));
        HeaderManager headerManager2 = new HeaderManager();
        headerManager2.add(new Header("2ndLevelTestHeader", "testValue2"));

        DebugSampler debugSampler = new DebugSampler();
        debugSampler.addProperty(new StringProperty("name", "DebugSampler_50799"));
        debugSampler.setRunningVersion(true);
        assertTrue(debugSampler.getProperty("HeaderManager.headers") instanceof NullProperty);
        debugSampler.addTestElement(headerManager);
        assertFalse(debugSampler.getProperty("HeaderManager.headers") instanceof NullProperty);
        assertEquals(debugSampler.getProperty("HeaderManager.headers").getStringValue() ,"[1stLevelTestHeader	testValue1]");

        debugSampler.addTestElement(headerManager2);
        assertEquals(debugSampler.getProperty("HeaderManager.headers").getStringValue() ,"[1stLevelTestHeader	testValue1, 2ndLevelTestHeader	testValue2]");
        assertEquals(2, ((CollectionProperty)debugSampler.getProperty("HeaderManager.headers")).size());
        
        headerManager.recoverRunningVersion();
        headerManager2.recoverRunningVersion();
        debugSampler.recoverRunningVersion();

        assertEquals(1, headerManager.size());
        assertEquals(1, headerManager2.size());
        assertEquals(0, ((CollectionProperty)debugSampler.getProperty("HeaderManager.headers")).size());
        assertEquals(new Header("1stLevelTestHeader", "testValue1"), headerManager.get(0));
        assertEquals(new Header("2ndLevelTestHeader", "testValue2"), headerManager2.get(0));
    }

    public void testRecovery() throws Exception {
        ConfigTestElement config = new ConfigTestElement();
        config.addProperty(new StringProperty("name", "config"));
        config.setRunningVersion(true);
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.setUsername("user1");
        loginConfig.setPassword("pass1");
        assertTrue(config.getProperty("login") instanceof NullProperty);
        // This test should work whether or not all Nulls are equal
        assertEquals(new NullProperty("login"), config.getProperty("login"));
        config.addProperty(new TestElementProperty("login", loginConfig));
        assertEquals(loginConfig.toString(), config.getPropertyAsString("login"));
        config.recoverRunningVersion();
        assertTrue(config.getProperty("login") instanceof NullProperty);
        assertEquals(new NullProperty("login"), config.getProperty("login"));
    }

    public void testArguments() throws Exception {
        Arguments args = new Arguments();
        args.addArgument("arg1", "val1", "=");
        TestElementProperty prop = new TestElementProperty("args", args);
        ConfigTestElement te = new ConfigTestElement();
        te.addProperty(prop);
        te.setRunningVersion(true);
        Arguments config = new Arguments();
        config.addArgument("config1", "configValue", "=");
        TestElementProperty configProp = new TestElementProperty("args", config);
        ConfigTestElement te2 = new ConfigTestElement();
        te2.addProperty(configProp);
        te.addTestElement(te2);
        assertEquals(2, args.getArgumentCount());
        assertEquals("config1=configValue", args.getArgument(1).toString());
        te.recoverRunningVersion();
        te.addTestElement(te2);
        assertEquals(2, args.getArgumentCount());
        assertEquals("config1=configValue", args.getArgument(1).toString());

    }
}
