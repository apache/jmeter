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

package org.apache.jmeter.protocol.http.correlation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestCorrelation {

    private List<HTTPSamplerBase> requests;
    private List<HeaderManager> headers;
    private Map<String, String> jmxParameterMap;
    private HTTPSamplerBase loginRequest;
    private HTTPSamplerBase logoutRequest;

    @BeforeEach
    public void setup() {
        requests = new ArrayList<>();
        headers = new ArrayList<>();
        loginRequest = new HTTPSamplerProxy();
        loginRequest.setDomain("localhost");
        logoutRequest = new HTTPSamplerProxy();
        logoutRequest.setDomain("localhost");
        jmxParameterMap = new HashMap<>();
    }

    @Test
    public void testCreateJmxParameterMapRequestBody() {
        // One request, distinct parameters
        loginRequest.setPath("/login");
        Arguments loginRequestArguments = new Arguments();
        loginRequestArguments.addArgument(new HTTPArgument("_csrf", "dummytoken"));
        loginRequestArguments.addArgument(new HTTPArgument("username", "dummyusername"));
        loginRequestArguments.addArgument(new HTTPArgument("password", "dummypassword"));
        loginRequest.setArguments(loginRequestArguments);
        requests.add(loginRequest);
        // Result Data
        jmxParameterMap.put("_csrf", "dummytoken");
        jmxParameterMap.put("username", "dummyusername");
        jmxParameterMap.put("password", "dummypassword");
        Assertions.assertEquals(jmxParameterMap, Correlation.createJmxParameterMap(requests, headers));

        // Add request with exactly same parameter name and same values
        requests.add(loginRequest);
        // Results data should be unchanged
        Assertions.assertEquals(jmxParameterMap, Correlation.createJmxParameterMap(requests, headers));

        // Add request with same parameter name but different value
        HTTPSamplerBase logoutRequest = new HTTPSamplerProxy();
        logoutRequest.setPath("/logout");
        Arguments logoutRequestArguments = new Arguments();
        logoutRequestArguments.addArgument(new HTTPArgument("_csrf", "dummytoken2"));
        logoutRequest.setArguments(logoutRequestArguments);
        requests.add(logoutRequest);
        // Result Data
        jmxParameterMap.put("_csrf(1)", "dummytoken2");
        Assertions.assertEquals(jmxParameterMap, Correlation.createJmxParameterMap(requests, headers));
    }

    @Test
    public void testCreateJmxParameterMapRequestPath() {
        // One request, distinct parameters
        loginRequest.setPath("/login?username=dummyusername&password=dummypassword&_csrf=dummytoken");
        requests.add(loginRequest);
        // Result Data
        jmxParameterMap.put("_csrf", "dummytoken");
        jmxParameterMap.put("username", "dummyusername");
        jmxParameterMap.put("password", "dummypassword");
        Assertions.assertEquals(jmxParameterMap, Correlation.createJmxParameterMap(requests, headers));

        // Add request with exactly same parameter name and same values
        requests.add(loginRequest);
        // Results data should be unchanged
        Assertions.assertEquals(jmxParameterMap, Correlation.createJmxParameterMap(requests, headers));

        // Add request with same parameter name but different value
        HTTPSamplerBase logoutRequest = new HTTPSamplerProxy();
        logoutRequest.setPath("/logout?_csrf=dummytoken2");
        requests.add(logoutRequest);
        // Result Data
        jmxParameterMap.put("_csrf(1)", "dummytoken2");
        Assertions.assertEquals(jmxParameterMap, Correlation.createJmxParameterMap(requests, headers));
    }

    @Test
    public void testCreateJmxParameterMapRequestPathAndBody() {
        // One request, distinct parameters in both path and body
        loginRequest.setPath("/login?state=dummystate");
        Arguments loginRequestArguments = new Arguments();
        loginRequestArguments.addArgument(new HTTPArgument("_csrf", "tokenvalue1"));
        loginRequestArguments.addArgument(new HTTPArgument("username", "dummyusername"));
        loginRequestArguments.addArgument(new HTTPArgument("password", "dummypassword"));
        loginRequest.setArguments(loginRequestArguments);
        requests.add(loginRequest);
        // Result Data
        jmxParameterMap.put("_csrf", "tokenvalue1");
        jmxParameterMap.put("username", "dummyusername");
        jmxParameterMap.put("password", "dummypassword");
        jmxParameterMap.put("state", "dummystate");
        Assertions.assertEquals(jmxParameterMap, Correlation.createJmxParameterMap(requests, headers));

        // Add request with same parameter name and same values
        requests.add(loginRequest);
        // Results data should be unchanged
        Assertions.assertEquals(jmxParameterMap, Correlation.createJmxParameterMap(requests, headers));

        // Add request with same parameter name but different values
        HTTPSamplerBase logoutRequest = new HTTPSamplerProxy();
        logoutRequest.setPath("/logout?state=dummystate2");
        Arguments logoutRequestArguments = new Arguments();
        logoutRequestArguments.addArgument(new HTTPArgument("_csrf", "tokenvalue2"));
        logoutRequest.setArguments(logoutRequestArguments);
        requests.add(logoutRequest);
        // Results data
        jmxParameterMap.put("_csrf(1)", "tokenvalue2");
        jmxParameterMap.put("state(1)", "dummystate2");
        Assertions.assertEquals(jmxParameterMap, Correlation.createJmxParameterMap(requests, headers));
    }

    @Test
    public void testCreateJmxParameterMapRequestHeader() {
        // Data in request header
        HeaderManager headerManager = new HeaderManager();
        headerManager.add(new Header("Authorization", "Bearer bhdvjhsavsj"));
        headerManager.add(new Header("Content-Type", "application/json"));
        headers.add(headerManager);
        // Results data
        jmxParameterMap.put("Authorization", "bhdvjhsavsj");
        Assertions.assertEquals(jmxParameterMap, Correlation.createJmxParameterMap(requests, headers));
    }

    @Test
    public void testExtractCorrelationCandidates() {
        // Test Data
        Map<String, String> firstJmxParameterMap = new LinkedHashMap<>();
        firstJmxParameterMap.put("username", "john");
        firstJmxParameterMap.put("password", "secret");
        firstJmxParameterMap.put("_csrf", "token1");
        firstJmxParameterMap.put("_csrf(1)", "token2");
        Map<String, String> secondJmxParameterMap = new LinkedHashMap<>();
        secondJmxParameterMap.put("username", "john");
        secondJmxParameterMap.put("password", "secret");
        secondJmxParameterMap.put("_csrf", "token3");
        secondJmxParameterMap.put("_csrf(1)", "token4");
        // Result Data
        Object[][] tableData = { { Boolean.FALSE, "_csrf", "token1", "token3" },
                { Boolean.FALSE, "_csrf(1)", "token2", "token4" } };
        Assertions.assertArrayEquals(tableData,
                Correlation.extractCorrelationCandidates(firstJmxParameterMap, secondJmxParameterMap));
    }

}
