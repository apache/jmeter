// $Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.jmeter.testelement.property;

import junit.framework.TestCase;

import org.apache.jmeter.config.LoginConfig;

/**
 *Class for testing the property package.
 */
public class PackageTest extends TestCase
{

    public PackageTest(String name)
    {
        super(name);
    }
    
    public void testStringProperty() throws Exception
    {
        StringProperty prop = new StringProperty("name","value");
        prop.setRunningVersion(true);
        prop.setObjectValue("new Value");
        assertEquals("new Value",prop.getStringValue());
        prop.recoverRunningVersion(null);
        assertEquals("value",prop.getStringValue());
        prop.setObjectValue("new Value");
        prop.setObjectValue("2nd Value");
        assertEquals("2nd Value",prop.getStringValue());
        prop.recoverRunningVersion(null);
        assertEquals("value",prop.getStringValue());        
    }
    
    public void testElementProperty() throws Exception
        {
            LoginConfig config = new LoginConfig();
            config.setUsername("username");
            config.setPassword("password");
            TestElementProperty prop = new TestElementProperty("name",config);
            prop.setRunningVersion(true);
            config = new LoginConfig();
            config.setUsername("user2");
            config.setPassword("pass2");
            prop.setObjectValue(config);
            assertEquals("user2=pass2",prop.getStringValue());
            prop.recoverRunningVersion(null);
            assertEquals("username=password",prop.getStringValue());
            config = new LoginConfig();
            config.setUsername("user2");
            config.setPassword("pass2");
            prop.setObjectValue(config);
            config = new LoginConfig();
            config.setUsername("user3");
            config.setPassword("pass3");
            prop.setObjectValue(config);
            assertEquals("user3=pass3",prop.getStringValue());
            prop.recoverRunningVersion(null);
            assertEquals("username=password",prop.getStringValue());        
        }
}
