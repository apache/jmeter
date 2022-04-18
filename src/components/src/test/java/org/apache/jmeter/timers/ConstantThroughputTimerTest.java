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

package org.apache.jmeter.timers;

import java.util.UUID;

import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.TestJMeterContextService;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.ScriptingTestElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class ConstantThroughputTimerTest {

    @Test
    void testTimer1() throws Exception {
        ConstantThroughputTimer timer = new ConstantThroughputTimer();
        Assertions.assertEquals(0, timer.getCalcMode());// Assume this thread only
        timer.setThroughput(60.0);// 1 per second
        long start = System.currentTimeMillis();
        long delay = timer.delay(); // Initialise
        Assertions.assertEquals(0, delay);
        // The test tries to check if the calculated delay is correct.
        // If the build machine is busy, then the sleep(500) may take longer in
        // which case the calculated delay should be shorter.
        // Since the test aims at 1 per second, the delay should be 1000 - elapsed.
        // The test currently assumes elapsed is 500.

        Thread.sleep(500);
        long elapsed = System.currentTimeMillis() - start;
        long expected = 1000-elapsed; // 1 second less what has already elapsed
        if (expected < 0) {
            expected = 0;
        }
        assertAlmostEquals(expected, timer.delay(), 50);
    }

    private static void assertAlmostEquals(long expected, long actual, long delta) {
        long actualDelta = Math.abs(actual - expected);
        if (actualDelta > delta) {
            Assertions.fail(() -> "Expected " + expected + " within delta of " + delta
                    + ", but got " + actual + " which is a delta of " + actualDelta);
        }
    }
    @Test
    void testTimer2() throws Exception {
        ConstantThroughputTimer timer = new ConstantThroughputTimer();
        Assertions.assertEquals(0, timer.getCalcMode());// Assume this thread only
        timer.setThroughput(60.0);// 1 per second
        Assertions.assertEquals(1000, timer.calculateCurrentTarget(0)); // Should delay for 1 second
        timer.setThroughput(60000.0);// 1 per milli-second
        Assertions.assertEquals(1, timer.calculateCurrentTarget(0)); // Should delay for 1 milli-second
    }

    @Test
    void testTimer3() throws Exception {
        ConstantThroughputTimer timer = new ConstantThroughputTimer();
        timer.setMode(ConstantThroughputTimer.Mode.AllActiveThreads); //$NON-NLS-1$ - all threads
        Assertions.assertEquals(1, timer.getCalcMode());// All threads
        for(int i=1; i<=10; i++){
            TestJMeterContextService.incrNumberOfThreads();
        }
        Assertions.assertEquals(10, JMeterContextService.getNumberOfThreads());
        timer.setThroughput(600.0);// 10 per second
        Assertions.assertEquals(1000, timer.calculateCurrentTarget(0)); // Should delay for 1 second
        timer.setThroughput(600000.0);// 10 per milli-second
        Assertions.assertEquals(1, timer.calculateCurrentTarget(0)); // Should delay for 1 milli-second
        for(int i=1; i<=990; i++){
            TestJMeterContextService.incrNumberOfThreads();
        }
        Assertions.assertEquals(1000, JMeterContextService.getNumberOfThreads());
        timer.setThroughput(60000000.0);// 1000 per milli-second
        Assertions.assertEquals(1, timer.calculateCurrentTarget(0)); // Should delay for 1 milli-second
    }

    @Test
    void testTimerBSH() throws Exception {
        Assumptions.assumeTrue(BeanShellInterpreter.isInterpreterPresent(),
                "BeanShell jar should be on the classpath, otherwise the test makes no sense");
        BeanShellTimer timer = new BeanShellTimer();

        timer.setScript("\"60\"");
        Assertions.assertEquals(60, timer.delay());

        timer.setScript("60");
        Assertions.assertEquals(60, timer.delay());

        timer.setScript("5*3*4");
        Assertions.assertEquals(60, timer.delay());
    }

    @Test
    void testTimerJSR223Timer() throws Exception {
        JSR223Timer timer = new JSR223Timer();
        timer.setScriptLanguage(ScriptingTestElement.DEFAULT_SCRIPT_LANGUAGE);
        timer.setCacheKey(UUID.randomUUID().toString());

        timer.setScript("\"60\"");
        Assertions.assertEquals(60, timer.delay());

        timer.setScript("60");
        Assertions.assertEquals(60, timer.delay());

        timer.setScript("5*3*4");
        Assertions.assertEquals(60, timer.delay());
    }

    @Test
    void testUniformRandomTimer() throws Exception {
        UniformRandomTimer timer = new UniformRandomTimer();
        timer.setDelay("1000");
        timer.setRange(100d);
        timer.iterationStart(null);
        long delay = timer.delay();
        assertBetween(1000, 1100, delay);
    }

    private static void assertBetween(long expectedLow, long expectedHigh, long actual) {
        if (actual < expectedLow || actual > expectedHigh) {
            Assertions.fail(() -> "delay not in expected range: expected " + actual
                    + " to be within " + expectedLow + " and " + expectedHigh);
        }
    }
    @Test
    void testConstantTimer() throws Exception {
        ConstantTimer timer = new ConstantTimer();
        timer.setDelay("1000");
        timer.iterationStart(null);
        Assertions.assertEquals(1000, timer.delay());
    }

    @Test
    void testPoissonRandomTimerRangeHigherThan30() throws Exception {
        PoissonRandomTimer timer = new PoissonRandomTimer();
        timer.setDelay("300");
        timer.setRange(100d);
        timer.iterationStart(null);
        long delay = timer.delay();
        assertBetween(356, 457, delay);
    }

    @Test
    void testPoissonRandomTimerRangeLowerThan30() throws Exception {
        PoissonRandomTimer timer = new PoissonRandomTimer();
        timer.setDelay("300");
        timer.setRange(30d);
        timer.iterationStart(null);
        long delay = timer.delay();
        assertBetween(305, 362, delay);
    }
}
