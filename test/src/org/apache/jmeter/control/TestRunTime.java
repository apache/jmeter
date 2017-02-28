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

package org.apache.jmeter.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.samplers.Sampler;
import org.junit.Test;

/**
 */
public class TestRunTime extends JMeterTestCase {

    @Test
    public void testProcessing() throws Exception {

        RunTime controller = new RunTime();
        controller.setRuntime(10);
        TestSampler samp1 = new TestSampler("Sample 1", 500);
        TestSampler samp2 = new TestSampler("Sample 2", 480);

        LoopController sub1 = new LoopController();
        sub1.setLoops(2);
        sub1.setContinueForever(false);
        sub1.addTestElement(samp1);

        LoopController sub2 = new LoopController();
        sub2.setLoops(40);
        sub2.setContinueForever(false);
        sub2.addTestElement(samp2);
        controller.addTestElement(sub1);
        controller.addTestElement(sub2);
        controller.setRunningVersion(true);
        sub1.setRunningVersion(true);
        sub2.setRunningVersion(true);
        controller.initialize();
        Sampler sampler = null;
        int loops = 0;
        long now = System.currentTimeMillis();
        while ((sampler = controller.next()) != null) {
            loops++;
            sampler.sample(null);
        }
        long elapsed = System.currentTimeMillis() - now;
        assertTrue("Should be at least 20 loops " + loops, loops >= 20);
        assertTrue("Should be fewer than 30 loops " + loops, loops < 30);
        assertTrue("Should take at least 10 seconds " + elapsed, elapsed >= 10000);
        assertTrue("Should take less than 12 seconds " + elapsed, elapsed <= 12000);
        assertEquals("Sampler 1 should run 2 times", 2, samp1.getSamples());
        assertTrue("Sampler 2 should run >= 18 times", samp2.getSamples() >= 18);
    }
}
