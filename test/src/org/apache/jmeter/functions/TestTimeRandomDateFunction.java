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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Before;
import org.junit.Test;

public class TestTimeRandomDateFunction extends JMeterTestCase {

    private AbstractFunction function;

    private SampleResult result;

    private JMeterVariables vars;

    private JMeterContext jmctx = null;

    private String value;

    @Before
    public void setUp() {
        jmctx = JMeterContextService.getContext();
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        result = new SampleResult();
        jmctx.setPreviousResult(result);
        function = new RandomDate();
    }

    @Test
    public void testParameterCount() throws Exception {
        checkInvalidParameterCounts(function, 1, 5);
    }

    @Test
    public void testDefault() throws Exception {
        String EndDate = "2099-01-01";
        String FormatDate = "yyyy-dd-MM";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FormatDate);
        Collection<CompoundVariable> params = makeParams(FormatDate, "", EndDate, "", "");
        function.setParameters(params);
        value = function.execute(result, null);
        LocalDate result = LocalDate.parse(value, formatter);
        LocalDate now = LocalDate.now();
        LocalDate max = LocalDate.parse(EndDate, formatter);
        assertTrue(now.isBefore(result) && result.isBefore(max));
    }

    @Test
    public void testDefault2() throws Exception {
        String EndDate = "2099-01-01";
        Collection<CompoundVariable> params = makeParams("yyyy-dd-MM", "", EndDate, "", "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertEquals(10, value.length());
    }

    @Test
    public void testFormatDate() throws Exception {
        String EndDate = "01 01 2099";
        String FormatDate = "dd MM yyyy";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FormatDate);
        Collection<CompoundVariable> params = makeParams(FormatDate, "", EndDate, "", "");
        function.setParameters(params);
        value = function.execute(result, null);
        LocalDate result = LocalDate.parse(value, formatter);
        LocalDate now = LocalDate.now();
        LocalDate max = LocalDate.parse(EndDate, formatter);
        assertTrue(now.isBefore(result) && result.isBefore(max));
    }

    @Test
    public void testFormatDate2() throws Exception {
        String EndDate = "01012099";
        String FormatDate = "ddMMyyyy";
        Collection<CompoundVariable> params = makeParams(FormatDate, "", EndDate, "", "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertEquals(8, value.length());
    }

    @Test
    public void testFormatDate3() throws Exception {
        String StartDate = "29 Aug 2111";
        String EndDate = "30 Aug 2111";
        String FormatDate = "dd MMM yyyy";
        String localeAsString = "en_EN";
        Collection<CompoundVariable> params = makeParams(FormatDate, StartDate, EndDate, localeAsString, "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("29 Aug 2111")));
    }

    @Test
    public void testFrenchFormatDate() throws Exception {
        String StartDate = "29 mars 2111";
        String EndDate = "30 mars 2111";
        String FormatDate = "dd MMM yyyy";
        String localeAsString = "fr_FR";
        Collection<CompoundVariable> params = makeParams(FormatDate, StartDate, EndDate, localeAsString, "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("29 mars 2111")));
    }
}
