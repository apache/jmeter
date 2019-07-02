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

package org.apache.jmeter.timers;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.TestJMeterContextService;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.ScriptingTestElement;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstantThroughputTimerTest {

    private static final Logger log = LoggerFactory.getLogger(ConstantThroughputTimerTest.class);

    @Test
    public void testTimer1() throws Exception {
        ConstantThroughputTimer timer = new ConstantThroughputTimer();
        assertEquals(0,timer.getCalcMode());// Assume this thread only
        timer.setThroughput(60.0);// 1 per second
        long start = System.currentTimeMillis();
        long delay = timer.delay(); // Initialise
        assertEquals(0,delay);
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
        assertEquals("Expected delay of approx 500", expected, timer.delay(), 50);
    }

    @Test
    public void testTimer2() throws Exception {
        ConstantThroughputTimer timer = new ConstantThroughputTimer();
        assertEquals(0,timer.getCalcMode());// Assume this thread only
        timer.setThroughput(60.0);// 1 per second
        assertEquals(1000,timer.calculateCurrentTarget(0)); // Should delay for 1 second
        timer.setThroughput(60000.0);// 1 per milli-second
        assertEquals(1,timer.calculateCurrentTarget(0)); // Should delay for 1 milli-second
    }

    @Test
    public void testTimer3() throws Exception {
        ConstantThroughputTimer timer = new ConstantThroughputTimer();
        timer.setMode(ConstantThroughputTimer.Mode.AllActiveThreads); //$NON-NLS-1$ - all threads
        assertEquals(1,timer.getCalcMode());// All threads
        for(int i=1; i<=10; i++){
            TestJMeterContextService.incrNumberOfThreads();
        }
        assertEquals(10,JMeterContextService.getNumberOfThreads());
        timer.setThroughput(600.0);// 10 per second
        assertEquals(1000,timer.calculateCurrentTarget(0)); // Should delay for 1 second
        timer.setThroughput(600000.0);// 10 per milli-second
        assertEquals(1,timer.calculateCurrentTarget(0)); // Should delay for 1 milli-second
        for(int i=1; i<=990; i++){
            TestJMeterContextService.incrNumberOfThreads();
        }
        assertEquals(1000,JMeterContextService.getNumberOfThreads());
        timer.setThroughput(60000000.0);// 1000 per milli-second
        assertEquals(1,timer.calculateCurrentTarget(0)); // Should delay for 1 milli-second
    }

    @Test
    public void testTimerBSH() throws Exception {
        Assume.assumeTrue("BeanShell jar should be on the classpath, otherwise the test makes no sense",
                BeanShellInterpreter.isInterpreterPresent());
        BeanShellTimer timer = new BeanShellTimer();

        timer.setScript("\"60\"");
        assertEquals(60, timer.delay());

        timer.setScript("60");
        assertEquals(60, timer.delay());

        timer.setScript("5*3*4");
        assertEquals(60,timer.delay());
    }

    @Test
    public void testTimerJSR223Timer() throws Exception {
        JSR223Timer timer = new JSR223Timer();
        timer.setScriptLanguage(ScriptingTestElement.DEFAULT_SCRIPT_LANGUAGE);
        timer.setCacheKey(UUID.randomUUID().toString());

        timer.setScript("\"60\"");
        assertEquals(60, timer.delay());

        timer.setScript("60");
        assertEquals(60, timer.delay());

        timer.setScript("5*3*4");
        assertEquals(60,timer.delay());
    }

    @Test
    public void testUniformRandomTimer() throws Exception {
        UniformRandomTimer timer = new UniformRandomTimer();
        timer.setDelay("1000");
        timer.setRange(100d);
        timer.iterationStart(null);
        long delay = timer.delay();
        Assert.assertTrue("delay:"+delay +" is not in expected range", delay >= 1000 && delay <=1100);
    }

    @Test
    public void testConstantTimer() throws Exception {
        ConstantTimer timer = new ConstantTimer();
        timer.setDelay("1000");
        timer.iterationStart(null);
        assertEquals(1000, timer.delay());
    }

    @Test
    public void testPoissonRandomTimerRangeHigherThan30() throws Exception {
        PoissonRandomTimer timer = new PoissonRandomTimer();
        timer.setDelay("300");
        timer.setRange(100d);
        timer.iterationStart(null);
        long delay = timer.delay();
        Assert.assertTrue("delay:"+delay +" is not in expected range", delay >= 356 && delay <=457);
    }

    @Test
    public void testPoissonRandomTimerRangeLowerThan30() throws Exception {
        PoissonRandomTimer timer = new PoissonRandomTimer();
        timer.setDelay("300");
        timer.setRange(30d);
        timer.iterationStart(null);
        long delay = timer.delay();
        Assert.assertTrue("delay:"+delay +" is not in expected range", delay >= 305 && delay <=362);
    }
}
