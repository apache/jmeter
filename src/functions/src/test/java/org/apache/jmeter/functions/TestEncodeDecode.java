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

import java.util.Collection;
import java.util.LinkedList;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class TestEncodeDecode extends JMeterTestCase  {
    protected AbstractFunction encodeDecode;
    private SampleResult result;
    private Collection<CompoundVariable> params;
    private JMeterVariables vars;
    private JMeterContext jmctx;
    
    @Before
    public void setUp() {
        encodeDecode = new EncodeDecodeFunction();
        result = new SampleResult();
        jmctx = JMeterContextService.getContext();
        String data = "dummy data";
        result.setResponseData(data, null);
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        params = new LinkedList<>();
    }

    @Test
    public void testParameterCount512() throws Exception {
        checkInvalidParameterCounts(encodeDecode, 2, 3);
    }

    @Test
    public void testBase64() throws Exception {
        params.add(new CompoundVariable("BASE64_ENCODE"));
        params.add(new CompoundVariable("I am a string"));
        params.add(new CompoundVariable("salt"));
        encodeDecode.setParameters(params);
        String returnValue = encodeDecode.execute(result, null);
        assertEquals("SSBhbSBhIHN0cmluZw==", returnValue);
    }
    
    @Test
    public void testDecodeBase64() throws Exception {
        params.add(new CompoundVariable("BASE64_DECODE"));
        params.add(new CompoundVariable("SSBhbSBhIHN0cmluZw=="));
        encodeDecode.setParameters(params);
        String returnValue = encodeDecode.execute(result, null);
        assertEquals("I am a string", returnValue);
    }
    
    @Test
    public void testHex() throws Exception {
        params.add(new CompoundVariable("HEX_ENCODE"));
        params.add(new CompoundVariable("I am a string"));        
        encodeDecode.setParameters(params);
        String returnValue = encodeDecode.execute(result, null);
        assertEquals("4920616d206120737472696e67", returnValue);
    }
    
    @Test
    public void testDecodeHex() throws Exception {
        params.add(new CompoundVariable("HEX_DECODE"));
        params.add(new CompoundVariable("4920616d206120737472696e67"));
        encodeDecode.setParameters(params);
        String returnValue = encodeDecode.execute(result, null);
        assertEquals("I am a string", returnValue);
    }
    
    @Test
    public void testInvalid() throws Exception {
        params.add(new CompoundVariable(null));
        params.add(new CompoundVariable("I am a string"));        
        encodeDecode.setParameters(params);
        String returnValue = encodeDecode.execute(result, null);
        assertEquals(null, returnValue);
    }
    
}
