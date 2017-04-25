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
import static org.junit.Assert.assertTrue;

import java.time.Instant;
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

public class TestTimeShiftingFunction extends JMeterTestCase {
        private Function function;

        private SampleResult result;

        private Collection<CompoundVariable> params;

        private JMeterVariables vars;

        private JMeterContext jmctx = null;

        private String value;
        
        @Before
        public void setUp() {
            jmctx = JMeterContextService.getContext();
            vars = new JMeterVariables();
            jmctx.setVariables(vars);
            jmctx.setPreviousResult(result);
            params = new LinkedList<>();
            result = new SampleResult();
            function = new TimeShiftingFunction();
        }

        @Test
        public void testDatePlusOneDay() throws Exception {
            Collection<CompoundVariable> params = makeParams("yyyy-dd-MM","2017-01-01","1d","");
            function.setParameters(params);
            value = function.execute(result, null);
            assertTrue(value.equalsIgnoreCase("2017-02-01"));
        }
        
        @Test
        public void testDatePlusOneDayInVariable() throws Exception {
            Collection<CompoundVariable> params = makeParams("yyyy-dd-MM","2017-01-01","1d","VAR");
            function.setParameters(params);
            function.execute(result, null);
            assertTrue(vars.get("VAR").equalsIgnoreCase("2017-02-01"));
        }
        
        @Test
        public void testDateLessOneDay() throws Exception {
            Collection<CompoundVariable> params = makeParams("yyyy-dd-MM","2017-01-01","-1d","");
            function.setParameters(params);
            value = function.execute(result, null);
            assertTrue(value.equalsIgnoreCase("2016-31-12"));
        }
        
        @Test
        public void testDatePlusOneHour() throws Exception {
            Collection<CompoundVariable> params = makeParams("HH:mm:ss","14:00:00","1H","");
            function.setParameters(params);
            value = function.execute(result, null);
            assertTrue(value.equalsIgnoreCase("15:00:00"));
        }
        
        @Test
        public void testDateLessOneMinute() throws Exception {
            Collection<CompoundVariable> params = makeParams("HH:mm:ss","14:00:00","-1m","");
            function.setParameters(params);
            value = function.execute(result, null);
            assertTrue(value.equalsIgnoreCase("13:59:00"));
        }
        
        @Test
        public void testDefault() throws Exception {
            Collection<CompoundVariable> params = makeParams("","","","");
            function.setParameters(params);
            long before = Instant.now().toEpochMilli();
            value = function.execute(result, null);
            long after = Instant.now().toEpochMilli();
            long now = Long.parseLong(value);
            assertTrue(now >= before && now <= after);
        }
        
        @Test
        public void testDefaultPlusOneDay() throws Exception {
            Collection<CompoundVariable> params = makeParams("","","1d","");
            function.setParameters(params);
            long before = Instant.now().toEpochMilli();
            before = before + 86_400_000;
            value = function.execute(result, null);
            long after = Instant.now().toEpochMilli();
            after = after + 86_400_000;
            long now = Long.parseLong(value);
            assertTrue(now >= before && now <= after);
        }
        
        @Test
        public void testWrongAmountToAdd() throws Exception {
            // Nothin is add with wrong value
            Collection<CompoundVariable> params = makeParams("","","1dsd","");
            function.setParameters(params);
            long before = Instant.now().toEpochMilli();
            value = function.execute(result, null);
            long after = Instant.now().toEpochMilli();
            long now = Long.parseLong(value);
            assertTrue(now >= before && now <= after);
        }
        
        @Test
        public void testWrongFormatDate() throws Exception {
            Collection<CompoundVariable> params = makeParams("hjfdjyra:fd","","1d","");
            function.setParameters(params);
            long before = Instant.now().toEpochMilli();
            before = before + 86_400_000;
            value = function.execute(result, null);
            long after = Instant.now().toEpochMilli();
            after = after + 86_400_000;
            long now = Long.parseLong(value);
            assertTrue(now >= before && now <= after);
        }
        
        @Test
        public void testAllWrongParameter() throws Exception {
            Collection<CompoundVariable> params = makeParams("hjfdjyra:fd","2014-35-2","-ad51r","");
            function.setParameters(params);
            long before = Instant.now().toEpochMilli();
            value = function.execute(result, null);
            long after = Instant.now().toEpochMilli();
            long now = Long.parseLong(value);
            assertTrue(now >= before && now <= after);
        }
        
}
