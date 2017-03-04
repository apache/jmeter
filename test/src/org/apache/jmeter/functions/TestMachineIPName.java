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

package org.apache.jmeter.functions;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.Before;
import org.junit.Test;

public class TestMachineIPName extends JMeterTestCase {
    protected AbstractFunction function;

    private SampleResult result;

    private Collection<CompoundVariable> params;

    private JMeterVariables vars;

    private JMeterContext jmctx;

    @Before
    public void setUp() {
        result = new SampleResult();
        jmctx = JMeterContextService.getContext();
        String data = "The quick brown fox";
        result.setResponseData(data, null);
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        params = new LinkedList<>();
    }

    @Test
    public void testParameterCount() throws Exception {
        function = new MachineName();
        checkInvalidParameterCounts(function, 0, 1);

        function = new MachineIP();
        checkInvalidParameterCounts(function, 0, 1);
    }

    @Test
    public void testMachineName() throws Exception {
        function = new MachineName();
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals(JMeterUtils.getLocalHostName(), ret);
    }
    
    @Test
    public void testMachineNameWithVar() throws Exception {
        function = new MachineName();
        params.add(new CompoundVariable("HOST_NAME"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals(JMeterUtils.getLocalHostName(), ret);
        assertEquals(JMeterUtils.getLocalHostName(), vars.get("HOST_NAME"));
    }

    @Test
    public void testMachineIP() throws Exception {
        function = new MachineIP();
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals(JMeterUtils.getLocalHostIP(), ret);
        
    }
    
    @Test
    public void testMachineIPWithVar() throws Exception {
        function = new MachineIP();
        params.add(new CompoundVariable("HOST_IP"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals(JMeterUtils.getLocalHostIP(), ret);
        assertEquals(JMeterUtils.getLocalHostIP(), vars.get("HOST_IP"));
    }
}
