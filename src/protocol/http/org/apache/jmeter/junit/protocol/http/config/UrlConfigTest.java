// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.jmeter.junit.protocol.http.config;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;

/**
 * @author Michael Stover
 * @version $Revision$
 */
public class UrlConfigTest extends JMeterTestCase
{
    HTTPSampler config;
    HTTPSampler defaultConfig;
    HTTPSampler partialConfig;

    public UrlConfigTest(String name)
    {
        super(name);
    }

    protected void setUp()
    {
        Arguments args = new Arguments();
        args.addArgument("username", "mstover");
        args.addArgument("password", "pass");
        args.addArgument("action", "login");
        config = new HTTPSampler();
        config.setName("Full Config");
        config.setProperty(HTTPSampler.DOMAIN, "www.lazer.com");
        config.setProperty(HTTPSampler.PATH, "login.jsp");
        config.setProperty(HTTPSampler.METHOD, HTTPSampler.POST);
        config.setProperty(
            new TestElementProperty(HTTPSampler.ARGUMENTS, args));
        defaultConfig = new HTTPSampler();
        defaultConfig.setName("default");
        defaultConfig.setProperty(HTTPSampler.DOMAIN, "www.xerox.com");
        defaultConfig.setProperty(HTTPSampler.PATH, "default.html");
        partialConfig = new HTTPSampler();
        partialConfig.setProperty(HTTPSampler.PATH, "main.jsp");
        partialConfig.setProperty(HTTPSampler.METHOD, HTTPSampler.GET);
    }

    public void testSimpleConfig()
    {
        assertTrue(config.getName().equals("Full Config"));
        assertEquals(config.getDomain(), "www.lazer.com");
    }

    public void testOverRide()
    {
    	JMeterProperty jmp =partialConfig.getProperty(HTTPSampler.DOMAIN);
        assertTrue(jmp instanceof NullProperty);
        assertTrue(new NullProperty(HTTPSampler.DOMAIN).equals(jmp));
        partialConfig.addTestElement(defaultConfig);
        assertEquals(
            partialConfig.getPropertyAsString(HTTPSampler.DOMAIN),
            "www.xerox.com");
        assertEquals(
            partialConfig.getPropertyAsString(HTTPSampler.PATH),
            "main.jsp");
    }
}