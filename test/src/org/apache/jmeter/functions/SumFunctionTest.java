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
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Before;
import org.junit.Test;

public class SumFunctionTest extends JMeterTestCase {

    private JMeterVariables vars = null;

    @Before
    public void setUp() {
        JMeterContext jmctx = JMeterContextService.getContext();
        jmctx.setVariables(new JMeterVariables());
        vars = jmctx.getVariables();
    }

    @Test
    public void sumTest() throws Exception {
        String maxIntVal = Integer.toString(Integer.MAX_VALUE);
        String minIntVal = Integer.toString(Integer.MIN_VALUE);

        IntSum intSum = new IntSum();
        checkInvalidParameterCounts(intSum, 2);
        checkSum(intSum,"3", new String[]{"1","2"});
        checkSumNoVar(intSum,"3", new String[]{"1","2"});
        checkSum(intSum,"1", new String[]{"-1","1","1","1","-2","1"});
        checkSumNoVar(intSum,"1", new String[]{"-1","1","1","1","-2","1"});
        checkSumNoVar(intSum,"-1", new String[]{"-1","1","1","1","-2","-1"});
        checkSum(intSum,maxIntVal, new String[]{maxIntVal,"0"});
        checkSum(intSum,minIntVal, new String[]{maxIntVal,"1"}); // wrap-round check

        LongSum longSum = new LongSum();
        checkInvalidParameterCounts(longSum, 2);
        checkSum(longSum,"3", new String[]{"1","2"});
        checkSum(longSum,"1", new String[]{"-1","1","1","1","-1","0"});
        checkSumNoVar(longSum,"3", new String[]{"1","2"});
        checkSumNoVar(longSum,"1", new String[]{"-1","1","1","1","-1","0"});
        checkSumNoVar(longSum,"0", new String[]{"-1","1","1","1","-1","-1"});
        String maxIntVal_1 = Long.toString(1+(long)Integer.MAX_VALUE);
        checkSum(longSum,maxIntVal, new String[]{maxIntVal,"0"});
        checkSum(longSum,maxIntVal_1, new String[]{maxIntVal,"1"}); // no wrap-round check
        String maxLongVal = Long.toString(Long.MAX_VALUE);
        String minLongVal = Long.toString(Long.MIN_VALUE);
        checkSum(longSum,maxLongVal, new String[]{maxLongVal,"0"});
        checkSum(longSum,minLongVal, new String[]{maxLongVal,"1"}); // wrap-round check
    }

    // Perform a sum and check the results
    private void checkSum(AbstractFunction func, String value, String[] addends)  throws Exception {
        Collection<CompoundVariable> parms = Arrays.stream(addends)
                .map(CompoundVariable::new)
                .collect(Collectors.toCollection(LinkedList::new));
        parms.add(new CompoundVariable("Result"));
        func.setParameters(parms);
        assertEquals(value, func.execute(null,null));
        assertEquals(value, vars.getObject("Result"));
    }

    // Perform a sum and check the results
    private void checkSumNoVar(AbstractFunction func, String value, String[] addends)  throws Exception {
        Collection<CompoundVariable> parms = Arrays.stream(addends)
                .map(CompoundVariable::new)
                .collect(Collectors.toCollection(LinkedList::new));
        func.setParameters(parms);
        assertEquals(value,func.execute(null,null));
    }
}
