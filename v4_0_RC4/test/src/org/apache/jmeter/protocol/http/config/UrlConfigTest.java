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

package org.apache.jmeter.protocol.http.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPNullSampler;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.junit.Before;
import org.junit.Test;

public class UrlConfigTest extends JMeterTestCase {
    private HTTPSamplerBase config;

    private HTTPSamplerBase defaultConfig;

    private HTTPSamplerBase partialConfig;

    @Before
    public void setUp() {
        Arguments args = new Arguments();
        args.addArgument("username", "mstover");
        args.addArgument("password", "pass");
        args.addArgument("action", "login");
        config = new HTTPNullSampler();
        config.setName("Full Config");
        config.setProperty(HTTPSamplerBase.DOMAIN, "www.lazer.com");
        config.setProperty(HTTPSamplerBase.PATH, "login.jsp");
        config.setProperty(HTTPSamplerBase.METHOD, HTTPConstants.POST);
        config.setProperty(new TestElementProperty(HTTPSamplerBase.ARGUMENTS, args));
        defaultConfig = new HTTPNullSampler();
        defaultConfig.setName("default");
        defaultConfig.setProperty(HTTPSamplerBase.DOMAIN, "www.xerox.com");
        defaultConfig.setProperty(HTTPSamplerBase.PATH, "default.html");
        partialConfig = new HTTPNullSampler();
        partialConfig.setProperty(HTTPSamplerBase.PATH, "main.jsp");
        partialConfig.setProperty(HTTPSamplerBase.METHOD, HTTPConstants.GET);
    }

    @Test
    public void testSimpleConfig() {
        assertEquals("Full Config", config.getName());
        assertEquals("www.lazer.com", config.getDomain());
    }

    @Test
    public void testOverRide() {
        JMeterProperty jmp = partialConfig.getProperty(HTTPSamplerBase.DOMAIN);
        assertTrue(jmp instanceof NullProperty);
        assertEquals(jmp, new NullProperty(HTTPSamplerBase.DOMAIN));
        partialConfig.addTestElement(defaultConfig);
        assertEquals(partialConfig.getPropertyAsString(HTTPSamplerBase.DOMAIN), "www.xerox.com");
        assertEquals(partialConfig.getPropertyAsString(HTTPSamplerBase.PATH), "main.jsp");
    }
}
