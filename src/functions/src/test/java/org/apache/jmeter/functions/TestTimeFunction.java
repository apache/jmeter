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

package org.apache.jmeter.functions;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestTimeFunction extends JMeterTestCase {

    private Function variable;
    private SampleResult result;
    private Collection<CompoundVariable> params;
    private JMeterVariables vars;
    private JMeterContext jmctx = null;
    private String value;

    @BeforeEach
    void setUp() {
        jmctx = JMeterContextService.getContext();
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        params = new ArrayList<>();
        result = new SampleResult();
        variable = new TimeFunction();
    }

    @Test
    void testDefault() throws Exception {
        variable.setParameters(params);
        long before = System.currentTimeMillis();
        value = variable.execute(result, null);
        long now = Long.parseLong(value);
        long after = System.currentTimeMillis();
        assertBetween(before, after, now);
    }

    private static void assertBetween(long expectedLow, long expectedHigh, long actual) {
        if (actual < expectedLow || actual > expectedHigh) {
            Assertions.fail(() -> actual + " not within " + expectedLow + " and " + expectedHigh);
        }
    }

    @Test
    void testDefault1() throws Exception {
        params.add(new CompoundVariable());
        variable.setParameters(params);
        long before = System.currentTimeMillis();
        value = variable.execute(result, null);
        long now = Long.parseLong(value);
        long after = System.currentTimeMillis();
        assertBetween(before, after, now);
    }

    @Test
    void testDefault2() throws Exception {
        params.add(new CompoundVariable());
        params.add(new CompoundVariable());
        variable.setParameters(params);
        long before = System.currentTimeMillis();
        value = variable.execute(result, null);
        long now = Long.parseLong(value);
        long after = System.currentTimeMillis();
        assertBetween(before, after, now);
    }

    @Test
    void testDefaultNone() throws Exception {
        long before = System.currentTimeMillis();
        value = variable.execute(result, null);
        long now = Long.parseLong(value);
        long after = System.currentTimeMillis();
        assertBetween(before, after, now);
    }

    @Test
    void testTooMany() throws Exception {
        params.add(new CompoundVariable("YMD"));
        params.add(new CompoundVariable("NAME"));
        params.add(new CompoundVariable("YMD"));
        assertThrows(InvalidVariableException.class, () -> variable.setParameters(params));
    }

    @Test
    void testYMD() throws Exception {
        params.add(new CompoundVariable("YMD"));
        params.add(new CompoundVariable("NAME"));
        variable.setParameters(params);
        value = variable.execute(result, null);
        Assertions.assertEquals(8, value.length());
        Assertions.assertEquals(value, vars.get("NAME"));
    }

    @Test
    void testYMDnoV() throws Exception {
        params.add(new CompoundVariable("YMD"));
        variable.setParameters(params);
        value = variable.execute(result, null);
        Assertions.assertEquals(8, value.length());
        Assertions.assertNull(vars.get("NAME"));
    }

    @Test
    void testHMS() throws Exception {
        params.add(new CompoundVariable("HMS"));
        variable.setParameters(params);
        value = variable.execute(result, null);
        Assertions.assertEquals(6, value.length());
    }

    @Test
    void testYMDHMS() throws Exception {
        params.add(new CompoundVariable("YMDHMS"));
        variable.setParameters(params);
        value = variable.execute(result, null);
        Assertions.assertEquals(15, value.length());
    }

    @Test
    void testUSER1() throws Exception {
        params.add(new CompoundVariable("USER1"));
        variable.setParameters(params);
        value = variable.execute(result, null);
        Assertions.assertEquals(0, value.length());
    }

    @Test
    void testUSER2() throws Exception {
        params.add(new CompoundVariable("USER2"));
        variable.setParameters(params);
        value = variable.execute(result, null);
        Assertions.assertEquals(0, value.length());
    }

    @Test
    void testFixed() throws Exception {
        params.add(new CompoundVariable("'Fixed text'"));
        variable.setParameters(params);
        value = variable.execute(result, null);
        Assertions.assertEquals("Fixed text", value);
    }

    @Test
    void testMixed() throws Exception {
        params.add(new CompoundVariable("G"));
        variable.setParameters(params);
        Locale locale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        value = variable.execute(result, null);
        Locale.setDefault(locale);
        Assertions.assertEquals("AD", value);
    }

    @Test
    void testDivisor() throws Exception {
        params.add(new CompoundVariable("/1000"));
        variable.setParameters(params);
        long before = System.currentTimeMillis() / 1000;
        value = variable.execute(result, null);
        long now = Long.parseLong(value);
        long after = System.currentTimeMillis() / 1000;
        assertBetween(before, after, now);
    }

    @Test
    void testDivisorNoMatch() throws Exception {
        params.add(new CompoundVariable("/1000 ")); // trailing space
        variable.setParameters(params);
        value = variable.execute(result, null);
        Assertions.assertEquals("/1000 ", value);
    }

}
