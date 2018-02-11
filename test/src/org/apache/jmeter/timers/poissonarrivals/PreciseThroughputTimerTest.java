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

package org.apache.jmeter.timers.poissonarrivals;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreciseThroughputTimerTest {
    private static final Logger LOG = LoggerFactory.getLogger(PreciseThroughputTimerTest.class);

    @Test
    public void testTimer1() throws Exception {
        ConstantPoissonProcessGenerator gen = getConstantPoissonProcessGenerator(2, 5, 42L);
        gen.generateNext();
        assertEquals(0.6501751901910952, gen.next(), 0.01);
        assertEquals(1.2249545461599474, gen.next(), 0.01);
        assertEquals(1.409559315928937, gen.next(), 0.01);
        assertEquals(1.5717866281130652, gen.next(), 0.01);
        assertEquals(2.1194190047658874, gen.next(), 0.01);
        assertEquals(3.2878637366551384, gen.next(), 0.01);
        assertEquals(3.517916456559849, gen.next(), 0.01);
        assertEquals(3.679224444929692, gen.next(), 0.01);
        assertEquals(3.9907119513763165, gen.next(), 0.01);
        assertEquals(4.754414649148714, gen.next(), 0.01);
        // ^^ 10 samples for 5 seconds
        assertEquals(6.013095167372755, gen.next(), 0.01);
    }

    @Test
    public void testExactNumberOfSamples() throws Exception {
        java.util.Random rnd = new java.util.Random();
        long seed = rnd.nextLong();
        // Log seed, so the test can be reproduced in case of failure
        LOG.info("testExactNumberOfSamples is using seed " + seed);
        rnd.setSeed(seed);

        int testDuration = 5;
        for (int i = 0; i < 1000; i++) {
            ConstantPoissonProcessGenerator gen = getConstantPoissonProcessGenerator(2, testDuration, rnd.nextLong());
            gen.generateNext();
            for (int j = 0; j < 10; j++) {
                double next = gen.next();
                assertTrue("Delay #" + j + " (0-based) exceeds " + testDuration + " seconds", next < 5.0);
            }
        }
    }

    protected ConstantPoissonProcessGenerator getConstantPoissonProcessGenerator(
            final double throughput, final int duration, long seed) {
        return new ConstantPoissonProcessGenerator(
                new ThroughputProvider() {
                    @Override
                    public double getThroughput() {
                        return throughput; // samples per second
                    }
                },
                1,
                0,
                new DurationProvider() {
                    @Override
                    public long getDuration() {
                        return duration; // "expected" test duration: 3 seconds
                    }
                },
                10000,
                0.1,
                seed, // Seed
                false
        );
    }

}
