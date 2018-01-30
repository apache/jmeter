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
import static org.exparity.hamcrest.date.LocalDateMatchers.sameDay;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Random;
import java.util.TimeZone;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Before;
import org.junit.Test;

public class TestTimeShiftFunction extends JMeterTestCase {
    private Function function;

    private SampleResult result;

    private JMeterVariables vars;

    private JMeterContext jmctx = null;

    private String value;

    @Before
    public void setUp() {
        jmctx = JMeterContextService.getContext();
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        result = new SampleResult();
        function = new TimeShift();
    }

    @Test
    public void testDatePlusOneDay() throws Exception {
        Collection<CompoundVariable> params = makeParams("yyyy-dd-MM", "2017-01-01", "P1D", "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("2017-02-01")));
    }

    @Test
    public void testDatePlusOneDayInVariable() throws Exception {
        Collection<CompoundVariable> params = makeParams("yyyy-dd-MM", "2017-01-01", "P1d", "VAR");
        function.setParameters(params);
        function.execute(result, null);
        assertThat(vars.get("VAR"), is(equalTo("2017-02-01")));
    }

    @Test
    public void testDatePlusComplexPeriod() throws Exception {
        Collection<CompoundVariable> params = makeParams("yyyy-dd-MM HH:m", "2017-01-01 12:00", "P+32dT-1H-5m", "VAR");
        function.setParameters(params);
        String value = function.execute(result, null);
        assertThat(value, is(equalTo("2017-02-02 10:55")));
    }

    @Test
    public void testDefault() throws Exception {
        Collection<CompoundVariable> params = makeParams("", "", "", "");
        function.setParameters(params);
        value = function.execute(result, null);
        long resultat = Long.parseLong(value);
        LocalDateTime  nowFromFunction = LocalDateTime.ofInstant(Instant.ofEpochMilli(resultat), TimeZone
                .getDefault().toZoneId());
        assertThat(nowFromFunction, within(5, ChronoUnit.SECONDS, LocalDateTime.now()));
    }

    @Test
    public void testNowPlusOneDay() throws Exception {
        Collection<CompoundVariable> params = makeParams("yyyy-MM-dd", "", "P1d", "");
        function.setParameters(params);
        value = function.execute(result, null);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate tomorrowFromFunction = LocalDate.parse(value);
        assertThat(tomorrowFromFunction, sameDay(tomorrow));
    }
    
    @Test
    public void testNowWithComplexPeriod() throws Exception {
        Collection<CompoundVariable> params = makeParams("yyyy-MM-dd'T'HH:mm:ss", "", "P10DT-1H-5M5S", "");
        function.setParameters(params);
        value = function.execute(result, null);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(10).plusHours(-1).plusMinutes(-5).plusSeconds(5);
        LocalDateTime futureDateFromFunction = LocalDateTime.parse(value);
        assertThat(futureDateFromFunction, within(1, ChronoUnit.SECONDS, futureDate));
    }
    
    @Test
    public void testPotentialBugWithComplexPeriod() throws Exception {
        Collection<CompoundVariable> params = makeParams("yyyy-MM-dd'T'HH:mm:ss", "2017-12-21T12:00:00", "P10DT-1H-5M5S", "");
        function.setParameters(params);
        value = function.execute(result, null);
        LocalDateTime futureDateFromFunction = LocalDateTime.parse(value);
        
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss");
        LocalDateTime baseDate = LocalDateTime.parse("2017-12-21 12:00:00", dateFormat);
        LocalDateTime futureDate = baseDate.plusDays(10).plusHours(-1).plusMinutes(-5).plusSeconds(5);
        assertThat(futureDateFromFunction, within(1, ChronoUnit.SECONDS, futureDate));
    }

    public static void main(String[] args) {
        System.out.println(java.time.Duration.parse("P10DT-1H-5M5S").toMillis());
    }
    @Test
    public void testWrongAmountToAdd() throws Exception {
        // Nothing is add with wrong value, so check if return is now
        Collection<CompoundVariable> params = makeParams("", "", "qefv1Psd", "");
        function.setParameters(params);
        value = function.execute(result, null);
        long resultat = Long.parseLong(value);
        LocalDateTime  nowFromFunction = LocalDateTime.ofInstant(Instant.ofEpochMilli(resultat), TimeZone
                .getDefault().toZoneId());
        assertThat(nowFromFunction, within(5, ChronoUnit.SECONDS, LocalDateTime.now()));
    }

    @Test
    public void testWrongFormatDate() throws Exception {
        Collection<CompoundVariable> params = makeParams("hjfdjyra:fd", "", "P1D", "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("")));
    }
    
    
    @Test
    public void testRandomPeriod() throws Exception {
        Random r = new Random();
        int randomInt = r.ints(1, 60).limit(1).findFirst().getAsInt();
        vars.put("random", String.valueOf( randomInt ) );
        Collection<CompoundVariable> params = makeParams("yyyy-MM-dd'T'HH:mm:ss", "", "PT${random}M", "");
        function.setParameters(params);
        value = function.execute(result, null);
        LocalDateTime randomFutureDate = LocalDateTime.parse(value);
        LocalDateTime checkFutureDate = LocalDateTime.now().plusMinutes(randomInt);
        assertThat(randomFutureDate, within(5, ChronoUnit.SECONDS, checkFutureDate) );
        randomInt = r.ints(1, 60).limit(1).findFirst().getAsInt();
        vars.put("random", String.valueOf( randomInt ) );
        value = function.execute(result, null);
        randomFutureDate = LocalDateTime.parse(value);
        checkFutureDate = LocalDateTime.now().plusMinutes(randomInt);
        assertThat(randomFutureDate, within(5, ChronoUnit.SECONDS, checkFutureDate) );
        
    }
    
    
    @Test
    public void testNowPlusOneDayWithLocale() throws Exception {
        Collection<CompoundVariable> params = makeParams("yyyy-MMMM-dd", "2017-juillet-01", "P1D", "fr_FR", "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("2017-juillet-02")));
        params = makeParams("yyyy-MMMM-dd", "2017-July-01", "P1D", "en_EN", "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("2017-July-02")));
        params = makeParams("yyyy-MMMM-dd", "2017-julio-01", "P1D", "es_ES", "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("2017-julio-02")));
        params = makeParams("yyyy-MMMM-dd", "2017-Juli-01", "P1D", "de_DE", "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("2017-Juli-02")));
    }
   
}
