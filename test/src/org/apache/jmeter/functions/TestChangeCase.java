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
 * Test{@link ChangeCase} ChangeCase
 *  
 * @see ChangeCase 
 * @since 4.0
 */
public class TestChangeCase extends JMeterTestCase {
	
	protected AbstractFunction changeCase;
	private SampleResult result;

    private Collection<CompoundVariable> params;

    private JMeterVariables vars;

    private JMeterContext jmctx;

    @Before
    public void setUp() {
    	changeCase = new ChangeCase();
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
    public void testParameterCountIsPropDefined() throws Exception {
        checkInvalidParameterCounts(changeCase, 1, 3);
    }
    
    @Test
    public void testChangeCase() throws Exception {
    	params.add(new CompoundVariable("myUpperTest"));
    	changeCase.setParameters(params);
    	String returnValue = changeCase.execute(result, null);
    	assertEquals("MYUPPERTEST", returnValue);
    }

    @Test
    public void testChangeCaseLower() throws Exception {
    	params.add(new CompoundVariable("myUpperTest"));
    	params.add(new CompoundVariable("LOWER"));
    	changeCase.setParameters(params);
    	String returnValue = changeCase.execute(result, null);
    	assertEquals("myuppertest", returnValue);
    }
    
    @Test
    public void testChangeCaseWrongMode() throws Exception {
    	params.add(new CompoundVariable("myUpperTest"));
    	params.add(new CompoundVariable("Wrong"));
    	changeCase.setParameters(params);
    	String returnValue = changeCase.execute(result, null);
    	assertEquals("myUpperTest", returnValue);
    }
    
    @Test
    public void testChangeCaseCamelCase() throws Exception {
    	params.add(new CompoundVariable("ab-CD eF"));
    	params.add(new CompoundVariable("UPPER_CAMEL_CASE"));
    	changeCase.setParameters(params);
    	String returnValue = changeCase.execute(result, null);
    	assertEquals("AbCdEf", returnValue);
    }
    
    @Test
    public void testChangeCaseCapitalize() throws Exception {
    	params.add(new CompoundVariable("ab-CD eF"));
    	params.add(new CompoundVariable("CAPITALIZE"));
    	changeCase.setParameters(params);
    	String returnValue = changeCase.execute(result, null);
    	assertEquals("Ab-CD eF", returnValue);
    }
    
    @Test
    public void testChangeCaseCamelCaseFirstLower() throws Exception {
    	params.add(new CompoundVariable("ab-CD eF"));
        params.add(new CompoundVariable("LOWER_CAMEL_CASE"));
        changeCase.setParameters(params);
        String returnValue = changeCase.execute(result, null);
        assertEquals("abCdEf", returnValue);
    }
    
    @Test
    public void testChangeCaseCamelCaseFirstLowerWithFirstUpperCaseChar() throws Exception {
        params.add(new CompoundVariable("Ab-CD eF"));
        params.add(new CompoundVariable("lower_CAMEL_CASE"));
        changeCase.setParameters(params);
        String returnValue = changeCase.execute(result, null);
        assertEquals("abCdEf", returnValue);
        
        params.clear();
        params.add(new CompoundVariable(" zadad"));
        params.add(new CompoundVariable("lower_CAMEL_CASE"));
        changeCase.setParameters(params);
        returnValue = changeCase.execute(result, null);
        assertEquals("Zadad", returnValue);
    }
    
    @Test(expected=InvalidVariableException.class)
	public void testChangeCaseError() throws Exception {
		changeCase.setParameters(params);
		changeCase.execute(result, null);
	}    
    
    @Test
    public void testEmptyMode() throws Exception {
        params.add(new CompoundVariable("ab-CD eF"));
        params.add(new CompoundVariable(""));
        changeCase.setParameters(params);
        String returnValue = changeCase.execute(result, null);
        assertEquals("AB-CD EF", returnValue);
    }

    @Test
    public void testChangeCaseWrongModeIgnore() throws Exception {
    	params.add(new CompoundVariable("ab-CD eF"));
        params.add(new CompoundVariable("Wrong"));
        changeCase.setParameters(params);
        String returnValue = changeCase.execute(result, null);
        assertEquals("ab-CD eF", returnValue);
    }

}
