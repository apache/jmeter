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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.gui.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.gui.action.Correlation;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.junit.Before;
import org.junit.Test;

public class TestCorrelation {

    @Before
    public void setUp() {

    }

    @Test
    public void testCreateJmxObjectMap() {

        List<HTTPSamplerBase> request1 = new ArrayList<>();
        HTTPSamplerBase base = new HTTPSamplerProxy();
        base.setPath("/login");
        base.setDomain("localhost");
        base.setPort(8080);
        Arguments arguments = new Arguments();
        arguments.addArgument(new HTTPArgument("_csrf","abc@1234"));
        arguments.addArgument(new HTTPArgument("_csrf","av4556svrsh65"));
        arguments.addArgument(new HTTPArgument("username","ladd"));
        arguments.addArgument(new HTTPArgument("password","pass"));
        base.setArguments(arguments);
        request1.add(base);
        Correlation.createJmxObjectMap(request1);
    }

    @Test
    public void testExtractCorrelationCandidates() {
        Map<String, String> firstJmxMap = new HashMap<>();
        Map<String, String> secondJmxMap = new HashMap<>();

        firstJmxMap.put("_csrf", "abc@123456");
        secondJmxMap.put("_csrf", "av45svrsh65");
        Correlation.extractCorrelationCandidates(firstJmxMap,secondJmxMap);
    }

    @Test
    public void testExtractCorrelationCandidatesElseBlock() {
        Map<String, String> firstJmxMap = new HashMap<>();
        Map<String, String> secondJmxMap = new HashMap<>();

        firstJmxMap.put("_csrf", "abc@123456");
        firstJmxMap.put("_csrf", "abc@123456890");
        secondJmxMap.put("_csrf", "av45svrsh65");
        secondJmxMap.put("_csrf", "av45svrsh232365");
        Correlation.extractCorrelationCandidates(firstJmxMap,secondJmxMap);
    }

}
