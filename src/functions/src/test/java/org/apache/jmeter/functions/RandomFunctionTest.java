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

import static org.apache.jmeter.functions.FunctionTestHelper.makeParams;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RandomFunctionTest extends JMeterTestCase {

    @Before
    public void setup() {
        JMeterContextService.getContext().setVariables(new JMeterVariables());
    }
    @After
    public void tearDown() {
        JMeterContextService.getContext().clear();
    }

    @Test
    public void randomTest1() throws Exception {
        Random r = new Random();
        Collection<CompoundVariable> parms = makeParams("0","10000000000","VAR");
        r.setParameters(parms);
        String s = r.execute(null,null);
        long l = Long.parseLong(s);
        assertTrue(l>=0 && l<=10000000000L);

        parms = makeParams("1","1","VAR");
        r.setParameters(parms);
        s = r.execute(null,null);
        l = Long.parseLong(s);
        assertEquals(1, l);
        String varValue = JMeterContextService.getContext().getVariables().get("VAR");
        assertEquals("1", varValue);
    }

    @Test
    public void randomStringTest1() throws Exception {
        RandomString r = new RandomString();
        Collection<CompoundVariable> parms = makeParams("10","abcdefghijklmnopqrstuvwxyz","VAR");
        r.setParameters(parms);
        String s = r.execute(null,null);
        Assert.assertNotNull(s);
        assertEquals(10, s.length());
        assertTrue("Random String contains unexpected character", stringOnlyContainsChars(s, "abcdefghijklmnopqrstuvwxyz"));

        String varValue = JMeterContextService.getContext().getVariables().get("VAR");
        assertEquals(s, varValue);

        parms = makeParams("5","", "VAR2");
        r.setParameters(parms);
        s = r.execute(null,null);
        Assert.assertNotNull(s);
        assertEquals(5, s.length());

        varValue = JMeterContextService.getContext().getVariables().get("VAR2");
        assertEquals(s, varValue);
    }

    private boolean stringOnlyContainsChars(String value, String allowedChars) {
        Set<Character> allowedCharsAsSet = allowedChars.chars()
                .mapToObj(i -> (char) i)
                .collect(Collectors.toCollection(HashSet::new));
        return value.chars().allMatch(c -> allowedCharsAsSet.contains((char)c));
    }
}
