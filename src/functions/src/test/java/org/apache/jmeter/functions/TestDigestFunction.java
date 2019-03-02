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
import org.junit.Before;
import org.junit.Test;
/**
 * Test Digest function
 * @see DigestEncodeFunction
 * @since 4.0
 *
 */
public class TestDigestFunction extends JMeterTestCase {
    protected AbstractFunction digest;

    private SampleResult result;

    private Collection<CompoundVariable> params;

    private JMeterVariables vars;

    private JMeterContext jmctx;

    @Before
    public void setUp() {
        digest = new DigestEncodeFunction();
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
        checkInvalidParameterCounts(digest, 2, 5);
    }   
    
    @Test
    public void testSha512WithSalt() throws Exception {
        params.add(new CompoundVariable("SHA-512"));
        params.add(new CompoundVariable("nofile"));
        params.add(new CompoundVariable("salt"));
        digest.setParameters(params);
        String returnValue = digest.execute(result, null);
        assertEquals(
                "abc8c7a1c814c74d5882e527d21fabfccf480716df9d17bae73e5e767992d8a2a47033459a9ea91aca3186f75bfbe559419109bc44c1e6dfd618101fdc0beb1b",
                returnValue);
    }  
    
    @Test
    public void testSha512WithSaltAndSpace() throws Exception {
        params.add(new CompoundVariable("SHA-512"));
        params.add(new CompoundVariable("nofile"));
        params.add(new CompoundVariable("salt "));
        digest.setParameters(params);
        String returnValue = digest.execute(result, null);
        assertEquals(
                "961451eb5870ded3fa484ad49fd1481ae3c6decdcc560200e70624a1d62ad0d1793edf3c8eccd0786bffab0b3e4421f54c7fd11a9e7461580352346d039b8e16",
                returnValue);
    }  
    
    @Test
    public void testSha512WithSaltAndSpaceInBoth() throws Exception {
        params.add(new CompoundVariable("SHA-512"));
        params.add(new CompoundVariable("nofile "));
        params.add(new CompoundVariable("salt "));
        digest.setParameters(params);
        String returnValue = digest.execute(result, null);
        assertEquals(
                "3968fd028934466fa095f6323c527148e87d7b74601d1db5f474748dd7c643b4f508e46beb29a405ec658a64c0f581461e99eca063414099af0b63dc890b5739",
                returnValue);
    }  
    
    @Test
    public void testSha1() throws Exception {
        params.add(new CompoundVariable("SHA-1"));
        params.add(new CompoundVariable("nofile"));
        digest.setParameters(params);
        String returnValue = digest.execute(result, null);
        assertEquals("4ea2ced10057872be25371cfe638d3b096c58f2f", returnValue);
    }
    
    @Test
    public void testSha1Variable() throws Exception {
        params.add(new CompoundVariable("SHA-1"));
        params.add(new CompoundVariable("nofile"));
        params.add(new CompoundVariable(""));
        params.add(new CompoundVariable("true"));
        params.add(new CompoundVariable("newVar"));
        digest.setParameters(params);
        String returnValue = digest.execute(result, null);
        assertEquals("4EA2CED10057872BE25371CFE638D3B096C58F2F", returnValue);
    }

    @Test
    public void testSha512Variable() throws Exception {
        params.add(new CompoundVariable("SHA-512"));
        params.add(new CompoundVariable("nofile"));
        params.add(new CompoundVariable(""));  
        params.add(new CompoundVariable("true"));
        params.add(new CompoundVariable("newVar"));
        digest.setParameters(params);
        String returnValue = digest.execute(result, null);
        assertEquals(
                "58DA94D45A97B35B31D7F76D2EBAC184BC4BDA512B966CDBE43FDE1CAE1CFAF89617082CA89928FB5DC1C75D60B93ADB5631F518F970CA6DCC196E1AFC678B8C",
                returnValue);
    }
    
    @Test(expected=InvalidVariableException.class)
    public void testSha512Error() throws Exception {
        params.add(new CompoundVariable("nofile"));
        digest.setParameters(params);
        digest.execute(result, null);
    }
    
    @Test(expected=InvalidVariableException.class)
    public void testSha1Error() throws Exception {
        params.add(new CompoundVariable("SHA-1"));
        digest.setParameters(params);
        digest.execute(result, null);
    }
}
