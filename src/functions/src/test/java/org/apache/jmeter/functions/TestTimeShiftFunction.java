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
import static org.exparity.hamcrest.date.LocalDateMatchers.sameDay;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.zone.ZoneRules;
import java.util.Collection;
import java.util.Random;
import java.util.TimeZone;
import java.util.function.BooleanSupplier;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestTimeShiftFunction extends JMeterTestCase {

    private Function function;
    private SampleResult result;
    private JMeterVariables vars;
    private JMeterContext jmctx = null;
    private String value;

    @BeforeEach
    void setUp() {
        jmctx = JMeterContextService.getContext();
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        result = new SampleResult();
        function = new TimeShift();
    }

    @Test
    void testDatePlusOneDay() throws Exception {
        Collection<CompoundVariable> params = makeParams("yyyy-dd-MM", "2017-01-01", "P1D", "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("2017-02-01")));
    }

    @Test
    void testDatePlusOneDayInVariable() throws Exception {
        Collection<CompoundVariable> params = makeParams("yyyy-dd-MM", "2017-01-01", "P1d", "VAR");
        function.setParameters(params);
        function.execute(result, null);
        assertThat(vars.get("VAR"), is(equalTo("2017-02-01")));
    }

    @Test
    void testDatePlusComplexPeriod() throws Exception {
        Collection<CompoundVariable> params = makeParams("yyyy-dd-MM HH:m", "2017-01-01 12:00", "P+32dT-1H-5m", "VAR");
        function.setParameters(params);
        String value = function.execute(result, null);
        assertThat(value, is(equalTo("2017-02-02 10:55")));
    }

    @Test
    void testDefault() throws Exception {
        Collection<CompoundVariable> params = makeParams("", "", "", "");
        function.setParameters(params);
        value = function.execute(result, null);
        long resultat = Long.parseLong(value);
        LocalDateTime nowFromFunction = LocalDateTime.ofInstant(Instant.ofEpochMilli(resultat),
                TimeZone.getDefault().toZoneId());
        assertThat(nowFromFunction, within(5, ChronoUnit.SECONDS, LocalDateTime.now(ZoneId.systemDefault())));
    }

    @Test
    void testNowPlusOneDay() throws Exception {
        Collection<CompoundVariable> params = makeParams("yyyy-MM-dd", "", "P1d", "");
        function.setParameters(params);
        value = function.execute(result, null);
        LocalDate tomorrow = LocalDate.now(ZoneId.systemDefault()).plusDays(1);
        LocalDate tomorrowFromFunction = LocalDate.parse(value);
        assertThat(tomorrowFromFunction, sameDay(tomorrow));
    }

    @Test
    void testNowWithComplexPeriod() throws Exception {
        // Workaround to skip test, when we know it will fail
        // See Bug 65217 and PR 561 for discussions on how to fix the underlying issue
        Assumptions.assumeFalse(dstChangeAhead("P10DT-1H-5M5S"));

        Collection<CompoundVariable> params = makeParams("yyyy-MM-dd'T'HH:mm:ss", "", "P10DT-1H-5M5S", "");
        function.setParameters(params);
        value = function.execute(result, null);
        LocalDateTime futureDate = LocalDateTime.now(ZoneId.systemDefault())
                .plusDays(10).plusHours(-1).plusMinutes(-5).plusSeconds(5);
        LocalDateTime futureDateFromFunction = LocalDateTime.parse(value);
        assertThat(futureDateFromFunction, within(1, ChronoUnit.SECONDS, futureDate));
    }

    private static BooleanSupplier dstChangeAhead(String duration) {
        return () -> {
            ZoneId defaultZoneId = ZoneId.systemDefault();
            Instant now = LocalDateTime.now(defaultZoneId).atZone(defaultZoneId).toInstant();
            Instant then = LocalDateTime.now(defaultZoneId).plus(Duration.parse(duration))
                    .atZone(defaultZoneId).toInstant();
            ZoneRules rules = defaultZoneId.getRules();
            Duration nowDST = rules.getDaylightSavings(now);
            Duration thenDST = rules.getDaylightSavings(then);
            return !nowDST.equals(thenDST);
        };
    }

    @Test
    void testPotentialBugWithComplexPeriod() throws Exception {
        Collection<CompoundVariable> params = makeParams("yyyy-MM-dd'T'HH:mm:ss", "2017-12-21T12:00:00",
                "P10DT-1H-5M5S", "");
        function.setParameters(params);
        value = function.execute(result, null);
        LocalDateTime futureDateFromFunction = LocalDateTime.parse(value);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss");
        LocalDateTime baseDate = LocalDateTime.parse("2017-12-21 12:00:00", dateFormat);
        LocalDateTime futureDate = baseDate.plusDays(10).plusHours(-1).plusMinutes(-5).plusSeconds(5);
        assertThat(futureDateFromFunction, within(1, ChronoUnit.SECONDS, futureDate));
    }

    @Test
    void testShiftWithTimeZone() throws Exception {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        String timeString = "2017-12-21T12:00:00.000+0100";
        Collection<CompoundVariable> params = makeParams(pattern, timeString, "P10DT-1H-5M5S", "");
        function.setParameters(params);
        value = function.execute(result, null);
        ZonedDateTime futureDateFromFunction = ZonedDateTime.parse(value, DateTimeFormatter.ofPattern(pattern));

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime baseDate = ZonedDateTime.parse(timeString, dateFormat).toLocalDateTime();
        LocalDateTime futureDate = baseDate.plusDays(10).plusHours(-1).plusMinutes(-5).plusSeconds(5);
        assertThat(futureDateFromFunction.toLocalDateTime(), within(1, ChronoUnit.SECONDS, futureDate));

    }

    static void main(String[] args) {
        System.out.println(java.time.Duration.parse("P10DT-1H-5M5S").toMillis());
    }

    @Test
    void testWrongAmountToAdd() throws Exception {
        // Nothing is add with wrong value, so check if return is now
        Collection<CompoundVariable> params = makeParams("", "", "qefv1Psd", "");
        function.setParameters(params);
        value = function.execute(result, null);
        long resultat = Long.parseLong(value);
        LocalDateTime nowFromFunction = LocalDateTime.ofInstant(Instant.ofEpochMilli(resultat),
                TimeZone.getDefault().toZoneId());
        assertThat(nowFromFunction, within(5, ChronoUnit.SECONDS, LocalDateTime.now(ZoneId.systemDefault())));
    }

    @Test
    void testWrongFormatDate() throws Exception {
        Collection<CompoundVariable> params = makeParams("hjfdjyra:fd", "", "P1D", "");
        function.setParameters(params);
        value = function.execute(result, null);
        assertThat(value, is(equalTo("")));
    }

    @Test
    void testRandomPeriod() throws Exception {
        Random r = new Random();
        int randomInt = r.ints(1, 60).limit(1).findFirst().getAsInt();
        vars.put("random", String.valueOf(randomInt));
        Collection<CompoundVariable> params = makeParams("yyyy-MM-dd'T'HH:mm:ss", "", "PT${random}M", "");
        function.setParameters(params);
        value = function.execute(result, null);
        LocalDateTime randomFutureDate = LocalDateTime.parse(value);
        LocalDateTime checkFutureDate = LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(randomInt);
        assertThat(randomFutureDate, within(5, ChronoUnit.SECONDS, checkFutureDate));
        randomInt = r.ints(1, 60).limit(1).findFirst().getAsInt();
        vars.put("random", String.valueOf(randomInt));
        value = function.execute(result, null);
        randomFutureDate = LocalDateTime.parse(value);
        checkFutureDate = LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(randomInt);
        assertThat(randomFutureDate, within(5, ChronoUnit.SECONDS, checkFutureDate));
    }

    @Test
    void testNowPlusOneDayWithLocale() throws Exception {
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
