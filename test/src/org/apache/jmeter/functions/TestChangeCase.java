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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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

    private AbstractFunction changeCase;
    private SampleResult result;

    @Before
    public void setUp() {
        changeCase = new ChangeCase();
        JMeterContext jmctx = JMeterContextService.getContext();
        String data = "dummy data";
        result = new SampleResult();
        result.setResponseData(data, null);
        JMeterVariables vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
    }

    @Test
    public void testParameterCountIsPropDefined() throws Exception {
        checkInvalidParameterCounts(changeCase, 1, 3);
    }

    private String execute(String... params) throws InvalidVariableException {
        List<CompoundVariable> testParams =
                Arrays.stream(params)
                        .map(CompoundVariable::new)
                        .collect(Collectors.toList());
        changeCase.setParameters(testParams);
        return changeCase.execute(result, null);
    }

    @Test
    public void testChangeCase() throws Exception {
        String returnValue = execute("myUpperTest");
        assertEquals("MYUPPERTEST", returnValue);
    }

    @Test
    public void testChangeCaseLower() throws Exception {
        String returnValue = execute("myUpperTest", "LOWER");
        assertEquals("myuppertest", returnValue);
    }

    @Test
    public void testChangeCaseWrongMode() throws Exception {
        String returnValue = execute("myUpperTest", "Wrong");
        assertEquals("myUpperTest", returnValue);
    }

    @Test
    public void testChangeCaseCapitalize() throws Exception {
        String returnValue = execute("ab-CD eF", "CAPITALIZE");
        assertEquals("Ab-CD eF", returnValue);
    }

    @Test(expected = InvalidVariableException.class)
    public void testChangeCaseError() throws Exception {
        changeCase.setParameters(new LinkedList<>());
        changeCase.execute(result, null);
    }

    @Test
    public void testEmptyMode() throws Exception {
        String returnValue = execute("ab-CD eF", "");
        assertEquals("AB-CD EF", returnValue);
    }

    @Test
    public void testChangeCaseWrongModeIgnore() throws Exception {
        String returnValue = execute("ab-CD eF", "Wrong");
        assertEquals("ab-CD eF", returnValue);
    }

}
