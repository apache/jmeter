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

package org.apache.jmeter.timers.poissonarrivals;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class PreciseThroughputTimerTest {

    @Test
    public void testTimer1() throws Exception {
        // The case is to produce 2 samples per second, and we specify "test duration to be 5 seconds"
        // It means every 5 seconds the sampler must produce 2*5=10 samples
        // However, we generate 31 samples (to ensure generator overflows properly)
        final int throughput = 2;
        final int duration = 5;
        final long seed = 42L;
        final int batchSize = 1;
        ConstantPoissonProcessGenerator gen = getConstantPoissonProcessGenerator(throughput, duration, seed, batchSize);
        gen.generateNext();
        double[] expected = new double[] {
                1.3787403472085118, 1.3853924503706834,
                1.5435972766632988, 1.8439145670565282,
                2.3182678790457665, 3.327744758972868,
                3.416117358799227, 3.6378184001643405,
                3.914508893950179, 4.516861323360891,
                // ^^ 10 samples for 5 seconds
                5.861089688439262, 5.886892389546892,
                6.04883784433166, 6.932834371796743,
                7.182454872116432, 7.937136908931478,
                7.9717495544484205, 8.749530906277236,
                9.129829359439105, 9.596638914343584,
                // ^^ 10 samples for the next 5 seconds
                10.157091194132905, 11.789599597385642,
                12.088437733764593, 12.855201742074335,
                12.900124422510304, 13.567031289116144,
                13.7564020338373, 13.762549742953254,
                14.088984654178198, 14.870178407479408,
                // ^^ 10 samples for the next 5 seconds
                16.45828248705902
                // ^^ it should exceed 15
        };
        double[] actual = new double[expected.length];
        boolean ok = true;
        for (int i = 0; i < actual.length; i++) {
            actual[i] = gen.next();
            ok = ok && Math.abs(actual[i] - expected[i]) < 0.01;
        }

        if (!ok) {
            assertEquals(
                    "Schedule does not match expectation, " +
                            "throughput=" + throughput + ", duration=" + duration +
                            "seed=" + seed + ", batchSize=" + batchSize,
                    Arrays.toString(expected), Arrays.toString(actual)
            );
        }
    }

    @Test
    public void testExactNumberOfSamples() throws Exception {
        Random rnd = new Random();
        for (int i = 0; i < 100; i++) {
            long seed = rnd.nextLong();
            final int testDuration = rnd.nextInt(100) + 5;
            final int throughput = rnd.nextInt(40) + 1;
            final int throughputInterval = rnd.nextInt(100) + 1;
            verifyExactThroughput(seed, testDuration, throughput, throughputInterval);
        }
    }

    @Test
    public void testSingleExactNumberOfSamples() throws Exception {
        long seed = 6217980321110818258L;
        verifyExactThroughput(seed, 60, 5, 60);
    }

    @Test
    public void testSingleExactNumberOfSamples6812190053835844998() throws Exception {
        long seed = 6812190053835844998L;
        verifyExactThroughput(seed, 60, 5, 60);
    }

    private void verifyExactThroughput(
            long seed, int testDuration, int throughput, int throughputInterval) {
        for (int batchSize = 1; batchSize < 3; batchSize++) {
            verifyExactThroughput(seed, testDuration, throughput, throughputInterval, batchSize);
        }
    }

    @Test
    public void repro2110188512211996814L() {
        verifyExactThroughput(2110188512211996814L, 27, 1, 85, 1);
    }

    @Test
    public void reproduer4389853422207095555() {
        verifyExactThroughput(/*seed=*/ 4389853422207095555L, /*testDuration=*/ 21, /*throughput=*/ 26, /*throughputInterval=*/ 79, /*batchSize=*/ 1);
    }

    private void verifyExactThroughput(
            long seed, int testDuration, int throughput, int throughputInterval, int batchSize) {
        // 5 per second, and we specify the test duration as 1 second
        // The generator would prepare the input data for 1 second (~5 samples)
        // However, then we continue sampling, and the generator should still produce
        // exactly 5 items for each new second
        ConstantPoissonProcessGenerator gen =
                getConstantPoissonProcessGenerator(
                        throughput * batchSize * 1.0 / throughputInterval, testDuration, seed, batchSize);
        int samplesPerTest = (int) Math.ceil(throughput * 1.0 / throughputInterval * testDuration) * batchSize;
        try {
            ArrayList<Double> delays = new ArrayList<>();
            // The test will last 100 times longer than expected, so generator would have to re-generate
            // values 100 times or so
            double prev = 0;
            for (int time = 0; time < 100; time++) {
                for (int i = 0; i < samplesPerTest; i++) {
                    double next = gen.next();
                    if (prev > next) {
                        fail(
                                "Schedule should be monotonic, so each new event comes later. " +
                                        "prev: " + prev + ", next: " + next +
                                        ". Full schedule so far: " + delays
                        );
                    }
                    prev = next;
                    delays.add(next);

                    if (time * testDuration <= next && next < (time + 1) * testDuration) {
                        // OK
                        continue;
                    }
                    fail(
                            "Throughput violation at second #" + time + ". Event #" + delays.size() +
                                    " is scheduled at " + next + ", however it should be " +
                                    " between " + time * testDuration + " and " + (time + 1) * testDuration +
                                    ". Full schedule so far: " + delays
                    );

                }
            }
        } catch (Throwable t) {
            // This adds reproducer right into the stacktrace
            final String seedHex = Long.toUnsignedString(seed);
            t.addSuppressed(new Throwable(
                    "@Test public void reproduer" + seedHex + "() {" +
                    "verifyExactThroughput(/*seed=*/ " + seed + "L, /*testDuration=*/ " + testDuration +
                            ", /*throughput=*/ " + throughput + ", /*throughputInterval=*/ " + throughputInterval +
                            ", /*batchSize=*/ " + batchSize + "); }")
            );
            throw t;
        }
    }

    protected ConstantPoissonProcessGenerator getConstantPoissonProcessGenerator(
            final double throughput, final int duration, long seed, int batchSize) {
        return new ConstantPoissonProcessGenerator(
                () -> throughput, // samples per second
                batchSize,
                0,
                () -> duration, // "expected" test duration: 3 seconds
                seed, // Seed
                false
        );
    }

}
