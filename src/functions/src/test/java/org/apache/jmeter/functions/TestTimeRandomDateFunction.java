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

import static org.apache.jmeter.functions.FunctionTestHelper.makeParams;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestTimeRandomDateFunction extends JMeterTestCase {

    private AbstractFunction function;
    private SampleResult result;
    private JMeterVariables vars;
    private JMeterContext jmctx = null;
    private String value;

    @BeforeEach
    void setUp() {
        jmctx = JMeterContextService.getContext();
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        result = new SampleResult();
        jmctx.setPreviousResult(result);
        function = new RandomDate();
    }

    @Test
    void testParameterCount() throws Exception {
        checkInvalidParameterCounts(function, 3, 5);
    }

    @Test
    void testDefault() throws Exception {
        String endDate = "2099-01-01";
        String formatDate = "yyyy-dd-MM";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatDate);
        Collection<CompoundVariable> params = makeParams(formatDate, "", endDate, "", "");
        function.setParameters(params);
        value = function.execute(result, null);
        LocalDate result = LocalDate.parse(value, formatter);
        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        LocalDate max = LocalDate.parse(endDate, formatter);
        Assertions.assertTrue(now.isBefore(result) && result.isBefore(max));
    }

    @Test
    void testDefault2() throws Exception {
        String endDate = "2099-01-01";
        Collection<CompoundVariable> params = makeParams("yyyy-dd-MM", "", endDate, "", "");
        function.setParameters(params);
        value = function.execute(result, null);
        Assertions.assertEquals(10, value.length());
    }

    @Test
    void testFormatDate() throws Exception {
        String endDate = "01 01 2099";
        String formatDate = "dd MM yyyy";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatDate);
        Collection<CompoundVariable> params = makeParams(formatDate, "", endDate, "", "");
        function.setParameters(params);
        value = function.execute(result, null);
        LocalDate result = LocalDate.parse(value, formatter);
        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        LocalDate max = LocalDate.parse(endDate, formatter);
        Assertions.assertTrue(now.isBefore(result) && result.isBefore(max));
    }

    @Test
    void testFormatDate2() throws Exception {
        String endDate = "01012099";
        String formatDate = "ddMMyyyy";
        Collection<CompoundVariable> params = makeParams(formatDate, "", endDate, "", "");
        function.setParameters(params);
        value = function.execute(result, null);
        Assertions.assertEquals(8, value.length());
    }

    @Test
    void testFormatDate3() throws Exception {
        String startDate = "29 Aug 2111";
        String endDate = "30 Aug 2111";
        String formatDate = "dd MMM yyyy";
        String localeAsString = "en_EN";
        Collection<CompoundVariable> params = makeParams(formatDate, startDate, endDate, localeAsString, "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("29 Aug 2111")));
    }

    @Test
    void testFrenchFormatDate() throws Exception {
        String startDate = "29 mars 2111";
        String endDate = "30 mars 2111";
        String formatDate = "dd MMM yyyy";
        String localeAsString = "fr_FR";
        Collection<CompoundVariable> params = makeParams(formatDate, startDate, endDate, localeAsString, "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("29 mars 2111")));
    }

    @Test
    void testEmptyFormatDate() throws Exception {
        String startDate = "2111-03-29";
        String endDate = "2111-03-30";
        String formatDate = "";
        String localeAsString = "en";
        Collection<CompoundVariable> params = makeParams(formatDate, startDate, endDate, localeAsString, "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("2111-03-29")));
    }

    @Test
    void testEndDateBeforeStartDate() throws Exception {
        String startDate = "2111-03-29";
        String endDate = "2011-03-30";
        String formatDate = "";
        String localeAsString = "en";
        Collection<CompoundVariable> params = makeParams(formatDate, startDate, endDate, localeAsString, "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("")));
    }

    @Test
    void testEndDateBeforeStartDateNullVariable() throws Exception {
        String startDate = "2111-03-29";
        String endDate = "2111-03-30";
        String formatDate = "";
        String localeAsString = "en";
        Collection<CompoundVariable> params = makeParams(formatDate, startDate, endDate, localeAsString, null);
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("2111-03-29")));
    }

    @Test
    void testEndDateBeforeStartDateWithVariable() throws Exception {
        String startDate = "2111-03-29";
        String endDate = "2111-03-30";
        String formatDate = "";
        String localeAsString = "en";
        Collection<CompoundVariable> params = makeParams(formatDate, startDate, endDate, localeAsString, "MY_VAR");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("2111-03-29")));
        assertThat(vars.get("MY_VAR"), is(equalTo("2111-03-29")));
    }

    @Test
    void testInvalidFormat() throws Exception {
        String startDate = "2111-03-29";
        String endDate = "2011-03-30";
        String formatDate = "abcd";
        String localeAsString = "en";
        Collection<CompoundVariable> params = makeParams(formatDate, startDate, endDate, localeAsString, "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("")));
    }

    @Test
    void testInvalidStartDateFormat() throws Exception {
        String startDate = "23-2111-03";
        String endDate = "2011-03-30";
        String formatDate = "abcd";
        String localeAsString = "en";
        Collection<CompoundVariable> params = makeParams(formatDate, startDate, endDate, localeAsString, "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("")));
    }

    @Test
    void testInvalidEndDateFormat() throws Exception {
        String startDate = "2011-03-30";
        String endDate = "23-2111-03";
        String formatDate = "abcd";
        String localeAsString = "en";
        Collection<CompoundVariable> params = makeParams(formatDate, startDate, endDate, localeAsString, "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("")));
    }
}
